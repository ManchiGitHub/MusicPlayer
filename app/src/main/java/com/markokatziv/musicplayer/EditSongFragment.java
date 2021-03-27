package com.markokatziv.musicplayer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * Created By marko
 */
public class EditSongFragment extends Fragment {

    final static private String SONG_KEY = "songs";
    final static private String SONG_POSITION_KEY = "song_position";

    interface EditSongFragmentListener {
        void onEditComplete(int position);
    }

    private EditSongFragmentListener callbackToActivity;

    private Song songToEdit;
    private int songPosition;

    public EditSongFragment() {
        //empty
    }

    public static EditSongFragment newInstance(Song song, int position) {
        EditSongFragment fragment = new EditSongFragment();
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
            callbackToActivity = (EditSongFragment.EditSongFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("The activity must implement EditSongFragmentListener interface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.songPosition = getArguments().getInt(SONG_POSITION_KEY);
            this.songToEdit = (Song) getArguments().getSerializable(SONG_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.edit_song_layout, container, false);

        //TODO: bind views

        return rootView;
    }





}
