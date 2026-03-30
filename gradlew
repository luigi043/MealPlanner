#!/bin/sh
#
# Gradle wrapper script for Unix/Mac
#

# Resolve the script's directory
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Set the Gradle user home
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"

# Determine the Java executable
if [ -n "$JAVA_HOME" ]; then
    JAVA_EXE="$JAVA_HOME/bin/java"
else
    JAVA_EXE="java"
fi

# Classpath for the wrapper jar
WRAPPER_JAR="$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_PROPERTIES="$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.properties"

exec "$JAVA_EXE" \
    -classpath "$WRAPPER_JAR" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
