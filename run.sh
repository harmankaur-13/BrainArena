#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

# Prompt for password if not provided and running interactively
if [ -z "${DB_PASSWORD+x}" ] && [ -t 0 ]; then
    read -s -p "Enter MySQL password (or press Enter if blank): " DB_PASSWORD
    echo ""
    export DB_PASSWORD
fi

JAVA_BIN="java"
if [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin" ]; then
    JAVA_BIN="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/java"
fi

exec "$JAVA_BIN" -cp "target/classes:lib/*" quizbattle.Main "$@"
