@echo off
REM ============================================================
REM SmartQA Backend Launcher (Windows)
REM Load env vars from .env, then run the packaged jar on 8081.
REM Usage: double-click, or run "start-backend.bat" in cmd.
REM ============================================================
setlocal EnableDelayedExpansion

cd /d "%~dp0backend"

REM --- Load .env (simple KEY=VALUE parser) ---
if exist "..\.env" (
    for /f "usebackq tokens=1,* delims==" %%a in ("..\.env") do (
        set "line=%%a"
        if not "!line:~0,1!"=="#" (
            if not "%%a"=="" set "%%a=%%b"
        )
    )
)

REM --- Clear WorkBuddy-injected SERVER__PORT to force 8081 ---
set "SERVER__PORT="

REM --- Locate packaged jar ---
set "JAR="
for %%f in (target\*.jar) do set "JAR=%%f"

if not defined JAR (
    echo [ERROR] No packaged jar found under target\. Run "mvn clean package -DskipTests" first.
    pause
    exit /b 1
)

echo [START] Running %JAR% on port 8081 ...
java -jar "%JAR%" --server.port=8081

endlocal
