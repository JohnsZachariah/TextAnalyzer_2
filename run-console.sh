#!/usr/bin/env bash
set -e

# Automatically detect JDK home
if [ -z "$JAVA_HOME" ] || [ ! -x "$JAVA_HOME/bin/java" ]; then
    if [ -x "/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/bin/java" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
    elif [ -x "/Library/Java/JavaVirtualMachines/jdk-20.jdk/Contents/Home/bin/java" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-20.jdk/Contents/Home"
    elif [ -x "/Library/Java/JavaVirtualMachines/zulu-25.jdk/Contents/Home/bin/java" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/zulu-25.jdk/Contents/Home"
    fi
fi

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
    JAVAC_CMD="$JAVA_HOME/bin/javac"
else
    JAVA_CMD="java"
    JAVAC_CMD="javac"
fi

# Ensure compiled
if [ ! -d "bin" ] || [ -z "$(ls -A bin 2>/dev/null)" ]; then
    echo "==> Compiling source files..."
    mkdir -p bin
    mkdir -p lib
    "$JAVAC_CMD" -cp "lib/*:src:." -d bin $(find src -name "*.java")
fi

mkdir -p data/reports

echo "==> Launching Word Counter & Text Analyzer Console Mode..."
"$JAVA_CMD" -cp "bin:lib/*:." com.textanalyzer.Main --cli
