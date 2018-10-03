package com.alden.spotifyjukebox.net;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

public class JoinRequest extends NetRequest {
    public JoinRequest(Context _ctx, String nickName, String partyCode, boolean checkOnly) {
        super(_ctx, GetHost() + "join.php");

        if(!checkOnly)
            AddParameter("Nickname", nickName);
        AddParameter("PartyCode", partyCode);
    }
}
