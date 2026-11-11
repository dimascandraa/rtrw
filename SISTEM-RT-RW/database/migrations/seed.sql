-- =====================================================================
-- SISTEM INFORMASI RT/RW  -  DATA AWAL (SEED)
-- Jalankan SETELAH schema.sql, pada database KOSONG:
--   mysql -u root -p sistem_rt_rw < database/seed.sql
--
-- BAGIAN 1 (peran, izin_akses, peran_izin_akses) = data WAJIB, dipakai di semua lingkungan.
-- BAGIAN 2 (data dummy)                            = HANYA untuk pengembangan dan demo.
--   Semua nama, NIK, No. KK, telepon, dan email di bawah adalah KARANGAN.
--   JANGAN diganti dengan data warga asli.
--
-- AKUN DUMMY (semua password: Password123)
--   superadmin@contoh.test  -> Super Admin
--   adminrw@contoh.test     -> Admin RW
--   ketuarw@contoh.test     -> Ketua RW
--   ketuart1@contoh.test    -> Ketua RT 01
--   sekretaris@contoh.test  -> Sekretaris
--   bendahara@contoh.test   -> Bendahara
--   agus@contoh.test        -> Warga (RT 03)
--   rudi@contoh.test        -> Warga, status menunggu_verifikasi (untuk uji verifikasi akun)
-- =====================================================================

SET NAMES utf8mb4;
USE sistem_rt_rw;

-- =====================================================================
-- BAGIAN 1. DATA WAJIB
-- =====================================================================

-- Peran (sumber: sheet "RoleHak Akses")
INSERT INTO peran (id, kode, nama, deskripsi) VALUES
  (1, 'super_admin', 'Super Admin', 'Mengelola seluruh sistem dan organisasi RT/RW.'),
  (2, 'admin_rw', 'Admin RW', 'Mengelola data dan konfigurasi tingkat RW.'),
  (3, 'ketua_rw', 'Ketua RW', 'Mengawasi dan menyetujui kegiatan administrasi RW.'),
  (4, 'ketua_rt', 'Ketua RT', 'Mengelola dan mengawasi administrasi di wilayah RT.'),
  (5, 'sekretaris', 'Sekretaris', 'Mengelola data warga, surat, pengumuman, dan administrasi.'),
  (6, 'bendahara', 'Bendahara', 'Mengelola iuran, tagihan, pembayaran, dan laporan keuangan.'),
  (7, 'warga', 'Warga', 'Mengakses informasi, mengajukan surat, dan melakukan pembayaran.');

-- Izin akses = modul x aksi (create/read/update/delete, plus approve untuk mutasi & surat)
INSERT INTO izin_akses (kode, nama, modul, aksi) VALUES
  ('pengguna.create', 'Tambah manajemen pengguna', 'pengguna', 'create'),
  ('pengguna.read', 'Lihat manajemen pengguna', 'pengguna', 'read'),
  ('pengguna.update', 'Ubah manajemen pengguna', 'pengguna', 'update'),
  ('pengguna.delete', 'Hapus manajemen pengguna', 'pengguna', 'delete'),
  ('wilayah.create', 'Tambah data rt/rw', 'wilayah', 'create'),
  ('wilayah.read', 'Lihat data rt/rw', 'wilayah', 'read'),
  ('wilayah.update', 'Ubah data rt/rw', 'wilayah', 'update'),
  ('wilayah.delete', 'Hapus data rt/rw', 'wilayah', 'delete'),
  ('keluarga.create', 'Tambah data keluarga', 'keluarga', 'create'),
  ('keluarga.read', 'Lihat data keluarga', 'keluarga', 'read'),
  ('keluarga.update', 'Ubah data keluarga', 'keluarga', 'update'),
  ('keluarga.delete', 'Hapus data keluarga', 'keluarga', 'delete'),
  ('warga.create', 'Tambah data warga', 'warga', 'create'),
  ('warga.read', 'Lihat data warga', 'warga', 'read'),
  ('warga.update', 'Ubah data warga', 'warga', 'update'),
  ('warga.delete', 'Hapus data warga', 'warga', 'delete'),
  ('mutasi_warga.create', 'Tambah mutasi warga', 'mutasi_warga', 'create'),
  ('mutasi_warga.read', 'Lihat mutasi warga', 'mutasi_warga', 'read'),
  ('mutasi_warga.update', 'Ubah mutasi warga', 'mutasi_warga', 'update'),
  ('mutasi_warga.delete', 'Hapus mutasi warga', 'mutasi_warga', 'delete'),
  ('mutasi_warga.approve', 'Setujui mutasi warga', 'mutasi_warga', 'approve'),
  ('kategori_iuran.create', 'Tambah kategori iuran', 'kategori_iuran', 'create'),
  ('kategori_iuran.read', 'Lihat kategori iuran', 'kategori_iuran', 'read'),
  ('kategori_iuran.update', 'Ubah kategori iuran', 'kategori_iuran', 'update'),
  ('kategori_iuran.delete', 'Hapus kategori iuran', 'kategori_iuran', 'delete'),
  ('tagihan.create', 'Tambah tagihan iuran', 'tagihan', 'create'),
  ('tagihan.read', 'Lihat tagihan iuran', 'tagihan', 'read'),
  ('tagihan.update', 'Ubah tagihan iuran', 'tagihan', 'update'),
  ('tagihan.delete', 'Hapus tagihan iuran', 'tagihan', 'delete'),
  ('pembayaran.create', 'Tambah pembayaran', 'pembayaran', 'create'),
  ('pembayaran.read', 'Lihat pembayaran', 'pembayaran', 'read'),
  ('pembayaran.update', 'Ubah pembayaran', 'pembayaran', 'update'),
  ('pembayaran.delete', 'Hapus pembayaran', 'pembayaran', 'delete'),
  ('laporan_keuangan.create', 'Tambah laporan keuangan', 'laporan_keuangan', 'create'),
  ('laporan_keuangan.read', 'Lihat laporan keuangan', 'laporan_keuangan', 'read'),
  ('laporan_keuangan.update', 'Ubah laporan keuangan', 'laporan_keuangan', 'update'),
  ('laporan_keuangan.delete', 'Hapus laporan keuangan', 'laporan_keuangan', 'delete'),
  ('pengajuan_surat.create', 'Tambah pengajuan surat', 'pengajuan_surat', 'create'),
  ('pengajuan_surat.read', 'Lihat pengajuan surat', 'pengajuan_surat', 'read'),
  ('pengajuan_surat.update', 'Ubah pengajuan surat', 'pengajuan_surat', 'update'),
  ('pengajuan_surat.delete', 'Hapus pengajuan surat', 'pengajuan_surat', 'delete'),
  ('pengajuan_surat.approve', 'Setujui pengajuan surat', 'pengajuan_surat', 'approve'),
  ('persetujuan_surat.create', 'Tambah persetujuan surat', 'persetujuan_surat', 'create'),
  ('persetujuan_surat.read', 'Lihat persetujuan surat', 'persetujuan_surat', 'read'),
  ('persetujuan_surat.update', 'Ubah persetujuan surat', 'persetujuan_surat', 'update'),
  ('persetujuan_surat.delete', 'Hapus persetujuan surat', 'persetujuan_surat', 'delete'),
  ('persetujuan_surat.approve', 'Setujui persetujuan surat', 'persetujuan_surat', 'approve'),
  ('pengumuman.create', 'Tambah pengumuman', 'pengumuman', 'create'),
  ('pengumuman.read', 'Lihat pengumuman', 'pengumuman', 'read'),
  ('pengumuman.update', 'Ubah pengumuman', 'pengumuman', 'update'),
  ('pengumuman.delete', 'Hapus pengumuman', 'pengumuman', 'delete'),
  ('kegiatan.create', 'Tambah kegiatan warga', 'kegiatan', 'create'),
  ('kegiatan.read', 'Lihat kegiatan warga', 'kegiatan', 'read'),
  ('kegiatan.update', 'Ubah kegiatan warga', 'kegiatan', 'update'),
  ('kegiatan.delete', 'Hapus kegiatan warga', 'kegiatan', 'delete'),
  ('pengaduan.create', 'Tambah pengaduan', 'pengaduan', 'create'),
  ('pengaduan.read', 'Lihat pengaduan', 'pengaduan', 'read'),
  ('pengaduan.update', 'Ubah pengaduan', 'pengaduan', 'update'),
  ('pengaduan.delete', 'Hapus pengaduan', 'pengaduan', 'delete'),
  ('laporan_aktivitas.create', 'Tambah laporan aktivitas', 'laporan_aktivitas', 'create'),
  ('laporan_aktivitas.read', 'Lihat laporan aktivitas', 'laporan_aktivitas', 'read'),
  ('laporan_aktivitas.update', 'Ubah laporan aktivitas', 'laporan_aktivitas', 'update'),
  ('laporan_aktivitas.delete', 'Hapus laporan aktivitas', 'laporan_aktivitas', 'delete'),
  ('pengaturan.create', 'Tambah pengaturan sistem', 'pengaturan', 'create'),
  ('pengaturan.read', 'Lihat pengaturan sistem', 'pengaturan', 'read'),
  ('pengaturan.update', 'Ubah pengaturan sistem', 'pengaturan', 'update'),
  ('pengaturan.delete', 'Hapus pengaturan sistem', 'pengaturan', 'delete');

