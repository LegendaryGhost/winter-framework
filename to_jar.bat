@echo off
setlocal enabledelayedexpansion

set "work_dir=.\"
set "src=%work_dir%\src"
set "lib=%work_dir%\lib"
set "bin=%work_dir%\classes"
set "jar_name=winter-framework"
set "jar_path=%work_dir%\%jar_name%.jar"

if exist "%bin%" (
    rd /s /q "%bin%"
)

:: Java files compilation
dir /s /B "%src%\*.java" > sources.txt
javac -d "%bin%" -cp "%lib%\*" @sources.txt
del sources.txt

:: Jar packaging
echo Packaging %jar_name%.jar...
jar cf "%jar_path%" -C "%bin%" .

echo JAR packaging completed
pause
