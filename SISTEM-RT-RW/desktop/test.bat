@echo off
rem Menjalankan pengujian otomatis.
cd /d "%~dp0"
if exist out\test-classes rmdir /s /q out\test-classes
mkdir out\test-classes
javac --release 11 -encoding UTF-8 -d out\test-classes src\main\java\id\rtrw\desktop\*.java src\main\java\id\rtrw\desktop\api\*.java src\main\java\id\rtrw\desktop\config\*.java src\main\java\id\rtrw\desktop\ui\*.java src\main\java\id\rtrw\desktop\util\*.java src\dev\java\id\rtrw\desktop\dev\*.java src\test\java\id\rtrw\desktop\*.java
if errorlevel 1 exit /b 1
java -cp out\test-classes id.rtrw.desktop.TestRunner
