/*
TestRequest Summary:
A class for testing requests.

Written by Alden Viljoen.
*/

package com.alden.spotifyjukebox.net;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

public class TestRequest extends NetRequest {
    public TestRequest(Context _ctx) {
        super(_ctx, GetHost() + "mobile.php");
    }

    @Override
    public void Perform(Response.Listener<String> response, Response.ErrorListener error) {
        super.Perform(

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("TEST_REQUEST", response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("TEST_REQUEST", "Failure");
                    }
                });
    }
}
