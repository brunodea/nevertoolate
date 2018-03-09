package br.brunodea.nevertoolate.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.act.FullscreenImageActivity;
import br.brunodea.nevertoolate.act.MainActivity;
import br.brunodea.nevertoolate.frag.FavoritesFragment;
import br.brunodea.nevertoolate.model.SubmissionParcelable;
import br.brunodea.nevertoolate.util.GlideApp;

public class FavoritesWidget extends AppWidgetProvider {
    private static final String TAG = "FavoritesWidget";

    private static final String WIDGET_PREFS = "widget-prefs";
    private static final String CURR_FAVORITE_PREF = "curr_favorite_pref-";
    private static final String LEFT_ARROW_ON_CLICK_TAG = "left-on-click-tag";
    private static final String RIGH_ARROW_ON_CLICK_TAG = "right-on-click-tag";

    private PendingIntent getPendingSelfIntent(Context context, String action, int appWidgetID) {
        Intent intent = new Intent(context, getClass());
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetID);
        intent.setAction(action);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private ArrayList<SubmissionParcelable> getFavorites(Context context) {
        return NeverTooLateDB.listOfFavorites(context);
    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.d(TAG, "Updating widget id: " + appWidgetId);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.favorites_widget);
        ArrayList<SubmissionParcelable> favorites = getFavorites(context);
        if (favorites.isEmpty()) {
            views.setViewVisibility(R.id.tv_widget_no_favorites, View.VISIBLE);
            views.setViewVisibility(R.id.iv_widget_favorite, View.GONE);
            views.setViewVisibility(R.id.iv_widget_left_arrow, View.GONE);
            views.setViewVisibility(R.id.iv_widget_right_arrow, View.GONE);
        } else {
            views.setViewVisibility(R.id.iv_widget_left_arrow, View.VISIBLE);
            views.setViewVisibility(R.id.iv_widget_right_arrow, View.VISIBLE);

            SharedPreferences sp = context.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE);
            int position = sp.getInt(CURR_FAVORITE_PREF+appWidgetId, 0);
            if (favorites.size() == 1) {
                views.setViewVisibility(R.id.iv_widget_left_arrow, View.GONE);
                views.setViewVisibility(R.id.iv_widget_right_arrow, View.GONE);
            } else if (position == 0) {
                views.setViewVisibility(R.id.iv_widget_left_arrow, View.GONE);
            } else if (position == favorites.size() - 1) {
                views.setViewVisibility(R.id.iv_widget_right_arrow, View.GONE);
            }

            views.setOnClickPendingIntent(R.id.iv_widget_left_arrow,
                    getPendingSelfIntent(context, LEFT_ARROW_ON_CLICK_TAG, appWidgetId));
            views.setOnClickPendingIntent(R.id.iv_widget_right_arrow,
                    getPendingSelfIntent(context, RIGH_ARROW_ON_CLICK_TAG, appWidgetId));

            AppWidgetTarget mAppWidgetTarget = new AppWidgetTarget(context, R.id.iv_widget_favorite, views, appWidgetId) {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                    super.onResourceReady(resource, transition);
                }
            };

            SubmissionParcelable submission = favorites.get(position);
            GlideApp.with(context)
                    .asBitmap()
                    .load(submission.url())
                    .override(600)
                    .into(mAppWidgetTarget);

            // Make user go to favorites screen in the app by clicking in the favorite image
            Intent fullscreen_intent = new Intent(context, FullscreenImageActivity.class);
            fullscreen_intent.putExtra(FullscreenImageActivity.ARG_SUBMISSION, submission);
            fullscreen_intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            fullscreen_intent.setData(Uri.parse(fullscreen_intent.toUri(Intent.URI_INTENT_SCHEME)));

            Intent mainactivity_intent = new Intent(context, MainActivity.class);
            mainactivity_intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            mainactivity_intent.putExtra(MainActivity.ARG_CURR_SCREEN, MainActivity.Screen.FAVORITES.ordinal());
            mainactivity_intent.putExtra(FavoritesFragment.EXTRA_FAVORITE_POSITION, position);
            mainactivity_intent.setData(Uri.parse(mainactivity_intent.toUri(Intent.URI_INTENT_SCHEME)));

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(mainactivity_intent);
            stackBuilder.addNextIntent(fullscreen_intent);

            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.iv_widget_favorite, pendingIntent);
            ///////////////////////////////////////////////////////////////

            // Make user open app by clicking on the widget title
            Intent title_mainactivity_intent = new Intent(context, MainActivity.class);
            mainactivity_intent.putExtra(MainActivity.ARG_CURR_SCREEN, MainActivity.Screen.HOME.ordinal());
            views.setOnClickPendingIntent(R.id.tv_widget_title,
                    PendingIntent.getActivity(context, 0, title_mainactivity_intent, 0));
            ///////////////////////////////////////////////////////////////
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive()");
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        if (appWidgetId >= 0) {
            SharedPreferences sp = context.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE);
            ArrayList<SubmissionParcelable> favorites = getFavorites(context);
            int position = sp.getInt(CURR_FAVORITE_PREF + appWidgetId, 0);
            if (LEFT_ARROW_ON_CLICK_TAG.equals(intent.getAction())) {
                if (position > 0) {
                    position -= 1;
                }
            } else if (RIGH_ARROW_ON_CLICK_TAG.equals(intent.getAction())) {
                if (position < favorites.size() - 1) {
                    position += 1;
                }
            }

            Log.d(TAG, "Action on widget id: " + appWidgetId);

            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(CURR_FAVORITE_PREF + appWidgetId, position);
            editor.apply();
            updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
        }
        super.onReceive(context, intent);
    }
}