-- Relasi peran <-> izin. terbatas = 1 untuk tanda * di matriks (hanya data milik sendiri / wilayahnya).
-- Catatan: "Approve" otomatis disertai "read". Warga diberi pembayaran.create (terbatas)
-- karena deskripsi role Warga menyebut "melakukan pembayaran" (usulan penyesuaian, konfirmasi ke tim).
INSERT INTO peran_izin_akses (id_peran, id_izin_akses, terbatas)
SELECT (SELECT id FROM peran WHERE kode='super_admin'), id, 0 FROM izin_akses
WHERE kode IN ('pengguna.create', 'pengguna.read', 'pengguna.update', 'pengguna.delete', 'wilayah.create', 'wilayah.read', 'wilayah.update', 'wilayah.delete', 'keluarga.create', 'keluarga.read', 'keluarga.update', 'keluarga.delete', 'warga.create', 'warga.read', 'warga.update', 'warga.delete', 'mutasi_warga.create', 'mutasi_warga.read', 'mutasi_warga.update', 'mutasi_warga.delete', 'kategori_iuran.create', 'kategori_iuran.read', 'kategori_iuran.update', 'kategori_iuran.delete', 'tagihan.create', 'tagihan.read', 'tagihan.update', 'tagihan.delete', 'pembayaran.create', 'pembayaran.read', 'pembayaran.update', 'pembayaran.delete', 'laporan_keuangan.create', 'laporan_keuangan.read', 'laporan_keuangan.update', 'laporan_keuangan.delete', 'pengajuan_surat.create', 'pengajuan_surat.read', 'pengajuan_surat.update', 'pengajuan_surat.delete', 'persetujuan_surat.create', 'persetujuan_surat.read', 'persetujuan_surat.update', 'persetujuan_surat.delete', 'pengumuman.create', 'pengumuman.read', 'pengumuman.update', 'pengumuman.delete', 'kegiatan.create', 'kegiatan.read', 'kegiatan.update', 'kegiatan.delete', 'pengaduan.create', 'pengaduan.read', 'pengaduan.update', 'pengaduan.delete', 'laporan_aktivitas.create', 'laporan_aktivitas.read', 'laporan_aktivitas.update', 'laporan_aktivitas.delete', 'pengaturan.create', 'pengaturan.read', 'pengaturan.update', 'pengaturan.delete');

