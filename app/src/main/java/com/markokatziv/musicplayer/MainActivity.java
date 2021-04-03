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
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

//TODO: 1. Create option for view favorite songs only - tab created.
//      2. Create Edit song button in songPageFragment and maybe in expanded song cell - fragment created
//      3. Create Go Back button in playerFragment.
//      4. Add song progress timestamp in SongPageFragment.
//      5.

/**
 * Created By marko
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

    private SharedPreferences sharedPreferences; //TODO: not using this
    private ArrayList<Song> songsList;

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
        setTheme(R.style.splashScreenTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* load songs */
        songsList = SongFileHandler.readSongList(this);

        /* no songs in array list */
        if (songsList == null || songsList.size() == 0) {
            songsList = new ArrayList<>();
        }

        viewPagerFragment = ViewPagerFragment.newInstance();
        songRecyclerViewFragment = SongRecyclerViewFragment.newInstance(songsList);
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
        fragmentTransaction.setCustomAnimations(R.anim.slide_up_add_song, R.anim.animate_down, R.anim.animate_up, R.anim.slide_down_add_song);
        fragmentTransaction.add(R.id.activity_main_layout, editSongFragment, TAG_EDIT_SONG_FRAGMENT).addToBackStack("edit_song_frag").commit();
//       fragmentManager.beginTransaction().
//               setCustomAnimations(R.anim.slide_up_add_song, R.anim.animate_down, R.anim.animate_up, R.anim.slide_down_add_song)
//               .add(R.id.activity_main_layout, editSongFragment, TAG_EDIT_SONG_FRAGMENT).commit();


        //  songRecyclerViewFragment.notifyItemChange(position);
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

        if (lastPosition == position) {
            if (isServiceBounded) {

                musicStateViewModel.getIsMusicPlayingMLD().observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        playerFragment.changePlayPauseIcon(aBoolean);
                    }
                });
            }


            songPageFragment = SongPageFragment.newInstance(song, position);
            playerFragment = PlayerFragment.newInstance();

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_up_add_song, R.anim.animate_down, R.anim.animate_up, R.anim.slide_down_add_song);
            fragmentTransaction.add(R.id.activity_main_layout, playerFragment, TAG_PLAYER_FRAGMENT).addToBackStack("player_frag");
            fragmentTransaction.add(R.id.activity_main_layout, songPageFragment, TAG_SONG_PAGE_FRAGMENT).addToBackStack("song_page_frag");
            fragmentTransaction.commit();

//            // TODO: this is temporary. replace this
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    playerFragment.changeProgressBarToBtnIcon(true);
//                }
//            }, 200);
        }
        else {
            lastPosition = position;
            //   musicStateViewModel.setIsMusicPlayingMLD(true);


            songPageFragment = SongPageFragment.newInstance(song, position);
            playerFragment = PlayerFragment.newInstance();

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_up_add_song, R.anim.animate_down, R.anim.animate_up, R.anim.slide_down_add_song);
            fragmentTransaction.add(R.id.activity_main_layout, playerFragment, TAG_PLAYER_FRAGMENT).addToBackStack("player_frag");
            fragmentTransaction.add(R.id.activity_main_layout, songPageFragment, TAG_SONG_PAGE_FRAGMENT).addToBackStack("song_page_frag");
            fragmentTransaction.commit();

            //  sharedPreferences.edit().putInt(LAST_SONG_KEY, position).commit(); //TODO: not using this

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
        lastPosition = prevSongPosition;

        if (songsList.size() > 0) {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            intent.putExtra("command", "prev");
            getIntent().putExtra("song", songsList.get(prevSongPosition));
            startService(intent);
        }
    }

    @Override
    public void onSkipNextClickPlayerFrag(int nextSongPosition) {
        lastPosition = nextSongPosition;

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

        sharedPreferences.edit().putInt(LAST_SONG_KEY, position).commit(); //TODO: not using this
  //      isPlaying = !isPlaying;

        if (!isServiceBounded) {
            intent.putExtra("command", "new_instance");
            Intent bindIntent = new Intent(this, MusicService.class);
            bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE); // CHANGES HERE

            //    playerFragment.changeBtnResource(true);
        }

        if (songsList.size() > 0) {
            intent.putExtra("position", position);
            startService(intent);
        }
    }

    @Override
    public void onCloseClickFromService(MediaPlayer mediaPlayer) {


        //  mediaPlayer.stop(); //TODO: check if needed, probably not.
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
                    lastPosition = songIndex;
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
}