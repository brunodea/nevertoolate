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
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.util.Pair;
import android.util.Log;

import net.dean.jraw.models.Submission;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.act.FullscreenImageActivity;
import br.brunodea.nevertoolate.act.MainActivity;
import br.brunodea.nevertoolate.db.DBExec;
import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.dao.MotivationRedditImageDaoAsyncTask;
import br.brunodea.nevertoolate.db.entity.Motivation;
import br.brunodea.nevertoolate.db.entity.MotivationRedditImage;
import br.brunodea.nevertoolate.db.entity.Notification;
import br.brunodea.nevertoolate.receiver.NotificationReceiver;

import static android.content.Context.ALARM_SERVICE;

public class NotificationUtil {
    private static final String TAG = "NotificationUtil";
    private static final String CHANNEL_NAME = "NeverTooLate";
    private static final String CHANNEL_ID = "4242";

    public static final String EXTRA_NOTIFICATION_ID = "extra-notification";

    @Inject
    static DBExec sDBExec;

    private static void notifyAboutRedditSubmission(Context context, Notification notification) {
        MotivationRedditImage mri = NeverTooLateDatabase.getInstance(context)
                .getMotivationRedditImageDao().findByMotivationId(notification.base_motivation_id);
        if (mri != null) {
            Submission submission = RedditUtils.fromString(mri.submission_json);
            Intent fullscreen_intent = new Intent(context, FullscreenImageActivity.class);
            fullscreen_intent.putExtra(FullscreenImageActivity.ARG_SUBMISSION, RedditUtils.toString(submission));
            fullscreen_intent.putExtra(FullscreenImageActivity.ARG_NOTIFICATION_ID, notification.notification_id);

            Intent mainactivity_intent = new Intent(context, MainActivity.class);
            mainactivity_intent.putExtra(MainActivity.ARG_CURR_SCREEN, MainActivity.Screen.NOTIFICATIONS.ordinal());

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(mainactivity_intent);
            stackBuilder.addNextIntent(fullscreen_intent);

            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_small_notification)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(RedditUtils.handleRedditTitle(submission.getTitle()))
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
                    nm.notify((int) notification.notification_id, builder.build());
                }
            } else {
                NotificationManagerCompat mgr = NotificationManagerCompat.from(context);
                mgr.notify((int) notification.notification_id, builder.build());
            }
        } else {
            Log.e(TAG, "Tried to send a notification with no submission associated! " +
                    "Notification notification_id:" + notification.notification_id);
        }
    }

    // XXX: notification *has* to already exist in the database!!!
    public static void sendNotification(Context context, final Notification notification) {
        // first, we get a random submission from the top 10 reddit posts from the current day;
        // then, we add the image the submission to the database as "for_notification"
        // then, we update the submission notification_id for the notification model entry in the database;
        // then we actually send the notification
        RedditUtils.queryGetMotivated(submissions -> {
            new GetMotivatedResultAsyncTask(notification)
                    .execute(Pair.create(context, submissions));
        }, 10);
    }

    public static PendingIntent pendingIntentForNotification(Context context, long notification_id) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notification_id);
        return PendingIntent.getBroadcast(context, (int) notification_id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void scheduleNotification(Context context, int hour, int min,
                                            long notification_id) {
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

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC, setcalendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntentForNotification(context, notification_id));
        Log.d(TAG, "notification scheduled to: " + hour + ":" + min);
    }

    public static void cancelNotificationSchedule(Context context, long notification_id) {
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.cancel(pendingIntentForNotification(context, notification_id));
        Log.d(TAG, "canceled notification scheduled: " + notification_id);
    }

    private static class GetMotivatedResultAsyncTask extends AsyncTask<Pair<Context, List<Submission>>, Void, Void> {
        private Notification mNotification;
        GetMotivatedResultAsyncTask(Notification notification) {
            mNotification = notification;
        }

        @Override
        protected Void doInBackground(Pair<Context, List<Submission>>[] pairs) {
            Context context = pairs[0].first;
            List<Submission> submissions = pairs[0].second;
            NeverTooLateDatabase db = NeverTooLateDatabase.getInstance(context);
            Submission chosen_submission = submissions.get(new Random().nextInt(submissions.size()));
            MotivationRedditImage mri = db.getMotivationRedditImageDao().findByRedditId(chosen_submission.getId());
            {
                // since we are going to replace the submission associated with the notification,
                // we should remove the old submission.
                Motivation motivation = db.getMotivationDao().findbyId(mNotification.base_motivation_id);
                if (motivation != null && !motivation.favorite) {
                    MotivationRedditImage old_mri = db.getMotivationRedditImageDao().findById(motivation.child_motivation_id);
                    if (mri.motivation_reddit_image_id != old_mri.motivation_reddit_image_id) {
                        new MotivationRedditImageDaoAsyncTask(motivation, mri, db, MotivationRedditImageDaoAsyncTask.Action.DELETE)
                                .execute();
                    }
                }
            }
            if (mri == null) {
                // if the submission doesn't exist in the DB, we insert it.
                mri = new MotivationRedditImage(chosen_submission.getPermalink(), RedditUtils.handleRedditURL(chosen_submission.getUrl()),
                        chosen_submission.getId(), RedditUtils.handleRedditTitle(chosen_submission.getTitle()), RedditUtils.toString(chosen_submission),
                        0);
                Motivation motivation = new Motivation(Motivation.MotivationType.REDDIT_IMAGE, 0, false);
                sDBExec.insertMotivationRedditImage(motivation, mri, result -> {
                    mNotification.base_motivation_id = result.first.motivation_id;
                    db.runInTransaction(() -> {
                        db.getNotificationDao().update(mNotification);
                        NotificationUtil.notifyAboutRedditSubmission(context, mNotification);
                    });
                });
            } else {
                mNotification.base_motivation_id = mri.parent_motivation_id;
                db.getNotificationDao().update(mNotification);
                NotificationUtil.notifyAboutRedditSubmission(context, mNotification);
            }
            return null;
        }
    }
}
