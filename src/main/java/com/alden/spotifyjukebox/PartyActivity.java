package com.alden.spotifyjukebox;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.DataSetObserver;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.alden.spotifyjukebox.component.Song;
import com.alden.spotifyjukebox.component.SongItem;
import com.alden.spotifyjukebox.net.UpdateRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PartyActivity extends AppCompatActivity {
    private String partyName = null;
    private String userHash = null;

    private Button btnAddSongs = null;
    private ListView lsParty = null;

    private ArrayList<Song> lastFetchedSongs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party);

        btnAddSongs = (Button)findViewById(R.id.btnAddSongs);
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