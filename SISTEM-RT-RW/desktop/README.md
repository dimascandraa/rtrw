# Desktop - Sistem Informasi RT/RW (Java Swing)

Aplikasi desktop yang memanggil Backend REST API (PHP). Tidak mengakses MySQL langsung.

## Prasyarat
- JDK 11 atau lebih baru (`javac -version`)
- Tanpa library eksternal (JSON memakai `util/Json.java`)

## Menjalankan
1. Salin `config.example.properties` menjadi `config.properties`
2. Windows: `run.bat`. Linux/macOS: `./run.sh` (jalankan dari folder `desktop/`)

Mode mock (`mock.mode=true`) tidak butuh backend. Akun uji: `admin/admin123`, `rt/rt123`, `rw/rw123`.
Setelah backend siap: ubah `mock.mode=false` dan isi `api.base.url`.

## Struktur
| Folder | Isi |
|---|---|
| `app/` | Main, AppConfig |
| `api/` | ApiClient (HTTP), AuthApi, dan nanti ResidentApi, InvoiceApi, dst |
| `model/` | Kelas data (User, nanti Resident, Invoice, ...) |
| `ui/` | Jendela dan panel (LoginFrame, MainFrame, panel per modul) |
| `util/` | SessionManager, Json |

## Aturan
- Panggilan API wajib lewat `SwingWorker` (lihat `LoginFrame.doLogin`).
- Setiap endpoint baru dibuat sebagai kelas di `api/` yang memakai `ApiClient`.
- Jangan commit `config.properties`.
- Branch: `feature/desktop-<nama-fitur>`, Pull Request ke `develop`.

## Asumsi yang perlu dicek ke tim Backend
- `POST /api/login` menerima `{"username","password"}` dan membalas `{"token","user":{"id","name","role"}}`
- Header autentikasi: `Authorization: Bearer <token>`
- Pesan error ada di field `message`
