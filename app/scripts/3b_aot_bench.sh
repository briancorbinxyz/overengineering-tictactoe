#!/usr/bin/env sh
java -XX:AOTCache=app.aot --enable-native-access=ALL-UNNAMED --enable-preview  -cp "$(gradle -q buildClasspath)" org.xxdc.oss.example.AppTrainer