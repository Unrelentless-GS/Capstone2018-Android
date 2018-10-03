package com.alden.spotifyjukebox.net;

import android.content.Context;

public class VoteRequest extends NetRequest {
    public VoteRequest(Context _ctx, String _userHash, int _songID, int _value) {
        super(_ctx, GetHost() + "vote.php");

        AddParameter("Action", "Voting");
        MakeAuthorised(_userHash);

        AddParameter("SongID", String.valueOf(_songID));
        AddParameter("Value", String.valueOf(_value));
    }
}
