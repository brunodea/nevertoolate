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

    public String url() {
        return mURL;
    }
    public String title() {
        return mTitle;
    }

    public void from(Submission submission) {
        mURL = submission.getUrl();
        mTitle = submission.getTitle();
    }
}
