package com.markokatziv.musicplayer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songsList;
    Context context;
    Bitmap defaultBitmap;
    private SongListenerInterface listener;
    Handler handler;


    interface SongListenerInterface {
        void onSongCardClicked(int position, View view);
    }


    public void setListener(SongListenerInterface listener) {
        this.listener = listener;
    }

    public SongAdapter(List<Song> songsList, Context context) {
        this.songsList = songsList;
        this.context = context;
        defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_song_img);

    }

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private boolean isCardExpanded = false;
        private LinearLayout songCellDrawer;

        ImageView songThumbNailIV;
        TextView songTitle;
        TextView artistTitle;
        TextView albumTitle;
        ImageView heartIV;
        LinearLayout topLayout;
        ImageButton infoImageBtn;


        RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        //  ScaleAnimation scaleUpAnimation = new ScaleAnimation(1f, 1.5f, 1f, 1.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);
        // ScaleAnimation scaleDownAnimation = new ScaleAnimation(1.5f, 1f, 1.5f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);

        OvershootInterpolator overshootInterpolator = new OvershootInterpolator();


        public SongViewHolder(View v) {
            super(v);

            rotate.setDuration(300);
            rotate.setInterpolator(overshootInterpolator);

            albumTitle = v.findViewById(R.id.album_title);
            artistTitle = v.findViewById(R.id.artist_title);
            songThumbNailIV = v.findViewById(R.id.song_image);
            songTitle = v.findViewById(R.id.song_title);
            heartIV = v.findViewById(R.id.heart_img);
            songCellDrawer = v.findViewById(R.id.expanded_layout);
            topLayout = v.findViewById(R.id.top_layout);
            infoImageBtn = v.findViewById(R.id.info);
            infoImageBtn.setOnClickListener(this);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (listener != null) {
                        listener.onSongCardClicked(getAdapterPosition(), v);
                    }
                }
            });


            if (!isCardExpanded) {
                songCellDrawer.setVisibility(View.GONE);
                songCellDrawer.setEnabled(false);
            }

        }


        @Override
        public void onClick(final View view) {

            int originalHeight = songCellDrawer.getHeight();
            ValueAnimator valueAnimator;
            view.startAnimation(rotate);

            if (!isCardExpanded) {
                songCellDrawer.setVisibility(View.VISIBLE);
                songCellDrawer.setEnabled(true);
                isCardExpanded = true;
                valueAnimator = ValueAnimator.ofInt(originalHeight, originalHeight + 200); //TODO: set variable to static final int
            }
            else {
                isCardExpanded = false;
                valueAnimator = ValueAnimator.ofInt(originalHeight, originalHeight - 200); //TODO: set veriable to static final int

                Animation alphaAnimation = new AlphaAnimation(1.00f, 0.00f); // fade out
                alphaAnimation.setDuration(200);
                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        songCellDrawer.setVisibility(View.INVISIBLE);
                        songCellDrawer.setEnabled(false);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                // Set the animation on the custom view
                songCellDrawer.startAnimation(alphaAnimation);
            }
            valueAnimator.setDuration(200);
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer value = (Integer) animation.getAnimatedValue();
                    songCellDrawer.getLayoutParams().height = value.intValue();
                    songCellDrawer.requestLayout();
                }
            });
            valueAnimator.start();

        }
    }

    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_cell, parent, false);
        SongViewHolder songViewHolder = new SongViewHolder(view);


        return songViewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {


        Song song = songsList.get(position);
        holder.songTitle.setText(song.getSongTitle());
        holder.artistTitle.setText(song.getArtistTitle());
        holder.albumTitle.setText(song.getAlbumTitle());

        if (song.isFavorite()) {
            holder.heartIV.setVisibility(View.VISIBLE);
        }
        else {
            holder.heartIV.setVisibility(View.INVISIBLE);
        }

        if (song.getImagePath().equals("")) {
            Glide.with(this.context).load(defaultBitmap).thumbnail(0.25f).into(holder.songThumbNailIV);
        }
        else {
            Log.d("PATHOFIMAGE", "onBindViewHolder: "+song.getImagePath());

            if (song.getImagePath().contains("content")){
                System.out.println(song.getImagePath()+"HELLLOOOOO");
      //          Uri uri = Uri.parse(song.getImagePath());
            }
            Glide.with(this.context).load(song.getImagePath()).thumbnail(0.15f).into(holder.songThumbNailIV);
        }
    }


    @Override
    public int getItemCount() {
        return songsList == null ? 0 : songsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        System.out.println(position + "    ----------------------------------");
        return super.getItemViewType(position);

    }
}
