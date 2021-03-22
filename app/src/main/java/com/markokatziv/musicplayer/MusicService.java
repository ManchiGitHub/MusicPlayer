package com.markokatziv.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;


import java.io.IOException;
import java.util.ArrayList;

/**
 * Created By marko katziv
 */
public class MusicService extends LifecycleService implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {


    interface MusicServiceListener {
        void onPlayPauseClickFromService(boolean isPlay);

        void onPrevClickFromService(int position);

        void onNextClickFromService(int position);

        void onCloseClickFromService(MediaPlayer mediaPlayer);
    }


  //  public MutableLiveData<Song> songMutableMLD;
    public MutableLiveData<Boolean> isMusicPlayingMLD;
    public MutableLiveData<Integer> songPositionMLD;

    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    // Registered callbacks
    private MusicServiceListener listener;


    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        MusicService getService() {
            // Return this instance of MyService so clients can call public methods
            return MusicService.this;
        }
    }


    public void setCallbacks(MusicServiceListener callbacks) {
        listener = callbacks;
    }


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
    boolean isMusicPlaying = false;

    /**
     * changed here
     */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return binder;
    }

//    public MutableLiveData<Song> getSongMLD() {
//        return songMutableMLD;
//    }


    public MutableLiveData<Boolean> getIsMusicPlayingMLD() {
        return isMusicPlayingMLD;
    }

    public void setIsMusicPlayingMLD() {
        isMusicPlayingMLD = new MutableLiveData<>();
    }

//    public void setSongMLD() {
//        songMutableMLD = new MutableLiveData<>();
//    }

    public MutableLiveData<Integer> getSongPositionMLD() {
        return songPositionMLD;
    }

    public void setSongPositionMLD() {
        songPositionMLD = new MutableLiveData<>();
    }

    //
//    SharedPreferences sp;

//    public void removeObserver(Observer<Song> observer){
//        songMutableMLD.removeObserver(observer);
//    }

    @Override
    public void onCreate() {
        super.onCreate();


//        sp = getSharedPreferences("continuation", MODE_PRIVATE);

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

        Intent openAppIntent = new Intent(this, MainActivity.class);
        openAppIntent.putExtra("no_splash_screen", true);
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

        builder.setCustomBigContentView(remoteViews);
        builder.setSmallIcon(R.drawable.ic_baseline_music_note_24);

        //TODO: notification id should be const
        notification = builder.build();
        startForeground(NOTIFICATION_IDENTIFIER_ID, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        songs = SongFileHandler.readSongList(MusicService.this);

        String command = intent.getStringExtra("command");
        int position = intent.getIntExtra("position", 0);

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
                    remoteViews.setTextViewText(R.id.song_title_notif, songs.get(currentPlaying).getSongTitle());
                    remoteViews.setTextViewText(R.id.artist_title_notif, songs.get(currentPlaying).getArtistTitle());
                    manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);

                } catch (IOException e) {
                    e.printStackTrace();
                }


                break;
            case "play_pause":
                if (mediaPlayer.isPlaying()) {
                    remoteViews.setImageViewResource(R.id.play_pause_btn_notif, R.drawable.ic_outline_play_circle_24);
                    manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);
                    mediaPlayer.pause();

                    /**
                     * changed here
                     */
                    /*
                    if (listener != null) {

                       listener.onPlayPauseClickFromService(false);
                    }
                     */
                    isMusicPlayingMLD.setValue(false);
                }
                else { //mediaPlayer is not playing
                    remoteViews.setImageViewResource(R.id.play_pause_btn_notif, R.drawable.ic_outline_pause_circle_24);
                    manager.notify(NOTIFICATION_IDENTIFIER_ID, notification);
                    mediaPlayer.start();

                    /**
                     * changed here
                     */
                    /*
                    if (listener != null) {

                        listener.onPlayPauseClickFromService(true);
                    }
                     */
                    isMusicPlayingMLD.setValue(true);
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

                /**
                 * changed here
                 */
                /*
                if (listener != null) {
                    listener.onNextClickFromService(currentPlaying);
                }

                 */

//                sp.edit().putInt("last_song_played", currentPlaying).commit();
//                songMutableMLD.setValue(songs.get(currentPlaying));
                songPositionMLD.setValue(currentPlaying);


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

                /**
                 * changed here
                 */
                /*
                if (listener != null) {
                    listener.onPrevClickFromService(currentPlaying);
                }
                 */

//                sp.edit().putInt("last_song_played", currentPlaying).commit();
//                songMutableMLD.setValue(songs.get(currentPlaying));
                songPositionMLD.setValue(currentPlaying);


                break;
            case "close":



                listener.onCloseClickFromService(mediaPlayer);
                songPositionMLD.removeObservers(this);
                isMusicPlayingMLD.removeObservers(this);

                stopSelf();

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