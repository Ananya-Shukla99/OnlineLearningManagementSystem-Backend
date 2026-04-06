@REM Licensed to the Apache Software Foundation (ASF)

@echo off
setlocal

set ERROR_CODE=0

set MAVEN_JAVA_EXE=%JAVA_HOME%\bin\java.exe
if not exist "%MAVEN_JAVA_EXE%" (
    echo Error: JAVA_HOME is not set and no 'java' command found in PATH.
    exit /b 1
)

for %%i in ("%MAVEN_JAVA_EXE%") do set JAVA_HOME=%%~dpi
set JAVA_HOME=%JAVA_HOME:~0,-1%

set CLASSPATH=%~dp0\.mvn\wrapper\maven-wrapper.jar
set MAVEN_HOME=%~dp0\.mvn
set MAVEN_CMD_LINE_ARGS=%*

"%MAVEN_JAVA_EXE%" %MAVEN_OPTS% -classpath "%CLASSPATH%" "-Dmaven.home=%MAVEN_HOME%" "-Dmaven.multiModuleProjectDirectory=%~dp0." org.apache.maven.wrapper.MavenWrapperMain %MAVEN_CMD_LINE_ARGS%

if %ERRORLEVEL% neq 0 (
    exit /b %ERRORLEVEL%
)

endlocal

