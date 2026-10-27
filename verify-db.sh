#!/usr/bin/env bash
set -e

# Detect JDK
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

echo "==> Compiling latest source code..."
mkdir -p bin data/exports data/backups data/reports
"$JAVAC_CMD" -cp "lib/*:src:." -d bin $(find src -name "*.java")

echo "==> Running SQL Database & JDBC Verification Suite..."
"$JAVA_CMD" -cp "bin:lib/*:." com.textanalyzer.test.DatabaseVerificationTest