INSERT INTO peran_izin_akses (id_peran, id_izin_akses, terbatas)
SELECT (SELECT id FROM peran WHERE kode='admin_rw'), id, 0 FROM izin_akses
WHERE kode IN ('pengguna.create', 'pengguna.read', 'pengguna.update', 'pengguna.delete', 'wilayah.create', 'wilayah.read', 'wilayah.update', 'wilayah.delete', 'keluarga.create', 'keluarga.read', 'keluarga.update', 'keluarga.delete', 'warga.create', 'warga.read', 'warga.update', 'warga.delete', 'mutasi_warga.create', 'mutasi_warga.read', 'mutasi_warga.update', 'mutasi_warga.delete', 'kategori_iuran.create', 'kategori_iuran.read', 'kategori_iuran.update', 'kategori_iuran.delete', 'tagihan.create', 'tagihan.read', 'tagihan.update', 'tagihan.delete', 'pembayaran.read', 'laporan_keuangan.read', 'pengajuan_surat.read', 'pengumuman.create', 'pengumuman.read', 'pengumuman.update', 'pengumuman.delete', 'kegiatan.create', 'kegiatan.read', 'kegiatan.update', 'kegiatan.delete', 'pengaduan.create', 'pengaduan.read', 'pengaduan.update', 'pengaduan.delete', 'laporan_aktivitas.read', 'pengaturan.create', 'pengaturan.read', 'pengaturan.update', 'pengaturan.delete');

INSERT INTO peran_izin_akses (id_peran, id_izin_akses, terbatas)
SELECT (SELECT id FROM peran WHERE kode='ketua_rw'), id, 0 FROM izin_akses
WHERE kode IN ('pengguna.read', 'wilayah.read', 'keluarga.read', 'warga.read', 'mutasi_warga.approve', 'mutasi_warga.read', 'kategori_iuran.read', 'tagihan.read', 'pembayaran.read', 'laporan_keuangan.read', 'pengumuman.create', 'pengumuman.read', 'pengumuman.update', 'pengumuman.delete', 'kegiatan.create', 'kegiatan.read', 'kegiatan.update', 'kegiatan.delete', 'pengaduan.read', 'laporan_aktivitas.read');

INSERT INTO peran_izin_akses (id_peran, id_izin_akses, terbatas)
SELECT (SELECT id FROM peran WHERE kode='ketua_rw'), id, 1 FROM izin_akses
WHERE kode IN ('pengajuan_surat.approve', 'pengajuan_surat.read', 'persetujuan_surat.approve', 'persetujuan_surat.read');

INSERT INTO peran_izin_akses (id_peran, id_izin_akses, terbatas)
SELECT (SELECT id FROM peran WHERE kode='ketua_rt'), id, 0 FROM izin_akses
WHERE kode IN ('pengguna.read', 'wilayah.read', 'keluarga.create', 'keluarga.read', 'keluarga.update', 'keluarga.delete', 'warga.create', 'warga.read', 'warga.update', 'warga.delete', 'mutasi_warga.create', 'mutasi_warga.read', 'mutasi_warga.update', 'mutasi_warga.delete', 'kategori_iuran.read', 'tagihan.read', 'pembayaran.read', 'laporan_keuangan.read', 'pengaduan.create', 'pengaduan.read', 'pengaduan.update', 'pengaduan.delete', 'laporan_aktivitas.read');

INSERT INTO peran_izin_akses (id_peran, id_izin_akses, terbatas)
SELECT (SELECT id FROM peran WHERE kode='ketua_rt'), id, 1 FROM izin_akses
WHERE kode IN ('pengajuan_surat.approve', 'pengajuan_surat.read', 'persetujuan_surat.approve', 'persetujuan_surat.read', 'pengumuman.create', 'pengumuman.read', 'pengumuman.update', 'pengumuman.delete', 'kegiatan.create', 'kegiatan.read', 'kegiatan.update', 'kegiatan.delete');

INSERT INTO peran_izin_akses (id_peran, id_izin_akses, terbatas)
SELECT (SELECT id FROM peran WHERE kode='sekretaris'), id, 0 FROM izin_akses
WHERE kode IN ('keluarga.create', 'keluarga.read', 'keluarga.update', 'keluarga.delete', 'warga.create', 'warga.read', 'warga.update', 'warga.delete', 'mutasi_warga.create', 'mutasi_warga.read', 'mutasi_warga.update', 'mutasi_warga.delete', 'pengajuan_surat.create', 'pengajuan_surat.read', 'pengajuan_surat.update', 'pengajuan_surat.delete', 'persetujuan_surat.read', 'pengaduan.create', 'pengaduan.read', 'pengaduan.update', 'pengaduan.delete', 'laporan_aktivitas.create', 'laporan_aktivitas.read', 'laporan_aktivitas.update', 'laporan_aktivitas.delete');

INSERT INTO peran_izin_akses (id_peran, id_izin_akses, terbatas)
SELECT (SELECT id FROM peran WHERE kode='sekretaris'), id, 1 FROM izin_akses
WHERE kode IN ('pengumuman.create', 'pengumuman.read', 'pengumuman.update', 'pengumuman.delete', 'kegiatan.create', 'kegiatan.read', 'kegiatan.update', 'kegiatan.delete');

INSERT INTO peran_izin_akses (id_peran, id_izin_akses, terbatas)
SELECT (SELECT id FROM peran WHERE kode='bendahara'), id, 0 FROM izin_akses
WHERE kode IN ('keluarga.read', 'warga.read', 'kategori_iuran.create', 'kategori_iuran.read', 'kategori_iuran.update', 'kategori_iuran.delete', 'tagihan.create', 'tagihan.read', 'tagihan.update', 'tagihan.delete', 'pembayaran.create', 'pembayaran.read', 'pembayaran.update', 'pembayaran.delete', 'laporan_keuangan.create', 'laporan_keuangan.read', 'laporan_keuangan.update', 'laporan_keuangan.delete', 'kegiatan.read', 'laporan_aktivitas.read');

INSERT INTO peran_izin_akses (id_peran, id_izin_akses, terbatas)
SELECT (SELECT id FROM peran WHERE kode='warga'), id, 0 FROM izin_akses
WHERE kode IN ('kategori_iuran.read', 'pengajuan_surat.create', 'pengumuman.read', 'kegiatan.read', 'pengaduan.create');

