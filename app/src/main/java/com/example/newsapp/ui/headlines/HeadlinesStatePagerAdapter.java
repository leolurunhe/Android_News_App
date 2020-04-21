package com.example.newsapp.ui.headlines;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

public class HeadlinesStatePagerAdapter extends FragmentStatePagerAdapter {
    private List<String> mTitles;
    private List<Fragment> mFragments;

    HeadlinesStatePagerAdapter(FragmentManager fm){
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        return mFragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.size() != 0 ? mTitles.get(position) : "";
    }

    @Override
    public int getCount() {
        return mTitles.size();
    }

    void setHeadlinesViewPagerAdapter(List<String> titles, List<Fragment> fragments){
        this.mTitles = titles;
        this.mFragments = fragments;
    }

    @Override
    public int getItemPosition(@Nullable Object object) {

        return POSITION_NONE;
    }

}
