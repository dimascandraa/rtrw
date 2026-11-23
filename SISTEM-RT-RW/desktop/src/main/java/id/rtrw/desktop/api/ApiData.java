package id.rtrw.desktop.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pembantu membaca respons JSON dari Backend API.
 *
 * Format respons belum dikunci di documentation/API, jadi pembacaannya dibuat
 * toleran: daftar data boleh berada di akar, di "data", atau di "data.items".
 * Jika format final berbeda, cukup ubah kelas ini.
 */
public final class ApiData {

    private static final String[] WRAPPER_KEYS = {"data", "items", "rows", "results", "records", "list"};
    private static final String[] MESSAGE_KEYS = {"message", "pesan", "error", "msg"};

    private ApiData() {
    }

    /** Mengambil daftar baris (objek) dari respons JSON. */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> rows(Object json) {
        Object node = json;
        for (int depth = 0; depth < 4 && node instanceof Map; depth++) {
            Map<String, Object> map = (Map<String, Object>) node;
            Object next = null;
            for (String key : WRAPPER_KEYS) {
                Object candidate = map.get(key);
                if (candidate instanceof List || candidate instanceof Map) {
                    next = candidate;
                    break;
                }
            }
            if (next == null) {
                for (Object v : map.values()) {
                    if (v instanceof List) {
                        next = v;
                        break;
                    }
                }
            }
            if (next == null) {
                break;
            }
            node = next;
        }

        List<Map<String, Object>> result = new ArrayList<>();
        if (node instanceof List) {
            for (Object item : (List<Object>) node) {
                if (item instanceof Map) {
                    result.add((Map<String, Object>) item);
                } else {
                    Map<String, Object> wrapped = new LinkedHashMap<>();
                    wrapped.put("nilai", item);
                    result.add(wrapped);
                }
            }
        } else if (node instanceof Map) {
            result.add((Map<String, Object>) node);
        }
        return result;
    }

    /** Pesan dari server (message/pesan/error), atau null. */
    public static String message(Object json) {
        if (json instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) json;
            for (String key : MESSAGE_KEYS) {
                Object v = map.get(key);
                if (v instanceof String && !((String) v).trim().isEmpty()) {
                    return ((String) v).trim();
                }
            }
            Object error = map.get("error");
            if (error instanceof Map) {
                return message(error);
            }
        }
        return null;
    }

    /** True bila respons berbentuk {"success": false} atau {"status": "error"}. */
    public static boolean isFailure(Object json) {
        if (!(json instanceof Map)) {
            return false;
        }
        Map<?, ?> map = (Map<?, ?>) json;
        if (Boolean.FALSE.equals(map.get("success"))) {
            return true;
        }
        Object status = map.get("status");
        return status instanceof String && "error".equalsIgnoreCase((String) status);
    }

    /** Mencari string pada akar respons atau di dalam "data". */
    public static String findString(Object json, String... keys) {
        if (!(json instanceof Map)) {
            return null;
        }
        Map<?, ?> map = (Map<?, ?>) json;
        for (String key : keys) {
            Object v = map.get(key);
            if (v instanceof String && !((String) v).isEmpty()) {
                return (String) v;
            }
        }
        Object data = map.get("data");
        return data instanceof Map ? findString(data, keys) : null;
    }

    /** Mencari objek pada akar respons atau di dalam "data". */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> findMap(Object json, String... keys) {
        if (!(json instanceof Map)) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = (Map<String, Object>) json;
        for (String key : keys) {
            Object v = map.get(key);
            if (v instanceof Map) {
                return (Map<String, Object>) v;
            }
        }
        Object data = map.get("data");
        return data instanceof Map ? findMap(data, keys) : Collections.<String, Object>emptyMap();
    }

    /** Nilai pertama yang ada dari beberapa kemungkinan nama kunci. */
    public static Object firstValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) {
                return map.get(key);
            }
        }
        return null;
    }

    /** Membaca angka dari respons atau objek di dalam "data". */
    public static Integer findInteger(Object json, String key) {
        if (!(json instanceof Map)) {
            return null;
        }
        Map<?, ?> map = (Map<?, ?>) json;
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.valueOf((String) value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        Object data = map.get("data");
        return data instanceof Map ? findInteger(data, key) : null;
    }

    /** Teks yang ditampilkan di tabel untuk sebuah nilai JSON. */
    public static String display(Object v) {
        if (v == null) {
            return "";
        }
        if (v instanceof Boolean) {
            return ((Boolean) v) ? "Ya" : "Tidak";
        }
        if (v instanceof Double) {
            double d = (Double) v;
            if (d == Math.rint(d) && Math.abs(d) < 1e15) {
                return Long.toString((long) d);
            }
            return BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
        }
        if (v instanceof Map || v instanceof List) {
            return Json.stringify(v);
        }
        return v.toString();
    }
}
