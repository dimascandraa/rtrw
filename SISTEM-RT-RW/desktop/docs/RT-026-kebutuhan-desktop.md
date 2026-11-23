# RT-026: Penentuan Kebutuhan Desktop

| | |
|---|---|
| **Proyek** | Sistem Informasi RT/RW |
| **Tugas** | RT-026: Tentukan kebutuhan desktop (aplikasi admin / cukup web responsif) |
| **PIC** | Taufiq (Leader), Fachry, Umam |
| **Deadline** | 26 November 2026 |
| **Status** | Draf untuk dikonfirmasi Project Leader (Dimas) |

## 1. Keputusan

**Desktop dibuat sebagai aplikasi Java Swing yang menjadi klien REST API untuk pengurus dan warga, dengan fitur yang dibatasi oleh izin backend.**

Dasarnya adalah arsitektur yang sudah disepakati tim:

- Desktop memakai **Java Swing**, berkomunikasi lewat **REST API (JSON)** ke Backend PHP Native, tanpa framework.
- Hanya backend yang mengakses MySQL. Desktop **tidak punya database atau logika bisnis sendiri**.
- Warga tetap dilayani Mobile dan Web; desktop juga dapat dipakai untuk alur warga yang tersedia melalui API, seperti pengajuan surat.
- Desktop tidak mengasumsikan izin berdasarkan peran saja. Menu disaring dari `user.permissions` dan backend tetap memvalidasi setiap request.

## 2. Mengapa aplikasi admin, bukan "cukup web responsif"

| Pertimbangan | Penjelasan |
|---|---|
| Kebutuhan platform | Tujuan tugas besar adalah memahami aplikasi multiplatform. Arsitektur tim sudah menetapkan tiga aplikasi: Web, Mobile, Desktop |
| Kebutuhan pengguna | Warga dan pengurus dapat memakai desktop untuk alur yang tersedia di API; tabel mendukung pencarian, pengurutan, ekspor CSV, dan cetak |
| Nilai tambah desktop | Tabel yang bisa dicari dan diurutkan, ekspor CSV untuk Excel, cetak langsung, jendela sendiri tanpa browser |
| Risiko | Kecil: tidak ada aturan bisnis yang ditulis dua kali, semua lewat API yang sama |

Jika Project Leader atau dosen menilai desktop tidak diperlukan, fallback-nya adalah web responsif untuk pengurus, dan RT-027 ditandai tidak diperlukan.

## 3. Pengguna dan peran

Peran menurut Daftar Fitur F-003: Super Admin, Admin Perumahan, Ketua RT/RW, Bendahara, Pengurus, Warga.

| Peran | Memakai desktop? | Keterangan |
|---|---|---|
| Bendahara | Ya, sesuai izin API | Tagihan, pembayaran baca-saja, kas, ringkasan |
| Ketua RT/RW, Pengurus | Ya, sesuai izin API | Data warga, pengumuman, dan persetujuan surat/akun bila berhak |
| Admin Perumahan, Super Admin | Ya | Pengelolaan data dan platform |
| Warga | Ya, terbatas | Pengajuan dan status surat, data milik sendiri, dan fitur lain hanya bila backend memberi izin |

## 4. Cakupan fitur desktop

Dipetakan dari Daftar Fitur (F-001 sampai F-027). Status: **Selesai** (sudah ada di aplikasi), **Tahap berikut** (rencana), **Di luar desktop**.

