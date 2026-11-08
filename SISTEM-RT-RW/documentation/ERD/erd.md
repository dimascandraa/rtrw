# ERD — Sistem Informasi RT/RW

> Dihasilkan otomatis dari `database/schema.sql` (41 tabel). Diagram di bawah memakai **Mermaid**, jadi tampil langsung di GitHub.
> Hanya kolom kunci (PK = primary key, FK = foreign key, UK = unique) yang ditampilkan agar diagram tetap terbaca. Daftar kolom lengkap ada di [kamus-data.md](kamus-data.md).

**Cara membaca:** `||--o{` artinya satu induk memiliki nol atau banyak anak. `||--o|` artinya satu induk memiliki nol atau satu anak. Simbol `|o` di sisi induk = relasi opsional (kolom FK boleh NULL, anak tidak wajib punya induk). `UK` pada diagram = kolom unik tunggal; kolom yang menjadi bagian dari unique gabungan (mis. `id_organisasi + nomor_rw`) tidak diberi tanda.

**Isolasi multi-RT/RW:** hampir semua tabel operasional memiliki `id_organisasi` (lihat catatan di bagian bawah halaman ini).


## Wilayah, akun, dan hak akses

```mermaid
erDiagram
  organisasi {
    BIGINT id PK
    VARCHAR kode UK
  }
  rw {
    BIGINT id PK
    BIGINT id_organisasi FK
  }
  rt {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_rw FK
  }
  pengguna {
    BIGINT id PK
    BIGINT id_warga FK,UK
    VARCHAR email UK
    VARCHAR telepon UK
  }
  peran {
    BIGINT id PK
    VARCHAR kode UK
    VARCHAR nama UK
  }
  izin_akses {
    BIGINT id PK
    VARCHAR kode UK
  }
  peran_izin_akses {
    BIGINT id PK
    BIGINT id_peran FK
    BIGINT id_izin_akses FK
  }
  penugasan_pengguna {
    BIGINT id PK
    BIGINT id_pengguna FK
    BIGINT id_peran FK
    BIGINT id_organisasi FK
    BIGINT id_rw FK
    BIGINT id_rt FK
  }
  kode_verifikasi {
    BIGINT id PK
    BIGINT id_pengguna FK
  }
  sesi_pengguna {
    BIGINT id PK
    BIGINT id_pengguna FK
    CHAR hash_token UK
  }
  pengguna ||--o{ kode_verifikasi : "id_pengguna"
  pengguna ||--o{ penugasan_pengguna : "id_pengguna"
  organisasi |o--o{ penugasan_pengguna : "id_organisasi"
  rw |o--o{ penugasan_pengguna : "id_rw"
  rt |o--o{ penugasan_pengguna : "id_rt"
  peran ||--o{ penugasan_pengguna : "id_peran"
  peran ||--o{ peran_izin_akses : "id_peran"
  izin_akses ||--o{ peran_izin_akses : "id_izin_akses"
  rw ||--o{ rt : "id_rw"
  organisasi ||--o{ rt : "id_organisasi"
  organisasi ||--o{ rw : "id_organisasi"
  pengguna ||--o{ sesi_pengguna : "id_pengguna"
```

## Data warga dan keluarga

```mermaid
erDiagram
  organisasi {
    BIGINT id PK
    VARCHAR kode UK
  }
  rt {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_rw FK
  }
  keluarga {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_rt FK
    CHAR nomor_kk UK
    BIGINT id_kepala_keluarga FK
  }
  warga {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_keluarga FK
    CHAR nik UK
  }
  mutasi_warga {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_warga FK
    BIGINT dicatat_oleh FK
  }
  pengguna {
    BIGINT id PK
    BIGINT id_warga FK,UK
    VARCHAR email UK
    VARCHAR telepon UK
  }
  organisasi ||--o{ keluarga : "id_organisasi"
  warga |o--o{ keluarga : "id_kepala_keluarga"
  rt ||--o{ keluarga : "id_rt"
  organisasi ||--o{ mutasi_warga : "id_organisasi"
  warga ||--o{ mutasi_warga : "id_warga"
  pengguna |o--o{ mutasi_warga : "dicatat_oleh"
  warga |o--o| pengguna : "id_warga"
  organisasi ||--o{ rt : "id_organisasi"
  organisasi ||--o{ warga : "id_organisasi"
  keluarga ||--o{ warga : "id_keluarga"
```

