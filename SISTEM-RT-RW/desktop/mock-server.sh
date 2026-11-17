#!/usr/bin/env bash
# Menjalankan API tiruan (data dummy) untuk mencoba desktop sebelum backend siap.
# Akun uji: admin@rtrw.test / admin123. Pemakaian: ./mock-server.sh [port]
set -euo pipefail
cd "$(dirname "$0")"

rm -rf out/dev-classes
mkdir -p out/dev-classes
find src/main/java src/dev/java -name '*.java' > out/dev-sources.txt
javac --release 11 -encoding UTF-8 -d out/dev-classes @out/dev-sources.txt
exec java -cp out/dev-classes id.rtrw.desktop.dev.MockServer "$@"
