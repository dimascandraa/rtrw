package id.rtrw.desktop.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser dan penulis JSON sederhana tanpa library tambahan.
 *
 * Hasil parse: Map (objek, urutan kunci dipertahankan), List (array), String,
 * Long atau Double (angka), Boolean, atau null.
 */
public final class Json {

    private Json() {
    }

    /** Kesalahan format JSON. */
    public static final class JsonException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public JsonException(String message) {
            super(message);
        }
    }

    public static Object parse(String text) {
        if (text == null) {
            throw new JsonException("JSON kosong");
        }
        Parser p = new Parser(text);
        p.skipWs();
        Object value = p.readValue();
        p.skipWs();
        if (!p.atEnd()) {
            throw p.error("Ada karakter tambahan setelah JSON");
        }
        return value;
    }

    public static String stringify(Object value) {
        StringBuilder sb = new StringBuilder();
        write(sb, value);
        return sb.toString();
    }

    // ------------------------------------------------------------------ penulis

    private static void write(StringBuilder sb, Object v) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String) {
            quote(sb, (String) v);
        } else if (v instanceof Boolean) {
            sb.append(v.toString());
        } else if (v instanceof Number) {
            writeNumber(sb, (Number) v);
        } else if (v instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                quote(sb, String.valueOf(e.getKey()));
                sb.append(':');
                write(sb, e.getValue());
            }
            sb.append('}');
        } else if (v instanceof Iterable) {
            sb.append('[');
            boolean first = true;
            for (Object item : (Iterable<?>) v) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                write(sb, item);
            }
            sb.append(']');
        } else {
            quote(sb, v.toString());
        }
    }

    private static void writeNumber(StringBuilder sb, Number n) {
        if (n instanceof Double || n instanceof Float) {
            double d = n.doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                sb.append("null");
            } else if (d == Math.rint(d) && Math.abs(d) < 1e15) {
                sb.append((long) d);
            } else {
                sb.append(d);
            }
        } else {
            sb.append(n.toString());
        }
    }

    private static void quote(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    // ------------------------------------------------------------------ parser

    private static final class Parser {
        private final String s;
        private int i;

        Parser(String s) {
            this.s = s;
        }

        boolean atEnd() {
            return i >= s.length();
        }

        JsonException error(String message) {
            return new JsonException(message + " (posisi " + i + ")");
        }

        void skipWs() {
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '﻿') {
                    i++;
                } else {
                    break;
                }
            }
        }

        Object readValue() {
            skipWs();
            if (atEnd()) {
                throw error("Data JSON berakhir tiba-tiba");
            }
            char c = s.charAt(i);
            switch (c) {
                case '{':
                    return readObject();
                case '[':
                    return readArray();
                case '"':
                    return readString();
                case 't':
                    expectWord("true");
                    return Boolean.TRUE;
                case 'f':
                    expectWord("false");
                    return Boolean.FALSE;
                case 'n':
                    expectWord("null");
                    return null;
                default:
                    if (c == '-' || (c >= '0' && c <= '9')) {
                        return readNumber();
                    }
                    throw error("Karakter tidak dikenal '" + c + "'");
            }
        }

        private void expectWord(String word) {
            if (!s.startsWith(word, i)) {
                throw error("Diharapkan '" + word + "'");
            }
            i += word.length();
        }

        private Map<String, Object> readObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            i++; // {
            skipWs();
            if (!atEnd() && s.charAt(i) == '}') {
                i++;
                return map;
            }
            while (true) {
                skipWs();
                if (atEnd() || s.charAt(i) != '"') {
                    throw error("Diharapkan nama kunci berupa string");
                }
                String key = readString();
                skipWs();
                if (atEnd() || s.charAt(i) != ':') {
                    throw error("Diharapkan ':'");
                }
                i++;
                map.put(key, readValue());
                skipWs();
                if (atEnd()) {
                    throw error("Objek JSON tidak ditutup");
                }
                char c = s.charAt(i++);
                if (c == ',') {
                    continue;
                }
                if (c == '}') {
                    return map;
                }
                throw error("Diharapkan ',' atau '}'");
            }
        }

        private List<Object> readArray() {
            List<Object> list = new ArrayList<>();
            i++; // [
            skipWs();
            if (!atEnd() && s.charAt(i) == ']') {
                i++;
                return list;
            }
            while (true) {
                list.add(readValue());
                skipWs();
                if (atEnd()) {
                    throw error("Array JSON tidak ditutup");
                }
                char c = s.charAt(i++);
                if (c == ',') {
                    continue;
                }
                if (c == ']') {
                    return list;
                }
                throw error("Diharapkan ',' atau ']'");
            }
        }

        private String readString() {
            StringBuilder sb = new StringBuilder();
            i++; // "
            while (true) {
                if (atEnd()) {
                    throw error("String JSON tidak ditutup");
                }
                char c = s.charAt(i++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                if (atEnd()) {
                    throw error("Escape tidak lengkap");
                }
                char e = s.charAt(i++);
                switch (e) {
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case '/':
                        sb.append('/');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'u':
                        if (i + 4 > s.length()) {
                            throw error("Escape \\u tidak lengkap");
                        }
                        try {
                            sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        } catch (NumberFormatException ex) {
                            throw error("Escape \\u tidak valid");
                        }
                        i += 4;
                        break;
                    default:
                        throw error("Escape tidak dikenal '\\" + e + "'");
                }
            }
        }

        private Object readNumber() {
            int start = i;
            while (i < s.length()) {
                char c = s.charAt(i);
                if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
                    i++;
                } else {
                    break;
                }
            }
            String token = s.substring(start, i);
            try {
                if (token.indexOf('.') >= 0 || token.indexOf('e') >= 0 || token.indexOf('E') >= 0) {
                    return Double.valueOf(token);
                }
                try {
                    return Long.valueOf(token);
                } catch (NumberFormatException tooBig) {
                    return Double.valueOf(token);
                }
            } catch (NumberFormatException ex) {
                throw error("Angka tidak valid '" + token + "'");
            }
        }
    }
}
