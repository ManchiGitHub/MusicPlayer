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


    Handler handler;
    Button playPauseBtn;
    AppCompatSeekBar seekBar;
    ProgressBar progressBar;

    private int songIndex = 0;
    private int currentSongProgress = 0;

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


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        progressBar = rootView.findViewById(R.id.progress_circle);

        seekBar = rootView.findViewById(R.id.player_frag_seekBar);
        seekBar.setMax(PreferenceHandler.getInt(PreferenceHandler.TAG_SONG_DURATION, getActivity()));
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

        Button skipPrev = rootView.findViewById(R.id.skip_prev);
        skipPrev.setOnClickListener(this);

        Button skipNext = rootView.findViewById(R.id.skip_next);
        skipNext.setOnClickListener(this);

        myAnimations = new MyAnimations();

//        if (LanguageUtils.getCurrentLanguage() == LanguageUtils.ENGLISH) {
//            skipPrev.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_skip_previous_24, null));
//            skipNext.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_skip_next_24, null));
//        }
//
        /* Flip prev and next buttons according to current language */
        String language = LanguageUtils.getCurrentLanguage();
        if (!(language.equals(LanguageUtils.ENGLISH))) {
            skipPrev.setScaleX(-1);
            skipNext.setScaleX(-1);
        }

        playPauseBtn = rootView.findViewById(R.id.play_pause_button);
        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbackToActivity.onPlayPauseClickPlayerFrag(songIndex, v);
            }
        });

        MusicStateViewModel musicStateViewModel = new ViewModelProvider(requireActivity()).get(MusicStateViewModel.class);
        musicStateViewModel.getIsMusicPlayingMLD().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                changePlayPauseIcon(aBoolean);
            }
        });

        SongProgressViewModel songProgressViewModel = new ViewModelProvider(requireActivity()).get(SongProgressViewModel.class);
        songProgressViewModel.getSongProgressMLD().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                currentSongProgress = integer;
            }
        });

        musicStateViewModel.getIsSongReady().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean songReady) {
                changeProgressBarToBtnIcon(songReady);
            }
        });

        Runnable r = new Runnable() {
            @Override
            public void run() {
                callbackToActivity.onRequestSongProgress();
                seekBar.setProgress(currentSongProgress / 1000);
                handler.postDelayed(this, 500);
            }
        };

        getActivity().runOnUiThread(r);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //   changeBtnResource(musicStateViewModel.isPlaying());

    }

    public void changeSongDuration(int duration) {
        seekBar.setMax(duration / 1000);
    }

    public void changeProgressBarToBtnIcon(boolean isSongReady) {
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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());
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