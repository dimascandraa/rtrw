#!/usr/bin/env bash
# Jalankan dari folder desktop/ :  ./run.sh
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out ./*.java
java -cp out app.Main
