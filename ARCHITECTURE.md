# Arsitektur Miladiyyah-KMP

## 1. Clean Architecture Strict Rules
UI <-> Presentation <-> Domain <-> Repository <-> Local/Remote Data Source

## 2. Batasan Keras (Hard Rules)
- UI **TIDAK BOLEH** memanggil HTTP request.
- UI **TIDAK BOLEH** mengakses Firebase/GApps Script langsung.
- Offline-First: Selalu baca dari Local DB (Room). Internet hanya untuk sync background.
