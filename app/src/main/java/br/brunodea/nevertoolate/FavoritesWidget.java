package br.brunodea.nevertoolate;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;

import br.brunodea.nevertoolate.act.FullscreenImageActivity;
import br.brunodea.nevertoolate.act.MainActivity;
import br.brunodea.nevertoolate.db.NeverTooLateDB;
import br.brunodea.nevertoolate.frag.FavoritesFragment;
import br.brunodea.nevertoolate.model.SubmissionParcelable;
import br.brunodea.nevertoolate.util.GlideApp;

public class FavoritesWidget extends AppWidgetProvider {
    private static final String WIDGET_PREFS = "widget-prefs";
    private static final String CURR_FAVORITE_PREF = "curr_favorite_pref-";
    private static final String EXTRA_APP_WIDGET_ID = "extra-app-widget-id";
    private static final String LEFT_ARROW_ON_CLICK_TAG = "left-on-click-tag";
    private static final String RIGH_ARROW_ON_CLICK_TAG = "right-on-click-tag";

    static private AppWidgetTarget mAppWidgetTarget;

    private PendingIntent getPendingSelfIntent(Context context, String action, int appWidgetID) {
        Intent intent = new Intent(context, getClass());
        intent.putExtra(EXTRA_APP_WIDGET_ID, appWidgetID);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private ArrayList<SubmissionParcelable> getFavorites(Context context) {
        return NeverTooLateDB.listOfFavorites(context);
    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
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

            SubmissionParcelable submission = favorites.get(position);
            GlideApp.with(context)
                    .asBitmap()
                    .load(submission.url())
                    .centerCrop()
                    .into(mAppWidgetTarget);

            // Make user go to favorites screen in the app by clicking in the favorite image
            Intent fullscreen_intent = new Intent(context, FullscreenImageActivity.class);
            fullscreen_intent.putExtra(FullscreenImageActivity.ARG_SUBMISSION, submission);

            Intent mainactivity_intent = new Intent(context, MainActivity.class);
            mainactivity_intent.putExtra(MainActivity.ARG_CURR_SCREEN, MainActivity.Screen.FAVORITES.ordinal());
            mainactivity_intent.putExtra(FavoritesFragment.EXTRA_FAVORITE_POSITION, position);

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
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.favorites_widget);
        mAppWidgetTarget = new AppWidgetTarget(context, R.id.iv_widget_favorite, remoteViews, appWidgetIds) {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                super.onResourceReady(resource, transition);
            }
        };

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
        /*
        int appWidgetId = intent.getIntExtra(EXTRA_APP_WIDGET_ID, -1);
        if (appWidgetId >= 0) {
            SharedPreferences sp = context.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE);
            ArrayList<SubmissionParcelable> favorites = getFavorites(context);
            int position = sp.getInt(CURR_FAVORITE_PREF + appWidgetId, 0);
            int old_position = position;
            if (LEFT_ARROW_ON_CLICK_TAG.equals(intent.getAction())) {
                if (position > 0) {
                    position -= 1;
                }
            } else if (RIGH_ARROW_ON_CLICK_TAG.equals(intent.getAction())) {
                if (position < favorites.size() - 1) {
                    position += 1;
                }
            }

            if (position != old_position) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt(CURR_FAVORITE_PREF + appWidgetId, position);
                editor.apply();
                onUpdate(context, AppWidgetManager.getInstance(context), new int [] { appWidgetId });
            }
        }*/
        super.onReceive(context, intent);
    }
}

