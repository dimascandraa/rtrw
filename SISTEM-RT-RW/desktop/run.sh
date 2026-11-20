#!/usr/bin/env bash
# Menjalankan aplikasi desktop. Opsi: ./run.sh --url=http://alamat-server
set -euo pipefail
cd "$(dirname "$0")"
[ -f out/rtrw-desktop.jar ] || ./build.sh
exec java -jar out/rtrw-desktop.jar "$@"
 