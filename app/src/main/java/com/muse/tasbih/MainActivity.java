package com.muse.tasbih;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.app.TimePickerDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * مسبحة إلكترونية v2: عدّاد بحلقة تقدم، أذكار لكل ذكر عدّاده وهدفه،
 * عدّ تلقائي، زر عائم، سمات وإعدادات — كله دون مكتبات خارجية.
 */
public class MainActivity extends Activity {

    // ---------- النصوص بثلاث لغات ----------
    private static final Map<String, String[]> STR = new HashMap<>();
    static {
        STR.put("tabCounter", new String[]{"العداد", "Counter", "Compteur"});
        STR.put("tabAdhkar", new String[]{"الأذكار", "Adhkar", "Adhkar"});
        STR.put("tabSettings", new String[]{"الإعدادات", "Settings", "Paramètres"});
        STR.put("tapToCount", new String[]{"اضغط للعد", "Tap to count", "Touchez pour compter"});
        STR.put("goal", new String[]{"الهدف", "Goal", "Objectif"});
        STR.put("custom", new String[]{"مخصص", "Custom", "Personnalisé"});
        STR.put("autoCount", new String[]{"عدّ تلقائي", "Auto count", "Comptage auto"});
        STR.put("autoDesc", new String[]{"يزيد العداد وحده أثناء بقاء التطبيق مفتوحاً.", "Counts automatically while the app stays open.", "Compte tout seul tant que l'app reste ouverte."});
        STR.put("interval", new String[]{"الفاصل", "Interval", "Intervalle"});
        STR.put("sec", new String[]{"ث", "s", "sec"});
        STR.put("start", new String[]{"بدء", "Start", "Démarrer"});
        STR.put("stop", new String[]{"إيقاف", "Stop", "Arrêter"});
        STR.put("reset", new String[]{"تصفير", "Reset", "Réinitialiser"});
        STR.put("newDhikr", new String[]{"تسبيح جديد", "New dhikr", "Nouveau dhikr"});
        STR.put("current", new String[]{"الحالي", "Current", "Actuel"});
        STR.put("language", new String[]{"اللغة", "Language", "Langue"});
        STR.put("appearance", new String[]{"المظهر", "Appearance", "Apparence"});
        STR.put("appAuto", new String[]{"تلقائي", "Auto", "Auto"});
        STR.put("light", new String[]{"فاتح", "Light", "Clair"});
        STR.put("dark", new String[]{"داكن", "Dark", "Sombre"});
        STR.put("alerts", new String[]{"التنبيه", "Alerts", "Alertes"});
        STR.put("vibration", new String[]{"الاهتزاز", "Vibration", "Vibration"});
        STR.put("clickSound", new String[]{"صوت النقرة", "Click sound", "Son du clic"});
        STR.put("floating", new String[]{"زر عائم", "Floating button", "Bouton flottant"});
        STR.put("floatingHint", new String[]{"سبّح وأنت داخل تطبيق آخر. اسحب الدائرة لتحريكها، واضغط × لإغلاقها.", "Count inside other apps. Drag the circle to move it, tap × to close it.", "Comptez dans d'autres applis. Déplacez le cercle, touchez × pour le fermer."});
        STR.put("background", new String[]{"الخلفية", "Background", "Arrière-plan"});
        STR.put("photo", new String[]{"صورة من المعرض", "Photo from gallery", "Photo de la galerie"});
        STR.put("theme0", new String[]{"غسق الصحراء", "Desert dusk", "Crépuscule du désert"});
        STR.put("theme1", new String[]{"أخضر المدينة", "Medina green", "Vert de Médine"});
        STR.put("theme2", new String[]{"ذهب الكعبة", "Kaaba gold", "Or de la Kaaba"});
        STR.put("theme3", new String[]{"ليل زمردي", "Emerald night", "Nuit émeraude"});
        STR.put("footer", new String[]{"يعمل دون إنترنت. العدّ يُحفظ على هذا الهاتف.", "Works offline. Your count is saved on this phone.", "Fonctionne hors ligne. Le compte est enregistré sur ce téléphone."});
        STR.put("addTitle", new String[]{"إضافة ذكر جديد", "Add a new dhikr", "Ajouter un dhikr"});
        STR.put("nameHint", new String[]{"اسم الذكر", "Dhikr name", "Nom du dhikr"});
        STR.put("meaningHint", new String[]{"المعنى (اختياري)", "Meaning (optional)", "Signification (optionnel)"});
        STR.put("targetHint", new String[]{"الهدف", "Target", "Objectif"});
        STR.put("add", new String[]{"إضافة", "Add", "Ajouter"});
        STR.put("cancel", new String[]{"إلغاء", "Cancel", "Annuler"});
        STR.put("goalDone", new String[]{"ما شاء الله، أتممت الهدف", "MashaAllah, goal reached", "MachaAllah, objectif atteint"});
        STR.put("needOverlay", new String[]{"امنح إذن «العرض فوق التطبيقات» لتشغيل الزر العائم", "Grant the overlay permission to use the floating button", "Autorisez l'affichage par-dessus les applis pour le bouton flottant"});
        STR.put("resetDone", new String[]{"تم التصفير", "Reset done", "Réinitialisé"});
        STR.put("tabStats", new String[]{"الإحصائيات", "Statistics", "Statistiques"});
        STR.put("tabQuran", new String[]{"البقرة", "Al-Baqarah", "Al-Baqara"});
        STR.put("baqaraTitle", new String[]{"سورة البقرة", "Surah Al-Baqarah", "Sourate Al-Baqara"});
        STR.put("warsh", new String[]{"رواية ورش", "Warsh narration", "Riwaya Warsh"});
        STR.put("hafs", new String[]{"رواية حفص", "Hafs narration", "Riwaya Hafs"});
        STR.put("savePlace", new String[]{"🔖 حفظ مكاني", "🔖 Save my place", "🔖 Marquer ma place"});
        STR.put("resumeRead", new String[]{"⏩ متابعة القراءة", "⏩ Continue reading", "⏩ Continuer"});
        STR.put("placeSaved", new String[]{"تم حفظ مكانك ✓", "Your place is saved ✓", "Place enregistrée ✓"});
        STR.put("reminder", new String[]{"تذكير الاستغفار", "Istighfar reminder", "Rappel d'istighfar"});
        STR.put("reminderOn", new String[]{"تذكير يومي بالآية صوتياً", "Daily reminder with the verse audio", "Rappel quotidien avec le verset audio"});
        STR.put("reminderTime", new String[]{"وقت التذكير", "Reminder time", "Heure du rappel"});
        STR.put("playVerse", new String[]{"▶️ تشغيل الآيات (نوح ١٠–١٢)", "▶️ Play the verses (Nuh 10–12)", "▶️ Écouter les versets (Nuh 10–12)"});
        STR.put("reminderSet", new String[]{"سيصلك التذكير يومياً في", "Daily reminder set for", "Rappel quotidien à"});
        STR.put("tahfiz", new String[]{"🎧 التحفيظ بالتكرار", "🎧 Memorize by repetition", "🎧 Mémorisation par répétition"});
        STR.put("teacher", new String[]{"المعلّم (القارئ)", "Teacher (reciter)", "Enseignant (récitateur)"});
        STR.put("fromAyah", new String[]{"من الآية", "From verse", "Du verset"});
        STR.put("toAyah", new String[]{"إلى الآية", "To verse", "Au verset"});
        STR.put("repAyah", new String[]{"التكرار لكل آية", "Repeat per verse", "Répétition par verset"});
        STR.put("repRange", new String[]{"تكرار المقطع", "Repeat the passage", "Répétition du passage"});
        STR.put("noRepeat", new String[]{"بدون تكرار", "No repeat", "Sans répétition"});
        STR.put("startLearn", new String[]{"▶️ ابدأ التعلم", "▶️ Start learning", "▶️ Commencer"});
        STR.put("stopLearn", new String[]{"⏹ إيقاف", "⏹ Stop", "⏹ Arrêter"});
        STR.put("pauseW", new String[]{"⏸ إيقاف مؤقت", "⏸ Pause", "⏸ Pause"});
        STR.put("resumeW", new String[]{"▶️ استئناف", "▶️ Resume", "▶️ Reprendre"});
        STR.put("ayahW", new String[]{"الآية", "Verse", "Verset"});
        STR.put("fullNote", new String[]{"تلاوة السورة كاملة بصوته (تسجيلاته متاحة كاملة فقط، دون تقطيع آيات)", "Full-surah recitation (his recordings are available whole only, not per verse)", "Récitation complète (ses enregistrements n'existent qu'en entier)"});
        STR.put("needNet", new String[]{"التحفيظ الصوتي يحتاج اتصالاً بالإنترنت", "Audio memorization needs an internet connection", "L'audio nécessite une connexion Internet"});
        STR.put("playError", new String[]{"تعذّر التشغيل — تحقق من الإنترنت", "Playback failed — check your connection", "Lecture impossible — vérifiez la connexion"});
        STR.put("finished", new String[]{"✅ أكملت المقطع، بارك الله فيك", "✅ Passage completed, well done", "✅ Passage terminé, bravo"});
        STR.put("atAyah", new String[]{"عند الآية", "At verse", "Au verset"});
        STR.put("putMark", new String[]{"🔖 ضع الشارة", "🔖 Set bookmark", "🔖 Marquer"});
        STR.put("goMark", new String[]{"⏩ اذهب للشارة", "⏩ Go to bookmark", "⏩ Aller au marque"});
        STR.put("markSet", new String[]{"وُضعت الشارة عند الآية", "Bookmark set at verse", "Marque posée au verset"});
        STR.put("loadingW", new String[]{"⏳ جارٍ بدء التحفيظ… الآية", "⏳ Starting… verse", "⏳ Démarrage… verset"});
        STR.put("today", new String[]{"اليوم", "Today", "Aujourd'hui"});
        STR.put("thisWeek", new String[]{"هذا الأسبوع", "This week", "Cette semaine"});
        STR.put("thisMonth", new String[]{"هذا الشهر", "This month", "Ce mois"});
        STR.put("thisYear", new String[]{"هذه السنة", "This year", "Cette année"});
        STR.put("allTotal", new String[]{"المجموع الكلي", "All-time total", "Total général"});
        STR.put("last7", new String[]{"آخر 7 أيام", "Last 7 days", "7 derniers jours"});
        STR.put("perDhikr", new String[]{"حسب الذكر", "By dhikr", "Par dhikr"});
        STR.put("clearStats", new String[]{"مسح الإحصائيات", "Clear statistics", "Effacer les statistiques"});
        STR.put("statsNote", new String[]{"الإحصائيات اليومية تُسجَّل من هذا التحديث فصاعداً، وتصفير أي عدّاد لا يمسحها.", "Daily statistics are recorded from this update onwards; resetting a counter doesn't erase them.", "Les statistiques quotidiennes sont enregistrées à partir de cette mise à jour ; réinitialiser un compteur ne les efface pas."});
        STR.put("autoSound", new String[]{"صوت العدّ التلقائي", "Auto-count sound", "Son du comptage auto"});
        STR.put("autoVib", new String[]{"اهتزاز العدّ التلقائي", "Auto-count vibration", "Vibration du comptage auto"});
        STR.put("floatVib", new String[]{"اهتزاز الزر العائم", "Floating button vibration", "Vibration du bouton flottant"});
        STR.put("floatSound", new String[]{"صوت الزر العائم", "Floating button sound", "Son du bouton flottant"});
        STR.put("floatBg", new String[]{"خلفية الزر العائم", "Floating button background", "Fond du bouton flottant"});
        STR.put("floatSize", new String[]{"حجم الزر العائم", "Floating button size", "Taille du bouton flottant"});
    }