## Keuangan

```mermaid
erDiagram
  organisasi {
    BIGINT id PK
    VARCHAR kode UK
  }
  keluarga {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_rt FK
    CHAR nomor_kk UK
    BIGINT id_kepala_keluarga FK
  }
  kategori_iuran {
    BIGINT id PK
    BIGINT id_organisasi FK
  }
  aturan_iuran {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_kategori_iuran FK
    BIGINT id_rt FK
  }
  periode_tagihan {
    BIGINT id PK
    BIGINT id_organisasi FK
  }
  tagihan {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_keluarga FK
    BIGINT id_periode_tagihan FK
  }
  rincian_tagihan {
    BIGINT id PK
    BIGINT id_tagihan FK
    BIGINT id_kategori_iuran FK
  }
  pembayaran {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_tagihan FK
    BIGINT id_pengguna_pembayar FK
    BIGINT diverifikasi_oleh FK
  }
  bukti_pembayaran {
    BIGINT id PK
    BIGINT id_pembayaran FK
    BIGINT id_berkas FK
    BIGINT diunggah_oleh FK
  }
  kuitansi {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_pembayaran FK,UK
    BIGINT id_berkas FK
  }
  kategori_pengeluaran {
    BIGINT id PK
    BIGINT id_organisasi FK
  }
  transaksi_kas {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_kategori_pengeluaran FK
    BIGINT id_pembayaran FK,UK
    BIGINT id_berkas FK
    BIGINT dicatat_oleh FK
  }
  berkas {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT diunggah_oleh FK
  }
  pengguna {
    BIGINT id PK
    BIGINT id_warga FK,UK
    VARCHAR email UK
    VARCHAR telepon UK
  }
  organisasi ||--o{ aturan_iuran : "id_organisasi"
  kategori_iuran ||--o{ aturan_iuran : "id_kategori_iuran"
  pengguna |o--o{ berkas : "diunggah_oleh"
  organisasi ||--o{ berkas : "id_organisasi"
  berkas ||--o{ bukti_pembayaran : "id_berkas"
  pengguna |o--o{ bukti_pembayaran : "diunggah_oleh"
  pembayaran ||--o{ bukti_pembayaran : "id_pembayaran"
  organisasi ||--o{ kategori_iuran : "id_organisasi"
  organisasi ||--o{ kategori_pengeluaran : "id_organisasi"
  organisasi ||--o{ keluarga : "id_organisasi"
  pembayaran ||--o| kuitansi : "id_pembayaran"
  organisasi ||--o{ kuitansi : "id_organisasi"
  berkas |o--o{ kuitansi : "id_berkas"
  pengguna |o--o{ pembayaran : "id_pengguna_pembayar"
  organisasi ||--o{ pembayaran : "id_organisasi"
  pengguna |o--o{ pembayaran : "diverifikasi_oleh"
  tagihan ||--o{ pembayaran : "id_tagihan"
  organisasi ||--o{ periode_tagihan : "id_organisasi"
  kategori_iuran ||--o{ rincian_tagihan : "id_kategori_iuran"
  tagihan ||--o{ rincian_tagihan : "id_tagihan"
  periode_tagihan ||--o{ tagihan : "id_periode_tagihan"
  organisasi ||--o{ tagihan : "id_organisasi"
  keluarga ||--o{ tagihan : "id_keluarga"
  pengguna |o--o{ transaksi_kas : "dicatat_oleh"
  pembayaran |o--o| transaksi_kas : "id_pembayaran"
  organisasi ||--o{ transaksi_kas : "id_organisasi"
  kategori_pengeluaran |o--o{ transaksi_kas : "id_kategori_pengeluaran"
  berkas |o--o{ transaksi_kas : "id_berkas"
```

