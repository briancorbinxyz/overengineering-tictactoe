#!/usr/bin/env sh
java -XX:AOTMode=record -XX:AOTConfiguration=app.aotconf --enable-native-access=ALL-UNNAMED --enable-preview  -cp "$(gradle -q buildClasspath)" org.xxdc.oss.example.AppTrainer