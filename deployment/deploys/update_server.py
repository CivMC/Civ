import sys
from pathlib import Path


sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import ALL_SERVERS
from _common import configure_backups
from _common import configure_docker
from _common import configure_ufw
from _common import deploy_infrastructure_stacks
from _common import deploy_minecraft_stack
from _common import environment
from _common import install_docker
from _common import install_packages
from _common import preflight
from _common import stop_servers


preflight()
setting, secret = environment()

stop_servers(setting, ALL_SERVERS)
install_packages()
install_docker()
configure_backups(secret)
configure_docker()
configure_ufw()
deploy_infrastructure_stacks(setting, secret)
deploy_minecraft_stack(setting, secret, ALL_SERVERS)
