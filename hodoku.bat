@echo off
rem Lanzador sin terminal (milestone 1.2): doble clic -> builda si hace falta y
rem abre la GUI. Requiere Java en el PATH (el mismo que usa gradlew).
cd /d "%~dp0"
call "%~dp0gradlew.bat" -q jar
if errorlevel 1 (
    echo.
    echo El build fallo: revisar los mensajes de arriba.
    pause
    exit /b 1
)
set "JAR="
for %%f in ("build\libs\hodoku-*.jar") do set "JAR=%%~ff"
if not defined JAR (
    echo No se encontro el jar en build\libs.
    pause
    exit /b 1
)
start "HoDoKu" javaw -jar "%JAR%"
