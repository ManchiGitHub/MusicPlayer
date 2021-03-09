package com.markokatziv.musicplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class SongPageFragment extends Fragment {

    final static private String SONG_POSITION_KEY = "song_position";
    final static private String SONG_KEY = "song";


    interface SongPageListener {
        void onFavoriteButtonClick(int position);
    }

    SongPageFragment.SongPageListener callback;

    private Song song;
    private int songPosition;
    Bitmap defaultBitmap;

    public SongPageFragment() {
        // Required empty public constructor
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

    public static SongPageFragment newInstance(Song song, int position) {
        SongPageFragment fragment = new SongPageFragment();
        Bundle args = new Bundle();
        args.putInt(SONG_POSITION_KEY, position);
        args.putSerializable(SONG_KEY, song);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            song = (Song) getArguments().getSerializable("song");
            songPosition = (int) getArguments().getInt("song_position", 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_song_page, container, false);

        defaultBitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.default_song_img);
        ImageView songImage = rootView.findViewById(R.id.song_page_frag_song_img);
        TextView songTitle = rootView.findViewById(R.id.song_page_frag_song_title);
        TextView artistTitle = rootView.findViewById(R.id.song_page_frag_artist);
        Button favoriteBtn = rootView.findViewById(R.id.song_page_frag_favorite_btn);

        songTitle.setText(song.getSongTitle());
        artistTitle.setText(song.getArtistTitle());

        if (song.isFavorite()) {
            favoriteBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.heart_solid));
        }

        if (song.getImagePath().equals("")) {
            Glide.with(getActivity()).load(defaultBitmap).into(songImage);
        }
        else {
            Glide.with(getActivity()).load(song.getImagePath()).into(songImage);
        }

        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MyAnimations.AnimateFavoriteButton(v);
                //animate button
                System.out.println("HELLLLLLLLLLLLLLLOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
                // if song is not favorite
                if (!song.isFavorite()) {
                    song.setFavorite(true);
                    v.setBackground(getActivity().getDrawable(R.drawable.heart_solid));
                }
                else { //song is favorite
                    song.setFavorite(false);
                    v.setBackgroundResource(R.drawable.ic_heart);
                }

                callback.onFavoriteButtonClick(songPosition);
            }
        });


        return rootView;
    }


}