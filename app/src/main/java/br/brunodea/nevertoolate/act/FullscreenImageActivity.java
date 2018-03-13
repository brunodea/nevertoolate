package br.brunodea.nevertoolate.act;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.analytics.FirebaseAnalytics;

import net.dean.jraw.models.Submission;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.entity.Notification;
import br.brunodea.nevertoolate.frag.list.SubmissionActions;
import br.brunodea.nevertoolate.util.GlideApp;
import br.brunodea.nevertoolate.util.NeverTooLateUtil;
import br.brunodea.nevertoolate.util.RedditUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FullscreenImageActivity extends AppCompatActivity {
    public static String ARG_SUBMISSION = "submission";
    public static String ARG_NOTIFICATION_ID = "notification-notification_id";
    /**
     * Some older devices need a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    @BindView(R.id.pb_loading_fullscreen_image) ProgressBar mPBLoadingFullscreenImage;
    @BindView(R.id.tv_error_loading) TextView mTVErrorLoading;
    @BindView(R.id.fullscreen_content) ViewGroup mContentView;
    @BindView(R.id.pv_fullscreen) PhotoView mPVFullscreen;
    @BindView(R.id.fl_actions_container) FrameLayout mFLActionsContainer;

    private FirebaseAnalytics mFirebaseAnalytics;

    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = () -> {
        // Delayed display of UI elements
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    };
    private final Runnable mHideRunnable = this::hide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_fullscreen_image);
        ButterKnife.bind(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(ARG_SUBMISSION)) {
            mPBLoadingFullscreenImage.setVisibility(View.VISIBLE);
            // This submission should never be null.
            // If it is, then it is a programmer error and we want it to crash indeed!
            // TODO: instead of crash, generate some analytics instead and fail gracefully to the user?
            Submission s = RedditUtils.fromString(intent.getStringExtra(ARG_SUBMISSION));
            GlideApp.with(this)
                    .load(RedditUtils.handleRedditURL(s.getUrl()))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            mPBLoadingFullscreenImage.setVisibility(View.GONE);
                            mTVErrorLoading.setVisibility(View.VISIBLE);
                            mFLActionsContainer.findViewById(R.id.iv_post_share).setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            mPBLoadingFullscreenImage.setVisibility(View.GONE);
                            mTVErrorLoading.setVisibility(View.GONE);
                            mFLActionsContainer.findViewById(R.id.iv_post_share).setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(mPVFullscreen);

            DefaultSubmissionCardListener actionsListener =
                    new DefaultSubmissionCardListener(this, mContentView);
            SubmissionActions mSubmissionActions = new SubmissionActions(this, (event_name, params) -> {
                Bundle bundle = new Bundle();
                for (Pair<String, String> p : params) {
                    bundle.putString(p.first, p.second);
                }
                mFirebaseAnalytics.logEvent(event_name, bundle);
            });
            mSubmissionActions.onBind(mContentView, s, actionsListener, mPVFullscreen, false);
            mSubmissionActions.setFullscreenTheme();

            if (intent.hasExtra(ARG_NOTIFICATION_ID)) {
                long notification_id = intent.getLongExtra(ARG_NOTIFICATION_ID, -1);
                new NotificationDeletedAsyncTask(new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        NeverTooLateUtil.displayWarningDialog(FullscreenImageActivity.this, R.string.notification_for_submission_deleted);
                    }
                }).execute(Pair.create(this, notification_id));
            }
        }
        mPVFullscreen.setOnClickListener(v -> supportFinishAfterTransition());

        hide();
    }

    @Override
    protected void onDestroy() {
        supportFinishAfterTransition();
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private static class NotificationDeletedAsyncTask extends AsyncTask<Pair<Context, Long>, Void, Void> {
        Handler mHandler;
        NotificationDeletedAsyncTask(Handler handler) {
            mHandler = handler;
        }

        @Override
        protected Void doInBackground(Pair<Context, Long>... p) {
            Notification notification = NeverTooLateDatabase.getInstance(p[0].first)
                    .getNotificationDao()
                    .findById(p[0].second);
            if (notification == null) {
                mHandler.sendEmptyMessage(0);
            }
            return null;
        }
    }
}
