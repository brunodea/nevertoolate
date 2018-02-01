package br.brunodea.nevertoolate.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import br.brunodea.nevertoolate.db.NeverTooLateDB;
import br.brunodea.nevertoolate.model.NotificationModel;
import br.brunodea.nevertoolate.util.NotificationUtil;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "ON RECEIVE!");
        if (intent != null && intent.hasExtra(NotificationUtil.EXTRA_NOTIFICATION_MODEL_ID)) {
            Log.d(TAG, "intent not null and has EXTRA_NOTIFICATION_MODEL_ID!");
            long notification_id = intent.getLongExtra(NotificationUtil.EXTRA_NOTIFICATION_MODEL_ID, 0);
            NotificationModel nm = NeverTooLateDB.findNotificationByID(context, notification_id);
            if (nm != null) {
                Log.d(TAG, "NotificationModel not null: found on DB!");
                // notify the user about the alert!
                NotificationUtil.sendNotification(context, nm);

                if (nm.type() == NotificationModel.Type.Time) {
                    Log.d(TAG, "Notification type: TIME");
                    int h = intent.getIntExtra(NotificationUtil.EXTRA_NOTIFICATION_HOUR, 0);
                    int m = intent.getIntExtra(NotificationUtil.EXTRA_NOTIFICATION_MIN, 0);
                    // schedule the notification for the next day
                    NotificationUtil.scheduleNotification(context, h, m, notification_id);
                }
            }
        }
    }
}
