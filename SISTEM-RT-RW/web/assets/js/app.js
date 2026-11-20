const modules = [
  {
    id: "reports",
    title: "Dashboard & Laporan",
    short: "D",
    description: "Ringkasan data dan laporan lingkungan.",
    tables: ["transaksi_kas", "tagihan", "pembayaran", "warga"],
    features: [
      {
        title: "Dashboard statistik interaktif",
        description: "Grafik saldo kas, tagihan lunas/belum, tunggakan, pemasukan/pengeluaran, dan warga.",
        tables: ["transaksi_kas", "tagihan", "pembayaran", "warga"]
      },
      {
        title: "Laporan PDF/Excel",
        description: "Ekspor laporan kas, iuran, tunggakan, dan rekap pembayaran.",
        tables: ["transaksi_kas", "tagihan", "pembayaran"]
      }
    ]
  },
  {
    id: "residents",
    title: "Manajemen Data Warga",
    short: "M",
    description: "Data warga, keluarga, dan mutasi.",
    tables: ["warga", "keluarga", "mutasi_warga"],
    features: [
      {
        title: "Data warga & kepala keluarga",
        description: "Kelola profil warga, anggota keluarga, dan kepala keluarga per wilayah.",
        tables: ["warga", "keluarga"]
      },
      {
        title: "Mutasi dan riwayat warga",
        description: "Catat warga pindah masuk, pindah keluar, kelahiran, dan perubahan status.",
        tables: ["mutasi_warga", "warga"]
      }
    ]
  },
  {
    id: "finance",
    title: "Keuangan RT/RW",
    short: "K",
    description: "Iuran, tagihan, pembayaran, dan kas.",
    tables: ["kategori_iuran", "aturan_iuran", "tagihan", "pembayaran", "transaksi_kas"],
    features: [
      {
        title: "Kategori dan aturan iuran",
        description: "Atur jenis iuran, nominal, siklus, jatuh tempo, dan cakupan RT.",
        tables: ["kategori_iuran", "aturan_iuran"]
      },
      {
        title: "Tagihan otomatis",
        description: "Terbitkan tagihan bulanan berdasarkan keluarga, periode, dan aturan iuran.",
        tables: ["periode_tagihan", "tagihan", "rincian_tagihan"]
      },
      {
        title: "Pembayaran QRIS/transfer/tunai",
        description: "Catat pembayaran warga melalui QRIS, transfer, atau pembayaran tunai.",
        tables: ["pembayaran", "tagihan"]
      },
      {
        title: "Verifikasi bukti pembayaran",
        description: "Periksa bukti transfer dan tindak lanjuti pembayaran yang menunggu verifikasi.",
        tables: ["pembayaran", "bukti_pembayaran", "berkas"]
      },
      {
        title: "Riwayat pembayaran & kuitansi",
        description: "Telusuri pembayaran terverifikasi dan kuitansi digital yang diterbitkan.",
        tables: ["pembayaran", "kuitansi"]
      },
      {
        title: "Kas masuk dan pengeluaran",
        description: "Catat arus kas lingkungan dan lihat saldo berdasarkan transaksi.",
        tables: ["transaksi_kas", "kategori_pengeluaran", "v_saldo_kas"]
      },
      {
        title: "Pengingat iuran/tunggakan",
        description: "Kirim pengingat tagihan yang mendekati jatuh tempo atau belum dibayar.",
        tables: ["tagihan", "notifikasi", "log_notifikasi"]
      }
    ]
  },
  {
    id: "letters",
    title: "Administrasi Surat",
    short: "A",
    description: "Pengajuan dan persetujuan surat.",
    tables: ["jenis_surat", "pengajuan_surat", "persetujuan_surat"],
    features: [
      {
        title: "Pengajuan surat online",
        description: "Ajukan surat keterangan domisili, pengantar, dan surat keterangan usaha.",
        tables: ["jenis_surat", "pengajuan_surat"]
      },
      {
        title: "Verifikasi dan persetujuan surat",
        description: "Tinjau kelengkapan data, catat keputusan, dan pantau status penerbitan.",
        tables: ["pengajuan_surat", "persetujuan_surat"]
      }
    ]
  },
  {
    id: "services",
    title: "Layanan Warga",
    short: "L",
    description: "Penilaian warga atas layanan pengurus.",
    tables: ["survei", "jawaban_survei"],
    features: [
      {
        title: "Survei kepuasan warga",
        description: "Buat survei layanan dan lihat ringkasan penilaian serta masukan warga.",
        tables: ["survei", "jawaban_survei"]
      }
    ]
  },
  {
    id: "communications",
    title: "Informasi & Komunikasi",
    short: "I",
    description: "Pengumuman dan notifikasi.",
    tables: ["pengumuman", "target_pengumuman", "notifikasi", "log_notifikasi"],
    features: [
      {
        title: "Pengumuman RT/RW",
        description: "Terbitkan informasi kepada seluruh lingkungan atau wilayah tertentu.",
        tables: ["pengumuman", "target_pengumuman"]
      },
      {
        title: "Notifikasi aplikasi",
        description: "Pantau pemberitahuan tagihan, pembayaran, dan informasi lingkungan.",
        tables: ["notifikasi", "log_notifikasi"]
      }
    ]
  },
  {
    id: "activities",
    title: "Sosial & Kegiatan",
    short: "S",
    description: "Agenda kerja bakti dan kegiatan sosial.",
    tables: ["kegiatan", "kehadiran_kegiatan"],
    features: [
      {
        title: "Jadwal kegiatan/kerja bakti",
        description: "Bagikan agenda warga dan catat konfirmasi kehadiran kegiatan.",
        tables: ["kegiatan", "kehadiran_kegiatan"]
      }
    ]
  },
  {
    id: "safety",
    title: "Keamanan & Lingkungan",
    short: "K",
    description: "Laporan kejadian dan kontak darurat.",
    tables: ["pengaduan", "tanggapan_pengaduan", "kontak_darurat"],
    features: [
      {
        title: "Laporan keluhan/kejadian",
        description: "Sampaikan laporan lingkungan dan tindak lanjuti progres penanganannya.",
        tables: ["pengaduan", "tanggapan_pengaduan"]
      },
      {
        title: "Kontak darurat/direktori layanan",
        description: "Simpan kontak pengurus, keamanan, ambulans, polisi, dan layanan lainnya.",
        tables: ["kontak_darurat"]
      }
    ]
  },
  {
    id: "access",
    title: "Registrasi & Akses",
    short: "R",
    description: "Pendaftaran, verifikasi, dan hak akses.",
    tables: ["pengguna", "kode_verifikasi", "peran", "izin_akses", "penugasan_pengguna"],
    features: [
      {
        title: "Registrasi warga",
        description: "Daftarkan akun warga dan kirim kode verifikasi melalui email atau telepon.",
        tables: ["pengguna", "kode_verifikasi"]
      },
      {
        title: "Verifikasi akun oleh Admin",
        description: "Tinjau pendaftaran dan hubungkan akun dengan data warga serta wilayahnya.",
        tables: ["pengguna", "warga", "penugasan_pengguna"]
      },
      {
        title: "Manajemen peran dan hak akses",
        description: "Atur peran, penugasan wilayah, dan izin aksi setiap pengguna.",
        tables: ["peran", "izin_akses", "peran_izin_akses", "penugasan_pengguna"]
      }
    ]
  },
  {
    id: "account-security",
    title: "Keamanan Akun",
    short: "K",
    description: "Riwayat aktivitas dan sesi login.",
    tables: ["log_audit", "sesi_pengguna"],
    features: [
      {
        title: "Riwayat aktivitas/audit log",
        description: "Lihat jejak aktivitas penting untuk membantu pengawasan dan audit.",
        tables: ["log_audit"]
      },
      {
        title: "Manajemen sesi/perangkat",
        description: "Periksa perangkat yang pernah masuk dan cabut sesi yang tidak digunakan.",
        tables: ["sesi_pengguna"]
      }
    ]
  },
  {
    id: "platform",
    title: "Manajemen Platform",
    short: "M",
    description: "Multi-lingkungan serta ekspor dan backup data.",
    tables: ["organisasi", "rw", "rt", "cadangan_data", "pengaturan"],
    features: [
      {
        title: "Multi-RT/RW",
        description: "Kelola organisasi, RW, RT, dan pengaturan lingkungan dalam satu platform.",
        tables: ["organisasi", "rw", "rt", "pengaturan"]
      },
      {
        title: "Ekspor dan backup data",
        description: "Lihat riwayat pencadangan data yang tersimpan di lokasi privat.",
        tables: ["cadangan_data"]
      }
    ]
  }
];

