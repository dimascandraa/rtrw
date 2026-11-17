# Aplikasi Desktop: Sistem Informasi RT/RW

Aplikasi desktop **Java Swing** untuk pengurus RT/RW (Bendahara, Ketua RT/RW, Admin). Aplikasi ini hanya **klien REST API**: semua data dan aturan bisnis ada di Backend PHP Native, dan hanya backend yang mengakses MySQL.

```
Web (HTML, CSS, JS) ─┐
Mobile (Android Java) ┼── Backend REST API (PHP Native) ── MySQL
Desktop (Java Swing) ─┘                                       <- folder ini
```

Sesuai ketentuan repo: **tanpa framework dan tanpa library tambahan**. Hanya JDK (Swing, `java.net.http`). Parser JSON ditulis sendiri di `api/Json.java`. Jika kelompok ingin memakai library (misalnya FlatLaf), tunggu persetujuan tim dan dosen dulu.

Dokumen keputusan RT-026: [`docs/RT-026-kebutuhan-desktop.md`](docs/RT-026-kebutuhan-desktop.md)

## Persyaratan

- **JDK 11 atau lebih baru** (kode dikompilasi dengan `--release 11`, sudah dicoba di JDK 21)
- Backend API berjalan, **atau** pakai API tiruan di bawah ini

## Cara menjalankan di Windows

1. Pastikan JDK 11 atau lebih baru terpasang. Buka PowerShell atau Command Prompt dan cek:

   ```powershell
   java -version
   javac -version
   ```

   Keduanya harus menampilkan versi 11 atau lebih baru. Jika perintah tidak ditemukan, pasang JDK dan tambahkan folder `bin` JDK ke `PATH`.

2. Buka terminal pertama, lalu jalankan API tiruan dari folder `desktop`:

   ```powershell
   cd C:\TicTacToe\rtrw\SISTEM-RT-RW\desktop
   .\mock-server.bat
   ```

   API tiruan berjalan di `http://127.0.0.1:8000` dan menyediakan data dummy. Biarkan terminal ini tetap terbuka.

3. Buka terminal kedua pada folder yang sama dan jalankan aplikasi:

   ```powershell
   cd C:\TicTacToe\rtrw\SISTEM-RT-RW\desktop
   .\run.bat
   ```

   Pertama kali dijalankan, `run.bat` akan membangun aplikasi secara otomatis. Pada layar login, gunakan alamat server `http://127.0.0.1:8000`, akun `admin@rtrw.test`, dan kata sandi `admin123`.

4. Untuk menghentikan API tiruan, tekan `Ctrl+C` di terminal pertama. Tutup jendela aplikasi seperti biasa.

Jika sudah memiliki backend yang berjalan, lewati langkah menjalankan API tiruan. Jalankan aplikasi dengan alamat backend:

```powershell
.\run.bat --url=http://localhost:8000
```

Ganti URL dengan alamat backend Anda. URL juga dapat dimasukkan pada kolom **Alamat server** di halaman login; alamat tersebut akan disimpan untuk penggunaan berikutnya.

## Mulai cepat (tanpa backend)

```bash
./mock-server.sh      # terminal 1: API tiruan di http://127.0.0.1:8000 (data dummy)
./run.sh              # terminal 2: jalankan aplikasi (build otomatis bila perlu)
```

