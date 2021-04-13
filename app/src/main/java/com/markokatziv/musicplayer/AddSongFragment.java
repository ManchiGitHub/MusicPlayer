package com.markokatziv.musicplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * Created By marko
 */
public class AddSongFragment extends Fragment {

    interface AddSongFragmentListener{
        void onSaveSongClick(Song song);
    }

    AddSongFragment.AddSongFragmentListener callback;

    private final int WRITE_PERMISSION_REQUEST = 1;
    private final int GALLERY_REQUEST = 2;
    final int CAMERA_REQUEST = 1;

    private File photoFile;
    private Uri imageUri;

    boolean isUserPic = false;
    boolean isFromGallery = false;

    private EditText artistNameET;
    private EditText songTitleET;
    private EditText songLinkET;
    private ImageView imgPreviewIV;
    private Button chooseFromGallaryBtn;
    private Button takePicBtn;
    private Button saveSongBtn;
    private CheckBox favoriteCheckBox;


    public static AddSongFragment newInstance() {
        return new AddSongFragment();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (Build.VERSION.SDK_INT >= 23) {
            int hasWritePermission = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
            }
        }

        try{
            callback = (AddSongFragment.AddSongFragmentListener) context;
        }
        catch (ClassCastException ex){
            throw new ClassCastException("The activity must implement AddSongFragmentListener interface");
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_add_song, container, false);

        initViews(rootView);
        initListeners();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            isUserPic = true;
            imgPreviewIV.setVisibility(View.VISIBLE); // image preview
            Glide.with(this).load(photoFile.getAbsoluteFile()).into(imgPreviewIV);
        }

        if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {

            if (data!=null){
                imageUri = data.getData();
            }

            isFromGallery = true;
            imgPreviewIV.setVisibility(View.VISIBLE); // image preview

            Glide.with(this).load(imageUri).into(imgPreviewIV);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Need Permissions to add a picture", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews(View rootView) {

        artistNameET = rootView.findViewById(R.id.artist_name_input_add_song_frag);
        songTitleET = rootView.findViewById(R.id.song_title_input_add_song_frag);
        songLinkET = rootView.findViewById(R.id.song_link_add_song_frag);
        imgPreviewIV = rootView.findViewById(R.id.img_preview_add_song_frag);
        chooseFromGallaryBtn = rootView.findViewById(R.id.choose_from_gallery_btn_add_song_frag);
        takePicBtn = rootView.findViewById(R.id.take_pic_btn_add_song_frag);
        favoriteCheckBox = rootView.findViewById(R.id.favorite_checkbox_add_song_frag);
        saveSongBtn = rootView.findViewById(R.id.save_song_btn_add_song_frag);
    }

    private void initListeners() {

        takePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int imageIndex = PreferenceHandler.getInt(PreferenceHandler.TAG_IMAGE_INDEX,getActivity());
                photoFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "pic.jpg" + imageIndex);
                PreferenceHandler.putInt(PreferenceHandler.TAG_IMAGE_INDEX, ++imageIndex, getActivity());
                imageUri = FileProvider.getUriForFile(getActivity(), "com.markokatziv.musicplayer.provider", photoFile);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, CAMERA_REQUEST);
            }
        });

        chooseFromGallaryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        saveSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Song song = new Song();

                song.setFavorite(favoriteCheckBox.isChecked());

                String artistName = artistNameET.getText().toString().trim();
                String songTitle = songTitleET.getText().toString().trim();
                String songLink = songLinkET.getText().toString().trim();

                boolean isArtistTitleValid = checkArtistTitle(artistName);
                boolean isSongTitleValid = checkSongTitle(songTitle);

                if (!isArtistTitleValid || !isSongTitleValid){
                    Toast.makeText(getActivity(), "Song was not added. song info cannot be empty. please enter valid input.", Toast.LENGTH_LONG).show();
                }
                else{
                    // Validate song info.
                    song.setSongTitle(songTitle);
                    song.setArtistTitle(artistName);
                    song.setLinkToSong(songLink);

                    if (isUserPic) {
                        song.setImagePath(photoFile.getAbsolutePath());
                        isUserPic = false;
                    }
                    if (isFromGallery) {
                        System.out.println("imageUri: " + imageUri.toString());
                        song.setImagePath(imageUri.toString());
                        isFromGallery = false;
                    }

                    callback.onSaveSongClick(song);
                }

            }
        });

    }

    private boolean checkSongTitle(String songTitle) {
        if (songTitle.equals("")){
            return false;
        }
        else
        {
            return true;
        }
    }

    private boolean checkArtistTitle(String artistName) {
        if (artistName.equals("")){
            return false;
        }
        else
        {
            return true;
        }
    }
}
