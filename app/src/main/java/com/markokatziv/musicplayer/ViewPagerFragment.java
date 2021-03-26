package com.markokatziv.musicplayer;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Created By marko
 */
public class ViewPagerFragment extends Fragment {

    interface ViewPagerFragmentListener{
        void onViewPagerCreated();
    }

    private ViewPagerFragment.ViewPagerFragmentListener callback;

    private TabLayout tabLayout;
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager2 viewPager2;

    public ViewPagerFragment() {
    }

    public static ViewPagerFragment newInstance() {
        ViewPagerFragment fragment = new ViewPagerFragment();
        Bundle args = new Bundle();
//        args.putInt(SONG_POSITION_KEY, position);
//        args.putInt("list_size", listSize);
//        args.putBoolean("is_playing", isPlaying);
//        args.putSerializable(SONG_KEY, song);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            callback = (ViewPagerFragment.ViewPagerFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("The activity must implement ViewPagerFragmentListener interface");
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void addFragmentToViewPager(Fragment fragment, String title) {
        viewPagerAdapter.addFragment(fragment, title);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.tab_and_viewpager_layout, container, false);
        viewPagerAdapter = new ViewPagerAdapter(getActivity());
        tabLayout = rootView.findViewById(R.id.tab_layout);
        viewPager2 = rootView.findViewById(R.id.viewpager);
        viewPager2.setOffscreenPageLimit(1);
        viewPager2.setUserInputEnabled(false);
        viewPager2.setAdapter(viewPagerAdapter);


        new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0) {
                    tab.setIcon(R.drawable.ic__favorite);
                }
                if (position == 1) {
                    tab.setIcon(R.drawable.ic_baseline_music_note_24);
                }
            }
        }).attach();

        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        callback.onViewPagerCreated();
     //   viewPager2.setCurrentItem(1, false);
        viewPagerAdapter.printFragmentArray();
    }
}
