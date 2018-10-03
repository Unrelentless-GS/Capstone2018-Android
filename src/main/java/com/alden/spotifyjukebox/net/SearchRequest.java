package com.alden.spotifyjukebox.net;

import android.content.Context;

public class SearchRequest extends NetRequest {
    public SearchRequest(Context _ctx, String term, String type, String userHash) {
        super(_ctx, GetHost() + "search.php");

        MakeAuthorised(userHash);
        AddParameter("Term", term);
        if(type != "")
            AddParameter("Type", type);
        AddParameter("Mode", "AuthorisationCode");
    }
}