const demoData = {
  organization: "Perumahan Griya Contoh",
  region: "RW 05",
  user: "Rina Wijaya",
  role: "Admin RW",
  residents: 12,
  families: 6,
  unpaidInvoices: 5,
  cashBalance: 130000,
  collected: 50000,
  billed: 310000
};

let dashboardData = { ...demoData };
let currentUser = {
  name: demoData.user,
  role: demoData.role,
  organizationName: demoData.organization,
  region: demoData.region
};
let announcements = [
  {
    title: "Kerja bakti lingkungan",
    body: "Kerja bakti dilaksanakan Minggu, 15 November 2026 pukul 07.00 di area RW 05.",
    publishedAt: "2026-11-05 08:00:00"
  },
  {
    title: "Pengumuman iuran RT 01",
    body: "Iuran November jatuh tempo tanggal 10. Mohon dibayar melalui aplikasi.",
    publishedAt: "2026-11-02 08:00:00"
  }
];
let activities = [
  { title: "Pembayaran November telah diverifikasi", detail: "Tagihan TGH/2026/11/0001 · Rp 50.000", actor: "Dewi Lestari", createdAt: "2026-11-03 09:30:00" },
  { title: "Pengajuan surat pengantar diterima", detail: "Andi Wijaya · 7 November 2026", actor: "Hendra Gunawan", createdAt: "2026-11-07 09:00:00" },
  { title: "1 pembayaran menunggu verifikasi", detail: "Transfer · Rp 55.000", actor: "Agus Pratama", createdAt: "2026-11-07 21:10:00" }
];
let notifications = [
  { title: "Pembayaran perlu diverifikasi", message: "Ada 1 pembayaran menunggu verifikasi.", type: "pembayaran", readAt: null, createdAt: "2026-11-07 21:10:00" },
  { title: "Kerja bakti lingkungan", message: "Minggu, 15 November · Taman RW 05", type: "pengumuman", readAt: null, createdAt: "2026-11-05 08:00:00" }
];
let unreadNotificationCount = notifications.length;
let isConnectedToApi = false;

const appShell = document.querySelector("#app-shell");
const loginScreen = document.querySelector("#login-screen");
const navigation = document.querySelector("#main-navigation");
const pageContent = document.querySelector("#page-content");
const globalSearch = document.querySelector("#global-search");
const toast = document.querySelector("#toast");
let activePage = "home";
let activeFeature = null;
let featureRows = [];
let featureLoading = false;
let featureError = "";
let featureRequestId = 0;
let toastTimeout;

const featureEndpoints = {
  reports: ["dashboard/summary", "dashboard/summary"],
  residents: ["resources/warga", "resources/mutasi"],
  finance: [
    "resources/kategori-iuran", "resources/tagihan", "resources/pembayaran",
    "resources/pembayaran", "resources/pembayaran", "resources/transaksi-kas",
    "resources/tagihan"
  ],
  letters: ["resources/jenis-surat", "resources/pengajuan-surat"],
  services: ["resources/survei"],
  communications: ["announcements?limit=100", "notifications?limit=100"],
  activities: ["resources/kegiatan"],
  safety: ["resources/pengaduan", "resources/kontak-darurat"],
  access: ["resources/pengguna", "registrations/pending", "resources/peran"],
  "account-security": ["resources/audit-log", "resources/sesi"],
  platform: ["resources/wilayah", "resources/cadangan-data"]
};

const resourcePermissions = {
  warga: { code: "warga.read", supportsRestrictedAccess: true },
  mutasi: { code: "mutasi_warga.read", supportsRestrictedAccess: true },
  "kategori-iuran": { code: "kategori_iuran.read", supportsRestrictedAccess: false },
  tagihan: { code: "tagihan.read", supportsRestrictedAccess: true },
  pembayaran: { code: "pembayaran.read", supportsRestrictedAccess: true },
  "transaksi-kas": { code: "laporan_keuangan.read", supportsRestrictedAccess: false },
  "jenis-surat": { code: "pengajuan_surat.create", supportsRestrictedAccess: false },
  "pengajuan-surat": { code: "pengajuan_surat.read", supportsRestrictedAccess: true },
  survei: { code: "warga.read", supportsRestrictedAccess: true },
  pengumuman: { code: "pengumuman.read", supportsRestrictedAccess: true },
  kegiatan: { code: "kegiatan.read", supportsRestrictedAccess: false },
  pengaduan: { code: "pengaduan.read", supportsRestrictedAccess: true },
  "kontak-darurat": { code: "warga.read", supportsRestrictedAccess: true },
  pengguna: { code: "pengguna.read", supportsRestrictedAccess: false },
  peran: { code: "pengguna.read", supportsRestrictedAccess: false },
  "audit-log": { code: "laporan_aktivitas.read", supportsRestrictedAccess: false },
  wilayah: { code: "wilayah.read", supportsRestrictedAccess: false },
  "cadangan-data": { code: "pengaturan.read", supportsRestrictedAccess: false }
};

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, (character) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    "\"": "&quot;",
    "'": "&#39;"
  })[character]);
}

function formatRupiah(value) {
  if (value === null || value === undefined || !Number.isFinite(Number(value))) {
    return "—";
  }
  return new Intl.NumberFormat("id-ID", {
    style: "currency",
    currency: "IDR",
    maximumFractionDigits: 0
  }).format(Number(value));
}

function formatShortDate(value) {
  const match = String(value ?? "").match(/^(\d{4})-(\d{2})-(\d{2})/);
  if (!match) {
    return "Tanggal tidak tersedia";
  }
  const date = new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]), 12);
  return new Intl.DateTimeFormat("id-ID", { day: "numeric", month: "short" }).format(date);
}

function renderNotifications() {
  const unreadElement = document.querySelector("#notification-unread");
  const notificationButton = document.querySelector("#notification-toggle");
  const notificationDot = notificationButton.querySelector(".notification-dot");
  const itemsContainer = document.querySelector("#notification-items");

  unreadElement.textContent = `${unreadNotificationCount} baru`;
  notificationDot.hidden = unreadNotificationCount === 0;
  notificationButton.setAttribute(
    "aria-label",
    unreadNotificationCount > 0 ? `Notifikasi, ${unreadNotificationCount} belum dibaca` : "Notifikasi"
  );

  if (!notifications.length) {
    itemsContainer.innerHTML = '<p class="notification-empty">Belum ada notifikasi.</p>';
    return;
  }

  itemsContainer.innerHTML = notifications.map((notification) => `
    <article class="notification-item">
      <span class="notification-icon ${notification.type === "pembayaran" ? "warning-icon" : "green-icon"}" aria-hidden="true">${notification.type === "pembayaran" ? "!" : "↗"}</span>
      <div>
        <strong>${escapeHtml(notification.title)}</strong>
        <p>${escapeHtml(notification.message)}</p>
        <time>${escapeHtml(formatShortDate(notification.createdAt))}</time>
      </div>
    </article>
  `).join("");
}

function renderAnnouncements() {
  if (!announcements.length) {
    return '<p class="empty-state">Belum ada pengumuman aktif.</p>';
  }

  return announcements.map((announcement) => {
    const date = formatShortDate(announcement.publishedAt).split(" ");
    return `
      <article class="announcement-item">
        <div class="announcement-date"><strong>${escapeHtml(date[0])}</strong><span>${escapeHtml(date[1] || "")}</span></div>
        <div class="announcement-copy"><h3>${escapeHtml(announcement.title)}</h3><p>${escapeHtml(announcement.body)}</p></div>
      </article>
    `;
  }).join("");
}

function renderActivities() {
  if (!activities.length) {
    return '<p class="empty-state">Belum ada aktivitas yang dapat ditampilkan.</p>';
  }

  return activities.map((activity, index) => `
    <div class="activity-item">
      <span class="activity-mark" aria-hidden="true">${index === 0 ? "✓" : "↗"}</span>
      <div class="activity-copy">
        <strong>${escapeHtml(activity.title)}</strong>
        <span>${escapeHtml(activity.detail || activity.actor || "Aktivitas lingkungan")} · ${escapeHtml(formatShortDate(activity.createdAt))}</span>
      </div>
    </div>
  `).join("");
}

