package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.api.ApiData;

import javax.swing.JOptionPane;
import java.util.Map;

final class RegistrationApprovalPanel extends DataTablePanel {
    private static final long serialVersionUID = 1L;

    RegistrationApprovalPanel(ApiClient api, SessionListener listener) {
        super(api, "Verifikasi Akun", "Pendaftaran yang menunggu persetujuan di wilayah Anda.",
                api::pendingRegistrations, listener);
        FlatButton approve = new FlatButton("Setujui Warga", FlatButton.Kind.PRIMARY);
        approve.addActionListener(e -> approveSelected());
        addAction(approve);
    }

    private void approveSelected() {
        Map<String, Object> row = selectedRow();
        Object value = row == null ? null : ApiData.firstValue(row, "id");
        if (!(value instanceof Number)) {
            JOptionPane.showMessageDialog(this, "Pilih pendaftaran dengan ID yang valid terlebih dahulu.",
                    "Verifikasi Akun", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long id = ((Number) value).longValue();
        int answer = JOptionPane.showConfirmDialog(this,
                "Setujui pendaftaran ini sebagai akun Warga?", "Setujui Akun",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            requestAsync(() -> api.approveRegistration(id), this::refresh);
        }
    }
}
