package br.brunodea.nevertoolate.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDB;
import br.brunodea.nevertoolate.model.NotificationModel;
import br.brunodea.nevertoolate.model.SubmissionParcelable;

public class NotificationUtil {
    private static final String TAG = "NotificationUtil";
    private static final String CHANNEL = "NeverTooLate";

    public static void notifyAboutRedditSubmission(Context context, NotificationModel notificationModel) {
        SubmissionParcelable submissionParcelable = notificationModel.submission();
        if (submissionParcelable != null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL)
                    .setSmallIcon(R.mipmap.ic_small_notification)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(submissionParcelable.title())
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round));

            NotificationManagerCompat mgr = NotificationManagerCompat.from(context);
            mgr.notify((int) notificationModel.id(), builder.build());
        } else {
            Log.e(TAG, "Tried to send a notification with no submission associated! " +
                    "Notification id:" + notificationModel.id());
        }
    }
}
