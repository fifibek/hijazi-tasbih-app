package com.muse.tasbih;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** نموذج بيانات الأذكار: يُحفظ كاملاً في SharedPreferences. */
public class TasbihData {

    public static class Item {
        public String name;
        public String meaning;
        public int target;
        public int count;

        Item(String name, String meaning, int target, int count) {
            this.name = name;
            this.meaning = meaning;
            this.target = target;
            this.count = count;
        }
    }

    public static final String PREFS = "tasbih_prefs_v2";
    public static final int BUILTIN_COUNT = 8;

    public static final String[] NAMES_AR = {
            "سبحان الله", "الحمد لله", "الله أكبر", "لا إله إلا الله",
            "أستغفر الله", "اللهم صلِّ على محمد", "سبحان الله وبحمده", "لا حول ولا قوة إلا بالله"
    };
    public static final String[] NAMES_EN = {
            "Subhan Allah", "Alhamdulillah", "Allahu Akbar", "La ilaha illa Allah",
            "Astaghfirullah", "Allahumma salli ala Muhammad", "Subhan Allah wa bihamdih",
            "La hawla wa la quwwata illa billah"
    };
    public static final String[] NAMES_FR = {
            "Subhan Allah", "Alhamdulillah", "Allahu Akbar", "La ilaha illa Allah",
            "Astaghfirullah", "Allahumma salli ala Muhammad", "Subhan Allah wa bihamdih",
            "La hawla wa la quwwata illa billah"
    };
    public static final String[] MEANINGS_AR = {
            "تنزيه الله عن كل نقص", "الثناء كله لله", "الله أعظم من كل شيء", "لا معبود بحق إلا الله",
            "طلب المغفرة من الله", "الصلاة على النبي ﷺ", "تنزيه الله مع حمده", "لا قدرة إلا بعون الله"
    };
    public static final String[] MEANINGS_EN = {
            "Glory be to Allah above all defects", "All praise belongs to Allah",
            "Allah is greater than everything", "No god truly worthy of worship but Allah",
            "Seeking Allah's forgiveness", "Sending blessings upon the Prophet ﷺ",
            "Glory and praise be to Allah", "No power except through Allah"
    };
    public static final String[] MEANINGS_FR = {
            "Gloire à Allah, au-dessus de tout défaut", "Toute louange appartient à Allah",
            "Allah est plus grand que tout", "Nul dieu digne d'adoration sauf Allah",
            "Demander le pardon d'Allah", "Bénédictions sur le Prophète ﷺ",
            "Gloire et louange à Allah", "Nulle puissance que par Allah"
    };
    private static final int[] DEFAULT_TARGETS = {33, 100, 100, 100, 100, 100, 100, 100};

    private static final String SEP_FIELD = "\u0001";
    private static final String SEP_ITEM = "\n";

    public static List<Item> load(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = prefs.getString("data", null);
        List<Item> list = new ArrayList<>();
        if (raw == null || raw.isEmpty()) {
            for (int i = 0; i < BUILTIN_COUNT; i++) {
                list.add(new Item(NAMES_AR[i], MEANINGS_AR[i], DEFAULT_TARGETS[i], 0));
            }
            return list;
        }
        for (String row : raw.split(SEP_ITEM)) {
            if (row.isEmpty()) continue;
            String[] f = row.split(SEP_FIELD, -1);
            if (f.length < 4) continue;
            try {
                list.add(new Item(f[0], f[1], Integer.parseInt(f[2]), Integer.parseInt(f[3])));
            } catch (NumberFormatException ignored) { }
        }
        if (list.isEmpty()) {
            for (int i = 0; i < BUILTIN_COUNT; i++) {
                list.add(new Item(NAMES_AR[i], MEANINGS_AR[i], DEFAULT_TARGETS[i], 0));
            }
        }
        return list;
    }

    public static void save(Context ctx, List<Item> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            Item it = list.get(i);
            if (i > 0) sb.append(SEP_ITEM);
            sb.append(it.name.replace(SEP_FIELD, " ").replace(SEP_ITEM, " "))
              .append(SEP_FIELD)
              .append(it.meaning.replace(SEP_FIELD, " ").replace(SEP_ITEM, " "))
              .append(SEP_FIELD).append(it.target)
              .append(SEP_FIELD).append(it.count);
        }
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString("data", sb.toString()).apply();
    }

    public static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // خلفيات الزر العائم: لون الدائرة ولون الرقم داخلها
    public static final int[] FLOAT_BG = {
            0xFF1B5E20, // أخضر
            0xFFB34700, // غسق برتقالي
            0xFFFFD54F, // ذهبي
            0xFF04525B, // زمردي
            0xFF212121, // أسود
            0xFF4A148C  // بنفسجي
    };
    public static final int[] FLOAT_TXT = {
            0xFFFFFFFF, 0xFFFFFFFF, 0xFF201503, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF
    };

    // ---------- الإحصائيات اليومية ----------
    // تُحفظ بصيغة: "2026-11-02=15;2026-11-03=40"

    public static String todayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime());
    }

    public static void recordToday(Context ctx) {
        Map<String, Integer> daily = loadDaily(ctx);
        String key = todayKey();
        Integer cur = daily.get(key);
        daily.put(key, cur == null ? 1 : cur + 1);
        saveDaily(ctx, daily);
    }

    public static Map<String, Integer> loadDaily(Context ctx) {
        Map<String, Integer> map = new TreeMap<>();
        String raw = prefs(ctx).getString("daily", "");
        if (raw != null && !raw.isEmpty()) {
            for (String part : raw.split(";")) {
                if (part.isEmpty()) continue;
                String[] kv = part.split("=", -1);
                if (kv.length == 2) {
                    try { map.put(kv[0], Integer.parseInt(kv[1])); } catch (NumberFormatException ignored) { }
                }
            }
        }
        return map;
    }

    public static void saveDaily(Context ctx, Map<String, Integer> daily) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Integer> e : daily.entrySet()) {
            if (!first) sb.append(";");
            first = false;
            sb.append(e.getKey()).append("=").append(e.getValue());
        }
        prefs(ctx).edit().putString("daily", sb.toString()).apply();
    }

    public static void clearDaily(Context ctx) {
        prefs(ctx).edit().remove("daily").apply();
    }
}
