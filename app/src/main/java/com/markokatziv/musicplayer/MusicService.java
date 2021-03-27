package com.markokatziv.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;

/**
 * Created By marko katziv
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    final String COMMAND_NEW_INSTANCE = "new_instance";
    final String COMMAND_PLAY_PAUSE = "play_pause";
    final String COMMAND_NEXT = "next";
    final String COMMAND_PREVIOUS = "prev";
    final String COMMAND_CLOSE = "close";
    final String COMMAND_SEEK_TO = "seek_to";
    final String CHANNEL_ID = "channel_id";
    final String CHANNEL_NAME = "music";

    /* LiveData */
    private MutableLiveData<Boolean> isMusicPlayingMLD;
    private MutableLiveData<Integer> songPositionMLD;

    // changes here
    private MutableLiveData<Integer> songCurrentPositionMLD;

    /* Binder given to clients. */
    private final IBinder binder = new LocalBinder();

    /* Class used for the client Binder. */
    public class LocalBinder extends Binder {
        MusicService getService() {
            /* Return this instance of MusicService so clients can call public methods. */
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    interface MusicServiceListener {
        void onCloseClickFromService(MediaPlayer mediaPlayer);

        void onPreparedListener(int duration);

        void onSongReady(boolean isSongReady);
    }

    /* Registered callbacks. */
    private MusicServiceListener listener;

    private final int PLAY_PAUSE_REQUEST_CODE = 0;
    private final int NEXT_REQUEST_CODE = 1;
    private final int PREV_PAUSE_REQUEST_CODE = 2;
    private final int CLOSE_REQUEST_CODE = 3;
    private final int NOTIFICATION_IDENTIFIER_ID = 1000;

    private MediaPlayer mediaPlayer;
    private NotificationManager manager;
    private RemoteViews remoteViews;
    private Notification notification;
    private ArrayList<Song> songs;
    private int currentPlaying = -1;
    private int progressToSeekTo = 0;

    public void setCallbacks(MusicServiceListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.reset();

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }

        remoteViews = new RemoteViews(getPackageName(), R.layout.music_player_notification);
        initializeCommandIntents();
        notification = buildNotification(remoteViews);
        startForeground(NOTIFICATION_IDENTIFIER_ID, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        songs = SongFileHandler.readSongList(MusicService.this);
        int position = intent.getIntExtra("position", 0);
        String command = intent.getStringExtra("command");
        progressToSeekTo = intent.getIntExtra("progress_from_user", 0);
        switchReceiveCommand(command, position);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
        if (listener != null) {
            listener.onPreparedListener(mediaPlayer.getDuration());
        }
    }

    public int getSongProgress() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playSong(true);
        listener.onSongReady(false);
        Log.d("markomarko", "onCompletion: music service");
    }

    private void switchReceiveCommand(String command, int position) {

        switch (command) {
            case COMMAND_NEW_INSTANCE:
                currentPlaying = position;
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }

                mediaPlayer.reset();
                try {
                    remoteViews.setImageViewResource(R.id.play_pause_btn_notif, R.drawable.ic_outline_pause_circle_24);
                    manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);
                    mediaPlayer.setDataSource(songs.get(currentPlaying).getLinkToSong());
                    mediaPlayer.prepareAsync();
                    remoteViews.setTextViewText(R.id.song_title_notif, songs.get(currentPlaying).getSongTitle());
                    remoteViews.setTextViewText(R.id.artist_title_notif, songs.get(currentPlaying).getArtistTitle());
                    manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case COMMAND_PLAY_PAUSE:
                if (mediaPlayer.isPlaying()) {
                    remoteViews.setImageViewResource(R.id.play_pause_btn_notif, R.drawable.ic_outline_play_circle_24);
                    manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);
                    mediaPlayer.pause();

                    isMusicPlayingMLD.setValue(false);
                }
                else { //mediaPlayer is not playing
                    remoteViews.setImageViewResource(R.id.play_pause_btn_notif, R.drawable.ic_outline_pause_circle_24);
                    manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);
                    mediaPlayer.start();

                    isMusicPlayingMLD.setValue(true);
                }

                break;
            case COMMAND_NEXT:
                if (!mediaPlayer.isPlaying()) {
                    remoteViews.setImageViewResource(R.id.play_pause_btn_notif, R.drawable.ic_outline_pause_circle_24);
                    manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);
                    mediaPlayer.stop();
                }

                playSong(true);
                remoteViews.setTextViewText(R.id.song_title_notif, songs.get(currentPlaying).getSongTitle());
                remoteViews.setTextViewText(R.id.artist_title_notif, songs.get(currentPlaying).getArtistTitle());
                manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);
                break;
            case COMMAND_PREVIOUS:
                if (!mediaPlayer.isPlaying()) {
                    remoteViews.setImageViewResource(R.id.play_pause_btn_notif, R.drawable.ic_outline_pause_circle_24);
                    mediaPlayer.stop();
                }

                playSong(false);
                remoteViews.setTextViewText(R.id.song_title_notif, songs.get(currentPlaying).getSongTitle());
                remoteViews.setTextViewText(R.id.artist_title_notif, songs.get(currentPlaying).getArtistTitle());
                manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);
                break;
            case COMMAND_CLOSE:
                if (listener != null) {
                    listener.onCloseClickFromService(mediaPlayer);
                }

                mediaPlayer.release();
                stopSelf();
                break;
            case COMMAND_SEEK_TO:
                mediaPlayer.seekTo(progressToSeekTo);
        }
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

        if (listener != null) {
            listener.onSongReady(false);
        }

        songPositionMLD.setValue(currentPlaying);
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(songs.get(currentPlaying).getLinkToSong());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Notification buildNotification(RemoteViews remoteViews) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setCustomBigContentView(remoteViews);
        builder.setSmallIcon(R.drawable.ic_baseline_music_note_24);

        return builder.build();
    }

    private void initializeCommandIntents() {
        Intent openAppIntent = new Intent(this, MainActivity.class);
        openAppIntent.putExtra("restarted_from_notification", true);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(this, 10, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_container, openAppPendingIntent);

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
    }

    public MutableLiveData<Boolean> getIsMusicPlayingMLD() {
        return isMusicPlayingMLD;
    }

    public void setIsMusicPlayingMLD() {
        isMusicPlayingMLD = new MutableLiveData<>();
    }

    public MutableLiveData<Integer> getSongPositionMLD() {
        return songPositionMLD;
    }

    public void setSongPositionMLD() {
        songPositionMLD = new MutableLiveData<>();
    }
}