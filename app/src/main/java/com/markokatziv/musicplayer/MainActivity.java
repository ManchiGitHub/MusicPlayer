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
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;


//TODO:
// 2. better ui colors.

/**
 * Created By marko
 */
public class MainActivity extends AppCompatActivity implements FloatingFragment.FloatingFragmentListener,
        AddSongDialogFragment.AddSongListener,
        SongRecyclerViewFragment.SongRecyclerViewListener,
        SongPageFragment.SongPageListener,
        PlayerFragment.PlayerFragmentListener, MusicService.MusicServiceListener {

    private final String TAG_ADD_SONG_FRAGMENT = "add_song_fragment";
    private final String TAG_PLAYER_FRAGMENT = "player_fragment";
    private final String TAG_SONG_PAGE_FRAGMENT = "song_page_fragment";

    /* Service */
    private MusicService musicService;

    /* View Models */
    private MusicStateViewModel musicStateViewModel;
    private SongProgressViewModel songProgressViewModel;

    private ArrayList<Song> songsList;
    private SongRecyclerViewFragment songRecyclerViewFragment;
    private PlayerFragment playerFragment;
    private SongPageFragment songPageFragment;
    private boolean isServiceBounded = false;
    private boolean isPlaying;
    private int lastSongIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.splashScreenTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean notFirstTime = PreferenceHandler.getBoolean(PreferenceHandler.TAG_FIRST_TIME, this);

        isPlaying = PreferenceHandler.getBoolean(PreferenceHandler.TAG_WAS_PLAYING, this);
        lastSongIndex = PreferenceHandler.getInt(PreferenceHandler.TAG_LAST_SONG_INDEX, this);

        if (MusicService.isServiceRunning) {
            Intent bindIntent = new Intent(this, MusicService.class);
            bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            isPlaying = PreferenceHandler.getBoolean(PreferenceHandler.TAG_WAS_PLAYING, this);
            lastSongIndex = PreferenceHandler.getInt(PreferenceHandler.TAG_LAST_SONG_INDEX, this);
        }


        if (!notFirstTime) {
            songsList = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                insertMySongs();
            }
            SongFileHandler.saveSongList(this, songsList);
            PreferenceHandler.putBoolean(PreferenceHandler.TAG_FIRST_TIME, true, this);
        }
        else {
            /* load songs */
            songsList = SongFileHandler.readSongList(this);

            /* no songs in array list */
            if (songsList == null || songsList.size() == 0) {
                songsList = new ArrayList<>();
            }
        }

        songRecyclerViewFragment = SongRecyclerViewFragment.newInstance(songsList);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.activity_main_layout, songRecyclerViewFragment).commit();

        musicStateViewModel = new ViewModelProvider(this).get(MusicStateViewModel.class);
        songProgressViewModel = new ViewModelProvider(this).get(SongProgressViewModel.class);
    }

    private void insertMySongs() {

        Song song1 = new Song();
        song1.setFavorite(true);
        song1.setSongTitle("One More Cup of Coffee");
        song1.setArtistTitle("Bob Dylan");
        song1.setLinkToSong("https://www.syntax.org.il/xtra/bob.m4a");
        song1.setImagePath("/drawable/bob1.png");
        song1.setImagePath("file:///android_asset/bob1.png");

        Song song2 = new Song();
        song2.setFavorite(false);
        song2.setSongTitle("The Main In me");
        song2.setArtistTitle("Bob Dylan");
        song2.setLinkToSong("https://www.syntax.org.il/xtra/bob2.mp3");
        song2.setImagePath("file:///android_asset/bob2.png");

        Song song3 = new Song();
        song3.setFavorite(true);
        song3.setSongTitle("Sara");
        song3.setArtistTitle("Bob Dylan");
        song3.setLinkToSong("https://www.syntax.org.il/xtra/bob1.m4a");
        song3.setImagePath("file:///android_asset/bob3.png");

        songsList.add(song1);
        songsList.add(song2);
        songsList.add(song3);
    }

    @Override
    public void onAddSongBtnClickFloatFrag() {
        AddSongDialogFragment addSongDialogFragment = new AddSongDialogFragment();
        addSongDialogFragment.show(getSupportFragmentManager(), TAG_ADD_SONG_FRAGMENT);
    }


    @Override
    public void onPlaySongsClickFloatFrag() {

        Intent intent = new Intent(MainActivity.this, MusicService.class);
        intent.putExtra("command", "play_pause");

        if (!isServiceBounded) {
            intent.putExtra("command", "new_instance");
            Intent bindIntent = new Intent(this, MusicService.class);
            bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE); // CHANGES HERE
        }


        if (songsList.size() > 0) {
            intent.putExtra("position", lastSongIndex);
            startService(intent);
        }
        //TODO: Decide functionality
    }

    @Override
    public void onAddSongAddSongFrag(Song song) {

        songsList.add(song);
        SongFileHandler.saveSongList(this, songsList);
        songRecyclerViewFragment.notifyItemInsert(song);
    }

    @Override
    public void onCardClick(View view, int position) {

        if (!isServiceBounded) {
            isServiceBounded = true;
            Intent bindIntent = new Intent(this, MusicService.class);
            bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        Song song = songsList.get(position);

        /* Simple solution for making sure only one instance of playerFragment and SongPageFragment can exist. */
        if (songPageFragment != null && songPageFragment.isActive()) {
            return;
        }

        if (lastSongIndex == position) {
            if (isServiceBounded) {

                musicStateViewModel.getIsMusicPlayingMLD().observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (playerFragment != null) {
                            if (playerFragment.isResumed()) {
                                playerFragment.changePlayPauseIcon(aBoolean);
                            }
                        }
                    }
                });
            }

            songPageFragment = SongPageFragment.newInstance(song, position);
            playerFragment = PlayerFragment.newInstance();

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_up_add_song, R.anim.animate_fade_out, R.anim.animate_fade_in, R.anim.slide_down_add_song);
            fragmentTransaction.add(R.id.activity_main_layout, playerFragment, TAG_PLAYER_FRAGMENT).addToBackStack("player_frag");
            fragmentTransaction.add(R.id.activity_main_layout, songPageFragment, TAG_SONG_PAGE_FRAGMENT).addToBackStack("song_page_frag");
            fragmentTransaction.commit();

        }
        else {
            lastSongIndex = position;

            songPageFragment = SongPageFragment.newInstance(song, position);
            playerFragment = PlayerFragment.newInstance();

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_up_add_song, R.anim.animate_fade_out, R.anim.animate_fade_in, R.anim.slide_down_add_song);
            fragmentTransaction.add(R.id.activity_main_layout, playerFragment, TAG_PLAYER_FRAGMENT).addToBackStack("player_frag");
            fragmentTransaction.add(R.id.activity_main_layout, songPageFragment, TAG_SONG_PAGE_FRAGMENT).addToBackStack("song_page_frag");
            fragmentTransaction.commit();

            if (songsList.size() > 0) {
                Intent intent = new Intent(MainActivity.this, MusicService.class);
                intent.putExtra("command", "new_instance");
                intent.putExtra("position", position);
                startService(intent);
            }
        }
    }

    @Override
    public void onFavoriteButtonClickSongPageFrag(int position) {
        SongFileHandler.saveSongList(this, songsList);
        songRecyclerViewFragment.notifyFavoriteButtonClick(position);
    }

    @Override
    public void onSkipPrevClickPlayerFrag(int prevSongPosition) {



        if (songsList.size() > 0) {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.putExtra("command", "prev");
            getIntent().putExtra("song", songsList.get(prevSongPosition));
            startService(intent);
        }
    }

    @Override
    public void onSkipNextClickPlayerFrag(int nextSongPosition) {

        if (songsList.size() > 0) {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.putExtra("command", "next");
            getIntent().putExtra("song", songsList.get(nextSongPosition));

            startService(intent);
        }
    }

    @Override
    public void onPlayPauseClickPlayerFrag(int position, View view) {

        Intent intent = new Intent(MainActivity.this, MusicService.class);
        intent.putExtra("command", "play_pause");

        if (!isServiceBounded) {
            intent.putExtra("command", "new_instance");
            Intent bindIntent = new Intent(this, MusicService.class);
            bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE); // CHANGES HERE

        }

        if (songsList.size() > 0) {
            intent.putExtra("position", position);
            startService(intent);
        }
    }

    @Override
    public void onCloseClickFromService(MediaPlayer mediaPlayer) {

        if (playerFragment != null) {
            playerFragment.changePlayPauseIcon(false);
        }

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
            musicService = binder.getService(); // MusiceService.this

            musicService.setCallbacks(MainActivity.this);

            /* Observer for the song position. */
            Observer<Integer> songIndexObserver = new Observer<Integer>() {
                @Override
                public void onChanged(Integer songIndex) {

                    musicStateViewModel.setCurrentSong(songsList.get(songIndex));
                    lastSongIndex = songIndex;

                    if (songPageFragment!=null){
                        songPageFragment.changeSongIndex(songIndex);
                    }

                }
            };
            musicService.getSongIndex().observe(MainActivity.this, songIndexObserver);

            /* Observer for play/pause. */
            Observer<Boolean> isMusicPlayingObserver = new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    /*  playerFragment.changeBtnResource(aBoolean); */
                    musicStateViewModel.setIsMusicPlayingMLD(aBoolean);
                    isPlaying = aBoolean;
                }
            };
            musicService.getIsMusicPlaying().observe(MainActivity.this, isMusicPlayingObserver);

            /* Observer for song ready/not. */
            Observer<Boolean> isSongReadyObserver = new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean isSongReady) {
                    musicStateViewModel.setIsSongReadyMLD(isSongReady);
                }
            };
            musicService.getIsSongReady().observe(MainActivity.this, isSongReadyObserver);

            Observer<Integer> songDurationObserver = new Observer<Integer>() {
                @Override
                public void onChanged(Integer integer) {
                    musicStateViewModel.setSongDuration(integer);
                }
            };
            musicService.getSongDuration().observe(MainActivity.this, songDurationObserver);
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
    protected void onPause() {
        super.onPause();

        String songTitle = songsList.get(lastSongIndex).getSongTitle();
        String artistTitle = songsList.get(lastSongIndex).getArtistTitle();

        PreferenceHandler.saveState(
                lastSongIndex,
                songTitle,
                artistTitle,
                this);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

