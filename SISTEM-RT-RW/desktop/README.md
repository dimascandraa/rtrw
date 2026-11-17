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

- **JDK 11 atau lebih baru** (JDK lengkap, termasuk `javac` dan `jar`; sudah diuji dengan JDK 23).
- Backend API yang sudah berjalan, atau API tiruan lokal untuk mencoba aplikasi tanpa backend/MySQL.

## Menjalankan di Windows (PowerShell)

Cara ini menjalankan aplikasi beserta API tiruan berisi data contoh. Tidak perlu menyalakan PHP atau MySQL.

1. Pastikan JDK terpasang. Buka PowerShell dan jalankan:

   ```powershell
   java -version
   javac -version
   jar --version
   ```

   Semua perintah harus dikenali. Versi Java harus 11 atau lebih baru.

2. Jalankan API tiruan di terminal PowerShell pertama:

   ```powershell
   Set-Location -LiteralPath 'C:\TicTacToe\rtrw\SISTEM-RT-RW\desktop'
   .\mock-server.bat
   ```

   Tunggu sampai muncul `Mock API berjalan di http://127.0.0.1:8000`. Biarkan terminal ini tetap terbuka.

   Jika proyek berada di lokasi lain, ganti alamat pada `Set-Location` dengan lokasi folder `desktop` Anda.

3. Buka terminal PowerShell kedua dan jalankan aplikasi:

   ```powershell
   Set-Location -LiteralPath 'C:\TicTacToe\rtrw\SISTEM-RT-RW\desktop'
   .\build.bat
   .\run.bat
   ```

   Tunggu sampai `build.bat` menampilkan `Selesai: out\rtrw-desktop.jar`. Saat jendela login terbuka, isi:

   - **Alamat server:** `http://127.0.0.1:8000`
   - **Email atau telepon:** `admin@rtrw.test`
   - **Kata sandi:** `admin123`

   `run.bat` akan membangun JAR otomatis jika belum ada. Menjalankan `build.bat` secara eksplisit di atas membantu memastikan JAR terbaru berhasil dibuat.

4. Untuk berhenti, tutup aplikasi desktop, lalu tekan `Ctrl+C` pada terminal API tiruan.

API tiruan hanya menggunakan data dummy dalam memori. Perubahan data akan hilang saat server dihentikan.

### Jika tidak menggunakan PowerShell

Di Command Prompt, buka dua jendela dan jalankan perintah berikut pada masing-masing:

```bat
cd /d C:\TicTacToe\rtrw\SISTEM-RT-RW\desktop
mock-server.bat
```

```bat
cd /d C:\TicTacToe\rtrw\SISTEM-RT-RW\desktop
build.bat
run.bat
```

Ganti lokasi folder jika proyek tidak berada di `C:\TicTacToe\rtrw\SISTEM-RT-RW`.

### Menjalankan dengan backend sendiri

Jalankan backend sesuai dokumentasinya, lalu buka aplikasi dari folder `desktop`:

```powershell
.\run.bat --url=http://localhost:8000
```

Ganti URL dengan alamat backend yang benar. Alternatifnya, buka aplikasi tanpa argumen dan isi URL tersebut pada kolom **Alamat server** di halaman login. URL yang digunakan akan disimpan untuk pemakaian berikutnya. Untuk server produksi, gunakan HTTPS.

### Mengatasi masalah saat menjalankan

| Pesan atau masalah | Yang perlu dilakukan |
|---|---|
| VS Code menampilkan `debugjava.ps1 cannot be loaded because running scripts is disabled` | Ini kebijakan PowerShell yang memblokir skrip debugger Java VS Code, bukan error dari aplikasi. Untuk mencoba desktop, jalankan `run.bat` dari Command Prompt sesuai langkah di atas. Jangan mengubah execution policy seluruh komputer tanpa persetujuan administrator. |
| `java`, `javac`, atau `jar` tidak dikenali | Pasang JDK 11+ (bukan hanya JRE), tambahkan folder `bin` JDK ke `PATH`, tutup lalu buka kembali terminal, dan ulangi pemeriksaan versi. |
| Build menampilkan error `javac` | Pastikan terminal berada di folder `desktop`, gunakan JDK 11+, lalu jalankan `.\build.bat` lagi. Baca error `javac` pertama; jika masih gagal, salin seluruh output terminal untuk diperiksa. |
| `Unable to access jarfile` atau `Could not find or load main class` | Build mungkin belum berhasil. Jalankan `.\build.bat` dan pastikan ada pesan `Selesai: out\rtrw-desktop.jar` sebelum menjalankan `.\run.bat`. |
| `Address already in use` saat menjalankan API tiruan | Port 8000 sedang dipakai. Jalankan `.\mock-server.bat 8001`, lalu gunakan alamat `http://127.0.0.1:8001` di halaman login. |
| Aplikasi menampilkan tidak dapat terhubung ke server | Pastikan terminal API tiruan masih terbuka dan alamat/port di halaman login sama dengan alamat yang dicetak oleh server. |
| Login ditolak | Untuk API tiruan, gunakan tepat `admin@rtrw.test` dan `admin123`. Akun `warga@rtrw.test` memang tidak diizinkan masuk ke aplikasi desktop. |
| JAR tidak dapat ditulis karena sedang digunakan | Tutup semua jendela aplikasi desktop yang berjalan, kemudian jalankan ulang `.\build.bat`. |

Jika masih gagal, kirimkan **pesan error lengkap dari terminal** dan sebutkan perintah terakhir yang dijalankan. Jangan kirim kata sandi atau token backend.

## Perintah

| Windows | Linux/macOS | Fungsi |
|---|---|---|
| `build.bat` | `./build.sh` | Kompilasi dan buat `out/rtrw-desktop.jar` |
| `run.bat` | `./run.sh` | Jalankan aplikasi |
| `test.bat` | `./test.sh` | Jalankan pengujian otomatis |
| `mock-server.bat [port]` | `./mock-server.sh [port]` | Jalankan API tiruan (port bawaan 8000) |

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
