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
const dialog = document.querySelector("#feature-dialog");
const toast = document.querySelector("#toast");
let activePage = "home";
let toastTimeout;

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
  const moduleLinks = modules.map((module) => `
    <button class="nav-link${activePage === module.id ? " active" : ""}" type="button" data-page="${module.id}"${activePage === module.id ? ' aria-current="page"' : ""}>
      <span class="nav-icon" aria-hidden="true">${module.short}</span>
      <span class="nav-label">${module.title}</span>
      <span class="nav-count">${module.features.length}</span>
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
}

function renderModuleCards(query = "") {
  const normalizedQuery = query.trim().toLocaleLowerCase("id-ID");
  const visibleModules = modules.filter((module) =>
    `${module.title} ${module.description} ${module.features.map((feature) => feature.title).join(" ")}`
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
      <span class="module-card-bottom"><span>${module.features.length} fitur</span><span>Buka modul</span></span>
    </button>
  `).join("");
}

function renderHome() {
  const query = globalSearch.value;
  const filteredModules = modules.filter((module) =>
    `${module.title} ${module.description} ${module.features.map((feature) => feature.title).join(" ")}`
      .toLocaleLowerCase("id-ID")
      .includes(query.trim().toLocaleLowerCase("id-ID"))
  );
  const moduleContent = renderModuleCards(query);
  const moduleDescription = query.trim()
    ? `${filteredModules.length} dari ${modules.length} modul ditemukan.`
    : `Pilih menu untuk mulai mengelola lingkungan ${escapeHtml(currentUser.region || "Anda")}.`;
  const periodName = dashboardData.currentPeriodName || "Periode berjalan";
  const cashNote = isConnectedToApi ? "Saldo berdasarkan transaksi kas" : "Akumulasi transaksi kas demo";

  pageContent.innerHTML = `
    <section class="page-heading">
      <div>
        <p class="eyebrow">Ringkasan lingkungan</p>
        <h1>Selamat datang, ${escapeHtml((currentUser.name || "Pengguna").split(" ")[0])}!</h1>
        <p>${moduleDescription}</p>
      </div>
      <span class="heading-tag"><span class="demo-dot" aria-hidden="true"></span>${escapeHtml(dashboardData.organizationName)} · ${escapeHtml(dashboardData.region)}</span>
    </section>

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

    <section aria-labelledby="modules-title">
      <div class="section-heading">
        <div><h2 id="modules-title">Jelajahi modul</h2><p>Semua kebutuhan pengelolaan lingkungan dalam satu tempat.</p></div>
        <span class="heading-tag">${modules.length} modul · 26 fitur</span>
      </div>
      <div class="module-grid">${moduleContent}</div>
    </section>

    ${query.trim() ? "" : `
      <section class="home-lower-grid" aria-label="Informasi terbaru">
        <article class="panel">
          <div class="panel-heading"><div><h2>Informasi lingkungan</h2><p>Pengumuman terbaru dari pengurus</p></div><span class="heading-tag">${announcements.length} aktif</span></div>
          <div class="announcement-list">${renderAnnouncements()}</div>
        </article>
        <article class="panel">
          <div class="panel-heading"><div><h2>Aktivitas terbaru</h2><p>Ringkasan dari log aktivitas</p></div></div>
          <div class="activity-list">${renderActivities()}</div>
        </article>
      </section>
    `}
  `;
}

