package com.markokatziv.musicplayer;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created By marko katziv
 */
public class MainActivity extends AppCompatActivity implements FABButtonFragment.FABButtonFragmentListener,
        AddSongDialogFragment.AddSongListener,
        SongRecyclerViewFragment.SongRecyclerViewListener,
        SongPageFragment.SongPageListener,
        PlayerFragment.PlayerFragmentListener, MusicService.MusicServiceListener {

    final String TAG_SPLASH_SCREEN_FRAGMENT = "splash_screen_fragment";
    final String TAG_ADD_SONG_FRAGMENT = "add_song_fragment";
    final String TAG_PLAYER_FRAGMENT = "player_fragment";
    final String TAG_SONG_PAGE_FRAGMENT = "song_page_fragment";
    final int SPLASH_DELAY_MILISEC = 500;
    private final String LAST_SONG_KEY = "last_song_played";
    SharedPreferences sp;

    private boolean isPlaying = false;


    SplashScreenFragment splashScreenFragment;
    SongRecyclerViewFragment songRecyclerViewFragment;
    FABButtonFragment fabButtonFragment;
    AddSongDialogFragment addSongDialogFragment;
    PlayerFragment playerFragment;
    ArrayList<Song> songs;
    SongPageFragment songPageFragment;


    private MusicService musicService;
    private boolean bound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.splashScreenTheme);
        setContentView(R.layout.activity_main);


        Log.d("ON STATE", "onCreate: main activity");

        sp = getSharedPreferences("continuation", MODE_PRIVATE);

        // load songs
        songs = SongFileHandler.readSongList(this);


        if (songs == null || songs.size() == 0) {
            songs = new ArrayList<Song>();
        }

        splashScreenFragment = new SplashScreenFragment();
        songRecyclerViewFragment = SongRecyclerViewFragment.newInstance(songs);
        fabButtonFragment = new FABButtonFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().add(R.id.activity_main_layout, songRecyclerViewFragment).add(R.id.activity_main_layout, fabButtonFragment).commit();

