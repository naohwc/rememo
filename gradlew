#!/bin/sh
#
# Gradle 启动脚本（精简版）：使用 gradle/wrapper/gradle-wrapper.jar 启动 Gradle。
# 用法（在工程根目录执行）：
#   ./gradlew assembleDebug
#   ./gradlew installDebug
#
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR" || exit 1

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
else
  JAVA_CMD="java"
fi

exec "$JAVA_CMD" \
  -Dorg.gradle.appname=gradlew \
  -classpath "$DIR/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain "$@"
