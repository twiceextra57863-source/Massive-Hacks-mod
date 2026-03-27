#!/bin/sh

#
# Gradle start up script for POSIX systems.
#

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Resolve symlinks
PRG="$0"
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done

SAVED_PWD=`pwd`
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME=`pwd -P`
cd "$SAVED_PWD" >/dev/null

# ✅ FIXED JVM OPTIONS (NO QUOTE BUG)
DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"

# Java command
if [ -n "$JAVA_HOME" ] ; then
    JAVA_EXEC="$JAVA_HOME/bin/java"
else
    JAVA_EXEC="java"
fi

if ! command -v "$JAVA_EXEC" >/dev/null 2>&1; then
    echo "ERROR: Java not found. Install Java 21."
    exit 1
fi

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

exec "$JAVA_EXEC" \
  $DEFAULT_JVM_OPTS \
  $JAVA_OPTS \
  $GRADLE_OPTS \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain "$@"
