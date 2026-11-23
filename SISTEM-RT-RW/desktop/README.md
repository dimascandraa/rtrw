# Aplikasi Desktop: Sistem Informasi RT/RW

Aplikasi desktop **Java Swing** untuk warga dan pengurus RT/RW. Fitur yang ditampilkan mengikuti izin dari backend; semua data dan aturan bisnis tetap berada di Backend PHP Native, dan hanya backend yang mengakses MySQL.

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

   Tunggu sampai `build.bat` menampilkan `Selesai: out\rtrw-desktop.jar`.    Saat jendela login terbuka, akun demo tersedia:

   - **Alamat server:** `http://127.0.0.1:8000`
   - **Admin RW:** `admin@rtrw.test` / `admin123`
   - **Warga:** `warga@rtrw.test` / `warga123` (termasuk pengajuan surat online)

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
| Login ditolak | Untuk API tiruan, gunakan salah satu akun demo yang tertera di atas. Untuk backend sendiri, akun harus aktif dan memiliki penugasan wilayah. |
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
| Koneksi | `GET /api/v1/health` | Memeriksa konektivitas API dan database | (internal) |
| Login dan sesi | `POST /api/v1/auth/login`, `GET /api/v1/auth/me`, `POST /api/v1/auth/logout` | Masuk dengan token native dan cookie sesi, memuat profil, lalu mencabut sesi saat keluar | (akses) |
| Beranda dan navigasi modul | - | Beranda hanya menampilkan modul/fitur yang sesuai izin akun; setiap modul memiliki halaman katalog | Navigasi UI/UX |
| Dashboard statistik | `GET /api/v1/dashboard/summary` | Statistik warga dan tagihan dari ringkasan backend | F-015 (versi ringkas) |
| Pengumuman | `GET /api/v1/announcements?limit=50` | Daftar pengumuman sesuai wilayah dan hak akses pengguna | F-017 |
| Notifikasi | `GET /api/v1/notifications?limit=50` | Daftar notifikasi pengguna dan jumlah yang belum dibaca | Komunikasi |
| Data operasional | `GET /api/v1/resources/{resource}` | Membaca daftar resource sesuai peran dan wilayah akun, termasuk warga, tagihan, pembayaran, kegiatan, pengaduan, serta pengaturan | Beragam |
| Registrasi akun | `POST /api/v1/auth/register` | Formulir publik pada halaman login mengirim pendaftaran akun warga untuk ditinjau pengurus | Akses |
| Persetujuan pendaftaran | `GET /api/v1/registrations/pending`, `POST /api/v1/registrations/{id}/approve` | Melihat dan menyetujui pendaftaran yang dapat ditinjau akun | Akses |
| Pengajuan surat online | `GET /api/v1/resources/jenis-surat`, `POST /api/v1/letters` | Warga aktif memilih jenis surat dan mengirim keperluan | Administrasi |
| Persetujuan surat | `GET /api/v1/resources/pengajuan-surat`, `POST /api/v1/letters/{id}/decision` | Meninjau pengajuan dan mengirim keputusan setuju/tolak beserta catatan | Administrasi |
| Profil dan keamanan | `PATCH /api/v1/auth/profile`, `POST /api/v1/auth/password` | Memperbarui profil sendiri atau mengganti kata sandi dengan verifikasi kata sandi saat ini | Akses |
| Semua tabel | - | Cari, pagination server, urutkan kolom, **Ekspor CSV** halaman aktif, **Cetak**, detail baris (klik dua kali) | F-016 |
| Tema | - | Mode terang/gelap, pilihan diingat | F-027 |

Status berwarna mengikuti desain UI/UX: hijau (selesai), kuning (menunggu), merah (ditolak/menunggak). Sesi yang berakhir (HTTP 401) otomatis kembali ke halaman login.

Menu dan kartu katalog difilter menggunakan `user.permissions` dan `roleCode` dari backend; backend tetap memeriksa setiap permintaan. Desktop menyediakan registrasi publik, pengajuan surat untuk Warga, persetujuan pendaftaran/surat dan pembayaran untuk pengurus, pengelolaan keluarga/warga/tagihan/pengumuman/kegiatan sesuai izin, ekspor laporan XLSX, serta cetak laporan melalui dialog sistem (termasuk printer PDF bila tersedia). Backend tetap menjadi sumber kebenaran untuk validasi, wilayah, dan izin.

## Kontrak API desktop

Desktop memakai prefix `/api/v1`. Login mengirim `client: "desktop"` dan menyimpan `accessToken` sebagai `Authorization: Bearer ...`; cookie sesi PHP juga disimpan otomatis. Endpoint selain health, login, dan registrasi memerlukan sesi yang sesuai. Backend membatasi resource berdasarkan peran dan wilayah, dan menjadi sumber kebenaran untuk seluruh izin.

Respons sukses menggunakan pembungkus `data`; daftar ditampilkan dari `data.items`. Body profil menggunakan `name`, `email`, dan `phone`; kata sandi menggunakan `currentPassword` dan `newPassword`; pengajuan surat menggunakan `jenisSuratId` dan `keperluan`; keputusan surat menggunakan `keputusan` (`setuju`/`tolak`) dan `catatan`. Daftar endpoint lengkap ada di [`../documentation/API/README.md`](../documentation/API/README.md). HTTP 401 mengarahkan pengguna kembali ke login, sedangkan HTTP 403 ditampilkan sebagai penolakan akses dari backend.

## Struktur

```
desktop/
├── src/main/java/id/rtrw/desktop/
│   ├── Main.java
│   ├── api/       Json, ApiClient, ApiData, ApiException
│   ├── config/    AppConfig (config.properties)
│   ├── ui/        LoginFrame, MainFrame, DashboardPanel, DataTablePanel,
│   │              RegistrationApprovalPanel, LetterDecisionPanel,
│   │              AccountRegistrationPanel, AccountSettingsPanel, Theme, FlatButton, UiKit
│   └── util/      CsvExporter
├── src/dev/java/…/dev/MockServer.java     API tiruan (hanya untuk pengembangan)
├── src/test/java/…/TestRunner.java        pengujian tanpa framework
├── build.sh / run.sh / test.sh / mock-server.sh   (+ versi .bat)
├── config.properties.example
└── docs/RT-026-kebutuhan-desktop.md
```

## Menambah halaman baru

Halaman daftar resource baru dapat memakai klien dan tabel generik yang ada di `ui/MainFrame.java`:

1. Tambahkan menu di blok `static`: `MENU.put("kas", "Kas");`
2. Tambahkan `case` di `page(...)`:

```java
case "kas":
    page = new DataTablePanel(api, "Kas", "Kas masuk dan pengeluaran.",
            () -> api.resource("transaksi-kas"), this);
    break;
```

Kolom tabel, pencarian, ekspor CSV, cetak, dan penanganan sesi sudah otomatis. Tambahkan aksi tulis hanya jika endpoint tersedia di kontrak backend; tangani error HTTP tanpa menganggap penolakan akses sebagai daftar kosong.

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