function updateUserDisplay() {
  const userName = currentUser.name || "Pengguna";
  const userRegion = currentUser.region || "Semua wilayah";
  const organizationName = currentUser.organizationName || "Semua organisasi";

  document.querySelector("#profile-name").textContent = userName;
  document.querySelector("#profile-role").textContent = `${currentUser.role || "Pengguna"} · ${userRegion}`;
  document.querySelector("#profile-avatar").textContent = userName.trim().charAt(0).toLocaleUpperCase("id-ID");
  document.querySelector("#organization-name").textContent = organizationName;
  document.querySelector("#region-name").textContent = userRegion;
  document.querySelector("#connection-status").textContent = isConnectedToApi
    ? "Terhubung ke API · Data dari MySQL"
    : "Mode demo offline · Data contoh";
}

function renderNavigation() {
  const moduleLinks = getAccessibleModules().map((module) => `
    <button class="nav-link${activePage === module.id ? " active" : ""}" type="button" data-page="${module.id}"${activePage === module.id ? ' aria-current="page"' : ""}>
      <span class="nav-icon" aria-hidden="true">${module.short}</span>
      <span class="nav-label">${module.title}</span>
      <span class="nav-count">${getAccessibleFeatures(module).length}</span>
    </button>
  `).join("");

  navigation.innerHTML = `
    <p class="nav-section-label">Menu utama</p>
    <button class="nav-link${activePage === "home" ? " active" : ""}" type="button" data-page="home"${activePage === "home" ? ' aria-current="page"' : ""}>
      <span class="nav-icon" aria-hidden="true">B</span><span class="nav-label">Beranda</span>
    </button>
    <p class="nav-section-label">Modul</p>
    ${moduleLinks}
  `;
  document.querySelector("#account-settings-button").classList.toggle("active", activePage === "account");
}

function renderModuleCards(query = "") {
  const normalizedQuery = query.trim().toLocaleLowerCase("id-ID");
  const visibleModules = getAccessibleModules().filter((module) =>
    `${module.title} ${module.description} ${getAccessibleFeatures(module).map(({ feature }) => feature.title).join(" ")}`
      .toLocaleLowerCase("id-ID")
      .includes(normalizedQuery)
  );

  if (!visibleModules.length) {
    return '<div class="empty-state">Tidak ada modul yang cocok dengan pencarian Anda.</div>';
  }

  return visibleModules.map((module) => `
    <button class="module-card" type="button" data-page="${module.id}" aria-label="Buka modul ${module.title}">
      <span class="module-card-top">
        <span class="module-icon" aria-hidden="true">${module.short}</span>
        <span class="module-arrow" aria-hidden="true">↗</span>
      </span>
      <h3>${module.title}</h3>
      <p>${module.description}</p>
      <span class="module-card-bottom"><span>${getAccessibleFeatures(module).length} fitur</span><span>Buka modul</span></span>
    </button>
  `).join("");
}

function renderHome() {
  const query = globalSearch.value;
  const visibleModules = getAccessibleModules();
  const filteredModules = visibleModules.filter((module) =>
    `${module.title} ${module.description} ${getAccessibleFeatures(module).map(({ feature }) => feature.title).join(" ")}`
      .toLocaleLowerCase("id-ID")
      .includes(query.trim().toLocaleLowerCase("id-ID"))
  );
  const moduleContent = renderModuleCards(query);
  const featureCount = visibleModules.reduce((count, module) => count + getAccessibleFeatures(module).length, 0);
  const moduleDescription = query.trim()
    ? `${filteredModules.length} dari ${visibleModules.length} modul ditemukan.`
    : `Pilih menu untuk mulai mengelola lingkungan ${escapeHtml(currentUser.region || "Anda")}.`;
  const periodName = dashboardData?.currentPeriodName || "Periode berjalan";
  const cashNote = isConnectedToApi ? "Saldo berdasarkan transaksi kas" : "Akumulasi transaksi kas demo";
  const canViewAnnouncements = !isConnectedToApi || userCanUsePermission(currentUser, "pengumuman.read", true);
  const canViewActivities = !isConnectedToApi || userCanUsePermission(currentUser, "laporan_aktivitas.read");
  const lowerPanelCount = Number(canViewAnnouncements) + Number(canViewActivities);

  pageContent.innerHTML = `
    <section class="page-heading">
      <div>
        <p class="eyebrow">Ringkasan lingkungan</p>
        <h1>Selamat datang, ${escapeHtml((currentUser.name || "Pengguna").split(" ")[0])}!</h1>
        <p>${moduleDescription}</p>
      </div>
      <span class="heading-tag"><span class="demo-dot" aria-hidden="true"></span>${escapeHtml(dashboardData.organizationName)} · ${escapeHtml(dashboardData.region)}</span>
    </section>

    ${dashboardData?.summaryAvailable === false ? "" : `
    <section class="stats-grid" aria-label="Ringkasan data lingkungan">
      <article class="stat-card">
        <div class="stat-card-top"><span class="stat-label">Warga terdaftar</span><span class="stat-icon" aria-hidden="true">W</span></div>
        <strong>${dashboardData.residents}</strong><small>Data aktif di lingkungan</small>
      </article>
      <article class="stat-card">
        <div class="stat-card-top"><span class="stat-label">Kepala keluarga</span><span class="stat-icon" aria-hidden="true">K</span></div>
        <strong>${dashboardData.families}</strong><small>Rumah tangga terdata</small>
      </article>
      <article class="stat-card warning">
        <div class="stat-card-top"><span class="stat-label">Tagihan belum lunas</span><span class="stat-icon" aria-hidden="true">!</span></div>
        <strong>${dashboardData.unpaidInvoices}</strong><small>${escapeHtml(periodName)}</small>
      </article>
      <article class="stat-card gold">
        <div class="stat-card-top"><span class="stat-label">Saldo kas lingkungan</span><span class="stat-icon" aria-hidden="true">Rp</span></div>
        <strong>${formatRupiah(dashboardData.cashBalance)}</strong><small>${cashNote}</small>
      </article>
    </section>
    `}

    <section aria-labelledby="modules-title">
      <div class="section-heading">
        <div><h2 id="modules-title">Jelajahi modul</h2><p>${isConnectedToApi ? "Modul yang tersedia sesuai izin peran Anda." : "Semua kebutuhan pengelolaan lingkungan dalam satu tempat."}</p></div>
        <span class="heading-tag">${visibleModules.length} modul · ${featureCount} fitur</span>
      </div>
      <div class="module-grid">${moduleContent}</div>
    </section>

    ${query.trim() || lowerPanelCount === 0 ? "" : `
      <section class="home-lower-grid${lowerPanelCount === 1 ? " single-panel" : ""}" aria-label="Informasi terbaru">
        ${canViewAnnouncements ? `
        <article class="panel">
          <div class="panel-heading"><div><h2>Informasi lingkungan</h2><p>Pengumuman terbaru dari pengurus</p></div><span class="heading-tag">${announcements.length} aktif</span></div>
          <div class="announcement-list">${renderAnnouncements()}</div>
        </article>
        ` : ""}
        ${canViewActivities ? `
        <article class="panel">
          <div class="panel-heading"><div><h2>Aktivitas terbaru</h2><p>Ringkasan dari log aktivitas</p></div></div>
          <div class="activity-list">${renderActivities()}</div>
        </article>
        ` : ""}
      </section>
    `}
  `;
}

function renderFeatureCards(module, query = "") {
  const normalizedQuery = query.trim().toLocaleLowerCase("id-ID");
  const visibleFeatures = getAccessibleFeatures(module).filter(({ feature }) =>
    `${feature.title} ${feature.description} ${feature.tables.join(" ")}`
      .toLocaleLowerCase("id-ID")
      .includes(normalizedQuery)
  );

  if (!visibleFeatures.length) {
    return '<div class="empty-state">Tidak ada fitur dalam modul ini yang cocok dengan pencarian.</div>';
  }

  return visibleFeatures.map((feature, index) => `
    <button class="feature-card" type="button" data-feature-module="${module.id}" data-feature-index="${feature.index}" aria-label="Buka fitur ${feature.feature.title}">
      <span class="feature-icon" aria-hidden="true">${String(index + 1).padStart(2, "0")}</span>
      <h3>${feature.feature.title}</h3>
      <p>${feature.feature.description}</p>
      <span class="feature-card-footer"><span class="button button-secondary">Buka fitur</span><span class="feature-arrow" aria-hidden="true">→</span></span>
    </button>
  `).join("");
}

