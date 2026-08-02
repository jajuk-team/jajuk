#!/bin/bash
# Script to run targeted JUnit5 tests using ConsoleLauncher
# Usage: ./run-junit5-test.sh org.jajuk.services.mpris.TestMprisService

if [ $# -eq 0 ]; then
  echo "Usage: $0 <test-class-name>"
  echo "Example: $0 org.jajuk.services.mpris.TestMprisService"
  exit 1
fi

TEST_CLASS=$1
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# Build comprehensive classpath
CLASSPATH="bin"
for jar in lib/*.jar lib/build/*.jar; do
  if [ -f "$jar" ]; then
    CLASSPATH="$CLASSPATH:$jar"
  fi
done

echo "Running test: $TEST_CLASS"
echo "================================================"

# Run test with ConsoleLauncher
java -cp "$CLASSPATH" \
  org.junit.platform.console.ConsoleLauncher \
  --select-class "$TEST_CLASS" \
  --fail-if-no-tests



