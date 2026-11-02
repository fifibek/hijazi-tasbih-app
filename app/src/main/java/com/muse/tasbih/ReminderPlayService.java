package com.muse.tasbih;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.IBinder;

/** تشغيل آيات الاستغفار (نوح ١٠–١٢) كاملة عند حلول وقت التذكير. */
public class ReminderPlayService extends Service {

    private MediaPlayer mp;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (mp != null) {
                try { mp.stop(); } catch (Exception ignored) { }
                mp.release();
                mp = null;
            }
            mp = MediaPlayer.create(this, R.raw.nuh);
            if (mp == null) {
                stopSelf();
                return START_NOT_STICKY;
            }
            mp.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build());
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override public void onCompletion(MediaPlayer m) { stopSelf(); }
            });
            mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override public boolean onError(MediaPlayer m, int what, int extra) {
                    stopSelf();
                    return true;
                }
            });
            mp.start();
        } catch (Exception e) {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mp != null) {
            try { mp.stop(); } catch (Exception ignored) { }
            try { mp.release(); } catch (Exception ignored) { }
            mp = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
