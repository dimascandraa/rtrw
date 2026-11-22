@echo off
rem Menjalankan aplikasi desktop. Opsi: run.bat --url=http://alamat-server
cd /d "%~dp0"
if not exist out\rtrw-desktop.jar call build.bat
java -jar out\rtrw-desktop.jar %*
