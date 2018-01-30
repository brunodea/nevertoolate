package br.brunodea.nevertoolate.frag.list;

import android.net.Uri;
import android.widget.ImageView;

import br.brunodea.nevertoolate.model.SubmissionParcelable;

/**
 * This interface must be implemented by activities that contain this
 * fragment to allow an interaction in this fragment to be communicated
 * to the activity and potentially other fragments contained in that
 * activity.
 * <p/>
 * See the Android Training lesson <a href=
 * "http://developer.android.com/training/basics/fragments/communicating.html"
 * >Communicating with Other Fragments</a> for more information.
 */
public interface SubmissionCardListener {
    void onActionFavorite(SubmissionParcelable submission, UpdateFavoriteImageListener imageListener);
    void onActionShare(SubmissionParcelable submission, Uri bitmapUri);
    void onActionReddit(SubmissionParcelable submission);
    void onImageClick(ImageView imageView, SubmissionParcelable submission);

    interface UpdateFavoriteImageListener {
        void update(boolean is_favorite);
    }
}
