#!/bin/bash

echo "⚙️ Memperbaiki nama aplikasi menjadi 'Miladiyyah' dan menyiapkan folder ikon..."

# 1. BUAT FILE STRINGS.XML (Standar Android untuk Nama Aplikasi)
mkdir -p composeApp/src/androidMain/res/values
cat << 'EOF' > composeApp/src/androidMain/res/values/strings.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Miladiyyah</string>
</resources>
EOF

# 2. UPDATE ANDROID MANIFEST (Arahkan nama dan ikon ke resource lokal)
cat << 'EOF' > composeApp/src/androidMain/AndroidManifest.xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <application
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@drawable/ic_launcher"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:label="@string/app_name"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
EOF

# 3. SIAPKAN FOLDER DRAWABLE UNTUK TEMPAT UPLOAD LOGO
mkdir -p composeApp/src/androidMain/res/drawable
cat << 'EOF' > composeApp/src/androidMain/res/drawable/README.md
Folder ini adalah tempat Anda meng-upload logo aplikasi.
Pastikan nama file logo Anda adalah: ic_launcher.png
EOF

# 4. BUAT IKON DUMMY SEMENTARA (Agar build tidak error sebelum Anda upload gambar asli)
cat << 'EOF' > composeApp/src/androidMain/res/drawable/ic_launcher.xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#1B4332" android:pathData="M0,0h108v108h-108z"/>
</vector>
EOF

echo "✅ Nama aplikasi diset ke 'Miladiyyah' dan folder ikon telah siap!"
EOF
