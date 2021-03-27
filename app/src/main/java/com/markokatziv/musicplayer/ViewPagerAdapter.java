package com.markokatziv.musicplayer;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.ArrayList;

/**
 * Created By marko
 */
public class ViewPagerAdapter extends FragmentStateAdapter {

    private final ArrayList<Fragment> fragmentList;
    private final ArrayList<String> titleList;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragmentList = new ArrayList<>();
        titleList = new ArrayList<>();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
//        Fragment fragment = null;
        return fragmentList.get(position);
//        switch (position) {
//            case 0:
//                fragment = fragmentList.get(0);
//                break;
//            case 1:
//                fragment = fragmentList.get(1);
//                break;
//        }
//        return fragment;
    }

    @Override
    public int getItemCount() {
      //  Log.d("markomarko", "getItemCount: " + fragmentList.size());
        return 2;
    }


    //    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
//        super(fm, behavior);
//    }
//
//    @NonNull
//    @Override
//    public Fragment getItem(int position) {
//        return fragmentList.get(position);
//    }
//
//    @Override
//    public int getCount() {
//        return titleList.size();
//    }
//
//
//    @Nullable
//    @Override
//    public CharSequence getPageTitle(int position) {
//        return titleList.get(position);
//    }
//
//
    public void addFragment(Fragment fragment, String title) {
        fragmentList.add(fragment);
        titleList.add(title);
    }

}
