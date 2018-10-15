package com.alden.spotifyjukebox.component;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.alden.spotifyjukebox.R;
import com.alden.spotifyjukebox.net.VoteRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SongItem extends ArrayAdapter<Song> {
    private String userHash = null;
    private List<Song> songs = null;

    public SongItem(@NonNull Context context , @NonNull List<Song> objects, String _userHash) {
        super(context, R.layout.adapter_song, objects);

        this.userHash = _userHash;
        this.songs = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View result = convertView;
        if(result == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            result = inflater.inflate(R.layout.adapter_song, null);
        }

        final Song song = getItem(position);

        TextView songName = result.findViewById(R.id.lblSongName);
        TextView songArtistAlbum = result.findViewById(R.id.lblArtistAlbum);
        TextView voteCount = result.findViewById(R.id.lblVoteCount);

        TextView tvUpvote = result.findViewById(R.id.lblUpvote);
        TextView tvDownvote = result.findViewById(R.id.lblDownvote);

        songName.setText(song.name);
        songArtistAlbum.setText(song.artist + " • " + song.album);

        tvUpvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Upvote(song);
            }
        });

        tvDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Downvote(song);
            }
        });

        voteCount.setText(String.valueOf(song.voteCount));

        return result;
    }

    public void Upvote(Song s) {
        int v = 1;

        if(s.yourVote == 1)
            v = 0;
        SendVote(s.id, v);
    }

    public void Downvote(Song s) {
        int v = -1;

        if(s.yourVote == -1)
            v = 0;
        SendVote(s.id, -1);
    }

    private void SendVote(int songid, int value) {
        VoteRequest vote = new VoteRequest(getContext(), userHash, songid, value);
        vote.Perform(
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONObject juke_msg = json.getJSONObject("JUKE_MSG");

                            if(juke_msg.getString("Status").equals("Success")) {
                                Log.d("SendVote", "Successfully voted!");
                                notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("SendVote", error.getMessage().toString());
                    }
                }
        );
    }
}
