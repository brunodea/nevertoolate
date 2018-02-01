package br.brunodea.nevertoolate.act;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.ImageView;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDB;
import br.brunodea.nevertoolate.frag.list.SubmissionCardListener;
import br.brunodea.nevertoolate.model.SubmissionParcelable;

public class DefaultSubmissionCardListener implements SubmissionCardListener {

    private Activity mActivity;
    private ViewGroup mMainLayout;

    DefaultSubmissionCardListener(Activity activity, ViewGroup main_layout) {
        mActivity = activity;
        mMainLayout = main_layout;
    }

    @Override
    public void onActionFavorite(SubmissionParcelable submission, UpdateFavoriteImageListener imageListener) {
        if (NeverTooLateDB.isFavorite(mActivity, submission)) {
            new AlertDialog.Builder(mActivity)
                    .setMessage(R.string.ask_remove_from_favorites)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        NeverTooLateDB.deleteSubmission(mActivity, submission, false);
                        imageListener.update(false);
                    })
                    .setNegativeButton(R.string.no, (dialog, which) -> {/*do nothing*/})
                    .show();
        } else if (NeverTooLateDB.insertSubmission(mActivity, submission, false) > 0) {
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
