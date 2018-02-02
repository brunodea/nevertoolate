package br.brunodea.nevertoolate.act;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.github.chrisbanes.photoview.PhotoView;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDB;
import br.brunodea.nevertoolate.frag.list.SubmissionActions;
import br.brunodea.nevertoolate.model.SubmissionParcelable;
import br.brunodea.nevertoolate.util.GlideApp;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FullscreenImageActivity extends AppCompatActivity {
    public static String ARG_SUBMISSION = "submission";
    public static String ARG_NOTIFICATION_ID = "notification-id";
    /**
     * Some older devices need a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    @BindView(R.id.fullscreen_content)
    ViewGroup mContentView;
    @BindView(R.id.pv_fullscreen) PhotoView mPVFullscreen;

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

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(ARG_SUBMISSION)) {
            SubmissionParcelable s = intent.getParcelableExtra(ARG_SUBMISSION);
            GlideApp.with(this)
                    .load(s.url())
                    .into(mPVFullscreen);
            DefaultSubmissionCardListener actionsListener =
                    new DefaultSubmissionCardListener(this, mContentView);
            SubmissionActions mSubmissionActions = new SubmissionActions(this);
            mSubmissionActions.onBind(mContentView, s, actionsListener, mPVFullscreen, false);
            mSubmissionActions.setFullscreenTheme();
            if (intent.hasExtra(ARG_NOTIFICATION_ID)) {
                long notification_id = intent.getLongExtra(ARG_NOTIFICATION_ID, -1);
                if (notification_id < 0 || NeverTooLateDB.findNotificationByID(this, notification_id) == null) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.notification_for_submission_deleted_title)
                            .setMessage(R.string.notification_for_submission_deleted)
                            .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                                // do nothing
                            })
                            .show();
                }
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
}
