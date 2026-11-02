package com.muse.tasbih;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** إعادة جدولة تذكير الاستغفار بعد إعادة تشغيل الهاتف. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            ReminderScheduler.schedule(ctx);
        }
    }
}