INSERT INTO peran_izin_akses (id_peran, id_izin_akses, terbatas)
SELECT (SELECT id FROM peran WHERE kode='warga'), id, 1 FROM izin_akses
WHERE kode IN ('keluarga.read', 'warga.read', 'mutasi_warga.read', 'tagihan.read', 'pembayaran.read', 'laporan_keuangan.read', 'persetujuan_surat.read', 'pengaduan.read', 'pembayaran.create');

-- =====================================================================
-- BAGIAN 2. DATA DUMMY (hanya pengembangan & demo)
-- =====================================================================

-- Wilayah ---------------------------------------------------------------
INSERT INTO organisasi (id, kode, nama, alamat, status) VALUES
  (1, 'PRM-001', 'Perumahan Griya Contoh', 'Jl. Contoh Raya, Kota Contoh', 'aktif');

INSERT INTO rw (id, id_organisasi, nomor_rw, nama_ketua) VALUES
  (1, 1, '05', 'Budi Santoso');

INSERT INTO rt (id, id_organisasi, id_rw, nomor_rt, nama_ketua) VALUES
  (1, 1, 1, '01', 'Andi Wijaya'),
  (2, 1, 1, '02', 'Hendra Gunawan'),
  (3, 1, 1, '03', 'Agus Pratama');

-- Keluarga (id_kepala_keluarga diisi setelah data warga ada) -----------------
INSERT INTO keluarga (id, id_organisasi, id_rt, nomor_kk, alamat, nomor_rumah, jenis_hunian) VALUES
  (1, 1, 1, '3500000000000001', 'Jl. Mawar',   '1', 'milik_sendiri'),
  (2, 1, 1, '3500000000000002', 'Jl. Mawar',   '2', 'kontrak'),
  (3, 1, 2, '3500000000000003', 'Jl. Melati',  '5', 'milik_sendiri'),
  (4, 1, 2, '3500000000000004', 'Jl. Melati',  '7', 'milik_sendiri'),
  (5, 1, 3, '3500000000000005', 'Jl. Anggrek', '3', 'milik_sendiri'),
  (6, 1, 3, '3500000000000006', 'Jl. Anggrek', '9', 'kos');

-- Warga (NIK karangan) ---------------------------------------------------------
INSERT INTO warga (id, id_organisasi, id_keluarga, nik, nama_lengkap, tempat_lahir, tanggal_lahir, jenis_kelamin, hubungan_keluarga, pekerjaan, telepon, status_tinggal, status) VALUES
  (1,  1, 1, '3500000000000101', 'Budi Santoso',   'Kota Contoh', '1975-03-12', 'L', 'kepala_keluarga', 'Wiraswasta',  '081200000001', 'tetap',   'aktif'),
  (2,  1, 1, '3500000000000102', 'Siti Aminah',    'Kota Contoh', '1978-07-21', 'P', 'istri',           'Guru',        '081200000002', 'tetap',   'aktif'),
  (3,  1, 2, '3500000000000103', 'Andi Wijaya',    'Kota Contoh', '1980-01-30', 'L', 'kepala_keluarga', 'Karyawan',    '081200000003', 'kontrak', 'aktif'),
  (4,  1, 2, '3500000000000104', 'Rina Wijaya',    'Kota Contoh', '1983-11-05', 'P', 'istri',           'Karyawan',    '081200000004', 'kontrak', 'aktif'),
  (5,  1, 3, '3500000000000105', 'Hendra Gunawan', 'Kota Contoh', '1972-05-18', 'L', 'kepala_keluarga', 'PNS',         '081200000005', 'tetap',   'aktif'),
  (6,  1, 3, '3500000000000106', 'Dewi Lestari',   'Kota Contoh', '1976-09-09', 'P', 'istri',           'Ibu rumah tangga', '081200000006', 'tetap', 'aktif'),
  (7,  1, 4, '3500000000000107', 'Slamet Riyadi',  'Kota Contoh', '1969-12-01', 'L', 'kepala_keluarga', 'Pensiunan',   '081200000007', 'tetap',   'aktif'),
  (8,  1, 4, '3500000000000108', 'Putri Riyadi',   'Kota Contoh', '2001-04-14', 'P', 'anak',            'Mahasiswa',   '081200000008', 'tetap',   'aktif'),
  (9,  1, 5, '3500000000000109', 'Agus Pratama',   'Kota Contoh', '1985-08-25', 'L', 'kepala_keluarga', 'Wiraswasta',  '081200000009', 'tetap',   'aktif'),
  (10, 1, 5, '3500000000000110', 'Maya Sari',      'Kota Contoh', '1987-02-17', 'P', 'istri',           'Karyawan',    '081200000010', 'tetap',   'aktif'),
  (11, 1, 6, '3500000000000111', 'Rudi Hartono',   'Kota Contoh', '1990-06-06', 'L', 'kepala_keluarga', 'Karyawan',    '081200000011', 'kos',     'aktif'),
  (12, 1, 6, '3500000000000112', 'Lina Marlina',   'Kota Contoh', '1992-10-10', 'P', 'istri',           'Karyawan',    '081200000012', 'kos',     'aktif');

UPDATE keluarga SET id_kepala_keluarga = 1  WHERE id = 1;
UPDATE keluarga SET id_kepala_keluarga = 3  WHERE id = 2;
UPDATE keluarga SET id_kepala_keluarga = 5  WHERE id = 3;
UPDATE keluarga SET id_kepala_keluarga = 7  WHERE id = 4;
UPDATE keluarga SET id_kepala_keluarga = 9  WHERE id = 5;
UPDATE keluarga SET id_kepala_keluarga = 11 WHERE id = 6;

