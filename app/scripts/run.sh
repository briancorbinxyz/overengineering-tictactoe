#!/usr/bin/env sh
java --enable-native-access=ALL-UNNAMED --enable-preview  -cp "$(gradle -q buildClasspath)" org.xxdc.oss.example.App