#!/usr/bin/env bash
# Kompilasi aplikasi desktop dan buat out/rtrw-desktop.jar (hanya butuh JDK 11+).
set -euo pipefail
cd "$(dirname "$0")"

rm -rf out/classes
mkdir -p out/classes
find src/main/java -name '*.java' > out/sources.txt
javac --release 11 -encoding UTF-8 -d out/classes @out/sources.txt
jar --create --file out/rtrw-desktop.jar --main-class id.rtrw.desktop.Main -C out/classes .

echo "Selesai: out/rtrw-desktop.jar"
echo "Jalankan: ./run.sh   atau   java -jar out/rtrw-desktop.jar"
