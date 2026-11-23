package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.api.ApiData;

import javax.swing.JOptionPane;
import java.util.LinkedHashMap;
import java.util.Map;

final class PaymentVerificationPanel extends DataTablePanel {
    private static final long serialVersionUID = 1L;

    PaymentVerificationPanel(ApiClient api, SessionListener listener) {
        super(api, "Verifikasi Pembayaran", "Tinjau pembayaran yang menunggu verifikasi sesuai wilayah Anda.",
                (limit, offset, search) -> api.resource("pembayaran", limit, offset, search), listener);
        FlatButton reject = new FlatButton("Tolak", FlatButton.Kind.DANGER);
        FlatButton approve = new FlatButton("Setujui", FlatButton.Kind.PRIMARY);
        reject.addActionListener(event -> decide(false));
        approve.addActionListener(event -> decide(true));
        addAction(reject);
        addAction(approve);
    }

    private void decide(boolean approve) {
        Map<String, Object> row = selectedRow();
        Object value = row == null ? null : ApiData.firstValue(row, "id");
        if (!(value instanceof Number)) {
            JOptionPane.showMessageDialog(this, "Pilih pembayaran dengan ID yang valid terlebih dahulu.",
                    "Verifikasi Pembayaran", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String note = JOptionPane.showInputDialog(this,
                approve ? "Catatan verifikasi (opsional):" : "Alasan penolakan:",
                approve ? "Verifikasi Pembayaran" : "Tolak Pembayaran", JOptionPane.QUESTION_MESSAGE);
        if (note == null) {
            return;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("keputusan", approve ? "setuju" : "tolak");
        body.put("catatan", note.trim());
        if ("super_admin".equals(api.userRoleCode())) {
            Object organizationId = row.get("id_organisasi");
            if (!(organizationId instanceof Number) || ((Number) organizationId).longValue() < 1) {
                JOptionPane.showMessageDialog(this, "ID organisasi pada baris tidak tersedia.",
                        "Verifikasi Pembayaran", JOptionPane.ERROR_MESSAGE);
                return;
            }
            body.put("organizationId", ((Number) organizationId).longValue());
        }
        long id = ((Number) value).longValue();
        requestAsync(() -> api.decidePayment(id, body), this::refresh);
    }
}
