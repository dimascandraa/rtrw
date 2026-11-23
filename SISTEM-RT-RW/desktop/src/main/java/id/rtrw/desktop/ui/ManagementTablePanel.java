package id.rtrw.desktop.ui;

import id.rtrw.desktop.api.ApiClient;
import id.rtrw.desktop.api.ApiData;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;

final class ManagementTablePanel extends DataTablePanel {
    private static final long serialVersionUID = 1L;

    enum Kind {
        FAMILIES("keluarga", "Data Keluarga"),
        RESIDENTS("warga", "Data Warga"),
        INVOICES("tagihan", "Tagihan"),
        ANNOUNCEMENTS("pengumuman", "Pengumuman"),
        ACTIVITIES("kegiatan", "Kegiatan");

        final String resource;
        final String title;

        Kind(String resource, String title) {
            this.resource = resource;
            this.title = title;
        }
    }

    private final Kind kind;

    ManagementTablePanel(ApiClient api, Kind kind, SessionListener listener) {
        super(api, kind.title, "Data sesuai izin dan wilayah akun Anda.",
                (limit, offset, search) -> api.resource(kind.resource, limit, offset, search), listener);
        this.kind = kind;
        addPermissionActions(api);
    }

    private void addPermissionActions(ApiClient api) {
        String module = permissionModule();
        if (api.hasPermission(module + ".create")) {
            FlatButton add = new FlatButton("Tambah", FlatButton.Kind.PRIMARY);
            add.addActionListener(event -> createRecord());
            addAction(add);
        }
        if (api.hasPermission(module + ".update")) {
            FlatButton edit = new FlatButton("Ubah", FlatButton.Kind.NEUTRAL);
            edit.addActionListener(event -> updateRecord());
            addAction(edit);
        }
        if (api.hasPermission(module + ".delete")) {
            FlatButton delete = new FlatButton(deleteLabel(), FlatButton.Kind.DANGER);
            delete.addActionListener(event -> deleteRecord());
            addAction(delete);
        }
    }

    private String permissionModule() {
        switch (kind) {
            case FAMILIES:
                return "keluarga";
            case RESIDENTS:
                return "warga";
            case INVOICES:
                return "tagihan";
            case ANNOUNCEMENTS:
                return "pengumuman";
            case ACTIVITIES:
                return "kegiatan";
            default:
                throw new IllegalStateException("Unsupported management resource.");
        }
    }

    private String deleteLabel() {
        switch (kind) {
            case INVOICES:
                return "Batalkan";
            case RESIDENTS:
            case FAMILIES:
                return "Arsipkan";
            default:
                return "Hapus";
        }
    }

    private void createRecord() {
        Form form = new Form();
        addOrganizationField(form, null);
        switch (kind) {
            case FAMILIES:
                form.text("rtId", "ID RT (lihat menu Wilayah)", "");
                form.text("familyNumber", "Nomor KK (16 digit)", "");
                form.text("address", "Alamat", "");
                form.text("houseNumber", "Nomor rumah", "");
                form.combo("housingType", "Jenis hunian", new String[] {
                        "milik_sendiri", "kontrak", "kos", "kosong"
                }, "milik_sendiri");
                break;
            case RESIDENTS:
                form.text("familyId", "ID keluarga (lihat Data Keluarga)", "");
                form.text("nik", "NIK (16 digit)", "");
                form.text("name", "Nama lengkap", "");
                form.combo("sex", "Jenis kelamin", new String[] {"L", "P"}, "L");
                form.combo("relationship", "Hubungan keluarga", new String[] {
                        "kepala_keluarga", "suami", "istri", "anak", "orang_tua", "famili_lain", "lainnya"
                }, "lainnya");
                form.text("birthPlace", "Tempat lahir", "");
                form.text("birthDate", "Tanggal lahir (YYYY-MM-DD)", "");
                form.text("occupation", "Pekerjaan", "");
                form.text("phone", "Nomor telepon", "");
                form.combo("livingStatus", "Status tinggal", new String[] {
                        "tetap", "kontrak", "kos", "sementara"
                }, "tetap");
                form.combo("status", "Status warga", new String[] {
                        "aktif", "pindah", "meninggal", "nonaktif"
                }, "aktif");
                break;
            case INVOICES:
                form.text("familyId", "ID keluarga", "");
                form.text("periodId", "ID periode tagihan", "");
                form.text("categoryId", "ID kategori iuran", "");
                form.text("amount", "Nominal", "");
                form.text("dueDate", "Jatuh tempo (YYYY-MM-DD)", "");
                form.text("description", "Keterangan", "");
                break;
            case ANNOUNCEMENTS:
                contentFields(form, null);
                break;
            case ACTIVITIES:
                activityFields(form, null);
                break;
            default:
                return;
        }
        submitForm("Tambah " + kind.title, form, null);
    }

