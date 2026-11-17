#!/usr/bin/env bash
# Jalankan dari folder desktop/ :  ./run.sh
set -e
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out app.Main
