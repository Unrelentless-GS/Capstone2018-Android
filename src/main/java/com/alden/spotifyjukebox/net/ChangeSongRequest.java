package com.alden.spotifyjukebox.net;

import android.content.Context;

public class ChangeSongRequest extends NetRequest {
    public ChangeSongRequest(Context _ctx, String userhash, String songid) {
        super(_ctx, GetHost() + "test.php");

        MakeAuthorised(userhash);
        AddParameter("Operation", "PlaySong");
        AddParameter("SpotifySongID", songid);
    }
}
