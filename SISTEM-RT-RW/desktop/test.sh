#!/usr/bin/env bash
# Menjalankan pengujian otomatis (JSON, parser respons, klien API vs API tiruan).
set -euo pipefail
cd "$(dirname "$0")"

rm -rf out/test-classes
mkdir -p out/test-classes
find src/main/java src/dev/java src/test/java -name '*.java' > out/test-sources.txt
javac --release 11 -encoding UTF-8 -d out/test-classes @out/test-sources.txt
java -cp out/test-classes id.rtrw.desktop.TestRunner
