package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.api.ApiData;

import javax.swing.JOptionPane;
import java.util.LinkedHashMap;
import java.util.Map;

final class LetterDecisionPanel extends DataTablePanel {
    private static final long serialVersionUID = 1L;

    LetterDecisionPanel(ApiClient api, SessionListener listener) {
        super(api, "Persetujuan Surat", "Pengajuan surat sesuai peran dan wilayah Anda.",
                (limit, offset, search) -> api.resource("pengajuan-surat", limit, offset, search), listener);
        FlatButton reject = new FlatButton("Tolak", FlatButton.Kind.DANGER);
        FlatButton approve = new FlatButton("Setujui", FlatButton.Kind.PRIMARY);
        reject.addActionListener(e -> decide(false));
        approve.addActionListener(e -> decide(true));
        addAction(reject);
        addAction(approve);
    }

    private void decide(boolean approve) {
        Map<String, Object> row = selectedRow();
        Object value = row == null ? null : ApiData.firstValue(row, "id");
        if (!(value instanceof Number)) {
            JOptionPane.showMessageDialog(this, "Pilih pengajuan surat dengan ID yang valid terlebih dahulu.",
                    "Persetujuan Surat", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String note = JOptionPane.showInputDialog(this,
                approve ? "Catatan persetujuan (opsional):" : "Alasan penolakan:",
                approve ? "Setujui Surat" : "Tolak Surat", JOptionPane.QUESTION_MESSAGE);
        if (note == null) {
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("keputusan", approve ? "setuju" : "tolak");
        body.put("catatan", note.trim());
        long id = ((Number) value).longValue();
        requestAsync(() -> api.decideLetter(id, body), this::refresh);
    }
}
