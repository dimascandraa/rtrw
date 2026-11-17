# Desktop - Sistem Informasi RT/RW

Aplikasi Java Swing memakai backend REST API yang sama dengan web dan tidak mengakses MySQL secara langsung. Login menggunakan cookie sesi dari API; ringkasan, pengumuman, aktivitas, dan notifikasi diambil dari endpoint backend.

## Prasyarat

- JDK 11 atau lebih baru (`java -version` dan `javac -version`)
- PHP 7.4+ dengan `pdo_mysql`
- MySQL/MariaDB

## Menyiapkan demo lokal (Windows)

1. Nyalakan MySQL di XAMPP. Buat database dengan mengimpor `database/migrations/schema.sql`, lalu `database/migrations/seed.sql` melalui phpMyAdmin. Seed hanya dijalankan satu kali pada database kosong.
2. Salin konfigurasi backend dan sesuaikan `DB_USER` serta `DB_PASSWORD` jika konfigurasi MySQL berbeda:

   ```powershell
   Copy-Item backend\.env.example backend\.env
   ```
3. Buka terminal di folder `SISTEM-RT-RW`, lalu jalankan server web dan API:

   ```powershell
   php -S 127.0.0.1:8000 -t web backend/public/router.php
   ```

   Jangan tutup terminal ini. Web dapat dibuka di `http://127.0.0.1:8000/`.
   API dapat dicek di `http://127.0.0.1:8000/backend/public/api/v1/health`.
4. Di terminal kedua, masuk ke folder `SISTEM-RT-RW\desktop`. Pastikan `config.properties` menunjuk ke API lokal:

   ```properties
   api.base.url=http://127.0.0.1:8000/backend/public/api/v1
   ```

   Jalankan aplikasi:

   ```powershell
   .\run.bat
   ```

5. Login dengan akun demo seed:

   ```text
   Email: adminrw@contoh.test
   Kata sandi: Password123
   ```

   Akun demo lain tercantum di `database/migrations/README.md`. Untuk demo, desktop dan web memakai akun/database yang sama.

## Menjalankan di Linux/macOS

Siapkan database dan `backend/.env` seperti langkah di atas. Dari folder `SISTEM-RT-RW`, jalankan server PHP dengan perintah yang sama; dari folder `desktop/`, jalankan `./run.sh`.

## Endpoint yang dipakai

- `POST /auth/login` - email/telepon dan kata sandi
- `GET /dashboard/summary`
- `GET /announcements?limit=5`
- `GET /notifications?limit=5`
- `POST /auth/logout`

URL endpoint di atas ditambahkan ke `api.base.url`. Endpoint API lain untuk CRUD modul belum tersedia, jadi menu Data Warga, Iuran, Pembayaran, Pengumuman, dan Laporan masih berupa placeholder. Desktop tidak memakai mode mock/offline; demo memerlukan backend dan database yang sudah disiapkan.

## Pengembangan

- Semua permintaan jaringan dijalankan melalui `SwingWorker`, bukan di UI thread.
- Tambahkan pemanggilan endpoint lewat kelas di package `api` dan gunakan `ApiClient`.
- Salin `config.example.properties` menjadi `config.properties` bila file konfigurasi belum tersedia; jangan commit konfigurasi lokal.
- Branch: `feature/desktop-<nama-fitur>`, Pull Request ke `develop`.
