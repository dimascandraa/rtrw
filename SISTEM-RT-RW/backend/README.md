# Backend REST API

Backend ini memakai PHP native dan PDO MySQL tanpa framework. Database hanya diakses dari backend; halaman web memakai JSON API.

## Menjalankan lokal

Persyaratan: PHP 7.4+ dengan ekstensi `pdo_mysql`, dan MySQL/MariaDB.

1. Salin `backend/.env.example` menjadi `backend/.env`, lalu sesuaikan kredensial MySQL.
2. Buat database dengan mengimpor `database/migrations/schema.sql`, lalu `database/migrations/seed.sql` melalui phpMyAdmin atau MySQL.
3. Dari folder `SISTEM-RT-RW`, jalankan:

   ```powershell
   php -S 127.0.0.1:8000 -t web backend/public/router.php
   ```

4. Buka `http://127.0.0.1:8000/`. Jangan membuka `web/index.html` langsung dengan `file://`, karena browser tidak dapat mengirim cookie sesi ke API.

Untuk XAMPP, letakkan repository di dalam `htdocs`, aktifkan `mod_rewrite`, dan izinkan `.htaccess`. Halaman web menggunakan alamat API relatif sehingga frontend dan backend tetap satu origin.

Akun demo setelah mengimpor seed:

```text
Email: adminrw@contoh.test
Kata sandi: Password123
```

Seluruh akun dummy di seed menggunakan kata sandi tersebut. Seed hanya untuk database pengembangan kosong; jangan dijalankan ulang pada database yang sudah berisi data.

## Endpoint yang tersedia

Semua respons JSON menggunakan format `{ "data": ... }` atau `{ "error": { "code": "...", "message": "..." } }`.

| Metode | Endpoint | Keterangan |
|---|---|---|
| `GET` | `/api/v1/health` | Memeriksa koneksi database |
| `POST` | `/api/v1/auth/login` | Masuk dengan email/telepon dan kata sandi |
| `GET` | `/api/v1/auth/me` | Mendapatkan pengguna dari sesi aktif |
| `POST` | `/api/v1/auth/logout` | Mencabut sesi aktif |
| `GET` | `/api/v1/dashboard/summary` | Statistik warga, keluarga, tagihan, dan kas |
| `GET` | `/api/v1/announcements?limit=5` | Pengumuman yang terlihat di wilayah pengguna |
| `GET` | `/api/v1/notifications?limit=5` | Notifikasi milik pengguna dan jumlah yang belum dibaca |

Body login:

```json
{
  "identity": "adminrw@contoh.test",
  "password": "Password123"
}
```

Login membuat token acak; hanya hash SHA-256 token yang disimpan di `sesi_pengguna`. Token asli dikirim sebagai cookie `HttpOnly`, `SameSite=Lax`, dan `Secure` ketika HTTPS aktif. Kata sandi diverifikasi dengan `password_verify()`. Lima kegagalan login mengunci akun selama 15 menit.

Endpoint dashboard dan pengumuman memeriksa izin pada `peran_izin_akses`/`izin_akses` dan membatasi query berdasarkan organisasi serta penugasan wilayah. Endpoint API mengembalikan error generik dan detail teknis hanya dicatat pada log server.

## Pengembangan endpoint

Tambahkan endpoint berdasarkan resource dan aksi bisnis—misalnya `/warga`, `/tagihan`, atau `/pembayaran`—bukan endpoint terpisah untuk setiap halaman. Halaman dashboard boleh memakai endpoint ringkasan agregat. Saat fitur CRUD dibuat, validasi input, pemeriksaan izin, dan filter `id_organisasi` harus diterapkan di backend.

Saat ini login, ringkasan beranda, pengumuman, dan notifikasi sudah menggunakan API. Kartu fitur modul lain masih berupa pratinjau UI dan belum memiliki operasi CRUD.
