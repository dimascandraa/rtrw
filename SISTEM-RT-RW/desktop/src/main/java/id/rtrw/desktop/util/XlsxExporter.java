package id.rtrw.desktop.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Writes a minimal Office Open XML workbook without external dependencies. */
public final class XlsxExporter {
    private static final int MAX_ROWS = 1_048_576;
    private static final int MAX_COLUMNS = 16_384;

    private XlsxExporter() {
    }

    public static final class Sheet {
        private final String name;
        private final List<Map<String, Object>> rows;

        public Sheet(String name, List<Map<String, Object>> rows) {
            this.name = name;
            this.rows = rows;
        }
    }

    public static void write(Path file, List<Sheet> sheets) throws IOException {
        if (sheets == null || sheets.isEmpty() || sheets.size() > 255) {
            throw new IllegalArgumentException("Workbook must contain between 1 and 255 sheets.");
        }
        try (OutputStream output = Files.newOutputStream(file);
                ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            addEntry(zip, "[Content_Types].xml", contentTypes(sheets.size()));
            addEntry(zip, "_rels/.rels", rootRelationships());
            addEntry(zip, "xl/workbook.xml", workbook(sheets));
            addEntry(zip, "xl/_rels/workbook.xml.rels", workbookRelationships(sheets.size()));
            for (int i = 0; i < sheets.size(); i++) {
                Sheet sheet = sheets.get(i);
                if (sheet.rows.size() + 1 > MAX_ROWS) {
                    throw new IOException("Sheet exceeds the Excel row limit: " + sheet.name);
                }
                addEntry(zip, "xl/worksheets/sheet" + (i + 1) + ".xml", worksheet(sheet));
            }
        }
    }

    private static String contentTypes(int count) {
        StringBuilder xml = new StringBuilder(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>");
        for (int i = 1; i <= count; i++) {
            xml.append("<Override PartName=\"/xl/worksheets/sheet").append(i)
                    .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }
        return xml.append("</Types>").toString();
    }

    private static String rootRelationships() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
    }

    private static String workbook(List<Sheet> sheets) {
        StringBuilder xml = new StringBuilder(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets>");
        for (int i = 0; i < sheets.size(); i++) {
            xml.append("<sheet name=\"").append(escape(safeSheetName(sheets.get(i).name)))
                    .append("\" sheetId=\"").append(i + 1).append("\" r:id=\"rId").append(i + 1).append("\"/>");
        }
        return xml.append("</sheets></workbook>").toString();
    }

    private static String workbookRelationships(int count) {
        StringBuilder xml = new StringBuilder(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
        for (int i = 1; i <= count; i++) {
            xml.append("<Relationship Id=\"rId").append(i)
                    .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet")
                    .append(i).append(".xml\"/>");
        }
        return xml.append("</Relationships>").toString();
    }

    private static String worksheet(Sheet sheet) throws IOException {
        Set<String> columns = new LinkedHashSet<>();
        for (Map<String, Object> row : sheet.rows) {
            columns.addAll(row.keySet());
        }
        if (columns.isEmpty()) {
            columns.add("Data");
        }
        if (columns.size() > MAX_COLUMNS) {
            throw new IOException("Sheet exceeds the Excel column limit: " + sheet.name);
        }

        List<String> headers = new ArrayList<>(columns);
        StringBuilder xml = new StringBuilder(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        appendRow(xml, 1, headers, null);
        for (int i = 0; i < sheet.rows.size(); i++) {
            appendRow(xml, i + 2, headers, sheet.rows.get(i));
        }
        return xml.append("</sheetData></worksheet>").toString();
    }

    private static void appendRow(StringBuilder xml, int rowNumber, List<String> headers,
            Map<String, Object> values) {
        xml.append("<row r=\"").append(rowNumber).append("\">");
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            Object value = values == null ? header : values.get(header);
            String reference = columnName(i + 1) + rowNumber;
            if (value instanceof Number && isNumericColumn(header)) {
                xml.append("<c r=\"").append(reference).append("\"><v>")
                        .append(value).append("</v></c>");
            } else if (value != null && isNumericColumn(header) && isPlainNumber(value.toString())) {
                xml.append("<c r=\"").append(reference).append("\"><v>")
                        .append(value).append("</v></c>");
            } else {
                xml.append("<c r=\"").append(reference).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                        .append(escape(value == null ? "" : value.toString()))
                        .append("</t></is></c>");
            }
        }
        xml.append("</row>");
    }

    private static boolean isNumericColumn(String header) {
        String key = header.toLowerCase(java.util.Locale.ROOT);
        return key.contains("nominal") || key.contains("saldo") || key.contains("jumlah")
                || key.contains("total") || key.contains("ukuran");
    }

    private static boolean isPlainNumber(String value) {
        return value.matches("-?[0-9]+(?:\\.[0-9]+)?");
    }

    private static String columnName(int number) {
        StringBuilder name = new StringBuilder();
        while (number > 0) {
            int remainder = (number - 1) % 26;
            name.insert(0, (char) ('A' + remainder));
            number = (number - 1) / 26;
        }
        return name.toString();
    }

    private static String safeSheetName(String name) {
        String safe = name == null ? "Data" : name.replaceAll("[\\\\/?*\\[\\]:]", "_").trim();
        if (safe.isEmpty()) {
            safe = "Data";
        }
        return safe.length() > 31 ? safe.substring(0, 31) : safe;
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character == '\t' || character == '\n' || character == '\r' || character >= 0x20) {
                switch (character) {
                    case '&':
                        escaped.append("&amp;");
                        break;
                    case '<':
                        escaped.append("&lt;");
                        break;
                    case '>':
                        escaped.append("&gt;");
                        break;
                    case '"':
                        escaped.append("&quot;");
                        break;
                    case '\'':
                        escaped.append("&apos;");
                        break;
                    default:
                        escaped.append(character);
                }
            }
        }
        return escaped.toString();
    }

    private static void addEntry(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
