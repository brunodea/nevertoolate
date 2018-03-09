package br.brunodea.nevertoolate.frag.list;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.ImageViewCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import net.cachapa.expandablelayout.ExpandableLayout;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.model.SubmissionParcelable;
import br.brunodea.nevertoolate.util.NeverTooLateUtil;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SubmissionActions {
    private static final String ANALYTICS_EVENT_ACTION = "submission_action";

    @BindView(R.id.iv_post_favorite) ImageView mIVActionFavorite;
    @BindView(R.id.iv_post_reddit) ImageView mIVActionReddit;
    @BindView(R.id.iv_post_share) ImageView mIVActionShare;
    @BindView(R.id.iv_post_expand) ImageView mIVActionExpand;
    @BindView(R.id.tv_post_description) TextView mTVDescription;
    @BindView(R.id.expandable_layout) ExpandableLayout mExpandableLayout;

    private Context mContext;
    private NeverTooLateUtil.AnalyticsListener mAnalyticsListener;
    public SubmissionActions(Context context, NeverTooLateUtil.AnalyticsListener analyticsListener) {
        mContext = context;
        mAnalyticsListener = analyticsListener;
    }

    public void onBind(View viewtoBind,
                       SubmissionParcelable submission,
                       SubmissionCardListener submissionCardListener,
                       ImageView postImage,
                       boolean expanded_text) {
        ButterKnife.bind(this, viewtoBind);

        mTVDescription.setText(submission.title());
        adjust_favorite_icon(NeverTooLateDB.isFavorite(mContext, submission));
        Pair<String, String> p2 = Pair.create(FirebaseAnalytics.Param.ITEM_ID,
                submission.id());
        Pair<String, String> p3 = Pair.create("permalink", submission.permalink());
        mIVActionFavorite.setOnClickListener(view -> {
            if (submissionCardListener != null) {
                submissionCardListener.onActionFavorite(submission, is_favorite -> {
                    if (mAnalyticsListener != null) {
                        Pair<String, String> p1 = Pair.create(FirebaseAnalytics.Param.ITEM_NAME,
                                is_favorite ? "favorite" : "unfavorite");
                        mAnalyticsListener.onEvent(ANALYTICS_EVENT_ACTION, p1, p2, p3);
                    }
                    adjust_favorite_icon(is_favorite);
                });
            }
        });
        mIVActionReddit.setOnClickListener(view ->{
            if (submissionCardListener != null) {
                if (mAnalyticsListener != null) {
                    Pair<String, String> p1 = Pair.create(FirebaseAnalytics.Param.ITEM_NAME,
                            "goto_reddit");
                    mAnalyticsListener.onEvent(ANALYTICS_EVENT_ACTION, p1, p2, p3);
                }
                submissionCardListener.onActionReddit(submission);
            }
        });
        mIVActionShare.setOnClickListener(view -> {
            if (submissionCardListener != null) {
                if (mAnalyticsListener != null) {
                    Pair<String, String> p1 = Pair.create(FirebaseAnalytics.Param.ITEM_NAME,
                            "share");
                    mAnalyticsListener.onEvent(ANALYTICS_EVENT_ACTION, p1, p2, p3);
                }
                submissionCardListener.onActionShare(
                        submission,
                        NeverTooLateUtil.getLocalBitmapUri(mContext, postImage)
                );
            }
        });
        // Expand/Collaspe the image title based on a tag added to the image view.
        mIVActionExpand.setOnClickListener(view -> {
            String tag_down = mContext.getString(R.string.card_action_expand_tag_down);
            String tag_up = mContext.getString(R.string.card_action_expand_tag_up);
            if (mIVActionExpand.getTag().equals(tag_down)) {
                mIVActionExpand.setImageResource(R.drawable.ic_expand_up_24dp);
                mIVActionExpand.setTag(tag_up);
                mExpandableLayout.expand();
                if (mAnalyticsListener != null) {
                    Pair<String, String> p1 = Pair.create(FirebaseAnalytics.Param.ITEM_NAME,
                            "expand_text");
                    mAnalyticsListener.onEvent(ANALYTICS_EVENT_ACTION, p1, p2, p3);
                }
            } else {
                mIVActionExpand.setImageResource(R.drawable.ic_expand_down_24dp);
                mIVActionExpand.setTag(tag_down);
                mExpandableLayout.collapse();
                if (mAnalyticsListener != null) {
                    Pair<String, String> p1 = Pair.create(FirebaseAnalytics.Param.ITEM_NAME,
                            "collapse_text");
                    mAnalyticsListener.onEvent(ANALYTICS_EVENT_ACTION, p1, p2, p3);
                }
            }
        });

        if (expanded_text) {
            mIVActionExpand.setVisibility(View.GONE);
            mExpandableLayout.expand();
        }
    }

    void disableShare() {
        mIVActionShare.setVisibility(View.GONE);
    }

    // used in the fullscreen activity... should probably find a nicer why to do this.
    public void setFullscreenTheme() {
        changeImageViewTint(mIVActionFavorite, android.R.color.white);
        changeImageViewTint(mIVActionReddit, android.R.color.white);
        changeImageViewTint(mIVActionShare, android.R.color.white);
        changeImageViewTint(mIVActionExpand, android.R.color.white);
        mTVDescription.setTextColor(mContext.getResources().getColor(android.R.color.white));
    }

    private void changeImageViewTint(ImageView iv, int color_id) {
        ImageViewCompat.setImageTintList(iv, ColorStateList.valueOf(ContextCompat.getColor
                (mContext, color_id)));
    }

    private void adjust_favorite_icon(boolean is_favorite) {
        if (is_favorite) {
            mIVActionFavorite.setImageDrawable(mContext.getDrawable(R.drawable.ic_favorite_24dp));
        } else {
            mIVActionFavorite.setImageDrawable(mContext.getDrawable(R.drawable.ic_favorite_outline_24dp));
        }
    }
}
