package com.alden.spotifyjukebox;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alden.spotifyjukebox.msg.HostResponse;
import com.alden.spotifyjukebox.net.HostRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class HostActivity extends AppCompatActivity {
    private LinearLayout frmLoading;
    private LinearLayout frmParty;

    private TextView lblProgress;
    private EditText txtNickname;
    private Button btnCreate;

    private static final String CLIENT_ID = "19ad9a26512a4c729f357d826130ffad";
    private static final String REDIRECT_URI = "jukebox://";

    private static final int REQUEST_CODE = 1999;

    // Protected data.
    private String authCode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        setTitle("Let's start partying");

        this.frmLoading = (LinearLayout)findViewById(R.id.frmLoading);
        this.frmParty = (LinearLayout)findViewById(R.id.frmPartyInfo);
        this.lblProgress = (TextView)findViewById(R.id.lblLoading);
        this.txtNickname = (EditText)findViewById(R.id.txtNickname);
        this.btnCreate = (Button)findViewById(R.id.btnCreate);

        this.btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateParty();
            }
        });

        if(savedInstanceState == null) {
            this.SetLoadingVisible(true, "Authorising...");
            this.SetFormVisible(false);

            this.AuthoriseUser();
        }else{
            onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onResume() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onResume();
    }

    @Override
    public  void onPause() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        if(authCode != null)
            savedInstanceState.putString("Code", authCode);
        savedInstanceState.putString("txtNickname", txtNickname.getText().toString());

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState.containsKey("txtNickname"))
            this.txtNickname.setText(savedInstanceState.getString("txtNickname"));

        if(savedInstanceState.containsKey("Code")) {
            this.SetFormVisible(true);
            this.SetLoadingVisible(false, "");

            this.authCode = savedInstanceState.getString("Code");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.CODE) {
                String code = response.getCode();

                this.authCode = code;

                this.SetFormVisible(true);
                this.SetLoadingVisible(false, "");
            }
        }
    }

    private void AuthoriseUser() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.CODE, REDIRECT_URI);
        builder.setScopes(getString(R.string.juke_scopes).split(" "));
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    private void CreateParty() {
        if(authCode == null) {
            Log.d("HostActivity", "Couldn't create party; auth code is NULL.");
            return;
        }

        final Activity ctx = this;
        final HostRequest host = new HostRequest(this, authCode, txtNickname.getText().toString());
        host.Perform(
                new Response.Listener<String>() {
                     @Override
                     public void onResponse(String response) {
                         if(response.equals("")) {
                             // TODO: Server returned blank.
                             return;
                         }

                         HostResponse hostResponse = new HostResponse(response);

                        Intent intent = new Intent(ctx, PartyActivity.class);
                        intent.putExtra("UserHash", hostResponse.GetUserHash());
                        intent.putExtra("PartyName", txtNickname.getText().toString());
                        intent.putExtra("JoinCode", hostResponse.GetJoinCode());
                        intent.putExtra("IsHost", true);

                        startActivity(intent);
                         ctx.finish();
                     }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("TEST_REQUEST", error.getMessage().toString());
                    }
                });
    }

    private void SetLoadingVisible(boolean visible, String override) {
        if(visible)
            lblProgress.setText(override);
        frmLoading.setVisibility((visible) ? View.VISIBLE : View.GONE);
    }

    private void SetFormVisible(boolean visible) {
        frmParty.setVisibility((visible) ? View.VISIBLE : View.GONE);
    }
}
