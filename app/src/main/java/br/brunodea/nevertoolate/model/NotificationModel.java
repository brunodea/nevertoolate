package br.brunodea.nevertoolate.model;

public class NotificationModel {
    public enum Type {
        Time,
        GeoFence
    }

    private String mInfo;
    private Type mType;
    private long mId;

    public NotificationModel(String info, int type) {
        mInfo = info;
        mType = type == 0 ? Type.Time : Type.GeoFence;
        mId = -1;
    }

    public NotificationModel(String info, int type, long id) {
        mInfo = info;
        mType = type == 0 ? Type.Time : Type.GeoFence;
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
}
