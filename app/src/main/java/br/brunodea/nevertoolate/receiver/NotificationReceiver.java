package br.brunodea.nevertoolate.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.entity.Motivation;
import br.brunodea.nevertoolate.db.entity.Notification;
import br.brunodea.nevertoolate.model.NotificationModel;
import br.brunodea.nevertoolate.model.SubmissionParcelable;
import br.brunodea.nevertoolate.util.NeverTooLateDBUtil;
import br.brunodea.nevertoolate.util.NotificationUtil;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "ON RECEIVE!");
        if (intent != null && intent.hasExtra(NotificationUtil.EXTRA_NOTIFICATION_MODEL_ID)) {
            Log.d(TAG, "intent not null and has EXTRA_NOTIFICATION_MODEL_ID!");
            long notification_id = intent.getLongExtra(NotificationUtil.EXTRA_NOTIFICATION_MODEL_ID, 0);
            NeverTooLateDatabase db = NeverTooLateDatabase.getInstance(context);
            Notification notification = db.getNotificationDao().findById(notification_id);
            if (notification != null) {
                Log.d(TAG, "NotificationModel not null: found on DB!");
                int type = -1;
                switch (notification.type) {
                    case GEOFENCE:
                        type = 0;
                        break;
                    case TIME:
                        type = 1;
                        break;
                }
                Motivation m = db.getMotivationDao().findbyId(notification.motivationId);
                SubmissionParcelable submission = null;
                if (m != null) {
                    submission = NeverTooLateDBUtil.from(db, m);
                }
                // notify the user about the alert!
                NotificationUtil.sendNotification(context, new NotificationModel(notification.info,
                        type, notification_id, notification.motivationId, submission));
            }
        }
    }
}
