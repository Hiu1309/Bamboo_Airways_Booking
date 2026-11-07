@echo off
setlocal
set WRAPPER_DIR=%~dp0.mvn\wrapper
java -Dmaven.multiModuleProjectDirectory=%~dp0 -cp "%WRAPPER_DIR%\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