function renderReportPreview() {
  const income = [34, 57, 48, 77, 61, 88];
  const expense = [22, 38, 29, 49, 37, 54];
  const months = ["Jun", "Jul", "Agu", "Sep", "Okt", "Nov"];
  const bars = income.map((value, index) => `
    <div class="chart-group" aria-label="${months[index]}: ilustrasi pemasukan dan pengeluaran">
      <span class="chart-bar" style="height:${value}%"></span>
      <span class="chart-bar expense" style="height:${expense[index]}%"></span>
    </div>
  `).join("");
  const collected = Number(dashboardData.collected) || 0;
  const billed = Number(dashboardData.billed) || 0;
  const collectionRate = billed > 0 ? Math.min(100, Math.round((collected / billed) * 100)) : 0;
  const paidFamilies = Number(dashboardData.paidFamilies) || 0;
  const pendingPayments = Number(dashboardData.pendingPayments) || 0;
  const periodName = dashboardData.currentPeriodName || "Tidak ada periode aktif";

  return `
    <section class="dashboard-preview" aria-label="Ringkasan keuangan">
      <article class="panel chart-panel">
        <div class="panel-heading"><div><h2>Arus kas lingkungan</h2><p>Ilustrasi visual · data grafik menyusul</p></div><div class="chart-legend"><span><i class="legend-dot"></i>Masuk</span><span><i class="legend-dot expense"></i>Keluar</span></div></div>
        <div class="chart-area" role="img" aria-label="Ilustrasi arus kas selama enam bulan">${bars}</div>
        <div class="chart-labels">${months.map((month) => `<span>${month}</span>`).join("")}</div>
      </article>
      <article class="panel collection-panel">
        <div class="panel-heading"><div><h2>Iuran</h2><p>Ringkasan periode tagihan</p></div><span class="heading-tag">${escapeHtml(periodName)}</span></div>
        <div class="collection-content">
          <div class="collection-amount"><strong>${formatRupiah(collected)}</strong><span>dari ${formatRupiah(billed)}</span></div>
          <div class="progress-track" aria-label="${collectionRate} persen tagihan terkumpul"><span style="width:${collectionRate}%"></span></div>
          <div class="collection-foot"><span>${paidFamilies} dari ${dashboardData.families} keluarga lunas</span><span>${collectionRate}%</span></div>
          <div class="collection-separator"></div>
          <div class="collection-list">
            <div class="collection-row"><span>Tagihan belum lunas</span><strong>${dashboardData.unpaidInvoices} tagihan</strong></div>
            <div class="collection-row"><span>Menunggu verifikasi</span><strong>${pendingPayments} pembayaran</strong></div>
          </div>
        </div>
      </article>
    </section>
  `;
}

function renderModule(module) {
  const query = globalSearch.value;
  const accessibleFeatures = getAccessibleFeatures(module);
  const matchingFeatures = accessibleFeatures.filter(({ feature }) =>
    `${feature.title} ${feature.description} ${feature.tables.join(" ")}`
      .toLocaleLowerCase("id-ID")
      .includes(query.trim().toLocaleLowerCase("id-ID"))
  );
  const resultDescription = query.trim()
    ? `${matchingFeatures.length} dari ${accessibleFeatures.length} fitur cocok dengan pencarian.`
    : module.description;

  pageContent.innerHTML = `
    <section class="page-heading">
      <div><p class="eyebrow">${escapeHtml(currentUser.role)} · ${escapeHtml(currentUser.region)}</p><h1>${module.title}</h1><p>${resultDescription}</p></div>
      <span class="heading-tag"><span class="demo-dot" aria-hidden="true"></span>${accessibleFeatures.length} fitur tersedia</span>
    </section>
    ${module.id === "reports" && !query.trim() ? renderReportPreview() : ""}
    <section aria-label="Daftar fitur ${module.title}">
      <div class="section-heading"><div><h2>Fitur modul</h2><p>Pilih fitur untuk membuka ruang kerja dan data terkait.</p></div></div>
      <div class="feature-grid">${renderFeatureCards(module, query)}</div>
    </section>
  `;
}

function renderAccountSettings() {
  pageContent.innerHTML = `
    <section class="page-heading">
      <div><p class="eyebrow">Pengaturan pribadi</p><h1>Akun saya</h1><p>Perbarui profil dan keamanan akun. Peran hanya dapat ditetapkan oleh pengurus.</p></div>
      <span class="heading-tag">${escapeHtml(currentUser.role || "Pengguna")}</span>
    </section>
    <section class="account-settings-grid">
      <article class="panel account-panel">
        <div class="panel-heading"><div><h2>Profil</h2><p>Informasi kontak untuk akun RT/RW.</p></div></div>
        <form id="profile-form" class="account-form">
          <label for="profile-name-input">Nama lengkap</label>
          <input id="profile-name-input" name="name" type="text" maxlength="150" value="${escapeHtml(currentUser.name || "")}" required>
          <label for="profile-email-input">Email</label>
          <input id="profile-email-input" name="email" type="email" maxlength="150" value="${escapeHtml(currentUser.email || "")}">
          <label for="profile-phone-input">Nomor telepon</label>
          <input id="profile-phone-input" name="phone" type="tel" maxlength="20" value="${escapeHtml(currentUser.phone || "")}">
          <button class="button button-primary" type="submit">Simpan profil</button>
        </form>
      </article>
      <article class="panel account-panel">
        <div class="panel-heading"><div><h2>Ganti kata sandi</h2><p>Gunakan minimal 8 karakter.</p></div></div>
        <form id="password-form" class="account-form">
          <label for="current-password">Kata sandi saat ini</label>
          <input id="current-password" name="currentPassword" type="password" autocomplete="current-password" required>
          <label for="new-password">Kata sandi baru</label>
          <input id="new-password" name="newPassword" type="password" autocomplete="new-password" minlength="8" required>
          <label for="confirm-password">Ulangi kata sandi baru</label>
          <input id="confirm-password" name="confirmPassword" type="password" autocomplete="new-password" minlength="8" required>
          <button class="button button-primary" type="submit">Perbarui kata sandi</button>
        </form>
      </article>
      <p class="account-role-note">Peran saat ini: <strong>${escapeHtml(currentUser.role || "Pengguna")}</strong>. Hubungi admin/RT/RW untuk perubahan peran; pendaftaran baru selalu dimulai sebagai Warga.</p>
    </section>
  `;
}

function xmlEscape(value) {
  return String(value ?? "")
    .replace(/[\u0000-\u0008\u000B\u000C\u000E-\u001F]/g, "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&apos;");
}

async function loadAllReportRows(resource) {
  const rows = [];
  let offset = 0;
  while (offset < 1000000) {
    const response = await window.RtrwApi.request(`/resources/${resource}?limit=100&offset=${offset}`);
    const pageRows = Array.isArray(response.items) ? response.items : [];
    rows.push(...pageRows);
    if (pageRows.length < 100) {
      return rows;
    }
    offset += pageRows.length;
  }
  throw new Error("Ekspor dihentikan karena jumlah data melewati batas aman.");
}

function reportWorkbookXml(sheets) {
  const worksheetXml = sheets.map(({ name, rows }) => {
    const columns = rows.length ? Object.keys(rows[0]) : ["Data"];
    const tableRows = [
      columns,
      ...rows.map((row) => columns.map((column) => row[column] ?? ""))
    ].map((values, rowIndex) => `
      <Row>${values.map((value, columnIndex) => {
        const column = columns[columnIndex];
        const numeric = rowIndex > 0
          && /(nominal|saldo|jumlah|total)/i.test(column)
          && /^-?\d+(?:\.\d+)?$/.test(String(value));
        return `<Cell><Data ss:Type="${numeric ? "Number" : "String"}">${xmlEscape(value)}</Data></Cell>`;
      }).join("")}</Row>`).join("");
    return `<Worksheet ss:Name="${name}"><Table>${tableRows}</Table></Worksheet>`;
  }).join("");

  return `<?xml version="1.0" encoding="UTF-8"?><?mso-application progid="Excel.Sheet"?>
    <Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
      xmlns:o="urn:schemas-microsoft-com:office:office"
      xmlns:x="urn:schemas-microsoft-com:office:excel"
      xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">${worksheetXml}</Workbook>`;
}

