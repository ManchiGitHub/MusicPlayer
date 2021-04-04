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
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;


//TODO:
// 1. persist last song index and music state and change animations and icons accordingly.
// 2. better ui colors.
// 3. EDIT SONG FRAGMENT

/**
 * Created By marko
 */
public class MainActivity extends AppCompatActivity implements FloatingFragment.FloatingFragmentListener,
        AddSongDialogFragment.AddSongListener,
        SongRecyclerViewFragment.SongRecyclerViewListener,
        SongPageFragment.SongPageListener,
        PlayerFragment.PlayerFragmentListener, MusicService.MusicServiceListener, EditSongFragment.EditSongFragmentListener {

    private final String TAG_ADD_SONG_FRAGMENT = "add_song_fragment";
    private final String TAG_PLAYER_FRAGMENT = "player_fragment";
    private final String TAG_SONG_PAGE_FRAGMENT = "song_page_fragment";
    private final String TAG_EDIT_SONG_FRAGMENT = "edit_song_fragment";
    private static final int TIME_INTERVAL_BACK_PRESS = 2000;
    private long backPressed;

    /* Service */
    private MusicService musicService;

    /* View Models */
    private MusicStateViewModel musicStateViewModel;
    private SongProgressViewModel songProgressViewModel;

    private ArrayList<Song> songsList;
    private SongRecyclerViewFragment songRecyclerViewFragment;
    private PlayerFragment playerFragment;
    private SongPageFragment songPageFragment;
    private EditSongFragment editSongFragment;
    private boolean isServiceBounded = false;
    private boolean isPlaying;
    private int lastSongIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.splashScreenTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* load songs */
        songsList = SongFileHandler.readSongList(this);

        /* no songs in array list */
        if (songsList == null || songsList.size() == 0) {
            songsList = new ArrayList<>();
        }

        songRecyclerViewFragment = SongRecyclerViewFragment.newInstance(songsList);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.activity_main_layout, songRecyclerViewFragment).commit();

        musicStateViewModel = new ViewModelProvider(this).get(MusicStateViewModel.class);
        songProgressViewModel = new ViewModelProvider(this).get(SongProgressViewModel.class);
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
            intent.putExtra("position", 0);
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
    public void onEditSongClick(int position) {
        Song song = songsList.get(position);

        editSongFragment = EditSongFragment.newInstance(song, position);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_up_add_song, R.anim.animate_fade_out, R.anim.animate_fade_in, R.anim.slide_down_add_song);
        fragmentTransaction.add(R.id.activity_main_layout, editSongFragment, TAG_EDIT_SONG_FRAGMENT).addToBackStack("edit_song_frag").commit();
    }

    @Override
    public void onEditComplete(int position) {
        SongFileHandler.saveSongList(this, songsList);
        songRecyclerViewFragment.notifyItemChange(position);
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
                            playerFragment.changePlayPauseIcon(aBoolean);
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
            //  lastPosition = position;

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
        //    lastPosition = prevSongPosition;

        if (songsList.size() > 0) {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.putExtra("command", "prev");
            getIntent().putExtra("song", songsList.get(prevSongPosition));
            startService(intent);
        }
    }

    @Override
    public void onSkipNextClickPlayerFrag(int nextSongPosition) {
        //    lastPosition = nextSongPosition;

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
                    lastSongIndex = songIndex;
                    musicStateViewModel.setCurrentSong(songsList.get(songIndex));
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
    public void onBackPressed() {
        if (backPressed + TIME_INTERVAL_BACK_PRESS > System.currentTimeMillis() || songPageFragment.isActive()) {
            super.onBackPressed();
            return;
        }
        else {
            Toast.makeText(this, "Tap back button in order to exit", Toast.LENGTH_SHORT).show();
        }

        backPressed = System.currentTimeMillis();
    }
}

