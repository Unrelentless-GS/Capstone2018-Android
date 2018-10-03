package com.alden.spotifyjukebox.msg;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class NetMessage implements Parcelable {
    public static final Creator<NetMessage> CREATOR = new Creator<NetMessage>() {
        @Override
        public NetMessage createFromParcel(Parcel in) {
            return new NetMessage(in);
        }

        @Override
        public NetMessage[] newArray(int size) {
            return new NetMessage[size];
        }
    };

    private String raw;
    private boolean valid;

    private JSONObject obj;

    public NetMessage(String _raw) {
        this.raw = _raw;

        ProcessRaw(raw);
    }

    protected NetMessage(Parcel in) {
        raw = in.readString();

        ProcessRaw(raw);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(raw);
    }

    protected void ReadObjectInfo() {

    }

    private void ProcessRaw(String raw) {
        try{
            JSONObject juke_msg = new JSONObject(raw).getJSONObject("JUKE_MSG");
            this.obj = juke_msg;
            valid = true;

        }catch(JSONException je) {
            je.printStackTrace();
            valid = false;
        }

        ReadObjectInfo();
    }

    public boolean IsValid() {
        return valid;
    }

    public JSONObject GetObject() {
        return obj;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
