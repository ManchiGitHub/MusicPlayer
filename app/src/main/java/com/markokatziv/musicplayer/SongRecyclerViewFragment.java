package com.markokatziv.musicplayer;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created By marko
 */
public class SongRecyclerViewFragment extends Fragment implements SongAdapter.SongListenerInterface {

    interface SongRecyclerViewListener {
        void onCardClick(View view, int position);
    }

    SongRecyclerViewListener callback;

    final static private String SONGS_LIST_KEY = "songs_list";

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private ArrayList<Song> songs;
    private TextView addSongsText;

    FloatingFragment fabButtonFragment;

    public SongRecyclerViewFragment() {
        // Required empty public constructor
    }

    public static SongRecyclerViewFragment newInstance(ArrayList<Song> songsList) {
        SongRecyclerViewFragment fragment = new SongRecyclerViewFragment();
        Bundle args = new Bundle();
        args.putSerializable(SONGS_LIST_KEY, songsList);
        fragment.setArguments(args);
        return fragment;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            songs = (ArrayList<Song>) getArguments().getSerializable("songs_list");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /* Inflate the layout for this fragment */
        View rootView = inflater.inflate(R.layout.fragment_song_recycler_view, container, false);

        recyclerView = rootView.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        addSongsText = rootView.findViewById(R.id.add_songs_text);
        checkChangeInitialTextOnScreen(addSongsText);

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.SimpleCallback callback = createSongTouchHelper();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        songAdapter = new SongAdapter(songs, getActivity());
        songAdapter.setListener(this);
        recyclerView.setAdapter(songAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fabButtonFragment = new FloatingFragment();

        /* Add main FAB button as a sub fragment. */
        getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_up_fragment, R.anim.slide_down_fragment)
                .add(R.id.recycler_view_container,fabButtonFragment)
                .commit();
    }

    public void notifyItemInsert(Song song) {
        songAdapter.notifyItemInserted(songs.size());
        checkChangeInitialTextOnScreen(addSongsText);
    }

    public void notifyFavoriteButtonClick(int position) {
        songAdapter.notifyItemChanged(position);
    }

    private ItemTouchHelper.SimpleCallback createSongTouchHelper() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END) {

            final Animation scaleDownAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_scale_down);
            final Animation scaleUpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.animate_scale_up);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                Collections.swap(songs, fromPosition, toPosition);
                songAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.startAnimation(scaleDownAnimation);
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);

                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    scaleUpAnimation.setFillAfter(true);
                    viewHolder.itemView.startAnimation(scaleUpAnimation);
                }
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                MaterialAlertDialogBuilder builder = createSongDeleteDialog(viewHolder);
                builder.show();
            }
        };

        return callback;
    }

    private MaterialAlertDialogBuilder createSongDeleteDialog(RecyclerView.ViewHolder viewHolder) {
        Song song = songs.get(viewHolder.getAdapterPosition());
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle("Delete Song").setIcon(R.drawable.ic_baseline_remove_circle_outline_24).setMessage("Are you sure you want to remove " + song.getSongTitle() + " ?");
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                songAdapter.notifyItemChanged(viewHolder.getAdapterPosition());

            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                songs.remove(viewHolder.getAdapterPosition());
                songAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                checkChangeInitialTextOnScreen(addSongsText);
                SongFileHandler.saveSongList(getActivity(), songs);

            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                songAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
            }
        });

        return builder;
    }

    private void checkChangeInitialTextOnScreen(TextView addSongsText) {
        if (songs.size() > 0) {
            addSongsText.setVisibility(View.GONE);
        }
        else {
            addSongsText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSongCardClicked(int position, View view) {
        callback.onCardClick(view, position);
    }

    @Override
    public void onFavoriteClicked(int position, boolean isFavorite) {
        songs.get(position).setFavorite(isFavorite);
        songAdapter.notifyItemChanged(position);
        SongFileHandler.saveSongList(getActivity(), songs);
    }
}
