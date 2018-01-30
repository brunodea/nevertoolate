package br.brunodea.nevertoolate.frag.list;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDB;
import br.brunodea.nevertoolate.model.SubmissionParcelable;

public class CursorSubmissionRecyclerViewAdapter extends CursorRecyclerViewAdapter<SubmissionCardViewHolder> {
    private SubmissionCardListener mSubmissionCardListener;
    private int mImageFixedSize;

    public CursorSubmissionRecyclerViewAdapter(Context context, Cursor cursor, SubmissionCardListener submissionCardListener) {
        super(context, cursor);
        mSubmissionCardListener = submissionCardListener;
        mImageFixedSize = 0;
    }

    public void setFixedImageSize(int image_size) {
        mImageFixedSize = image_size;
    }

    @Override
    public SubmissionCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.submission_item, parent, false);
        return new SubmissionCardViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(SubmissionCardViewHolder viewHolder, Cursor cursor) {
        SubmissionParcelable submissionParcelable = NeverTooLateDB.fromFavoritesTableCursor(cursor);
        if (submissionParcelable != null) {
            viewHolder.onBind(submissionParcelable, mSubmissionCardListener, mImageFixedSize);
        }
    }
}
