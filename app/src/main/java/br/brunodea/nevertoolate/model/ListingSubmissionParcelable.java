package br.brunodea.nevertoolate.model;

import android.os.Parcel;
import android.os.Parcelable;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.List;

import br.brunodea.nevertoolate.util.RedditUtils;

public class ListingSubmissionParcelable implements Parcelable {
    public ListingSubmissionParcelable(Listing<Submission> submissions) {
        from(submissions);
    }

    public ListingSubmissionParcelable(Parcel in) {
        mNextName = in.readString();
        mSubmissionsJsons = new ArrayList<>();
        in.readStringList(mSubmissionsJsons);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mNextName);
        parcel.writeStringList(mSubmissionsJsons);
    }

    public static final Creator<ListingSubmissionParcelable> CREATOR = new Creator<ListingSubmissionParcelable>() {
        @Override
        public ListingSubmissionParcelable createFromParcel(Parcel in) {
            return new ListingSubmissionParcelable(in);
        }

        @Override
        public ListingSubmissionParcelable[] newArray(int size) {
            return new ListingSubmissionParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    private String mNextName;
    private List<String> mSubmissionsJsons;

    public boolean isEmpty() {
        return mSubmissionsJsons.isEmpty();
    }

    public void from(Listing<Submission> submissions) {
        mNextName = submissions.getNextName();
        mSubmissionsJsons = new ArrayList<>();
        for (Submission s : submissions) {
            mSubmissionsJsons.add(RedditUtils.toString(s));
        }
    }

    public Submission at(int position) {
        return RedditUtils.fromString(mSubmissionsJsons.get(position));
    }

    public int size() {
        return mSubmissionsJsons.size();
    }
}
