#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

JAVA_BIN="java"
if [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin" ]; then
    JAVA_BIN="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/java"
fi

exec "$JAVA_BIN" -cp "target/classes:lib/*" quizbattle.Main "$@"
