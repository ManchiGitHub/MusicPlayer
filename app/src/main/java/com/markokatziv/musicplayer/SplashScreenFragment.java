package com.markokatziv.musicplayer;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreenFragment extends Fragment {

    private static final String TAG_RECYCLER_VIEW_FRAGMENT = "recycler_view_fragment";





    public SplashScreenFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_splash_screen, container, false);


        TextView textView = rootView.findViewById(R.id.splash_text);
        ImageView imageView = rootView.findViewById(R.id.music_note);


        // Inflate the layout for this fragment
        return rootView;
    }


//    @Override
//    public void onStart() {
//        super.onStart();
//
//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out);
//        fragmentTransaction.add(R.id.activity_main_layout, new SongRecyclerViewFragment(), TAG_RECYCLER_VIEW_FRAGMENT).addToBackStack(null);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//                    fragmentTransaction.commit();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }


}