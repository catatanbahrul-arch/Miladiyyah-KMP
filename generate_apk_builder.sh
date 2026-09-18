#!/bin/bash

echo "⚙️ Memerintahkan GitHub untuk merakit APK..."

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

      - name: Buat Gradle Wrapper & Build APK
        run: |
          gradle wrapper --gradle-version 8.6
          ./gradlew :composeApp:assembleDebug --stacktrace

      - name: Upload APK Output
        uses: actions/upload-artifact@v4
        with:
          name: Miladiyyah-Beranda-UI-APK
          path: composeApp/build/outputs/apk/debug/*.apk
EOF

echo "✅ Perintah Build APK siap dikirim ke GitHub!"
EOF
