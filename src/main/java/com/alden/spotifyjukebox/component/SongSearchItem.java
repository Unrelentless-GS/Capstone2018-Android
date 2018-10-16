package com.alden.spotifyjukebox.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alden.spotifyjukebox.R;
import com.alden.spotifyjukebox.net.UpdateRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class SongSearchItem extends ArrayAdapter<Song> {
    private String userHash;

    private List<Song> results;
    private List<Song> currentSongs;

    public SongSearchItem(@NonNull Context context, @NonNull List<Song> results, List<Song> current, String hash) {
        super(context, R.layout.adapter_search_song, results);

        this.userHash = hash;
        this.results = results;
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
        TextView tvAdded = result.findViewById(R.id.lblAdded);
        TextView songName = result.findViewById(R.id.lblSongName);
        TextView songArtistAlbum = result.findViewById(R.id.lblArtistAlbum);
        ImageView songImage = result.findViewById(R.id.songImage);

        final Song s = getItem(position);
        songName.setText(s.name);
        songArtistAlbum.setText(s.artist + " • " + s.album);
        //songImage.setImageDrawable(ImageOperations(s.imageLink));

        if(SongAdded(s)) {
            tvAdded.setVisibility(View.VISIBLE);
        }else{
            tvAdded.setVisibility(View.GONE);

            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddSong(s);
                }
            });
        }

        return result;
    }

    private Drawable ImageOperations(String url) {
        try {
            InputStream is = (InputStream) this.fetch(url);
            Drawable d = Drawable.createFromStream(is, "src");
            return d;
        }  catch (IOException e) {
            Log.d("ImageOps", "Error");
            return null;
        }
    }

    public Object fetch(String address) throws IOException {
        URL url = new URL(address);
        return url.getContent();
    }

    private void AddSong(Song s) {
        final Song _s = s;
        final ArrayAdapter<Song> adapter = this;

        UpdateRequest add = new UpdateRequest(getContext(), userHash, "AddSong");
        add.AddParameter("SongSpotifyID", s.spotifyID);

        add.Perform(
                new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
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
                    Log.d("AddSong", error.getMessage().toString());
                }
        });
    }

    private boolean SongAdded(Song s) {
        for(int i = 0; i < currentSongs.size(); i++) {
            if(currentSongs.get(i).spotifyID.equals(s.spotifyID))
                return true;
        }
        return false;
    }
}
