package br.brunodea.nevertoolate.model;

public class NotificationModel {
    // XXX: the order in which the types are placed in the Type enum should *not* be changed.
    // their order is used to identify them in the database.
    public enum Type {
        Time, // 0: in the DB
        GeoFence, // 1: in the DB
        Invalid // shouldn't be in the DB
    }

    private String mInfo;
    private Type mType;
    private long mId;
    private long mSubmissionId;
    private SubmissionParcelable mSubmissionParcelable;

    public NotificationModel(String info, int type) {
        mInfo = info;
        mType = type == 0 ? Type.Time : Type.GeoFence;
        mId = -1;
        mSubmissionId = 0;
        mSubmissionParcelable = null;
    }

    public NotificationModel(String info, int type, long id, long submission_id, SubmissionParcelable submissionParcelable) {
        mInfo = info;
        mType = type == 0 ? Type.Time : Type.GeoFence;
        mId = id;
        mSubmissionId = submission_id;
        mSubmissionParcelable = submissionParcelable;
    }

    public void setSubmission(SubmissionParcelable submission) {
        mSubmissionParcelable = submission;
    }
    public void setSubmissionId(long id) {
        mSubmissionId = id;
    }
    public void setType(Type type) {
        mType = type;
    }
    public void setID(long id) {
        mId = id;
    }

    public String info() {
        return mInfo;
    }
    public Type type() {
        return mType;
    }
    public long id() {
        return mId;
    }
    public long submission_id() {
        return mSubmissionId;
    }
    public SubmissionParcelable submission() {
        return mSubmissionParcelable;
    }
}
