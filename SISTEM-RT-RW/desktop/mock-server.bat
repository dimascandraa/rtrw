@echo off
rem API tiruan (data dummy) untuk mencoba desktop sebelum backend siap.
rem Akun uji: admin@rtrw.test / admin123. Pemakaian: mock-server.bat [port]
cd /d "%~dp0"
if exist out\dev-classes rmdir /s /q out\dev-classes
mkdir out\dev-classes
javac --release 11 -encoding UTF-8 -d out\dev-classes src\main\java\id\rtrw\desktop\*.java src\main\java\id\rtrw\desktop\api\*.java src\main\java\id\rtrw\desktop\config\*.java src\main\java\id\rtrw\desktop\ui\*.java src\main\java\id\rtrw\desktop\util\*.java src\dev\java\id\rtrw\desktop\dev\*.java
if errorlevel 1 exit /b 1
java -cp out\dev-classes id.rtrw.desktop.dev.MockServer %*
