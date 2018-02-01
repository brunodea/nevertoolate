package br.brunodea.nevertoolate.util;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.AlarmManagerCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import net.dean.jraw.models.Submission;

import java.util.Calendar;
import java.util.Random;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.act.FullscreenImageActivity;
import br.brunodea.nevertoolate.act.MainActivity;
import br.brunodea.nevertoolate.db.NeverTooLateDB;
import br.brunodea.nevertoolate.model.NotificationModel;
import br.brunodea.nevertoolate.model.SubmissionParcelable;
import br.brunodea.nevertoolate.receiver.NotificationReceiver;

import static android.content.Context.ALARM_SERVICE;

public class NotificationUtil {
    private static final String TAG = "NotificationUtil";
    private static final String CHANNEL_NAME = "NeverTooLate";
    private static final String CHANNEL_ID = "4242";

    public static final String EXTRA_NOTIFICATION_MODEL_ID = "extra-notification-model";
    public static final String EXTRA_NOTIFICATION_HOUR = "extra-notification-hour";
    public static final String EXTRA_NOTIFICATION_MIN = "extra-notification-min";

    private static void notifyAboutRedditSubmission(Context context, NotificationModel notificationModel) {
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

    public static void sendNotification(Context context, NotificationModel notificationModel) {
        // first, we get a random submission from the top 10 reddit posts from the current day;
        // then, we add the image the submission to the database as "for_notification"
        // then, we update the submission id for the notification model entry in the database;
        // then we actually send the notification
        RedditUtils.queryGetMotivated(submissions -> {
            // since we are going to replace the submission associated with the notification,
            // we should remove the old submission.
            if (notificationModel.submission() != null) {
                NeverTooLateDB.deleteSubmission(context, notificationModel.submission(), true);
            }
            Submission s = submissions.get(new Random().nextInt(submissions.size()));
            notificationModel.setSubmission(new SubmissionParcelable(s));
            long id = NeverTooLateDB.insertSubmission(context, notificationModel.submission(), true);
            notificationModel.setSubmissionId(id);
            NeverTooLateDB.updateNotificationSubmissionId(context, notificationModel);
            NotificationUtil.notifyAboutRedditSubmission(context, notificationModel);
        }, 10);
    }

    public static void scheduleNotification(Context context, int hour, int min,
                                            NotificationModel notificationModel) {
        Log.d(TAG, "Start schedule notification");
        Calendar calendar = Calendar.getInstance();
        Calendar setcalendar = Calendar.getInstance();

        setcalendar.set(Calendar.HOUR_OF_DAY, hour);
        setcalendar.set(Calendar.MINUTE, min);
        setcalendar.set(Calendar.SECOND, 0);

        if(setcalendar.before(calendar))
            setcalendar.add(Calendar.DATE,1);

        // Enable a receiver
        ComponentName receiver = new ComponentName(context, NotificationReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        long req_code = notificationModel.id();
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(EXTRA_NOTIFICATION_MODEL_ID, notificationModel.id());
        intent.putExtra(EXTRA_NOTIFICATION_HOUR, hour);
        intent.putExtra(EXTRA_NOTIFICATION_MIN, min);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                (int) req_code, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        AlarmManagerCompat.setAlarmClock(am, setcalendar.getTimeInMillis(), null, pendingIntent);
        Log.d(TAG, "notification scheduled to: " + hour + ":" + min);
    }
}
