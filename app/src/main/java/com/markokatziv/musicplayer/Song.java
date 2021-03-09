package com.markokatziv.musicplayer;

import java.io.Serializable;

public class Song implements Serializable {

    // private Preferences preferences;

    private String artistTitle;
    private String albumTitle;
    //  transient private Bitmap heart;
    private String songTitle;
    private String linkToSong;
    private String ImagePath = "";
    private boolean isFavorite = false;

    public String getAlbumTitle() {
        return albumTitle;
    }

    public void setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
    }

    //   private boolean isExpanded;
//
//    public boolean isExpanded() {
//        return isExpanded;
//    }
//
//    public void setExpanded(boolean expanded) {
//        isExpanded = expanded;
//    }

    //transient private Bitmap songThumbNail;
    // private static int photoIndex = 0;


//    public static void setPhotoIndex(int photoIndex) {
//        Song.photoIndex = photoIndex;
//    }
//
//    public static int getPhotoIndex() {
//        return ++photoIndex;
//    }

//    public Bitmap getSongThumbNail() {
//        return songThumbNail;
//    }

//    public void setSongThumbNail(Bitmap songThumbNail) {
//        this.songThumbNail = songThumbNail;
//    }

    //private Bitmap songThumbNail;


//    public Bitmap getHeart() {
//        return heart;
//    }
//
//    public void setHeart(Bitmap heart) {
//        this.heart = heart;
//    }

    public String getArtistTitle() {
        return artistTitle;
    }

    public void setArtistTitle(String artistTitle) {
        this.artistTitle = artistTitle;
    }

    public Song() {
//        this.isExpanded = false;
//        Resources res = context.getResources();
//        Bitmap defaultBitmap = BitmapFactory.decodeResource(res, R.drawable.default_song_img);
//        Bitmap bitmap = Bitmap.createScaledBitmap(defaultBitmap, 150, 200, true);
//        this.songThumbNail = bitmap;
//          preferences = getPreferences(MODE)

    }

//    public Bitmap getSongThumbNail() {
//        return songThumbNail;
//    }

//    public void setSongThumbNail(Bitmap songThumbNail) {
//        this.songThumbNail = songThumbNail;
//    }

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

//    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
//       // heart.compress(Bitmap.CompressFormat.JPEG, 80, out);
//        songThumbNail.compress(Bitmap.CompressFormat.JPEG, 80, out);
//        out.defaultWriteObject();
//    }
//
//    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
//        heart = BitmapFactory.decodeStream(in);
//        songThumbNail = BitmapFactory.decodeStream(in);
//        in.defaultReadObject();
//    }


}