Pada Windows, ikuti bagian [Cara menjalankan di Windows](#cara-menjalankan-di-windows). Akun uji: `admin@rtrw.test` / `admin123` (peran Bendahara). Akun `warga@rtrw.test` / `warga123` sengaja ditolak karena warga memakai aplikasi Mobile.

## Menghubungkan ke backend sebenarnya

1. Isi **Alamat server** di halaman login (misalnya `http://localhost:8000`). Alamat tersimpan otomatis ke `config.properties`.
2. Atau salin `config.properties.example` menjadi `config.properties` dan ubah isinya. Argumen `--url=...` juga bisa dipakai: `./run.sh --url=https://rtrw.example.com` (Windows: `.\run.bat --url=https://rtrw.example.com`).

`config.properties` tidak ikut di-commit. Jangan simpan password atau token di sana. Token hanya disimpan di memori selama aplikasi berjalan.

## Perintah

| Perintah | Fungsi |
|---|---|
| `./build.sh` | Kompilasi dan buat `out/rtrw-desktop.jar` |
| `./run.sh` | Jalankan aplikasi |
| `./test.sh` | Jalankan 27 pengujian otomatis (JSON, parser respons, klien API vs API tiruan) |
| `./mock-server.sh [port]` | Jalankan API tiruan |

## Fitur saat ini

| Menu | Endpoint | Fungsi | Fitur di Daftar Fitur |
|---|---|---|---|
| Login | `POST /api/login` | Masuk, token dan cookie sesi dibaca otomatis | (akses) |
| Beranda dan navigasi modul | - | Beranda menampilkan 11 modul dan 26 kartu fitur sesuai rancangan UI/UX; setiap modul punya halaman katalog sendiri | Navigasi UI/UX |
| Dashboard statistik | `GET /api/residents`, `GET /api/invoices` | Jumlah warga, tagihan lunas/belum, persentase pelunasan | F-015 (versi ringkas) |
| Data warga | `GET`, `POST /api/residents` | Daftar, cari, tambah warga | F-004 |
| Tagihan | `GET /api/invoices` | Daftar tagihan dan status | F-009 |
| Pembayaran | `GET /api/payments`, `POST /api/payments/verify` | Daftar dan verifikasi (terima/tolak) oleh bendahara | F-010, F-011 |
| Pengumuman | `GET /api/announcements` | Daftar pengumuman | F-017 |
| Laporan | `GET /api/reports` | Daftar laporan | F-016 |
| Semua tabel | - | Cari, urutkan kolom, **Ekspor CSV** (terbaca di Excel), **Cetak**, detail baris (klik dua kali) | F-016 |
| Tema | - | Mode terang/gelap, pilihan diingat | F-027 |

Status berwarna mengikuti desain UI/UX: hijau (selesai), kuning (menunggu), merah (ditolak/menunggak). Sesi yang berakhir (HTTP 401) otomatis kembali ke halaman login.

Menu dan kartu katalog tersedia untuk semua 11 modul pada rancangan UI/UX. Halaman operasional yang belum memiliki endpoint desktop menampilkan pemberitahuan bahwa fitur menunggu integrasi; data tiruan tidak ditampilkan. Fitur tersebut meliputi mutasi warga, surat, kas, kategori iuran, audit log, sesi/perangkat, multi-RT/RW, backup, notifikasi, kegiatan, pengaduan, kontak darurat, dan survei. Ekspor PDF dan XLSX asli membutuhkan library, jadi sementara memakai CSV dan cetak. Ini perlu persetujuan tim dan dosen bila ingin ditambah.

## Asumsi kontrak API (WAJIB dicocokkan dengan Dimas)

`documentation/API/` belum saya lihat, jadi bagian berikut adalah **asumsi**. Semuanya terpusat sehingga mudah diubah.

| Asumsi | Lokasi perubahan |
|---|---|
| Respons berbentuk `{"success":true,"message":"...","data":[...]}`. Daftar boleh di akar, di `data`, atau di `data.items`. Kegagalan: `{"success":false,"message":"..."}` atau HTTP 4xx/5xx | `api/ApiData.java` |
| Login: body `{"email": "...", "password": "..."}` (nama kolom di `api.login.field`). Respons memuat `token` dan `user` (di akar atau di `data`) | `ApiClient.login`, `config.properties` |
| Token dikirim sebagai `Authorization: Bearer <token>`. Cookie sesi PHP juga disimpan otomatis | `api/ApiClient.java` |
| Peran dibaca dari `user.peran` / `role` / `nama_peran`; peran `warga` ditolak | `ApiClient.userRole`, `desktop.blocked.roles` |
| `GET /api/payments` ada (daftar pembayaran); `GET /api/announcements` dan `GET /api/reports` mengembalikan daftar | `ui/MainFrame.java` |
| Verifikasi: `POST /api/payments/verify` body `{"id":..., "status":"diterima"\|"ditolak", "catatan":"..."}` | `ui/PaymentsPanel.java` |
| Tambah warga: body `{nik, nama, no_kk, alamat, telepon}` | `FORM_FIELDS` di `ui/ResidentsPanel.java` |
| Status tagihan lunas = kolom berisi kata "status" yang memuat "lunas" tanpa "belum" | `DashboardPanel.isPaid` |
| Kolom tabel dibuat otomatis dari kunci JSON; kunci berisi `password`, `token`, `hash`, `secret` disembunyikan | `DataTablePanel` |

## Struktur

```
desktop/
├── src/main/java/id/rtrw/desktop/
│   ├── Main.java
│   ├── api/       Json, ApiClient, ApiData, ApiException
│   ├── config/    AppConfig (config.properties)
│   ├── ui/        LoginFrame, MainFrame, DashboardPanel, DataTablePanel,
│   │              ResidentsPanel, PaymentsPanel, Theme, FlatButton, UiKit
│   └── util/      CsvExporter
├── src/dev/java/…/dev/MockServer.java     API tiruan (hanya untuk pengembangan)
├── src/test/java/…/TestRunner.java        pengujian tanpa framework
├── build.sh / run.sh / test.sh / mock-server.sh   (+ versi .bat)
├── config.properties.example
└── docs/RT-026-kebutuhan-desktop.md
```

## Menambah halaman baru

Halaman daftar baru cukup dua baris di `ui/MainFrame.java`:

1. Tambahkan menu di blok `static`: `MENU.put("kas", "Kas");`
2. Tambahkan `case` di `page(...)`:

```java
case "kas":
    page = new DataTablePanel(api, "Kas", "Kas masuk dan pengeluaran.", "/api/cash", this);
    break;
```

Kolom tabel, pencarian, ekspor CSV, cetak, dan penanganan sesi sudah otomatis. Untuk aksi khusus (tambah/ubah/verifikasi), turunkan `DataTablePanel` seperti `ResidentsPanel` dan `PaymentsPanel`, lalu tambahkan tombol dengan `addAction(...)` dan kirim data dengan `postAsync(...)`.

## Membuat paket aplikasi (RT-027)

Jar siap pakai di mesin yang punya Java: `java -jar out/rtrw-desktop.jar`.

Untuk paket yang bisa diinstal tanpa Java terpisah, JDK 14+ menyediakan `jpackage` (belum dicoba di proyek ini):

```bash
./build.sh
jpackage --type app-image --name "Aplikasi RT RW" --input out --main-jar rtrw-desktop.jar --main-class id.rtrw.desktop.Main
```

Untuk installer `.exe`/`.msi` di Windows, `jpackage` membutuhkan WiX Toolset. Jalankan di Windows (`--type exe`).

## Keamanan

- Tidak ada kredensial di kode atau di repo. Token hanya di memori dan dihapus saat keluar.
- Kolom sensitif (`password`, `token`, dst.) tidak ditampilkan di tabel.
- CSV melindungi dari rumus berbahaya di Excel (sel yang diawali `=`, `+`, `-`, `@` diberi tanda petik).
- Gunakan **HTTPS** untuk server produksi (RT-033). Dengan `http://`, kata sandi dan token terkirim tanpa enkripsi.
- Hak akses sebenarnya diperiksa backend. Desktop hanya menampilkan pesan bila server menolak (HTTP 403).
- Gunakan data dummy, bukan data warga asli, untuk pengembangan dan demo.

## Alur kerja Git (sesuai kesepakatan tim)

Clone, `git switch develop && git pull origin develop`, buat branch fitur, uji, commit, push, Pull Request ke `develop`, minimal satu reviewer, merge. Jangan kerja langsung di `main` atau `develop`, dan jangan `git push --force`.

Contoh branch: `feature/desktop-login`, `feature/desktop-dashboard`, `feature/desktop-payments`, `fix/desktop-csv-export`.
Contoh commit: `feat(desktop): tambah verifikasi pembayaran`.

## Pembagian tugas tim desktop (usulan)

| Anggota | Fokus |
|---|---|
| Taufiq (Leader) | Klien API, login dan sesi, integrasi dengan backend, paket aplikasi, review PR |
| Fachry | Antarmuka halaman sesuai desain UI/UX: form tambah/ubah, halaman baru, tema |
| Umam | Pengujian (`TestRunner`, uji manual per peran), dokumentasi teknis, bantuan integrasi |
