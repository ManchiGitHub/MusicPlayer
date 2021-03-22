package com.markokatziv.musicplayer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Created By marko katziv
 */
public class FABButtonFragment extends Fragment {

    /* Interface for callbacks. */
    interface FABButtonFragmentListener {
        void onAddSongBtnClickFABFrag();

        void onPlaySongBtnClickFABFrag(View view);
    }

    FABButtonFragmentListener callback;

    /* Buttons */
    private FloatingActionButton fab;
    private FloatingActionButton addSongfab;
    private FloatingActionButton playSongfab;

    /* Animations */
    private Animation animUp;
    private Animation animDown;
    private Animation animRight;
    private Animation animLeft;
    private Animation rotateOpen;
    private Animation rotateClose;

    boolean isClicked = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            callback = (FABButtonFragment.FABButtonFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("The activity must implement FABButtonFragmentListener interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_floating_button, container, false);

        fab = rootView.findViewById(R.id.main_fab_btn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFABButtonClicked();
            }
        });

        addSongfab = rootView.findViewById(R.id.add_song_fab_btn);
        addSongfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onAddSongBtnClickFABFrag();
            }
        });

        playSongfab = rootView.findViewById(R.id.play_song_fab_btn);
        playSongfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onPlaySongBtnClickFABFrag(v);
            }
        });

        return rootView;
    }

    @Nullable
    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        animUp = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_up);
        animDown = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_down);
        animRight = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_down_play_btn);
        animLeft = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_up_play_btn);
        rotateOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_close_anim);

        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    private void onFABButtonClicked() {
        setVisibilityAndClickability(isClicked);
        setAnimation(isClicked);
        isClicked = !isClicked;
    }

    private void setVisibilityAndClickability(boolean clicked) {
        if (!clicked) {

            addSongfab.setVisibility(View.VISIBLE);
            addSongfab.setClickable(true);
            playSongfab.setVisibility(View.VISIBLE);
            playSongfab.setClickable(true);
        }
        else {

            addSongfab.setVisibility(View.INVISIBLE);
            addSongfab.setClickable(false);
            playSongfab.setVisibility(View.INVISIBLE);
            playSongfab.setClickable(false);
        }
    }

    private void setAnimation(boolean clicked) {
        if (!clicked) {

            addSongfab.startAnimation(animUp);
            playSongfab.startAnimation(animRight);
            fab.startAnimation(rotateOpen);
        }
        else {

            fab.startAnimation(rotateClose);
            addSongfab.startAnimation(animDown);
            playSongfab.startAnimation(animLeft);
        }
    }
}