    private void updateRecord() {
        Map<String, Object> row = selectedRow();
        Long id = rowId(row);
        if (id == null) {
            warnSelect();
            return;
        }
        Form form = new Form();
        addOrganizationField(form, row);
        switch (kind) {
            case FAMILIES:
                form.text("address", "Alamat", text(row, "alamat"));
                form.text("houseNumber", "Nomor rumah", text(row, "nomor_rumah"));
                form.combo("housingType", "Jenis hunian", new String[] {
                        "milik_sendiri", "kontrak", "kos", "kosong"
                }, text(row, "jenis_hunian"));
                break;
            case RESIDENTS:
                form.text("name", "Nama lengkap", text(row, "nama"));
                form.text("phone", "Nomor telepon", text(row, "telepon"));
                form.combo("sex", "Jenis kelamin", new String[] {"L", "P"}, text(row, "jenis_kelamin"));
                form.combo("status", "Status warga", new String[] {
                        "aktif", "pindah", "meninggal", "nonaktif"
                }, text(row, "status"));
                break;
            case INVOICES:
                form.text("categoryId", "ID kategori iuran", "");
                form.text("amount", "Nominal", text(row, "total_nominal"));
                form.text("dueDate", "Jatuh tempo (YYYY-MM-DD)", text(row, "tanggal_jatuh_tempo"));
                form.text("description", "Keterangan", "");
                break;
            case ANNOUNCEMENTS:
                contentFields(form, row);
                break;
            case ACTIVITIES:
                activityFields(form, row);
                break;
            default:
                return;
        }
        submitForm("Ubah " + kind.title, form, id);
    }

    private void contentFields(Form form, Map<String, Object> row) {
        form.text("title", "Judul", text(row, "judul"));
        form.text("body", "Isi", text(row, "isi"));
        form.text("rtId", "ID RT (kosong = seluruh wilayah)", text(row, "id_rt"));
        form.text("publishAt", "Terbit (YYYY-MM-DD HH:MM, kosong = draf)",
                text(row, "dipublikasikan_pada"));
        form.text("expiresAt", "Kedaluwarsa (YYYY-MM-DD HH:MM)", text(row, "kedaluwarsa_pada"));
    }

    private void activityFields(Form form, Map<String, Object> row) {
        form.text("title", "Judul", text(row, "judul"));
        form.text("description", "Deskripsi", text(row, "deskripsi"));
        form.combo("type", "Jenis kegiatan", new String[] {
                "kerja_bakti", "rapat", "posyandu", "sosial", "lainnya"
        }, text(row, "jenis"));
        form.text("startAt", "Mulai (YYYY-MM-DD HH:MM)", text(row, "mulai_pada"));
        form.text("endAt", "Selesai (YYYY-MM-DD HH:MM)", text(row, "selesai_pada"));
        form.text("location", "Lokasi", text(row, "lokasi"));
        form.text("rtId", "ID RT (kosong = seluruh wilayah)", text(row, "id_rt"));
    }

    private void addOrganizationField(Form form, Map<String, Object> row) {
        if ("super_admin".equals(api.userRoleCode())) {
            form.text("organizationId", "ID organisasi", text(row, "id_organisasi"));
        }
    }

    private void submitForm(String title, Form form, Long id) {
        JScrollPane scroll = new JScrollPane(form.panel);
        scroll.setBorder(null);
        scroll.setPreferredSize(new java.awt.Dimension(560, Math.min(560, form.panel.getPreferredSize().height + 12)));
        if (JOptionPane.showConfirmDialog(this, scroll, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        Map<String, Object> body;
        try {
            body = form.values();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), title, JOptionPane.WARNING_MESSAGE);
            return;
        }
        DataTablePanel.ApiRequest request;
        if (id == null) {
            request = createRequest(body);
        } else {
            request = updateRequest(id, body);
        }
        requestAsync(request, () -> {
            setStatus(title + " berhasil.", false);
            refresh();
        });
    }

    private DataTablePanel.ApiRequest createRequest(Map<String, Object> body) {
        switch (kind) {
            case FAMILIES:
                return () -> api.createFamily(body);
            case RESIDENTS:
                return () -> api.createResident(body);
            case INVOICES:
                return () -> api.createInvoice(body);
            case ANNOUNCEMENTS:
                return () -> api.createAnnouncement(body);
            case ACTIVITIES:
                return () -> api.createActivity(body);
            default:
                throw new IllegalStateException("Unsupported management resource.");
        }
    }

