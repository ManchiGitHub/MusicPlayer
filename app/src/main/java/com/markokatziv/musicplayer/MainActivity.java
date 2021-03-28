package com.markokatziv.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

//TODO: 1. Create option for view favorite songs only - tab created.
//      2. Create Edit song button in songPageFragment and maybe in expanded song cell - fragment created
//      3. Create Go Back button in playerFragment.
//      4. Add song progress timestamp in SongPageFragment.
//      5.

/**
 * Created By marko katziv
 */
public class MainActivity extends AppCompatActivity implements FABButtonFragment.FABButtonFragmentListener,
        AddSongDialogFragment.AddSongListener,
        SongRecyclerViewFragment.SongRecyclerViewListener,
        SongPageFragment.SongPageListener,
        PlayerFragment.PlayerFragmentListener, MusicService.MusicServiceListener, ViewPagerFragment.ViewPagerFragmentListener, EditSongFragment.EditSongFragmentListener {

    private final String TAG_ADD_SONG_FRAGMENT = "add_song_fragment";
    private final String TAG_PLAYER_FRAGMENT = "player_fragment";
    private final String TAG_SONG_PAGE_FRAGMENT = "song_page_fragment";
    private final String LAST_SONG_KEY = "last_song_played";
    private final String TAG_EDIT_SONG_FRAGMENT = "edit_song_fragment";

    private MusicService musicService;

    /* View Models */
    private MusicStateViewModel musicStateViewModel;
    private SongProgressViewModel songProgressViewModel;

    /* Observers for live data */
    private Observer<Integer> songPositionObserver;
    private Observer<Boolean> isMusicPlayingObserver;

    private SharedPreferences sharedPreferences; //TODO: not using this
    private ArrayList<Song> fullSongsList;

    private SongRecyclerViewFragment songRecyclerViewFragment;
    private PlayerFragment playerFragment;
    private SongPageFragment songPageFragment;
    private ViewPagerFragment viewPagerFragment;
    private EditSongFragment editSongFragment;

    private FavoriteSongsFragment favoriteSongsFragment;

    private boolean isServiceBounded = false;
    private boolean isPlaying = false;
    private int lastPosition = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.splashScreenTheme);
        setContentView(R.layout.activity_main);


        /* load songs */
        fullSongsList = SongFileHandler.readSongList(this);

        /* no songs in array list */
        if (fullSongsList == null || fullSongsList.size() == 0) {
            fullSongsList = new ArrayList<>();
        }

        viewPagerFragment = ViewPagerFragment.newInstance();
        songRecyclerViewFragment = SongRecyclerViewFragment.newInstance(fullSongsList);
        favoriteSongsFragment = FavoriteSongsFragment.newInstance("yes");

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.activity_main_layout, viewPagerFragment).commit();

        sharedPreferences = getSharedPreferences("continuation", MODE_PRIVATE); //TODO: not using this

        //TODO: Integrate Navigation component.
        //    FragmentManager fragmentManager = getSupportFragmentManager();
        //   fragmentManager.beginTransaction().add(R.id.activity_main_layout, songRecyclerViewFragment).commit();
        musicStateViewModel = new ViewModelProvider(this).get(MusicStateViewModel.class);
        songProgressViewModel = new ViewModelProvider(this).get(SongProgressViewModel.class);
    }

    @Override
    public void onViewPagerCreated() {
        viewPagerFragment.addFragmentToViewPager(songRecyclerViewFragment, "");
        viewPagerFragment.addFragmentToViewPager(favoriteSongsFragment, "");
    }

    @Override
    public void onAddSongBtnClickFABFrag() {
        AddSongDialogFragment addSongDialogFragment = new AddSongDialogFragment();
        addSongDialogFragment.show(getSupportFragmentManager(), TAG_ADD_SONG_FRAGMENT);
    }

    @Override
    public void onShowFavoriteSongsClickFABFrag() {
        Log.d("markomarko", "onShowFavoriteSongsClickFABFrag: ");
        //TODO: Decide functionality
    }

    @Override
    public void onAddSongAddSongFrag(Song song) {

        fullSongsList.add(song);
        SongFileHandler.saveSongList(this, fullSongsList);
        songRecyclerViewFragment.notifyItemInsert(song);
    }

    @Override
    public void onEditSongClick(int position) {
        Song song = fullSongsList.get(position);

        editSongFragment = EditSongFragment.newInstance(song, position);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_up_add_song, R.anim.animate_down, R.anim.animate_up, R.anim.slide_down_add_song);
        fragmentTransaction.add(R.id.activity_main_layout, editSongFragment, TAG_EDIT_SONG_FRAGMENT).addToBackStack("edit_song_frag").commit();
