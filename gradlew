#!/bin/sh
# Gradle wrapper script

APP_HOME="$(cd "$(dirname "$0")" && pwd)"
JAVA_EXE="$JAVA_HOME/bin/java"

exec "$JAVA_EXE" \
  -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain "$@"
