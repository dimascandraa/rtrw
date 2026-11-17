package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.api.ApiData;

import javax.swing.JOptionPane;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pembayaran (F-010, F-011): daftar pembayaran dan verifikasi bukti oleh
 * bendahara lewat POST /api/payments/verify.
 *
 * Asumsi yang perlu dicocokkan dengan dokumentasi API:
 *  - daftar pembayaran tersedia di GET /api/payments
 *  - body verifikasi: {"id": ..., "status": "diterima" | "ditolak", "catatan": "..."}
 */
final class PaymentsPanel extends DataTablePanel {
    private static final long serialVersionUID = 1L;

    private static final String STATUS_ACCEPT = "diterima";
    private static final String STATUS_REJECT = "ditolak";

    PaymentsPanel(ApiClient api, SessionListener listener) {
        super(api, "Pembayaran", "Daftar pembayaran dan verifikasi bukti oleh bendahara.",
                "/api/payments", listener);
        FlatButton reject = new FlatButton("Tolak", FlatButton.Kind.DANGER);
        FlatButton accept = new FlatButton("Terima", FlatButton.Kind.PRIMARY);
        reject.addActionListener(e -> verify(false));
        accept.addActionListener(e -> verify(true));
        addAction(reject);
        addAction(accept);
    }

    private void verify(boolean accept) {
        Map<String, Object> row = selectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Pilih satu pembayaran pada tabel terlebih dahulu.",
                    "Verifikasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object id = rowId(row);
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Baris ini tidak memiliki kolom id.", "Verifikasi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String note = null;
        if (accept) {
            int ok = JOptionPane.showConfirmDialog(this, "Terima pembayaran ini sebagai sah?",
                    "Terima Pembayaran", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) {
                return;
            }
        } else {
            note = JOptionPane.showInputDialog(this, "Alasan penolakan (boleh dikosongkan):",
                    "Tolak Pembayaran", JOptionPane.QUESTION_MESSAGE);
            if (note == null) {
                return; // dibatalkan
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", id);
        body.put("status", accept ? STATUS_ACCEPT : STATUS_REJECT);
        if (note != null && !note.trim().isEmpty()) {
            body.put("catatan", note.trim());
        }
        postAsync("/api/payments/verify", body, this::refresh);
    }

    /** Kunci baris: kolom "id", atau "id_pembayaran". */
    static Object rowId(Map<String, Object> row) {
        return ApiData.firstValue(row, "id", "id_pembayaran");
    }
}