    private DataTablePanel.ApiRequest updateRequest(long id, Map<String, Object> body) {
        switch (kind) {
            case FAMILIES:
                return () -> api.updateFamily(id, body);
            case RESIDENTS:
                return () -> api.updateResident(id, body);
            case INVOICES:
                return () -> api.updateInvoice(id, body);
            case ANNOUNCEMENTS:
                return () -> api.updateAnnouncement(id, body);
            case ACTIVITIES:
                return () -> api.updateActivity(id, body);
            default:
                throw new IllegalStateException("Unsupported management resource.");
        }
    }

    private void deleteRecord() {
        Map<String, Object> row = selectedRow();
        Long id = rowId(row);
        if (id == null) {
            warnSelect();
            return;
        }
        String warning = kind == Kind.RESIDENTS
                ? "\nWarga yang terhubung ke akun tidak dapat diarsipkan."
                : kind == Kind.INVOICES
                    ? "\nTagihan hanya dapat dibatalkan bila belum memiliki pembayaran."
                    : "";
        int answer = JOptionPane.showConfirmDialog(this,
                "Lanjutkan " + deleteLabel().toLowerCase() + " data terpilih?" + warning,
                deleteLabel() + " " + kind.title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        String path = "/api/v1/" + routeName() + "/" + id;
        if ("super_admin".equals(api.userRoleCode())) {
            Object organizationId = row.get("id_organisasi");
            if (!(organizationId instanceof Number) || ((Number) organizationId).longValue() < 1) {
                JOptionPane.showMessageDialog(this, "ID organisasi pada baris tidak tersedia.",
                        "Penghapusan dibatalkan", JOptionPane.ERROR_MESSAGE);
                return;
            }
            path += "?organizationId=" + ((Number) organizationId).longValue();
        }
        final String requestPath = path;
        requestAsync(() -> api.delete(requestPath), this::refresh);
    }

    private String routeName() {
        switch (kind) {
            case FAMILIES:
                return "families";
            case RESIDENTS:
                return "residents";
            case INVOICES:
                return "invoices";
            case ANNOUNCEMENTS:
                return "announcements";
            case ACTIVITIES:
                return "activities";
            default:
                throw new IllegalStateException("Unsupported management resource.");
        }
    }

    private static Long rowId(Map<String, Object> row) {
        Object value = row == null ? null : row.get("id");
        return value instanceof Number ? ((Number) value).longValue() : null;
    }

    private static String text(Map<String, Object> row, String key) {
        Object value = row == null ? null : row.get(key);
        return value == null ? "" : value.toString();
    }

    private void warnSelect() {
        JOptionPane.showMessageDialog(this, "Pilih baris data dengan ID yang valid terlebih dahulu.",
                kind.title, JOptionPane.WARNING_MESSAGE);
    }

    private static final class Form {
        private final JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        private final Map<String, JComponent> fields = new LinkedHashMap<>();

        void text(String key, String label, String value) {
            JTextField field = new JTextField(value == null ? "" : value, 20);
            UiKit.styleField(field);
            panel.add(UiKit.label(label, 12f, true, Theme.text()));
            panel.add(field);
            fields.put(key, field);
        }

        void combo(String key, String label, String[] options, String selected) {
            JComboBox<String> field = new JComboBox<>(options);
            if (selected != null && !selected.isEmpty()) {
                field.setSelectedItem(selected);
            }
            panel.add(UiKit.label(label, 12f, true, Theme.text()));
            panel.add(field);
            fields.put(key, field);
        }

        Map<String, Object> values() {
            Map<String, Object> values = new LinkedHashMap<>();
            for (Map.Entry<String, JComponent> entry : fields.entrySet()) {
                String value = entry.getValue() instanceof JTextField
                        ? ((JTextField) entry.getValue()).getText().trim()
                        : String.valueOf(((JComboBox<?>) entry.getValue()).getSelectedItem()).trim();
                if (("rtId".equals(entry.getKey()) && value.isEmpty())
                        || ("birthDate".equals(entry.getKey()) && value.isEmpty())) {
                    values.put(entry.getKey(), "");
                } else if (isIdentifier(entry.getKey()) && !value.isEmpty()) {
                    try {
                        long id = Long.parseLong(value);
                        if (id < 1) {
                            throw new NumberFormatException();
                        }
                        values.put(entry.getKey(), id);
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException(entry.getKey() + " harus berupa angka positif.");
                    }
                } else {
                    values.put(entry.getKey(), value);
                }
            }
            return values;
        }

        private static boolean isIdentifier(String key) {
            return key.equals("rtId") || key.equals("familyId") || key.equals("periodId")
                    || key.equals("categoryId") || key.equals("organizationId");
        }
    }
}
