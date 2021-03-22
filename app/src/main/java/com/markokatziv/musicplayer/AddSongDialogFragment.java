package com.markokatziv.musicplayer;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

import java.io.File;
import java.security.Permission;
import java.util.Objects;

/**
 * Created By marko katziv
 */
public class AddSongDialogFragment extends DialogFragment {

    interface AddSongListener {
        void onAddSongAddSongFrag(Song song);
    }

    AddSongListener callback;

    private final int WRITE_PERMISSION_REQUEST = 1;
    private final int GALLERY_REQUEST = 2;
    final int CAMERA_REQUEST = 1;

    File photoFile;
    Uri imageUri;
    SharedPreferences sp;
    ImageView imgThumbnail;

    boolean isUserPic = false;
    boolean isFromGallery = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (Build.VERSION.SDK_INT >= 23) {
            int hasWritePermission = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
            }
        }

        try {
            callback = (AddSongListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("The activity must implement AddSongListener interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.fragment_add_song, null);

        imgThumbnail = dialogView.findViewById(R.id.img_preview);

        final EditText artistNameEt = dialogView.findViewById(R.id.artist_name_input);
        final EditText songTitleEt = dialogView.findViewById(R.id.song_title_input);
        final CheckBox favoriteCheckBox = dialogView.findViewById(R.id.favorite_checkbox);

        sp = getActivity().getSharedPreferences("details", Context.MODE_PRIVATE);

        final Button takePicBtn = dialogView.findViewById(R.id.take_pic_btn);
        takePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int imageIndex = sp.getInt("image_index", 0);
                photoFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "pic.jpg" + imageIndex);
                sp.edit().putInt("image_index", ++imageIndex).commit();
                imageUri = FileProvider.getUriForFile(getActivity(), "com.markokatziv.musicplayer.provider", photoFile);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, CAMERA_REQUEST);
            }
        });

        final Button choosePicFromGalleryBtn = dialogView.findViewById(R.id.choose_from_gallery_btn);
        choosePicFromGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        builder.setView(dialogView).setPositiveButton("Add Song", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String artistName = artistNameEt.getText().toString();
                String songTitle = songTitleEt.getText().toString();

                Song song = new Song();
                song.setFavorite(favoriteCheckBox.isChecked());
                song.setSongTitle(songTitle);
                song.setArtistTitle(artistName);
                song.setLinkToSong("https://www.mboxdrive.com/Circles.mp3");

                if (isUserPic) {
                    song.setImagePath(photoFile.getAbsolutePath());
                    isUserPic = false;
                }
                if (isFromGallery) {
                    song.setImagePath(imageUri.toString());
                    isFromGallery = false;
                }

                callback.onAddSongAddSongFrag(song);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.color.light_pink);
        dialog.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;

        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == getActivity().RESULT_OK) {
            if (data == null) {
            }

            isUserPic = true;
            imgThumbnail.setVisibility(View.VISIBLE);
            Glide.with(getActivity()).load(photoFile.getAbsoluteFile()).into(imgThumbnail);
        }

        if (requestCode == GALLERY_REQUEST && resultCode == getActivity().RESULT_OK) {
            isFromGallery = true;
            imgThumbnail.setVisibility(View.VISIBLE);
            imageUri = data.getData();
            Glide.with(getActivity()).load(imageUri).into(imgThumbnail);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Can't take picture. Need Permissions", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
