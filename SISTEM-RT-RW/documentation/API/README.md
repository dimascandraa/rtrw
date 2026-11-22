# Referensi REST API v1

Semua aplikasi (web, mobile, dan desktop) menggunakan backend yang sama. Backend adalah satu-satunya pihak yang mengakses MySQL.

## Dasar

- Base path: `/api/v1`
- Format request dan response: JSON UTF-8
- Sukses: `{ "data": ... }`
- Gagal: `{ "error": { "code": "...", "message": "..." } }`
- Endpoint data memerlukan sesi aktif kecuali `GET /health` dan `POST /auth/login`.
- Resource list memakai `limit` (default 50, maksimum 100), `offset` (default 0), dan `search` (maksimum 100 karakter).
- Respons list: `{ "data": { "items": [...], "pagination": { "limit": 50, "offset": 0 } } }`

## Autentikasi lintas platform

Web menggunakan cookie sesi HttpOnly same-origin. Aplikasi native meminta token pada login:

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "identity": "adminrw@contoh.test",
  "password": "Password123",
  "client": "mobile"
}
```

Gunakan `client: "mobile"` atau `client: "desktop"` agar respons login menyertakan `accessToken`. Simpan token di penyimpanan aman platform, bukan di log atau penyimpanan yang dapat dibaca halaman lain.

```http
Authorization: Bearer <accessToken>
```

`client` untuk web boleh dihilangkan atau bernilai `web`; token tidak dikembalikan ke JavaScript web. Logout dengan `POST /api/v1/auth/logout` mencabut token atau cookie yang sedang dipakai. Untuk web lintas origin, konfigurasi `APP_CORS_ALLOWED_ORIGINS` sebagai allowlist origin di `backend/.env`.

Respons login dan `GET /auth/me` menyertakan daftar `user.permissions` dan `user.restrictedPermissions`. Daftar izin dipakai aplikasi untuk menyembunyikan menu yang tidak tersedia bagi peran pengguna; izin terbatas menandai akses yang hanya berlaku untuk data milik sendiri atau wilayah yang ditugaskan. Backend tetap memeriksa izin pada setiap endpoint.

## Endpoint

| Metode | Path | Resource / penggunaan |
|---|---|---|
| `GET` | `/health` | Status database |
| `POST` | `/auth/login` | Membuat sesi |
| `POST` | `/auth/register` | Mendaftar akun dalam status menunggu verifikasi |
| `GET` | `/auth/me` | Pengguna, wilayah, dan izin sesi |
| `POST` | `/auth/logout` | Mencabut sesi aktif |
| `PATCH` | `/auth/profile` | Mengubah profil sendiri |
| `POST` | `/auth/password` | Mengganti kata sandi dengan verifikasi sandi saat ini |
| `GET` | `/registrations/pending` | Pendaftaran yang dapat ditinjau pengurus wilayah |
| `POST` | `/registrations/{id}/approve` | Menyetujui pendaftaran sebagai Warga |
| `POST` | `/letters` | Mengirim pengajuan surat warga |
| `POST` | `/letters/{id}/decision` | Menyetujui atau menolak surat sesuai wilayah |
| `GET` | `/dashboard/summary` | Ringkasan beranda dan aktivitas |
| `GET` | `/announcements` | Pengumuman sesuai wilayah |
| `GET` | `/notifications` | Notifikasi pengguna dan jumlah belum dibaca |
| `GET` | `/resources/warga` | Data warga (tanpa NIK/No. KK) |
| `GET` | `/resources/mutasi` | Riwayat mutasi warga |
| `GET` | `/resources/kategori-iuran` | Kategori iuran |
| `GET` | `/resources/tagihan` | Tagihan dan status pembayaran |
| `GET` | `/resources/pembayaran` | Pembayaran dan status verifikasi |
| `GET` | `/resources/transaksi-kas` | Transaksi kas |
| `GET` | `/resources/jenis-surat` | Jenis surat aktif untuk pengajuan |
| `GET` | `/resources/pengajuan-surat` | Pengajuan dan status surat |
| `GET` | `/resources/survei` | Survei dan jumlah jawaban |
| `GET` | `/resources/kegiatan` | Agenda kegiatan warga |
| `GET` | `/resources/pengaduan` | Laporan/keluhan |
| `GET` | `/resources/kontak-darurat` | Direktori layanan darurat aktif |
| `GET` | `/resources/pengguna` | Akun dan peran aktif |
| `GET` | `/resources/peran` | Daftar peran |
| `GET` | `/resources/audit-log` | Riwayat aktivitas sesuai izin |
| `GET` | `/resources/sesi` | Sesi/perangkat milik pengguna aktif |
| `GET` | `/resources/wilayah` | Struktur RT/RW sesuai wilayah |
| `GET` | `/resources/pengaturan` | Pengaturan organisasi sesuai izin |
| `GET` | `/resources/cadangan-data` | Metadata riwayat backup (tanpa path privat) |

Contoh pencarian dan pagination:

```http
GET /api/v1/resources/warga?search=Rina&limit=20&offset=0
Authorization: Bearer <accessToken>
```

Pendaftaran menerima `name`, `email` (opsional jika telepon tersedia), `phone`, dan `password` minimal 8 karakter. Akun disimpan dalam status menunggu verifikasi tanpa peran maupun sesi. Admin, Ketua RW, atau Ketua RT dapat melihat pendaftaran yang teleponnya cocok dengan data warga aktif di wilayahnya dan menyetujui akun sebagai Warga. Akun dengan nomor telepon yang belum tercatat pada data warga tidak dapat disetujui.

Pengajuan surat menerima `jenisSuratId` dan `keperluan`; warga hanya dapat melihat pengajuan miliknya. Pengurus hanya dapat memutuskan pengajuan dalam organisasi/wilayah yang ditugaskan. Profil menerima `name`, `email`, dan `phone`; penggantian kata sandi menerima `currentPassword` dan `newPassword`.

Setiap resource memiliki izin, batas organisasi/wilayah, dan bentuk data khusus di backend. Akses terbatas yang tidak memiliki filter aman akan mendapat `403 restricted_resource`, bukan daftar global. Ekspor laporan web mengambil data melalui resource API yang sama dan mempertahankan batas akses tersebut.