function reportPrintHtml(sheets) {
  const sections = sheets.map(({ name, rows }) => {
    const columns = rows.length ? Object.keys(rows[0]) : ["Data"];
    return `<section><h2>${escapeHtml(name)}</h2><table><thead><tr>${columns.map((column) => `<th>${escapeHtml(column.replace(/_/g, " "))}</th>`).join("")}</tr></thead><tbody>${rows.length
      ? rows.map((row) => `<tr>${columns.map((column) => `<td>${escapeHtml(row[column] ?? "—")}</td>`).join("")}</tr>`).join("")
      : `<tr><td colspan="${columns.length}">Tidak ada data</td></tr>`}</tbody></table></section>`;
  }).join("");
  return `<!doctype html><html lang="id"><head><meta charset="utf-8"><title>Laporan RT/RW</title>
    <style>body{font:12px Arial,sans-serif;color:#17231e;margin:24px}h1{font-size:20px}h2{font-size:15px;margin-top:24px}p{color:#53635b}table{width:100%;border-collapse:collapse;font-size:9px}th,td{border:1px solid #ccd5cf;padding:6px;text-align:left;overflow-wrap:anywhere}th{background:#edf2ef;text-transform:capitalize}@media print{body{margin:12mm}section{break-inside:avoid}h2{break-after:avoid}}</style>
    </head><body><h1>Laporan Sistem Informasi RT/RW</h1><p>${escapeHtml(currentUser.organizationName)} · ${escapeHtml(currentUser.region)} · ${new Date().toLocaleString("id-ID")}</p>${sections}</body></html>`;
}

async function exportReports(format) {
  const printWindow = format === "pdf" ? window.open("", "_blank") : null;
  if (format === "pdf" && !printWindow) {
    showToast("Izinkan pop-up browser untuk mencetak laporan.");
    return;
  }

  try {
    showToast("Mengambil seluruh data laporan dari API...");
    const [cash, invoices, payments] = await Promise.all([
      loadAllReportRows("transaksi-kas"),
      loadAllReportRows("tagihan"),
      loadAllReportRows("pembayaran")
    ]);
    const sheets = [
      { name: "Kas", rows: cash },
      { name: "Tagihan", rows: invoices },
      { name: "Pembayaran", rows: payments }
    ];

    if (format === "excel") {
      const workbook = reportWorkbookXml(sheets);
      const blob = new Blob([workbook], { type: "application/vnd.ms-excel;charset=utf-8" });
      const downloadUrl = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = downloadUrl;
      link.download = `laporan-rtrw-${new Date().toISOString().slice(0, 10)}.xls`;
      document.body.append(link);
      link.click();
      link.remove();
      window.setTimeout(() => URL.revokeObjectURL(downloadUrl), 1000);
      showToast("File Excel berhasil diunduh.");
      return;
    }

    printWindow.document.open();
    printWindow.document.write(reportPrintHtml(sheets));
    printWindow.document.close();
    window.setTimeout(() => {
      printWindow.focus();
      printWindow.print();
    }, 500);
    showToast("Laporan siap dicetak atau disimpan sebagai PDF.");
  } catch (error) {
    if (printWindow) {
      printWindow.close();
    }
    showToast(error.message);
  }
}

function resourcePath(moduleId, featureIndex) {
  const endpoints = featureEndpoints[moduleId];
  return endpoints ? endpoints[featureIndex] : null;
}

function userHasPermission(user, permission) {
  return user?.roleCode === "super_admin"
    || (Array.isArray(user?.permissions) && user.permissions.includes(permission));
}

function userCanUsePermission(user, permission, supportsRestrictedAccess = false) {
  if (!userHasPermission(user, permission)) {
    return false;
  }
  if (user?.roleCode === "super_admin" || supportsRestrictedAccess) {
    return true;
  }
  return !Array.isArray(user?.restrictedPermissions)
    || !user.restrictedPermissions.includes(permission);
}

function featureRequiredPermissions(moduleId, featureIndex) {
  const endpoint = resourcePath(moduleId, featureIndex);
  if (!endpoint) {
    return null;
  }

  const route = endpoint.split("?")[0];
  if (route === "dashboard/summary") {
    return [
      { code: "warga.read", supportsRestrictedAccess: true },
      { code: "laporan_keuangan.read", supportsRestrictedAccess: true }
    ];
  }
  if (route === "announcements") {
    return [{ code: "pengumuman.read", supportsRestrictedAccess: true }];
  }
  if (route === "notifications") {
    return [];
  }
  if (route === "registrations/pending") {
    return [];
  }

  const resourceMatch = route.match(/^resources\/([a-z-]+)$/);
  if (!resourceMatch || !Object.prototype.hasOwnProperty.call(resourcePermissions, resourceMatch[1])) {
    return null;
  }
  return [resourcePermissions[resourceMatch[1]]];
}

function canAccessFeature(moduleId, featureIndex) {
  if (!isConnectedToApi) {
    return true;
  }

  if (resourcePath(moduleId, featureIndex) === "registrations/pending") {
    return ["super_admin", "admin_rw", "ketua_rw", "ketua_rt"].includes(currentUser.roleCode);
  }

  const requiredPermissions = featureRequiredPermissions(moduleId, featureIndex);
  return requiredPermissions !== null
    && requiredPermissions.every(({ code, supportsRestrictedAccess }) =>
      userCanUsePermission(currentUser, code, supportsRestrictedAccess)
    );
}

function getAccessibleFeatures(module) {
  return module.features.reduce((features, feature, index) => {
    if (canAccessFeature(module.id, index)) {
      features.push({ feature, index });
    }
    return features;
  }, []);
}

function getAccessibleModules() {
  return modules.filter((module) => getAccessibleFeatures(module).length > 0);
}

function renderWorkspaceData(module, feature, endpoint) {
  if (featureLoading) {
    return '<div class="empty-state" role="status">Memuat data dari API...</div>';
  }
  if (featureError) {
    return `<div class="empty-state resource-error" role="alert">${escapeHtml(featureError)}</div>`;
  }
  if (!isConnectedToApi) {
    return '<div class="empty-state">Masuk melalui API untuk memuat data lingkungan. Mode demo tidak mengubah atau menyimpan data.</div>';
  }
  if (!featureRows.length) {
    return '<div class="empty-state">Belum ada data untuk fitur ini.</div>';
  }

  const query = (document.querySelector("#resource-search")?.value || "").trim().toLocaleLowerCase("id-ID");
  const filteredRows = featureRows.filter((row) =>
    Object.values(row).some((value) => String(value ?? "").toLocaleLowerCase("id-ID").includes(query))
  );
  if (!filteredRows.length) {
    return '<div class="empty-state">Tidak ada data yang cocok dengan pencarian.</div>';
  }

  if (endpoint === "dashboard/summary") {
    return `
      <div class="resource-stat-grid">
        ${filteredRows.map((row) => `
          <article class="resource-stat">
            <span>${escapeHtml(row.label)}</span>
            <strong>${escapeHtml(row.value)}</strong>
          </article>
        `).join("")}
      </div>
    `;
  }

  const columns = Object.keys(filteredRows[0]).slice(0, 7);
  const canApproveLetters = endpoint === "resources/pengajuan-surat"
    && userHasPermission(currentUser, "pengajuan_surat.approve");
  const canApproveRegistrations = endpoint === "registrations/pending";
  return `
    <div class="resource-table-wrap">
      <table class="resource-table">
        <thead><tr>${columns.map((column) => `<th scope="col">${escapeHtml(column.replace(/_/g, " "))}</th>`).join("")}${canApproveLetters || canApproveRegistrations ? "<th scope=\"col\">Tindakan</th>" : ""}</tr></thead>
        <tbody>
          ${filteredRows.map((row) => `
            <tr>
              ${columns.map((column) => `<td>${escapeHtml(row[column] === null || row[column] === "" ? "—" : row[column])}</td>`).join("")}
              ${canApproveLetters ? `<td>${["diajukan", "diproses"].includes(row.status) ? `<button class="button button-secondary table-action" type="button" data-letter-decision="setuju" data-letter-id="${Number(row.id)}">Setujui</button> <button class="button button-outline table-action" type="button" data-letter-decision="tolak" data-letter-id="${Number(row.id)}">Tolak</button>` : "—"}</td>` : ""}
              ${canApproveRegistrations ? `<td>${row.id_warga ? `<button class="button button-primary table-action" type="button" data-approve-registration="${Number(row.id)}">Setujui sebagai Warga</button>` : '<span class="muted">Data warga/telepon belum cocok</span>'}</td>` : ""}
            </tr>
          `).join("")}
        </tbody>
      </table>
    </div>
  `;
}

