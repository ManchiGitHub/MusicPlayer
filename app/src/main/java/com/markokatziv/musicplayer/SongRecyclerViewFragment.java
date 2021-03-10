package com.markokatziv.musicplayer;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class SongRecyclerViewFragment extends Fragment implements SongAdapter.SongListenerInterface {

    final static private String SONGS_LIST_KEY = "songs_list";

    interface SongRecyclerViewListener {
        void onCardClick(View view, int position);
    }

    SongRecyclerViewListener callback;

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private List<Song> songs;
    private TextView addSongsText;

    public static SongRecyclerViewFragment newInstance(ArrayList<Song> songsList) {
        SongRecyclerViewFragment fragment = new SongRecyclerViewFragment();
        Bundle args = new Bundle();
        args.putSerializable(SONGS_LIST_KEY, songsList);
        fragment.setArguments(args);
        return fragment;
    }

    public SongRecyclerViewFragment() {
        // Required empty public constructor
    }

    public void notifyItemInsert(Song song) {
        songAdapter.notifyItemInserted(songs.size());
        changeInitialTextOnScreen(addSongsText);
    }

    public void notifyFavoriteButtonClick(int position) {
        songAdapter.notifyItemChanged(position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            songs = (List<Song>) getArguments().getSerializable("songs_list");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            callback = (SongRecyclerViewFragment.SongRecyclerViewListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("The activity must implement SongRecyclerViewListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_song_recycler_view, container, false);
        // Inflate the layout for this fragment

        recyclerView = rootView.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        addSongsText = rootView.findViewById(R.id.add_songs_text);
        changeInitialTextOnScreen(addSongsText);

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                Song song = songs.get(viewHolder.getAdapterPosition());

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
                builder.setTitle("Delete Song").setIcon(R.drawable.ic_baseline_remove_circle_outline_24).setMessage("Are you sure you want to remove " + song.getSongTitle() + " ?");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        songs.remove(viewHolder.getAdapterPosition());
                        songAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                        changeInitialTextOnScreen(addSongsText);
                        SongFileHandler.saveSongList(getActivity(), songs);
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        songAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    }
                }).show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        songAdapter = new SongAdapter(songs, getActivity());
        songAdapter.setListener(this);
        recyclerView.setAdapter(songAdapter);

        return rootView;
    }

    private void changeInitialTextOnScreen(TextView addSongsText) {
        if (songs.size() > 0) {
            addSongsText.setVisibility(View.GONE);
        }
        else{
            addSongsText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSongCardClicked(int position, View view) {
        callback.onCardClick(view, position);
    }
}
