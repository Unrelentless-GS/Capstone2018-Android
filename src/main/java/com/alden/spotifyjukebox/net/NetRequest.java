/*
NetRequest Summary:
A class dedicated to performing requests and handling the reply.

Written by Alden Viljoen.
*/

package com.alden.spotifyjukebox.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Xml;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class NetRequest {
    // The actual URI of the resource.
    private String address;
    private Context ctx;
    private RequestQueue requests;

    // The parameters of this request.
    private HashMap<String, String> params;

    public static boolean DEBUG = true;

    public NetRequest(Context _ctx, String _addy) {
        this.ctx = _ctx;
        this.requests = Volley.newRequestQueue(_ctx);

        this.address = _addy;
        this.params = new HashMap<>();

        AddParameter("ImMobile", "ImMobile");
    }

    public boolean IsConnected() {
        ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        return info != null && info.isConnectedOrConnecting();
    }

    public void AddParameter(String name, String value) {
        if(!params.containsKey(name)) {
            this.params.put(name, value);
        }else {
            params.remove(name);
            params.put(name, value);
        }
    }

    public void Perform(Response.Listener<String> response, Response.ErrorListener error) {

        StringRequest sr = new StringRequest(Request.Method.POST, address, response, error) {
            @Override
            public Map<String, String> getParams() {
                return params;
            }
        };

        requests.add(sr);
    }

    protected void MakeAuthorised(String userHash) {
        AddParameter("JukeboxCookie", userHash);
    }

    public static String GetHost() {
        if(DEBUG) {
            return "http://192.168.0.11/xampp/SpotifyJukebox/";
        }else{
            return "https://spotify-jukebox.viljoen.industries/";
        }
    }
}
