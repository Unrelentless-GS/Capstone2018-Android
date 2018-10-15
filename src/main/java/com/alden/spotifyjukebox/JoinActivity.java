package com.alden.spotifyjukebox;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alden.spotifyjukebox.net.JoinRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public class JoinActivity extends AppCompatActivity {
    private LinearLayout frmParty;
    private LinearLayout frmLoading;

    private EditText txtPartyCode;
    private EditText txtNickname;
    private Button btnJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        setTitle("Join in");

        frmParty = findViewById(R.id.frmParty);
        frmLoading = findViewById(R.id.frmLoading);

        txtPartyCode = findViewById(R.id.txtJoin);
        txtNickname = findViewById(R.id.txtNickname);
        btnJoin = findViewById(R.id.btnJoin);

        final Activity _act = this;
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Nickname validation.
                JoinRequest join = new JoinRequest(_act, txtNickname.getText().toString(), txtPartyCode.getText().toString(), false);
                join.Perform(
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            ProcessJoinResponse(response);
                        }
                    },

                     new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Join", error.getMessage().toString());
                        }
                    });
            }
        });
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

    private void ProcessJoinResponse(String response) {
        try {
            JSONObject object = new JSONObject(response);
            JSONObject juke_msg = object.getJSONObject("JUKE_MSG");

            String userHash = juke_msg.getString("UserHash");
            String partyName = juke_msg.getString("HostName");
            String joinCode = juke_msg.getString("JoinCode");

            // Open PartyActivity, same as Host.
            Intent intent = new Intent(this, PartyActivity.class);
            intent.putExtra("UserHash", userHash);
            intent.putExtra("PartyName", partyName);
            intent.putExtra("JoinCode", joinCode);

            // This isn't a security risk, as the webserver will also have its own authentication
            // ensuring a user isn't attempting disband party. An alternative is, of course, adding IsHost to the reply from the
            // webserver.
            intent.putExtra("IsHost", false);

            startActivity(intent);
            finish();
        }catch(JSONException je) {
            je.printStackTrace();
        }
    }
}
