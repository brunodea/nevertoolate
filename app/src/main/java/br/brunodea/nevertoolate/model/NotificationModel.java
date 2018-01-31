package br.brunodea.nevertoolate.model;

public class NotificationModel {
    public enum Type {
        Time,
        GeoFence
    }

    private String mInfo;
    private Type mType;
    private long mId;
    private long mSubmissionId;

    public NotificationModel(String info, int type) {
        mInfo = info;
        mType = type == 0 ? Type.Time : Type.GeoFence;
        mId = -1;
        mSubmissionId = 0;
    }

    public NotificationModel(String info, int type, long id, long submission_id) {
        mInfo = info;
        mType = type == 0 ? Type.Time : Type.GeoFence;
        mId = id;
        mSubmissionId = submission_id;
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
}
