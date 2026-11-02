package com.muse.tasbih;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/** زر عائم فوق التطبيقات الأخرى: اضغط للعدّ، اسحب للتحريك، × للإغلاق. */
public class FloatingService extends Service {

    public static boolean running = false;

    private WindowManager wm;
    private View rootView;
    private TextView countText;
    private WindowManager.LayoutParams params;
    private ToneGenerator tone;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        try {
            tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        } catch (Exception ignored) { }
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);

        countText = new TextView(this);
        countText.setTextSize(20);
        countText.setGravity(Gravity.CENTER);
        applyStyle();
        applySize();

        TextView close = new TextView(this);
        close.setText("×");
        close.setTextColor(0xFFFFD54F);
        close.setTextSize(18);
        close.setGravity(Gravity.CENTER);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TasbihData.prefs(FloatingService.this).edit().putBoolean("floating_on", false).apply();
                stopSelf();
            }
        });

        box.addView(countText);
        box.addView(close);
        rootView = box;

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = dp(24);
        params.y = dp(220);

        countText.setOnTouchListener(new View.OnTouchListener() {
            private int startX, startY;
            private float touchX, touchY;
            private boolean moved;

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = params.x;
                        startY = params.y;
                        touchX = e.getRawX();
                        touchY = e.getRawY();
                        moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (e.getRawX() - touchX);
                        int dy = (int) (e.getRawY() - touchY);
                        if (Math.abs(dx) > 12 || Math.abs(dy) > 12) moved = true;
                        params.x = startX + dx;
                        params.y = startY + dy;
                        try { wm.updateViewLayout(rootView, params); } catch (Exception ignored) { }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved) increment();
                        return true;
                }
                return false;
            }
        });

        refreshText();
        try {
            wm.addView(rootView, params);
        } catch (Exception e) {
            stopSelf();
        }
    }

    /** تطبيق خلفية الزر العائم المختارة من الإعدادات. */
    public void applyStyle() {
        if (countText == null) return;
        int idx = TasbihData.prefs(this).getInt("floating_bg", 0);
        if (idx < 0 || idx >= TasbihData.FLOAT_BG.length) idx = 0;
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(TasbihData.FLOAT_BG[idx]);
        circle.setStroke(3, 0xFFFFD54F);
        countText.setBackground(circle);
        countText.setTextColor(TasbihData.FLOAT_TXT[idx]);
    }

    /** تطبيق حجم الزر العائم المختار من الإعدادات (44 إلى 110). */
    public void applySize() {
        if (countText == null) return;
        int sz = TasbihData.prefs(this).getInt("floating_size", 62);
        if (sz < 44) sz = 44;
        if (sz > 110) sz = 110;
        countText.setLayoutParams(new LinearLayout.LayoutParams(dp(sz), dp(sz)));
        countText.setTextSize(sz * 0.32f);
        if (rootView != null) rootView.requestLayout();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        applyStyle();
        applySize();
        refreshText();
        return START_NOT_STICKY;
    }

    private void increment() {
        List<TasbihData.Item> items = TasbihData.load(this);
        int idx = TasbihData.prefs(this).getInt("current", 0);
        if (idx < 0 || idx >= items.size()) idx = 0;
        TasbihData.Item it = items.get(idx);
        it.count++;
        TasbihData.save(this, items);
        TasbihData.recordToday(this);
        if (TasbihData.prefs(this).getBoolean("floating_vib", true)) {
            Vibrator tv = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (tv != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // أقصى قوة اهتزاز ومدة أطول لتكون محسوسة بوضوح
                    tv.vibrate(VibrationEffect.createOneShot(70, 255));
                } else {
                    tv.vibrate(70);
                }
            }
        }
        if (TasbihData.prefs(this).getBoolean("floating_sound", false) && tone != null) {
            try { tone.startTone(ToneGenerator.TONE_PROP_BEEP, 90); } catch (Exception ignored) { }
        }
        if (it.target > 0 && it.count == it.target) {
            Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vib != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vib.vibrate(VibrationEffect.createOneShot(350, 255));
                } else {
                    vib.vibrate(350);
                }
            }
        }
        refreshText();
    }

    private void refreshText() {
        List<TasbihData.Item> items = TasbihData.load(this);
        int idx = TasbihData.prefs(this).getInt("current", 0);
        if (idx < 0 || idx >= items.size()) idx = 0;
        countText.setText(String.valueOf(items.get(idx).count));
    }

    @Override
    public void onDestroy() {
        running = false;
        if (tone != null) tone.release();
        if (wm != null && rootView != null) {
            try { wm.removeView(rootView); } catch (Exception ignored) { }
        }
        super.onDestroy();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
