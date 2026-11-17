# Database — Sistem Informasi RT/RW

Database MySQL tunggal untuk Web, Mobile, dan Desktop. Hanya **Backend (PHP)** yang boleh mengakses database ini; aplikasi lain memakai REST API.

| Item | Keterangan |
|---|---|
| DBMS | MySQL 5.7+ / 8 atau MariaDB 10.4+ (XAMPP) |
| Nama database | `sistem_rt_rw` |
| Charset | `utf8mb4` (`utf8mb4_unicode_ci`), engine InnoDB |
| Isi | 41 tabel + 1 view (`v_saldo_kas`) |

## Isi folder

```text
database/
├── schema.sql      Struktur lengkap (selalu versi terbaru)
├── seed.sql        Data awal: peran, izin akses, + data dummy
└── migrations/     Perubahan skema bertahap setelah rilis pertama
```

Dokumentasi: [`documentation/ERD/erd.md`](../documentation/ERD/erd.md) (diagram) dan [`documentation/ERD/kamus-data.md`](../documentation/ERD/kamus-data.md) (semua tabel dan kolom).

## Cara membuat database di komputer sendiri

### Opsi A — phpMyAdmin (XAMPP)
1. Nyalakan **Apache** dan **MySQL** di XAMPP, buka `http://localhost/phpmyadmin`.
2. Tab **Import** → pilih `database/schema.sql` → **Go**. (Database `sistem_rt_rw` dibuat otomatis.)
3. Pilih database `sistem_rt_rw` di sisi kiri → tab **Import** → pilih `database/seed.sql` → **Go**.

### Opsi B — terminal
```bash
mysql -u root -p < database/schema.sql
mysql -u root -p sistem_rt_rw < database/seed.sql
```

### Mengulang dari nol
```sql
DROP DATABASE sistem_rt_rw;
```
lalu ulangi langkah di atas. Seed hanya boleh dijalankan sekali pada database kosong.

## Akun dummy (password semua: `Password123`)

| Email | Peran |
|---|---|
| superadmin@contoh.test | Super Admin |
| adminrw@contoh.test | Admin RW |
| ketuarw@contoh.test | Ketua RW |
| ketuart1@contoh.test | Ketua RT 01 |
| sekretaris@contoh.test | Sekretaris |
| bendahara@contoh.test | Bendahara |
| agus@contoh.test | Warga (RT 03) |
| rudi@contoh.test | Warga, `menunggu_verifikasi` (untuk menguji verifikasi akun) |

Seluruh data di `seed.sql` bagian 2 adalah **karangan**. Jangan memasukkan data warga asli ke repository.

## Aturan penting untuk tim Backend

1. **Isolasi multi-RT/RW.** Setiap query pada tabel operasional wajib memakai `WHERE id_organisasi = ?` (kecuali Super Admin). Tabel anak mengikuti induknya, misalnya `rincian_tagihan` lewat `tagihan`.
2. **Hak akses.** Cek lewat `penugasan_pengguna` → `peran` → `peran_izin_akses` → `izin_akses`. Kolom `terbatas = 1` berarti izin bertanda `*` di matriks hak akses: hanya data milik sendiri atau wilayahnya sendiri.
3. **Kata sandi** disimpan dengan `password_hash()`; **token sesi dan kode OTP** disimpan sebagai hash, bukan aslinya.
4. **NIK dan No. KK** adalah data sensitif: samarkan di respons API (mis. `3500********0101`) dan batasi aksesnya.
5. **Pembayaran → kas.** Baris `transaksi_kas` (masuk) dan `kuitansi` dibuat setelah `pembayaran.status = 'terverifikasi'`. Perbarui `tagihan.nominal_dibayar` dan `tagihan.status` dalam **satu transaksi database** yang sama.
6. **Status `lewat_jatuh_tempo`** pada `tagihan` perlu diperbarui oleh proses terjadwal (cron) atau dihitung saat query.
7. **`log_audit`** hanya di-INSERT. Jangan UPDATE/DELETE dari aplikasi.
8. **File upload** disimpan di folder privat di luar folder web publik; database hanya menyimpan metadata di tabel `berkas`.
9. **Urutan insert** untuk keluarga dan warga: `keluarga` (tanpa kepala) → `warga` → isi `keluarga.id_kepala_keluarga`.

## Cara mengubah struktur database

Setelah `schema.sql` dipakai bersama, **jangan edit tabel langsung lewat phpMyAdmin** karena perubahan itu tidak tercatat dan tidak sampai ke anggota lain. Alurnya:

1. Buat branch: `git switch -c feature/database-nama-perubahan`
2. Tulis perubahan di file baru `database/migrations/NNN_deskripsi_singkat.sql` (nomor berurutan), misalnya:

   ```sql
   -- 001_tambah_kolom_rt_pada_pengumuman.sql
   -- Alasan : ...
   -- Dampak : endpoint /api/announcements
   USE sistem_rt_rw;
   ALTER TABLE pengumuman ADD COLUMN prioritas ENUM('biasa','penting') NOT NULL DEFAULT 'biasa' AFTER judul;
   ```
3. Perbarui juga `schema.sql` agar anggota baru langsung mendapat versi terbaru.
4. Perbarui `documentation/ERD/` bila relasi atau kolom berubah.
5. Buat Pull Request ke `develop`. Cantumkan perubahan database dan **tandai Backend** sebagai reviewer, karena API ikut terdampak.

## Backup

File backup asli (`*.sql` hasil dump, `*.sql.enc`) **tidak boleh** di-commit. Gunakan data dummy untuk pengembangan. Contoh dump untuk diri sendiri:

```bash
mysqldump -u root -p sistem_rt_rw > ~/backup-sistem_rt_rw.sql
```