| Fitur | Status | Catatan |
|---|---|---|
| Login, sesi | Selesai | `/api/v1/auth/*`, Bearer token dan cookie; menu mengikuti izin |
| Dashboard statistik (F-015) | Selesai (ringkas) | Ringkasan dari `GET /api/v1/dashboard/summary` |
| Data warga dan KK (F-004) | Selesai sesuai izin | Tambah/ubah keluarga dan warga; arsipkan bila tidak memiliki data terkait |
| Tagihan (F-009) | Selesai sesuai izin | Tambah/ubah dan batalkan tagihan; tagihan dengan riwayat pembayaran dilindungi |
| Pembayaran (F-010, F-011) | Selesai sesuai izin | Pengurus berwenang dapat menyetujui/menolak pembayaran |
| Pengumuman (F-017) | Selesai sesuai izin | Tambah/ubah/arsipkan; cakupan publikasi mengikuti wilayah dan izin |
| Kegiatan | Selesai sesuai izin | Tambah/ubah/arsipkan kegiatan pada organisasi/wilayah yang diizinkan |
| Laporan (F-016) | Selesai | Ekspor XLSX dan cetak melalui dialog sistem; PDF tersedia bila OS menyediakan printer PDF |
| Tema terang/gelap (F-027) | Selesai | |
| Mutasi, kategori iuran, kas, survei, pengaduan, kontak darurat, pengguna, wilayah, pengaturan, audit log, backup | Baca-saja | Daftar hanya ditampilkan jika izin resource tersedia |
| Verifikasi akun (F-002) | Selesai | Pengurus yang berhak dapat meninjau dan menyetujui pendaftaran |
| Pengajuan surat (F-006) | Selesai | Warga dengan izin dan profil aktif dapat mengajukan surat |
| Verifikasi surat (F-007) | Selesai | Pengurus dengan izin dapat menyetujui/menolak pengajuan |
| CRUD, verifikasi pembayaran, publikasi/pengubahan data, dan laporan PDF/XLSX | Selesai | Endpoint operasi backend dan aksi desktop tersedia dengan pemeriksaan izin server |

Halaman daftar baru hanya butuh sekitar lima baris kode (lihat README bagian "Menambah halaman baru").

## 5. Hal yang perlu dikonfirmasi ke Project Leader (Dimas)

1. Verifikasi role dan permission pada seed/database serta lakukan uji integrasi dengan backend dan MySQL aktif; desktop hanya memakai izin untuk menampilkan aksi, backend tetap menegakkan akses.
2. **CORS tidak diperlukan** untuk desktop (bukan browser), tetapi **HTTPS** wajib saat rilis (RT-033).
3. Ekspor XLSX dibuat tanpa library tambahan; PDF menggunakan dialog cetak sistem dan bergantung pada printer PDF yang tersedia.

Untuk tim UI/UX (Alam): desain desktop yang tersedia di `documentation/UI-UX` berupa draf tampilan admin lebar. Implementasi desktop mengikuti warna (hijau pinus dengan aksen kuning), sidebar kiri, dan status berwarna. Form tambah/ubah dan halaman detail belum didesain. Perlu desainnya sebelum dikerjakan.

## 6. Mengenai RT-027

Task Tracker menulis RT-027 sebagai "buat wrapper desktop dan konfigurasi build". Karena keputusan arsitektur adalah Java Swing, bukan pembungkus web, RT-027 diartikan sebagai **membangun aplikasi desktop dan konfigurasi build (jar, jpackage)**. Mohon wording tugasnya disesuaikan di Task Tracker.

## 7. Kriteria selesai

- [ ] Keputusan dikonfirmasi Project Leader (dan dosen bila perlu)
- [ ] Kontrak API dicocokkan, asumsi di README diperbarui
- [ ] Login, data warga, tagihan, pembayaran, pengumuman, laporan berjalan terhadap backend sebenarnya
- [ ] `./test.sh` lulus dan uji manual per peran selesai (Umam)
- [ ] `out/rtrw-desktop.jar` berjalan di minimal 2 laptop anggota
- [ ] Paket aplikasi (`jpackage`) dicoba di Windows
- [ ] Dokumentasi teknis dan panduan pemakaian tersedia

## 8. Pembagian kerja (usulan)

| Pekerjaan | Penanggung jawab |
|---|---|
| Klien API, login dan sesi, integrasi backend, paket aplikasi | Taufiq |
| Halaman baru dan form sesuai desain, tema | Fachry |
| Pengujian, dokumentasi teknis, bantuan integrasi | Umam |
