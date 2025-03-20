#!/usr/bin/env sh
# Generate an ML-DSA key store
keytool -keystore ks \
  -storepass changeit \
  -genkeypair \
  -alias mldsa \
  -keyalg ML-DSA \
  -groupname ML-DSA-87 \
  -dname CN=ML-DSA