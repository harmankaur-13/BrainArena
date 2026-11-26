#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

# 1. Load environment variables from .env if present
if [ -f .env ]; then
    set -a
    source .env 2>/dev/null || true
    set +a
fi

# 2. Locate Java 17 binaries
JAVA_BIN="java"
JAVAC_BIN="javac"

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/javac" ]; then
    JAVA_BIN="$JAVA_HOME/bin/java"
    JAVAC_BIN="$JAVA_HOME/bin/javac"
elif [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin" ]; then
    JAVA_BIN="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/java"
    JAVAC_BIN="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/javac"
elif command -v /usr/libexec/java_home >/dev/null 2>&1; then
    JH=$(/usr/libexec/java_home -v 17 2>/dev/null || true)
    if [ -n "$JH" ] && [ -x "$JH/bin/javac" ]; then
        JAVA_BIN="$JH/bin/java"
        JAVAC_BIN="$JH/bin/javac"
    fi
fi

# 3. Compile sources and copy SQL resources into bin/
mkdir -p bin
"$JAVAC_BIN" -d bin -cp "lib/*" $(find src/main/java -name "*.java")
cp src/main/resources/*.sql bin/ 2>/dev/null || true

# 4. Launch the application normally
exec "$JAVA_BIN" -cp "bin:lib/*" quizbattle.Main "$@"
