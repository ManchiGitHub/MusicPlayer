package com.markokatziv.musicplayer;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements FABButtonFragment.FABButtonFragmentListener,
        AddSongDialogFragment.AddSongListener,
        SongRecyclerViewFragment.SongRecyclerViewListener,
        SongPageFragment.SongPageListener,
        PlayerFragment.PlayerFragmentListener {

    final String TAG_SPLASH_SCREEN_FRAGMENT = "splash_screen_fragment";
    final String TAG_ADD_SONG_FRAGMENT = "add_song_fragment";
    final String TAG_PLAYER_FRAGMENT = "player_fragment";
    final String TAG_SONG_PAGE_FRAGMENT = "song_page_fragment";
    final int SPLASH_DELAY_MILISEC = 1000;
    private boolean isPlaying = false;
    private boolean isStarted = false;

    SplashScreenFragment splashScreenFragment;
    SongRecyclerViewFragment songRecyclerViewFragment;
    FABButtonFragment fabButtonFragment;
    AddSongDialogFragment addSongDialogFragment;
    PlayerFragment playerFragment;
    ArrayList<Song> songs;
    SongPageFragment songPageFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        songs = SongFileHandler.readSongList(this);


        if (songs == null || songs.size() == 0) {
            songs = new ArrayList<Song>();
        }

        splashScreenFragment = new SplashScreenFragment();
        songRecyclerViewFragment = SongRecyclerViewFragment.newInstance(songs);
        fabButtonFragment = new FABButtonFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.
                beginTransaction().
                setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out).
                add(R.id.activity_main_layout, splashScreenFragment, TAG_SPLASH_SCREEN_FRAGMENT).commit();

        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_up_fragment, R.anim.fragment_fade_out)
//                        .remove(getSupportFragmentManager().findFragmentByTag(TAG_SPLASH_SCREEN_FRAGMENT))
//                        .add(R.id.activity_main_layout, songRecyclerViewFragment, TAG_RECYCLERVIEW_FRAGMENT).addToBackStack("transaction")
//                        .add(R.id.activity_main_layout, fabButtonFragment, TAG_FAB_FRAGMENT)
                        .replace(R.id.activity_main_layout, songRecyclerViewFragment).add(R.id.activity_main_layout, fabButtonFragment)
                        .commit();
            }
        }, SPLASH_DELAY_MILISEC);
    }

    @Override
    public void onAddSongBtnClick() {
        addSongDialogFragment = new AddSongDialogFragment();
        addSongDialogFragment.show(getSupportFragmentManager(), TAG_ADD_SONG_FRAGMENT);
    }

    @Override
    public void onPlaySongBtnClick(View view) {

    //    if (!isPlaying) {
          //  isStarted= !isStarted;
            if (songs.size() > 0) {
                Intent intent = new Intent(MainActivity.this, MusicService.class);
                intent.putExtra("songs_list", songs);
                intent.putExtra("command", "new_instance");
                startService(intent);
            }
    //    }
     //   else{
    //        Toast.makeText(this, "Already playing", Toast.LENGTH_SHORT).show();
    //    }


     //   isPlaying = !isPlaying;
    }

    @Override
    public void onAddSong(Song song) {

        songs.add(song);
        Toast.makeText(this, "song added", Toast.LENGTH_SHORT).show();
        SongFileHandler.saveSongList(this, songs);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try { //TODO put this in a file handler class
//                    FileOutputStream fos = openFileOutput("songs_list", Context.MODE_PRIVATE);
//                    ObjectOutputStream oos = new ObjectOutputStream(fos);
//                    oos.writeObject(songs);
//                    oos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
        songRecyclerViewFragment.notifyItemInsert(song);
    }

    @Override
    public void onCardClick(View view, int position) {
        Song song = songs.get(position);

        // this is for making sure only one song page fragment and one player fragment are alive.
        if (songPageFragment != null) {
            if (songPageFragment.isActive()) {
                return;
            }
        }

        songPageFragment = SongPageFragment.newInstance(song, position);
        playerFragment = PlayerFragment.newInstance(song, position);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.setCustomAnimations(R.anim.slide_up_add_song, R.anim.animate_down, R.anim.animate_up, R.anim.slide_down_add_song);
        fragmentTransaction.add(R.id.activity_main_layout, playerFragment, TAG_PLAYER_FRAGMENT).addToBackStack("player_frag");
        fragmentTransaction.add(R.id.activity_main_layout, songPageFragment, TAG_SONG_PAGE_FRAGMENT).addToBackStack("song_page_frag");
        fragmentTransaction.commit();

    }

    @Override
    public void onFavoriteButtonClick(int position) {
        SongFileHandler.saveSongList(this, songs);
        songRecyclerViewFragment.notifyFavoriteButtonClick(position);

    }

    @Override
    public void onSkipPrevClick() {
        //TODO: skip to previous song
        Toast.makeText(this, "onSkipPrevClick", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSkipNextClick() {
        //TODO: skip to next song
        Toast.makeText(this, "onSkipNextClick", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlayPauseClick() {
        //TODO: play / pause song
        Toast.makeText(this, "onPlayPauseClick", Toast.LENGTH_SHORT).show();
    }


//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//
//        System.out.println(fragmentManager.getFragments().size()+"-----------------------------");
//        getSupportFragmentManager().beginTransaction().detach(splashScreenFragment).commit();
//        if (fragmentManager.getBackStackEntryCount()==0){
//            finish();
//        }
//    }
}