function renderFeatureWorkspace(module, feature, featureIndex) {
  const endpoint = resourcePath(module.id, featureIndex);
  const canExportReports = module.id === "reports" && featureIndex === 1
    && userCanUsePermission(currentUser, "laporan_keuangan.read");
  pageContent.innerHTML = `
    <section class="page-heading workspace-heading">
      <div>
        <button class="section-link workspace-back" type="button" data-page="${module.id}">← Kembali ke ${escapeHtml(module.title)}</button>
        <p class="eyebrow">${escapeHtml(module.title)} · Ruang kerja fitur</p>
        <h1>${escapeHtml(feature.title)}</h1>
        <p>${escapeHtml(feature.description)}</p>
      </div>
      <span class="heading-tag">${isConnectedToApi ? "Terhubung ke API" : "Mode demo"}</span>
    </section>
    <section class="workspace-grid">
      <article class="panel workspace-panel">
        <div class="panel-heading">
          <div><h2>Data fitur</h2><p>Data dibatasi sesuai izin dan wilayah akun Anda.</p></div>
          <div class="workspace-actions">
            ${canExportReports ? '<button class="button button-secondary" type="button" data-export-report="excel">Unduh Excel</button><button class="button button-secondary" type="button" data-export-report="pdf">Cetak / Simpan PDF</button>' : ""}
            <button class="button button-outline" type="button" data-resource-refresh${!isConnectedToApi ? " disabled" : ""}>Muat ulang</button>
          </div>
        </div>
        ${module.id === "letters" && featureIndex === 0 && userHasPermission(currentUser, "pengajuan_surat.create") ? `
          <div class="letter-request-area">
            <h3>Ajukan surat</h3>
            <form id="letter-request-form" class="inline-form">
              <label for="letter-type">Jenis surat</label>
              <select id="letter-type" name="jenisSuratId" required>
                <option value="">Pilih jenis surat</option>
                ${featureRows.map((row) => `<option value="${Number(row.id)}">${escapeHtml(row.nama)}</option>`).join("")}
              </select>
              <label for="letter-purpose">Keperluan</label>
              <input id="letter-purpose" name="keperluan" type="text" maxlength="255" placeholder="Jelaskan keperluan surat" required>
              <button class="button button-primary" type="submit"${!isConnectedToApi ? " disabled" : ""}>Kirim pengajuan</button>
            </form>
            ${!isConnectedToApi ? '<p class="registration-note">Pengajuan hanya dapat dikirim setelah masuk melalui API.</p>' : ""}
          </div>
        ` : ""}
        ${canExportReports ? '<p class="export-note">Ekspor mengambil seluruh data yang dapat diakses (kas, tagihan, pembayaran) dari API. PDF dibuka melalui dialog cetak browser.</p>' : ""}
        <div class="resource-toolbar">
          <label class="resource-search-label" for="resource-search">Cari dalam data</label>
          <input id="resource-search" class="resource-search" type="search" placeholder="Ketik kata kunci..." autocomplete="off">
        </div>
        <div id="resource-data" class="resource-data" aria-live="polite">
          ${renderWorkspaceData(module, feature, endpoint)}
        </div>
      </article>
    </section>
  `;

  if (isConnectedToApi && endpoint) {
    void loadFeatureData(module, feature, featureIndex, endpoint);
  }
}

function updateLetterTypeOptions() {
  const selector = document.querySelector("#letter-type");
  if (!selector) {
    return;
  }
  selector.innerHTML = `<option value="">Pilih jenis surat</option>${featureRows
    .map((row) => `<option value="${Number(row.id)}">${escapeHtml(row.nama)}</option>`)
    .join("")}`;
}

async function loadFeatureData(module, feature, featureIndex, endpoint) {
  const requestId = ++featureRequestId;
  featureLoading = true;
  featureError = "";
  const dataContainer = document.querySelector("#resource-data");
  if (dataContainer) {
    dataContainer.innerHTML = '<div class="empty-state" role="status">Memuat data dari API...</div>';
  }

  try {
    const response = await window.RtrwApi.request(`/${endpoint}`);
    if (requestId !== featureRequestId || !activeFeature || activeFeature.moduleId !== module.id || activeFeature.featureIndex !== featureIndex) {
      return;
    }

    if (endpoint === "dashboard/summary") {
      const { stats, period } = response;
      featureRows = [
        { label: "Warga terdaftar", value: stats.residents },
        { label: "Kepala keluarga", value: stats.families },
        { label: "Tagihan belum lunas", value: stats.unpaidInvoices },
        { label: "Saldo kas", value: stats.cashBalance === null ? "Tidak tersedia" : formatRupiah(stats.cashBalance) },
        { label: "Tagihan periode ini", value: formatRupiah(period.billed) },
        { label: "Pembayaran terkumpul", value: formatRupiah(period.collected) }
      ];
    } else {
      featureRows = Array.isArray(response.items) ? response.items : [];
    }
    featureLoading = false;
    if (endpoint === "resources/jenis-surat") {
      updateLetterTypeOptions();
    }
    const container = document.querySelector("#resource-data");
    if (container) {
      container.innerHTML = renderWorkspaceData(module, feature, endpoint);
    }
  } catch (error) {
    if (requestId !== featureRequestId) {
      return;
    }
    featureLoading = false;
    featureError = error.message;
    const container = document.querySelector("#resource-data");
    if (container) {
      container.innerHTML = renderWorkspaceData(module, feature, endpoint);
    }
  }
}

function renderPage() {
  if (activeFeature && !canAccessFeature(activeFeature.moduleId, activeFeature.featureIndex)) {
    activeFeature = null;
  }
  if (activePage !== "home" && activePage !== "account") {
    const activeModule = modules.find((item) => item.id === activePage);
    if (!activeModule || getAccessibleFeatures(activeModule).length === 0) {
      activePage = "home";
    }
  }

  renderNavigation();
  if (activePage === "account") {
    renderAccountSettings();
    return;
  }
  if (activeFeature) {
    const module = modules.find((item) => item.id === activeFeature.moduleId);
    const feature = module?.features[activeFeature.featureIndex];
    if (module && feature) {
      renderFeatureWorkspace(module, feature, activeFeature.featureIndex);
      return;
    }
    activeFeature = null;
  }
  if (activePage === "home") {
    renderHome();
    return;
  }

  const module = modules.find((item) => item.id === activePage);
  if (module) {
    renderModule(module);
  }
}

function showToast(message) {
  window.clearTimeout(toastTimeout);
  toast.textContent = message;
  toast.classList.add("visible");
  toastTimeout = window.setTimeout(() => toast.classList.remove("visible"), 3200);
}

function showDashboard() {
  loginScreen.hidden = true;
  appShell.hidden = false;
  activePage = "home";
  activeFeature = null;
  globalSearch.value = "";
  renderPage();
  pageContent.focus({ preventScroll: true });
}

