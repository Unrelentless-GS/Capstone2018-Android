package com.alden.spotifyjukebox.component;

import android.content.Context;
import android.graphics.Color;
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
import com.alden.spotifyjukebox.net.VoteRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;

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
        ImageView songImage = result.findViewById(R.id.ivSongImage);

        TextView tvUpvote = result.findViewById(R.id.lblUpvote);
        TextView tvDownvote = result.findViewById(R.id.lblDownvote);

        //For currently Playing
        LinearLayout frmVoting = result.findViewById(R.id.frmVoting);
        SetVotingEnabled(frmVoting, song);
        if(song.isPlaying) {
            result.setBackgroundColor(Color.parseColor("#8a550a"));
        }

        songName.setText(song.name);
        songArtistAlbum.setText(song.artist + " • " + song.album);
        Picasso.get().load(song.imageLink).resize(50, 50).into(songImage);

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
        SendVote(s.id, v);
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

    private void SetVotingEnabled(LinearLayout voting, Song song) {
        if(song.isPlaying) {
            voting.setEnabled(false);
            voting.setVisibility(View.GONE);
        }else{
            voting.setEnabled(true);
            voting.setVisibility(View.VISIBLE);
        }
    }
}