    private String tr(String key) {
        String[] arr = STR.get(key);
        if (arr == null) return key;
        int l = lang;
        if (l < 0 || l > 2) l = 0;
        return arr[l];
    }

    // ---------- السمات ----------
    private static final int[][] THEME_BG = {
            {0xFF2A0800, 0xFF7A1E02, 0xFFC75109},
            {0xFF03231A, 0xFF0B4D2E, 0xFF0E7A46},
            {0xFF000000, 0xFF171310, 0xFF3A2C12},
            {0xFF01262B, 0xFF04525B, 0xFF0A7C86}
    };

    private int[] bgColors;
    private int cardColor, textColor, subColor, accentColor, chipColor, navColor;
    private boolean lightMode;

    // ---------- الحالة ----------
    private SharedPreferences prefs;
    private List<TasbihData.Item> items;
    private int current = 0;
    private int lang = 0;
    private int appearance = 2; // 0 تلقائي، 1 فاتح، 2 داكن
    private int theme = 0;
    private boolean vibOn = true, soundOn = false, floatingOn = false, customBg = false;
    private boolean autoSound = false, autoVib = false;
    private int autoIntervalHalf = 8; // الفاصل بوحدات نصف ثانية: 8 = 4 ثوانٍ
    private boolean autoRunning = false;
    // يبقى حياً عبر recreate() داخل نفس الجلسة فقط، ليستأنف العدّ التلقائي بعد تغيير لغة/سمة
    private static boolean sAutoResume = false;

    private FrameLayout root;
    private LinearLayout[] sections = new LinearLayout[5];
    private Button[] navButtons = new Button[5];
    private int activeTab = 0;
    private LinearLayout statsBox;
    private ScrollView mainScroll;
    private MediaPlayer testPlayer;
    // ---- التحفيظ بالتكرار ----
    private static final String[] RECITER_NAMES = {"هشام الهراز", "العيون الكوشي", "إبراهيم الدوسري", "مشاري العفاسي", "ماهر المعيقلي", "سعد الغامدي", "عمر القزابري"};
    private static final String[] RECITER_TAGS = {"ورش · كاملة", "ورش · آيات", "ورش · آيات", "حفص · آيات", "حفص · مجوّد", "حفص · مرتل", "ورش · آيات"};
    private static final String[] RECITER_BASE = {
            "https://cdn.mp3quran.net/audio/hicham-lharraz/r1/",
            "https://cdn.mp3quran.net/audio/laayoun-kouchi/r1/",
            "https://everyayah.com/data/warsh/warsh_ibrahim_aldosary_128kbps/",
            "https://everyayah.com/data/Alafasy_128kbps/",
            "https://cdn.mp3quran.net/audio/maher-muaiqly/r3/",
            "https://cdn.mp3quran.net/audio/saad-ghamdi/r1/",
            "https://cdn.mp3quran.net/audio/omar-kazabri/r1/"};
    // وضع التشغيل: 0 = تلاوة كاملة، 1 = ملف لكل آية، 2 = توقيتات داخل ملف السورة
    private static final int[] RECITER_MODE = {0, 2, 1, 1, 2, 2, 2};
    private static final String[] RECITER_TIMING = {
            null, "timing_kouchi.csv", null, null,
            "timing_muaiqly.csv", "timing_ghamdi.csv", "timing_qazabri.csv"};
    private static final int[] REP_RANGE_VALS = {1, 2, 3, 5};
    private Button[] reciterChips = new Button[7];
    private Button[] repRangeChips = new Button[4];
    private EditText fromInput, toInput;
    private TextView tahfizStatus, tahfizNote, repAyahLabel;
    private SeekBar repAyahSeek;
    private Button tahfizStartBtn, tahfizPauseBtn;
    private int tahfizReciter = 4, repRangeIdx = 0, repAyahVal = 1;
    private final android.os.Handler statusHandler = new android.os.Handler();
    private final Runnable statusTick = new Runnable() {
        @Override public void run() {
            syncTahfizUi();
            statusHandler.postDelayed(this, 1000);
        }
    };
    private TextView quranBody;
    private Button warshChip, hafsChip;
    private int riwaya = 0; // 0 = ورش، 1 = حفص
    private String[][] baqaraCache = new String[2][];
    private int[][] baqaraOffsets = new int[2][];
    private EditText markInput;
    private int lastHighlight = 0;

    private RingView ring;
    private TextView dhikrTitle, dhikrMeaning, ratioText, counterIntervalLabel;
    private LinearLayout adhkarListBox;
    private SeekBar counterSeek;

    private Handler autoHandler = new Handler();
    private ToneGenerator tone;

    private TasbihData.Item cur() {
        if (items == null || items.isEmpty()) return null;
        if (current < 0 || current >= items.size()) current = 0;
        return items.get(current);
    }

    private String displayName(int idx) {
        TasbihData.Item it = items.get(idx);
        if (idx < TasbihData.BUILTIN_COUNT) {
            if (lang == 1) return TasbihData.NAMES_EN[idx];
            if (lang == 2) return TasbihData.NAMES_FR[idx];
            return TasbihData.NAMES_AR[idx];
        }
        return it.name;
    }

    private String displayMeaning(int idx) {
        TasbihData.Item it = items.get(idx);
        if (idx < TasbihData.BUILTIN_COUNT) {
            if (lang == 1) return TasbihData.MEANINGS_EN[idx];
            if (lang == 2) return TasbihData.MEANINGS_FR[idx];
            return TasbihData.MEANINGS_AR[idx];
        }
        return it.meaning;
    }

    // ---------- أدوات بناء الواجهة ----------
    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    private GradientDrawable roundedStroke(int color, int radiusDp, int strokeColor) {
        GradientDrawable g = rounded(color, radiusDp);
        g.setStroke(dp(2), strokeColor);
        return g;
    }

    private TextView label(String s, float sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(null, android.graphics.Typeface.BOLD);
        return t;
    }

