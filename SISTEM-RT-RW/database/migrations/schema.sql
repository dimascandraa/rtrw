-- =====================================================================
-- SISTEM INFORMASI RT/RW  -  SKEMA DATABASE
-- DBMS      : MySQL 5.7+ / MySQL 8 / MariaDB 10.4+ (XAMPP)
-- Engine    : InnoDB, charset utf8mb4
-- PIC       : Divisi Database (Linda)
--
-- CARA PAKAI
--   1) mysql -u root -p < database/schema.sql
--   2) mysql -u root -p sistem_rt_rw < database/seed.sql
--   Untuk mengulang dari nol: DROP DATABASE sistem_rt_rw;  lalu ulangi.
--
-- KONVENSI
--   * Nama tabel/kolom: snake_case Bahasa Indonesia (sesuai sheet "Database Management").
--   * Primary key  : id BIGINT UNSIGNED AUTO_INCREMENT.
--   * Foreign key  : id_<tabel_induk>. Default ON DELETE RESTRICT.
--   * Audit field  : dibuat_pada, diperbarui_pada, dihapus_pada (soft delete, bila perlu).
--   * Uang         : DECIMAL(15,2).  Waktu: DATETIME (simpan dalam zona waktu server/WIB).
--   * ISOLASI MULTI-RT/RW (RT-037): tabel operasional yang sering di-query langsung
--     memiliki kolom id_organisasi. Tabel anak (rincian_tagihan, bukti_pembayaran,
--     persetujuan_surat, dll.) mengikuti organisasi tabel induknya.
--     Backend WAJIB menambahkan "WHERE id_organisasi = ?" pada setiap query
--     (kecuali Super Admin).
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS sistem_rt_rw
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
USE sistem_rt_rw;

-- =====================================================================
-- A. WILAYAH, AKUN, PERAN, DAN HAK AKSES
-- =====================================================================

-- 1. organisasi -------------------------------------------------------
CREATE TABLE organisasi (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  kode             VARCHAR(20)  NOT NULL,
  nama             VARCHAR(150) NOT NULL,
  alamat           VARCHAR(255) NULL,
  status           ENUM('aktif','nonaktif') NOT NULL DEFAULT 'aktif',
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  dihapus_pada     DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_organisasi_kode (kode)
) ENGINE=InnoDB COMMENT='Induk lingkungan/perumahan (multi-organisasi)';

-- 2. rw ---------------------------------------------------------------
CREATE TABLE rw (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi    BIGINT UNSIGNED NOT NULL,
  nomor_rw         VARCHAR(5)   NOT NULL,
  nama_ketua       VARCHAR(100) NULL,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  dihapus_pada     DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_rw_nomor (id_organisasi, nomor_rw),
  CONSTRAINT fk_rw_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id)
) ENGINE=InnoDB COMMENT='Data RW; satu organisasi punya beberapa RW';

-- 3. rt ---------------------------------------------------------------
CREATE TABLE rt (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi    BIGINT UNSIGNED NOT NULL,
  id_rw            BIGINT UNSIGNED NOT NULL,
  nomor_rt         VARCHAR(5)   NOT NULL,
  nama_ketua       VARCHAR(100) NULL,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  dihapus_pada     DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_rt_nomor (id_rw, nomor_rt),
  KEY idx_rt_organisasi (id_organisasi),
  CONSTRAINT fk_rt_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_rt_rw FOREIGN KEY (id_rw) REFERENCES rw (id)
) ENGINE=InnoDB COMMENT='Data RT; berada di bawah RW';

-- 5. peran ------------------------------------------------------------
CREATE TABLE peran (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  kode             VARCHAR(30)  NOT NULL,
  nama             VARCHAR(50)  NOT NULL,
  deskripsi        VARCHAR(255) NULL,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_peran_kode (kode),
  UNIQUE KEY uq_peran_nama (nama)
) ENGINE=InnoDB COMMENT='Master peran pengguna';

-- 6. izin_akses -------------------------------------------------------
CREATE TABLE izin_akses (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  kode             VARCHAR(60)  NOT NULL COMMENT 'Format modul.aksi, mis. tagihan.read',
  nama             VARCHAR(100) NOT NULL,
  modul            VARCHAR(40)  NOT NULL,
  aksi             ENUM('create','read','update','delete','approve') NOT NULL,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_izin_kode (kode),
  UNIQUE KEY uq_izin_modul_aksi (modul, aksi)
) ENGINE=InnoDB COMMENT='Master hak akses rinci per modul/aksi';

