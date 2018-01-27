package br.brunodea.nevertoolate.model;

import android.os.Parcel;
import android.os.Parcelable;

import net.dean.jraw.models.Submission;

public class SubmissionParcelable implements Parcelable {
    public SubmissionParcelable(Submission submission) {
        from(submission);
    }

    protected SubmissionParcelable(Parcel in) {
        mURL = in.readString();
        mTitle = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mURL);
        dest.writeString(mTitle);
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

    public String url() {
        return mURL;
    }
    public String title() {
        return mTitle;
    }
    public String permalink() {
        return mPermalink;
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
        mURL = url;
        mTitle = submission.getTitle();
        mPermalink = submission.getPermalink();
    }
}
