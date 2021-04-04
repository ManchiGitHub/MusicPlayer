package com.markokatziv.musicplayer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Created By marko
 */
public class FloatingFragment extends Fragment {

    /* Interface for callbacks. */
    interface FloatingFragmentListener {
        void onAddSongBtnClickFloatFrag();

        void onPlaySongsClickFloatFrag();

    }

    FloatingFragmentListener callback;

    /* Buttons */
    private FloatingActionButton fab;
    private FloatingActionButton addSongFab;
    private FloatingActionButton playSongFab;
    private LinearLayout songInfoLayout;
    private TextView songTitle;
    private TextView artistTitle;

    /* Animations */
    private Animation animFadeIn;
    private Animation animFadeOut;
    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation rotateBtn;
    private Animation animateInfoSolid;
    private Animation animateInfoTransparent;

    boolean isClicked = false;
    private boolean isPlaying = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            callback = (FloatingFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("The activity must implement FABButtonFragmentListener interface");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        animFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_fade_out);

        rotateOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_close_anim);
        rotateBtn = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_fab_btn_anim);
        animateInfoSolid = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_info_fade_in_from_right);
        animateInfoTransparent = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_info_fade_out);

    }

    private void animateBtnRotation(boolean dance) {
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

        View rootView = inflater.inflate(R.layout.fragment_floating_layout, container, false);

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

//        //TODO: if app is recreated from notification on click, need to fab btn handle rotatation animation
//        if (getActivity().getIntent().getBooleanExtra("restarted_from_notification", false)) {
//            animateBtnRotation(true);
//        }

        addSongFab = rootView.findViewById(R.id.add_song_fab_btn);
        playSongFab = rootView.findViewById(R.id.play_fab_btn);
        songInfoLayout = rootView.findViewById(R.id.song_info_layout);
        songTitle = rootView.findViewById(R.id.song_title_floating_info);
        artistTitle = rootView.findViewById(R.id.artist_title_floating_info);

        addSongFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onAddSongBtnClickFloatFrag();
            }
        });

        playSongFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onPlaySongsClickFloatFrag();
            }
        });

        MusicStateViewModel musicStateViewModel = new ViewModelProvider(requireActivity()).get(MusicStateViewModel.class);

        musicStateViewModel.getIsMusicPlayingMLD().observe(this, isPlaying -> {
            this.isPlaying = isPlaying;
            animateBtnRotation(isPlaying);

            if(isPlaying){
                playSongFab.setImageResource(R.drawable.ic_baseline_pause_24);
            }
            else{
                playSongFab.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            }
        });

        musicStateViewModel.getCurrentSong().observe(this, song -> {
            songTitle.setText(song.getSongTitle());
            artistTitle.setText(song.getArtistTitle());
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
            playSongFab.setVisibility(View.VISIBLE);
            playSongFab.setClickable(true);
            songInfoLayout.setVisibility(View.VISIBLE);
            songInfoLayout.setClickable(true);
        }
        else {

            addSongFab.setVisibility(View.INVISIBLE);
            addSongFab.setClickable(false);
            playSongFab.setVisibility(View.INVISIBLE);
            playSongFab.setClickable(false);
            songInfoLayout.setVisibility(View.INVISIBLE);
            songInfoLayout.setClickable(false);
        }
    }

    private void setAnimation(boolean clicked) {
        if (!clicked) {

            songInfoLayout.startAnimation(animateInfoSolid);
            addSongFab.startAnimation(animFadeIn);
            playSongFab.startAnimation(animFadeIn);
            fab.startAnimation(rotateOpen);
        }
        else {
            songInfoLayout.startAnimation(animateInfoTransparent);
            fab.startAnimation(rotateClose);
            addSongFab.startAnimation(animFadeOut);
            playSongFab.startAnimation(animFadeOut);

        }
    }
}
