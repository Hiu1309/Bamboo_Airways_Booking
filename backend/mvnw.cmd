@echo off
setlocal
set "WRAPPER_DIR=%~sdp0.mvn\wrapper"
java -Dmaven.multiModuleProjectDirectory="%~sdp0." -cp "%WRAPPER_DIR%\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
