package br.brunodea.nevertoolate.act;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.dean.jraw.models.Submission;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.dao.MotivationRedditImageDaoAsyncTask;
import br.brunodea.nevertoolate.db.entity.Motivation;
import br.brunodea.nevertoolate.db.entity.MotivationRedditImage;
import br.brunodea.nevertoolate.db.join.MotivationRedditImageJoin;
import br.brunodea.nevertoolate.frag.list.SubmissionCardListener;
import br.brunodea.nevertoolate.util.RedditUtils;

public class DefaultSubmissionCardListener implements SubmissionCardListener {

    private Activity mActivity;
    private ViewGroup mMainLayout;

    private NeverTooLateDatabase mDB;

    DefaultSubmissionCardListener(Activity activity, ViewGroup main_layout) {
        mActivity = activity;
        mMainLayout = main_layout;
        mDB = NeverTooLateDatabase.getInstance(activity);
    }

    @Override
    public void onActionFavorite(Submission submission, UpdateFavoriteImageListener imageListener) {
        Handler deleteHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                MotivationRedditImageJoin motivation = (MotivationRedditImageJoin) inputMessage.obj;
                new AlertDialog.Builder(mActivity)
                        .setMessage(R.string.ask_remove_from_favorites)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            boolean pointed_by_notification =
                                    mDB.getNotificationDao().findByMotivationId(motivation.motivation.motivation_id) != null;
                            if (pointed_by_notification) {
                                motivation.motivation.favorite = false;
                                new MotivationRedditImageDaoAsyncTask(motivation.motivation, motivation.motivation_reddit_image,
                                        mDB, MotivationRedditImageDaoAsyncTask.Action.UPDATE).execute();
                            } else {
                                // only remove the favorite from the database if no notification points
                                // to it.
                                new MotivationRedditImageDaoAsyncTask(motivation.motivation, motivation.motivation_reddit_image,
                                        mDB, MotivationRedditImageDaoAsyncTask.Action.DELETE).execute();
                                // TODO: make sure line below isn't necessary
                                //mDB.getMotivationDao().delete(motivation);
                            }
                            imageListener.update(false);
                        })
                        .setNegativeButton(R.string.no, (dialog, which) -> {/*do nothing*/})
                        .show();
            }
        };
        new DaoActionsAsyncTask(mDB, submission, imageListener, deleteHandler)
                .execute(mActivity);
    }

    @Override
    public void onActionShare(Submission submission, Uri bitmapUri) {
        if (bitmapUri != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
            shareIntent.setType("image/*");
            mActivity.startActivity(Intent.createChooser(shareIntent,
                    mActivity.getString(R.string.share_image_title)));
        } else {
            Snackbar.make(mMainLayout, mActivity.getString(R.string.share_error),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActionReddit(Submission submission) {
        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://reddit.com" + submission.getPermalink())
        );
        mActivity.startActivity(intent);
    }

    @Override
    public void onImageClick(ImageView imageView, Submission submission) {
        Intent intent = new Intent(mActivity, FullscreenImageActivity.class);
        intent.putExtra(FullscreenImageActivity.ARG_SUBMISSION, RedditUtils.toString(submission));
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        mActivity,
                        imageView,
                        mActivity.getString(R.string.fullscreenImageViewTransition)
                );
        mActivity.startActivity(intent, options.toBundle());
    }



    static class DaoActionsAsyncTask extends AsyncTask<Context, Void, Void> {
        Submission mSubmission;
        NeverTooLateDatabase mDB;
        UpdateFavoriteImageListener mFavoriteImageListener;
        Handler mHandler;

        private DaoActionsAsyncTask(NeverTooLateDatabase db, Submission s,
                                    UpdateFavoriteImageListener u, Handler handler) {
            mDB = db;
            mSubmission = s;
            mFavoriteImageListener = u;
            mHandler = handler;
        }

        @Override
        protected Void doInBackground(Context... contexts) {
            Motivation motivation = mDB.getMotivationDao().findByRedditImageId(mSubmission.getId());
            if (motivation != null) {
                MotivationRedditImage motivation_reddit_image =
                        mDB.getMotivationRedditImageDao().findById(motivation.child_motivation_id);
                if (motivation.favorite) {
                    MotivationRedditImageJoin mrij = new MotivationRedditImageJoin();
                    mrij.motivation = motivation;
                    mrij.motivation_reddit_image = motivation_reddit_image;
                    Message msg = mHandler.obtainMessage();
                    msg.obj = mrij;
                    mHandler.sendMessage(msg);
                } else {
                    motivation.favorite = true;
                    new MotivationRedditImageDaoAsyncTask(motivation, motivation_reddit_image,
                            mDB, MotivationRedditImageDaoAsyncTask.Action.UPDATE).execute();
                    mFavoriteImageListener.update(true);
                }
            } else {
                Motivation new_favorite = new Motivation(Motivation.MotivationType.REDDIT_IMAGE,
                        0, true);
                MotivationRedditImage new_reddit_image = new MotivationRedditImage(
                        mSubmission.getPermalink(), RedditUtils.handleRedditURL(mSubmission.getUrl()), mSubmission.getId(),
                        RedditUtils.handleRedditTitle(mSubmission.getTitle()), RedditUtils.toString(mSubmission), 0);
                new MotivationRedditImageDaoAsyncTask(new_favorite, new_reddit_image,
                        mDB, MotivationRedditImageDaoAsyncTask.Action.INSERT).execute();
                mFavoriteImageListener.update(true);
            }
            return null;
        }
    }
}
