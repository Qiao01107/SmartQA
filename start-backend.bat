@echo off
REM ============================================================
REM SmartQA 后端启动脚本（Windows）
REM 从 .env 加载环境变量后启动 Spring Boot（端口 8081）
REM ============================================================
setlocal EnableDelayedExpansion

cd /d "%~dp0backend"

REM 加载 .env（简单解析 KEY=VALUE）
if exist "..\.env" (
    for /f "usebackq tokens=1,* delims==" %%a in ("..\.env") do (
        set "line=%%a"
        if not "!line:~0,1!"=="#" (
            if not "%%a"=="" set "%%a=%%b"
        )
    )
)

REM 移除 WorkBuddy 注入的 SERVER__PORT 干扰，强制使用 8081
set "SERVER__PORT="

echo [启动] SmartQA 后端，端口 8081 ...
mvn spring-boot:run

endlocal
