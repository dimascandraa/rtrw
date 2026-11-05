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

**Ketentuan:** Tidak menggunakan framework. Library diperbolehkan sesuai persetujuan tim dan dosen.

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

## 4. Penggunaan GitHub Bersama

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

## 5. Mengatasi Merge Conflict

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

## 6. File yang Tidak Boleh Diunggah

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

## 7. Aturan Integrasi Aplikasi

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

## 8. Kesepakatan Kerja Tim

- Satu repository untuk seluruh aplikasi.
- Setiap anggota memiliki tanggung jawab utama.
- Komunikasikan perubahan pada file yang dikerjakan bersama.
- Pull perubahan terbaru sebelum mulai bekerja.
- Jangan langsung merge ke `main`.
- Lakukan review kode sebelum merge.
- Gunakan data dummy, bukan data pribadi warga asli.
- Dokumentasikan perubahan fitur, API, dan database.

**Tujuan utama:** menjaga agar seluruh anggota dapat bekerja secara bersamaan tanpa merusak pekerjaan anggota lainnya, serta memastikan aplikasi Web, Mobile, dan Desktop terintegrasi dalam satu platform.