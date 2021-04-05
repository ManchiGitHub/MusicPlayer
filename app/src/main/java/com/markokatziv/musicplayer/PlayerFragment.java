package com.markokatziv.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;

/**
 * Created By marko katziv
 */
public class PlayerFragment extends Fragment implements View.OnClickListener {

    final static private int Y_TRANSITION_SKIP_PREV = 40;

    interface PlayerFragmentListener {
        void onSkipPrevClickPlayerFrag(int songPosition);

        void onSkipNextClickPlayerFrag(int songPosition);

        void onPlayPauseClickPlayerFrag(int songPosition, View view);

        void onRequestSongProgress();
    }

    private PlayerFragmentListener callbackToActivity;

    MyAnimations myAnimations;

    private Handler handler;
    private Button playPauseBtn;
    private AppCompatSeekBar seekBar;
    private ProgressBar progressBar;
    private Button skipPrev;
    private Button skipNext;

    private Runnable progressLoop;

    private int songIndex = 0;
    private int currentSongProgress = 0;
    private boolean isPlaying = false;

    public PlayerFragment() {
        // Required empty public constructor
    }

    public static PlayerFragment newInstance() {
        return new PlayerFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            callbackToActivity = (PlayerFragment.PlayerFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("The activity must implement PlayerFragmentListener interface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());
        myAnimations = new MyAnimations();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        initViewsAndListeners(rootView);
        initObservers();
        checkLocaleAndSetIcons();
        initProgressLoop();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void changeIcons(boolean isSongReady) {
        if (isSongReady) {
            progressBar.setVisibility(View.GONE);
            playPauseBtn.setVisibility(View.VISIBLE);
        }
        else {
            progressBar.setVisibility(View.VISIBLE);
            playPauseBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(progressLoop);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());
    }

    private void initProgressLoop() {

        progressLoop = new Runnable() {
            @Override
            public void run() {
                callbackToActivity.onRequestSongProgress();
                seekBar.setProgress(currentSongProgress / 1000);
                Log.d("markomarko", "run: " + seekBar.getProgress());
                handler.postDelayed(this, 1000);
            }
        };

        progressLoop.run();
    }

    private void checkLocaleAndSetIcons() {

        /* Flip prev and next buttons according to current language */
        String language = LanguageUtils.getCurrentLanguage();
        if (!(language.equals(LanguageUtils.ENGLISH))) {
            skipPrev.setScaleX(-1);
            skipNext.setScaleX(-1);
        }
    }

    private void initObservers() {

        initMusicStateObservers();
        initSongProgressObserver();
    }

    private void initSongProgressObserver() {
        SongProgressViewModel songProgressViewModel = new ViewModelProvider(requireActivity()).get(SongProgressViewModel.class);

        songProgressViewModel.getSongProgressMLD().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                currentSongProgress = integer;
            }
        });
    }

    private void initMusicStateObservers() {

        MusicStateViewModel musicStateViewModel = new ViewModelProvider(requireActivity()).get(MusicStateViewModel.class);

        musicStateViewModel.getSongDuration().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                seekBar.setMax(integer / 1000);
            }
        });

        musicStateViewModel.getIsMusicPlayingMLD().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                changePlayPauseIcon(aBoolean);
            }
        });

        musicStateViewModel.getIsSongReady().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean songReady) {
                changeIcons(songReady);
            }
        });
    }

    private void initViewsAndListeners(View rootView) {

        playPauseBtn = rootView.findViewById(R.id.play_pause_button);
        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbackToActivity.onPlayPauseClickPlayerFrag(songIndex, v);
            }
        });

        skipPrev = rootView.findViewById(R.id.skip_prev);
        skipPrev.setOnClickListener(this);
        skipNext = rootView.findViewById(R.id.skip_next);
        skipNext.setOnClickListener(this);
        progressBar = rootView.findViewById(R.id.progress_circle);

        seekBar = rootView.findViewById(R.id.player_frag_seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    Intent intent = new Intent(getActivity(), MusicService.class);
                    intent.putExtra("command", "seek_to");
                    intent.putExtra("progress_from_user", progress * 1000);
                    getActivity().startService(intent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void changePlayPauseIcon(boolean isPlay) {

        if (isPlay) {
            playPauseBtn.setBackgroundResource(R.drawable.ic_outline_pause_circle_24);

        }
        else { // not playing
            playPauseBtn.setBackgroundResource(R.drawable.ic_outline_play_circle_24);

        }
    }

    @Override
    public void onClick(View v) {
        handler.post(new Runnable() {
            @Override
            public void run() {

                int ID = v.getId();

                if (ID == R.id.skip_prev) {
                    myAnimations.startMyAnimation(v, (Y_TRANSITION_SKIP_PREV));

                    callbackToActivity.onSkipPrevClickPlayerFrag(songIndex);
                }
                else if (ID == R.id.skip_next) {
                    myAnimations.startMyAnimation(v, (-1 * Y_TRANSITION_SKIP_PREV));

                    callbackToActivity.onSkipNextClickPlayerFrag(songIndex);
                }
            }
        });
    }
}