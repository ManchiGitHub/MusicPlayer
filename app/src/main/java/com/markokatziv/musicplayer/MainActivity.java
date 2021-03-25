package com.markokatziv.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import java.util.ArrayList;

/**
 * Created By marko katziv
 */
public class MainActivity extends AppCompatActivity implements FABButtonFragment.FABButtonFragmentListener,
        AddSongDialogFragment.AddSongListener,
        SongRecyclerViewFragment.SongRecyclerViewListener,
        SongPageFragment.SongPageListener,
        PlayerFragment.PlayerFragmentListener, MusicService.MusicServiceListener {

    final String TAG_ADD_SONG_FRAGMENT = "add_song_fragment";
    final String TAG_PLAYER_FRAGMENT = "player_fragment";
    final String TAG_SONG_PAGE_FRAGMENT = "song_page_fragment";
    private final String LAST_SONG_KEY = "last_song_played";

    /* View Models */
    private MusicStateViewModel musicStateViewModel;
    private SongProgressViewModel songProgressViewModel;

    /* Observers for live data */
    Observer<Integer> songPositionObserver;
    Observer<Boolean> isMusicPlayingObserver;

    private SharedPreferences sp; //TODO: not using this

    private ArrayList<Song> songs;
    private SongRecyclerViewFragment songRecyclerViewFragment;
    private PlayerFragment playerFragment;
    private SongPageFragment songPageFragment;

    private MusicService musicService;
    private boolean isServiceBounded = false;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.splashScreenTheme);
        setContentView(R.layout.activity_main);

        sp = getSharedPreferences("continuation", MODE_PRIVATE); //TODO: not using this

        // load songs
        songs = SongFileHandler.readSongList(this);

        if (songs == null || songs.size() == 0) {
            songs = new ArrayList<>();
        }

        songRecyclerViewFragment = SongRecyclerViewFragment.newInstance(songs);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.activity_main_layout, songRecyclerViewFragment).commit();

        musicStateViewModel = new ViewModelProvider(this).get(MusicStateViewModel.class);
        songProgressViewModel = new ViewModelProvider(this).get(SongProgressViewModel.class);
    }

    @Override
    public void onAddSongBtnClickFABFrag() {
        AddSongDialogFragment addSongDialogFragment = new AddSongDialogFragment();
        addSongDialogFragment.show(getSupportFragmentManager(), TAG_ADD_SONG_FRAGMENT);
    }

    @Override
    public void onPlaySongBtnClickFABFrag(View view) {

        //TODO: Decide functionality
    }

    @Override
    public void onAddSongAddSongFrag(Song song) {

        songs.add(song);
        SongFileHandler.saveSongList(this, songs);
        songRecyclerViewFragment.notifyItemInsert(song);
    }

    @Override
    public void onCardClick(View view, int position) {

        Song song = songs.get(position);

        musicStateViewModel.setIsMusicPlayingMLD(true);

        if (!isServiceBounded) {
            isServiceBounded = true;
            Intent bindIntent = new Intent(this, MusicService.class);
            bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        /* Simple solution for making sure only one instance of playerFragment and SongPageFragment can exist. */
        if (songPageFragment != null && songPageFragment.isActive()) {
            return;
        }

        songPageFragment = SongPageFragment.newInstance(song, position);
        playerFragment = PlayerFragment.newInstance(song, position, isPlaying, songs.size());

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_up_add_song, R.anim.animate_down, R.anim.animate_up, R.anim.slide_down_add_song);
        fragmentTransaction.add(R.id.activity_main_layout, playerFragment, TAG_PLAYER_FRAGMENT).addToBackStack("player_frag");
        fragmentTransaction.add(R.id.activity_main_layout, songPageFragment, TAG_SONG_PAGE_FRAGMENT).addToBackStack("song_page_frag");
        fragmentTransaction.commit();

        sp.edit().putInt(LAST_SONG_KEY, position).commit(); //TODO: not using this
        isPlaying = !isPlaying;
        if (songs.size() > 0) {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.putExtra("command", "new_instance");
            intent.putExtra("position", position);
            startService(intent);
        }
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
            //    songPageFragment.changeSongInfo(songs.get(prevSongPosition), prevSongPosition);
            startService(intent);
        }
    }

    @Override
    public void onSkipNextClickPlayerFrag(int nextSongPosition) {

        if (songs.size() > 0) {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.putExtra("command", "next");
            getIntent().putExtra("song", songs.get(nextSongPosition));
            //    songPageFragment.changeSongInfo(songs.get(nextSongPosition), nextSongPosition);
            startService(intent);
        }
    }

    @Override
    public void onPlayPauseClickPlayerFrag(int position, View view) {
        //TODO: play / pause song

        Intent intent = new Intent(MainActivity.this, MusicService.class);
        intent.putExtra("command", "play_pause");

        sp.edit().putInt(LAST_SONG_KEY, position).commit(); //TODO: not using this
        isPlaying = !isPlaying;

        if (!isServiceBounded) {
            intent.putExtra("command", "new_instance");
            Intent bindIntent = new Intent(this, MusicService.class);
            bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE); // CHANGES HERE
            playerFragment.changeBtnResource(true);
        }

        if (songs.size() > 0) {
            intent.putExtra("position", position);
            startService(intent);
        }
    }

    @Override
    public void onCloseClickFromService(MediaPlayer mediaPlayer) {

        //  mediaPlayer.stop(); //TODO: check if needed, probably not.
        playerFragment.changeBtnResource(false);

        if (isServiceBounded) {
            musicService.setCallbacks(null); // unregister
            unbindService(serviceConnection);
            isServiceBounded = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unbind from service
        if (isServiceBounded) {
            musicService.setCallbacks(null); // unregister
            unbindService(serviceConnection);
            isServiceBounded = false;
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            /* Getting service instance with IBinder. */
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            isServiceBounded = true;
            musicService = binder.getService();
            musicService.setCallbacks(MainActivity.this);

            /* Rebinding to the service requires resetting the MutableLiveData objects. */
            musicService.setIsMusicPlayingMLD();
            musicService.setSongPositionMLD();

            /* Observer for the song position. */
            songPositionObserver = new Observer<Integer>() {
                @Override
                public void onChanged(Integer songPosition) {

                    if (songPageFragment.isActive()) {
                        songPageFragment.changeSong(songs.get(songPosition), songPosition);
                    }
                }
            };
            musicService.getSongPositionMLD().observe(MainActivity.this, songPositionObserver);

            /* Observer for play/pause. */
            isMusicPlayingObserver = new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    playerFragment.changeBtnResource(aBoolean);
                    musicStateViewModel.setIsMusicPlayingMLD(aBoolean);
                }
            };
            musicService.getIsMusicPlayingMLD().observe(MainActivity.this, isMusicPlayingObserver);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceBounded = false;
        }
    };

    public int getSongProgressFromService() {

        int progressFromService = 0;
        if (musicService != null && isServiceBounded) {
            progressFromService = musicService.getSongProgress();
        }

        return progressFromService;
    }

    @Override
    public void onRequestSongProgress() {

        int progress = getSongProgressFromService();
        songProgressViewModel.setSongProgressMLD(progress);
    }

    @Override
    public void onPreparedListener(int duration) {
        playerFragment.changeSongDuration(duration);
        playerFragment.changeProgressBarToBtnIcon(true);
    }

    @Override
    public void onSongReady(boolean isSongReady) {
        playerFragment.changeProgressBarToBtnIcon(false);
    }
}