function renderFeatureCards(module, query = "") {
  const normalizedQuery = query.trim().toLocaleLowerCase("id-ID");
  const visibleFeatures = module.features.filter((feature) =>
    `${feature.title} ${feature.description} ${feature.tables.join(" ")}`
      .toLocaleLowerCase("id-ID")
      .includes(normalizedQuery)
  );

  if (!visibleFeatures.length) {
    return '<div class="empty-state">Tidak ada fitur dalam modul ini yang cocok dengan pencarian.</div>';
  }

  return visibleFeatures.map((feature, index) => `
    <button class="feature-card" type="button" data-feature-module="${module.id}" data-feature-index="${module.features.indexOf(feature)}" aria-label="Lihat pratinjau ${feature.title}">
      <span class="feature-icon" aria-hidden="true">${String(index + 1).padStart(2, "0")}</span>
      <h3>${feature.title}</h3>
      <p>${feature.description}</p>
      <span class="feature-card-footer"><span class="button button-secondary">Lihat tampilan</span><span class="feature-arrow" aria-hidden="true">→</span></span>
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
  const matchingFeatures = module.features.filter((feature) =>
    `${feature.title} ${feature.description} ${feature.tables.join(" ")}`
      .toLocaleLowerCase("id-ID")
      .includes(query.trim().toLocaleLowerCase("id-ID"))
  );
  const resultDescription = query.trim()
    ? `${matchingFeatures.length} dari ${module.features.length} fitur cocok dengan pencarian.`
    : module.description;

  pageContent.innerHTML = `
    <section class="page-heading">
      <div><p class="eyebrow">${escapeHtml(currentUser.role)} · ${escapeHtml(currentUser.region)}</p><h1>${module.title}</h1><p>${resultDescription}</p></div>
      <span class="heading-tag"><span class="demo-dot" aria-hidden="true"></span>${module.features.length} fitur tersedia</span>
    </section>
    ${module.id === "reports" && !query.trim() ? renderReportPreview() : ""}
    <section aria-label="Daftar fitur ${module.title}">
      <div class="section-heading"><div><h2>Fitur modul</h2><p>Pilih fitur untuk melihat pratinjau dan tabel terkait.</p></div></div>
      <div class="feature-grid">${renderFeatureCards(module, query)}</div>
    </section>
  `;
}

function renderPage() {
  renderNavigation();
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

function openFeaturePreview(moduleId, featureIndex) {
  const module = modules.find((item) => item.id === moduleId);
  const feature = module?.features[featureIndex];
  if (!module || !feature) {
    return;
  }

  document.querySelector("#dialog-icon").textContent = module.short;
  document.querySelector("#dialog-title").textContent = feature.title;
  document.querySelector("#dialog-description").textContent = feature.description;
  document.querySelector("#dialog-tables-list").innerHTML = feature.tables
    .map((table) => `<span class="table-tag">${table}</span>`)
    .join("");
  dialog.showModal();
}

function showDashboard() {
  loginScreen.hidden = true;
  appShell.hidden = false;
  activePage = "home";
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
    window.RtrwApi.request("/dashboard/summary"),
    window.RtrwApi.request("/announcements?limit=5"),
    window.RtrwApi.request("/notifications?limit=5")
  ]);
  const organizationName = summary.organization.name || user.organizationName || "Semua organisasi";
  const regionParts = [];
  if (summary.organization.rtNumber) {
    regionParts.push(`RT ${summary.organization.rtNumber}`);
  }
  if (summary.organization.rwNumber) {
    regionParts.push(`RW ${summary.organization.rwNumber}`);
  }
  const region = user.region || regionParts.join(" / ") || "Semua wilayah";

  currentUser = { ...user, organizationName, region };
  dashboardData = {
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
    pendingPayments: summary.period.pendingPayments
  };
  announcements = announcementResponse.items;
  activities = summary.activities || [];
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

document.addEventListener("click", (event) => {
  const target = event.target;
  if (!(target instanceof Element)) {
    return;
  }

  const pageButton = target.closest("[data-page]");
  if (pageButton) {
    activePage = pageButton.dataset.page;
    globalSearch.value = "";
    renderPage();
    closeMobileMenu();
    return;
  }

  const featureButton = target.closest("[data-feature-module]");
  if (featureButton) {
    openFeaturePreview(featureButton.dataset.featureModule, Number(featureButton.dataset.featureIndex));
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

  if (target.closest("#dialog-close") || target.closest("#dialog-done")) {
    dialog.close();
    return;
  }

  if (target.closest("#forgot-password")) {
    showToast("Reset kata sandi belum tersedia. Silakan hubungi pengurus RT/RW.");
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

document.querySelector("#demo-login").addEventListener("click", openDemoDashboard);

document.querySelector("#toggle-password").addEventListener("click", (event) => {
  const passwordInput = document.querySelector("#login-password");
  const showPassword = passwordInput.type === "password";
  passwordInput.type = showPassword ? "text" : "password";
  event.currentTarget.textContent = showPassword ? "Sembunyikan" : "Tampilkan";
  event.currentTarget.setAttribute("aria-label", `${showPassword ? "Sembunyikan" : "Tampilkan"} kata sandi`);
});

dialog.addEventListener("click", (event) => {
  if (event.target === dialog) {
    dialog.close();
  }
});

globalSearch.addEventListener("input", () => {
  if (!appShell.hidden) {
    renderPage();
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
