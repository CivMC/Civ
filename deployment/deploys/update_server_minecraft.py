import sys
from pathlib import Path


sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import ALL_SERVERS
from _common import deploy_minecraft_stack
from _common import environment
from _common import preflight
from _common import stop_servers


preflight()
setting, secret = environment()

stop_servers(setting, ALL_SERVERS)
deploy_minecraft_stack(setting, secret, ALL_SERVERS)
