@echo off
rem Maven Wrapper script pointing to the local Maven bundled in tools
set MAVEN_BIN=%~dp0..\tools\apache-maven-3.9.9\bin\mvn.cmd
if exist "%MAVEN_BIN%" (
    call "%MAVEN_BIN%" %*
) else (
    mvn %*
)
