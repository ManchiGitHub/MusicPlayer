package com.markokatziv.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created By marko katziv
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private final int PLAY_PAUSE_REQUEST_CODE = 0;
    private final int NEXT_REQUEST_CODE = 1;
    private final int PREV_PAUSE_REQUEST_CODE = 2;
    private final int CLOSE_REQUEST_CODE = 3;
    private final int NOTIFICATION_IDENTIFIER_ID = 1;

    MediaPlayer mediaPlayer;
    NotificationManager manager;
    RemoteViews remoteViews;
    NotificationCompat.Builder builder;
    Notification notification;
    ArrayList<Song> songs;
    int currentPlaying = -1;
    boolean isDataSourceSet = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.reset();

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelID = "channel_id";
        String channelName = "music";

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        builder = new NotificationCompat.Builder(this, channelID);
        remoteViews = new RemoteViews(getPackageName(), R.layout.music_player_notification);

        //TODO: requestcode should be const

        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.putExtra("command", "play_pause");
        PendingIntent playPendingIntent = PendingIntent.getService(this, PLAY_PAUSE_REQUEST_CODE, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.play_pause_btn_notif, playPendingIntent);

        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.putExtra("command", "next");
        PendingIntent nextPendingIntent = PendingIntent.getService(this, NEXT_REQUEST_CODE, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.next_btn_notif, nextPendingIntent);

        Intent prevIntent = new Intent(this, MusicService.class);
        prevIntent.putExtra("command", "prev");
        PendingIntent prevPendingIntent = PendingIntent.getService(this, PREV_PAUSE_REQUEST_CODE, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.previous_btn_notif, prevPendingIntent);

        Intent closeIntent = new Intent(this, MusicService.class);
        closeIntent.putExtra("command", "close");
        PendingIntent closePendingIntent = PendingIntent.getService(this, CLOSE_REQUEST_CODE, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.close_notification_btn_notif, closePendingIntent);

        builder.setCustomBigContentView(remoteViews);
        builder.setSmallIcon(R.drawable.ic_baseline_music_note_24);

        //TODO: notification id should be const
        notification = builder.build();
        startForeground(NOTIFICATION_IDENTIFIER_ID, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String command = intent.getStringExtra("command");
        System.out.println(command + " COMMAND STRING");
        int position = intent.getIntExtra("position", 0);
        songs = SongFileHandler.readSongList(MusicService.this);

        switch (command) {
            case "new_instance":
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                currentPlaying = position;
                mediaPlayer.reset();
                try {
                    remoteViews.setImageViewResource(R.id.play_pause_btn_notif, R.drawable.ic_outline_pause_circle_24);
                    manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);
                    mediaPlayer.setDataSource(songs.get(currentPlaying).getLinkToSong());
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "play_pause":
                if (mediaPlayer.isPlaying()) {
                    remoteViews.setImageViewResource(R.id.play_pause_btn_notif, R.drawable.ic_outline_play_circle_24);
                    manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);
                    mediaPlayer.pause();

                    //TODO: this should probably be implemented with broadcasts.
                    PlayerFragment.playPauseBtn.setBackgroundResource(R.drawable.ic_outline_play_circle_24);
                    PlayerFragment.switchNumber = 0;
                }
                else {
                    remoteViews.setImageViewResource(R.id.play_pause_btn_notif, R.drawable.ic_outline_pause_circle_24);
                    manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);
                    mediaPlayer.start();

                    //TODO: this should probably be implemented with broadcasts.
                    PlayerFragment.playPauseBtn.setBackgroundResource(R.drawable.ic_outline_pause_circle_24);
                    PlayerFragment.switchNumber = 1;
                }
                break;
            case "next":
                if (!mediaPlayer.isPlaying()) {
                    remoteViews.setImageViewResource(R.id.play_pause_btn_notif, R.drawable.ic_outline_pause_circle_24);
                    manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);
                    mediaPlayer.stop();
                }
                playSong(true);
                remoteViews.setTextViewText(R.id.song_title_notif, songs.get(currentPlaying).getSongTitle());
                remoteViews.setTextViewText(R.id.artist_title_notif, songs.get(currentPlaying).getArtistTitle());
                manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);

                //TODO: this should probably be implemented with broadcasts.
                PlayerFragment.playPauseBtn.setBackgroundResource(R.drawable.ic_outline_pause_circle_24);
                PlayerFragment.switchNumber = 1;
                break;
            case "prev":
                if (!mediaPlayer.isPlaying()) {
                    remoteViews.setImageViewResource(R.id.play_pause_btn_notif, R.drawable.ic_outline_pause_circle_24);
                    mediaPlayer.stop();
                }
                playSong(false);
                remoteViews.setTextViewText(R.id.song_title_notif, songs.get(currentPlaying).getSongTitle());
                remoteViews.setTextViewText(R.id.artist_title_notif, songs.get(currentPlaying).getArtistTitle());
                manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);

                //TODO: this should probably be implemented with broadcasts.
                PlayerFragment.playPauseBtn.setBackgroundResource(R.drawable.ic_outline_pause_circle_24);
                PlayerFragment.switchNumber = 1;
                break;
            case "close":
                System.out.println("SHOULD CLOSE NOW");
                stopSelf();
                mediaPlayer.reset();
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

            mediaPlayer.release();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playSong(true);
    }

    private void playSong(boolean isNext) {

        if (isNext) {
            currentPlaying++;
            if (currentPlaying == songs.size()) {
                currentPlaying = 0;
            }
        }
        else { // previous
            currentPlaying--;
            if (currentPlaying < 0) {
                currentPlaying = songs.size() - 1;
            }
        }

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(songs.get(currentPlaying).getLinkToSong());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
