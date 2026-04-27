from __future__ import annotations

from lib.civ_deploy import configure_backups
from lib.civ_deploy import configure_docker
from lib.civ_deploy import configure_ufw
from lib.civ_deploy import deploy_infrastructure_stacks
from lib.civ_deploy import deploy_minecraft_stack
from lib.civ_deploy import environment
from lib.civ_deploy import install_docker
from lib.civ_deploy import install_packages
from lib.civ_deploy import ping
from lib.civ_deploy import preflight
from lib.civ_deploy import start_kira
from lib.civ_deploy import start_servers
from lib.civ_deploy import stop_kira
from lib.civ_deploy import stop_servers


ALL_SERVERS = ["pvp", "paper", "gamma", "proxy"]
