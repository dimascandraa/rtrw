@echo off
rem Kompilasi aplikasi desktop dan buat out\rtrw-desktop.jar (butuh JDK 11+).
cd /d "%~dp0"
if exist out\classes rmdir /s /q out\classes
mkdir out\classes
javac --release 11 -encoding UTF-8 -d out\classes src\main\java\id\rtrw\desktop\*.java src\main\java\id\rtrw\desktop\api\*.java src\main\java\id\rtrw\desktop\config\*.java src\main\java\id\rtrw\desktop\ui\*.java src\main\java\id\rtrw\desktop\util\*.java
if errorlevel 1 exit /b 1
jar --create --file out\rtrw-desktop.jar --main-class id.rtrw.desktop.Main -C out\classes .
if errorlevel 1 exit /b 1
echo Selesai: out\rtrw-desktop.jar
echo Jalankan: run.bat