INSERT INTO mutasi_warga (id, id_organisasi, id_warga, jenis_mutasi, tanggal_berlaku, asal_tujuan, catatan, dicatat_oleh) VALUES
  (1, 1, 11, 'pindah_masuk', '2026-09-01', 'Kota Lain', 'Menempati kos Jl. Anggrek 9', NULL),
  (2, 1, 12, 'pindah_masuk', '2026-09-01', 'Kota Lain', 'Menempati kos Jl. Anggrek 9', NULL);

-- Pengguna (password semua akun = Password123, hash bcrypt) -------------------
INSERT INTO pengguna (id, id_warga, nama, email, telepon, hash_kata_sandi, status, email_terverifikasi_pada) VALUES
  (1, NULL, 'Super Admin',    'superadmin@contoh.test', '081300000001', '$2y$10$9.lzmaZ2PPHUcZtZaXPgjOdgLZo7QMZ.jsb1o6tHMPsTTM2WjWtvK', 'aktif', NOW()),
  (2, 4,    'Rina Wijaya',    'adminrw@contoh.test',    '081300000002', '$2y$10$9.lzmaZ2PPHUcZtZaXPgjOdgLZo7QMZ.jsb1o6tHMPsTTM2WjWtvK', 'aktif', NOW()),
  (3, 1,    'Budi Santoso',   'ketuarw@contoh.test',    '081300000003', '$2y$10$9.lzmaZ2PPHUcZtZaXPgjOdgLZo7QMZ.jsb1o6tHMPsTTM2WjWtvK', 'aktif', NOW()),
  (4, 3,    'Andi Wijaya',    'ketuart1@contoh.test',   '081300000004', '$2y$10$9.lzmaZ2PPHUcZtZaXPgjOdgLZo7QMZ.jsb1o6tHMPsTTM2WjWtvK', 'aktif', NOW()),
  (5, 5,    'Hendra Gunawan', 'sekretaris@contoh.test', '081300000005', '$2y$10$9.lzmaZ2PPHUcZtZaXPgjOdgLZo7QMZ.jsb1o6tHMPsTTM2WjWtvK', 'aktif', NOW()),
  (6, 6,    'Dewi Lestari',   'bendahara@contoh.test',  '081300000006', '$2y$10$9.lzmaZ2PPHUcZtZaXPgjOdgLZo7QMZ.jsb1o6tHMPsTTM2WjWtvK', 'aktif', NOW()),
  (7, 9,    'Agus Pratama',  'agus@contoh.test',       '081300000007', '$2y$10$9.lzmaZ2PPHUcZtZaXPgjOdgLZo7QMZ.jsb1o6tHMPsTTM2WjWtvK', 'aktif', NOW()),
  (8, 11,   'Rudi Hartono',  'rudi@contoh.test',       '081300000008', '$2y$10$9.lzmaZ2PPHUcZtZaXPgjOdgLZo7QMZ.jsb1o6tHMPsTTM2WjWtvK', 'menunggu_verifikasi', NULL);

-- Penugasan: pengguna -> peran -> wilayah (Super Admin lintas organisasi => organisasi NULL)
-- id_peran: 1 super_admin, 2 admin_rw, 3 ketua_rw, 4 ketua_rt, 5 sekretaris, 6 bendahara, 7 warga
INSERT INTO penugasan_pengguna (id_pengguna, id_peran, id_organisasi, id_rw, id_rt, aktif, mulai_pada) VALUES
  (1, 1, NULL, NULL, NULL, 1, '2026-01-01'),
  (2, 2, 1, 1, NULL, 1, '2026-01-01'),
  (3, 3, 1, 1, NULL, 1, '2026-01-01'),
  (4, 4, 1, 1, 1,    1, '2026-01-01'),
  (5, 5, 1, 1, NULL, 1, '2026-01-01'),
  (6, 6, 1, 1, NULL, 1, '2026-01-01'),
  (7, 7, 1, 1, 3,    1, '2026-01-01');
-- Pengguna 8 (Rudi) belum punya penugasan: Admin membuatnya saat verifikasi akun (F-002).

INSERT INTO kode_verifikasi (id_pengguna, tujuan, kanal, hash_kode, jumlah_percobaan, kedaluwarsa_pada) VALUES
  (8, 'registrasi', 'email', SHA2('kode-otp-dummy', 256), 0, DATE_ADD(NOW(), INTERVAL 10 MINUTE));

INSERT INTO sesi_pengguna (id_pengguna, nama_perangkat, hash_token, alamat_ip, agen_pengguna, terakhir_aktif_pada, kedaluwarsa_pada) VALUES
  (3, 'Laptop Ketua RW', SHA2('token-dummy-ketuarw', 256), '127.0.0.1', 'Mozilla/5.0 (dummy)', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY));

-- Berkas (metadata saja; file sebenarnya tidak ada) -----------------------------
INSERT INTO berkas (id, id_organisasi, path_penyimpanan, nama_asli, tipe_mime, ukuran, diunggah_oleh) VALUES
  (1, 1, 'private/bukti/2026/10/dummy-bukti-1.jpg', 'bukti-transfer-oktober.jpg', 'image/jpeg', 120000, 4),
  (2, 1, 'private/bukti/2026/11/dummy-bukti-2.jpg', 'bukti-transfer-november.jpg', 'image/jpeg', 98000, 4),
  (3, 1, 'private/bukti/2026/11/dummy-bukti-3.jpg', 'bukti-transfer-agus.jpg', 'image/jpeg', 110000, 7),
  (4, 1, 'private/pengaduan/2026/11/dummy-lampu.jpg', 'lampu-jalan-mati.jpg', 'image/jpeg', 150000, 7),
  (5, 1, 'private/surat/2026/11/dummy-surat-domisili.pdf', 'surat-domisili.pdf', 'application/pdf', 45000, 5);

-- Keuangan --------------------------------------------------------------------------
INSERT INTO kategori_iuran (id, id_organisasi, nama, jenis_iuran, deskripsi) VALUES
  (1, 1, 'Kebersihan', 'wajib',    'Pengangkutan sampah dan kebersihan lingkungan'),
  (2, 1, 'Keamanan',   'wajib',    'Honor petugas keamanan'),
  (3, 1, 'Sosial',     'sukarela', 'Dana sosial warga');

