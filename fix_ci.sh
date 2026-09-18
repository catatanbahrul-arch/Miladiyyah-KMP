#!/bin/bash

echo "🔧 Memperbaiki konfigurasi Gradle di GitHub Actions..."

cat << 'EOF' > .github/workflows/ci.yml
name: Build Android APK

on:
  push:
    branches: [ "main" ]

jobs:
  build-apk:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Source Code
        uses: actions/checkout@v4

      - name: Setup Java (JDK 17)
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'zulu'

      - name: Setup Gradle 8.6
        uses: gradle/actions/setup-gradle@v3
        with:
          gradle-version: 8.6

      - name: Build APK
        run: gradle :composeApp:assembleDebug

      - name: Upload APK Output
        uses: actions/upload-artifact@v4
        with:
          name: Miladiyyah-Beranda-UI-APK
          path: composeApp/build/outputs/apk/debug/*.apk
EOF

echo "✅ File CI diperbarui dengan Setup Gradle 8.6"
EOF
