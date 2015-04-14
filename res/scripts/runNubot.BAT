@ECHO OFF

:checkprocess
tasklist | find "notepad.exe" > nul

if %ERRORLEVEL% == 0 (

    echo found notepad
)

if %ERRORLEVEL% == 1 (

    echo did not find notepad

	exit
)

timeout /t 2 /nobreak > nul

goto checkprocess