INSERT INTO aturan_iuran (id_organisasi, id_kategori_iuran, id_rt, jenis_hunian, nominal, siklus_tagihan, hari_jatuh_tempo, boleh_cicilan, berlaku_mulai) VALUES
  (1, 1, NULL, NULL, 20000, 'bulanan', 10, 1, '2026-01-01'),
  (1, 1, 3,    NULL, 25000, 'bulanan', 10, 1, '2026-01-01'),
  (1, 2, NULL, NULL, 30000, 'bulanan', 10, 1, '2026-01-01'),
  (1, 3, NULL, NULL, 10000, 'bulanan', 10, 0, '2026-01-01');

INSERT INTO periode_tagihan (id, id_organisasi, nama, periode_mulai, periode_akhir, status) VALUES
  (1, 1, 'Oktober 2026',  '2026-10-01', '2026-10-31', 'ditutup'),
  (2, 1, 'November 2026', '2026-11-01', '2026-11-30', 'terbit');

INSERT INTO tagihan (id, id_organisasi, id_keluarga, id_periode_tagihan, nomor_tagihan, total_nominal, nominal_dibayar, status, tanggal_jatuh_tempo) VALUES
  (1, 1, 1, 1, 'TGH/2026/10/0001', 50000, 50000, 'lunas', '2026-10-10'),
  (2, 1, 2, 1, 'TGH/2026/10/0002', 50000, 50000, 'lunas', '2026-10-10'),
  (3, 1, 3, 1, 'TGH/2026/10/0003', 50000, 20000, 'sebagian', '2026-10-10'),
  (4, 1, 4, 1, 'TGH/2026/10/0004', 50000, 50000, 'lunas', '2026-10-10'),
  (5, 1, 5, 1, 'TGH/2026/10/0005', 55000, 0, 'lewat_jatuh_tempo', '2026-10-10'),
  (6, 1, 6, 1, 'TGH/2026/10/0006', 55000, 55000, 'lunas', '2026-10-10'),
  (7, 1, 1, 2, 'TGH/2026/11/0001', 50000, 50000, 'lunas', '2026-11-10'),
  (8, 1, 2, 2, 'TGH/2026/11/0002', 50000, 0, 'belum_dibayar', '2026-11-10'),
  (9, 1, 3, 2, 'TGH/2026/11/0003', 50000, 0, 'belum_dibayar', '2026-11-10'),
  (10, 1, 4, 2, 'TGH/2026/11/0004', 50000, 0, 'belum_dibayar', '2026-11-10'),
  (11, 1, 5, 2, 'TGH/2026/11/0005', 55000, 0, 'belum_dibayar', '2026-11-10'),
  (12, 1, 6, 2, 'TGH/2026/11/0006', 55000, 0, 'belum_dibayar', '2026-11-10');

INSERT INTO rincian_tagihan (id_tagihan, id_kategori_iuran, deskripsi, nominal) VALUES
  (1, 1, 'Iuran kebersihan', 20000),
  (1, 2, 'Iuran keamanan', 30000),
  (2, 1, 'Iuran kebersihan', 20000),
  (2, 2, 'Iuran keamanan', 30000),
  (3, 1, 'Iuran kebersihan', 20000),
  (3, 2, 'Iuran keamanan', 30000),
  (4, 1, 'Iuran kebersihan', 20000),
  (4, 2, 'Iuran keamanan', 30000),
  (5, 1, 'Iuran kebersihan', 25000),
  (5, 2, 'Iuran keamanan', 30000),
  (6, 1, 'Iuran kebersihan', 25000),
  (6, 2, 'Iuran keamanan', 30000),
  (7, 1, 'Iuran kebersihan', 20000),
  (7, 2, 'Iuran keamanan', 30000),
  (8, 1, 'Iuran kebersihan', 20000),
  (8, 2, 'Iuran keamanan', 30000),
  (9, 1, 'Iuran kebersihan', 20000),
  (9, 2, 'Iuran keamanan', 30000),
  (10, 1, 'Iuran kebersihan', 20000),
  (10, 2, 'Iuran keamanan', 30000),
  (11, 1, 'Iuran kebersihan', 25000),
  (11, 2, 'Iuran keamanan', 30000),
  (12, 1, 'Iuran kebersihan', 25000),
  (12, 2, 'Iuran keamanan', 30000);

-- Pembayaran: ada yang terverifikasi, ditolak, dan menunggu verifikasi (untuk uji alur bendahara)
INSERT INTO pembayaran (id, id_organisasi, id_tagihan, id_pengguna_pembayar, nominal, metode, status, referensi_pembayaran, dibayar_pada, diverifikasi_oleh, diverifikasi_pada, catatan_verifikasi) VALUES
  (1, 1, 1,  3,    50000, 'qris',     'terverifikasi',       'QRIS-DUMMY-0001', '2026-10-05 09:15:00', 6, '2026-10-05 10:00:00', NULL),
  (2, 1, 2,  4,    50000, 'transfer', 'terverifikasi',       'TRF-DUMMY-0002',  '2026-10-07 19:20:00', 6, '2026-10-08 08:00:00', NULL),
  (3, 1, 3,  NULL, 20000, 'tunai',    'terverifikasi',       NULL,              '2026-10-09 16:00:00', 6, '2026-10-09 16:00:00', 'Cicilan pertama, dicatat bendahara'),
  (4, 1, 4,  NULL, 50000, 'tunai',    'terverifikasi',       NULL,              '2026-10-08 17:30:00', 6, '2026-10-08 17:30:00', NULL),
  (5, 1, 6,  NULL, 55000, 'tunai',    'terverifikasi',       NULL,              '2026-10-09 18:00:00', 6, '2026-10-09 18:00:00', NULL),
  (6, 1, 7,  3,    50000, 'qris',     'terverifikasi',       'QRIS-DUMMY-0006', '2026-11-03 08:45:00', 6, '2026-11-03 09:30:00', NULL),
  (7, 1, 8,  4,    50000, 'transfer', 'ditolak',             'TRF-DUMMY-0007',  '2026-11-05 20:00:00', 6, '2026-11-06 08:00:00', 'Bukti transfer tidak terbaca, mohon unggah ulang'),
  (8, 1, 11, 7,    55000, 'transfer', 'menunggu_verifikasi', 'TRF-DUMMY-0008',  '2026-11-07 21:10:00', NULL, NULL, NULL);

