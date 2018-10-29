package com.alden.spotifyjukebox.component;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alden.spotifyjukebox.PartyActivity;
import com.alden.spotifyjukebox.R;
import com.alden.spotifyjukebox.net.UpdateRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

public class SongSearchItem extends ArrayAdapter<Song> {
    private String userHash;
    private PartyActivity partyActivity;

    private List<Song> results;
    private List<Song> currentSongs;

    public SongSearchItem(@NonNull Context context, @NonNull List<Song> results, List<Song> current, String hash) {
        super(context, R.layout.adapter_search_song, results);

        this.userHash = hash;
        this.results = results;
        this.partyActivity = (PartyActivity)getContext();
        this.currentSongs = current;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup group) {
        View result = convertView;
        if(result == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            result = inflater.inflate(R.layout.adapter_search_song, null);
        }

        LinearLayout btnAdd = result.findViewById(R.id.layoutBtnAdd);
        TextView songName = result.findViewById(R.id.lblSongName);
        TextView songArtistAlbum = result.findViewById(R.id.lblArtistAlbum);
        ImageView songImage = result.findViewById(R.id.songImage);

        final Song s = getItem(position);
        songName.setText(s.name);
        songArtistAlbum.setText(s.artist + " • " + s.album);
        Picasso.get().load(s.imageLink).resize(50, 50).into(songImage);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddSong(s);
            }
        });

        return result;
    }

    private void AddSong(Song s) {
        final Song _s = s;
        final ArrayAdapter<Song> adapter = this;

        partyActivity.CloseSearch();

        final UpdateRequest add = new UpdateRequest(getContext(), userHash, "AddSong");
        add.AddParameter("SongSpotifyID", s.spotifyID);

        add.Perform(
                new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    add.cleanUp();
                    try{
                        JSONObject object = new JSONObject(response);
                        if(object.has("JUKE_MSG")) {
                            JSONObject internal = object.getJSONObject("JUKE_MSG");
                            if(internal.getString("Status").equals("Success")) {
                                currentSongs.add(_s);
                                adapter.notifyDataSetChanged();

                                return;
                            }
                        }
                        throw new JSONException("The response was not a valid Jukebox response");
                    }catch(JSONException je) {
                        je.printStackTrace();
                        return;
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    add.cleanUp();
                    Log.d("AddSong", error.getMessage().toString());
                }
        });
    }
}
