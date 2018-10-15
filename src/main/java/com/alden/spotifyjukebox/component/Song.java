package com.alden.spotifyjukebox.component;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Song implements Parcelable {
    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    private boolean isValid = false;

    public int id;
    public String name;
    public String artist;
    public String album;
    public String spotifyID;
    public String imageLink;

    public int voteCount;
    public int yourVote;

    // Received from Spotify. Used in search results.
    public Song(JSONObject object, boolean useThisIfFromSpotify) {
        try {
            JSONObject album = object.getJSONObject("album");
            JSONObject artist = object.getJSONArray("artists").getJSONObject(0);
            JSONObject firstImage = album.getJSONArray("images").getJSONObject(0);

            this.id = -1;
            this.name = object.getString("name");
            this.artist = artist.getString("name");
            this.album = album.getString("name");
            this.spotifyID = object.getString("id");
            this.imageLink = firstImage.getString("url");

            isValid = true;
        }catch(JSONException je) {
            isValid = false;
        }
    }

    // Returned from the Jukebox server, a mirror of the database.
    public Song(JSONObject object) {
        try {
            this.id = object.getInt("SongID");
            this.name = object.getString("SongName");
            this.artist = object.getString("SongArtists");
            this.album = object.getString("SongAlbum");
            this.spotifyID = object.getString("SongSpotifyID");
            this.imageLink = object.getString("SongImageLink");
            this.voteCount = object.getInt("VoteCount");
            this.yourVote = object.getInt("YourVote");

            isValid = true;
        }catch(JSONException je) {
            je.printStackTrace();
        }
    }

    protected Song(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.artist = in.readString();
        this.album = in.readString();
        this.spotifyID = in.readString();
        this.imageLink = in.readString();
        this.voteCount = in.readInt();
        this.yourVote = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeString(spotifyID);
        dest.writeString(imageLink);
        dest.writeInt(voteCount);
        dest.writeInt(yourVote);
    }

    public boolean IsValid() {
        return this.isValid;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
