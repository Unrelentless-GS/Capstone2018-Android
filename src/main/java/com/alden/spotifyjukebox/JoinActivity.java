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
    private TextView lblPartyName;
    private TextView lblLoading;
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
        lblLoading = findViewById(R.id.lblLoading);
        lblPartyName = findViewById(R.id.lblPartyName);
        txtNickname = findViewById(R.id.txtNickname);
        btnJoin = findViewById(R.id.btnJoin);

        SetLoadingVisible(true);
        SetupTextListener(txtPartyCode);

        final Activity _act = this;
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Nickname validation.
                JoinRequest join = new JoinRequest(_act, txtNickname.getText().toString(), frmParty.getTag().toString(), false);
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

            // Open PartyActivity, same as Host.
            Intent intent = new Intent(this, PartyActivity.class);
            intent.putExtra("UserHash", userHash);
            intent.putExtra("PartyName", partyName);

            startActivity(intent);
        }catch(JSONException je) {
            je.printStackTrace();
        }
    }

    private void SetupTextListener(final EditText txtPartyCode) {
        final Activity _ctx = this;
        txtPartyCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                new CountDownTimer(1100, 1000) {
                    @Override
                    public void onTick(long millsUntilFinished) {
                        // Unused.
                    }
                    @Override
                    public void onFinish() {
                        _ctx.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lblLoading.setText("Searching for party ...");
                                SetLoadingVisible(true);

                                CheckForParty(txtPartyCode.getText().toString());
                            }
                        });
                    }
                }.start();
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void CheckForParty(String code) {
        final String _code = code;
        final Activity _ctx = this;

        JoinRequest join = new JoinRequest(this, "", code, true);
        join.Perform(
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            final JSONObject juke_msg = object.getJSONObject("JUKE_MSG");

                            if(juke_msg.has("JukeboxFault") && juke_msg.getString("JukeboxFault").equals("NoSuchParty")) {
                                _ctx.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        lblLoading.setText("Party not found!");
                                    }
                                });
                            }else if(juke_msg.has("Status") && juke_msg.getString("Status").equals("Success")) {
                                final String hostName = juke_msg.getString("HostName");

                                _ctx.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        frmParty.setTag(_code);
                                        lblPartyName.setText(PartyActivity.FORMAT_NAME(hostName));
                                        SetLoadingVisible(false);
                                    }
                                });
                            }
                        }catch(JSONException je) {
                            je.printStackTrace();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("TEST_REQUEST", error.getMessage().toString());
                    }
                });
    }

    private void SetLoadingVisible(boolean visible) {
        if(visible) {
            frmLoading.setVisibility(View.VISIBLE);
            frmParty.setVisibility(View.GONE);
        }else{
            frmLoading.setVisibility(View.GONE);
            frmParty.setVisibility(View.VISIBLE);
        }
    }
}
