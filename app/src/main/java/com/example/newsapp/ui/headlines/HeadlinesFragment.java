package com.example.newsapp.ui.headlines;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.newsapp.R;
import com.example.newsapp.ui.headlines.pages.business.BusinessPageFragment;
import com.example.newsapp.ui.headlines.pages.politics.PoliticsPageFragment;
import com.example.newsapp.ui.headlines.pages.science.SciencePageFragment;
import com.example.newsapp.ui.headlines.pages.sports.SportsPageFragment;
import com.example.newsapp.ui.headlines.pages.technology.TechnologyPageFragment;
import com.example.newsapp.ui.headlines.pages.world.WorldPageFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HeadlinesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HeadlinesFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ArrayList<String> mTitles;
    private ArrayList<Fragment> mFragments;
    private ViewPager mViewPager;


    public HeadlinesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HeadlinesFragment.
     */

    private static HeadlinesFragment newInstance(String param1, String param2) {
        HeadlinesFragment fragment = new HeadlinesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitles = new ArrayList<>();
        mTitles.add("WORLD");
        mTitles.add("BUSINESS");
        mTitles.add("POLITICS");
        mTitles.add("SPORTS");
        mTitles.add("TECHNOLOGY");
        mTitles.add("SCIENCE");
        mFragments = new ArrayList<>();
        mFragments.add(new WorldPageFragment());
        mFragments.add(new BusinessPageFragment());
        mFragments.add(new PoliticsPageFragment());
        mFragments.add(new SportsPageFragment());
        mFragments.add(new TechnologyPageFragment());
        mFragments.add(new SciencePageFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mView = inflater.inflate(R.layout.fragment_headlines, container, false);
        mViewPager = mView.findViewById(R.id.headlines_view_pager);
        TabLayout mTabLayout = mView.findViewById(R.id.headlines_tabs);
        mViewPager.setOffscreenPageLimit(0);
        FragmentManager fm = getChildFragmentManager();
        HeadlinesStatePagerAdapter mHeadlinesStatePagerAdapter = new HeadlinesStatePagerAdapter(fm);
        mHeadlinesStatePagerAdapter.setHeadlinesViewPagerAdapter(mTitles, mFragments);
        mViewPager.setAdapter(mHeadlinesStatePagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                Objects.requireNonNull(mViewPager.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}
