package com.muse.tasbih;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * خدمة التحفيظ بالتكرار: تشغيل في الخلفية بثلاثة أوضاع:
 * 0 = تلاوة كاملة (ملف السورة كاملاً)، 1 = ملف لكل آية،
 * 2 = توقيتات (بحث داخل ملف السورة حسب بداية/نهاية كل آية).
 */
public class TahfizService extends Service {

    public static final String ACTION_START = "com.muse.tasbih.TAHFIZ_START";
    public static final String ACTION_STOP = "com.muse.tasbih.TAHFIZ_STOP";
    public static final String ACTION_PAUSE = "com.muse.tasbih.TAHFIZ_PAUSE";

    public static final int MODE_FULL = 0;
    public static final int MODE_AYAH = 1;
    public static final int MODE_TIMED = 2;

    public static boolean playing = false;
    public static boolean paused = false;
    public static String statusText = "";
    public static int currentAyah = 0;

    private static final String CHANNEL = "tahfiz";
    private static final int NOTIF_ID = 2002;

    private MediaPlayer player;
    private PowerManager.WakeLock wakeLock;
    private final Handler poller = new Handler();
    private boolean prepared = false;

    private int mode = MODE_AYAH;
    private String base;
    private String audioUrl;
    private String reciterName = "";
    private long[] tStart, tEnd;
    private int fromAyah = 1, toAyah = 286, curAyah = 1;
    private int repAyah = 1, repRange = 1, ayahRepLeft = 1, rangeRepLeft = 1;

    private final Runnable pollTask = new Runnable() {
        @Override public void run() {
            if (playing && mode == MODE_TIMED && player != null && prepared && !paused) {
                try {
                    if (player.isPlaying() && tEnd != null
                            && player.getCurrentPosition() >= tEnd[curAyah - 1] - 40) {
                        nextStep();
                    }
                } catch (Exception ignored) { }
            }
            if (playing) poller.postDelayed(this, 200);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.createNotificationChannel(new NotificationChannel(CHANNEL,
                        "التحفيظ بالتكرار", NotificationManager.IMPORTANCE_LOW));
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopPlayback(false);
            return START_NOT_STICKY;
        }
        if (intent != null && ACTION_PAUSE.equals(intent.getAction())) {
            if (!playing) {
                stopSelf();
                return START_NOT_STICKY;
            }
            togglePause();
            return START_NOT_STICKY;
        }
        if (intent != null && ACTION_START.equals(intent.getAction())) {
            mode = intent.getIntExtra("mode", MODE_AYAH);
            base = intent.getStringExtra("base");
            audioUrl = intent.getStringExtra("audio");
            reciterName = intent.getStringExtra("name");
            fromAyah = intent.getIntExtra("from", 1);
            toAyah = intent.getIntExtra("to", 286);
            repAyah = intent.getIntExtra("repAyah", 1);
            repRange = intent.getIntExtra("repRange", 1);
            curAyah = fromAyah;
            ayahRepLeft = repAyah;
            rangeRepLeft = repRange;
            playing = true;
            paused = false;
            prepared = false;
            tStart = null;
            tEnd = null;
            if (mode == MODE_TIMED) {
                loadTimings(intent.getStringExtra("timing"));
                if (tStart == null) {
                    statusText = "تعذّر تحميل توقيتات الآيات";
                    playing = false;
                    stopSelf();
                    return START_NOT_STICKY;
                }
            }
            statusText = "⏳ جارٍ بدء التحفيظ… الآية " + arNum(fromAyah) + " · " + reciterName;
            startForeground(NOTIF_ID, buildNotification());
            acquireWakeLock();
            releasePlayer();
            poller.removeCallbacks(pollTask);
            poller.postDelayed(pollTask, 300);
            playCurrent();
        }
        return START_NOT_STICKY;
    }

