package br.brunodea.nevertoolate.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.dean.jraw.models.Submission;

import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.entity.Motivation;
import br.brunodea.nevertoolate.db.entity.Notification;
import br.brunodea.nevertoolate.util.NeverTooLateDBUtil;
import br.brunodea.nevertoolate.util.NotificationUtil;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "ON RECEIVE!");
        if (intent != null && intent.hasExtra(NotificationUtil.EXTRA_NOTIFICATION_ID)) {
            Log.d(TAG, "intent not null and has EXTRA_NOTIFICATION_ID!");
            long notification_id = intent.getLongExtra(NotificationUtil.EXTRA_NOTIFICATION_ID, 0);
            NeverTooLateDatabase db = NeverTooLateDatabase.getInstance(context);
            Notification notification = db.getNotificationDao().findById(notification_id);
            if (notification != null) {
                Log.d(TAG, "NotificationModel not null: found on DB!");
                // notify the user about the alert!
                NotificationUtil.sendNotification(context, notification);
            } else {
                Log.d(TAG, "NotificationModel not found on DB -- ignoring it!");
            }
        }
    }
}
