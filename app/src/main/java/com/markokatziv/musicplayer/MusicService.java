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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created By marko
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

    public static boolean isServiceRunning = false;

    /* LiveData */
    private MutableLiveData<Boolean> isMusicPlayingMLD;
    private MutableLiveData<Integer> songIndexMLD;
    private MutableLiveData<Boolean> isSongReadyMLD;
    private MutableLiveData<Integer> songDurationMLD;

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
        initLiveData(); //CHANGES HERE
        songs = SongFileHandler.readSongList(MusicService.this);

        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        initLiveData(); //CHANGES HERE
        songs = SongFileHandler.readSongList(MusicService.this);
    }

    interface MusicServiceListener {
        void onCloseClickFromService(MediaPlayer mediaPlayer);
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

        isServiceRunning = true;

        mediaPlayer = initMediaPlayer();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
        remoteViews = new RemoteViews(getPackageName(), R.layout.music_player_notification);
        buildCommandIntents();
        notification = buildNotification(remoteViews);
        startForeground(NOTIFICATION_IDENTIFIER_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        songs = SongFileHandler.readSongList(MusicService.this);
        int currentPosition = intent.getIntExtra("position", 0);
        String command = intent.getStringExtra("command");


      //  songIndexMLD.setValue(currentPosition); // set song index in LiveData

        progressToSeekTo = intent.getIntExtra("progress_from_user", 0);
        switchReceiveCommand(command, currentPosition);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
        isSongReadyMLD.setValue(true);
        isMusicPlayingMLD.setValue(true);
        songDurationMLD.setValue(mediaPlayer.getDuration());

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playSong(true);
    }

    private MediaPlayer initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.reset();

        return mediaPlayer;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        isMusicPlayingMLD.setValue(false);
        PreferenceHandler.putBoolean(PreferenceHandler.TAG_WAS_PLAYING, false, this);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }
    }

    public int getSongProgress() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }

        return 0;
    }

    private void switchReceiveCommand(String command, int position) {

        switch (command) {
            case COMMAND_NEW_INSTANCE:
                songIndexMLD.setValue(position);
                PreferenceHandler.putInt(PreferenceHandler.TAG_LAST_SONG_INDEX, position, this);
                PreferenceHandler.putBoolean(PreferenceHandler.TAG_WAS_PLAYING, true, this);
                PreferenceHandler.saveState(position,
                        songs.get(position).getSongTitle(),
                        songs.get(position).getArtistTitle(),
                        this);
                currentPlaying = position;
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }

                mediaPlayer.reset();

                // A new song has been selected.
                // The song is not ready yet, and needs to be prepared.
                isSongReadyMLD.setValue(false);

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
                PreferenceHandler.putBoolean(PreferenceHandler.TAG_WAS_PLAYING, false, this);
                // songIndexMLD.setValue(currentPlaying);
                if (mediaPlayer.isPlaying()) {
                    remoteViews.setImageViewResource(R.id.play_pause_btn_notif, R.drawable.ic_outline_play_circle_24);
                    manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);
                    mediaPlayer.pause();

                    isMusicPlayingMLD.setValue(false);
                }
                else { // mediaPlayer is not playing
                    PreferenceHandler.putBoolean(PreferenceHandler.TAG_WAS_PLAYING, true, this);
                    PreferenceHandler.putInt(PreferenceHandler.TAG_LAST_SONG_INDEX, position, this);
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
                isMusicPlayingMLD.setValue(false);
                PreferenceHandler.putBoolean(PreferenceHandler.TAG_WAS_PLAYING, false, this);
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

        isSongReadyMLD.setValue(false);

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

        /* Set new position (Integer) */
        songIndexMLD.setValue(currentPlaying);
        PreferenceHandler.putInt(PreferenceHandler.TAG_LAST_SONG_INDEX, currentPlaying, this);
        PreferenceHandler.saveState(currentPlaying,
                songs.get(currentPlaying).getSongTitle(),
                songs.get(currentPlaying).getArtistTitle(),
                this);

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

    private void buildCommandIntents() {
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

    public LiveData<Boolean> getIsMusicPlaying() {
        return isMusicPlayingMLD;
    }

    public LiveData<Integer> getSongIndex() {
        return songIndexMLD;
    }

    public LiveData<Boolean> getIsSongReady() {
        return isSongReadyMLD;
    }

    public LiveData<Integer> getSongDuration() {
        return songDurationMLD;
    }

    private void initLiveData() {
        this.isMusicPlayingMLD = new MutableLiveData<>(); //CHANGES HERE
        this.songIndexMLD = new MutableLiveData<>();
        this.isSongReadyMLD = new MutableLiveData<>();
        this.songDurationMLD = new MutableLiveData<>();
    }
}