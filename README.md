# SISTEM INFORMASI RT/RW

Platform Sistem Informasi RT/RW dengan tiga aplikasi: Web, Mobile, dan Desktop, menggunakan satu Backend REST API dan satu database MySQL.

## 1. Arsitektur Proyek

```text
Web (HTML, CSS, JavaScript) ─┐
Mobile (Android Java) ───────┼── Backend REST API (PHP Native) ── MySQL
Desktop (Java Swing) ────────┘
```

- Backend: PHP Native
- Web: HTML, CSS, JavaScript
- Mobile: Android Native Java
- Desktop: Java Swing
- Database: MySQL
- Komunikasi: REST API dengan JSON

<<<<<<< HEAD
=======
**Ketentuan:** Tidak menggunakan framework. Library diperbolehkan sesuai persetujuan tim dan dosen.
>>>>>>> 9fe82a518bd566df9b821f2aa3578b7c2ce07d4b

## 2. Struktur Repository

```text
SISTEM-RT-RW/
├── backend/
├── web/
├── mobile/
├── desktop/
├── database/
│   ├── schema.sql
│   ├── seed.sql
│   └── migrations/
├── documentation/
│   ├── ERD/
│   ├── API/
│   ├── UI-UX/
│   └── laporan/
└── README.md
```

## 3. Anggota Tim

| Nama | Tanggung Jawab |
|---|---|
| Dimas | Koordinator proyek, Backend |
| Rida' | Backend dan integrasi |
| Fachry | Web |
| Aulia | Mobile |
| Fauzan | Mobile |
| Nando | UI/UX dan testing |
| Linda | Database |
| Alam | UI/UX |
| Umam | Desktop |
| Dias | Web dan testing |
| Taufik | Desktop |

## 4. Cara Menjalankan Aplikasi

Jalankan perintah dari folder `SISTEM-RT-RW` (folder induk aplikasi), bukan dari folder repository paling luar.

### Persiapan backend dan database

Persyaratan:

- PHP 8.0 atau lebih baru dengan ekstensi `pdo_mysql`.
- MySQL atau MariaDB.
- Java 17 atau lebih baru untuk Desktop.
- Android Studio untuk Mobile.

1. Buat database dan pengguna MySQL.
2. Salin file konfigurasi backend:

   ```powershell
   Copy-Item backend\.env.example backend\.env
   ```

3. Sesuaikan `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, dan `DB_PASSWORD` pada `backend\.env`.
4. Impor `database\migrations\schema.sql`, lalu `database\migrations\seed.sql` melalui MySQL CLI, phpMyAdmin, atau MySQL Workbench.
5. Aktifkan ekstensi `pdo_mysql` pada konfigurasi PHP jika belum aktif.

Akun contoh setelah seed:

```text
Email: adminrw@contoh.test
Kata sandi: Password123
```

### Menjalankan Web

```powershell
cd D:\Dimas\Kuliah\Semester 5\Pemrograman Berbasis Platform\rtrw\SISTEM-RT-RW
php -S 127.0.0.1:8000 -t web backend\public\router.php
```

Buka <http://127.0.0.1:8000/>. Jangan membuka `web\index.html` langsung melalui `file://` karena halaman memerlukan sesi dan panggilan API melalui backend.

Berhenti menjalankan server dengan menekan `Ctrl+C` pada terminal.

### Menjalankan Desktop

Pastikan backend sudah berjalan di `http://127.0.0.1:8000`, lalu jalankan JAR yang sudah tersedia:

```powershell
cd D:\Dimas\Kuliah\Semester 5\Pemrograman Berbasis Platform\rtrw\SISTEM-RT-RW\desktop
java -jar out\rtrw-desktop.jar
```

Alamat backend Desktop diatur pada `desktop\config.properties`:

```properties
api.base.url=http://127.0.0.1:8000
```

Folder `desktop\out` berisi hasil build Desktop yang siap dijalankan. Folder `desktop\src` belum tersedia di repository saat ini; JAR dapat dijalankan bila Java sudah terpasang, tetapi pengembangan ulang atau build dari source memerlukan source Java dan skrip build yang lengkap.

### Menjalankan Mobile

Saat ini folder `mobile` masih belum berisi proyek Android dan belum dapat dijalankan sebagai aplikasi.

Setelah proyek Android Native Java ditambahkan, langkah umumnya:

1. Buka Android Studio.
2. Pilih **Open** dan pilih folder `SISTEM-RT-RW\mobile`.
3. Tunggu proses sinkronisasi Gradle selesai.
4. Aktifkan emulator Android atau sambungkan perangkat melalui USB.
5. Pilih konfigurasi aplikasi, lalu tekan **Run**.

Atur alamat backend menjadi `http://10.0.2.2:8000` untuk Emulator Android. Gunakan alamat IP LAN komputer untuk perangkat fisik, misalnya `http://192.168.1.10:8000`, dan jalankan PHP agar dapat diakses dari jaringan lokal:

```powershell
php -S 0.0.0.0:8000 -t web backend\public\router.php
```

## 5. Penggunaan GitHub Bersama

Gunakan satu repository utama bernama `SISTEM-RT-RW`.

Repository disarankan menggunakan pengaturan **Private** selama proyek masih dikembangkan. Undang seluruh anggota sebagai collaborator.

### Strategi Branch

```text
main
└── develop
    ├── feature/backend-login
    ├── feature/backend-resident-api
    ├── feature/web-dashboard
    ├── feature/mobile-login
    ├── feature/desktop-dashboard
    └── fix/payment-validation
```

Ketentuan branch:

- `main`: versi stabil dan siap rilis.
- `develop`: gabungan fitur yang sedang dikembangkan dan diuji.
- `feature/nama-fitur`: pengembangan fitur baru.
- `fix/nama-perbaikan`: perbaikan bug.

