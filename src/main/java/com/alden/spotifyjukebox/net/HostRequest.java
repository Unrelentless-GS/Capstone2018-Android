/*
HostRequest Summary:
A child of the NetRequest class, HostRequest wraps a create party request.
Specifically, once the user has authorised with Spotify, this request is used to inform the server of the intention,
which then performs its own request server-server.

Expect a userhash be returned from the server. This userhash is the key for any further host related interaction.

Written by Alden Viljoen.
*/

package com.alden.spotifyjukebox.net;

import android.content.Context;

public class HostRequest extends NetRequest {
    public HostRequest(Context _ctx, String code, String hostNickname) {
        super(_ctx, GetHost() + "jukebox.php");

        AddParameter("Code", code);
        AddParameter("HostNickname", hostNickname);
    }
}