    private void loadTimings(String asset) {
        if (asset == null) return;
        try {
            InputStream in = getAssets().open(asset);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            List<long[]> rows = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.trim().split(",");
                if (p.length == 2) rows.add(new long[]{Long.parseLong(p[0]), Long.parseLong(p[1])});
            }
            br.close();
            if (rows.size() > 0) {
                tStart = new long[rows.size()];
                tEnd = new long[rows.size()];
                for (int i = 0; i < rows.size(); i++) {
                    tStart[i] = rows.get(i)[0];
                    tEnd[i] = rows.get(i)[1];
                }
            }
        } catch (Exception ignored) { }
    }

    private String ayahUrl() {
        return base + String.format(Locale.US, "002%03d.mp3", curAyah);
    }

    private void playCurrent() {
        if (mode == MODE_AYAH) {
            playAyahFile();
        } else if (mode == MODE_TIMED) {
            if (player == null || !prepared) prepareLongFile(true);
            else seekPlayCurrent();
        } else {
            if (player == null || !prepared) prepareLongFile(false);
            else {
                try { player.seekTo(0); if (!paused) player.start(); } catch (Exception ignored) { }
                updateStatus();
                refreshNotification();
            }
        }
    }

    private void prepareLongFile(final boolean seekInto) {
        releasePlayer();
        prepared = false;
        updateStatus();
        try {
            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build());
            player.setDataSource(mode == MODE_TIMED || mode == MODE_FULL ? audioUrl : base);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override public void onPrepared(MediaPlayer mp) {
                    prepared = true;
                    if (!playing) return;
                    if (seekInto) seekPlayCurrent();
                    else {
                        try { if (!paused) mp.start(); } catch (Exception ignored) { }
                        updateStatus();
                        refreshNotification();
                    }
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override public void onCompletion(MediaPlayer mp) { onFileCompleted(); }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override public boolean onError(MediaPlayer mp, int what, int extra) {
                    statusText = "تعذّر التشغيل — تحقق من الإنترنت";
                    stopPlayback(false);
                    return true;
                }
            });
            player.prepareAsync();
        } catch (Exception e) {
            statusText = "تعذّر التشغيل — تحقق من الإنترنت";
            stopPlayback(false);
        }
    }

    private void seekPlayCurrent() {
        if (player == null || tStart == null) return;
        try {
            player.seekTo((int) tStart[curAyah - 1]);
            if (!paused) player.start();
        } catch (Exception ignored) { }
        updateStatus();
        refreshNotification();
    }

    private void playAyahFile() {
        releasePlayer();
        updateStatus();
        try {
            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build());
            player.setDataSource(ayahUrl());
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override public void onPrepared(MediaPlayer mp) {
                    prepared = true;
                    if (playing && !paused) {
                        try { mp.start(); } catch (Exception ignored) { }
                    }
                    updateStatus();
                    refreshNotification();
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override public void onCompletion(MediaPlayer mp) { nextStep(); }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override public boolean onError(MediaPlayer mp, int what, int extra) {
                    statusText = "تعذّر التشغيل — تحقق من الإنترنت";
                    stopPlayback(false);
                    return true;
                }
            });
            player.prepareAsync();
            refreshNotification();
        } catch (Exception e) {
            statusText = "تعذّر التشغيل — تحقق من الإنترنت";
            stopPlayback(false);
        }
    }

    private void onFileCompleted() {
        if (!playing) return;
        if (mode == MODE_FULL) {
            if (rangeRepLeft > 1) { rangeRepLeft--; playCurrent(); }
            else stopPlayback(true);
            return;
        }
        // وضع التوقيتات: نهاية الملف تعني نهاية آخر آية في المقطع
        if (curAyah >= toAyah) {
            if (rangeRepLeft > 1) {
                rangeRepLeft--;
                curAyah = fromAyah;
                ayahRepLeft = repAyah;
                seekPlayCurrent();
            } else {
                stopPlayback(true);
            }
        } else {
            nextStep();
        }
    }

    private void nextStep() {
        if (!playing) return;
        if (mode == MODE_FULL) {
            if (rangeRepLeft > 1) { rangeRepLeft--; playCurrent(); }
            else stopPlayback(true);
            return;
        }
        if (ayahRepLeft > 1) { ayahRepLeft--; playCurrent(); return; }
        if (curAyah < toAyah) {
            curAyah++;
            ayahRepLeft = repAyah;
            playCurrent();
            return;
        }
        if (rangeRepLeft > 1) {
            rangeRepLeft--;
            curAyah = fromAyah;
            ayahRepLeft = repAyah;
            playCurrent();
            return;
        }
        stopPlayback(true);
    }

    private void togglePause() {
        if (!playing || player == null) return;
        try {
            if (paused) {
                player.start();
                paused = false;
            } else {
                player.pause();
                paused = true;
            }
            updateStatus();
            refreshNotification();
        } catch (Exception ignored) { }
    }

    private void updateStatus() {
        currentAyah = (mode == MODE_FULL) ? 0 : curAyah;
        if (mode == MODE_FULL) {
            int round = repRange - rangeRepLeft + 1;
            statusText = "🎧 " + reciterName + " · سورة البقرة"
                    + (repRange > 1 ? " · " + round + "/" + repRange : "")
                    + (paused ? " · ⏸" : "");
        } else {
            int repDone = repAyah - ayahRepLeft + 1;
            int round = repRange - rangeRepLeft + 1;
            statusText = "🎧 الآية " + arNum(curAyah) + " · " + repDone + "/" + repAyah
                    + (repRange > 1 ? " · " + round + "/" + repRange : "")
                    + " · " + reciterName
                    + (paused ? " · ⏸" : "");
        }
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

    private Notification buildNotification() {
        Intent open = new Intent(this, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) piFlags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent openPi = PendingIntent.getActivity(this, 51, open, piFlags);
        PendingIntent pausePi = PendingIntent.getService(this, 52,
                new Intent(this, TahfizService.class).setAction(ACTION_PAUSE), piFlags);
        PendingIntent stopPi = PendingIntent.getService(this, 53,
                new Intent(this, TahfizService.class).setAction(ACTION_STOP), piFlags);
        Notification.Builder b = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL)
                : new Notification.Builder(this);
        b.setSmallIcon(getApplicationInfo().icon);
        b.setContentTitle("🎧 التحفيظ بالتكرار — سورة البقرة");
        b.setContentText(statusText);
        b.setContentIntent(openPi);
        b.setOngoing(true);
        b.addAction(android.R.drawable.ic_media_pause, paused ? "استئناف" : "إيقاف مؤقت", pausePi);
        b.addAction(android.R.drawable.ic_menu_close_clear_cancel, "إيقاف", stopPi);
        return b.build();
    }

    private void refreshNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null && playing) nm.notify(NOTIF_ID, buildNotification());
    }

    private void acquireWakeLock() {
        try {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "hijazi:tahfiz");
                wakeLock.acquire(6 * 60 * 60 * 1000L);
            }
        } catch (Exception ignored) { }
    }

    private void releasePlayer() {
        if (player != null) {
            try { player.stop(); } catch (Exception ignored) { }
            try { player.release(); } catch (Exception ignored) { }
            player = null;
        }
        prepared = false;
    }

    private void stopPlayback(boolean finished) {
        playing = false;
        paused = false;
        currentAyah = 0;
        poller.removeCallbacks(pollTask);
        releasePlayer();
        if (wakeLock != null) {
            try { if (wakeLock.isHeld()) wakeLock.release(); } catch (Exception ignored) { }
            wakeLock = null;
        }
        if (finished) statusText = "✅ أكملت المقطع، بارك الله فيك";
        else if (statusText == null || !statusText.startsWith("تعذّر")) statusText = "⏹ توقّف التحفيظ";
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        playing = false;
        paused = false;
        poller.removeCallbacks(pollTask);
        releasePlayer();
        if (wakeLock != null) {
            try { if (wakeLock.isHeld()) wakeLock.release(); } catch (Exception ignored) { }
            wakeLock = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