function openDemoDashboard() {
  dashboardData = {
    ...demoData,
    organizationName: demoData.organization,
    currentPeriodName: "November 2026",
    paidFamilies: 1,
    pendingPayments: 1
  };
  currentUser = {
    name: demoData.user,
    role: demoData.role,
    organizationName: demoData.organization,
    region: demoData.region
  };
  announcements = [
    {
      title: "Kerja bakti lingkungan",
      body: "Kerja bakti dilaksanakan Minggu, 15 November 2026 pukul 07.00 di area RW 05.",
      publishedAt: "2026-11-05 08:00:00"
    },
    {
      title: "Pengumuman iuran RT 01",
      body: "Iuran November jatuh tempo tanggal 10. Mohon dibayar melalui aplikasi.",
      publishedAt: "2026-11-02 08:00:00"
    }
  ];
  activities = [
    { title: "Pembayaran November telah diverifikasi", detail: "Tagihan TGH/2026/11/0001 · Rp 50.000", actor: "Dewi Lestari", createdAt: "2026-11-03 09:30:00" },
    { title: "Pengajuan surat pengantar diterima", detail: "Andi Wijaya · 7 November 2026", actor: "Hendra Gunawan", createdAt: "2026-11-07 09:00:00" },
    { title: "1 pembayaran menunggu verifikasi", detail: "Transfer · Rp 55.000", actor: "Agus Pratama", createdAt: "2026-11-07 21:10:00" }
  ];
  notifications = [
    { title: "Pembayaran perlu diverifikasi", message: "Ada 1 pembayaran menunggu verifikasi.", type: "pembayaran", readAt: null, createdAt: "2026-11-07 21:10:00" },
    { title: "Kerja bakti lingkungan", message: "Minggu, 15 November · Taman RW 05", type: "pengumuman", readAt: null, createdAt: "2026-11-05 08:00:00" }
  ];
  unreadNotificationCount = notifications.length;
  isConnectedToApi = false;
  window.clearTimeout(toastTimeout);
  toast.classList.remove("visible");
  toast.textContent = "";
  updateUserDisplay();
  renderNotifications();
  showDashboard();
}

async function openApiDashboard(user) {
  const [summary, announcementResponse, notificationResponse] = await Promise.all([
    ["warga.read", "laporan_keuangan.read"].every((permission) =>
      userCanUsePermission(user, permission, true)
    )
      ? window.RtrwApi.request("/dashboard/summary")
      : Promise.resolve(null),
    userCanUsePermission(user, "pengumuman.read", true)
      ? window.RtrwApi.request("/announcements?limit=5")
      : Promise.resolve({ items: [] }),
    window.RtrwApi.request("/notifications?limit=5")
  ]);
  const organizationName = summary?.organization.name || user.organizationName || "Semua organisasi";
  const regionParts = [];
  if (summary?.organization.rtNumber) {
    regionParts.push(`RT ${summary.organization.rtNumber}`);
  }
  if (summary?.organization.rwNumber) {
    regionParts.push(`RW ${summary.organization.rwNumber}`);
  }
  const region = user.region || regionParts.join(" / ") || "Semua wilayah";

  currentUser = { ...user, organizationName, region };
  dashboardData = summary ? {
    organizationName,
    region,
    residents: summary.stats.residents,
    families: summary.stats.families,
    unpaidInvoices: summary.stats.unpaidInvoices,
    cashBalance: summary.stats.cashBalance,
    currentPeriodName: summary.period.name,
    collected: summary.period.collected,
    billed: summary.period.billed,
    paidFamilies: summary.period.paidFamilies,
    pendingPayments: summary.period.pendingPayments,
    summaryAvailable: true
  } : {
    organizationName,
    region,
    summaryAvailable: false
  };
  announcements = announcementResponse.items;
  activities = summary?.activities || [];
  notifications = notificationResponse.items;
  unreadNotificationCount = notificationResponse.unreadCount;
  isConnectedToApi = true;

  updateUserDisplay();
  renderNotifications();
  showDashboard();
}

async function logoutFromApp() {
  if (isConnectedToApi) {
    try {
      await window.RtrwApi.request("/auth/logout", { method: "POST" });
    } catch (error) {
      showToast(error.message);
      return;
    }
  }

  appShell.hidden = true;
  loginScreen.hidden = false;
  document.querySelector("#login-form").reset();
  closeMobileMenu();
  isConnectedToApi = false;
}

async function restoreSession() {
  if (window.location.protocol === "file:") {
    return;
  }

  try {
    const response = await window.RtrwApi.request("/auth/me");
    await openApiDashboard(response.user);
  } catch (error) {
    if (error instanceof window.RtrwApi.ApiError && error.status === 401) {
      return;
    }
    showToast(error.message);
  }
}

function closeMobileMenu() {
  appShell.classList.remove("sidebar-open");
  document.querySelector("#mobile-menu-toggle").setAttribute("aria-expanded", "false");
}

function setTheme(theme) {
  document.body.dataset.theme = theme;
  document.querySelectorAll("[data-theme-toggle]").forEach((button) => {
    const icon = button.querySelector(".theme-icon");
    const label = button.querySelector(".theme-label");
    const darkMode = theme === "dark";
    icon.textContent = darkMode ? "☀" : "☾";
    label.textContent = darkMode ? "Terang" : "Gelap";
    button.setAttribute("aria-label", `Aktifkan mode ${darkMode ? "terang" : "gelap"}`);
  });
  try {
    window.localStorage.setItem("rtrw-theme", theme);
  } catch (error) {
    showToast("Preferensi tema hanya disimpan selama halaman ini terbuka.");
  }
}

function initializeTheme() {
  let savedTheme = "light";
  try {
    savedTheme = window.localStorage.getItem("rtrw-theme") || "light";
  } catch (error) {
    savedTheme = "light";
  }
  setTheme(savedTheme === "dark" ? "dark" : "light");
}

document.addEventListener("click", async (event) => {
  const target = event.target;
  if (!(target instanceof Element)) {
    return;
  }

  const pageButton = target.closest("[data-page]");
  if (pageButton) {
    const page = pageButton.dataset.page;
    if (page !== "home" && page !== "account" && !getAccessibleModules().some((module) => module.id === page)) {
      return;
    }
    if (page === "account" && !isConnectedToApi) {
      return;
    }
    activePage = page;
    activeFeature = null;
    featureRequestId += 1;
    globalSearch.value = "";
    renderPage();
    closeMobileMenu();
    return;
  }

  const featureButton = target.closest("[data-feature-module]");
  if (featureButton) {
    const moduleId = featureButton.dataset.featureModule;
    const featureIndex = Number(featureButton.dataset.featureIndex);
    if (!canAccessFeature(moduleId, featureIndex)) {
      return;
    }
    activeFeature = {
      moduleId,
      featureIndex
    };
    featureRows = [];
    featureLoading = false;
    featureError = "";
    renderPage();
    pageContent.focus({ preventScroll: true });
    return;
  }

  if (target.closest("[data-resource-refresh]") && activeFeature) {
    const module = modules.find((item) => item.id === activeFeature.moduleId);
    const feature = module?.features[activeFeature.featureIndex];
    const endpoint = resourcePath(activeFeature.moduleId, activeFeature.featureIndex);
    if (module && feature && endpoint) {
      void loadFeatureData(module, feature, activeFeature.featureIndex, endpoint);
    }
    return;
  }

  const exportButton = target.closest("[data-export-report]");
  if (exportButton) {
    void exportReports(exportButton.dataset.exportReport);
    return;
  }

  const approvalButton = target.closest("[data-approve-registration]");
  if (approvalButton) {
    if (!window.confirm("Setujui pendaftaran ini sebagai akun Warga?")) {
      return;
    }
    approvalButton.disabled = true;
    try {
      const result = await window.RtrwApi.request(`/registrations/${approvalButton.dataset.approveRegistration}/approve`, {
        method: "POST",
        body: {}
      });
      showToast(result.message || "Pendaftaran disetujui.");
      const module = modules.find((item) => item.id === activeFeature?.moduleId);
      const feature = module?.features[activeFeature?.featureIndex];
      const endpoint = activeFeature ? resourcePath(activeFeature.moduleId, activeFeature.featureIndex) : null;
      if (module && feature && endpoint) {
        void loadFeatureData(module, feature, activeFeature.featureIndex, endpoint);
      }
    } catch (error) {
      approvalButton.disabled = false;
      showToast(error.message);
    }
    return;
  }

  const letterDecisionButton = target.closest("[data-letter-decision]");
  if (letterDecisionButton) {
    const decision = letterDecisionButton.dataset.letterDecision;
    const label = decision === "setuju" ? "menyetujui" : "menolak";
    if (!window.confirm(`Apakah Anda yakin ${label} pengajuan surat ini?`)) {
      return;
    }
    letterDecisionButton.disabled = true;
    try {
      const result = await window.RtrwApi.request(`/letters/${letterDecisionButton.dataset.letterId}/decision`, {
        method: "POST",
        body: { keputusan: decision }
      });
      showToast(result.message || "Keputusan surat berhasil disimpan.");
      const module = modules.find((item) => item.id === activeFeature?.moduleId);
      const feature = module?.features[activeFeature?.featureIndex];
      const endpoint = activeFeature ? resourcePath(activeFeature.moduleId, activeFeature.featureIndex) : null;
      if (module && feature && endpoint) {
        void loadFeatureData(module, feature, activeFeature.featureIndex, endpoint);
      }
    } catch (error) {
      letterDecisionButton.disabled = false;
      showToast(error.message);
    }
    return;
  }

  if (target.closest("#show-register")) {
    document.querySelector("#login-form").hidden = true;
    document.querySelector("#login-footnote").hidden = true;
    document.querySelector(".demo-callout").hidden = true;
    document.querySelector("#register-form").hidden = false;
    document.querySelector("#login-eyebrow").textContent = "Akses warga";
    document.querySelector("#login-title").textContent = "Buat akun";
    document.querySelector("#login-intro").textContent = "Isi data akun menggunakan nomor telepon yang sudah terdaftar sebagai warga.";
    return;
  }

  if (target.closest("#show-login")) {
    document.querySelector("#register-form").hidden = true;
    document.querySelector("#login-form").hidden = false;
    document.querySelector("#login-footnote").hidden = false;
    document.querySelector(".demo-callout").hidden = false;
    document.querySelector("#login-eyebrow").textContent = "Selamat datang kembali";
    document.querySelector("#login-title").textContent = "Masuk";
    document.querySelector("#login-intro").textContent = "Gunakan email atau nomor telepon yang terdaftar.";
    return;
  }

  if (target.closest("[data-theme-toggle]")) {
    setTheme(document.body.dataset.theme === "dark" ? "light" : "dark");
    return;
  }

  if (target.closest("#notification-toggle")) {
    const popover = document.querySelector("#notification-popover");
    const isOpen = !popover.hidden;
    popover.hidden = isOpen;
    document.querySelector("#notification-toggle").setAttribute("aria-expanded", String(!isOpen));
    return;
  }

  if (target.closest("#mobile-menu-toggle")) {
    const isOpen = appShell.classList.toggle("sidebar-open");
    document.querySelector("#mobile-menu-toggle").setAttribute("aria-expanded", String(isOpen));
    return;
  }

  if (target.closest("#sidebar-backdrop")) {
    closeMobileMenu();
    return;
  }

  if (target.closest("#forgot-password")) {
    showToast("Gunakan menu Akun setelah masuk untuk mengganti kata sandi.");
    return;
  }

  if (target.closest("#logout-button")) {
    void logoutFromApp();
    return;
  }

  if (!target.closest(".notification-wrap")) {
    const popover = document.querySelector("#notification-popover");
    popover.hidden = true;
    document.querySelector("#notification-toggle").setAttribute("aria-expanded", "false");
  }
});

