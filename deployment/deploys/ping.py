import sys
from pathlib import Path


sys.path.insert(0, str(Path(__file__).resolve().parent))

from _common import environment
from _common import ping
from _common import preflight


preflight(require_build=False)
environment()
ping()
