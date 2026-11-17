# Kamus Data — Sistem Informasi RT/RW

> Dihasilkan otomatis dari `database/schema.sql`. Jika skema berubah, perbarui dokumen ini (dan catat perubahan di `database/migrations/`).

Keterangan kolom **Kunci**: `PK` primary key, `FK` foreign key (ke tabel yang disebut), `UK` unique tunggal, `UK gabungan` kolom ini bersama kolom lain harus unik (lihat definisi di `schema.sql`).

## Daftar tabel

| No | Tabel | Fungsi |
|---|---|---|
| 1 | [`aturan_iuran`](#aturan_iuran) | Aturan nominal/periode iuran; tarif bisa beda per RT dan jenis hunian |
| 2 | [`berkas`](#berkas) | Metadata file/lampiran; isi file TIDAK disimpan di database |
| 3 | [`bukti_pembayaran`](#bukti_pembayaran) | Bukti transfer; akses hanya pihak berwenang |
| 4 | [`cadangan_data`](#cadangan_data) | Riwayat pencadangan (F-026). Simpan file backup di luar repository |
| 5 | [`izin_akses`](#izin_akses) | Master hak akses rinci per modul/aksi |
| 6 | [`jawaban_survei`](#jawaban_survei) | Jawaban survei; satu jawaban per warga. Bila survei anonim, API tidak boleh menampilkan id_warga |
| 7 | [`jenis_surat`](#jenis_surat) | Master jenis surat dan templatnya |
| 8 | [`kategori_iuran`](#kategori_iuran) | Master kategori iuran (kebersihan, keamanan, sosial, ...) |
| 9 | [`kategori_pengeluaran`](#kategori_pengeluaran) | Kategori pengeluaran kas |
| 10 | [`kegiatan`](#kegiatan) | Agenda kegiatan warga (F-019) |
| 11 | [`kehadiran_kegiatan`](#kehadiran_kegiatan) | RSVP/absensi kegiatan (opsional) |
| 12 | [`keluarga`](#keluarga) | Data keluarga/rumah tangga. No. KK sensitif, batasi akses |
| 13 | [`kode_verifikasi`](#kode_verifikasi) | Kode OTP registrasi/reset sandi (F-001); tabel tambahan di luar 40 tabel awal |
| 14 | [`kontak_darurat`](#kontak_darurat) | Direktori kontak darurat dan layanan (F-021) |
| 15 | [`kuitansi`](#kuitansi) | Kuitansi digital; terbit setelah pembayaran terverifikasi |
| 16 | [`log_audit`](#log_audit) | Jejak aktivitas penting (F-023). Hanya INSERT; jangan di-UPDATE/DELETE oleh aplikasi |
| 17 | [`log_notifikasi`](#log_notifikasi) | Log pengiriman notifikasi per kanal |
| 18 | [`mutasi_warga`](#mutasi_warga) | Riwayat mutasi warga (F-005) |
| 19 | [`notifikasi`](#notifikasi) | Notifikasi aplikasi (F-018) dan pengingat iuran (F-014) |
| 20 | [`organisasi`](#organisasi) | Induk lingkungan/perumahan (multi-organisasi) |
| 21 | [`pembayaran`](#pembayaran) | Pembayaran tagihan; satu tagihan bisa punya banyak pembayaran/cicilan |
| 22 | [`pengaduan`](#pengaduan) | Laporan keluhan/kejadian (F-020) |
| 23 | [`pengajuan_surat`](#pengajuan_surat) | Pengajuan surat online (F-006) |
| 24 | [`pengaturan`](#pengaturan) | Konfigurasi per lingkungan: identitas, format nomor surat/kuitansi, preferensi |
| 25 | [`pengguna`](#pengguna) | Akun seluruh pengguna |
| 26 | [`pengumuman`](#pengumuman) | Pengumuman RT/RW |
| 27 | [`penugasan_pengguna`](#penugasan_pengguna) | Penempatan akun ke peran dan wilayah; satu pengguna bisa punya banyak penugasan |
| 28 | [`peran`](#peran) | Master peran pengguna |
| 29 | [`peran_izin_akses`](#peran_izin_akses) | Relasi peran dan izin akses |
| 30 | [`periode_tagihan`](#periode_tagihan) | Periode penagihan; UNIQUE mencegah periode ganda |
| 31 | [`persetujuan_surat`](#persetujuan_surat) | Riwayat persetujuan surat (F-007) |
| 32 | [`rincian_tagihan`](#rincian_tagihan) | Komponen tagihan; satu tagihan bisa berisi beberapa kategori iuran |
| 33 | [`rt`](#rt) | Data RT; berada di bawah RW |
| 34 | [`rw`](#rw) | Data RW; satu organisasi punya beberapa RW |
| 35 | [`sesi_pengguna`](#sesi_pengguna) | Sesi/perangkat aktif pengguna (F-024) |
| 36 | [`survei`](#survei) | Survei kepuasan warga (F-022) |
| 37 | [`tagihan`](#tagihan) | Tagihan per keluarga per periode; mendukung bayar sebagian |
| 38 | [`tanggapan_pengaduan`](#tanggapan_pengaduan) | Histori tindak lanjut pengaduan |
| 39 | [`target_pengumuman`](#target_pengumuman) | Target multi-wilayah pengumuman (opsional) |
| 40 | [`transaksi_kas`](#transaksi_kas) | Kas masuk/keluar; kas masuk dicatat setelah pembayaran terverifikasi |
| 41 | [`warga`](#warga) | Data individu warga. NIK sensitif: samarkan di API, akses terbatas |

## aturan_iuran

Aturan nominal/periode iuran; tarif bisa beda per RT dan jenis hunian

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id |  |
| `id_kategori_iuran` | bigint(20) unsigned | Tidak |  | FK → kategori_iuran.id |  |
| `id_rt` | bigint(20) unsigned | Ya | NULL | FK → rt.id | NULL = berlaku untuk semua RT |
| `jenis_hunian` | enum('milik_sendiri','kontrak','kos','kosong') | Ya | NULL |  | NULL = semua jenis hunian |
| `nominal` | decimal(15,2) | Tidak |  |  |  |
| `siklus_tagihan` | enum('bulanan','tahunan','sekali') | Tidak | bulanan |  |  |
| `hari_jatuh_tempo` | tinyint(3) unsigned | Tidak | 10 |  | Tanggal jatuh tempo tiap periode (1-28) |
| `boleh_cicilan` | tinyint(1) | Tidak | 1 |  |  |
| `berlaku_mulai` | date | Tidak |  |  |  |
| `berlaku_sampai` | date | Ya | NULL |  |  |
| `aktif` | tinyint(1) | Tidak | 1 |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |

## berkas

Metadata file/lampiran; isi file TIDAK disimpan di database

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id |  |
| `path_penyimpanan` | varchar(500) | Tidak |  |  | Lokasi di penyimpanan PRIVAT (di luar folder publik web) |
| `nama_asli` | varchar(255) | Tidak |  |  |  |
| `tipe_mime` | varchar(100) | Tidak |  |  |  |
| `ukuran` | bigint(20) unsigned | Tidak |  |  | Byte |
| `hash_berkas` | char(64) | Ya | NULL |  | SHA-256 isi berkas (opsional, untuk deteksi duplikat) |
| `diunggah_oleh` | bigint(20) unsigned | Ya | NULL | FK → pengguna.id |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `dihapus_pada` | datetime | Ya | NULL |  |  |

## bukti_pembayaran

Bukti transfer; akses hanya pihak berwenang

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_pembayaran` | bigint(20) unsigned | Tidak |  | FK → pembayaran.id |  |
| `id_berkas` | bigint(20) unsigned | Tidak |  | FK → berkas.id |  |
| `diunggah_oleh` | bigint(20) unsigned | Ya | NULL | FK → pengguna.id |  |
| `diunggah_pada` | datetime | Tidak | current_timestamp() |  |  |
| `status_verifikasi` | enum('menunggu','diterima','ditolak') | Tidak | menunggu |  |  |
| `catatan` | varchar(255) | Ya | NULL |  |  |

## cadangan_data

Riwayat pencadangan (F-026). Simpan file backup di luar repository

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Ya | NULL | FK → organisasi.id | NULL = cadangan seluruh platform |
| `path_berkas` | varchar(500) | Tidak |  |  |  |
| `jenis_cadangan` | enum('penuh','inkremental','per_organisasi') | Tidak | penuh |  |  |
| `status` | enum('berjalan','berhasil','gagal') | Tidak | berjalan |  |  |
| `ukuran` | bigint(20) unsigned | Ya | NULL |  |  |
| `terenkripsi` | tinyint(1) | Tidak | 1 |  |  |
| `dibuat_oleh` | bigint(20) unsigned | Ya | NULL | FK → pengguna.id | NULL = dijalankan terjadwal |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `selesai_pada` | datetime | Ya | NULL |  |  |

## izin_akses

Master hak akses rinci per modul/aksi

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `kode` | varchar(60) | Tidak |  | UK | Format modul.aksi, mis. tagihan.read |
| `nama` | varchar(100) | Tidak |  |  |  |
| `modul` | varchar(40) | Tidak |  | UK gabungan |  |
| `aksi` | enum('create','read','update','delete','approve') | Tidak |  | UK gabungan |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |

## jawaban_survei

Jawaban survei; satu jawaban per warga. Bila survei anonim, API tidak boleh menampilkan id_warga

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_survei` | bigint(20) unsigned | Tidak |  | FK → survei.id, UK gabungan |  |
| `id_warga` | bigint(20) unsigned | Tidak |  | FK → warga.id, UK gabungan |  |
| `penilaian` | tinyint(3) unsigned | Tidak |  |  | Skala 1-5 |
| `komentar` | varchar(1000) | Ya | NULL |  |  |
| `dijawab_pada` | datetime | Tidak | current_timestamp() |  |  |

## jenis_surat

Master jenis surat dan templatnya

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id, UK gabungan |  |
| `kode` | varchar(20) | Tidak |  | UK gabungan | Dipakai dalam format nomor surat |
| `nama` | varchar(100) | Tidak |  |  |  |
| `templat` | mediumtext | Ya | NULL |  |  |
| `isian_wajib` | longtext | Ya | NULL |  | Daftar field tambahan yang harus diisi warga |
| `aktif` | tinyint(1) | Tidak | 1 |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |

## kategori_iuran

Master kategori iuran (kebersihan, keamanan, sosial, ...)

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id, UK gabungan |  |
| `nama` | varchar(100) | Tidak |  | UK gabungan |  |
| `jenis_iuran` | enum('wajib','sukarela') | Tidak | wajib |  |  |
| `deskripsi` | varchar(255) | Ya | NULL |  |  |
| `aktif` | tinyint(1) | Tidak | 1 |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |

## kategori_pengeluaran

Kategori pengeluaran kas

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id, UK gabungan |  |
| `nama` | varchar(100) | Tidak |  | UK gabungan |  |
| `aktif` | tinyint(1) | Tidak | 1 |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |

## kegiatan

Agenda kegiatan warga (F-019)

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id |  |
| `id_rt` | bigint(20) unsigned | Ya | NULL | FK → rt.id | NULL = kegiatan tingkat RW |
| `judul` | varchar(200) | Tidak |  |  |  |
| `deskripsi` | text | Ya | NULL |  |  |
| `jenis` | enum('kerja_bakti','rapat','posyandu','sosial','lainnya') | Tidak | lainnya |  |  |
| `mulai_pada` | datetime | Tidak |  |  |  |
| `selesai_pada` | datetime | Ya | NULL |  |  |
| `lokasi` | varchar(200) | Ya | NULL |  |  |
| `dibuat_oleh` | bigint(20) unsigned | Ya | NULL | FK → pengguna.id |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |
| `dihapus_pada` | datetime | Ya | NULL |  |  |

## kehadiran_kegiatan

RSVP/absensi kegiatan (opsional)

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_kegiatan` | bigint(20) unsigned | Tidak |  | FK → kegiatan.id, UK gabungan |  |
| `id_warga` | bigint(20) unsigned | Tidak |  | FK → warga.id, UK gabungan |  |
| `status_kehadiran` | enum('akan_hadir','hadir','tidak_hadir') | Tidak | akan_hadir |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |

## keluarga

Data keluarga/rumah tangga. No. KK sensitif, batasi akses

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id |  |
| `id_rt` | bigint(20) unsigned | Tidak |  | FK → rt.id |  |
| `nomor_kk` | char(16) | Tidak |  | UK |  |
| `alamat` | varchar(255) | Tidak |  |  |  |
| `nomor_rumah` | varchar(20) | Ya | NULL |  |  |
| `jenis_hunian` | enum('milik_sendiri','kontrak','kos','kosong') | Tidak | milik_sendiri |  | Dipakai aturan_iuran (tarif per jenis hunian) |
| `id_kepala_keluarga` | bigint(20) unsigned | Ya | NULL | FK → warga.id |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |
| `dihapus_pada` | datetime | Ya | NULL |  |  |

## kode_verifikasi

Kode OTP registrasi/reset sandi (F-001); tabel tambahan di luar 40 tabel awal

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_pengguna` | bigint(20) unsigned | Tidak |  | FK → pengguna.id |  |
| `tujuan` | enum('registrasi','reset_sandi','login') | Tidak |  |  |  |
| `kanal` | enum('email','telepon') | Tidak |  |  |  |
| `hash_kode` | varchar(255) | Tidak |  |  | Hash dari kode OTP, bukan OTP asli |
| `jumlah_percobaan` | tinyint(3) unsigned | Tidak | 0 |  |  |
| `kedaluwarsa_pada` | datetime | Tidak |  |  |  |
| `digunakan_pada` | datetime | Ya | NULL |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |

## kontak_darurat

Direktori kontak darurat dan layanan (F-021)

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id |  |
| `kategori` | enum('pengurus','keamanan','ambulans','puskesmas','pemadam_kebakaran','polisi','lainnya') | Tidak |  |  |  |
| `nama` | varchar(150) | Tidak |  |  |  |
| `telepon` | varchar(30) | Tidak |  |  |  |
| `alamat` | varchar(255) | Ya | NULL |  |  |
| `urutan` | smallint(5) unsigned | Tidak | 0 |  |  |
| `aktif` | tinyint(1) | Tidak | 1 |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |

## kuitansi

Kuitansi digital; terbit setelah pembayaran terverifikasi

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id, UK gabungan |  |
| `id_pembayaran` | bigint(20) unsigned | Tidak |  | FK → pembayaran.id, UK |  |
| `nomor_kuitansi` | varchar(40) | Tidak |  | UK gabungan |  |
| `id_berkas` | bigint(20) unsigned | Ya | NULL | FK → berkas.id | File PDF kuitansi |
| `diterbitkan_pada` | datetime | Tidak | current_timestamp() |  |  |

## log_audit

Jejak aktivitas penting (F-023). Hanya INSERT; jangan di-UPDATE/DELETE oleh aplikasi

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Ya | NULL | FK → organisasi.id |  |
| `id_pengguna` | bigint(20) unsigned | Ya | NULL | FK → pengguna.id |  |
| `aksi` | varchar(50) | Tidak |  |  | Mis. login, buat, ubah, hapus, verifikasi_pembayaran, setujui_surat |
| `nama_tabel` | varchar(64) | Ya | NULL |  |  |
| `id_data` | bigint(20) unsigned | Ya | NULL |  |  |
| `nilai_lama` | longtext | Ya | NULL |  |  |
| `nilai_baru` | longtext | Ya | NULL |  |  |
| `alamat_ip` | varchar(45) | Ya | NULL |  |  |
| `agen_pengguna` | varchar(255) | Ya | NULL |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |

## log_notifikasi

Log pengiriman notifikasi per kanal

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_notifikasi` | bigint(20) unsigned | Tidak |  | FK → notifikasi.id |  |
| `kanal` | enum('aplikasi','email','push','whatsapp') | Tidak |  |  |  |
| `tujuan` | varchar(150) | Ya | NULL |  |  |
| `status` | enum('antre','terkirim','gagal') | Tidak | antre |  |  |
| `pesan_galat` | varchar(255) | Ya | NULL |  |  |
| `dikirim_pada` | datetime | Ya | NULL |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |

## mutasi_warga

Riwayat mutasi warga (F-005)

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id |  |
| `id_warga` | bigint(20) unsigned | Tidak |  | FK → warga.id |  |
| `jenis_mutasi` | enum('pindah_masuk','pindah_keluar','lahir','meninggal','perubahan_status') | Tidak |  |  |  |
| `tanggal_berlaku` | date | Tidak |  |  |  |
| `asal_tujuan` | varchar(255) | Ya | NULL |  | Asal (pindah masuk) atau tujuan (pindah keluar) |
| `catatan` | varchar(500) | Ya | NULL |  |  |
| `dicatat_oleh` | bigint(20) unsigned | Ya | NULL | FK → pengguna.id |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |

## notifikasi

Notifikasi aplikasi (F-018) dan pengingat iuran (F-014)

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Ya | NULL | FK → organisasi.id |  |
| `id_pengguna` | bigint(20) unsigned | Tidak |  | FK → pengguna.id |  |
| `judul` | varchar(200) | Tidak |  |  |  |
| `isi` | varchar(500) | Tidak |  |  |  |
| `jenis` | enum('tagihan','pembayaran','surat','pengumuman','kegiatan','pengaduan','sistem') | Tidak |  |  |  |
| `tabel_referensi` | varchar(50) | Ya | NULL |  | Mis. tagihan, pengajuan_surat (untuk membuka detail) |
| `id_referensi` | bigint(20) unsigned | Ya | NULL |  |  |
| `dibaca_pada` | datetime | Ya | NULL |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |

## organisasi

Induk lingkungan/perumahan (multi-organisasi)

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `kode` | varchar(20) | Tidak |  | UK |  |
| `nama` | varchar(150) | Tidak |  |  |  |
| `alamat` | varchar(255) | Ya | NULL |  |  |
| `status` | enum('aktif','nonaktif') | Tidak | aktif |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |
| `dihapus_pada` | datetime | Ya | NULL |  |  |

## pembayaran

Pembayaran tagihan; satu tagihan bisa punya banyak pembayaran/cicilan

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id |  |
| `id_tagihan` | bigint(20) unsigned | Tidak |  | FK → tagihan.id |  |
| `id_pengguna_pembayar` | bigint(20) unsigned | Ya | NULL | FK → pengguna.id | NULL bila tunai dicatat bendahara untuk warga tanpa akun |
| `nominal` | decimal(15,2) | Tidak |  |  |  |
| `metode` | enum('qris','transfer','tunai') | Tidak |  |  |  |
| `status` | enum('menunggu_verifikasi','terverifikasi','ditolak','batal') | Tidak | menunggu_verifikasi |  |  |
| `referensi_pembayaran` | varchar(100) | Ya | NULL |  | Kode referensi QRIS/transfer |
| `dibayar_pada` | datetime | Tidak |  |  |  |
| `diverifikasi_oleh` | bigint(20) unsigned | Ya | NULL | FK → pengguna.id |  |
| `diverifikasi_pada` | datetime | Ya | NULL |  |  |
| `catatan_verifikasi` | varchar(255) | Ya | NULL |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |

## pengaduan

Laporan keluhan/kejadian (F-020)

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id |  |
| `id_warga` | bigint(20) unsigned | Tidak |  | FK → warga.id |  |
| `kategori` | enum('kehilangan','keamanan','fasilitas_rusak','kebersihan','lainnya') | Tidak |  |  |  |
| `judul` | varchar(200) | Tidak |  |  |  |
| `deskripsi` | text | Tidak |  |  |  |
| `status` | enum('baru','diproses','selesai','ditolak') | Tidak | baru |  |  |
| `bersifat_privat` | tinyint(1) | Tidak | 0 |  | 1 = hanya pengurus yang dapat melihat |
| `ditugaskan_ke` | bigint(20) unsigned | Ya | NULL | FK → pengguna.id |  |
| `id_berkas` | bigint(20) unsigned | Ya | NULL | FK → berkas.id | Satu lampiran (foto) opsional |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |

## pengajuan_surat

Pengajuan surat online (F-006)

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id, UK gabungan |  |
| `id_warga` | bigint(20) unsigned | Tidak |  | FK → warga.id |  |
| `id_jenis_surat` | bigint(20) unsigned | Tidak |  | FK → jenis_surat.id |  |
| `id_pengguna_pengaju` | bigint(20) unsigned | Ya | NULL | FK → pengguna.id |  |
| `keperluan` | varchar(255) | Tidak |  |  |  |
| `data_isian` | longtext | Ya | NULL |  |  |
| `status` | enum('diajukan','diproses','disetujui','ditolak','diterbitkan','dibatalkan') | Tidak | diajukan |  |  |
| `diajukan_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diproses_oleh` | bigint(20) unsigned | Ya | NULL | FK → pengguna.id |  |
| `nomor_surat` | varchar(60) | Ya | NULL | UK gabungan | Terisi saat surat diterbitkan |
| `diterbitkan_pada` | datetime | Ya | NULL |  |  |
| `id_berkas` | bigint(20) unsigned | Ya | NULL | FK → berkas.id | File PDF surat hasil cetak |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |

## pengaturan

Konfigurasi per lingkungan: identitas, format nomor surat/kuitansi, preferensi

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id, UK gabungan |  |
| `kunci_pengaturan` | varchar(80) | Tidak |  | UK gabungan |  |
| `nilai_pengaturan` | text | Ya | NULL |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |

## pengguna

Akun seluruh pengguna

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_warga` | bigint(20) unsigned | Ya | NULL | FK → warga.id, UK | Akun terhubung ke warga; NULL untuk Super Admin |
| `nama` | varchar(150) | Tidak |  |  |  |
| `email` | varchar(150) | Ya | NULL | UK |  |
| `telepon` | varchar(20) | Ya | NULL | UK |  |
| `hash_kata_sandi` | varchar(255) | Tidak |  |  | Hasil password_hash() PHP, JANGAN simpan teks biasa |
| `status` | enum('menunggu_verifikasi','aktif','ditangguhkan','nonaktif') | Tidak | menunggu_verifikasi |  |  |
| `email_terverifikasi_pada` | datetime | Ya | NULL |  |  |
| `telepon_terverifikasi_pada` | datetime | Ya | NULL |  |  |
| `percobaan_login_gagal` | smallint(5) unsigned | Tidak | 0 |  |  |
| `terkunci_sampai` | datetime | Ya | NULL |  | Pembatasan percobaan login (F-024) |
| `login_terakhir_pada` | datetime | Ya | NULL |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |
| `dihapus_pada` | datetime | Ya | NULL |  |  |

## pengumuman

Pengumuman RT/RW

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id |  |
| `id_rt` | bigint(20) unsigned | Ya | NULL | FK → rt.id | NULL = tingkat RW / seluruh organisasi |
| `judul` | varchar(200) | Tidak |  |  |  |
| `isi` | text | Tidak |  |  |  |
| `dibuat_oleh` | bigint(20) unsigned | Ya | NULL | FK → pengguna.id |  |
| `dipublikasikan_pada` | datetime | Ya | NULL |  |  |
| `kedaluwarsa_pada` | datetime | Ya | NULL |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |
| `dihapus_pada` | datetime | Ya | NULL |  |  |

## penugasan_pengguna

Penempatan akun ke peran dan wilayah; satu pengguna bisa punya banyak penugasan

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_pengguna` | bigint(20) unsigned | Tidak |  | FK → pengguna.id |  |
| `id_peran` | bigint(20) unsigned | Tidak |  | FK → peran.id |  |
| `id_organisasi` | bigint(20) unsigned | Ya | NULL | FK → organisasi.id | NULL hanya untuk Super Admin (lintas organisasi) |
| `id_rw` | bigint(20) unsigned | Ya | NULL | FK → rw.id |  |
| `id_rt` | bigint(20) unsigned | Ya | NULL | FK → rt.id |  |
| `aktif` | tinyint(1) | Tidak | 1 |  |  |
| `mulai_pada` | date | Ya | NULL |  |  |
| `selesai_pada` | date | Ya | NULL |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |

## peran

Master peran pengguna

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `kode` | varchar(30) | Tidak |  | UK |  |
| `nama` | varchar(50) | Tidak |  | UK |  |
| `deskripsi` | varchar(255) | Ya | NULL |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |

## peran_izin_akses

Relasi peran dan izin akses

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_peran` | bigint(20) unsigned | Tidak |  | FK → peran.id, UK gabungan |  |
| `id_izin_akses` | bigint(20) unsigned | Tidak |  | FK → izin_akses.id, UK gabungan |  |
| `terbatas` | tinyint(1) | Tidak | 0 |  | 1 = izin bertanda * di matriks (hanya data milik sendiri / wilayahnya sendiri) |

## periode_tagihan

Periode penagihan; UNIQUE mencegah periode ganda

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id, UK gabungan |  |
| `nama` | varchar(50) | Tidak |  |  | Contoh: Oktober 2026 |
| `periode_mulai` | date | Tidak |  | UK gabungan |  |
| `periode_akhir` | date | Tidak |  | UK gabungan |  |
| `status` | enum('draft','terbit','ditutup') | Tidak | draft |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |

## persetujuan_surat

Riwayat persetujuan surat (F-007)

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_pengajuan_surat` | bigint(20) unsigned | Tidak |  | FK → pengajuan_surat.id |  |
| `id_pengguna_penyetuju` | bigint(20) unsigned | Tidak |  | FK → pengguna.id |  |
| `tahap` | tinyint(3) unsigned | Tidak | 1 |  | Mendukung persetujuan bertahap (RT lalu RW) |
| `keputusan` | enum('setuju','tolak','revisi') | Tidak |  |  |  |
| `catatan` | varchar(500) | Ya | NULL |  |  |
| `diputuskan_pada` | datetime | Tidak | current_timestamp() |  |  |

## rincian_tagihan

Komponen tagihan; satu tagihan bisa berisi beberapa kategori iuran

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_tagihan` | bigint(20) unsigned | Tidak |  | FK → tagihan.id, UK gabungan |  |
| `id_kategori_iuran` | bigint(20) unsigned | Tidak |  | FK → kategori_iuran.id, UK gabungan |  |
| `deskripsi` | varchar(255) | Ya | NULL |  |  |
| `nominal` | decimal(15,2) | Tidak |  |  |  |

## rt

Data RT; berada di bawah RW

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id |  |
| `id_rw` | bigint(20) unsigned | Tidak |  | FK → rw.id, UK gabungan |  |
| `nomor_rt` | varchar(5) | Tidak |  | UK gabungan |  |
| `nama_ketua` | varchar(100) | Ya | NULL |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |
| `dihapus_pada` | datetime | Ya | NULL |  |  |

## rw

Data RW; satu organisasi punya beberapa RW

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id, UK gabungan |  |
| `nomor_rw` | varchar(5) | Tidak |  | UK gabungan |  |
| `nama_ketua` | varchar(100) | Ya | NULL |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |
| `dihapus_pada` | datetime | Ya | NULL |  |  |

## sesi_pengguna

Sesi/perangkat aktif pengguna (F-024)

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_pengguna` | bigint(20) unsigned | Tidak |  | FK → pengguna.id |  |
| `nama_perangkat` | varchar(150) | Ya | NULL |  |  |
| `hash_token` | char(64) | Tidak |  | UK | SHA-256 dari token; token asli tidak disimpan |
| `alamat_ip` | varchar(45) | Ya | NULL |  |  |
| `agen_pengguna` | varchar(255) | Ya | NULL |  |  |
| `terakhir_aktif_pada` | datetime | Ya | NULL |  |  |
| `kedaluwarsa_pada` | datetime | Ya | NULL |  |  |
| `dicabut_pada` | datetime | Ya | NULL |  | Diisi saat logout / keluar dari perangkat lain |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |

## survei

Survei kepuasan warga (F-022)

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id |  |
| `judul` | varchar(200) | Tidak |  |  |  |
| `deskripsi` | varchar(500) | Ya | NULL |  |  |
| `dimulai_pada` | datetime | Tidak |  |  |  |
| `berakhir_pada` | datetime | Tidak |  |  |  |
| `anonim` | tinyint(1) | Tidak | 0 |  |  |
| `dibuat_oleh` | bigint(20) unsigned | Ya | NULL | FK → pengguna.id |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |

## tagihan

Tagihan per keluarga per periode; mendukung bayar sebagian

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id, UK gabungan |  |
| `id_keluarga` | bigint(20) unsigned | Tidak |  | FK → keluarga.id, UK gabungan |  |
| `id_periode_tagihan` | bigint(20) unsigned | Tidak |  | FK → periode_tagihan.id, UK gabungan |  |
| `nomor_tagihan` | varchar(40) | Tidak |  | UK gabungan |  |
| `total_nominal` | decimal(15,2) | Tidak | 0.00 |  |  |
| `nominal_dibayar` | decimal(15,2) | Tidak | 0.00 |  |  |
| `status` | enum('belum_dibayar','sebagian','lunas','lewat_jatuh_tempo','batal') | Tidak | belum_dibayar |  |  |
| `tanggal_jatuh_tempo` | date | Tidak |  |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |

## tanggapan_pengaduan

Histori tindak lanjut pengaduan

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_pengaduan` | bigint(20) unsigned | Tidak |  | FK → pengaduan.id |  |
| `id_pengguna_penanggap` | bigint(20) unsigned | Tidak |  | FK → pengguna.id |  |
| `tanggapan` | text | Tidak |  |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |

## target_pengumuman

Target multi-wilayah pengumuman (opsional)

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_pengumuman` | bigint(20) unsigned | Tidak |  | FK → pengumuman.id |  |
| `id_rw` | bigint(20) unsigned | Ya | NULL | FK → rw.id |  |
| `id_rt` | bigint(20) unsigned | Ya | NULL | FK → rt.id |  |

## transaksi_kas

Kas masuk/keluar; kas masuk dicatat setelah pembayaran terverifikasi

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id |  |
| `jenis_transaksi` | enum('masuk','keluar') | Tidak |  |  |  |
| `id_kategori_pengeluaran` | bigint(20) unsigned | Ya | NULL | FK → kategori_pengeluaran.id | Wajib untuk kas keluar |
| `id_pembayaran` | bigint(20) unsigned | Ya | NULL | FK → pembayaran.id, UK | Terisi untuk kas masuk dari pembayaran terverifikasi |
| `nominal` | decimal(15,2) | Tidak |  |  |  |
| `tanggal_transaksi` | date | Tidak |  |  |  |
| `keterangan` | varchar(255) | Ya | NULL |  |  |
| `id_berkas` | bigint(20) unsigned | Ya | NULL | FK → berkas.id | Lampiran bukti transaksi |
| `dicatat_oleh` | bigint(20) unsigned | Ya | NULL | FK → pengguna.id |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |

## warga

Data individu warga. NIK sensitif: samarkan di API, akses terbatas

| Kolom | Tipe | Null | Default | Kunci | Keterangan |
|---|---|---|---|---|---|
| `id` | bigint(20) unsigned | Tidak | auto | PK |  |
| `id_organisasi` | bigint(20) unsigned | Tidak |  | FK → organisasi.id |  |
| `id_keluarga` | bigint(20) unsigned | Tidak |  | FK → keluarga.id |  |
| `nik` | char(16) | Tidak |  | UK |  |
| `nama_lengkap` | varchar(150) | Tidak |  |  |  |
| `tempat_lahir` | varchar(100) | Ya | NULL |  |  |
| `tanggal_lahir` | date | Ya | NULL |  |  |
| `jenis_kelamin` | enum('L','P') | Tidak |  |  |  |
| `hubungan_keluarga` | enum('kepala_keluarga','suami','istri','anak','orang_tua','famili_lain','lainnya') | Tidak | lainnya |  |  |
| `pekerjaan` | varchar(100) | Ya | NULL |  |  |
| `telepon` | varchar(20) | Ya | NULL |  |  |
| `status_tinggal` | enum('tetap','kontrak','kos','sementara') | Tidak | tetap |  |  |
| `status` | enum('aktif','pindah','meninggal','nonaktif') | Tidak | aktif |  |  |
| `dibuat_pada` | datetime | Tidak | current_timestamp() |  |  |
| `diperbarui_pada` | datetime | Tidak | current_timestamp() |  |  |
| `dihapus_pada` | datetime | Ya | NULL |  |  |
