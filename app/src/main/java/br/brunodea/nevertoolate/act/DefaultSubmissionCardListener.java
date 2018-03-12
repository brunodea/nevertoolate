package br.brunodea.nevertoolate.act;

import android.app.Activity;
import android.arch.persistence.room.Update;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.ImageView;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.dao.MotivationRedditImageDaoAsyncTask;
import br.brunodea.nevertoolate.db.entity.Motivation;
import br.brunodea.nevertoolate.db.entity.MotivationRedditImage;
import br.brunodea.nevertoolate.frag.list.SubmissionCardListener;
import br.brunodea.nevertoolate.model.SubmissionParcelable;

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
    public void onActionFavorite(SubmissionParcelable submission, UpdateFavoriteImageListener imageListener) {
        new DaoActionsAsyncTask(mDB, submission, imageListener).execute(mActivity);
    }

    @Override
    public void onActionShare(SubmissionParcelable submission, Uri bitmapUri) {
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
    public void onActionReddit(SubmissionParcelable submission) {
        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://reddit.com" + submission.permalink())
        );
        mActivity.startActivity(intent);
    }

    @Override
    public void onImageClick(ImageView imageView, SubmissionParcelable submission) {
        Intent intent = new Intent(mActivity, FullscreenImageActivity.class);
        intent.putExtra(FullscreenImageActivity.ARG_SUBMISSION, submission);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        mActivity,
                        imageView,
                        mActivity.getString(R.string.fullscreenImageViewTransition)
                );
        mActivity.startActivity(intent, options.toBundle());
    }

    static class DaoActionsAsyncTask extends AsyncTask<Context, Void, Void> {
        SubmissionParcelable mSubmission;
        NeverTooLateDatabase mDB;
        UpdateFavoriteImageListener mFavoriteImageListener;

        private DaoActionsAsyncTask(NeverTooLateDatabase db, SubmissionParcelable s,
                                    UpdateFavoriteImageListener u) {
            mDB = db;
            mSubmission = s;
            mFavoriteImageListener = u;
        }

        @Override
        protected Void doInBackground(Context... contexts) {
            Motivation motivation = mDB.getMotivationDao().findByRedditImageId(mSubmission.id());
            if (motivation != null) {
                MotivationRedditImage motivation_reddit_image =
                        mDB.getMotivationRedditImageDao().findById(motivation.motivationId);
                if (motivation.favorite) {
                    new AlertDialog.Builder(contexts[0])
                            .setMessage(R.string.ask_remove_from_favorites)
                            .setPositiveButton(R.string.yes, (dialog, which) -> {
                                boolean pointed_by_notification =
                                        mDB.getNotificationDao().findByMotivationId(motivation.id) != null;
                                if (pointed_by_notification) {
                                    motivation.favorite = false;
                                    new MotivationRedditImageDaoAsyncTask(motivation, motivation_reddit_image,
                                            mDB, MotivationRedditImageDaoAsyncTask.Action.UPDATE);
                                } else {
                                    // only remove the favorite from the database if no notification points
                                    // to it.
                                    new MotivationRedditImageDaoAsyncTask(motivation, motivation_reddit_image,
                                            mDB, MotivationRedditImageDaoAsyncTask.Action.DELETE);
                                    // TODO: make sure line below isn't necessary
                                    //mDB.getMotivationDao().delete(motivation);
                                }
                                mFavoriteImageListener.update(false);
                            })
                            .setNegativeButton(R.string.no, (dialog, which) -> {/*do nothing*/})
                            .show();
                } else {
                    motivation.favorite = true;
                    new MotivationRedditImageDaoAsyncTask(motivation, motivation_reddit_image,
                            mDB, MotivationRedditImageDaoAsyncTask.Action.UPDATE);
                    mFavoriteImageListener.update(true);
                }
            } else {
                Motivation new_favorite = new Motivation(Motivation.MotivationType.REDDIT_IMAGE,
                        0, true);
                MotivationRedditImage new_reddit_image = new MotivationRedditImage(
                        mSubmission.permalink(), mSubmission.url(), mSubmission.id(),
                        mSubmission.title(), 0);
                new MotivationRedditImageDaoAsyncTask(new_favorite, new_reddit_image,
                        mDB, MotivationRedditImageDaoAsyncTask.Action.INSERT);
                mFavoriteImageListener.update(true);
            }
            return null;
        }
    }
}
