import sys
from pathlib import Path


sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import environment
from _common import preflight
from _common import start_kira
from _common import stop_kira


preflight()
setting, _secret = environment()

stop_kira(setting)
start_kira(setting)
