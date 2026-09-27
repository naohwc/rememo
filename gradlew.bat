@rem
@rem Gradle 启动脚本（精简版，Windows）：使用 gradle\wrapper\gradle-wrapper.jar 启动 Gradle。
@rem 用法（在工程根目录执行）：
@rem   gradlew.bat assembleDebug
@rem
@echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.

if defined JAVA_HOME (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
  set JAVA_EXE=java
)

"%JAVA_EXE%" -Dorg.gradle.appname=gradlew -classpath "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*

endlocal