document.querySelector("#login-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const submitButton = form.querySelector(".login-submit");
  const identity = form.elements.identity.value.trim();
  const password = form.elements.password.value;

  submitButton.disabled = true;
  submitButton.textContent = "Memeriksa...";
  try {
    const response = await window.RtrwApi.request("/auth/login", {
      method: "POST",
      body: { identity, password }
    });
    await openApiDashboard(response.user);
  } catch (error) {
    showToast(error.message);
  } finally {
    submitButton.disabled = false;
    submitButton.textContent = "Masuk";
  }
});

document.querySelector("#register-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const submitButton = form.querySelector("button[type=submit]");
  submitButton.disabled = true;
  submitButton.textContent = "Mengirim...";
  try {
    const result = await window.RtrwApi.request("/auth/register", {
      method: "POST",
      body: {
        name: form.elements.name.value.trim(),
        email: form.elements.email.value.trim(),
        phone: form.elements.phone.value.trim(),
        password: form.elements.password.value
      }
    });
    form.reset();
    document.querySelector("#show-login").click();
    showToast(result.message || "Pendaftaran terkirim dan menunggu persetujuan.");
  } catch (error) {
    showToast(error.message);
  } finally {
    submitButton.disabled = false;
    submitButton.textContent = "Daftar akun";
  }
});

document.querySelector("#demo-login").addEventListener("click", openDemoDashboard);

document.querySelector("#toggle-password").addEventListener("click", (event) => {
  const passwordInput = document.querySelector("#login-password");
  const showPassword = passwordInput.type === "password";
  passwordInput.type = showPassword ? "text" : "password";
  event.currentTarget.textContent = showPassword ? "Sembunyikan" : "Tampilkan";
  event.currentTarget.setAttribute("aria-label", `${showPassword ? "Sembunyikan" : "Tampilkan"} kata sandi`);
});

globalSearch.addEventListener("input", () => {
  if (!appShell.hidden) {
    renderPage();
  }
});

pageContent.addEventListener("input", (event) => {
  if (event.target instanceof HTMLInputElement && event.target.id === "resource-search") {
    const container = document.querySelector("#resource-data");
    const module = modules.find((item) => item.id === activeFeature?.moduleId);
    const feature = module?.features[activeFeature.featureIndex];
    const endpoint = activeFeature ? resourcePath(activeFeature.moduleId, activeFeature.featureIndex) : null;
    if (container && module && feature && endpoint) {
      container.innerHTML = renderWorkspaceData(module, feature, endpoint);
    }
  }
});

pageContent.addEventListener("submit", async (event) => {
  const form = event.target;
  if (!(form instanceof HTMLFormElement)) {
    return;
  }
  event.preventDefault();

  if (form.id === "letter-request-form") {
    const submitButton = form.querySelector("button[type=submit]");
    submitButton.disabled = true;
    try {
      const result = await window.RtrwApi.request("/letters", {
        method: "POST",
        body: {
          jenisSuratId: Number(form.elements.jenisSuratId.value),
          keperluan: form.elements.keperluan.value.trim()
        }
      });
      showToast(result.message || "Pengajuan surat berhasil dikirim.");
      form.reset();
      const module = modules.find((item) => item.id === activeFeature?.moduleId);
      const feature = module?.features[activeFeature?.featureIndex];
      const endpoint = activeFeature ? resourcePath(activeFeature.moduleId, activeFeature.featureIndex) : null;
      if (module && feature && endpoint) {
        void loadFeatureData(module, feature, activeFeature.featureIndex, endpoint);
      }
    } catch (error) {
      showToast(error.message);
    } finally {
      submitButton.disabled = false;
    }
    return;
  }

  if (form.id === "profile-form") {
    const submitButton = form.querySelector("button[type=submit]");
    submitButton.disabled = true;
    try {
      const result = await window.RtrwApi.request("/auth/profile", {
        method: "PATCH",
        body: {
          name: form.elements.name.value.trim(),
          email: form.elements.email.value.trim(),
          phone: form.elements.phone.value.trim()
        }
      });
      currentUser = { ...currentUser, ...result.user };
      updateUserDisplay();
      showToast("Profil berhasil diperbarui.");
      renderAccountSettings();
    } catch (error) {
      showToast(error.message);
    } finally {
      submitButton.disabled = false;
    }
    return;
  }

  if (form.id === "password-form") {
    const submitButton = form.querySelector("button[type=submit]");
    if (form.elements.newPassword.value !== form.elements.confirmPassword.value) {
      showToast("Konfirmasi kata sandi baru tidak sama.");
      return;
    }
    submitButton.disabled = true;
    try {
      const result = await window.RtrwApi.request("/auth/password", {
        method: "POST",
        body: {
          currentPassword: form.elements.currentPassword.value,
          newPassword: form.elements.newPassword.value
        }
      });
      form.reset();
      showToast(result.message || "Kata sandi berhasil diperbarui.");
    } catch (error) {
      showToast(error.message);
    } finally {
      submitButton.disabled = false;
    }
  }
});

document.addEventListener("keydown", (event) => {
  if (event.key === "/" && !appShell.hidden && !(event.target instanceof HTMLInputElement)) {
    event.preventDefault();
    globalSearch.focus();
  }

  if (event.key === "Escape") {
    closeMobileMenu();
    const popover = document.querySelector("#notification-popover");
    popover.hidden = true;
    document.querySelector("#notification-toggle").setAttribute("aria-expanded", "false");
  }
});

initializeTheme();
void restoreSession();
