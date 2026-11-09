# Dokumentasi UI/UX: Sistem Informasi RT/RW

Folder ini berisi rancangan antarmuka (UI/UX) **Sistem Informasi RT/RW** milik kelompok kami. Rancangan masih berupa **draft desain** dan bukan frontend final. Isi tampilan mengikuti ERD (41 tabel) serta daftar modul yang sudah disepakati.

**Status tahap ini:** desain **login, beranda, dan 11 modul (menu)**, masing-masing dalam mode terang dan gelap (26 gambar). Desain tampilan **fitur di dalam tiap modul** dikerjakan pada tahap berikutnya.

## Daftar Isi

1. [Tujuan Perancangan](#1-tujuan-perancangan)
2. [Tools yang Digunakan](#2-tools-yang-digunakan)
3. [Struktur Folder](#3-struktur-folder)
4. [Penjelasan Halaman Desain](#4-penjelasan-halaman-desain)
5. [Catatan](#5-catatan)

---

## 1. Tujuan Perancangan

1. Memberi gambaran awal alur aplikasi, mulai dari **login**, **beranda**, sampai **menu modul**.
2. Menjadi acuan bersama anggota kelompok sebelum frontend dikerjakan, supaya tata letak, warna, dan istilah seragam.
3. Memastikan **nama modul dan cakupannya sejalan dengan ERD** dan daftar fitur sistem.
4. Menguji ide dan menampung revisi lebih awal, karena mengubah gambar desain lebih murah daripada mengubah kode.
5. Menyediakan **mode terang dan mode gelap** agar nyaman dipakai di berbagai kondisi.

---

## 2. Tools yang Digunakan

| Kebutuhan | Tools |
|---|---|
| Perancangan awal dan prototipe | Claude (Anthropic), untuk menurunkan modul dan ERD menjadi layar desain |
| Prototipe tampilan | HTML, CSS, dan JavaScript (tanpa framework) |
| Font | Plus Jakarta Sans (Google Fonts) |
| Pengambilan gambar | Screenshot halaman prototipe pada layar desktop |
| Acuan | `erd.md` (diagram Mermaid dari `database/schema.sql`) dan tabel modul serta fitur |
| Dokumentasi | GitHub (Markdown) |

> Tambahkan tools lain bila kelompok memakainya, misalnya Figma atau Canva.

**Gaya visual**

- Warna utama hijau pinus dengan aksen kuning, supaya terasa seperti lingkungan perumahan.
- Tata letak: sidebar menu di kiri dan area konten di kanan.
- Tombol **Terang/Gelap** di pojok untuk berganti tema.

---

## 3. Struktur Folder

```
UI-UX/
├── README.md
└── 01-desain-modul-menu/
    ├── 01-login-gelap.png
    ├── 02-login-terang.png
    ├── 03-beranda-terang.png
    ├── 04-beranda-gelap.png
    ├── 05-dashboard-laporan-terang.png  ...  26-manajemen-platform-gelap.png
```

Nama file diawali nomor urut (01 sampai 26) dan diakhiri `terang` atau `gelap`. Fitur per modul akan ditambahkan di folder `02-desain-fitur/` pada tahap berikutnya.

---

## 4. Penjelasan Halaman Desain

### 4.1 Halaman Login

| Mode terang | Mode gelap |
|---|---|
| <img src="./01-desain-modul-menu/02-login-terang.png" width="480" alt="Mode terang"> | <img src="./01-desain-modul-menu/01-login-gelap.png" width="480" alt="Mode gelap"> |

**Penjelasan:** Halaman pertama yang dilihat pengguna. Panel kiri berisi identitas aplikasi dan slogan, sedangkan panel kanan berisi form masuk dengan kolom **email atau telepon** dan **kata sandi**, pilihan *Ingat saya*, serta tautan *Lupa kata sandi*. Tombol di pojok kanan atas dipakai untuk berganti tema. Pendaftaran akun baru dirancang sebagai fitur pada modul *Registrasi & Akses*.
**Tabel ERD terkait:** `pengguna`, `sesi_pengguna`

### 4.2 Beranda

| Mode terang | Mode gelap |
|---|---|
| <img src="./01-desain-modul-menu/03-beranda-terang.png" width="480" alt="Mode terang"> | <img src="./01-desain-modul-menu/04-beranda-gelap.png" width="480" alt="Mode gelap"> |

**Penjelasan:** Halaman utama setelah login. Sidebar di kiri memuat **11 modul** sebagai menu navigasi, ditambah menu *Beranda*. Area utama menampilkan kartu tiap modul lengkap dengan deskripsi singkat dan jumlah fiturnya. Bagian bawah sidebar menampilkan pengguna yang sedang login, wilayahnya (contoh: RT 03 / RW 05), dan tombol *Keluar*.
**Tabel ERD terkait:** `pengguna`, `penugasan_pengguna`, `peran`, `rt`, `rw`

### 4.3 Halaman Modul (Menu)

Setiap modul punya halaman sendiri. Menu yang aktif ditandai pada sidebar, dan area utama menampilkan **kartu fitur** milik modul tersebut beserta deskripsinya. Tampilan isi tiap fitur dirancang pada tahap berikutnya.

#### 4.3.1 Dashboard & Laporan

| Mode terang | Mode gelap |
|---|---|
| <img src="./01-desain-modul-menu/05-dashboard-laporan-terang.png" width="480" alt="Mode terang"> | <img src="./01-desain-modul-menu/06-dashboard-laporan-gelap.png" width="480" alt="Mode gelap"> |

**Penjelasan:** Ringkasan data dan laporan lingkungan. Modul ini memuat 2 fitur: Dashboard statistik interaktif; Laporan PDF/Excel.
**Tabel ERD terkait:** `transaksi_kas`, `tagihan`, `pembayaran`, `warga`

#### 4.3.2 Manajemen Data Warga

| Mode terang | Mode gelap |
|---|---|
| <img src="./01-desain-modul-menu/07-manajemen-data-warga-terang.png" width="480" alt="Mode terang"> | <img src="./01-desain-modul-menu/08-manajemen-data-warga-gelap.png" width="480" alt="Mode gelap"> |

**Penjelasan:** Data warga, keluarga, dan mutasi. Modul ini memuat 2 fitur: Data warga & kepala keluarga; Mutasi dan riwayat warga.
**Tabel ERD terkait:** `warga`, `keluarga`, `mutasi_warga`

#### 4.3.3 Keuangan RT/RW

| Mode terang | Mode gelap |
|---|---|
| <img src="./01-desain-modul-menu/09-keuangan-rtrw-terang.png" width="480" alt="Mode terang"> | <img src="./01-desain-modul-menu/10-keuangan-rtrw-gelap.png" width="480" alt="Mode gelap"> |

**Penjelasan:** Iuran, tagihan, pembayaran, dan kas. Modul ini memuat 7 fitur: Kategori dan aturan iuran; Tagihan otomatis; Pembayaran QRIS/transfer/tunai; Verifikasi bukti pembayaran; Riwayat pembayaran & kuitansi; Kas masuk dan pengeluaran; Pengingat iuran/tunggakan.
**Tabel ERD terkait:** `kategori_iuran`, `aturan_iuran`, `tagihan`, `pembayaran`, `bukti_pembayaran`, `kuitansi`, `transaksi_kas`

#### 4.3.4 Administrasi Surat

| Mode terang | Mode gelap |
|---|---|
| <img src="./01-desain-modul-menu/11-administrasi-surat-terang.png" width="480" alt="Mode terang"> | <img src="./01-desain-modul-menu/12-administrasi-surat-gelap.png" width="480" alt="Mode gelap"> |

**Penjelasan:** Pengajuan dan persetujuan surat. Modul ini memuat 2 fitur: Pengajuan surat online; Verifikasi dan persetujuan surat.
**Tabel ERD terkait:** `jenis_surat`, `pengajuan_surat`, `persetujuan_surat`

#### 4.3.5 Layanan Warga

| Mode terang | Mode gelap |
|---|---|
| <img src="./01-desain-modul-menu/13-layanan-warga-terang.png" width="480" alt="Mode terang"> | <img src="./01-desain-modul-menu/14-layanan-warga-gelap.png" width="480" alt="Mode gelap"> |

**Penjelasan:** Penilaian warga atas layanan pengurus. Modul ini memuat 1 fitur: Survei kepuasan warga.
**Tabel ERD terkait:** `survei`, `jawaban_survei`

#### 4.3.6 Informasi & Komunikasi

| Mode terang | Mode gelap |
|---|---|
| <img src="./01-desain-modul-menu/15-informasi-komunikasi-terang.png" width="480" alt="Mode terang"> | <img src="./01-desain-modul-menu/16-informasi-komunikasi-gelap.png" width="480" alt="Mode gelap"> |

**Penjelasan:** Pengumuman dan notifikasi. Modul ini memuat 2 fitur: Pengumuman RT/RW; Notifikasi aplikasi.
**Tabel ERD terkait:** `pengumuman`, `target_pengumuman`, `notifikasi`, `log_notifikasi`

#### 4.3.7 Sosial & Kegiatan

| Mode terang | Mode gelap |
|---|---|
| <img src="./01-desain-modul-menu/17-sosial-kegiatan-terang.png" width="480" alt="Mode terang"> | <img src="./01-desain-modul-menu/18-sosial-kegiatan-gelap.png" width="480" alt="Mode gelap"> |

**Penjelasan:** Agenda kerja bakti dan kegiatan sosial. Modul ini memuat 1 fitur: Jadwal kegiatan/kerja bakti.
**Tabel ERD terkait:** `kegiatan`, `kehadiran_kegiatan`

#### 4.3.8 Keamanan & Lingkungan

| Mode terang | Mode gelap |
|---|---|
| <img src="./01-desain-modul-menu/19-keamanan-lingkungan-terang.png" width="480" alt="Mode terang"> | <img src="./01-desain-modul-menu/20-keamanan-lingkungan-gelap.png" width="480" alt="Mode gelap"> |

**Penjelasan:** Laporan kejadian dan kontak darurat. Modul ini memuat 2 fitur: Laporan keluhan/kejadian; Kontak darurat/direktori layanan.
**Tabel ERD terkait:** `pengaduan`, `tanggapan_pengaduan`, `kontak_darurat`

#### 4.3.9 Registrasi & Akses

| Mode terang | Mode gelap |
|---|---|
| <img src="./01-desain-modul-menu/21-registrasi-akses-terang.png" width="480" alt="Mode terang"> | <img src="./01-desain-modul-menu/22-registrasi-akses-gelap.png" width="480" alt="Mode gelap"> |

**Penjelasan:** Pendaftaran, verifikasi, dan hak akses. Modul ini memuat 3 fitur: Registrasi warga; Verifikasi akun oleh Admin; Manajemen peran dan hak akses.
**Tabel ERD terkait:** `pengguna`, `kode_verifikasi`, `peran`, `izin_akses`, `penugasan_pengguna`

#### 4.3.10 Keamanan Akun

| Mode terang | Mode gelap |
|---|---|
| <img src="./01-desain-modul-menu/23-keamanan-akun-terang.png" width="480" alt="Mode terang"> | <img src="./01-desain-modul-menu/24-keamanan-akun-gelap.png" width="480" alt="Mode gelap"> |

**Penjelasan:** Riwayat aktivitas dan sesi login. Modul ini memuat 2 fitur: Riwayat aktivitas/audit log; Manajemen sesi/perangkat.
**Tabel ERD terkait:** `log_audit`, `sesi_pengguna`

#### 4.3.11 Manajemen Platform

| Mode terang | Mode gelap |
|---|---|
| <img src="./01-desain-modul-menu/25-manajemen-platform-terang.png" width="480" alt="Mode terang"> | <img src="./01-desain-modul-menu/26-manajemen-platform-gelap.png" width="480" alt="Mode gelap"> |

**Penjelasan:** Multi-lingkungan serta ekspor dan backup data. Modul ini memuat 2 fitur: Multi-RT/RW; Ekspor dan backup data.
**Tabel ERD terkait:** `organisasi`, `rw`, `rt`, `cadangan_data`, `pengaturan`

---

## 5. Catatan

1. **Status:** semua gambar adalah **draft desain**, bukan tampilan final. Warna, tata letak, dan istilah masih bisa berubah mengikuti masukan kelompok atau dosen.
2. **Cakupan tahap ini:** login, beranda, dan 11 halaman modul (menu). Tampilan isi 26 fitur di dalam modul **belum dirancang** dan akan menyusul.
3. **Mode tampilan:** setiap halaman tersedia dalam mode terang dan gelap.
4. **Ukuran gambar:** diambil dalam tampilan desktop. Pada layar kecil, sidebar akan berubah menjadi menu geser.
5. **Data contoh:** nama pengguna (Admin RT) dan wilayah (RT 03 / RW 05) hanya contoh.
6. **Hak akses:** menu yang tampil akan menyesuaikan peran pengguna (Super Admin, Admin Perumahan, Ketua RT/RW, Bendahara, Pengurus, Warga). Draft ini memperlihatkan seluruh menu seperti yang dilihat admin.
7. **Acuan:** ERD pada `erd.md` adalah sumber utama. Bila ERD atau daftar modul berubah, gambar di folder ini perlu diperbarui.

### Rencana tahap berikutnya

| Tahap | Isi | Status |
|---|---|---|
| 1 | Login, beranda, dan 11 modul (terang dan gelap) | Selesai |
| 2 | Tampilan isi tiap fitur (26 fitur) | Direncanakan |
