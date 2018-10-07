package com.alden.spotifyjukebox.net;

import android.content.Context;

public class ChooseDevicRequest extends NetRequest {
    public ChooseDevicRequest(Context _ctx, String _userHash, String _action) {
        super(_ctx, GetHost() + "device.php");

        MakeAuthorised(_userHash);
        AddParameter("Action", _action);
    }
}
