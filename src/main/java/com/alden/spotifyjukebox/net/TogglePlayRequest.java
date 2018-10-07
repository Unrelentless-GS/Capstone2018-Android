package com.alden.spotifyjukebox.net;

import android.content.Context;

public class TogglePlayRequest extends NetRequest {
    public TogglePlayRequest(Context _ctx, String _userHash) {
        super(_ctx, GetHost() + "player.php");

        MakeAuthorised(_userHash);
    }
}