//
//        if (getIntent().getBooleanExtra("no_splash_screen", false) == false) {
//            fragmentManager.beginTransaction()
//                    .setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out)
//                    .add(R.id.activity_main_layout, splashScreenFragment, TAG_SPLASH_SCREEN_FRAGMENT).commit();
//
//            new Handler(getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_up_fragment, R.anim.fragment_fade_out)
//                            .replace(R.id.activity_main_layout, songRecyclerViewFragment).add(R.id.activity_main_layout, fabButtonFragment)
//                            .commit();
//                }
//            }, SPLASH_DELAY_MILISEC);
//        }
//        else {
//            new Handler(getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
//                    fragmentManager.beginTransaction()
//                            .replace(R.id.activity_main_layout, songRecyclerViewFragment).add(R.id.activity_main_layout, fabButtonFragment)
//                            .commit();
//                }
//            });
//        }
    }

    @Override
    public void onAddSongBtnClickFABFrag() {
        addSongDialogFragment = new AddSongDialogFragment();
        addSongDialogFragment.show(getSupportFragmentManager(), TAG_ADD_SONG_FRAGMENT);
    }

    @Override
    public void onPlaySongBtnClickFABFrag(View view) {

        /**
         * TODO:
         *  OPTION A:
         *  Click when NOT PLAYING:
         *  1) Start main FAB button icon spin animation
         *  2) Switch to play icon if needed.
         *  3) Resume song in last position OR start song in last position OR play first song if no last position.
         *  Click when PLAYING:
         *  1) Stop main FAB button icon spin animation
         *  2) Switch to pause icon.
         *  3) Pause music.
         *  4) Save song progress position.
         *  OPTION B:
         *  Click when NOT PLAYING:
         *  1) get last song position.
         *  2) open song page.
         *  Click when PLAYING:
         *  1) open current song page.
         *
         */

//        isPlaying = true;
//        int position = sp.getInt(LAST_SONG_KEY, 0);
//        Song song = songs.get(position);
//
//        songPageFragment = SongPageFragment.newInstance(song, position);
//        playerFragment = PlayerFragment.newInstance(song, position, true, isPlaying);
//
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.setCustomAnimations(R.anim.slide_up_add_song, R.anim.animate_down, R.anim.animate_up, R.anim.slide_down_add_song);
//        fragmentTransaction.add(R.id.activity_main_layout, playerFragment, TAG_PLAYER_FRAGMENT).addToBackStack("player_frag");
//        fragmentTransaction.add(R.id.activity_main_layout, songPageFragment, TAG_SONG_PAGE_FRAGMENT).addToBackStack("song_page_frag");
//        fragmentTransaction.commit();
//
//
//        if (songs.size() > 0) {
//            Intent intent = new Intent(MainActivity.this, MusicService.class);
//            //     intent.putExtra("songs_list", songs);
//            intent.putExtra("command", "new_instance");
//            intent.putExtra("position", position);
//            startService(intent);
//        }
//

    }

    @Override
    public void onAddSongAddSongFrag(Song song) {

        songs.add(song);
        Toast.makeText(this, "song added", Toast.LENGTH_SHORT).show();
        SongFileHandler.saveSongList(this, songs);
        songRecyclerViewFragment.notifyItemInsert(song);
    }

    @Override
    public void onCardClick(View view, int position) {


        Song song = songs.get(position);

        Intent bindIntent = new Intent(this, MusicService.class);
        bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // this is for making sure only one song page fragment and one player fragment are alive.
        if (songPageFragment != null) {
            if (songPageFragment.isActive()) {
                return;
            }
        }

        sp.edit().putInt(LAST_SONG_KEY, position);
        isPlaying = !isPlaying;
        if (songs.size() > 0) {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.putExtra("command", "new_instance");
            intent.putExtra("position", position);
            startService(intent);
        }
        songPageFragment = SongPageFragment.newInstance(song, position);
        playerFragment = PlayerFragment.newInstance(song, position, isPlaying, songs.size());


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_up_add_song, R.anim.animate_down, R.anim.animate_up, R.anim.slide_down_add_song);
        fragmentTransaction.add(R.id.activity_main_layout, playerFragment, TAG_PLAYER_FRAGMENT).addToBackStack("player_frag");
        fragmentTransaction.add(R.id.activity_main_layout, songPageFragment, TAG_SONG_PAGE_FRAGMENT).addToBackStack("song_page_frag");
        fragmentTransaction.commit();
    }

    @Override
    public void onFavoriteButtonClickSongPageFrag(int position) {
        SongFileHandler.saveSongList(this, songs);
        songRecyclerViewFragment.notifyFavoriteButtonClick(position);
    }

    @Override
    public void onSkipPrevClickPlayerFrag(int prevSongPosition) {

        if (songs.size() > 0) {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.putExtra("command", "prev");
            getIntent().putExtra("song", songs.get(prevSongPosition));
            songPageFragment.changeSongInfo(songs.get(prevSongPosition), prevSongPosition);
            startService(intent);
        }
    }

    @Override
    public void onSkipNextClickPlayerFrag(int nextSongPosition) {
        Log.d("TAGTAG", "current song position: " + nextSongPosition);

        if (songs.size() > 0) {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.putExtra("command", "next");
            getIntent().putExtra("song", songs.get(nextSongPosition));
            songPageFragment.changeSongInfo(songs.get(nextSongPosition), nextSongPosition);
            startService(intent);
        }
    }

    @Override
    public void onPlayPauseClickPlayerFrag(int position, View view) {
        //TODO: play / pause song
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        intent.putExtra("command", "play_pause");

        sp.edit().putInt(LAST_SONG_KEY, position).commit();
        //  isPlaying = !isPlaying;
        if (!bound){
            intent.putExtra("command", "new_instance");
            Intent bindIntent = new Intent(this, MusicService.class);
            bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            playerFragment.changeBtnResource(true);
        }


        Log.d("SONG", "onPlayPauseClickPlayerFrag: "+position);

        if (songs.size() > 0) {
            intent.putExtra("position", position);
            startService(intent);
        }
    }

    @Override
    public void onPlayPauseClickFromService(boolean isPlay) {
        if (playerFragment != null) {
            Log.d("PLAYERFRAGMENT", "onNextClick: IS IT REALLY WORKING");
            playerFragment.changeBtnResource(isPlay);
        }
    }

    @Override
    public void onPrevClickFromService(int position) {
        //TODO: check for nulls
        songPageFragment.changeSongInfo(songs.get(position), position);
    }

    @Override
    public void onNextClickFromService(int position) {
        //TODO:
        songPageFragment.changeSongInfo(songs.get(position), position);
    }

    @Override
    public void onCloseClickFromService(MediaPlayer mediaPlayer) {
        mediaPlayer.stop();

        playerFragment.changeBtnResource(false);

        if (bound) {
            musicService.setCallbacks(null); // unregister
            unbindService(serviceConnection);
            bound = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Intent intent = new Intent(this, MusicService.class);
//        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from service
        if (bound) {
            musicService.setCallbacks(null); // unregister
            unbindService(serviceConnection);
            bound = false;
        }
    }

    /**
     * Callbacks for service binding, passed to bindService()
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MusicService instance
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            bound = true;
            musicService.setCallbacks(MainActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
}