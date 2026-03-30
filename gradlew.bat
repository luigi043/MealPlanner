@rem Gradle wrapper script for Windows

@if "%DEBUG%"=="" @echo off
@rem Set local scope for variables
setlocal

set SCRIPT_DIR=%~dp0
set WRAPPER_JAR=%SCRIPT_DIR%gradle\wrapper\gradle-wrapper.jar
set WRAPPER_PROPERTIES=%SCRIPT_DIR%gradle\wrapper\gradle-wrapper.properties

if defined JAVA_HOME (
    set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
    set JAVA_EXE=java.exe
)

"%JAVA_EXE%" -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*

endlocal
