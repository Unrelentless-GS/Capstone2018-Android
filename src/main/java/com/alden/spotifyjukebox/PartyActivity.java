package com.alden.spotifyjukebox;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.DataSetObserver;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.support.design.widget.NavigationView;

import com.alden.spotifyjukebox.component.Song;
import com.alden.spotifyjukebox.component.SongItem;
import com.alden.spotifyjukebox.net.TogglePlayRequest;
import com.alden.spotifyjukebox.net.ChooseDevicRequest;
import com.alden.spotifyjukebox.net.UpdateRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PartyActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String partyName = null;
    private String userHash = null;

    private Button btnAddSongs = null;
    private Button btnTogglePlay = null;
    private ListView lsParty = null;

    private boolean isHost = false;

    //if (isHost == false)
    //{
    //    MenuItem chooseDevice = (MenuItem) findViewById(R.id.choose_device);
    //    MenuItem disbandParty = (MenuItem) findViewById(R.id.disband_party);
    //    MenuItem leaveParty = (MenuItem) findViewById(R.id.leave_party);

    //    chooseDevice.setVisible(false);
    //    disbandParty.setVisible(false);
    //    leaveParty.setVisible(true);
    //}

    private ArrayList<Song> lastFetchedSongs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_navdrawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        btnAddSongs = (Button)findViewById(R.id.btnAddSongs);
        btnTogglePlay = (Button)findViewById(R.id.btnTogglePlay);
        lsParty = (ListView)findViewById(R.id.lstParty);

        if(savedInstanceState == null) {
            // Setup.
            Intent intent = getIntent();
            userHash = intent.getStringExtra("UserHash");

            if(intent.hasExtra("PartyName"))
                partyName = intent.getStringExtra("PartyName");
        }else{
            onRestoreInstanceState(savedInstanceState);
        }

        if(partyName != null)
            setTitle(FORMAT_NAME(partyName));

        final Context ctx = this;
        btnAddSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, SearchActivity.class);
                intent.putExtra("UserHash", userHash);
                intent.putParcelableArrayListExtra("LastFetchedSongs", lastFetchedSongs);

                startActivity(intent);
            }
        });

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
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public boolean onNavigationItemSelected(MenuItem item) {
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
                            //TODO:Redirect to MainScreen
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
                            //TODO:Redirect to MainScreen
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

            ChooseDevicesPopup(devices, cause).show();
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
        super.onResume();
    }

    @Override
    public  void onPause() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("UserHash", userHash);
        savedInstanceState.putParcelableArrayList("LastFetchedSongs", (lastFetchedSongs == null) ? new ArrayList<Song>() : lastFetchedSongs);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        userHash = savedInstanceState.getString("UserHash");
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