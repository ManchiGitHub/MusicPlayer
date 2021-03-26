package com.markokatziv.musicplayer;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created By marko katziv
 */
public class SongFileHandler {


    public static void saveSongList(Context context, List<Song> songs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileOutputStream fos = context.openFileOutput("songs_list", Context.MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(songs);
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static ArrayList<Song> getFavoriteSongs(ArrayList<Song> songs) {
        ArrayList<Song> favoriteSongs = new ArrayList<>();

        for (Song song : songs) {
            if (song.isFavorite()) {
                favoriteSongs.add(song);
            }
        }

        return favoriteSongs;
    }

    public static ArrayList<Song> readSongList(Context context) {

        ArrayList<Song> songs = null;

        try {
            FileInputStream fis = context.openFileInput("songs_list");
            ObjectInputStream ois = new ObjectInputStream(fis);
            songs = (ArrayList<Song>) ois.readObject(); //stupid warning is stupid.
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return songs;
    }
}