-- 7. peran_izin_akses -------------------------------------------------
CREATE TABLE peran_izin_akses (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_peran         BIGINT UNSIGNED NOT NULL,
  id_izin_akses    BIGINT UNSIGNED NOT NULL,
  terbatas         TINYINT(1) NOT NULL DEFAULT 0
                   COMMENT '1 = izin bertanda * di matriks (hanya data milik sendiri / wilayahnya sendiri)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_peran_izin (id_peran, id_izin_akses),
  CONSTRAINT fk_pia_peran FOREIGN KEY (id_peran) REFERENCES peran (id) ON DELETE CASCADE,
  CONSTRAINT fk_pia_izin FOREIGN KEY (id_izin_akses) REFERENCES izin_akses (id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Relasi peran dan izin akses';

-- 4. pengguna ---------------------------------------------------------
-- id_warga menghubungkan akun ke data warga (F-002). FK ditambahkan di bawah
-- karena tabel warga dibuat setelah tabel ini.
CREATE TABLE pengguna (
  id                          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_warga                    BIGINT UNSIGNED NULL COMMENT 'Akun terhubung ke warga; NULL untuk Super Admin',
  nama                        VARCHAR(150) NOT NULL,
  email                       VARCHAR(150) NULL,
  telepon                     VARCHAR(20)  NULL,
  hash_kata_sandi             VARCHAR(255) NOT NULL COMMENT 'Hasil password_hash() PHP, JANGAN simpan teks biasa',
  status                      ENUM('menunggu_verifikasi','aktif','ditangguhkan','nonaktif')
                              NOT NULL DEFAULT 'menunggu_verifikasi',
  email_terverifikasi_pada    DATETIME NULL,
  telepon_terverifikasi_pada  DATETIME NULL,
  percobaan_login_gagal       SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  terkunci_sampai             DATETIME NULL COMMENT 'Pembatasan percobaan login (F-024)',
  login_terakhir_pada         DATETIME NULL,
  dibuat_pada                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  dihapus_pada                DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_pengguna_email (email),
  UNIQUE KEY uq_pengguna_telepon (telepon),
  UNIQUE KEY uq_pengguna_warga (id_warga),
  KEY idx_pengguna_status (status)
) ENGINE=InnoDB COMMENT='Akun seluruh pengguna';

-- 8. penugasan_pengguna -----------------------------------------------
CREATE TABLE penugasan_pengguna (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_pengguna      BIGINT UNSIGNED NOT NULL,
  id_peran         BIGINT UNSIGNED NOT NULL,
  id_organisasi    BIGINT UNSIGNED NULL COMMENT 'NULL hanya untuk Super Admin (lintas organisasi)',
  id_rw            BIGINT UNSIGNED NULL,
  id_rt            BIGINT UNSIGNED NULL,
  aktif            TINYINT(1) NOT NULL DEFAULT 1,
  mulai_pada       DATE NULL,
  selesai_pada     DATE NULL,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_penugasan_pengguna (id_pengguna, aktif),
  KEY idx_penugasan_wilayah (id_organisasi, id_rw, id_rt),
  CONSTRAINT fk_penugasan_pengguna FOREIGN KEY (id_pengguna) REFERENCES pengguna (id),
  CONSTRAINT fk_penugasan_peran FOREIGN KEY (id_peran) REFERENCES peran (id),
  CONSTRAINT fk_penugasan_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_penugasan_rw FOREIGN KEY (id_rw) REFERENCES rw (id),
  CONSTRAINT fk_penugasan_rt FOREIGN KEY (id_rt) REFERENCES rt (id)
) ENGINE=InnoDB COMMENT='Penempatan akun ke peran dan wilayah; satu pengguna bisa punya banyak penugasan';

-- 41 (baru). kode_verifikasi -------------------------------------------
CREATE TABLE kode_verifikasi (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_pengguna      BIGINT UNSIGNED NOT NULL,
  tujuan           ENUM('registrasi','reset_sandi','login') NOT NULL,
  kanal            ENUM('email','telepon') NOT NULL,
  hash_kode        VARCHAR(255) NOT NULL COMMENT 'Hash dari kode OTP, bukan OTP asli',
  jumlah_percobaan TINYINT UNSIGNED NOT NULL DEFAULT 0,
  kedaluwarsa_pada DATETIME NOT NULL,
  digunakan_pada   DATETIME NULL,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_kodever_pengguna (id_pengguna, tujuan),
  CONSTRAINT fk_kodever_pengguna FOREIGN KEY (id_pengguna) REFERENCES pengguna (id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Kode OTP registrasi/reset sandi (F-001); tabel tambahan di luar 40 tabel awal';

-- 38. sesi_pengguna ----------------------------------------------------
CREATE TABLE sesi_pengguna (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_pengguna         BIGINT UNSIGNED NOT NULL,
  nama_perangkat      VARCHAR(150) NULL,
  hash_token          CHAR(64) NOT NULL COMMENT 'SHA-256 dari token; token asli tidak disimpan',
  alamat_ip           VARCHAR(45)  NULL,
  agen_pengguna       VARCHAR(255) NULL,
  terakhir_aktif_pada DATETIME NULL,
  kedaluwarsa_pada    DATETIME NULL,
  dicabut_pada        DATETIME NULL COMMENT 'Diisi saat logout / keluar dari perangkat lain',
  dibuat_pada         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_sesi_token (hash_token),
  KEY idx_sesi_pengguna (id_pengguna, dicabut_pada),
  CONSTRAINT fk_sesi_pengguna FOREIGN KEY (id_pengguna) REFERENCES pengguna (id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Sesi/perangkat aktif pengguna (F-024)';

-- 36. berkas -----------------------------------------------------------
CREATE TABLE berkas (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi    BIGINT UNSIGNED NOT NULL,
  path_penyimpanan VARCHAR(500) NOT NULL COMMENT 'Lokasi di penyimpanan PRIVAT (di luar folder publik web)',
  nama_asli        VARCHAR(255) NOT NULL,
  tipe_mime        VARCHAR(100) NOT NULL,
  ukuran           BIGINT UNSIGNED NOT NULL COMMENT 'Byte',
  hash_berkas      CHAR(64) NULL COMMENT 'SHA-256 isi berkas (opsional, untuk deteksi duplikat)',
  diunggah_oleh    BIGINT UNSIGNED NULL,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  dihapus_pada     DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_berkas_organisasi (id_organisasi),
  CONSTRAINT fk_berkas_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_berkas_pengunggah FOREIGN KEY (diunggah_oleh) REFERENCES pengguna (id)
) ENGINE=InnoDB COMMENT='Metadata file/lampiran; isi file TIDAK disimpan di database';

-- =====================================================================
-- B. DATA WARGA
-- =====================================================================

-- 9. keluarga ----------------------------------------------------------
CREATE TABLE keluarga (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi       BIGINT UNSIGNED NOT NULL,
  id_rt               BIGINT UNSIGNED NOT NULL,
  nomor_kk            CHAR(16)     NOT NULL,
  alamat              VARCHAR(255) NOT NULL,
  nomor_rumah         VARCHAR(20)  NULL,
  jenis_hunian        ENUM('milik_sendiri','kontrak','kos','kosong') NOT NULL DEFAULT 'milik_sendiri'
                      COMMENT 'Dipakai aturan_iuran (tarif per jenis hunian)',
  id_kepala_keluarga  BIGINT UNSIGNED NULL,
  dibuat_pada         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  dihapus_pada        DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_keluarga_kk (nomor_kk),
  KEY idx_keluarga_organisasi (id_organisasi),
  KEY idx_keluarga_rt (id_rt),
  CONSTRAINT fk_keluarga_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_keluarga_rt FOREIGN KEY (id_rt) REFERENCES rt (id)
) ENGINE=InnoDB COMMENT='Data keluarga/rumah tangga. No. KK sensitif, batasi akses';

-- 10. warga ------------------------------------------------------------
CREATE TABLE warga (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi       BIGINT UNSIGNED NOT NULL,
  id_keluarga         BIGINT UNSIGNED NOT NULL,
  nik                 CHAR(16)     NOT NULL,
  nama_lengkap        VARCHAR(150) NOT NULL,
  tempat_lahir        VARCHAR(100) NULL,
  tanggal_lahir       DATE NULL,
  jenis_kelamin       ENUM('L','P') NOT NULL,
  hubungan_keluarga   ENUM('kepala_keluarga','suami','istri','anak','orang_tua','famili_lain','lainnya')
                      NOT NULL DEFAULT 'lainnya',
  pekerjaan           VARCHAR(100) NULL,
  telepon             VARCHAR(20)  NULL,
  status_tinggal      ENUM('tetap','kontrak','kos','sementara') NOT NULL DEFAULT 'tetap',
  status              ENUM('aktif','pindah','meninggal','nonaktif') NOT NULL DEFAULT 'aktif',
  dibuat_pada         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  dihapus_pada        DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_warga_nik (nik),
  KEY idx_warga_organisasi (id_organisasi),
  KEY idx_warga_keluarga (id_keluarga),
  KEY idx_warga_nama (nama_lengkap),
  KEY idx_warga_status (status),
  CONSTRAINT fk_warga_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_warga_keluarga FOREIGN KEY (id_keluarga) REFERENCES keluarga (id)
) ENGINE=InnoDB COMMENT='Data individu warga. NIK sensitif: samarkan di API, akses terbatas';

-- Relasi melingkar keluarga <-> warga dan pengguna -> warga
ALTER TABLE keluarga
  ADD CONSTRAINT fk_keluarga_kepala FOREIGN KEY (id_kepala_keluarga) REFERENCES warga (id);
ALTER TABLE pengguna
  ADD CONSTRAINT fk_pengguna_warga FOREIGN KEY (id_warga) REFERENCES warga (id);

-- 11. mutasi_warga -----------------------------------------------------
CREATE TABLE mutasi_warga (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi    BIGINT UNSIGNED NOT NULL,
  id_warga         BIGINT UNSIGNED NOT NULL,
  jenis_mutasi     ENUM('pindah_masuk','pindah_keluar','lahir','meninggal','perubahan_status') NOT NULL,
  tanggal_berlaku  DATE NOT NULL,
  asal_tujuan      VARCHAR(255) NULL COMMENT 'Asal (pindah masuk) atau tujuan (pindah keluar)',
  catatan          VARCHAR(500) NULL,
  dicatat_oleh     BIGINT UNSIGNED NULL,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_mutasi_warga (id_warga, tanggal_berlaku),
  KEY idx_mutasi_organisasi (id_organisasi),
  CONSTRAINT fk_mutasi_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_mutasi_warga FOREIGN KEY (id_warga) REFERENCES warga (id),
  CONSTRAINT fk_mutasi_pencatat FOREIGN KEY (dicatat_oleh) REFERENCES pengguna (id)
) ENGINE=InnoDB COMMENT='Riwayat mutasi warga (F-005)';

-- =====================================================================
-- C. KEUANGAN
-- =====================================================================

-- 12. kategori_iuran ---------------------------------------------------
CREATE TABLE kategori_iuran (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi    BIGINT UNSIGNED NOT NULL,
  nama             VARCHAR(100) NOT NULL,
  jenis_iuran      ENUM('wajib','sukarela') NOT NULL DEFAULT 'wajib',
  deskripsi        VARCHAR(255) NULL,
  aktif            TINYINT(1) NOT NULL DEFAULT 1,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_kategori_iuran (id_organisasi, nama),
  CONSTRAINT fk_kategori_iuran_org FOREIGN KEY (id_organisasi) REFERENCES organisasi (id)
) ENGINE=InnoDB COMMENT='Master kategori iuran (kebersihan, keamanan, sosial, ...)';

-- 13. aturan_iuran -----------------------------------------------------
CREATE TABLE aturan_iuran (
  id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi      BIGINT UNSIGNED NOT NULL,
  id_kategori_iuran  BIGINT UNSIGNED NOT NULL,
  id_rt              BIGINT UNSIGNED NULL COMMENT 'NULL = berlaku untuk semua RT',
  jenis_hunian       ENUM('milik_sendiri','kontrak','kos','kosong') NULL COMMENT 'NULL = semua jenis hunian',
  nominal            DECIMAL(15,2) NOT NULL,
  siklus_tagihan     ENUM('bulanan','tahunan','sekali') NOT NULL DEFAULT 'bulanan',
  hari_jatuh_tempo   TINYINT UNSIGNED NOT NULL DEFAULT 10 COMMENT 'Tanggal jatuh tempo tiap periode (1-28)',
  boleh_cicilan      TINYINT(1) NOT NULL DEFAULT 1,
  berlaku_mulai      DATE NOT NULL,
  berlaku_sampai     DATE NULL,
  aktif              TINYINT(1) NOT NULL DEFAULT 1,
  dibuat_pada        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_aturan_organisasi (id_organisasi),
  KEY idx_aturan_kategori (id_kategori_iuran),
  CONSTRAINT fk_aturan_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_aturan_kategori FOREIGN KEY (id_kategori_iuran) REFERENCES kategori_iuran (id),
  CONSTRAINT fk_aturan_rt FOREIGN KEY (id_rt) REFERENCES rt (id),
  CONSTRAINT ck_aturan_nominal CHECK (nominal >= 0),
  CONSTRAINT ck_aturan_hari CHECK (hari_jatuh_tempo BETWEEN 1 AND 28)
) ENGINE=InnoDB COMMENT='Aturan nominal/periode iuran; tarif bisa beda per RT dan jenis hunian';

-- 14. periode_tagihan --------------------------------------------------
CREATE TABLE periode_tagihan (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi    BIGINT UNSIGNED NOT NULL,
  nama             VARCHAR(50) NOT NULL COMMENT 'Contoh: Oktober 2026',
  periode_mulai    DATE NOT NULL,
  periode_akhir    DATE NOT NULL,
  status           ENUM('draft','terbit','ditutup') NOT NULL DEFAULT 'draft',
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_periode (id_organisasi, periode_mulai, periode_akhir),
  CONSTRAINT fk_periode_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT ck_periode_tanggal CHECK (periode_akhir >= periode_mulai)
) ENGINE=InnoDB COMMENT='Periode penagihan; UNIQUE mencegah periode ganda';

-- 15. tagihan ----------------------------------------------------------
CREATE TABLE tagihan (
  id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi        BIGINT UNSIGNED NOT NULL,
  id_keluarga          BIGINT UNSIGNED NOT NULL,
  id_periode_tagihan   BIGINT UNSIGNED NOT NULL,
  nomor_tagihan        VARCHAR(40)   NOT NULL,
  total_nominal        DECIMAL(15,2) NOT NULL DEFAULT 0,
  nominal_dibayar      DECIMAL(15,2) NOT NULL DEFAULT 0,
  status               ENUM('belum_dibayar','sebagian','lunas','lewat_jatuh_tempo','batal')
                       NOT NULL DEFAULT 'belum_dibayar',
  tanggal_jatuh_tempo  DATE NOT NULL,
  dibuat_pada          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_tagihan_nomor (id_organisasi, nomor_tagihan),
  UNIQUE KEY uq_tagihan_keluarga_periode (id_keluarga, id_periode_tagihan),
  KEY idx_tagihan_status (id_organisasi, status),
  KEY idx_tagihan_jatuh_tempo (tanggal_jatuh_tempo),
  CONSTRAINT fk_tagihan_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_tagihan_keluarga FOREIGN KEY (id_keluarga) REFERENCES keluarga (id),
  CONSTRAINT fk_tagihan_periode FOREIGN KEY (id_periode_tagihan) REFERENCES periode_tagihan (id),
  CONSTRAINT ck_tagihan_nominal CHECK (total_nominal >= 0 AND nominal_dibayar >= 0 AND nominal_dibayar <= total_nominal)
) ENGINE=InnoDB COMMENT='Tagihan per keluarga per periode; mendukung bayar sebagian';

-- 16. rincian_tagihan --------------------------------------------------
CREATE TABLE rincian_tagihan (
  id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_tagihan         BIGINT UNSIGNED NOT NULL,
  id_kategori_iuran  BIGINT UNSIGNED NOT NULL,
  deskripsi          VARCHAR(255) NULL,
  nominal            DECIMAL(15,2) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_rincian (id_tagihan, id_kategori_iuran),
  CONSTRAINT fk_rincian_tagihan FOREIGN KEY (id_tagihan) REFERENCES tagihan (id) ON DELETE CASCADE,
  CONSTRAINT fk_rincian_kategori FOREIGN KEY (id_kategori_iuran) REFERENCES kategori_iuran (id),
  CONSTRAINT ck_rincian_nominal CHECK (nominal >= 0)
) ENGINE=InnoDB COMMENT='Komponen tagihan; satu tagihan bisa berisi beberapa kategori iuran';

-- 17. pembayaran -------------------------------------------------------
CREATE TABLE pembayaran (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi         BIGINT UNSIGNED NOT NULL,
  id_tagihan            BIGINT UNSIGNED NOT NULL,
  id_pengguna_pembayar  BIGINT UNSIGNED NULL COMMENT 'NULL bila tunai dicatat bendahara untuk warga tanpa akun',
  nominal               DECIMAL(15,2) NOT NULL,
  metode                ENUM('qris','transfer','tunai') NOT NULL,
  status                ENUM('menunggu_verifikasi','terverifikasi','ditolak','batal')
                        NOT NULL DEFAULT 'menunggu_verifikasi',
  referensi_pembayaran  VARCHAR(100) NULL COMMENT 'Kode referensi QRIS/transfer',
  dibayar_pada          DATETIME NOT NULL,
  diverifikasi_oleh     BIGINT UNSIGNED NULL,
  diverifikasi_pada     DATETIME NULL,
  catatan_verifikasi    VARCHAR(255) NULL,
  dibuat_pada           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_pembayaran_tagihan (id_tagihan),
  KEY idx_pembayaran_status (id_organisasi, status),
  CONSTRAINT fk_pembayaran_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_pembayaran_tagihan FOREIGN KEY (id_tagihan) REFERENCES tagihan (id),
  CONSTRAINT fk_pembayaran_pembayar FOREIGN KEY (id_pengguna_pembayar) REFERENCES pengguna (id),
  CONSTRAINT fk_pembayaran_verifikator FOREIGN KEY (diverifikasi_oleh) REFERENCES pengguna (id),
  CONSTRAINT ck_pembayaran_nominal CHECK (nominal > 0)
) ENGINE=InnoDB COMMENT='Pembayaran tagihan; satu tagihan bisa punya banyak pembayaran/cicilan';

-- 18. bukti_pembayaran -------------------------------------------------
CREATE TABLE bukti_pembayaran (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_pembayaran         BIGINT UNSIGNED NOT NULL,
  id_berkas             BIGINT UNSIGNED NOT NULL,
  diunggah_oleh         BIGINT UNSIGNED NULL,
  diunggah_pada         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status_verifikasi     ENUM('menunggu','diterima','ditolak') NOT NULL DEFAULT 'menunggu',
  catatan               VARCHAR(255) NULL,
  PRIMARY KEY (id),
  KEY idx_bukti_pembayaran (id_pembayaran),
  CONSTRAINT fk_bukti_pembayaran FOREIGN KEY (id_pembayaran) REFERENCES pembayaran (id),
  CONSTRAINT fk_bukti_berkas FOREIGN KEY (id_berkas) REFERENCES berkas (id),
  CONSTRAINT fk_bukti_pengunggah FOREIGN KEY (diunggah_oleh) REFERENCES pengguna (id)
) ENGINE=InnoDB COMMENT='Bukti transfer; akses hanya pihak berwenang';

-- 19. kuitansi ---------------------------------------------------------
CREATE TABLE kuitansi (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi    BIGINT UNSIGNED NOT NULL,
  id_pembayaran    BIGINT UNSIGNED NOT NULL,
  nomor_kuitansi   VARCHAR(40) NOT NULL,
  id_berkas        BIGINT UNSIGNED NULL COMMENT 'File PDF kuitansi',
  diterbitkan_pada DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_kuitansi_nomor (id_organisasi, nomor_kuitansi),
  UNIQUE KEY uq_kuitansi_pembayaran (id_pembayaran),
  CONSTRAINT fk_kuitansi_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_kuitansi_pembayaran FOREIGN KEY (id_pembayaran) REFERENCES pembayaran (id),
  CONSTRAINT fk_kuitansi_berkas FOREIGN KEY (id_berkas) REFERENCES berkas (id)
) ENGINE=InnoDB COMMENT='Kuitansi digital; terbit setelah pembayaran terverifikasi';

-- 21. kategori_pengeluaran ---------------------------------------------
CREATE TABLE kategori_pengeluaran (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi    BIGINT UNSIGNED NOT NULL,
  nama             VARCHAR(100) NOT NULL,
  aktif            TINYINT(1) NOT NULL DEFAULT 1,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_kategori_pengeluaran (id_organisasi, nama),
  CONSTRAINT fk_kategori_pengeluaran_org FOREIGN KEY (id_organisasi) REFERENCES organisasi (id)
) ENGINE=InnoDB COMMENT='Kategori pengeluaran kas';

-- 20. transaksi_kas ----------------------------------------------------
CREATE TABLE transaksi_kas (
  id                        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi             BIGINT UNSIGNED NOT NULL,
  jenis_transaksi           ENUM('masuk','keluar') NOT NULL,
  id_kategori_pengeluaran   BIGINT UNSIGNED NULL COMMENT 'Wajib untuk kas keluar',
  id_pembayaran             BIGINT UNSIGNED NULL COMMENT 'Terisi untuk kas masuk dari pembayaran terverifikasi',
  nominal                   DECIMAL(15,2) NOT NULL,
  tanggal_transaksi         DATE NOT NULL,
  keterangan                VARCHAR(255) NULL,
  id_berkas                 BIGINT UNSIGNED NULL COMMENT 'Lampiran bukti transaksi',
  dicatat_oleh              BIGINT UNSIGNED NULL,
  dibuat_pada               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_kas_pembayaran (id_pembayaran),
  KEY idx_kas_tanggal (id_organisasi, tanggal_transaksi),
  CONSTRAINT fk_kas_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_kas_kategori FOREIGN KEY (id_kategori_pengeluaran) REFERENCES kategori_pengeluaran (id),
  CONSTRAINT fk_kas_pembayaran FOREIGN KEY (id_pembayaran) REFERENCES pembayaran (id),
  CONSTRAINT fk_kas_berkas FOREIGN KEY (id_berkas) REFERENCES berkas (id),
  CONSTRAINT fk_kas_pencatat FOREIGN KEY (dicatat_oleh) REFERENCES pengguna (id),
  CONSTRAINT ck_kas_nominal CHECK (nominal > 0),
  CONSTRAINT ck_kas_jenis CHECK (
    (jenis_transaksi = 'masuk'  AND id_kategori_pengeluaran IS NULL) OR
    (jenis_transaksi = 'keluar' AND id_kategori_pengeluaran IS NOT NULL AND id_pembayaran IS NULL)
  )
) ENGINE=InnoDB COMMENT='Kas masuk/keluar; kas masuk dicatat setelah pembayaran terverifikasi';

-- =====================================================================
-- D. INFORMASI DAN KOMUNIKASI
-- =====================================================================

-- 22. pengumuman -------------------------------------------------------
CREATE TABLE pengumuman (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi         BIGINT UNSIGNED NOT NULL,
  id_rt                 BIGINT UNSIGNED NULL COMMENT 'NULL = tingkat RW / seluruh organisasi',
  judul                 VARCHAR(200) NOT NULL,
  isi                   TEXT NOT NULL,
  dibuat_oleh           BIGINT UNSIGNED NULL,
  dipublikasikan_pada   DATETIME NULL,
  kedaluwarsa_pada      DATETIME NULL,
  dibuat_pada           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  dihapus_pada          DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_pengumuman_publikasi (id_organisasi, dipublikasikan_pada),
  CONSTRAINT fk_pengumuman_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_pengumuman_rt FOREIGN KEY (id_rt) REFERENCES rt (id),
  CONSTRAINT fk_pengumuman_pembuat FOREIGN KEY (dibuat_oleh) REFERENCES pengguna (id)
) ENGINE=InnoDB COMMENT='Pengumuman RT/RW';

-- 23. target_pengumuman ------------------------------------------------
CREATE TABLE target_pengumuman (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_pengumuman    BIGINT UNSIGNED NOT NULL,
  id_rw            BIGINT UNSIGNED NULL,
  id_rt            BIGINT UNSIGNED NULL,
  PRIMARY KEY (id),
  KEY idx_target_pengumuman (id_pengumuman),
  CONSTRAINT fk_target_pengumuman FOREIGN KEY (id_pengumuman) REFERENCES pengumuman (id) ON DELETE CASCADE,
  CONSTRAINT fk_target_rw FOREIGN KEY (id_rw) REFERENCES rw (id),
  CONSTRAINT fk_target_rt FOREIGN KEY (id_rt) REFERENCES rt (id),
  CONSTRAINT ck_target_wilayah CHECK (id_rw IS NOT NULL OR id_rt IS NOT NULL)
) ENGINE=InnoDB COMMENT='Target multi-wilayah pengumuman (opsional)';

-- 24. notifikasi -------------------------------------------------------
CREATE TABLE notifikasi (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi    BIGINT UNSIGNED NULL,
  id_pengguna      BIGINT UNSIGNED NOT NULL,
  judul            VARCHAR(200) NOT NULL,
  isi              VARCHAR(500) NOT NULL,
  jenis            ENUM('tagihan','pembayaran','surat','pengumuman','kegiatan','pengaduan','sistem') NOT NULL,
  tabel_referensi  VARCHAR(50)  NULL COMMENT 'Mis. tagihan, pengajuan_surat (untuk membuka detail)',
  id_referensi     BIGINT UNSIGNED NULL,
  dibaca_pada      DATETIME NULL,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_notifikasi_pengguna (id_pengguna, dibaca_pada),
  CONSTRAINT fk_notifikasi_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_notifikasi_pengguna FOREIGN KEY (id_pengguna) REFERENCES pengguna (id)
) ENGINE=InnoDB COMMENT='Notifikasi aplikasi (F-018) dan pengingat iuran (F-014)';

-- 25. log_notifikasi ---------------------------------------------------
CREATE TABLE log_notifikasi (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_notifikasi    BIGINT UNSIGNED NOT NULL,
  kanal            ENUM('aplikasi','email','push','whatsapp') NOT NULL,
  tujuan           VARCHAR(150) NULL,
  status           ENUM('antre','terkirim','gagal') NOT NULL DEFAULT 'antre',
  pesan_galat      VARCHAR(255) NULL,
  dikirim_pada     DATETIME NULL,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_log_notifikasi (id_notifikasi),
  CONSTRAINT fk_log_notifikasi FOREIGN KEY (id_notifikasi) REFERENCES notifikasi (id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Log pengiriman notifikasi per kanal';

-- =====================================================================
-- E. ADMINISTRASI SURAT
-- =====================================================================

-- 26. jenis_surat ------------------------------------------------------
CREATE TABLE jenis_surat (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi    BIGINT UNSIGNED NOT NULL,
  kode             VARCHAR(20)  NOT NULL COMMENT 'Dipakai dalam format nomor surat',
  nama             VARCHAR(100) NOT NULL,
  templat          MEDIUMTEXT NULL,
  isian_wajib      JSON NULL COMMENT 'Daftar field tambahan yang harus diisi warga',
  aktif            TINYINT(1) NOT NULL DEFAULT 1,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_jenis_surat (id_organisasi, kode),
  CONSTRAINT fk_jenis_surat_org FOREIGN KEY (id_organisasi) REFERENCES organisasi (id)
) ENGINE=InnoDB COMMENT='Master jenis surat dan templatnya';

-- 27. pengajuan_surat --------------------------------------------------
CREATE TABLE pengajuan_surat (
  id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi      BIGINT UNSIGNED NOT NULL,
  id_warga           BIGINT UNSIGNED NOT NULL,
  id_jenis_surat     BIGINT UNSIGNED NOT NULL,
  id_pengguna_pengaju BIGINT UNSIGNED NULL,
  keperluan          VARCHAR(255) NOT NULL,
  data_isian         JSON NULL,
  status             ENUM('diajukan','diproses','disetujui','ditolak','diterbitkan','dibatalkan')
                     NOT NULL DEFAULT 'diajukan',
  diajukan_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diproses_oleh      BIGINT UNSIGNED NULL,
  nomor_surat        VARCHAR(60) NULL COMMENT 'Terisi saat surat diterbitkan',
  diterbitkan_pada   DATETIME NULL,
  id_berkas          BIGINT UNSIGNED NULL COMMENT 'File PDF surat hasil cetak',
  diperbarui_pada    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_pengajuan_nomor (id_organisasi, nomor_surat),
  KEY idx_pengajuan_status (id_organisasi, status),
  KEY idx_pengajuan_warga (id_warga),
  CONSTRAINT fk_pengajuan_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_pengajuan_warga FOREIGN KEY (id_warga) REFERENCES warga (id),
  CONSTRAINT fk_pengajuan_jenis FOREIGN KEY (id_jenis_surat) REFERENCES jenis_surat (id),
  CONSTRAINT fk_pengajuan_pengaju FOREIGN KEY (id_pengguna_pengaju) REFERENCES pengguna (id),
  CONSTRAINT fk_pengajuan_pemroses FOREIGN KEY (diproses_oleh) REFERENCES pengguna (id),
  CONSTRAINT fk_pengajuan_berkas FOREIGN KEY (id_berkas) REFERENCES berkas (id)
) ENGINE=InnoDB COMMENT='Pengajuan surat online (F-006)';

-- 28. persetujuan_surat ------------------------------------------------
CREATE TABLE persetujuan_surat (
  id                      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_pengajuan_surat      BIGINT UNSIGNED NOT NULL,
  id_pengguna_penyetuju   BIGINT UNSIGNED NOT NULL,
  tahap                   TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'Mendukung persetujuan bertahap (RT lalu RW)',
  keputusan               ENUM('setuju','tolak','revisi') NOT NULL,
  catatan                 VARCHAR(500) NULL,
  diputuskan_pada         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_persetujuan_pengajuan (id_pengajuan_surat, tahap),
  CONSTRAINT fk_persetujuan_pengajuan FOREIGN KEY (id_pengajuan_surat) REFERENCES pengajuan_surat (id) ON DELETE CASCADE,
  CONSTRAINT fk_persetujuan_penyetuju FOREIGN KEY (id_pengguna_penyetuju) REFERENCES pengguna (id)
) ENGINE=InnoDB COMMENT='Riwayat persetujuan surat (F-007)';

-- =====================================================================
-- F. KEGIATAN, PENGADUAN, DAN LAYANAN WARGA
-- =====================================================================

-- 29. kegiatan ---------------------------------------------------------
CREATE TABLE kegiatan (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi    BIGINT UNSIGNED NOT NULL,
  id_rt            BIGINT UNSIGNED NULL COMMENT 'NULL = kegiatan tingkat RW',
  judul            VARCHAR(200) NOT NULL,
  deskripsi        TEXT NULL,
  jenis            ENUM('kerja_bakti','rapat','posyandu','sosial','lainnya') NOT NULL DEFAULT 'lainnya',
  mulai_pada       DATETIME NOT NULL,
  selesai_pada     DATETIME NULL,
  lokasi           VARCHAR(200) NULL,
  dibuat_oleh      BIGINT UNSIGNED NULL,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  dihapus_pada     DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_kegiatan_mulai (id_organisasi, mulai_pada),
  CONSTRAINT fk_kegiatan_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_kegiatan_rt FOREIGN KEY (id_rt) REFERENCES rt (id),
  CONSTRAINT fk_kegiatan_pembuat FOREIGN KEY (dibuat_oleh) REFERENCES pengguna (id)
) ENGINE=InnoDB COMMENT='Agenda kegiatan warga (F-019)';

-- 30. kehadiran_kegiatan -----------------------------------------------
CREATE TABLE kehadiran_kegiatan (
  id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_kegiatan        BIGINT UNSIGNED NOT NULL,
  id_warga           BIGINT UNSIGNED NOT NULL,
  status_kehadiran   ENUM('akan_hadir','hadir','tidak_hadir') NOT NULL DEFAULT 'akan_hadir',
  dibuat_pada        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_kehadiran (id_kegiatan, id_warga),
  CONSTRAINT fk_kehadiran_kegiatan FOREIGN KEY (id_kegiatan) REFERENCES kegiatan (id) ON DELETE CASCADE,
  CONSTRAINT fk_kehadiran_warga FOREIGN KEY (id_warga) REFERENCES warga (id)
) ENGINE=InnoDB COMMENT='RSVP/absensi kegiatan (opsional)';

-- 31. pengaduan --------------------------------------------------------
CREATE TABLE pengaduan (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi    BIGINT UNSIGNED NOT NULL,
  id_warga         BIGINT UNSIGNED NOT NULL,
  kategori         ENUM('kehilangan','keamanan','fasilitas_rusak','kebersihan','lainnya') NOT NULL,
  judul            VARCHAR(200) NOT NULL,
  deskripsi        TEXT NOT NULL,
  status           ENUM('baru','diproses','selesai','ditolak') NOT NULL DEFAULT 'baru',
  bersifat_privat  TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1 = hanya pengurus yang dapat melihat',
  ditugaskan_ke    BIGINT UNSIGNED NULL,
  id_berkas        BIGINT UNSIGNED NULL COMMENT 'Satu lampiran (foto) opsional',
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_pengaduan_status (id_organisasi, status),
  KEY idx_pengaduan_warga (id_warga),
  CONSTRAINT fk_pengaduan_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_pengaduan_warga FOREIGN KEY (id_warga) REFERENCES warga (id),
  CONSTRAINT fk_pengaduan_petugas FOREIGN KEY (ditugaskan_ke) REFERENCES pengguna (id),
  CONSTRAINT fk_pengaduan_berkas FOREIGN KEY (id_berkas) REFERENCES berkas (id)
) ENGINE=InnoDB COMMENT='Laporan keluhan/kejadian (F-020)';

-- 32. tanggapan_pengaduan ----------------------------------------------
CREATE TABLE tanggapan_pengaduan (
  id                      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_pengaduan            BIGINT UNSIGNED NOT NULL,
  id_pengguna_penanggap   BIGINT UNSIGNED NOT NULL,
  tanggapan               TEXT NOT NULL,
  dibuat_pada             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_tanggapan_pengaduan (id_pengaduan),
  CONSTRAINT fk_tanggapan_pengaduan FOREIGN KEY (id_pengaduan) REFERENCES pengaduan (id) ON DELETE CASCADE,
  CONSTRAINT fk_tanggapan_penanggap FOREIGN KEY (id_pengguna_penanggap) REFERENCES pengguna (id)
) ENGINE=InnoDB COMMENT='Histori tindak lanjut pengaduan';

-- 33. kontak_darurat ---------------------------------------------------
CREATE TABLE kontak_darurat (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi    BIGINT UNSIGNED NOT NULL,
  kategori         ENUM('pengurus','keamanan','ambulans','puskesmas','pemadam_kebakaran','polisi','lainnya') NOT NULL,
  nama             VARCHAR(150) NOT NULL,
  telepon          VARCHAR(30)  NOT NULL,
  alamat           VARCHAR(255) NULL,
  urutan           SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  aktif            TINYINT(1) NOT NULL DEFAULT 1,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  diperbarui_pada  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_kontak_organisasi (id_organisasi, kategori),
  CONSTRAINT fk_kontak_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id)
) ENGINE=InnoDB COMMENT='Direktori kontak darurat dan layanan (F-021)';

-- 34. survei -----------------------------------------------------------
CREATE TABLE survei (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi    BIGINT UNSIGNED NOT NULL,
  judul            VARCHAR(200) NOT NULL,
  deskripsi        VARCHAR(500) NULL,
  dimulai_pada     DATETIME NOT NULL,
  berakhir_pada    DATETIME NOT NULL,
  anonim           TINYINT(1) NOT NULL DEFAULT 0,
  dibuat_oleh      BIGINT UNSIGNED NULL,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_survei_organisasi (id_organisasi),
  CONSTRAINT fk_survei_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_survei_pembuat FOREIGN KEY (dibuat_oleh) REFERENCES pengguna (id),
  CONSTRAINT ck_survei_periode CHECK (berakhir_pada >= dimulai_pada)
) ENGINE=InnoDB COMMENT='Survei kepuasan warga (F-022)';

-- 35. jawaban_survei ---------------------------------------------------
CREATE TABLE jawaban_survei (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_survei        BIGINT UNSIGNED NOT NULL,
  id_warga         BIGINT UNSIGNED NOT NULL,
  penilaian        TINYINT UNSIGNED NOT NULL COMMENT 'Skala 1-5',
  komentar         VARCHAR(1000) NULL,
  dijawab_pada     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_jawaban_survei (id_survei, id_warga),
  CONSTRAINT fk_jawaban_survei FOREIGN KEY (id_survei) REFERENCES survei (id) ON DELETE CASCADE,
  CONSTRAINT fk_jawaban_warga FOREIGN KEY (id_warga) REFERENCES warga (id),
  CONSTRAINT ck_jawaban_penilaian CHECK (penilaian BETWEEN 1 AND 5)
) ENGINE=InnoDB COMMENT='Jawaban survei; satu jawaban per warga. Bila survei anonim, API tidak boleh menampilkan id_warga';

-- =====================================================================
-- G. KEAMANAN DAN SISTEM
-- =====================================================================

-- 37. log_audit --------------------------------------------------------
CREATE TABLE log_audit (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi    BIGINT UNSIGNED NULL,
  id_pengguna      BIGINT UNSIGNED NULL,
  aksi             VARCHAR(50)  NOT NULL COMMENT 'Mis. login, buat, ubah, hapus, verifikasi_pembayaran, setujui_surat',
  nama_tabel       VARCHAR(64)  NULL,
  id_data          BIGINT UNSIGNED NULL,
  nilai_lama       JSON NULL,
  nilai_baru       JSON NULL,
  alamat_ip        VARCHAR(45)  NULL,
  agen_pengguna    VARCHAR(255) NULL,
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_audit_pengguna (id_pengguna, dibuat_pada),
  KEY idx_audit_data (nama_tabel, id_data),
  KEY idx_audit_organisasi (id_organisasi, dibuat_pada),
  CONSTRAINT fk_audit_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id) ON DELETE SET NULL,
  CONSTRAINT fk_audit_pengguna FOREIGN KEY (id_pengguna) REFERENCES pengguna (id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='Jejak aktivitas penting (F-023). Hanya INSERT; jangan di-UPDATE/DELETE oleh aplikasi';

-- 39. pengaturan -------------------------------------------------------
CREATE TABLE pengaturan (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi       BIGINT UNSIGNED NOT NULL,
  kunci_pengaturan    VARCHAR(80) NOT NULL,
  nilai_pengaturan    TEXT NULL,
  diperbarui_pada     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_pengaturan (id_organisasi, kunci_pengaturan),
  CONSTRAINT fk_pengaturan_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id)
) ENGINE=InnoDB COMMENT='Konfigurasi per lingkungan: identitas, format nomor surat/kuitansi, preferensi';

-- 40. cadangan_data ----------------------------------------------------
CREATE TABLE cadangan_data (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organisasi    BIGINT UNSIGNED NULL COMMENT 'NULL = cadangan seluruh platform',
  path_berkas      VARCHAR(500) NOT NULL,
  jenis_cadangan   ENUM('penuh','inkremental','per_organisasi') NOT NULL DEFAULT 'penuh',
  status           ENUM('berjalan','berhasil','gagal') NOT NULL DEFAULT 'berjalan',
  ukuran           BIGINT UNSIGNED NULL,
  terenkripsi      TINYINT(1) NOT NULL DEFAULT 1,
  dibuat_oleh      BIGINT UNSIGNED NULL COMMENT 'NULL = dijalankan terjadwal',
  dibuat_pada      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  selesai_pada     DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_cadangan_organisasi (id_organisasi, dibuat_pada),
  CONSTRAINT fk_cadangan_organisasi FOREIGN KEY (id_organisasi) REFERENCES organisasi (id),
  CONSTRAINT fk_cadangan_pembuat FOREIGN KEY (dibuat_oleh) REFERENCES pengguna (id)
) ENGINE=InnoDB COMMENT='Riwayat pencadangan (F-026). Simpan file backup di luar repository';

-- =====================================================================
-- H. VIEW BANTU
-- =====================================================================

-- Saldo kas per organisasi (untuk dashboard F-015)
CREATE OR REPLACE VIEW v_saldo_kas AS
SELECT
  id_organisasi,
  COALESCE(SUM(CASE WHEN jenis_transaksi = 'masuk'  THEN nominal ELSE 0 END), 0) AS total_masuk,
  COALESCE(SUM(CASE WHEN jenis_transaksi = 'keluar' THEN nominal ELSE 0 END), 0) AS total_keluar,
  COALESCE(SUM(CASE WHEN jenis_transaksi = 'masuk' THEN nominal ELSE -nominal END), 0) AS saldo
FROM transaksi_kas
GROUP BY id_organisasi;

SET FOREIGN_KEY_CHECKS = 1;
