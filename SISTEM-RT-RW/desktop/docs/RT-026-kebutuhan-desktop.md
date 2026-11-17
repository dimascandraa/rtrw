# RT-026: Penentuan Kebutuhan Desktop

| | |
|---|---|
| **Proyek** | Sistem Informasi RT/RW |
| **Tugas** | RT-026: Tentukan kebutuhan desktop (aplikasi admin / cukup web responsif) |
| **PIC** | Taufiq (Leader), Fachry, Umam |
| **Deadline** | 26 November 2026 |
| **Status** | Draf untuk dikonfirmasi Project Leader (Dimas) |

## 1. Keputusan

**Desktop dibuat sebagai aplikasi admin untuk pengurus (Bendahara, Ketua RT/RW, Admin), berupa aplikasi Java Swing yang menjadi klien REST API.**

Dasarnya adalah arsitektur yang sudah disepakati tim:

- Desktop memakai **Java Swing**, berkomunikasi lewat **REST API (JSON)** ke Backend PHP Native, tanpa framework.
- Hanya backend yang mengakses MySQL. Desktop **tidak punya database atau logika bisnis sendiri**.
- Warga dilayani Mobile dan Web. Desktop tidak untuk warga.

## 2. Mengapa aplikasi admin, bukan "cukup web responsif"

| Pertimbangan | Penjelasan |
|---|---|
| Kebutuhan platform | Tujuan tugas besar adalah memahami aplikasi multiplatform. Arsitektur tim sudah menetapkan tiga aplikasi: Web, Mobile, Desktop |
| Pekerjaan pengurus | Bendahara dan pengurus bekerja di laptop/PC dengan tabel lebar: verifikasi pembayaran, rekap tagihan, data warga, laporan |
| Nilai tambah desktop | Tabel yang bisa dicari dan diurutkan, ekspor CSV untuk Excel, cetak langsung, jendela sendiri tanpa browser |
| Risiko | Kecil: tidak ada aturan bisnis yang ditulis dua kali, semua lewat API yang sama |

Jika Project Leader atau dosen menilai desktop tidak diperlukan, fallback-nya adalah web responsif untuk pengurus, dan RT-027 ditandai tidak diperlukan.

## 3. Pengguna dan peran

Peran menurut Daftar Fitur F-003: Super Admin, Admin Perumahan, Ketua RT/RW, Bendahara, Pengurus, Warga.

| Peran | Memakai desktop? | Keterangan |
|---|---|---|
| Bendahara | Ya | Verifikasi pembayaran, tagihan, kas, laporan |
| Ketua RT/RW, Pengurus | Ya | Data warga, pengumuman, laporan |
| Admin Perumahan, Super Admin | Ya | Pengelolaan data dan platform |
| Warga | Tidak | Memakai Mobile. Desktop menolak login peran ini (dapat diubah lewat `desktop.blocked.roles`) |

## 4. Cakupan fitur desktop

Dipetakan dari Daftar Fitur (F-001 sampai F-027). Status: **Selesai** (sudah ada di aplikasi), **Tahap berikut** (rencana), **Di luar desktop**.

| Fitur | Status | Catatan |
|---|---|---|
| Login, sesi | Selesai | `POST /api/login`, kembali ke login bila sesi berakhir |
| Dashboard statistik (F-015) | Selesai (ringkas) | Jumlah warga, tagihan lunas/belum, persentase. Grafik interaktif tahap berikut |
| Data warga dan KK (F-004) | Selesai | Daftar, cari, tambah |
| Tagihan (F-009) | Selesai | Daftar dan status berwarna |
| Verifikasi bukti pembayaran (F-010, F-011) | Selesai | Terima/tolak dengan catatan |
| Pengumuman (F-017) | Selesai | Daftar. Buat pengumuman tahap berikut |
| Laporan (F-016) | Selesai | Daftar, ekspor CSV, cetak. PDF dan XLSX asli perlu library |
| Tema terang/gelap (F-027) | Selesai | |
| Mutasi warga (F-005) | Tahap berikut | Cukup satu halaman daftar |
| Verifikasi surat (F-007) | Tahap berikut | Daftar pengajuan, setujui/tolak |
| Aturan iuran (F-008), kas (F-013), riwayat dan kuitansi (F-012) | Tahap berikut | Halaman daftar, lalu form |
| Verifikasi akun (F-002), peran (F-003) | Tahap berikut | |
| Audit log (F-023), sesi (F-024), multi-RT/RW (F-025), backup (F-026) | Tahap berikut | Untuk Admin dan Super Admin |
| Registrasi warga (F-001), pengajuan surat (F-006), survei (F-022), kegiatan (F-019), kontak darurat (F-021), pengingat (F-014) | Di luar desktop | Sisi warga, dilayani Mobile |

Halaman daftar baru hanya butuh sekitar lima baris kode (lihat README bagian "Menambah halaman baru").

## 5. Hal yang perlu dikonfirmasi ke Project Leader (Dimas)

1. **Kontrak API** di `documentation/API/`: bentuk respons, cara login (token Bearer atau session cookie), nama kolom body login, nama endpoint selain contoh (`GET /api/payments`, dst.). Daftar asumsi desktop ada di README.
2. **Peran dan wilayah** dalam respons login (`user.peran`), agar desktop dapat menampilkan peran dan menolak warga.
3. **Form tambah warga**: daftar kolom yang diterima `POST /api/residents`.
4. **CORS tidak diperlukan** untuk desktop (bukan browser), tetapi **HTTPS** wajib saat rilis (RT-033).
5. **Persetujuan library** bila ingin PDF/XLSX asli atau tema FlatLaf. Sementara aplikasi hanya memakai JDK.

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
