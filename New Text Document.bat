@echo off
setlocal enabledelayedexpansion

echo لیست کامل تمام فایل‌ها و پوشه‌ها:
echo ================================

for /r "%~dp0" %%F in (*) do (
    echo فایل: %%~fF
)

for /r "%~dp0" /d %%D in (*) do (
    echo پوشه: %%~fD
)

echo ================================
echo پایان لیست
pause