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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Created By marko katziv
 */
public class FABButtonFragment extends Fragment {

    /* Interface for callbacks. */
    interface FABButtonFragmentListener {
        void onAddSongBtnClickFABFrag();

        void onShowFavoriteSongsClickFABFrag();

    }

    FABButtonFragmentListener callback;
    private MusicStateViewModel musicStateViewModel;

    /* Buttons */
    private FloatingActionButton fab;
    private FloatingActionButton addSongFab;
    private FloatingActionButton showFavoriteSongsFab;


    private boolean isPlaying = false;
    private boolean showFavorites = false;

    /* Animations */
    private Animation animUp;
    private Animation animDown;
    private Animation animRight;
    private Animation animLeft;
    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation rotateBtn;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        animUp = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_up);
        animDown = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_down);
        animRight = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_down_play_btn);
        animLeft = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_up_play_btn);
        rotateOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_close_anim);

        rotateBtn = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_fab_btn_anim);

    }

    public void animateBtnRotation(boolean dance) {
        if (dance) {
            fab.startAnimation(rotateBtn);
        }
        else {
            fab.clearAnimation();
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
                if (isPlaying) {
                    animateBtnRotation(true);
                }
            }
        });

        //TODO: if app is recreated from notification on click, need to fab btn handle rotatation animation
        if (getActivity().getIntent().getBooleanExtra("restarted_from_notification", false)) {
            animateBtnRotation(true);
        }

        addSongFab = rootView.findViewById(R.id.add_song_fab_btn);
        addSongFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onAddSongBtnClickFABFrag();
            }
        });

        showFavoriteSongsFab = rootView.findViewById(R.id.favorite_songs_fab_btn);
        showFavoriteSongsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFavorites = !showFavorites;
                callback.onShowFavoriteSongsClickFABFrag();
            }
        });

        musicStateViewModel = new ViewModelProvider(requireActivity()).get(MusicStateViewModel.class);
        musicStateViewModel.getIsMusicPlayingMLD().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                animateBtnRotation(aBoolean);
                isPlaying = aBoolean;
            }
        });

        return rootView;
    }

    private void onFABButtonClicked() {
        setVisibilityAndClickability(isClicked);
        setAnimation(isClicked);
        isClicked = !isClicked;
    }

    private void setVisibilityAndClickability(boolean clicked) {
        if (!clicked) {

            addSongFab.setVisibility(View.VISIBLE);
            addSongFab.setClickable(true);
            showFavoriteSongsFab.setVisibility(View.VISIBLE);
            showFavoriteSongsFab.setClickable(true);
        }
        else {

            addSongFab.setVisibility(View.INVISIBLE);
            addSongFab.setClickable(false);
            showFavoriteSongsFab.setVisibility(View.INVISIBLE);
            showFavoriteSongsFab.setClickable(false);
        }
    }

    private void setAnimation(boolean clicked) {
        if (!clicked) {

            addSongFab.startAnimation(animUp);
            showFavoriteSongsFab.startAnimation(animRight);
            fab.startAnimation(rotateOpen);
        }
        else {

            fab.startAnimation(rotateClose);
            addSongFab.startAnimation(animDown);
            showFavoriteSongsFab.startAnimation(animLeft);
        }
    }
}
