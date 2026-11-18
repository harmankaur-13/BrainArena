#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

# 1. Locate Java 17 binaries
JAVA_BIN="java"
JAVAC_BIN="javac"
if [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin" ]; then
    JAVA_BIN="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/java"
    JAVAC_BIN="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/javac"
fi

# 2. Compile sources and copy SQL resources into bin/
mkdir -p bin
"$JAVAC_BIN" -d bin -cp "lib/*" $(find src/main/java -name "*.java")
cp src/main/resources/*.sql bin/

# 3. Prompt for password if not provided and running interactively
if [ -z "${DB_PASSWORD+x}" ] && [ -t 0 ]; then
    read -s -p "Enter MySQL password (or press Enter if blank): " DB_PASSWORD
    echo ""
    export DB_PASSWORD
fi

# 4. Launch the application
exec "$JAVA_BIN" -cp "bin:lib/*" quizbattle.Main "$@"
