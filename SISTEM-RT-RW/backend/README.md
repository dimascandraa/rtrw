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
| `GET` | `/api/v1/resources/{resource}` | Daftar data fitur dengan izin dan batas wilayah |

Body login:

```json
{
  "identity": "adminrw@contoh.test",
  "password": "Password123",
  "client": "web"
}
```

Web dapat menghilangkan properti `client` (default `web`); token hanya dikirim lewat cookie `HttpOnly`, `SameSite=Lax`, dan `Secure` ketika HTTPS aktif. Aplikasi native mengirim `client: "mobile"` atau `client: "desktop"` dan menerima `accessToken`. Kirim token pada setiap permintaan berikutnya dengan header `Authorization: Bearer <accessToken>`. Token native sama dengan token cookie; hanya hash SHA-256 yang disimpan di `sesi_pengguna`. Logout mencabut token yang digunakan. Kata sandi diverifikasi dengan `password_verify()`. Lima kegagalan login mengunci akun selama 15 menit.

Untuk aplikasi web lintas origin, isi `APP_CORS_ALLOWED_ORIGINS` pada `backend/.env` dengan daftar origin frontend yang dipisahkan koma, tanpa wildcard, misalnya `https://web.contoh.test,https://admin.contoh.test`. Biarkan kosong jika frontend satu origin. CORS memakai allowlist eksplisit dan mengizinkan header `Authorization`.

Endpoint data memeriksa izin pada `peran_izin_akses`/`izin_akses`, membatasi query berdasarkan organisasi/wilayah, dan menolak data yang tidak aman untuk akses terbatas. Daftar resource, filter, dan format respons ada di [`documentation/API/README.md`](../documentation/API/README.md). Endpoint API mengembalikan error generik dan detail teknis hanya dicatat pada log server.

## Pengembangan endpoint

Resource saat ini menyediakan operasi baca (`GET`). Sebelum menambah `POST`, `PATCH`, atau `DELETE`, implementasikan validasi per resource, izin aksi, isolasi `id_organisasi`, audit log, dan transaksi bisnis terkait; jangan menambah operasi tulis generik ke tabel.

Web memiliki halaman ruang kerja untuk 26 fitur dan memuat data melalui API. Operasi tulis/CRUD, unggahan berkas, serta proses bisnis seperti verifikasi pembayaran dan persetujuan surat belum tersedia.
