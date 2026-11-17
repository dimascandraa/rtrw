package id.rtrw.desktop.ui;

/** Dipanggil saat server menolak sesi (HTTP 401) di tengah pemakaian. */
interface SessionListener {
    void onSessionExpired(String message);
}