Jangan mengerjakan fitur langsung di branch `main` atau `develop`.

### Alur Kerja Anggota

1. Clone repository.
2. Ambil perubahan terbaru dari `develop`.
3. Buat branch fitur masing-masing.
4. Kerjakan fitur.
5. Uji aplikasi.
6. Commit perubahan.
7. Push branch ke GitHub.
8. Buat Pull Request menuju `develop`.
9. Minta anggota lain melakukan review.
10. Merge setelah disetujui.

### Perintah Git

**A. Clone repository**

```bash
git clone https://github.com/USERNAME/SISTEM-RT-RW.git
cd SISTEM-RT-RW
```

**B. Ambil perubahan terbaru**

```bash
git switch develop
git pull origin develop
```

**C. Buat branch fitur**

```bash
git switch -c feature/backend-login
```

**D. Commit pekerjaan**

```bash
git add .
git commit -m "feat(backend): add login API"
```

**E. Push ke GitHub**

```bash
git push -u origin feature/backend-login
```

Selanjutnya buka GitHub dan buat Pull Request menuju branch `develop`.

**F. Setelah Pull Request di-merge**

```bash
git switch develop
git pull origin develop
```

### Standar Commit

Gunakan format:

`jenis(area): ringkasan perubahan`

| Jenis | Fungsi |
|---|---|
| feat | Menambahkan fitur |
| fix | Memperbaiki bug |
| docs | Mengubah dokumentasi |
| style | Mengubah tampilan |
| refactor | Merapikan kode |
| test | Menambahkan pengujian |

Contoh:

```bash
git commit -m "feat(web): add resident dashboard"
git commit -m "fix(api): validate payment amount"
git commit -m "docs: update database documentation"
```

### Aturan Pull Request

Setiap Pull Request harus menjelaskan:

- Tujuan perubahan.
- Fitur yang dikerjakan.
- Folder atau file yang diubah.
- Cara menguji fitur.
- Screenshot jika ada perubahan UI.
- Perubahan database atau API jika ada.

Minimal satu anggota lain melakukan review sebelum merge.

## 6. Mengatasi Merge Conflict

Jika terjadi konflik:

1. Jangan langsung menghapus perubahan anggota lain.
2. Ambil perubahan terbaru dari `develop`.
3. Gabungkan perubahan tersebut ke branch fitur.
4. Selesaikan konflik bersama pemilik kode.
5. Jalankan pengujian ulang.
6. Commit dan push hasilnya.

Contoh:

```bash
git switch feature/backend-login
git fetch origin
git merge origin/develop
```

Setelah konflik diselesaikan:

```bash
git add .
git commit -m "fix: resolve merge conflict"
git push
```

Jangan menggunakan `git push --force` pada branch bersama atau branch utama.

## 7. File yang Tidak Boleh Diunggah

Buat `.gitignore` pada folder utama.

```gitignore
.env
.env.*
!.env.example
*.pem
*.key
*.log

.vscode/
.idea/
.DS_Store
Thumbs.db

vendor/
.gradle/
**/build/
local.properties
*.class
out/
target/
```

Jangan mengunggah:

- Password database.
- Token API.
- Private key.
- Data pribadi warga asli.
- Backup database produksi.
- File konfigurasi yang berisi kredensial.

Gunakan data dummy untuk pengembangan dan demo.

## 8. Aturan Integrasi Aplikasi

1. Semua aplikasi menggunakan satu Backend API.
2. Backend menjadi satu-satunya pihak yang mengakses MySQL.
3. Web, Mobile, dan Desktop berkomunikasi menggunakan JSON.
4. Perubahan API wajib diinformasikan kepada anggota lain.
5. Perubahan struktur database harus dikoordinasikan dengan PIC Database dan Backend.
6. Dokumentasikan endpoint di `documentation/API/`.

Contoh endpoint:

```text
POST /api/login
GET  /api/residents
POST /api/residents
GET  /api/invoices
POST /api/payments
POST /api/payments/verify
GET  /api/announcements
GET  /api/reports
```

## 9. Kesepakatan Kerja Tim

- Satu repository untuk seluruh aplikasi.
- Setiap anggota memiliki tanggung jawab utama.
- Komunikasikan perubahan pada file yang dikerjakan bersama.
- Pull perubahan terbaru sebelum mulai bekerja.
- Jangan langsung merge ke `main`.
- Lakukan review kode sebelum merge.
- Gunakan data dummy, bukan data pribadi warga asli.
- Dokumentasikan perubahan fitur, API, dan database.

**Tujuan utama:** menjaga agar seluruh anggota dapat bekerja secara bersamaan tanpa merusak pekerjaan anggota lainnya, serta memastikan aplikasi Web, Mobile, dan Desktop terintegrasi dalam satu platform.

## 10. Fitur Web yang Tersedia

- Warga dapat mendaftar; akun baru menunggu persetujuan admin, Ketua RW, atau Ketua RT dan hanya diaktifkan dengan peran Warga.
- Warga dapat mengajukan surat dan melihat status pengajuannya. Pengurus dapat memproses surat sesuai wilayah.
- Menu **Akun** menyediakan perubahan profil dan kata sandi. Perubahan peran tetap dikelola pengurus.
- Fitur **Laporan PDF/Excel** mengunduh workbook Excel untuk kas, tagihan, dan pembayaran; pilihan PDF menggunakan dialog cetak browser (**Simpan sebagai PDF**).

Detail endpoint dan validasi tersedia di `SISTEM-RT-RW\documentation\API\README.md`. Registrasi perlu memakai nomor telepon yang tercatat pada data warga agar pengurus dapat menghubungkan serta menyetujui akun.