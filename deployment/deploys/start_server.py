import sys
from pathlib import Path


sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import environment
from _common import preflight
from _common import start_servers


preflight()
setting, _secret = environment()

start_servers(setting)
