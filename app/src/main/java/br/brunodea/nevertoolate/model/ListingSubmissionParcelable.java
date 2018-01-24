package br.brunodea.nevertoolate.model;

import android.os.Parcel;
import android.os.Parcelable;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.List;

public class ListingSubmissionParcelable implements Parcelable {
    public ListingSubmissionParcelable(Listing<Submission> submissions) {
        from(submissions);
    }

    protected ListingSubmissionParcelable(Parcel in) {
        mNextName = in.readString();
        mSubmissions = new ArrayList<>();
        in.readTypedList(mSubmissions, SubmissionParcelable.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mNextName);
        parcel.writeTypedList(mSubmissions);
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
    private List<SubmissionParcelable> mSubmissions;

    public void from(Listing<Submission> submissions) {
        mNextName = submissions.getNextName();
        mSubmissions = new ArrayList<>();
        for (Submission s : submissions) {
            mSubmissions.add(new SubmissionParcelable(s));
        }
    }

    public SubmissionParcelable at(int position) {
        return mSubmissions.get(position);
    }

    public int size() {
        return mSubmissions.size();
    }
}
