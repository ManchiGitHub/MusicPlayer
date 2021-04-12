package com.markokatziv.musicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.util.Objects;


/**
 * Created By marko
 */
public class SongPageFragment extends Fragment {

    interface SongPageListener {
        void onFavoriteButtonClickSongPageFrag(int position);
    }

    SongPageFragment.SongPageListener callback;

    final static private String SONG_POSITION_KEY = "song_position";
    final static private String SONG_KEY = "song";

    private Song song;
    private int songPosition;
    private boolean isActive;

    private Bitmap defaultBitmap;
    private ImageView songImage;
    private TextView songTitle;
    private TextView artistTitle;
    private Button favoriteBtn;

    private Animation fadeOutAnimation;

    public SongPageFragment() {
        // Required empty public constructor
    }

    public static SongPageFragment newInstance(Song song, int position) {
        SongPageFragment fragment = new SongPageFragment();
        Bundle args = new Bundle();
        args.putInt(SONG_POSITION_KEY, position);
        args.putSerializable(SONG_KEY, song);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            callback = (SongPageFragment.SongPageListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("The activity must implement SongPageListener interface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.isActive = true;
            song = (Song) getArguments().getSerializable("song");
            songPosition = (int) getArguments().getInt("song_position", 0);
        }

        fadeOutAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fragment_fade_out);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_song_page, container, false);

        defaultBitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.default_song_img);
        songImage = rootView.findViewById(R.id.song_page_frag_song_img);
        songTitle = rootView.findViewById(R.id.song_page_frag_song_title);
        artistTitle = rootView.findViewById(R.id.song_page_frag_artist);
        favoriteBtn = rootView.findViewById(R.id.song_page_frag_favorite_btn);


        MusicStateViewModel musicStateViewModel = new ViewModelProvider(requireActivity()).get(MusicStateViewModel.class);
        musicStateViewModel.getCurrentSong().observe(this, new Observer<Song>() {
            @Override
            public void onChanged(Song currentSong) {
                song = currentSong;
                setSongInfo(currentSong);
            }
        });


        favoriteBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!song.isFavorite()) {
                    song.setFavorite(true);
                    v.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic__favorite));
                }
                else {
                    song.setFavorite(false);
                    v.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite_holo));
                }

                callback.onFavoriteButtonClickSongPageFrag(songPosition);
            }
        });

        return rootView;
    }

    public void changeSongIndex(int position){
        songPosition = position;
    }

    private void setSongInfo(Song song){

        songTitle.setText(song.getSongTitle());
        artistTitle.setText(song.getArtistTitle());

        if (song.isFavorite()) {
            favoriteBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic__favorite));
        }
        else{
            favoriteBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite_holo));
        }

        if (song.getImagePath().equals("")) {
            Glide.with(getActivity()).load(defaultBitmap).into(songImage);
        }
        else {
            Glide.with(getActivity()).load(song.getImagePath()).into(songImage);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.isActive = false; // page fragment is destroyed,
        // need to set isActive to false to be able to create new instance.
    }

    public boolean isActive() {
        return isActive;
    }
}