INSERT INTO bukti_pembayaran (id_pembayaran, id_berkas, diunggah_oleh, status_verifikasi, catatan) VALUES
  (2, 1, 4, 'diterima', NULL),
  (7, 2, 4, 'ditolak',  'Gambar buram'),
  (8, 3, 7, 'menunggu', NULL);

INSERT INTO kuitansi (id_organisasi, id_pembayaran, nomor_kuitansi, diterbitkan_pada) VALUES
  (1, 1, 'KWT/2026/10/0001', '2026-10-05 10:00:00'),
  (1, 2, 'KWT/2026/10/0002', '2026-10-08 08:00:00'),
  (1, 3, 'KWT/2026/10/0003', '2026-10-09 16:00:00'),
  (1, 4, 'KWT/2026/10/0004', '2026-10-08 17:30:00'),
  (1, 5, 'KWT/2026/10/0005', '2026-10-09 18:00:00'),
  (1, 6, 'KWT/2026/11/0001', '2026-11-03 09:30:00');

INSERT INTO kategori_pengeluaran (id, id_organisasi, nama) VALUES
  (1, 1, 'Kebersihan'), (2, 1, 'Keamanan'), (3, 1, 'Sosial'), (4, 1, 'Perbaikan');

-- Kas masuk dicatat dari pembayaran TERVERIFIKASI; kas keluar butuh kategori pengeluaran
INSERT INTO transaksi_kas (id_organisasi, jenis_transaksi, id_kategori_pengeluaran, id_pembayaran, nominal, tanggal_transaksi, keterangan, dicatat_oleh) VALUES
  (1, 'masuk',  NULL, 1, 50000, '2026-10-05', 'Iuran Oktober - KK 1', 6),
  (1, 'masuk',  NULL, 2, 50000, '2026-10-08', 'Iuran Oktober - KK 2', 6),
  (1, 'masuk',  NULL, 3, 20000, '2026-10-09', 'Iuran Oktober (cicilan) - KK 3', 6),
  (1, 'masuk',  NULL, 4, 50000, '2026-10-08', 'Iuran Oktober - KK 4', 6),
  (1, 'masuk',  NULL, 5, 55000, '2026-10-09', 'Iuran Oktober - KK 6', 6),
  (1, 'masuk',  NULL, 6, 50000, '2026-11-03', 'Iuran November - KK 1', 6),
  (1, 'masuk',  NULL, NULL, 100000, '2026-10-20', 'Sumbangan warga untuk kegiatan 17-an', 6),
  (1, 'keluar', 1, NULL, 100000, '2026-10-31', 'Honor petugas kebersihan', 6),
  (1, 'keluar', 2, NULL, 120000, '2026-10-31', 'Honor petugas keamanan', 6),
  (1, 'keluar', 4, NULL,  25000, '2026-11-02', 'Ganti lampu pos ronda', 6);

-- Pengumuman, notifikasi ----------------------------------------------------------------
INSERT INTO pengumuman (id, id_organisasi, id_rt, judul, isi, dibuat_oleh, dipublikasikan_pada, kedaluwarsa_pada) VALUES
  (1, 1, NULL, 'Kerja bakti lingkungan', 'Kerja bakti dilaksanakan Minggu, 15 November 2026 pukul 07.00 di area RW 05.', 3, '2026-11-05 08:00:00', '2026-11-16 00:00:00'),
  (2, 1, 1,    'Pengumuman iuran RT 01',  'Iuran November jatuh tempo tanggal 10. Mohon dibayar melalui aplikasi.', 4, '2026-11-02 08:00:00', '2026-11-11 00:00:00');

INSERT INTO target_pengumuman (id_pengumuman, id_rw, id_rt) VALUES
  (1, 1, NULL),
  (2, NULL, 1);

INSERT INTO notifikasi (id, id_organisasi, id_pengguna, judul, isi, jenis, tabel_referensi, id_referensi, dibaca_pada) VALUES
  (1, 1, 7, 'Tagihan November 2026', 'Tagihan Rp 55.000 jatuh tempo 10 November 2026.', 'tagihan', 'tagihan', 11, NULL),
  (2, 1, 6, 'Pembayaran perlu diverifikasi', 'Ada 1 pembayaran menunggu verifikasi.', 'pembayaran', 'pembayaran', 8, NULL),
  (3, 1, 7, 'Pengumuman baru', 'Kerja bakti lingkungan', 'pengumuman', 'pengumuman', 1, NOW());

INSERT INTO log_notifikasi (id_notifikasi, kanal, tujuan, status, dikirim_pada) VALUES
  (1, 'aplikasi', 'agus@contoh.test', 'terkirim', NOW()),
  (1, 'email',    'agus@contoh.test', 'antre',    NULL);

-- Surat --------------------------------------------------------------------------------------
INSERT INTO jenis_surat (id, id_organisasi, kode, nama, templat, isian_wajib) VALUES
  (1, 1, 'DOM', 'Surat Keterangan Domisili', 'Yang bertanda tangan di bawah ini menerangkan bahwa {{nama}} berdomisili di {{alamat}}.', JSON_ARRAY('keperluan')),
  (2, 1, 'PGT', 'Surat Pengantar',           'Surat pengantar untuk {{nama}} guna keperluan {{keperluan}}.', JSON_ARRAY('keperluan', 'instansi_tujuan')),
  (3, 1, 'USH', 'Surat Keterangan Usaha',    'Menerangkan bahwa {{nama}} menjalankan usaha {{nama_usaha}}.', JSON_ARRAY('nama_usaha', 'alamat_usaha'));

