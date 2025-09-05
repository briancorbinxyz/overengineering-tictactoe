#!/usr/bin/env sh
# Simple static file server using JDK's built-in jwebserver (JEP 408)
# Usage:
#   app/scripts/serve.sh [port] [directory]
# Defaults:
#   port: 8000
#   directory: project root (serves index.html if present)

PORT="${1:-8000}"
DIR="${2:-$(git rev-parse --show-toplevel 2>/dev/null || pwd)}"

echo "Serving ${DIR} at http://localhost:${PORT} (Ctrl+C to stop)"
# Note: jwebserver is available in JDK 18+
exec jwebserver --directory "${DIR}" --port "${PORT}"
