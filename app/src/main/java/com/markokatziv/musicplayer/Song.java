package com.markokatziv.musicplayer;

import java.io.Serializable;

/**
 * Created By marko katziv
 */
public class Song implements Serializable {

    private String artistTitle;
    private String songTitle;
    private String linkToSong;
    private String ImagePath = "";
    private boolean isFavorite = false;

    public String getArtistTitle() {
        return artistTitle;
    }

    public void setArtistTitle(String artistTitle) {
        this.artistTitle = artistTitle;
    }

    public Song() {
    }

    public Song(String title, String linkToSong, String imagePath, boolean isFavorite) {
        this.songTitle = title;
        this.linkToSong = linkToSong;
        ImagePath = imagePath;
        this.isFavorite = isFavorite;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getLinkToSong() {
        return linkToSong;
    }

    public void setLinkToSong(String linkToSong) {
        this.linkToSong = linkToSong;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

}