INSERT INTO pengajuan_surat (id, id_organisasi, id_warga, id_jenis_surat, id_pengguna_pengaju, keperluan, data_isian, status, diajukan_pada, diproses_oleh, nomor_surat, diterbitkan_pada, id_berkas) VALUES
  (1, 1, 9, 1, 7, 'Melamar pekerjaan', JSON_OBJECT('keperluan', 'Melamar pekerjaan'), 'diterbitkan', '2026-11-03 10:00:00', 5, 'DOM/001/RT03-RW05/XI/2026', '2026-11-04 13:00:00', 5),
  (2, 1, 3, 2, 4, 'Pengurusan administrasi kantor', JSON_OBJECT('keperluan', 'Administrasi kantor', 'instansi_tujuan', 'Kantor Kelurahan'), 'diajukan', '2026-11-07 09:00:00', NULL, NULL, NULL, NULL);

INSERT INTO persetujuan_surat (id_pengajuan_surat, id_pengguna_penyetuju, tahap, keputusan, catatan, diputuskan_pada) VALUES
  (1, 4, 1, 'setuju', 'Data sesuai', '2026-11-03 15:00:00'),
  (1, 3, 2, 'setuju', NULL,          '2026-11-04 09:00:00');

-- Kegiatan, pengaduan, kontak, survei ---------------------------------------------------------------
INSERT INTO kegiatan (id, id_organisasi, id_rt, judul, deskripsi, jenis, mulai_pada, selesai_pada, lokasi, dibuat_oleh) VALUES
  (1, 1, NULL, 'Kerja bakti lingkungan', 'Membersihkan selokan dan area taman.', 'kerja_bakti', '2026-11-15 07:00:00', '2026-11-15 10:00:00', 'Taman RW 05', 3),
  (2, 1, 1,    'Rapat bulanan RT 01',    'Pembahasan iuran dan keamanan.',      'rapat',       '2026-11-20 19:30:00', '2026-11-20 21:00:00', 'Pos RT 01', 4);

INSERT INTO kehadiran_kegiatan (id_kegiatan, id_warga, status_kehadiran) VALUES
  (1, 9, 'akan_hadir'), (1, 1, 'akan_hadir'), (2, 3, 'akan_hadir');

INSERT INTO pengaduan (id, id_organisasi, id_warga, kategori, judul, deskripsi, status, bersifat_privat, ditugaskan_ke, id_berkas) VALUES
  (1, 1, 9, 'fasilitas_rusak', 'Lampu jalan mati', 'Lampu jalan di depan Jl. Anggrek 3 mati sejak dua hari lalu.', 'diproses', 0, 4, 4);

INSERT INTO tanggapan_pengaduan (id_pengaduan, id_pengguna_penanggap, tanggapan) VALUES
  (1, 4, 'Terima kasih laporannya. Sudah dikoordinasikan dengan petugas listrik.');

INSERT INTO kontak_darurat (id_organisasi, kategori, nama, telepon, alamat, urutan) VALUES
  (1, 'pengurus',          'Ketua RW 05 (Budi Santoso)', '081200000001', NULL, 1),
  (1, 'keamanan',          'Pos Satpam Perumahan',       '081200000099', 'Gerbang utama', 2),
  (1, 'ambulans',          'Ambulans',                   '119', NULL, 3),
  (1, 'pemadam_kebakaran', 'Pemadam Kebakaran',          '113', NULL, 4),
  (1, 'polisi',            'Polisi',                     '110', NULL, 5),
  (1, 'puskesmas',         'Puskesmas Contoh',           '021-0000000', 'Jl. Contoh No. 1', 6);

INSERT INTO survei (id, id_organisasi, judul, deskripsi, dimulai_pada, berakhir_pada, anonim, dibuat_oleh) VALUES
  (1, 1, 'Kepuasan layanan pengurus Q4 2026', 'Mohon nilai layanan pengurus RT/RW.', '2026-11-01 00:00:00', '2026-11-30 23:59:59', 1, 3);

INSERT INTO jawaban_survei (id_survei, id_warga, penilaian, komentar) VALUES
  (1, 9, 4, 'Pelayanan cepat, mohon info iuran lebih jelas.'),
  (1, 1, 5, NULL);

-- Sistem -----------------------------------------------------------------------------------------------
INSERT INTO pengaturan (id_organisasi, kunci_pengaturan, nilai_pengaturan) VALUES
  (1, 'nama_lingkungan',        'Perumahan Griya Contoh'),
  (1, 'zona_waktu',             'Asia/Jakarta'),
  (1, 'format_nomor_tagihan',   'TGH/{YYYY}/{MM}/{NNNN}'),
  (1, 'format_nomor_kuitansi',  'KWT/{YYYY}/{MM}/{NNNN}'),
  (1, 'format_nomor_surat',     '{KODE}/{NNN}/RT{RT}-RW{RW}/{ROMAWI}/{YYYY}');

INSERT INTO cadangan_data (id_organisasi, path_berkas, jenis_cadangan, status, ukuran, terenkripsi, dibuat_oleh, selesai_pada) VALUES
  (NULL, 'private/backup/dummy-backup-2026-11-07.sql.enc', 'penuh', 'berhasil', 524288, 1, NULL, NOW());

INSERT INTO log_audit (id_organisasi, id_pengguna, aksi, nama_tabel, id_data, nilai_lama, nilai_baru, alamat_ip) VALUES
  (1, 6, 'verifikasi_pembayaran', 'pembayaran', 6, JSON_OBJECT('status', 'menunggu_verifikasi'), JSON_OBJECT('status', 'terverifikasi'), '127.0.0.1'),
  (1, 6, 'tolak_pembayaran',      'pembayaran', 7, JSON_OBJECT('status', 'menunggu_verifikasi'), JSON_OBJECT('status', 'ditolak'),       '127.0.0.1'),
  (1, 3, 'login',                 'pengguna',   3, NULL, NULL, '127.0.0.1');
