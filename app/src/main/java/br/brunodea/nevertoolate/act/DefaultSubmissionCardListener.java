package br.brunodea.nevertoolate.act;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcEvent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.ImageView;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.dao.MotivationDao;
import br.brunodea.nevertoolate.db.dao.MotivationRedditImageDao;
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
        Motivation motivation = mDB.getMotivationDao().findByRedditImageId(submission.id());
        if (motivation != null) {
            MotivationRedditImage motivation_reddit_image =
                    mDB.getMotivationRedditImageDao().findById(motivation.motivationId);
            if (motivation.favorite) {
                new AlertDialog.Builder(mActivity)
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
                            imageListener.update(false);
                        })
                        .setNegativeButton(R.string.no, (dialog, which) -> {/*do nothing*/})
                        .show();
            } else {
                motivation.favorite = true;
                new MotivationRedditImageDaoAsyncTask(motivation, motivation_reddit_image,
                        mDB, MotivationRedditImageDaoAsyncTask.Action.UPDATE);
                imageListener.update(true);
            }
        } else {
            Motivation new_favorite = new Motivation(Motivation.MotivationType.REDDIT_IMAGE,
                    0, true);
            MotivationRedditImage new_reddit_image = new MotivationRedditImage(
                    submission.permalink(), submission.url(), submission.id(),
                    submission.title(), 0);
            new MotivationRedditImageDaoAsyncTask(new_favorite, new_reddit_image,
                    mDB, MotivationRedditImageDaoAsyncTask.Action.INSERT);
            imageListener.update(true);
        }
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
}
