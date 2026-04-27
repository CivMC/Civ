import sys
from pathlib import Path


sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import ALL_SERVERS
from _common import environment
from _common import preflight
from _common import stop_servers


preflight()
setting, _secret = environment()

stop_servers(setting, ALL_SERVERS)
