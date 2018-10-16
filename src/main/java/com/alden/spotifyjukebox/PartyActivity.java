package com.alden.spotifyjukebox;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.support.design.widget.NavigationView;
import android.widget.TextView;

import com.alden.spotifyjukebox.component.Song;
import com.alden.spotifyjukebox.component.SongItem;
import com.alden.spotifyjukebox.component.SongSearchItem;
import com.alden.spotifyjukebox.net.SearchRequest;
import com.alden.spotifyjukebox.net.TogglePlayRequest;
import com.alden.spotifyjukebox.net.ChooseDevicRequest;
import com.alden.spotifyjukebox.net.UpdateRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PartyActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String partyName = null;
    private String userHash = null;
    private String joinCode = null;
    private Boolean isHost = false;

    private ImageButton btnTogglePlay = null;
    private ListView lsParty = null;
    private TextView tvRoomCode = null;
    private TextView tvCurrentSong = null;
    private TextView tvCurrentAlbum = null;


    private Timer updateTimer = null;

    private ArrayList<Song> lastFetchedSongs = null;

    private ArrayList<Song> recentSearchResults;
    private ArrayList<Song> currentSongs;

    private ListView lsResults;

    private Boolean isSearchOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_navdrawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        tvRoomCode = header.findViewById(R.id.tvRoomCode);
        tvCurrentSong = findViewById(R.id.tvCurrentSong);
        tvCurrentAlbum = findViewById(R.id.tvCurrentAlbum);

        btnTogglePlay = (ImageButton) findViewById(R.id.btnTogglePlay);
        lsParty = (ListView)findViewById(R.id.lstParty);

        if(savedInstanceState == null) {
            // Setup.
            Intent intent = getIntent();
            userHash = intent.getStringExtra("UserHash");
            joinCode = intent.getStringExtra("JoinCode");
            isHost = intent.getBooleanExtra("IsHost", false);

            if(intent.hasExtra("PartyName"))
                partyName = intent.getStringExtra("PartyName");
        }else{
            onRestoreInstanceState(savedInstanceState);
        }

        // Implementing host/guest options.
        ConfigureAuthorisation(navigationView.getMenu());

        if(tvRoomCode != null && joinCode != null)
            tvRoomCode.setText("Join: " + joinCode);

        if(partyName != null)
            setTitle(FORMAT_NAME(partyName));

        final Activity _act = this;
        btnTogglePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TogglePlayRequest toggle = new TogglePlayRequest(_act, userHash);
                toggle.Perform(
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                ProcessToggleResponse(response);
                            }
                        },

                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Toggle", error.getMessage().toString());
                            }
                        });
            }
        });

        //Search Code
        recentSearchResults = new ArrayList<Song>();

        View searchLinearLayer = findViewById(R.id.search);
        searchLinearLayer.setEnabled(false);

        EditText txtSearchTerm = findViewById(R.id.txtSearchTerm);
        lsResults = findViewById(R.id.lstResults);

        SetupTextListener(txtSearchTerm);

        if(savedInstanceState == null) {
            Intent intent = getIntent();
            currentSongs = intent.getParcelableArrayListExtra("LastFetchedSongs");
            if(currentSongs == null)
                currentSongs = new ArrayList<>();

            if(intent.hasExtra("UserHash"))
                userHash = intent.getStringExtra("UserHash");
        }else{
            onRestoreInstanceState(savedInstanceState);
        }
    }

    private void ProcessSearchResult(String response) {
        try {
            ArrayList<Song> results = new ArrayList<>();
            JSONObject object = new JSONObject(response);

            if (object.has("tracks")) {
                JSONObject tracks = object.getJSONObject("tracks");
                if (tracks.has("items")) {
                    JSONArray items = tracks.getJSONArray("items");

                    for(int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        Song s = new Song(item, true);

                        if(s.IsValid())
                            results.add(s);
                    }
                }
            }

            recentSearchResults = results;
            SongSearchItem adapter = new SongSearchItem(this, results, currentSongs, userHash);

            lsResults.setAdapter(adapter);
        }catch(JSONException je) {
            je.printStackTrace();
        }
    }

    private void PerformSearchQuery(String term) {
        View searchLinearLayer = findViewById(R.id.search);
        isSearchOpen = true;
        searchLinearLayer.setVisibility(View.VISIBLE);

        SearchRequest search = new SearchRequest(this, term, "track", userHash);
        search.Perform(
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ProcessSearchResult(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("TEST_REQUEST", error.getMessage());
                    }
                });
    }

    private void SetupTextListener(final EditText txtSearchTerm) {
        final Activity _ctx = this;
        txtSearchTerm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!txtSearchTerm.getText().toString().equals(""))
                {
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
                                    if (!txtSearchTerm.getText().toString().equals(""))
                                    {
                                        PerformSearchQuery(txtSearchTerm.getText().toString());
                                    }
                                }
                            });
                        }
                    }.start();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (isSearchOpen) {
            isSearchOpen = false;
            View searchLinearLayer = findViewById(R.id.search);
            searchLinearLayer.setVisibility(View.GONE);

            EditText txtSearchTerm = findViewById(R.id.txtSearchTerm);
            txtSearchTerm.setText("");
            txtSearchTerm.clearFocus();
        }else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public boolean onNavigationItemSelected(MenuItem item) {
        final Activity _act = this;
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.choose_device) {
            ChooseDeviceButtonHandler("");
        }
        else if (id == R.id.disband_party) {
            UpdateRequest updateReq = new UpdateRequest(PartyActivity.this, userHash, "EndParty");
            updateReq.Perform(
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            _act.finish();
                        }
                    },

                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("updateReq", error.getMessage().toString());
                        }
                    });
        }
        else if (id == R.id.leave_party) {
            UpdateRequest updateReq = new UpdateRequest(PartyActivity.this, userHash, "LeaveParty");
            updateReq.Perform(
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            _act.finish();
                        }
                    },

                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("updateReq", error.getMessage().toString());
                        }
                    });
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void ChooseDeviceButtonHandler(final String cause){
        ChooseDevicRequest chooseDev = new ChooseDevicRequest(PartyActivity.this, userHash, "GetDevices", "");
        chooseDev.Perform(
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ProcessChooseDeviceResponse(response, cause);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("chooseDev", error.getMessage().toString());
                    }
                });
    }

    private void ProcessChooseDeviceResponse(String response, String cause) {
        try {
            JSONObject object = new JSONObject(response);
            JSONArray devices = object.getJSONArray("devices");
            Log.d("chooseDev", devices.toString());

            if  (devices.length() < 1)
            {
                String string = "No Spotify Devices Found";
                Snackbar.make(findViewById(R.id.drawer_layout), string, Snackbar.LENGTH_LONG).show();
            }
            else
            {
                ChooseDevicesPopup(devices, cause).show();
            }
        }catch(JSONException je) {
            je.printStackTrace();
        }
    }

    private Dialog ChooseDevicesPopup(final JSONArray devices, final String cause) {

        AlertDialog.Builder builder = new AlertDialog.Builder(PartyActivity.this);

        try {
            String[] devicesNamesArray = new String[devices.length()];

            for (int i=0; i < devices.length(); i++) {
                JSONObject devOjb = devices.getJSONObject(i);
                String devName = devOjb.getString("name");
                if (devOjb.getBoolean("is_active"))
                {
                    devicesNamesArray[i] = devName + " (Current)";
                }
                else
                {
                    devicesNamesArray[i] = devName;
                }
            }

            builder.setTitle("Choose a device")
                    .setItems(devicesNamesArray, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                JSONObject devOjb = devices.getJSONObject(which);
                                String deviceID = devOjb.getString("id");

                                Activity _act = PartyActivity.this;

                                ChooseDevicRequest chooseDev = new ChooseDevicRequest(_act, userHash, "PlayOnDevice", deviceID);
                                chooseDev.Perform(
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                if (cause == "TP")
                                                {
                                                    TogglePlayRequest toggle = new TogglePlayRequest(PartyActivity.this, userHash);
                                                    toggle.Perform(
                                                            new Response.Listener<String>() {
                                                                @Override
                                                                public void onResponse(String response) {

                                                                }
                                                            },

                                                            new Response.ErrorListener() {
                                                                @Override
                                                                public void onErrorResponse(VolleyError error) {
                                                                    Log.d("Toggle", error.getMessage().toString());
                                                                }
                                                            });
                                                }
                                            }
                                        },

                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.d("chooseDev", error.getMessage().toString());
                                            }
                                        });
                            }catch(JSONException je) {
                                je.printStackTrace();
                            }
                        }
                    });

        }catch(JSONException je) {
            je.printStackTrace();
        }

        return builder.create();
    }

    private void ProcessToggleResponse(String response) {
        Log.d("Toggle", response);
        if (response.equals("NoDeviceSelected"))
        {
            //Display Modal With Device Options
            ChooseDeviceButtonHandler("TP");
        }
    }

    @Override
    public void onResume() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        DownloadPlaylist();
        DownloadPlayback();

        CreateTimer();

        super.onResume();
    }

    @Override
    public  void onPause() {
        CloseTimer();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("UserHash", userHash);
        savedInstanceState.putString("JoinCode", joinCode);
        savedInstanceState.putBoolean("IsHost", isHost);
        savedInstanceState.putParcelableArrayList("LastFetchedSongs", currentSongs);
        savedInstanceState.putParcelableArrayList("LastSearchResult", recentSearchResults);

        savedInstanceState.putParcelableArrayList("LastFetchedSongs", (lastFetchedSongs == null) ? new ArrayList<Song>() : lastFetchedSongs);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        userHash = savedInstanceState.getString("UserHash");
        joinCode = savedInstanceState.getString("JoinCode");
        isHost = savedInstanceState.getBoolean("IsHost");
        currentSongs = savedInstanceState.getParcelableArrayList("LastFetchedSongs");
        recentSearchResults = savedInstanceState.getParcelableArrayList("LastSearchResult");

        lastFetchedSongs = savedInstanceState.getParcelableArrayList("LastFetchedSongs");
    }

    private void DownloadPlaylist() {
        final Context ctx = this;

        UpdateRequest update = new UpdateRequest(ctx, userHash, "UpdatePlaylist");
        update.Perform(
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    PopulateListView(response);
                }
            },

            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
    }

    private void DownloadPlayback() {
        final Context ctx = this;

        UpdateRequest update = new UpdateRequest(ctx, userHash, "CurrentlyPlaying");
        update.Perform(
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ProcessPlayback(response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
    }

    private void PopulateListView(String raw) {
        if(raw.equals("")) {
            Log.d("PopulateListView", "Response is nil");
            return;
        }

        ArrayList<Song> songs = new ArrayList<>();

        try {
            JSONArray juke_msg = null;
            JSONObject msg = new JSONObject(raw);

            String nosongs = msg.getString("JUKE_MSG");
            if(nosongs.equals("NoSongsAdded")) {
                // TODO: No songs added.
                return;
            }
            juke_msg = msg.getJSONArray("JUKE_MSG");

            for(int i = 0; i < juke_msg.length(); i++) {
                Song s = null;
                JSONObject song = juke_msg.getJSONObject(i);

                if(song != null && (s = new Song(song)).IsValid()) {
                    songs.add(s);
                }
            }

            lastFetchedSongs = songs;
        }catch(JSONException je) {
            je.printStackTrace();
        }

        SongItem adapter = new SongItem(this, songs, userHash);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                DownloadPlaylist();

                super.onChanged();
            }
        });
        lsParty.setAdapter(adapter);
    }

    private void ProcessPlayback(String response) {
        try {
            JSONObject json = new JSONObject(response);
            JSONObject juke_msg = new JSONObject(json.getString("JUKE_MSG"));

            if(!juke_msg.getBoolean("is_playing")) {
                tvCurrentSong.setText("Nothing playing!");
                tvCurrentAlbum.setText("");
                return;
            }

            JSONObject item = juke_msg.getJSONObject("item");
            Song s = new Song(item, true);

            if(!s.IsValid()) {
                tvCurrentSong.setText("Updating ...");
                tvCurrentAlbum.setText("");
                return;
            }

            tvCurrentSong.setText(s.name);
            tvCurrentAlbum.setText(s.artist + " • " + s.album);
        }catch(JSONException je) {
            je.printStackTrace();
        }
    }

    private void ConfigureAuthorisation(Menu view) {
        MenuItem chooseDevice = (MenuItem) view.findItem(R.id.choose_device);
        MenuItem disbandParty = (MenuItem) view.findItem(R.id.disband_party);
        MenuItem leaveParty = (MenuItem) view.findItem(R.id.leave_party);

        if(isHost) {
            ActivateMenuItem(chooseDevice, true);
            ActivateMenuItem(disbandParty, true);
            ActivateMenuItem(leaveParty, false);

            btnTogglePlay.setVisibility(View.VISIBLE);
            btnTogglePlay.setEnabled(true);
        }else{
            ActivateMenuItem(chooseDevice, false);
            ActivateMenuItem(disbandParty, false);
            ActivateMenuItem(leaveParty, true);

            btnTogglePlay.setVisibility(View.GONE);
            btnTogglePlay.setEnabled(false);
        }
    }

    private void ActivateMenuItem(MenuItem v, boolean act) {
        v.setVisible(act);
        v.setEnabled(act);
    }

    private void CreateTimer() {
        CloseTimer();

        updateTimer = new Timer("UpdateTimer");
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                DownloadPlaylist();
                DownloadPlayback();
            }
        }, 1100);
    }

    private void CloseTimer() {
        if(updateTimer != null)
            updateTimer.cancel();
    }

    public static String FORMAT_NAME(String name) {
        String result = name;
        char p = 'P';

        // Both are upper case.
        if(name.toUpperCase().charAt(0) == name.charAt(0))
            p = 'P';
        else
            p = 'p';


        if(name.endsWith("s"))
            result += "' " + p + "arty";
        else
            result += "'s " + p + "arty";
        return result;
    }
}