## Surat

```mermaid
erDiagram
  organisasi {
    BIGINT id PK
    VARCHAR kode UK
  }
  warga {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_keluarga FK
    CHAR nik UK
  }
  pengguna {
    BIGINT id PK
    BIGINT id_warga FK,UK
    VARCHAR email UK
    VARCHAR telepon UK
  }
  jenis_surat {
    BIGINT id PK
    BIGINT id_organisasi FK
  }
  pengajuan_surat {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_warga FK
    BIGINT id_jenis_surat FK
    BIGINT id_pengguna_pengaju FK
    BIGINT diproses_oleh FK
    BIGINT id_berkas FK
  }
  persetujuan_surat {
    BIGINT id PK
    BIGINT id_pengajuan_surat FK
    BIGINT id_pengguna_penyetuju FK
  }
  berkas {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT diunggah_oleh FK
  }
  pengguna |o--o{ berkas : "diunggah_oleh"
  organisasi ||--o{ berkas : "id_organisasi"
  organisasi ||--o{ jenis_surat : "id_organisasi"
  jenis_surat ||--o{ pengajuan_surat : "id_jenis_surat"
  warga ||--o{ pengajuan_surat : "id_warga"
  berkas |o--o{ pengajuan_surat : "id_berkas"
  pengguna |o--o{ pengajuan_surat : "id_pengguna_pengaju"
  pengguna |o--o{ pengajuan_surat : "diproses_oleh"
  organisasi ||--o{ pengajuan_surat : "id_organisasi"
  warga |o--o| pengguna : "id_warga"
  pengajuan_surat ||--o{ persetujuan_surat : "id_pengajuan_surat"
  pengguna ||--o{ persetujuan_surat : "id_pengguna_penyetuju"
  organisasi ||--o{ warga : "id_organisasi"
```

## Informasi, kegiatan, pengaduan, dan survei

```mermaid
erDiagram
  organisasi {
    BIGINT id PK
    VARCHAR kode UK
  }
  rt {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_rw FK
  }
  rw {
    BIGINT id PK
    BIGINT id_organisasi FK
  }
  pengguna {
    BIGINT id PK
    BIGINT id_warga FK,UK
    VARCHAR email UK
    VARCHAR telepon UK
  }
  warga {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_keluarga FK
    CHAR nik UK
  }
  pengumuman {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_rt FK
    BIGINT dibuat_oleh FK
  }
  target_pengumuman {
    BIGINT id PK
    BIGINT id_pengumuman FK
    BIGINT id_rw FK
    BIGINT id_rt FK
  }
  notifikasi {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_pengguna FK
  }
  log_notifikasi {
    BIGINT id PK
    BIGINT id_notifikasi FK
  }
  kegiatan {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_rt FK
    BIGINT dibuat_oleh FK
  }
  kehadiran_kegiatan {
    BIGINT id PK
    BIGINT id_kegiatan FK
    BIGINT id_warga FK
  }
  pengaduan {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_warga FK
    BIGINT ditugaskan_ke FK
    BIGINT id_berkas FK
  }
  tanggapan_pengaduan {
    BIGINT id PK
    BIGINT id_pengaduan FK
    BIGINT id_pengguna_penanggap FK
  }
  kontak_darurat {
    BIGINT id PK
    BIGINT id_organisasi FK
  }
  survei {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT dibuat_oleh FK
  }
  jawaban_survei {
    BIGINT id PK
    BIGINT id_survei FK
    BIGINT id_warga FK
  }
  berkas {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT diunggah_oleh FK
  }
  pengguna |o--o{ berkas : "diunggah_oleh"
  organisasi ||--o{ berkas : "id_organisasi"
  warga ||--o{ jawaban_survei : "id_warga"
  survei ||--o{ jawaban_survei : "id_survei"
  rt |o--o{ kegiatan : "id_rt"
  pengguna |o--o{ kegiatan : "dibuat_oleh"
  organisasi ||--o{ kegiatan : "id_organisasi"
  kegiatan ||--o{ kehadiran_kegiatan : "id_kegiatan"
  warga ||--o{ kehadiran_kegiatan : "id_warga"
  organisasi ||--o{ kontak_darurat : "id_organisasi"
  notifikasi ||--o{ log_notifikasi : "id_notifikasi"
  organisasi |o--o{ notifikasi : "id_organisasi"
  pengguna ||--o{ notifikasi : "id_pengguna"
  organisasi ||--o{ pengaduan : "id_organisasi"
  berkas |o--o{ pengaduan : "id_berkas"
  warga ||--o{ pengaduan : "id_warga"
  pengguna |o--o{ pengaduan : "ditugaskan_ke"
  warga |o--o| pengguna : "id_warga"
  rt |o--o{ pengumuman : "id_rt"
  pengguna |o--o{ pengumuman : "dibuat_oleh"
  organisasi ||--o{ pengumuman : "id_organisasi"
  rw ||--o{ rt : "id_rw"
  organisasi ||--o{ rt : "id_organisasi"
  organisasi ||--o{ rw : "id_organisasi"
  pengguna |o--o{ survei : "dibuat_oleh"
  organisasi ||--o{ survei : "id_organisasi"
  pengguna ||--o{ tanggapan_pengaduan : "id_pengguna_penanggap"
  pengaduan ||--o{ tanggapan_pengaduan : "id_pengaduan"
  pengumuman ||--o{ target_pengumuman : "id_pengumuman"
  rw |o--o{ target_pengumuman : "id_rw"
  rt |o--o{ target_pengumuman : "id_rt"
  organisasi ||--o{ warga : "id_organisasi"
```

