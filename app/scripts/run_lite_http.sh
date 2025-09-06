#!/usr/bin/env sh
# Runs the HTTP + Game Lite demo entrypoint.
# Uses the Gradle task `:app:buildClasspath` to construct the classpath and then
# launches the main class `org.xxdc.oss.example.AppLiteHttp`.

java \
  --enable-native-access=ALL-UNNAMED \
  --enable-preview \
  -cp "$(gradle -q :app:buildClasspath)" \
  AppLiteHttp