//       fragmentManager.beginTransaction().
//               setCustomAnimations(R.anim.slide_up_add_song, R.anim.animate_down, R.anim.animate_up, R.anim.slide_down_add_song)
//               .add(R.id.activity_main_layout, editSongFragment, TAG_EDIT_SONG_FRAGMENT).commit();


      //  songRecyclerViewFragment.notifyItemChange(position);
    }

    @Override
    public void onEditComplete(int position) {
        SongFileHandler.saveSongList(this, fullSongsList);
        songRecyclerViewFragment.notifyItemChange(position);
    }

    @Override
    public void onCardClick(View view, int position) {

        Song song = fullSongsList.get(position);

        if (lastPosition == position ) {
            if (isServiceBounded){
                musicStateViewModel.getIsMusicPlayingMLD().observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        playerFragment.changeBtnResource(aBoolean);
                    }
                });
            }


            songPageFragment = SongPageFragment.newInstance(song, position);
            playerFragment = PlayerFragment.newInstance(song, position, fullSongsList.size());

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_up_add_song, R.anim.animate_down, R.anim.animate_up, R.anim.slide_down_add_song);
            fragmentTransaction.add(R.id.activity_main_layout, playerFragment, TAG_PLAYER_FRAGMENT).addToBackStack("player_frag");
            fragmentTransaction.add(R.id.activity_main_layout, songPageFragment, TAG_SONG_PAGE_FRAGMENT).addToBackStack("song_page_frag");
            fragmentTransaction.commit();

            // TODO: this is temporary. replace this
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    playerFragment.changeProgressBarToBtnIcon(true);
                }
            }, 200);
        }
        else {
            lastPosition = position;
         //   musicStateViewModel.setIsMusicPlayingMLD(true);

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
            playerFragment = PlayerFragment.newInstance(song, position, fullSongsList.size());

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_up_add_song, R.anim.animate_down, R.anim.animate_up, R.anim.slide_down_add_song);
            fragmentTransaction.add(R.id.activity_main_layout, playerFragment, TAG_PLAYER_FRAGMENT).addToBackStack("player_frag");
            fragmentTransaction.add(R.id.activity_main_layout, songPageFragment, TAG_SONG_PAGE_FRAGMENT).addToBackStack("song_page_frag");
            fragmentTransaction.commit();

            sharedPreferences.edit().putInt(LAST_SONG_KEY, position).commit(); //TODO: not using this

            if (fullSongsList.size() > 0) {
                Intent intent = new Intent(MainActivity.this, MusicService.class);
                intent.putExtra("command", "new_instance");
                intent.putExtra("position", position);
                startService(intent);
            }
        }
    }

    @Override
    public void onFavoriteButtonClickSongPageFrag(int position) {
        SongFileHandler.saveSongList(this, fullSongsList);
        songRecyclerViewFragment.notifyFavoriteButtonClick(position);
    }

    @Override
    public void onSkipPrevClickPlayerFrag(int prevSongPosition) {
        lastPosition = prevSongPosition;
        if (fullSongsList.size() > 0) {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.putExtra("command", "prev");
            getIntent().putExtra("song", fullSongsList.get(prevSongPosition));
            startService(intent);
        }
    }

    @Override
    public void onSkipNextClickPlayerFrag(int nextSongPosition) {

        lastPosition = nextSongPosition;
        if (fullSongsList.size() > 0) {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.putExtra("command", "next");
            getIntent().putExtra("song", fullSongsList.get(nextSongPosition));

            startService(intent);
        }
    }

    @Override
    public void onPlayPauseClickPlayerFrag(int position, View view) {

        Intent intent = new Intent(MainActivity.this, MusicService.class);
        intent.putExtra("command", "play_pause");

        sharedPreferences.edit().putInt(LAST_SONG_KEY, position).commit(); //TODO: not using this
        isPlaying = !isPlaying;

        if (!isServiceBounded) {
            intent.putExtra("command", "new_instance");
            Intent bindIntent = new Intent(this, MusicService.class);
            bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE); // CHANGES HERE

        //    playerFragment.changeBtnResource(true);
        }

        if (fullSongsList.size() > 0) {
            intent.putExtra("position", position);
            startService(intent);
        }
    }

    @Override
    public void onCloseClickFromService(MediaPlayer mediaPlayer) {

        //  mediaPlayer.stop(); //TODO: check if needed, probably not.
        if (playerFragment!=null){
            playerFragment.changeBtnResource(false);
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
            musicService = binder.getService();
            musicService.setCallbacks(MainActivity.this);

            /* Rebinding the service requires resetting the MutableLiveData objects. */
            musicService.setMutableLiveData();

            /* Observer for the song position. */
            songPositionObserver = new Observer<Integer>() {
                @Override
                public void onChanged(Integer songPosition) {
                    if (songPageFragment.isActive()) {
                        songPageFragment.changeSong(fullSongsList.get(songPosition), songPosition);
                    }
                }
            };
            musicService.getSongPositionMLD().observe(MainActivity.this, songPositionObserver);

            /* Observer for play/pause. */
            isMusicPlayingObserver = new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
               //     playerFragment.changeBtnResource(aBoolean);
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
        if (playerFragment != null) {
            playerFragment.changeSongDuration(duration);
            playerFragment.changeProgressBarToBtnIcon(true);
        }
    }

    @Override
    public void onSongReady(boolean isSongReady) {
        if (playerFragment != null) {
            playerFragment.changeProgressBarToBtnIcon(false);
        }
    }
}