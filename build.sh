#!/usr/bin/env bash
set -e

# Automatically detect JDK home
if [ -z "$JAVA_HOME" ] || [ ! -x "$JAVA_HOME/bin/javac" ]; then
    if [ -x "/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/bin/javac" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
    elif [ -x "/Library/Java/JavaVirtualMachines/jdk-20.jdk/Contents/Home/bin/javac" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-20.jdk/Contents/Home"
    elif [ -x "/Library/Java/JavaVirtualMachines/zulu-25.jdk/Contents/Home/bin/javac" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/zulu-25.jdk/Contents/Home"
    fi
fi

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/javac" ]; then
    JAVAC_CMD="$JAVA_HOME/bin/javac"
else
    JAVAC_CMD="javac"
fi

echo "==> Compiling Java sources using: $JAVAC_CMD"
mkdir -p bin
mkdir -p data/reports
mkdir -p lib

"$JAVAC_CMD" -cp "lib/*:src:." -d bin $(find src -name "*.java")

echo "==> Compilation successful! Classes compiled to bin/"
