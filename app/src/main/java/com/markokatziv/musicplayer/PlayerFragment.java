package com.markokatziv.musicplayer;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;

/**
 * Created By marko katziv
 */
public class PlayerFragment extends Fragment implements View.OnClickListener {

    final static private String SONG_KEY = "songs";
    final static private String SONG_POSITION_KEY = "song_position";
    final static private int Y_TRANSITION_SKIP_PREV = 40;


    interface PlayerFragmentListener {
        void onSkipPrevClick(int songPosition);

        void onSkipNextClick(int songPosition);

        void onPlayPauseClick(int songPosition, View view);
    }

    PlayerFragmentListener callback;

    Handler handler;
    static int switchNumber = 0;
    static Button playPauseBtn;
    //    private AnimatedVectorDrawableCompat avd;
//    private AnimatedVectorDrawable avd2;
    private int songPosition;

    public PlayerFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static PlayerFragment newInstance(Song song, int position, boolean isPlaying, int listSize) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putInt(SONG_POSITION_KEY, position);
        args.putInt("list_size", listSize);
        //  args.putBoolean("is_from_FAB_btn", isFromFabBtn);
        args.putBoolean("is_playing", isPlaying);
        args.putSerializable(SONG_KEY, song);
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
            this.songPosition = getArguments().getInt(SONG_POSITION_KEY);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        Button skipPrev = rootView.findViewById(R.id.skip_prev);
        skipPrev.setOnClickListener(this);

        Button skipNext = rootView.findViewById(R.id.skip_next);
        skipNext.setOnClickListener(this);

        // flip prev and next buttons according to current locale.
        String language = LanguageUtils.getCurrentLanguage();
        if (!(language.equals(LanguageUtils.EN_LANGUAGE))) {
            skipPrev.setScaleX(-1);
            skipNext.setScaleX(-1);
        }

        playPauseBtn = rootView.findViewById(R.id.play_pause_button);

//        if (getArguments().getBoolean("is_playing")) {
        playPauseBtn.setBackgroundResource(R.drawable.ic_outline_pause_circle_24);
        switchNumber = 1;
//        }

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
                fadeOut.setDuration(50);


                if (switchNumber == 0) {
                    switchNumber++;
                    //fadeOut.start();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            v.setBackgroundResource(R.drawable.ic_outline_pause_circle_24);
                        }
                    }, 50);
                }
                else {
                    switchNumber--;
                    // fadeOut.start();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            v.setBackgroundResource(R.drawable.ic_outline_play_circle_24);
                        }
                    }, 50);
                }


                //TODO insert animation to handler and separate private function
                //animatePlayPauseButton();
                v.startAnimation(fadeOut);
                callback.onPlayPauseClick(songPosition, v);
            }

        });

        return rootView;
    }


//    public void animatePlayPauseButton() {
//
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (switchNumber == 0) {
//                    Drawable drawable = ResourcesCompat.getDrawable(getActivity().getResources(), R.drawable.play_to_pause, null);
//                    playPauseBtn.setImageDrawable(drawable);
//
//                    if (drawable instanceof AnimatedVectorDrawableCompat) {
//                        avd = (AnimatedVectorDrawableCompat) drawable;
//                        avd.start();
//                    }
//                    else if (drawable instanceof AnimatedVectorDrawable) {
//                        avd2 = (AnimatedVectorDrawable) drawable;
//                        avd2.start();
//                    }
//
//                    switchNumber++;
//                }
//                else {
//                    Drawable drawable = ResourcesCompat.getDrawable(getActivity().getResources(), R.drawable.pause_to_play, null);
//                    playPauseBtn.setImageDrawable(drawable);
//
//                    if (drawable instanceof AnimatedVectorDrawableCompat) {
//                        avd = (AnimatedVectorDrawableCompat) drawable;
//                        avd.start();
//                    }
//                    else if (drawable instanceof AnimatedVectorDrawable) {
//                        avd2 = (AnimatedVectorDrawable) drawable;
//                        avd2.start();
//                    }
//                    switchNumber--;
//
//                }
//            }
//        });
//    }

    @Override
    public void onClick(View v) {
        handler.post(new Runnable() {
            @Override
            public void run() {

                int listSize = getArguments().getInt("list_size");

                int ID = v.getId();

                if (ID == R.id.skip_prev) {
                    MyAnimations.AnimateBackAndPrevBtns(v, (-1 * Y_TRANSITION_SKIP_PREV));

                    songPosition--;
                    if (songPosition < 0) {
                        songPosition = listSize - 1;
                    }
                    callback.onSkipPrevClick(songPosition);
                }
                else if (ID == R.id.skip_next) {
                    System.out.println("next");
                    Log.d("TAGTAG", "last song position: " + songPosition);
                    MyAnimations.AnimateBackAndPrevBtns(v, Y_TRANSITION_SKIP_PREV);

                    songPosition++;
                    if (songPosition == listSize) {
                        songPosition = 0;
                    }
                    callback.onSkipNextClick(songPosition);
                }
            }
        });
    }
}