## Sistem dan keamanan

```mermaid
erDiagram
  organisasi {
    BIGINT id PK
    VARCHAR kode UK
  }
  pengguna {
    BIGINT id PK
    BIGINT id_warga FK,UK
    VARCHAR email UK
    VARCHAR telepon UK
  }
  log_audit {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT id_pengguna FK
  }
  pengaturan {
    BIGINT id PK
    BIGINT id_organisasi FK
  }
  cadangan_data {
    BIGINT id PK
    BIGINT id_organisasi FK
    BIGINT dibuat_oleh FK
  }
  organisasi |o--o{ cadangan_data : "id_organisasi"
  pengguna |o--o{ cadangan_data : "dibuat_oleh"
  pengguna |o--o{ log_audit : "id_pengguna"
  organisasi |o--o{ log_audit : "id_organisasi"
  organisasi ||--o{ pengaturan : "id_organisasi"
```

## Catatan desain

1. **Relasi melingkar `keluarga` ↔ `warga`.** `warga.id_keluarga` menunjuk keluarga, dan `keluarga.id_kepala_keluarga` menunjuk warga. Saat mengisi data, buat keluarga dulu (kepala kosong), lalu warga, lalu isi `id_kepala_keluarga`.
2. **Akun terhubung ke warga lewat `pengguna.id_warga`** (UNIQUE, boleh NULL untuk Super Admin).
3. **Peran dan wilayah ada di `penugasan_pengguna`.** Satu pengguna dapat memiliki beberapa penugasan (peran + organisasi/RW/RT).
4. **Hak akses berbasis peran.** `peran` → `peran_izin_akses` → `izin_akses`. Kolom `terbatas = 1` berarti izin bertanda `*` di matriks (hanya data milik sendiri / wilayahnya).
5. **Alur uang:** `tagihan` → `pembayaran` → (`bukti_pembayaran`, `kuitansi`, `transaksi_kas`). Satu tagihan boleh memiliki banyak pembayaran (cicilan).
6. **Berkas** disimpan sebagai metadata di tabel `berkas`; file aslinya berada di penyimpanan privat di luar database.
7. **`id_organisasi`** ada di tabel yang di-query langsung. Tabel anak (`rincian_tagihan`, `bukti_pembayaran`, `persetujuan_surat`, `target_pengumuman`, `kehadiran_kegiatan`, `tanggapan_pengaduan`, `jawaban_survei`, `log_notifikasi`, `peran_izin_akses`) mengikuti organisasi tabel induknya.
