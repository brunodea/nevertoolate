package br.brunodea.nevertoolate.model;

import android.os.Parcel;
import android.os.Parcelable;

import net.dean.jraw.models.Submission;

public class SubmissionParcelable implements Parcelable {
    public SubmissionParcelable(Submission submission) {
        from(submission);
    }

    public SubmissionParcelable() {
        // empty submission
    }

    protected SubmissionParcelable(Parcel in) {
        mURL = in.readString();
        mTitle = in.readString();
        mPermalink = in.readString();
        mID = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mURL);
        dest.writeString(mTitle);
        dest.writeString(mPermalink);
        dest.writeString(mID);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SubmissionParcelable> CREATOR = new Creator<SubmissionParcelable>() {
        @Override
        public SubmissionParcelable createFromParcel(Parcel in) {
            return new SubmissionParcelable(in);
        }

        @Override
        public SubmissionParcelable[] newArray(int size) {
            return new SubmissionParcelable[size];
        }
    };

    private String mURL;
    private String mTitle;
    private String mPermalink;
    private String mID;

    public void setURL(String url) {
        mURL = url;
    }
    public String url() {
        return mURL;
    }
    public void setTitle(String title) {
        mTitle = title;
    }
    public String title() {
        return mTitle;
    }
    public void setPermalink(String permalink) {
        mPermalink = permalink;
    }
    public String permalink() {
        return mPermalink;
    }
    public void setID(String id) {
        mID = id;
    }
    public String id() {
        return mID;
    }

    public void from(Submission submission) {
        String url = submission.getUrl();
        if (url.contains("imgur")) {
            // If the link is for imgur, we need to change it to the address of the image location itself.
            // By appending a lowercase L to the imgur's image hash, we get a smaller image
            if (url.contains("/imgur")) {
                url = url.replace("/imgur", "/i.imgur");
                url += "l.jpg";
            } else {
                String ext = url.substring(url.lastIndexOf("."), url.length());
                url = url.replace(ext, "l" + ext);
            }
        }
        // Remove the tag from the title and capitalize its first letter.
        String title = submission.getTitle();
        title = title.replace(
                title.substring(0, title.indexOf("]") + 1),
                ""
        ).trim();
        if (title.length() > 1) {
            title = title.substring(0, 1).toUpperCase() + title.substring(1);
        }
        mURL = url;
        mTitle = title;
        mPermalink = submission.getPermalink();
        mID = submission.getId();
    }
}
