import sys
from pathlib import Path


sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import deploy_minecraft_stack
from _common import environment
from _common import preflight
from _common import stop_servers


SERVERS = ["gamma"]

preflight()
setting, secret = environment()

stop_servers(setting, SERVERS)
deploy_minecraft_stack(setting, secret, SERVERS)