    private Button chip(String s, boolean selected, View.OnClickListener click) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setPadding(dp(18), 0, dp(18), 0);
        if (selected) {
            b.setBackground(rounded(accentColor, 22));
            b.setTextColor(0xFF201503);
        } else {
            b.setBackground(rounded(chipColor, 22));
            b.setTextColor(textColor);
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(44));
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        b.setLayoutParams(lp);
        b.setOnClickListener(click);
        return b;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(rounded(cardColor, 22));
        c.setPadding(dp(18), dp(16), dp(18), dp(16));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(8), 0, dp(8));
        c.setLayoutParams(lp);
        return c;
    }

    private TextView sectionTitle(String s) {
        TextView t = label(s, 15, accentColor, true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(18), 0, dp(4));
        lp.gravity = Gravity.START;
        t.setLayoutParams(lp);
        return t;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = TasbihData.prefs(this);
        loadSettings();
        items = TasbihData.load(this);
        resolvePalette();
        try {
            tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 60);
        } catch (Exception ignored) { }

        root = new FrameLayout(this);
        applyBackground();
        getWindow().setStatusBarColor(bgColors[0]);

        ScrollView scroll = new ScrollView(this);
        mainScroll = scroll;
        scroll.setFillViewport(true);
        FrameLayout.LayoutParams scrollLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        scrollLp.bottomMargin = dp(76);
        scroll.setLayoutParams(scrollLp);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content);

        sections[0] = buildCounterSection();
        sections[1] = buildAdhkarSection();
        sections[2] = buildQuranSection();
        sections[3] = buildStatsSection();
        sections[4] = buildSettingsSection();
        for (LinearLayout s : sections) content.addView(s);

        root.addView(scroll);
        root.addView(buildNavBar());
        setContentView(root);
        showTab(0);
        refreshCounter();
        refreshAdhkar();
        statusHandler.post(statusTick);
        if (autoRunning) {
            autoHandler.postDelayed(autoRunnable, autoIntervalHalf * 500L);
        }
    }

    private void loadSettings() {
        autoRunning = sAutoResume;
        lang = prefs.getInt("lang", 0);
        appearance = prefs.getInt("appearance", 2);
        theme = prefs.getInt("theme", 0);
        vibOn = prefs.getBoolean("vib_on", true);
        soundOn = prefs.getBoolean("sound_on", false);
        autoSound = prefs.getBoolean("auto_sound", false);
        autoVib = prefs.getBoolean("auto_vib", false);
        floatingOn = prefs.getBoolean("floating_on", false);
        customBg = prefs.getBoolean("custom_bg", false);
        if (prefs.contains("auto_interval_half")) {
            autoIntervalHalf = prefs.getInt("auto_interval_half", 8);
        } else if (prefs.contains("auto_interval")) {
            autoIntervalHalf = Math.max(1, prefs.getInt("auto_interval", 4) * 2);
        } else {
            autoIntervalHalf = 8;
        }
        current = prefs.getInt("current", 0);
    }

    private void resolvePalette() {
        boolean night = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        lightMode = appearance == 1 || (appearance == 0 && !night);
        accentColor = 0xFFFFD54F;
        if (lightMode) {
            bgColors = new int[]{0xFFF7F0E4, 0xFFF3E3CE, 0xFFEFD9BC};
            cardColor = 0xE6FFFFFF;
            textColor = 0xFF2D1A08;
            subColor = 0xFF7A5C40;
            chipColor = 0x33B07D2B;
            navColor = 0xFFF3E7D3;
            accentColor = 0xFFC8912A;
        } else {
            bgColors = THEME_BG[Math.max(0, Math.min(3, theme))];
            cardColor = 0x59000000;
            textColor = Color.WHITE;
            subColor = 0xFFEFDDC8;
            chipColor = 0x40FFFFFF;
            navColor = 0xFF1A0D06;
        }
    }

    private void applyBackground() {
        if (customBg) {
            File f = new File(getFilesDir(), "bg.jpg");
            if (f.exists()) {
                Bitmap bm = BitmapFactory.decodeFile(f.getAbsolutePath());
                if (bm != null) {
                    root.setBackground(new BitmapDrawable(getResources(), bm));
                    return;
                }
            }
        }
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, bgColors);
        root.setBackground(g);
    }

    private View buildNavBar() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setBackgroundColor(navColor);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(76));
        lp.gravity = Gravity.BOTTOM;
        nav.setLayoutParams(lp);
        String[] names = {tr("tabCounter"), tr("tabAdhkar"), tr("tabQuran"), tr("tabStats"), tr("tabSettings")};
        String[] icons = {"⌂", "★", "📖", "📊", "⚙"};
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            Button b = new Button(this);
            b.setText(icons[i] + "\n" + names[i]);
            b.setAllCaps(false);
            b.setTextSize(12);
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0, dp(60), 1f);
            blp.setMargins(dp(3), dp(8), dp(3), dp(8));
            b.setLayoutParams(blp);
            b.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { showTab(idx); }
            });
            navButtons[i] = b;
            nav.addView(b);
        }
        return nav;
    }

    private String reminderTimeText() {
        int h = prefs.getInt("reminder_hour", 8);
        int m = prefs.getInt("reminder_min", 0);
        return String.format(Locale.US, "%02d:%02d", h, m);
    }

    private void playVerse() {
        try {
            if (testPlayer != null) {
                try { testPlayer.stop(); } catch (Exception ignored) { }
                testPlayer.release();
                testPlayer = null;
            }
            testPlayer = MediaPlayer.create(this, R.raw.nuh);
            if (testPlayer != null) {
                testPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override public void onCompletion(MediaPlayer mp) {
                        try { mp.release(); } catch (Exception ignored) { }
                        if (testPlayer == mp) testPlayer = null;
                    }
                });
                testPlayer.start();
            }
        } catch (Exception ignored) { }
    }

    private void showTab(int idx) {
        // حفظ موضع القراءة تلقائياً عند مغادرة تبويب البقرة
        if (activeTab == 2 && idx != 2 && mainScroll != null) {
            prefs.edit().putInt("qpos_" + riwaya, mainScroll.getScrollY()).apply();
        }
        activeTab = idx;
        for (int i = 0; i < 5; i++) {
            sections[i].setVisibility(i == idx ? View.VISIBLE : View.GONE);
            if (i == idx) {
                navButtons[i].setBackground(rounded(accentColor, 24));
                navButtons[i].setTextColor(0xFF201503);
            } else {
                navButtons[i].setBackgroundColor(Color.TRANSPARENT);
                navButtons[i].setTextColor(lightMode ? 0xFF5D4037 : Color.WHITE);
            }
        }
        if (idx == 0) refreshCounter();
        if (idx == 1) refreshAdhkar();
        if (idx == 3) refreshStats();
        if (idx == 2 && quranBody != null && TahfizService.playing && TahfizService.currentAyah > 0) {
            lastHighlight = TahfizService.currentAyah;
            applyQuranText();
            scrollToAyah(lastHighlight);
        }
    }

    // ---------- قسم العدّاد ----------
    private LinearLayout buildCounterSection() {
        LinearLayout s = new LinearLayout(this);
        s.setOrientation(LinearLayout.VERTICAL);
        s.setGravity(Gravity.CENTER_HORIZONTAL);
        s.setPadding(dp(22), dp(28), dp(22), dp(24));

        dhikrTitle = label("", 34, textColor, true);
        dhikrTitle.setGravity(Gravity.CENTER);
        s.addView(dhikrTitle);

        dhikrMeaning = label("", 16, subColor, false);
        dhikrMeaning.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams mLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLp.topMargin = dp(6);
        dhikrMeaning.setLayoutParams(mLp);
        s.addView(dhikrMeaning);

        ring = new RingView(this);
        ring.setColors(lightMode ? 0x33000000 : 0x33FFFFFF, accentColor, textColor);
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(dp(285), dp(285));
        rLp.topMargin = dp(26);
        ring.setLayoutParams(rLp);
        ring.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { doCount(true); }
        });
        s.addView(ring);

        ratioText = label("", 20, textColor, false);
        LinearLayout.LayoutParams raLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        raLp.topMargin = dp(14);
        ratioText.setLayoutParams(raLp);
        s.addView(ratioText);

        TextView tapHint = label(tr("tapToCount"), 14, subColor, false);
        s.addView(tapHint);

        // رقائق التنبيه
        LinearLayout toggles = new LinearLayout(this);
        toggles.setOrientation(LinearLayout.HORIZONTAL);
        toggles.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tLp.topMargin = dp(18);
        toggles.setLayoutParams(tLp);
        toggles.addView(chip(tr("vibration"), vibOn, new View.OnClickListener() {
            @Override public void onClick(View v) {
                vibOn = !vibOn;
                prefs.edit().putBoolean("vib_on", vibOn).apply();
                recreate();
            }
        }));
        toggles.addView(chip(tr("clickSound"), soundOn, new View.OnClickListener() {
            @Override public void onClick(View v) {
                soundOn = !soundOn;
                prefs.edit().putBoolean("sound_on", soundOn).apply();
                recreate();
            }
        }));
        toggles.addView(chip(tr("floating"), floatingOn, new View.OnClickListener() {
            @Override public void onClick(View v) { setFloating(!floatingOn); }
        }));
        s.addView(toggles);

        TextView floatHint = label(tr("floatingHint"), 12, subColor, false);
        floatHint.setGravity(Gravity.CENTER);
        s.addView(floatHint);

        // الهدف
        TextView goalTitle = sectionTitle(tr("goal"));
        LinearLayout.LayoutParams gLp = (LinearLayout.LayoutParams) goalTitle.getLayoutParams();
        gLp.gravity = Gravity.START;
        goalTitle.setLayoutParams(gLp);
        LinearLayout goalWrap = new LinearLayout(this);
        goalWrap.setOrientation(LinearLayout.VERTICAL);
        goalWrap.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        goalWrap.addView(goalTitle);
        LinearLayout goalRow = new LinearLayout(this);
        goalRow.setOrientation(LinearLayout.HORIZONTAL);
        goalRow.setGravity(Gravity.CENTER);
        final int[] goalVals = {33, 99, 100};
        TasbihData.Item c0 = cur();
        int curTarget = c0 != null ? c0.target : 100;
        for (final int gv : goalVals) {
            goalRow.addView(chip(String.valueOf(gv), curTarget == gv, new View.OnClickListener() {
                @Override public void onClick(View v) {
                    TasbihData.Item c = cur();
                    if (c != null) {
                        c.target = gv;
                        TasbihData.save(MainActivity.this, items);
                    }
                    recreate();
                }
            }));
        }
        boolean isCustomGoal = curTarget != 33 && curTarget != 99 && curTarget != 100;
        goalRow.addView(chip(tr("custom"), isCustomGoal, new View.OnClickListener() {
            @Override public void onClick(View v) { showCustomGoalDialog(); }
        }));
        goalWrap.addView(goalRow);
        s.addView(goalWrap);

        // العد التلقائي
        LinearLayout autoCard = card();
        autoCard.addView(label(tr("autoCount"), 18, textColor, true));
        autoCard.addView(label(tr("autoDesc"), 13, subColor, false));
        counterIntervalLabel = label("", 15, textColor, false);
        autoCard.addView(counterIntervalLabel);
        counterSeek = new SeekBar(this);
        counterSeek.setMax(19);
        counterSeek.setProgress(Math.max(0, Math.min(19, autoIntervalHalf - 1)));
        counterSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean fromUser) {
                autoIntervalHalf = p + 1;
                updateIntervalLabel();
                prefs.edit().putInt("auto_interval_half", autoIntervalHalf).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) { }
            @Override public void onStopTrackingTouch(SeekBar sb) { }
        });
        autoCard.addView(counterSeek);
        LinearLayout autoToggles = new LinearLayout(this);
        autoToggles.setOrientation(LinearLayout.HORIZONTAL);
        autoToggles.setGravity(Gravity.CENTER);
        autoToggles.addView(chip("🔊 " + tr("autoSound"), autoSound, new View.OnClickListener() {
            @Override public void onClick(View v) {
                autoSound = !autoSound;
                prefs.edit().putBoolean("auto_sound", autoSound).apply();
                recreate();
            }
        }));
        autoToggles.addView(chip("📳 " + tr("autoVib"), autoVib, new View.OnClickListener() {
            @Override public void onClick(View v) {
                autoVib = !autoVib;
                prefs.edit().putBoolean("auto_vib", autoVib).apply();
                recreate();
            }
        }));
        autoCard.addView(autoToggles);
        final Button autoBtn = new Button(this);
        autoBtn.setAllCaps(false);
        autoBtn.setText(tr(autoRunning ? "stop" : "start"));
        autoBtn.setBackground(rounded(accentColor, 22));
        autoBtn.setTextColor(0xFF201503);
        autoBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                autoRunning = !autoRunning;
                sAutoResume = autoRunning;
                autoBtn.setText(tr(autoRunning ? "stop" : "start"));
                if (autoRunning) {
                    autoHandler.postDelayed(autoRunnable, autoIntervalHalf * 500L);
                } else {
                    autoHandler.removeCallbacks(autoRunnable);
                }
            }
        });
        autoCard.addView(autoBtn);
        s.addView(autoCard);
        updateIntervalLabel();

        Button resetBtn = new Button(this);
        resetBtn.setText(tr("reset"));
        resetBtn.setAllCaps(false);
        resetBtn.setTextColor(accentColor);
        resetBtn.setBackgroundColor(Color.TRANSPARENT);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                TasbihData.Item c = cur();
                if (c != null) {
                    c.count = 0;
                    TasbihData.save(MainActivity.this, items);
                }
                refreshCounter();
                Toast.makeText(MainActivity.this, tr("resetDone"), Toast.LENGTH_SHORT).show();
            }
        });
        s.addView(resetBtn);
        return s;
    }

    private final Runnable autoRunnable = new Runnable() {
        @Override public void run() {
            if (!autoRunning) return;
            doCount(false);
            autoHandler.postDelayed(this, autoIntervalHalf * 500L);
        }
    };

    private String intervalText() {
        if (autoIntervalHalf % 2 == 0) return String.valueOf(autoIntervalHalf / 2);
        return (autoIntervalHalf / 2) + ".5";
    }

    private void updateIntervalLabel() {
        if (counterIntervalLabel != null) {
            counterIntervalLabel.setText(tr("interval") + " · " + intervalText() + " " + tr("sec"));
        }
    }

    private void doCount(boolean fromTap) {
        TasbihData.Item c = cur();
        if (c == null) return;
        c.count++;
        TasbihData.save(this, items);
        TasbihData.recordToday(this);
        if (fromTap) {
            if (vibOn) {
                // اهتزاز مباشر من محرك الاهتزاز: يعمل حتى لو كان اهتزاز اللمس معطّلاً في إعدادات النظام
                vibrate(30);
            }
            if (soundOn && tone != null) {
                try { tone.startTone(ToneGenerator.TONE_PROP_BEEP, 50); } catch (Exception ignored) { }
            }
        } else {
            // تنبيهات العدّ التلقائي: خيارات مستقلة لكل عدّة
            if (autoVib) vibrate(45);
            if (autoSound && tone != null) {
                try { tone.startTone(ToneGenerator.TONE_PROP_BEEP, 50); } catch (Exception ignored) { }
            }
        }
        if (c.target > 0 && c.count == c.target) {
            if (vibOn) vibrate(350);
            Toast.makeText(this, tr("goalDone") + " 🎉", Toast.LENGTH_SHORT).show();
        }
        refreshCounter();
    }

    private void refreshCounter() {
        TasbihData.Item c = cur();
        if (c == null || ring == null) return;
        dhikrTitle.setText(displayName(current));
        String m = displayMeaning(current);
        dhikrMeaning.setText(m != null ? m : "");
        ring.setData(c.count, c.target);
        ratioText.setText(c.count + " / " + c.target);
    }

    private void vibrate(long ms) {
        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vib == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // أقصى قوة (255) حتى يكون الاهتزاز محسوساً بوضوح
            vib.vibrate(VibrationEffect.createOneShot(ms, 255));
        } else {
            vib.vibrate(ms);
        }
    }

    // ---------- قسم الأذكار ----------
    private LinearLayout buildAdhkarSection() {
        LinearLayout s = new LinearLayout(this);
        s.setOrientation(LinearLayout.VERTICAL);
        s.setPadding(dp(22), dp(28), dp(22), dp(24));

        TextView title = label(tr("tabAdhkar"), 28, textColor, true);
        s.addView(title);

        Button addBtn = new Button(this);
        addBtn.setText(tr("newDhikr"));
        addBtn.setAllCaps(false);
        addBtn.setBackground(rounded(accentColor, 22));
        addBtn.setTextColor(0xFF201503);
        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(46));
        aLp.topMargin = dp(12);
        aLp.gravity = Gravity.START;
        addBtn.setLayoutParams(aLp);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showAddDhikrDialog(); }
        });
        s.addView(addBtn);

        adhkarListBox = new LinearLayout(this);
        adhkarListBox.setOrientation(LinearLayout.VERTICAL);
        s.addView(adhkarListBox);
        return s;
    }

    private void refreshAdhkar() {
        if (adhkarListBox == null) return;
        adhkarListBox.removeAllViews();
        for (int i = 0; i < items.size(); i++) {
            final int idx = i;
            TasbihData.Item it = items.get(i);
            LinearLayout c = card();

            LinearLayout head = new LinearLayout(this);
            head.setOrientation(LinearLayout.HORIZONTAL);
            TextView name = label(displayName(idx), 22, textColor, true);
            name.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            head.addView(name);
            if (idx == current) {
                TextView curTag = label(tr("current"), 13, accentColor, true);
                head.addView(curTag);
            }
            c.addView(head);

            String m = displayMeaning(idx);
            if (m != null && !m.isEmpty()) {
                c.addView(label(m, 14, subColor, false));
            }

            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            pb.setMax(Math.max(1, it.target));
            pb.setProgress(Math.min(it.count, Math.max(1, it.target)));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                pb.setProgressTintList(android.content.res.ColorStateList.valueOf(accentColor));
                pb.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        lightMode ? 0x33000000 : 0x33FFFFFF));
            }
            LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            pLp.topMargin = dp(12);
            pb.setLayoutParams(pLp);
            c.addView(pb);

            LinearLayout foot = new LinearLayout(this);
            foot.setOrientation(LinearLayout.HORIZONTAL);
            TextView nums = label(it.count + " / " + it.target, 15, textColor, false);
            nums.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            foot.addView(nums);
            Button reset = new Button(this);
            reset.setText(tr("reset"));
            reset.setAllCaps(false);
            reset.setTextSize(14);
            reset.setTextColor(accentColor);
            reset.setBackgroundColor(Color.TRANSPARENT);
            reset.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    items.get(idx).count = 0;
                    TasbihData.save(MainActivity.this, items);
                    refreshAdhkar();
                    if (idx == current) refreshCounter();
                }
            });
            foot.addView(reset);
            c.addView(foot);

            c.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    current = idx;
                    prefs.edit().putInt("current", current).apply();
                    showTab(0);
                    refreshAdhkar();
                }
            });
            adhkarListBox.addView(c);
        }
    }

    // ---------- قسم سورة البقرة ----------

    private String[] loadBaqara(int which) {
        if (baqaraCache[which] != null) return baqaraCache[which];
        java.util.List<String> lines = new java.util.ArrayList<>();
        try {
            InputStream in = getAssets().open(which == 0 ? "baqara_warsh.txt" : "baqara_hafs.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) lines.add(line.trim());
            }
            br.close();
        } catch (Exception ignored) { }
        baqaraCache[which] = lines.toArray(new String[0]);
        return baqaraCache[which];
    }

    private static String arNum(int n) {
        String s = String.valueOf(n);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            sb.append(c >= '0' && c <= '9' ? (char) ('٠' + (c - '0')) : c);
        }
        return sb.toString();
    }

    private String baqaraText(int which) {
        String[] v = loadBaqara(which);
        int mark = prefs.getInt("qayah_" + which, 0);
        StringBuilder sb = new StringBuilder();
        baqaraOffsets[which] = new int[v.length];
        for (int i = 0; i < v.length; i++) {
            sb.append(v[i]).append(" ﴿").append(arNum(i + 1)).append("﴾");
            baqaraOffsets[which][i] = sb.length();
            if (i + 1 == mark) sb.append(" 🔖");
            sb.append(" ");
        }
        return sb.toString();
    }

    /** يعرض نص السورة تفاعلياً: كل آية قابلة للنقر لوضع علامة الحفظ، والآية الجارية في التلاوة مظلَّلة. */
    private void applyQuranText() {
        if (quranBody == null) return;
        String text = baqaraText(riwaya);
        int[] offs = baqaraOffsets[riwaya];
        SpannableString ss = new SpannableString(text);
        if (offs != null) {
            int mark = prefs.getInt("qayah_" + riwaya, 0);
            int segStart = 0;
            for (int i = 0; i < offs.length; i++) {
                final int ayah = i + 1;
                int segEnd = offs[i] + (ayah == mark ? 3 : 0) + 1;
                if (segEnd > text.length()) segEnd = text.length();
                if (segEnd > segStart) {
                    ss.setSpan(new ClickableSpan() {
                        @Override public void onClick(View w) { onAyahTapped(ayah); }
                        @Override public void updateDrawState(TextPaint ds) { ds.setUnderlineText(false); }
                    }, segStart, segEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (ayah == lastHighlight && lastHighlight > 0) {
                        int hl = (accentColor & 0x00FFFFFF) | 0x66000000;
                        ss.setSpan(new BackgroundColorSpan(hl), segStart, segEnd,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                segStart = segEnd;
            }
        }
        quranBody.setText(ss);
    }

    private void onAyahTapped(int ayah) {
        prefs.edit().putInt("qayah_" + riwaya, ayah).apply();
        if (markInput != null) markInput.setText(String.valueOf(ayah));
        applyQuranText();
        Toast.makeText(this, tr("markSet") + " " + arNum(ayah) + " 🔖", Toast.LENGTH_SHORT).show();
    }

    /** تمرير الشاشة إلى آية معيّنة داخل نص السورة. */
    private void scrollToAyah(final int ayah) {
        if (mainScroll == null || quranBody == null) return;
        final int[] offs = baqaraOffsets[riwaya];
        if (offs == null || ayah < 1 || ayah > offs.length) {
            scrollToSavedPlace();
            return;
        }
        quranBody.post(new Runnable() {
            @Override public void run() {
                try {
                    android.text.Layout lay = quranBody.getLayout();
                    if (lay == null) { scrollToSavedPlace(); return; }
                    int idx = Math.min(offs[ayah - 1], quranBody.getText().length());
                    int line = lay.getLineForOffset(Math.max(0, idx - 1));
                    int y = lay.getLineTop(line);
                    View v = quranBody;
                    while (v != null && v.getParent() != mainScroll) {
                        y += v.getTop();
                        v = (View) v.getParent();
                    }
                    mainScroll.smoothScrollTo(0, Math.max(0, y - dp(90)));
                } catch (Exception e) {
                    scrollToSavedPlace();
                }
            }
        });
    }

    private void styleRiwayaChips() {
        if (warshChip != null) {
            warshChip.setBackground(rounded(riwaya == 0 ? accentColor : chipColor, 22));
            warshChip.setTextColor(riwaya == 0 ? 0xFF201503 : textColor);
        }
        if (hafsChip != null) {
            hafsChip.setBackground(rounded(riwaya == 1 ? accentColor : chipColor, 22));
            hafsChip.setTextColor(riwaya == 1 ? 0xFF201503 : textColor);
        }
    }

    private void selectRiwaya(int which) {
        // حفظ موضع الرواية الحالية قبل التبديل
        if (mainScroll != null && quranBody != null) {
            prefs.edit().putInt("qpos_" + riwaya, mainScroll.getScrollY()).apply();
        }
        riwaya = which;
        prefs.edit().putInt("riwaya", which).apply();
        styleRiwayaChips();
        applyQuranText();
        if (markInput != null) {
            int mk = prefs.getInt("qayah_" + which, 0);
            markInput.setText(String.valueOf(mk > 0 ? mk : 1));
        }
        scrollToSavedPlace();
    }

    private void scrollToSavedPlace() {
        if (mainScroll == null) return;
        final int y = prefs.getInt("qpos_" + riwaya, 0);
        mainScroll.post(new Runnable() {
            @Override public void run() { if (mainScroll != null) mainScroll.scrollTo(0, y); }
        });
    }

    private LinearLayout buildQuranSection() {
        LinearLayout s = new LinearLayout(this);
        s.setOrientation(LinearLayout.VERTICAL);
        s.setPadding(dp(22), dp(28), dp(22), dp(24));
        TextView title = label("📖 " + tr("baqaraTitle"), 28, textColor, true);
        title.setGravity(Gravity.CENTER);
        s.addView(title);

        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setGravity(Gravity.CENTER);
        riwaya = prefs.getInt("riwaya", 0);
        warshChip = chip(tr("warsh"), riwaya == 0, new View.OnClickListener() {
            @Override public void onClick(View v) { selectRiwaya(0); }
        });
        hafsChip = chip(tr("hafs"), riwaya == 1, new View.OnClickListener() {
            @Override public void onClick(View v) { selectRiwaya(1); }
        });
        chips.addView(warshChip);
        chips.addView(hafsChip);
        LinearLayout.LayoutParams chLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chLp.topMargin = dp(10);
        chips.setLayoutParams(chLp);
        s.addView(chips);

        LinearLayout bmRow = new LinearLayout(this);
        bmRow.setOrientation(LinearLayout.HORIZONTAL);
        bmRow.setGravity(Gravity.CENTER);
        bmRow.addView(chip(tr("savePlace"), false, new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mainScroll != null) {
                    prefs.edit().putInt("qpos_" + riwaya, mainScroll.getScrollY()).apply();
                }
                Toast.makeText(MainActivity.this, tr("placeSaved"), Toast.LENGTH_SHORT).show();
            }
        }));
        bmRow.addView(chip(tr("resumeRead"), false, new View.OnClickListener() {
            @Override public void onClick(View v) {
                int mk = prefs.getInt("qayah_" + riwaya, 0);
                if (mk > 0) scrollToAyah(mk); else scrollToSavedPlace();
            }
        }));
        s.addView(bmRow);

        // شارة عند آية التوقف
        LinearLayout markRow = new LinearLayout(this);
        markRow.setOrientation(LinearLayout.HORIZONTAL);
        markRow.setGravity(Gravity.CENTER);
        markRow.addView(label(tr("atAyah"), 15, subColor, false));
        markInput = ayahInput(prefs.getInt("qayah_" + riwaya, 0) > 0 ? prefs.getInt("qayah_" + riwaya, 0) : 1);
        LinearLayout.LayoutParams miLp = new LinearLayout.LayoutParams(dp(76), dp(48));
        miLp.setMargins(dp(8), dp(4), dp(8), dp(4));
        markInput.setLayoutParams(miLp);
        markRow.addView(markInput);
        markRow.addView(chip(tr("putMark"), false, new View.OnClickListener() {
            @Override public void onClick(View v) {
                int a = parseAyah(markInput, 1);
                markInput.setText(String.valueOf(a));
                prefs.edit().putInt("qayah_" + riwaya, a).apply();
                applyQuranText();
                Toast.makeText(MainActivity.this, tr("markSet") + " " + arNum(a) + " 🔖", Toast.LENGTH_SHORT).show();
            }
        }));
        markRow.addView(chip(tr("goMark"), false, new View.OnClickListener() {
            @Override public void onClick(View v) {
                int mk = prefs.getInt("qayah_" + riwaya, 0);
                if (mk > 0) scrollToAyah(mk);
                else {
                    int a = parseAyah(markInput, 1);
                    scrollToAyah(a);
                }
            }
        }));
        s.addView(markRow);
        s.addView(buildTahfizCard());

        TextView basmala = label("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", 20, subColor, true);
        basmala.setGravity(Gravity.CENTER);
        basmala.setTypeface(android.graphics.Typeface.SERIF);
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bLp.topMargin = dp(12);
        basmala.setLayoutParams(bLp);
        s.addView(basmala);

        quranBody = new TextView(this);
        quranBody.setTextColor(textColor);
        quranBody.setTextSize(21);
        quranBody.setTypeface(android.graphics.Typeface.SERIF);
        quranBody.setLineSpacing(dp(9), 1.0f);
        quranBody.setPadding(dp(2), dp(10), dp(2), dp(16));
        quranBody.setMovementMethod(LinkMovementMethod.getInstance());
        quranBody.setHighlightColor(Color.TRANSPARENT);
        s.addView(quranBody);
        applyQuranText();
        return s;
    }

    // ---------- التحفيظ بالتكرار ----------

    private void styleChipBtn(Button b, boolean selected) {
        if (b == null) return;
        b.setBackground(rounded(selected ? accentColor : chipColor, 22));
        b.setTextColor(selected ? 0xFF201503 : textColor);
    }

    private void styleTahfizChips() {
        for (int i = 0; i < reciterChips.length; i++) styleChipBtn(reciterChips[i], i == tahfizReciter);
        for (int i = 0; i < repRangeChips.length; i++) styleChipBtn(repRangeChips[i], i == repRangeIdx);
        boolean full = RECITER_MODE[tahfizReciter] == TahfizService.MODE_FULL;
        if (tahfizNote != null) {
            tahfizNote.setVisibility(full ? View.VISIBLE : View.GONE);
        }
        // مع التلاوة الكاملة (الهراز) لا تنطبق خانات الآيات: تُعطَّل بصرياً حتى لا تُخدع
        if (fromInput != null) { fromInput.setEnabled(!full); fromInput.setAlpha(full ? 0.4f : 1f); }
        if (toInput != null) { toInput.setEnabled(!full); toInput.setAlpha(full ? 0.4f : 1f); }
        if (repAyahSeek != null) { repAyahSeek.setEnabled(!full); repAyahSeek.setAlpha(full ? 0.4f : 1f); }
        if (repAyahLabel != null) { repAyahLabel.setAlpha(full ? 0.4f : 1f); }
    }

    private LinearLayout buildTahfizCard() {
        LinearLayout c = card();
        TextView title = label(tr("tahfiz"), 19, accentColor, true);
        title.setGravity(Gravity.CENTER);
        c.addView(title);
        c.addView(label(tr("teacher"), 14, subColor, false));
        tahfizReciter = prefs.getInt("tahfiz_reciter", 4);
        if (tahfizReciter < 0 || tahfizReciter >= RECITER_NAMES.length) tahfizReciter = 4;
        int[] oldRepVals = {1, 2, 3, 5, 10};
        repAyahVal = prefs.getInt("tahfiz_rep_ayah_val",
                oldRepVals[Math.max(0, Math.min(4, prefs.getInt("tahfiz_rep_ayah", 0)))]);
        repRangeIdx = prefs.getInt("tahfiz_rep_range", 0);
        for (int row = 0; row < 3; row++) {
            LinearLayout r = new LinearLayout(this);
            r.setOrientation(LinearLayout.HORIZONTAL);
            r.setGravity(Gravity.CENTER);
            for (int k = 0; k < 3; k++) {
                final int idx = row * 3 + k;
                if (idx >= RECITER_NAMES.length) break;
                Button b = chip(RECITER_NAMES[idx] + "\n" + RECITER_TAGS[idx], idx == tahfizReciter, new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        tahfizReciter = idx;
                        prefs.edit().putInt("tahfiz_reciter", idx).apply();
                        styleTahfizChips();
                    }
                });
                b.setTextSize(13);
                LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0, dp(56), 1f);
                blp.setMargins(dp(3), dp(4), dp(3), dp(4));
                b.setLayoutParams(blp);
                reciterChips[idx] = b;
                r.addView(b);
            }
            c.addView(r);
        }
        tahfizNote = label(tr("fullNote"), 12, subColor, false);
        c.addView(tahfizNote);

        LinearLayout rangeRow = new LinearLayout(this);
        rangeRow.setOrientation(LinearLayout.HORIZONTAL);
        rangeRow.setGravity(Gravity.CENTER);
        fromInput = ayahInput(prefs.getInt("tahfiz_from", 1));
        toInput = ayahInput(prefs.getInt("tahfiz_to", 286));
        rangeRow.addView(ayahCol(tr("fromAyah"), fromInput));
        rangeRow.addView(ayahCol(tr("toAyah"), toInput));
        c.addView(rangeRow);

        repAyahLabel = label(tr("repAyah") + " · ×" + repAyahVal, 14, subColor, false);
        c.addView(repAyahLabel);
        repAyahSeek = new SeekBar(this);
        repAyahSeek.setMax(99);
        repAyahSeek.setProgress(Math.max(0, Math.min(99, repAyahVal - 1)));
        repAyahSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean fromUser) {
                repAyahVal = p + 1;
                repAyahLabel.setText(tr("repAyah") + " · ×" + repAyahVal);
                prefs.edit().putInt("tahfiz_rep_ayah_val", repAyahVal).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) { }
            @Override public void onStopTrackingTouch(SeekBar sb) { }
        });
        c.addView(repAyahSeek);

        c.addView(label(tr("repRange"), 14, subColor, false));
        LinearLayout rrRow = new LinearLayout(this);
        rrRow.setOrientation(LinearLayout.HORIZONTAL);
        rrRow.setGravity(Gravity.CENTER);
        for (int i = 0; i < REP_RANGE_VALS.length; i++) {
            final int idx = i;
            Button b = chip(REP_RANGE_VALS[i] == 1 ? tr("noRepeat") : "×" + REP_RANGE_VALS[i], i == repRangeIdx, new View.OnClickListener() {
                @Override public void onClick(View v) {
                    repRangeIdx = idx;
                    prefs.edit().putInt("tahfiz_rep_range", idx).apply();
                    styleTahfizChips();
                }
            });
            repRangeChips[i] = b;
            rrRow.addView(b);
        }
        c.addView(rrRow);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);
        tahfizStartBtn = chip(tr("startLearn"), true, new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (TahfizService.playing) stopTahfiz(); else startTahfiz();
            }
        });
        tahfizPauseBtn = chip(tr("pauseW"), false, new View.OnClickListener() {
            @Override public void onClick(View v) { toggleTahfizPause(); }
        });
        btnRow.addView(tahfizStartBtn);
        btnRow.addView(tahfizPauseBtn);
        c.addView(btnRow);

        tahfizStatus = label(tr("needNet"), 14, textColor, true);
        tahfizStatus.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams stLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stLp.topMargin = dp(8);
        tahfizStatus.setLayoutParams(stLp);
        c.addView(tahfizStatus);
        styleTahfizChips();
        return c;
    }

    private EditText ayahInput(int val) {
        EditText e = new EditText(this);
        e.setText(String.valueOf(val));
        e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        e.setGravity(Gravity.CENTER);
        e.setTextSize(18);
        e.setTextColor(textColor);
        e.setBackground(rounded(chipColor, 14));
        e.setLayoutParams(new LinearLayout.LayoutParams(dp(96), dp(52)));
        return e;
    }

    private LinearLayout ayahCol(String lbl, EditText input) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(10), dp(6), dp(10), dp(6));
        col.setLayoutParams(lp);
        TextView t = label(lbl, 14, subColor, false);
        t.setGravity(Gravity.CENTER);
        col.addView(t);
        col.addView(input);
        return col;
    }

    private int parseAyah(EditText e, int fallback) {
        try {
            int v = Integer.parseInt(e.getText().toString().trim());
            return Math.max(1, Math.min(286, v));
        } catch (Exception ex) {
            return fallback;
        }
    }

    private void startTahfiz() {
        int tFrom = parseAyah(fromInput, 1);
        int tTo = parseAyah(toInput, 286);
        if (tFrom > tTo) { int tmp = tFrom; tFrom = tTo; tTo = tmp; }
        fromInput.setText(String.valueOf(tFrom));
        toInput.setText(String.valueOf(tTo));
        prefs.edit().putInt("tahfiz_from", tFrom).putInt("tahfiz_to", tTo).apply();
        Intent i = new Intent(this, TahfizService.class);
        i.setAction(TahfizService.ACTION_START);
        i.putExtra("mode", RECITER_MODE[tahfizReciter]);
        i.putExtra("base", RECITER_BASE[tahfizReciter]);
        i.putExtra("audio", RECITER_BASE[tahfizReciter] + "002.mp3");
        i.putExtra("timing", RECITER_TIMING[tahfizReciter]);
        i.putExtra("name", RECITER_NAMES[tahfizReciter]);
        i.putExtra("from", tFrom);
        i.putExtra("to", tTo);
        i.putExtra("repAyah", repAyahVal);
        i.putExtra("repRange", REP_RANGE_VALS[repRangeIdx]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i);
        else startService(i);
        TahfizService.statusText = tr("loadingW") + " " + arNum(tFrom);
        syncTahfizUi();
    }

    private void stopTahfiz() {
        Intent i = new Intent(this, TahfizService.class);
        i.setAction(TahfizService.ACTION_STOP);
        startService(i);
        syncTahfizUi();
    }

    private void toggleTahfizPause() {
        if (!TahfizService.playing) return;
        Intent i = new Intent(this, TahfizService.class);
        i.setAction(TahfizService.ACTION_PAUSE);
        startService(i);
        tahfizPauseBtn.setText(TahfizService.paused ? tr("pauseW") : tr("resumeW"));
    }

    private void syncTahfizUi() {
        if (tahfizStatus != null && TahfizService.statusText != null
                && !TahfizService.statusText.isEmpty()) {
            tahfizStatus.setText(TahfizService.statusText);
        }
        if (tahfizStartBtn != null) {
            tahfizStartBtn.setText(TahfizService.playing ? tr("stopLearn") : tr("startLearn"));
        }
        if (tahfizPauseBtn != null) {
            tahfizPauseBtn.setText(TahfizService.paused ? tr("resumeW") : tr("pauseW"));
        }
        // تظليل الآية الجارية في النص وتمرير الشاشة إليها أثناء التلاوة
        int ca = TahfizService.playing ? TahfizService.currentAyah : 0;
        if (ca != lastHighlight) {
            lastHighlight = ca;
            if (quranBody != null && activeTab == 2) {
                applyQuranText();
                if (ca > 0) scrollToAyah(ca);
            }
        }
    }

    // ---------- قسم الإحصائيات ----------
    private LinearLayout buildStatsSection() {
        LinearLayout s = new LinearLayout(this);
        s.setOrientation(LinearLayout.VERTICAL);
        s.setPadding(dp(22), dp(28), dp(22), dp(24));
        s.addView(label(tr("tabStats"), 28, textColor, true));
        statsBox = new LinearLayout(this);
        statsBox.setOrientation(LinearLayout.VERTICAL);
        s.addView(statsBox);
        return s;
    }

    private String dayKey(Calendar c) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
    }

    private int sumFrom(Map<String, Integer> daily, Calendar from) {
        Calendar day = (Calendar) from.clone();
        Calendar today = Calendar.getInstance();
        int sum = 0;
        while (true) {
            Integer v = daily.get(dayKey(day));
            if (v != null) sum += v;
            if (day.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && day.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) break;
            day.add(Calendar.DAY_OF_MONTH, 1);
        }
        return sum;
    }

    private LinearLayout statRow(String title, int value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = label(title, 17, textColor, false);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(t);
        row.addView(label(String.valueOf(value), 22, accentColor, true));
        return row;
    }

    private void refreshStats() {
        if (statsBox == null) return;
        statsBox.removeAllViews();
        Map<String, Integer> daily = TasbihData.loadDaily(this);

        Calendar todayStart = Calendar.getInstance();
        Calendar weekStart = Calendar.getInstance();
        weekStart.setFirstDayOfWeek(Calendar.MONDAY);
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Calendar monthStart = Calendar.getInstance();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        Calendar yearStart = Calendar.getInstance();
        yearStart.set(Calendar.DAY_OF_YEAR, 1);

        int allTime = 0;
        for (TasbihData.Item it : items) allTime += it.count;

        LinearLayout sumCard = card();
        sumCard.addView(statRow(tr("today"), sumFrom(daily, todayStart)));
        sumCard.addView(statRow(tr("thisWeek"), sumFrom(daily, weekStart)));
        sumCard.addView(statRow(tr("thisMonth"), sumFrom(daily, monthStart)));
        sumCard.addView(statRow(tr("thisYear"), sumFrom(daily, yearStart)));
        sumCard.addView(statRow(tr("allTotal"), allTime));
        statsBox.addView(sumCard);

        statsBox.addView(sectionTitle(tr("last7")));
        LinearLayout chartCard = card();
        int[] vals = new int[7];
        String[] labs = new String[7];
        Calendar d = Calendar.getInstance();
        d.add(Calendar.DAY_OF_MONTH, -6);
        for (int i = 0; i < 7; i++) {
            Integer v = daily.get(dayKey(d));
            vals[i] = v == null ? 0 : v;
            labs[i] = String.valueOf(d.get(Calendar.DAY_OF_MONTH));
            d.add(Calendar.DAY_OF_MONTH, 1);
        }
        BarChartView chart = new BarChartView(this);
        chart.setData(vals, labs, accentColor, textColor);
        chart.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(170)));
        chartCard.addView(chart);
        statsBox.addView(chartCard);

        statsBox.addView(sectionTitle(tr("perDhikr")));
        LinearLayout perCard = card();
        for (int i = 0; i < items.size(); i++) {
            perCard.addView(statRow(displayName(i), items.get(i).count));
        }
        statsBox.addView(perCard);

        TextView note = label(tr("statsNote"), 12, subColor, false);
        statsBox.addView(note);

        Button clearBtn = new Button(this);
        clearBtn.setText(tr("clearStats"));
        clearBtn.setAllCaps(false);
        clearBtn.setTextColor(accentColor);
        clearBtn.setBackgroundColor(Color.TRANSPARENT);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                TasbihData.clearDaily(MainActivity.this);
                refreshStats();
            }
        });
        statsBox.addView(clearBtn);
    }

    // ---------- قسم الإعدادات ----------
    private LinearLayout buildSettingsSection() {
        LinearLayout s = new LinearLayout(this);
        s.setOrientation(LinearLayout.VERTICAL);
        s.setPadding(dp(22), dp(28), dp(22), dp(24));

        // اللغة
        LinearLayout langCard = card();
        LinearLayout langRow = new LinearLayout(this);
        langRow.setOrientation(LinearLayout.HORIZONTAL);
        langRow.setGravity(Gravity.CENTER);
        String[] langNames = {"العربية", "English", "Français"};
        for (int i = 0; i < 3; i++) {
            final int li = i;
            langRow.addView(chip(langNames[i], lang == li, new View.OnClickListener() {
                @Override public void onClick(View v) {
                    lang = li;
                    prefs.edit().putInt("lang", lang).apply();
                    recreate();
                }
            }));
        }
        langCard.addView(langRow);

        TextView appLbl = label(tr("appearance"), 15, subColor, false);
        LinearLayout.LayoutParams apLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        apLp.topMargin = dp(14);
        appLbl.setLayoutParams(apLp);
        langCard.addView(appLbl);
        LinearLayout appRow = new LinearLayout(this);
        appRow.setOrientation(LinearLayout.HORIZONTAL);
        appRow.setGravity(Gravity.CENTER);
        String[] appKeys = {"appAuto", "light", "dark"};
        for (int i = 0; i < 3; i++) {
            final int ai = i;
            appRow.addView(chip(tr(appKeys[i]), appearance == ai, new View.OnClickListener() {
                @Override public void onClick(View v) {
                    appearance = ai;
                    prefs.edit().putInt("appearance", appearance).apply();
                    recreate();
                }
            }));
        }
        langCard.addView(appRow);
        s.addView(langCard);

        // التنبيه
        s.addView(sectionTitle(tr("alerts")));
        LinearLayout alertCard = card();
        alertCard.addView(switchRow(tr("vibration"), vibOn, new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean on) {
                vibOn = on;
                prefs.edit().putBoolean("vib_on", on).apply();
            }
        }));
        alertCard.addView(switchRow(tr("clickSound"), soundOn, new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean on) {
                soundOn = on;
                prefs.edit().putBoolean("sound_on", on).apply();
            }
        }));
        alertCard.addView(switchRow(tr("floating"), floatingOn, new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean on) {
                setFloating(on);
            }
        }));
        alertCard.addView(switchRow(tr("floatVib"), prefs.getBoolean("floating_vib", true), new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean on) {
                prefs.edit().putBoolean("floating_vib", on).apply();
            }
        }));
        alertCard.addView(switchRow(tr("floatSound"), prefs.getBoolean("floating_sound", false), new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean on) {
                prefs.edit().putBoolean("floating_sound", on).apply();
            }
        }));
        TextView fbLbl = label(tr("floatBg"), 15, subColor, false);
        LinearLayout.LayoutParams fbLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fbLp.topMargin = dp(12);
        fbLbl.setLayoutParams(fbLp);
        alertCard.addView(fbLbl);
        LinearLayout fbRow = new LinearLayout(this);
        fbRow.setOrientation(LinearLayout.HORIZONTAL);
        fbRow.setGravity(Gravity.CENTER);
        final int curBg = prefs.getInt("floating_bg", 0);
        for (int i = 0; i < TasbihData.FLOAT_BG.length; i++) {
            final int bi = i;
            View sw = new View(this);
            GradientDrawable swg = new GradientDrawable();
            swg.setShape(GradientDrawable.OVAL);
            swg.setColor(TasbihData.FLOAT_BG[i]);
            if (bi == curBg) swg.setStroke(dp(3), accentColor);
            sw.setBackground(swg);
            LinearLayout.LayoutParams swLp = new LinearLayout.LayoutParams(dp(42), dp(42));
            swLp.setMargins(dp(5), dp(6), dp(5), dp(6));
            sw.setLayoutParams(swLp);
            sw.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    prefs.edit().putInt("floating_bg", bi).apply();
                    if (FloatingService.running) {
                        startService(new Intent(MainActivity.this, FloatingService.class));
                    }
                    recreate();
                }
            });
            fbRow.addView(sw);
        }
        alertCard.addView(fbRow);
        final TextView fsLbl = label(tr("floatSize") + " · " + prefs.getInt("floating_size", 62), 15, subColor, false);
        LinearLayout.LayoutParams fsLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fsLp.topMargin = dp(12);
        fsLbl.setLayoutParams(fsLp);
        alertCard.addView(fsLbl);
        SeekBar fsSeek = new SeekBar(this);
        fsSeek.setMax(66);
        fsSeek.setProgress(Math.max(0, Math.min(66, prefs.getInt("floating_size", 62) - 44)));
        fsSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean fromUser) {
                int newSize = p + 44;
                fsLbl.setText(tr("floatSize") + " · " + newSize);
                prefs.edit().putInt("floating_size", newSize).apply();
                if (FloatingService.running) {
                    startService(new Intent(MainActivity.this, FloatingService.class));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) { }
            @Override public void onStopTrackingTouch(SeekBar sb) { }
        });
        alertCard.addView(fsSeek);
        TextView fh = label(tr("floatingHint"), 12, subColor, false);
        alertCard.addView(fh);
        s.addView(alertCard);

        // تذكير الاستغفار الصوتي
        s.addView(sectionTitle(tr("reminder")));
        LinearLayout remCard = card();
        remCard.addView(switchRow(tr("reminderOn"), prefs.getBoolean("reminder_on", false), new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean on) {
                prefs.edit().putBoolean("reminder_on", on).apply();
                if (on) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        try { requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 77); }
                        catch (Exception ignored) { }
                    }
                    ReminderScheduler.schedule(MainActivity.this);
                    Toast.makeText(MainActivity.this,
                            tr("reminderSet") + " " + reminderTimeText(), Toast.LENGTH_SHORT).show();
                } else {
                    ReminderScheduler.cancel(MainActivity.this);
                }
            }
        }));
        LinearLayout timeRow = new LinearLayout(this);
        timeRow.setOrientation(LinearLayout.HORIZONTAL);
        timeRow.setGravity(Gravity.CENTER_VERTICAL);
        timeRow.addView(label(tr("reminderTime"), 15, subColor, false));
        final TextView timeVal = label("🕗 " + reminderTimeText(), 16, textColor, true);
        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvLp.setMargins(dp(14), 0, dp(14), 0);
        timeVal.setLayoutParams(tvLp);
        timeVal.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                int h = prefs.getInt("reminder_hour", 8);
                int m = prefs.getInt("reminder_min", 0);
                new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        prefs.edit().putInt("reminder_hour", hourOfDay)
                                .putInt("reminder_min", minute).apply();
                        timeVal.setText("🕗 " + reminderTimeText());
                        if (prefs.getBoolean("reminder_on", false)) {
                            ReminderScheduler.schedule(MainActivity.this);
                        }
                    }
                }, h, m, true).show();
            }
        });
        timeRow.addView(timeVal);
        LinearLayout.LayoutParams trLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        trLp.topMargin = dp(8);
        timeRow.setLayoutParams(trLp);
        remCard.addView(timeRow);
        LinearLayout playRow = new LinearLayout(this);
        playRow.setOrientation(LinearLayout.HORIZONTAL);
        playRow.setGravity(Gravity.CENTER);
        playRow.addView(chip(tr("playVerse"), false, new View.OnClickListener() {
            @Override public void onClick(View v) { playVerse(); }
        }));
        remCard.addView(playRow);
        s.addView(remCard);

        // العد التلقائي
        s.addView(sectionTitle(tr("autoCount")));
        LinearLayout autoCard = card();
        final TextView setInterval = label(tr("interval") + " · " + intervalText() + " " + tr("sec"), 15, textColor, false);
        autoCard.addView(setInterval);
        SeekBar seek = new SeekBar(this);
        seek.setMax(19);
        seek.setProgress(Math.max(0, Math.min(19, autoIntervalHalf - 1)));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean fromUser) {
                autoIntervalHalf = p + 1;
                setInterval.setText(tr("interval") + " · " + intervalText() + " " + tr("sec"));
                prefs.edit().putInt("auto_interval_half", autoIntervalHalf).apply();
                if (counterSeek != null) counterSeek.setProgress(p);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) { }
            @Override public void onStopTrackingTouch(SeekBar sb) { }
        });
        autoCard.addView(seek);
        s.addView(autoCard);

        // الخلفية
        s.addView(sectionTitle(tr("background")));
        LinearLayout bgCard = card();
        LinearLayout swRow = new LinearLayout(this);
        swRow.setOrientation(LinearLayout.HORIZONTAL);
        swRow.setGravity(Gravity.CENTER);
        for (int i = 0; i < 4; i++) {
            final int ti = i;
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            cell.setLayoutParams(cLp);
            View sw = new View(this);
            GradientDrawable swg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, THEME_BG[ti]);
            swg.setCornerRadius(dp(14));
            if (theme == ti && !customBg) swg.setStroke(dp(3), accentColor);
            sw.setBackground(swg);
            sw.setLayoutParams(new LinearLayout.LayoutParams(dp(58), dp(58)));
            sw.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    theme = ti;
                    customBg = false;
                    prefs.edit().putInt("theme", theme).putBoolean("custom_bg", false).apply();
                    recreate();
                }
            });
            cell.addView(sw);
            TextView nm = label(tr("theme" + ti), 11, subColor, false);
            nm.setGravity(Gravity.CENTER);
            cell.addView(nm);
            swRow.addView(cell);
        }
        bgCard.addView(swRow);
        Button photoBtn = new Button(this);
        photoBtn.setText(tr("photo"));
        photoBtn.setAllCaps(false);
        photoBtn.setBackground(rounded(chipColor, 22));
        photoBtn.setTextColor(textColor);
        LinearLayout.LayoutParams phLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(44));
        phLp.topMargin = dp(14);
        phLp.gravity = Gravity.START;
        photoBtn.setLayoutParams(phLp);
        photoBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent pick = new Intent(Intent.ACTION_PICK);
                pick.setType("image/*");
                startActivityForResult(pick, 42);
            }
        });
        bgCard.addView(photoBtn);
        s.addView(bgCard);

        TextView footer = label(tr("footer"), 13, subColor, false);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fLp.topMargin = dp(20);
        footer.setLayoutParams(fLp);
        s.addView(footer);
        return s;
    }

    private LinearLayout switchRow(String text, boolean on, CompoundButton.OnCheckedChangeListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = label(text, 16, textColor, false);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(t);
        Switch sw = new Switch(this);
        sw.setChecked(on);
        sw.setOnCheckedChangeListener(listener);
        row.addView(sw);
        return row;
    }

    // ---------- الحوارات ----------
    private void showCustomGoalDialog() {
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint(tr("targetHint"));
        TasbihData.Item c = cur();
        if (c != null) input.setText(String.valueOf(c.target));
        new AlertDialog.Builder(this)
                .setTitle(tr("goal"))
                .setView(input)
                .setPositiveButton(tr("add"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        try {
                            int v = Integer.parseInt(input.getText().toString().trim());
                            if (v > 0) {
                                TasbihData.Item cc = cur();
                                if (cc != null) {
                                    cc.target = v;
                                    TasbihData.save(MainActivity.this, items);
                                }
                                recreate();
                            }
                        } catch (NumberFormatException ignored) { }
                    }
                })
                .setNegativeButton(tr("cancel"), null)
                .show();
    }

    private void showAddDhikrDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(8), dp(20), 0);
        final EditText nameIn = new EditText(this);
        nameIn.setHint(tr("nameHint"));
        final EditText meaningIn = new EditText(this);
        meaningIn.setHint(tr("meaningHint"));
        final EditText targetIn = new EditText(this);
        targetIn.setHint(tr("targetHint"));
        targetIn.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        targetIn.setText("100");
        box.addView(nameIn);
        box.addView(meaningIn);
        box.addView(targetIn);
        new AlertDialog.Builder(this)
                .setTitle(tr("addTitle"))
                .setView(box)
                .setPositiveButton(tr("add"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        String n = nameIn.getText().toString().trim();
                        if (n.isEmpty()) return;
                        int t = 100;
                        try { t = Integer.parseInt(targetIn.getText().toString().trim()); } catch (Exception ignored) { }
                        if (t <= 0) t = 100;
                        items.add(new TasbihData.Item(n, meaningIn.getText().toString().trim(), t, 0));
                        TasbihData.save(MainActivity.this, items);
                        refreshAdhkar();
                    }
                })
                .setNegativeButton(tr("cancel"), null)
                .show();
    }

    // ---------- الزر العائم ----------
    private void setFloating(boolean on) {
        floatingOn = on;
        prefs.edit().putBoolean("floating_on", on).apply();
        if (on) {
            if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, tr("needOverlay"), Toast.LENGTH_LONG).show();
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(i);
            } else {
                startService(new Intent(this, FloatingService.class));
            }
        } else {
            stopService(new Intent(this, FloatingService.class));
        }
        recreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        items = TasbihData.load(this);
        floatingOn = prefs.getBoolean("floating_on", false);
        if (floatingOn && Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(this)
                && !FloatingService.running) {
            startService(new Intent(this, FloatingService.class));
        }
        refreshCounter();
        refreshAdhkar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 42 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                InputStream is = getContentResolver().openInputStream(data.getData());
                Bitmap bm = BitmapFactory.decodeStream(is);
                if (is != null) is.close();
                if (bm != null) {
                    float scale = Math.min(1f, 1200f / Math.max(bm.getWidth(), bm.getHeight()));
                    Bitmap scaled = scale < 1f
                            ? Bitmap.createScaledBitmap(bm, (int) (bm.getWidth() * scale), (int) (bm.getHeight() * scale), true)
                            : bm;
                    FileOutputStream fos = openFileOutput("bg.jpg", MODE_PRIVATE);
                    scaled.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                    fos.close();
                    customBg = true;
                    prefs.edit().putBoolean("custom_bg", true).apply();
                    recreate();
                }
            } catch (Exception ignored) { }
        }
    }

    @Override
    protected void onPause() {
        // حفظ مكان القراءة أيضاً عند إيقاف التطبيق مؤقتاً (وليس فقط عند تبديل التبويب)
        if (activeTab == 2 && mainScroll != null && prefs != null) {
            prefs.edit().putInt("qpos_" + riwaya, mainScroll.getScrollY()).apply();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        sAutoResume = autoRunning;
        autoRunning = false;
        autoHandler.removeCallbacks(autoRunnable);
        if (tone != null) tone.release();
        if (testPlayer != null) {
            try { testPlayer.release(); } catch (Exception ignored) { }
            testPlayer = null;
        }
        statusHandler.removeCallbacks(statusTick);
        super.onDestroy();
    }
}
