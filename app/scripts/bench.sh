#!/usr/bin/env sh

# Usage:
#   app/scripts/bench.sh            # standard JVM
#   app/scripts/bench.sh --aot      # use AOT cache (expects app.aot present)

USE_AOT=false
for arg in "$@"; do
  case "$arg" in
    --aot|aot)
      USE_AOT=true
      ;;
  esac
done

JAVA_OPTS="--enable-native-access=ALL-UNNAMED --enable-preview"
if [ "$USE_AOT" = true ]; then
  JAVA_OPTS="-XX:AOTCache=app.aot $JAVA_OPTS"
fi

java $JAVA_OPTS \
  -cp "$(gradle -q buildClasspath)" \
  org.xxdc.oss.example.AppTrainer