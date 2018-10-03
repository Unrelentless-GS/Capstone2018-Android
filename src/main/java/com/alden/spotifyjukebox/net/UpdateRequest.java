package com.alden.spotifyjukebox.net;

import android.content.Context;

public class UpdateRequest extends NetRequest {
    public UpdateRequest(Context _ctx, String userHash, String operation) {
        super(_ctx, GetHost() + "update.php");

        MakeAuthorised(userHash);
        AddParameter("Operation", operation);
    }
}
