@echo off

set jre=java
set sws=%~f0
set opt=-XX:+UseG1GC -XX:+UseStringDeduplication -XX:+DisableExplicitGC -XX:+UseCompressedOops -XX:+OptimizeStringConcat

"%jre%" %opt% -jar "%sws%" %*

exit /b %ERRORLEVEL%
