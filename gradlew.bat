@echo off
setlocal

set APP_NAME=Gradle
set APP_BASE_NAME=%~n0
set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_HOME=%DIRNAME%

rem ✅ FIXED JVM OPTIONS
set DEFAULT_JVM_OPTS=-Xmx64m -Xms64m

if defined JAVA_HOME (
    set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
    set JAVA_EXE=java
)

where %JAVA_EXE% >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java not found. Install Java 21.
    exit /b 1
)

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

"%JAVA_EXE%" ^
 %DEFAULT_JVM_OPTS% ^
 %JAVA_OPTS% ^
 %GRADLE_OPTS% ^
 -classpath "%CLASSPATH%" ^
 org.gradle.wrapper.GradleWrapperMain %*

endlocal
