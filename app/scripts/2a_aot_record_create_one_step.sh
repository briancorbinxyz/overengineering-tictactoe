#!/usr/bin/env sh
# One-step AOT workflow using JEP 514's -XX:AOTCacheOutput

java -XX:AOTCacheOutput=app.aot \
  --enable-native-access=ALL-UNNAMED \
  --enable-preview \
  -cp "$(gradle -q buildClasspath)" \
  org.xxdc.oss.example.AppTrainer
