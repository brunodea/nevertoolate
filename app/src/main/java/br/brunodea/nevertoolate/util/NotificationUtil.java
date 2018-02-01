package br.brunodea.nevertoolate.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.act.FullscreenImageActivity;
import br.brunodea.nevertoolate.act.MainActivity;
import br.brunodea.nevertoolate.model.NotificationModel;
import br.brunodea.nevertoolate.model.SubmissionParcelable;

public class NotificationUtil {
    private static final String TAG = "NotificationUtil";
    private static final String CHANNEL_NAME = "NeverTooLate";
    private static final String CHANNEL_ID = "4242";

    public static void notifyAboutRedditSubmission(Context context, NotificationModel notificationModel) {
        SubmissionParcelable submissionParcelable = notificationModel.submission();
        if (submissionParcelable != null) {
            Intent fullscreen_intent = new Intent(context, FullscreenImageActivity.class);
            fullscreen_intent.putExtra(FullscreenImageActivity.ARG_SUBMISSION, notificationModel.submission());

            Intent mainactivity_intent = new Intent(context, MainActivity.class);
            mainactivity_intent.putExtra(MainActivity.ARG_CURR_SCREEN, MainActivity.Screen.NOTIFICATIONS.ordinal());

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(mainactivity_intent);
            stackBuilder.addNextIntent(fullscreen_intent);

            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_small_notification)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(submissionParcelable.title())
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round))
                    .setContentIntent(pendingIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);

                NotificationManager nm = (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (nm != null) {
                    nm.createNotificationChannel(notificationChannel);
                    nm.notify((int) notificationModel.id(), builder.build());
                }
            } else {
                NotificationManagerCompat mgr = NotificationManagerCompat.from(context);
                mgr.notify((int) notificationModel.id(), builder.build());
            }
        } else {
            Log.e(TAG, "Tried to send a notification with no submission associated! " +
                    "Notification id:" + notificationModel.id());
        }
    }
}
