package com.matano.sauty;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.matano.sauty.Model.SautyUser;

/**
 * Created by matano on 23/3/17.
 */

public class TabFragments extends Fragment implements TabLayout.OnTabSelectedListener
{
    TabLayout tabLayout;
    ViewPager viewPager;
    SautyUser user;
    public static TabFragments newInstance(SautyUser user)
    {

        Bundle args = new Bundle();

        args.putParcelable("user" , user);

        TabFragments fragment = new TabFragments();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        user = getArguments().getParcelable("user");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_tabs, container, false);

        tabLayout = (TabLayout) v.findViewById(R.id.tabLayout);

        //Adding the tabs using addTab() method

        tabLayout.addTab(tabLayout.newTab()
                .setText(getText(R.string.post_tab_title)));

        tabLayout.addTab(tabLayout.newTab()
                .setText(getText(R.string.discovery_tab_title)));

        tabLayout.addTab(tabLayout.newTab()
                .setText(getText(R.string.profile_tab_title)));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //Adding onTabSelectedListener to swipe views
        tabLayout.addOnTabSelectedListener(this);

        viewPager = (ViewPager) v.findViewById(R.id.pager);

        Pager tabPagerAdapter = new Pager(getChildFragmentManager(),
                tabLayout.getTabCount(), getContext(), user);

        //Adding adapter to pager
        viewPager.setAdapter(tabPagerAdapter);
        viewPager.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(tabLayout)
        );

        return v;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab)
    {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab)
    {

    }
}
