package com.markokatziv.musicplayer;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class PlayerFragment extends Fragment implements View.OnClickListener {

    interface PlayerFragmentListener {
        void onSkipPrevClick();
        void onSkipNextClick();
        void onPlayPauseClick();
    }

    PlayerFragmentListener callback;

    Handler handler;
    private int switchNumber = 0;
    ImageView playPauseIV;
    private AnimatedVectorDrawableCompat avd;
    private AnimatedVectorDrawable avd2;

    public PlayerFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static PlayerFragment newInstance(Song song, int position) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putInt("song_position", position);
        args.putSerializable("song", song);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            callback = (PlayerFragment.PlayerFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("The activity must implement PlayerFragmentListener interface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        Button skipPrev = rootView.findViewById(R.id.skip_prev);
        skipPrev.setOnClickListener(this);

        Button skipNext = rootView.findViewById(R.id.skip_next);
        skipNext.setOnClickListener(this);

        playPauseIV = rootView.findViewById(R.id.play_pause_button);
        playPauseIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO insert animation to handler and separate private function
                animatePlayPauseButton();



                callback.onPlayPauseClick();
            }

        });

        return rootView;
    }

    public void animatePlayPauseButton(){

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (switchNumber == 0) {
                    Drawable drawable = ResourcesCompat.getDrawable(getActivity().getResources(), R.drawable.play_to_pause, null);
                    playPauseIV.setImageDrawable(drawable);

                    if (drawable instanceof AnimatedVectorDrawableCompat) {
                        avd = (AnimatedVectorDrawableCompat) drawable;
                        avd.start();
                    }
                    else if (drawable instanceof AnimatedVectorDrawable) {
                        avd2 = (AnimatedVectorDrawable) drawable;
                        avd2.start();
                    }

                    switchNumber++;
                }
                else {
                    Drawable drawable = ResourcesCompat.getDrawable(getActivity().getResources(), R.drawable.pause_to_play, null);
                    playPauseIV.setImageDrawable(drawable);

                    if (drawable instanceof AnimatedVectorDrawableCompat) {
                        avd = (AnimatedVectorDrawableCompat) drawable;
                        avd.start();
                    }
                    else if (drawable instanceof AnimatedVectorDrawable) {
                        avd2 = (AnimatedVectorDrawable) drawable;
                        avd2.start();
                    }
                    switchNumber--;

                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        handler.post(new Runnable() {
            @Override
            public void run() {

                int ID = v.getId();

                switch (ID) {
                    case R.id.skip_prev:
                        System.out.println("prev");
                        MyAnimations.AnimateBackAndPrevBtns(v, -40); //TODO should be private final int
                        callback.onSkipPrevClick();

                        break;
                    case R.id.skip_next:
                        System.out.println("next");
                        MyAnimations.AnimateBackAndPrevBtns(v, 40); //TODO should be private final int
                        callback.onSkipNextClick();
                        break;
                }
            }
        });
    }
}