package com.alden.spotifyjukebox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;

import com.alden.spotifyjukebox.R;
import com.alden.spotifyjukebox.component.Song;
import com.alden.spotifyjukebox.component.SongSearchItem;
import com.alden.spotifyjukebox.net.SearchRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private String userHash;
    private ArrayList<Song> recentSearchResults;
    private ArrayList<Song> currentSongs;

    private ListView lsResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setTitle("Add Songs");

        recentSearchResults = new ArrayList<Song>();

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
        savedInstanceState.putString("UserHash", userHash);
        savedInstanceState.putParcelableArrayList("LastFetchedSongs", currentSongs);
        savedInstanceState.putParcelableArrayList("LastSearchResult", recentSearchResults);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        userHash = savedInstanceState.getString("UserHash");
        currentSongs = savedInstanceState.getParcelableArrayList("LastFetchedSongs");
        recentSearchResults = savedInstanceState.getParcelableArrayList("LastSearchResult");
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
                                PerformSearchQuery(txtSearchTerm.getText().toString());
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
}
