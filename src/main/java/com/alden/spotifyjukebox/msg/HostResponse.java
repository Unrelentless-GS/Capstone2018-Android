package com.alden.spotifyjukebox.msg;

import android.os.Parcel;

import org.json.JSONException;

public class HostResponse extends NetMessage {
    public static final Creator<HostResponse> CREATOR = new Creator<HostResponse>() {
        @Override
        public HostResponse createFromParcel(Parcel in) {
            return new HostResponse(in);
        }

        @Override
        public HostResponse[] newArray(int size) {
            return new HostResponse[size];
        }
    };

    private String userHash;
    private String joinCode;

    public HostResponse(String _raw) {
        super(_raw);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userHash);
        parcel.writeString(joinCode);

        super.writeToParcel(parcel, i);
    }

    protected HostResponse(Parcel in) {
        super(in);

        userHash = in.readString();
        joinCode = in.readString();
    }

    @Override
    public void ReadObjectInfo() {
        if(IsValid()) {
            try {
                userHash = GetObject().getString("UserHash");
                joinCode = GetObject().getString("JoinCode");
            }catch (JSONException je) {
                je.printStackTrace();
            }
        }
    }

    public String GetUserHash() {
        return userHash;
    }

    public String GetJoinCode(){
        return joinCode;
    }
}
