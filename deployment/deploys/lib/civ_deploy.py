from __future__ import annotations

from pathlib import Path
from typing import Any

import yaml
from pyinfra import host
from pyinfra.operations import apt, files, pip, server


DEPLOYMENT_DIR = Path(__file__).resolve().parents[2]
BUILD_DIR = DEPLOYMENT_DIR / "build"
FILES_DIR = DEPLOYMENT_DIR / "files"
TEMPLATES_DIR = DEPLOYMENT_DIR / "templates"
VARIABLES_DIR = DEPLOYMENT_DIR / "variables"


class DotDict(dict):

    def __getattr__(self, key: str) -> Any:
        try:
            return self[key]
        except KeyError as error:
            raise AttributeError(key) from error


def _dot(value: Any) -> Any:
    if isinstance(value, dict):
        return DotDict({key: _dot(nested) for key, nested in value.items()})
    if isinstance(value, list):
        return [_dot(nested) for nested in value]
    return value


def _read_yaml(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as file:
        loaded = yaml.safe_load(file)
    if not isinstance(loaded, dict):
        raise ValueError(f"{path} must contain a YAML mapping")
    return loaded


def preflight(require_build: bool = True) -> None:
    secrets_path = VARIABLES_DIR / "secrets.yml"
    if not secrets_path.exists():
        raise RuntimeError("secrets.yml is missing, please create it by copying variables/secrets.example.yml")
    if require_build and not BUILD_DIR.exists():
        raise RuntimeError("build/ is missing, please create it by running `gradle :deployment:build`")


def environment() -> tuple[DotDict, DotDict]:
    setting_name = host.data.get("setting")
    if not setting_name:
        raise RuntimeError("inventory host is missing required `setting` data")

    settings = _read_yaml(VARIABLES_DIR / "settings.yml")["settings"]
    secrets = _read_yaml(VARIABLES_DIR / "secrets.yml")["secrets"]
    if setting_name not in settings:
        raise RuntimeError(f"settings.yml does not define setting `{setting_name}`")
    if setting_name not in secrets:
        raise RuntimeError(f"secrets.yml does not define setting `{setting_name}`")
    return _dot(settings[setting_name]), _dot(secrets[setting_name])


def is_swarm_manager() -> bool:
    return "swarm_manager" in host.groups


def ssh_user() -> str:
    return host.data.get("ssh_user", "actions")


def install_packages() -> None:
    apt.packages(
        name="Install backup packages",
        packages=["restic"],
        present=True,
        _sudo=True,
    )


def install_docker() -> None:
    apt.packages(
        name="Install Docker repository dependencies",
        packages=["ca-certificates", "curl", "gnupg", "lsb-release", "python3-pip"],
        present=True,
        _sudo=True,
    )
    pip.packages(
        name="Install Docker Python packages",
        packages=["jsondiff", "docker"],
        present=True,
        _sudo=True,
    )
    server.shell(
        name="Install Docker apt repository",
        commands=[
            "install -m 0755 -d /etc/apt/keyrings",
            "curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg",
            "chmod a+r /etc/apt/keyrings/docker.gpg",
            "sh -c 'echo \"deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo $VERSION_CODENAME) stable\" > /etc/apt/sources.list.d/docker.list'",
        ],
        _sudo=True,
    )
    apt.packages(
        name="Install Docker packages",
        packages=["docker-ce", "docker-ce-cli", "containerd.io", "docker-compose-plugin"],
        present=True,
        update=True,
        _sudo=True,
    )
    server.shell(
        name="Install Loki Docker logging plugin",
        commands="docker plugin inspect grafana/loki-docker-driver >/dev/null 2>&1 || docker plugin install --grant-all-permissions grafana/loki-docker-driver:latest",
        _sudo=True,
    )
    server.shell(
        name="Enable Loki Docker logging plugin",
        commands="docker plugin enable grafana/loki-docker-driver || true",
        _sudo=True,
    )


def configure_docker() -> None:
    if not is_swarm_manager():
        return
    server.shell(
        name="Initialize Docker swarm",
        commands="docker info --format '{{.Swarm.LocalNodeState}}' | grep -q active || docker swarm init",
        _sudo=True,
    )
    files.directory(name="Create stacks directory", path="/opt/stacks", present=True, _sudo=True)
    for network in ["traefik-public", "monitoring"]:
        server.shell(
            name=f"Create Docker network {network}",
            commands=f"docker network inspect {network} >/dev/null 2>&1 || docker network create --driver overlay {network}",
            _sudo=True,
        )


def configure_ufw() -> None:
    server.shell(name="Allow SSH through UFW", commands="ufw allow OpenSSH", _sudo=True)
    server.shell(
        name="Protect Loki port from public access",
        commands="iptables -C DOCKER-USER -i ens18 -p tcp --dport 3100 -j REJECT 2>/dev/null || iptables -A DOCKER-USER -i ens18 -p tcp --dport 3100 -j REJECT",
        _sudo=True,
    )
    server.shell(name="Enable UFW reject policy", commands="ufw --force default reject && ufw --force enable", _sudo=True)


def configure_backups(secret: DotDict) -> None:
    if not is_swarm_manager():
        return
    files.directory(name="Create backup directory", path="/opt/backups", present=True, _sudo=True)
    files.template(
        name="Install backup script",
        src=str(TEMPLATES_DIR / "backup-and-restart.sh"),
        dest="/opt/backup-and-restart.sh",
        mode="755",
        secret=secret,
        _sudo=True,
    )
    server.shell(
        name="Install backup cron",
        commands="printf '%s\n' '0 10 * * * root /opt/backup-and-restart.sh' > /etc/cron.d/minecraft",
        _sudo=True,
    )


def deploy_simple_stack(name: str, enabled: bool, volume_paths: list[str], setting: DotDict, secret: DotDict) -> None:
    if not is_swarm_manager() or not enabled:
        return
    stack_dir = f"/opt/stacks/{name}"
    files.directory(name=f"Create {name} stack directory", path=stack_dir, present=True, _sudo=True)
    for volume_path in volume_paths:
        files.directory(name=f"Create {volume_path}", path=volume_path, present=True, _sudo=True)
    files.template(
        name=f"Render {name} stack",
        src=str(TEMPLATES_DIR / "stacks" / f"{name}.yml"),
        dest=f"{stack_dir}/{name}.yml",
        setting=setting,
        secret=secret,
        _sudo=True,
    )
    server.shell(
        name=f"Deploy {name} stack",
        commands=f"docker stack deploy -c {stack_dir}/{name}.yml {name}",
        _sudo=True,
    )


def deploy_infrastructure_stacks(setting: DotDict, secret: DotDict) -> None:
    deploy_simple_stack("traefik", setting.traefik.enabled, ["/opt/stacks/traefik/traefik-certs"], setting, secret)
    deploy_simple_stack("auth", setting.auth.enabled, ["/opt/stacks/auth/postgres-data"], setting, secret)
    deploy_simple_stack("maven", setting.maven.enabled, ["/opt/stacks/maven/nexus-data"], setting, secret)
    deploy_simple_stack("monitoring", setting.monitoring.enabled, ["/opt/stacks/monitoring/grafana-data", "/opt/stacks/monitoring/loki-data"], setting, secret)


def deploy_minecraft_stack(setting: DotDict, secret: DotDict, servers: list[str]) -> None:
    if not is_swarm_manager() or not setting.minecraft.enabled:
        return
    files.directory(name="Create Minecraft stack directory", path="/opt/stacks/minecraft", present=True, _sudo=True)
    files.directory(name="Create user Minecraft staging directory", path="~/stacks/minecraft", present=True)
    volume_paths = ["/opt/stacks/minecraft/mariadb-data", "/opt/stacks/minecraft/postgres-data"]
    if "paper" in servers:
        volume_paths.append("/opt/stacks/minecraft/paper-data")
    if "pvp" in servers:
        volume_paths.append("/opt/stacks/minecraft/pvp-data")
    if "gamma" in servers:
        volume_paths.append("/opt/stacks/minecraft/gamma-data")
    for volume_path in volume_paths:
        files.directory(name=f"Create {volume_path}", path=volume_path, present=True, _sudo=True)
    files.template(
        name="Render Minecraft stack",
        src=str(TEMPLATES_DIR / "stacks" / "minecraft.yml.j2"),
        dest="/opt/stacks/minecraft/minecraft.yml",
        setting=setting,
        secret=secret,
        _sudo=True,
    )
    for server_name in servers:
        _sync_minecraft_dir(f"{server_name}-config", FILES_DIR / f"{server_name}-config")
        _sync_minecraft_dir(f"{server_name}-plugins", BUILD_DIR / f"{server_name}-plugins")
    server.shell(name="Deploy Minecraft stack", commands="docker stack deploy -c /opt/stacks/minecraft/minecraft.yml minecraft", _sudo=True)


def _sync_minecraft_dir(remote_name: str, local_path: Path) -> None:
    files.sync(
        name=f"Sync {remote_name}",
        src=str(local_path),
        dest="~/stacks/minecraft",
        delete=True,
    )
    server.shell(
        name=f"Install {remote_name}",
        commands=f"cp -r /home/{ssh_user()}/stacks/minecraft/{remote_name} /opt/stacks/minecraft/{remote_name}",
        _sudo=True,
    )


def stop_servers(setting: DotDict, servers: list[str]) -> None:
    if not is_swarm_manager() or not setting.minecraft.enabled:
        return
    service_names = {
        "proxy": "minecraft_waterfall",
        "paper": "minecraft_paper",
        "gamma": "minecraft_gamma",
        "pvp": "minecraft_pvp",
    }
    for server_name in servers:
        server.shell(
            name=f"Stop {server_name}",
            commands=f"docker service scale {service_names[server_name]}=0 || true",
            _sudo=True,
        )


def start_servers(setting: DotDict) -> None:
    if not is_swarm_manager() or not setting.minecraft.enabled:
        return
    for service_name in ["minecraft_paper", "minecraft_gamma", "minecraft_pvp", "minecraft_waterfall"]:
        server.shell(name=f"Start {service_name}", commands=f"docker service scale {service_name}=1", _sudo=True)


def stop_kira(setting: DotDict) -> None:
    if is_swarm_manager() and setting.minecraft.enabled:
        server.shell(name="Stop Kira", commands="docker service scale minecraft_kira=0", _sudo=True)


def start_kira(setting: DotDict) -> None:
    if is_swarm_manager() and setting.minecraft.enabled:
        server.shell(name="Start Kira", commands="docker service scale minecraft_kira=1", _sudo=True)


def ping() -> None:
    server.shell(name="Ping host", commands="true")
