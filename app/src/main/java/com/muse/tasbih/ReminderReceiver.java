package com.muse.tasbih;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;

/** عند حلول وقت التذكير: إشعار + تشغيل آية الاستغفار صوتياً. */
public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        // ---- الإشعار ----
        try {
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                String channelId = "istighfar_reminder";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel ch = new NotificationChannel(channelId,
                            "تذكير الاستغفار", NotificationManager.IMPORTANCE_HIGH);
                    nm.createNotificationChannel(ch);
                }
                Intent open = new Intent(ctx, MainActivity.class);
                open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) piFlags |= PendingIntent.FLAG_IMMUTABLE;
                PendingIntent pi = PendingIntent.getActivity(ctx, 43, open, piFlags);
                Notification.Builder b = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? new Notification.Builder(ctx, channelId)
                        : new Notification.Builder(ctx);
                b.setSmallIcon(ctx.getApplicationInfo().icon);
                b.setContentTitle("🔔 تذكير الاستغفار");
                b.setContentText("فَقُلْتُ اسْتَغْفِرُوا رَبَّكُمْ إِنَّهُ كَانَ غَفَّارًا");
                b.setStyle(new Notification.BigTextStyle()
                        .bigText("فَقُلْتُ اسْتَغْفِرُوا رَبَّكُمْ إِنَّهُ كَانَ غَفَّارًا ۝ يُرْسِلِ السَّمَاءَ عَلَيْكُم مِّدْرَارًا ۝ وَيُمْدِدْكُم بِأَمْوَالٍ وَبَنِينَ وَيَجْعَل لَّكُمْ جَنَّاتٍ وَيَجْعَل لَّكُمْ أَنْهَارًا ﴿نوح: ١٠–١٢﴾\nحان وقت التسبيح والاستغفار 🤲"));
                b.setContentIntent(pi);
                b.setAutoCancel(true);
                nm.notify(1001, b.build());
            }
        } catch (Exception ignored) { }

        // ---- تشغيل الآيات صوتياً عبر خدمة (لإكمال المقطع كاملاً) ----
        try {
            ctx.startService(new Intent(ctx, ReminderPlayService.class));
            return;
        } catch (Exception ignored) { }
        // احتياط: التشغيل داخل المستقبل نفسه
        final PendingResult pr = goAsync();
        try {
            final MediaPlayer mp = MediaPlayer.create(ctx, R.raw.nuh);
            if (mp == null) {
                pr.finish();
                return;
            }
            mp.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build());
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override public void onCompletion(MediaPlayer m) {
                    try { m.release(); } catch (Exception ignored) { }
                    pr.finish();
                }
            });
            mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override public boolean onError(MediaPlayer m, int what, int extra) {
                    try { m.release(); } catch (Exception ignored) { }
                    pr.finish();
                    return true;
                }
            });
            mp.start();
        } catch (Exception e) {
            pr.finish();
        }
    }
}
