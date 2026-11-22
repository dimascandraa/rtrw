package id.rtrw.desktop.util;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Ekspor tabel ke CSV. Memakai pemisah titik koma dan BOM UTF-8 supaya langsung
 * terbaca benar di Excel dengan pengaturan regional Indonesia.
 */
public final class CsvExporter {

    private static final char SEPARATOR = ';';

    private CsvExporter() {
    }

    public static void write(Path file, List<String> headers, List<List<String>> rows) throws IOException {
        try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write('﻿');
            writeLine(w, headers);
            for (List<String> row : rows) {
                writeLine(w, row);
            }
        }
    }

    static String escape(String value) {
        String v = value == null ? "" : value;
        // Cegah rumus Excel berbahaya (CSV injection) pada sel yang diawali = + - @
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0 && !isPlainNumber(v)) {
            v = "'" + v;
        }
        boolean quote = v.indexOf(SEPARATOR) >= 0 || v.indexOf('"') >= 0
                || v.indexOf('\n') >= 0 || v.indexOf('\r') >= 0;
        if (quote) {
            return '"' + v.replace("\"", "\"\"") + '"';
        }
        return v;
    }

    private static boolean isPlainNumber(String v) {
        try {
            Double.parseDouble(v);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void writeLine(Writer w, List<String> cells) throws IOException {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                w.write(SEPARATOR);
            }
            w.write(escape(cells.get(i)));
        }
        w.write("\r\n");
    }

    /** Untuk pengujian. */
    public static String escapeForTest(String value) {
        return escape(value);
    }
}
