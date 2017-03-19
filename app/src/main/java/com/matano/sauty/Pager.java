package com.matano.sauty;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.matano.sauty.DiscoveryFragment;
import com.matano.sauty.Model.SautyUser;
import com.matano.sauty.PostFragment;
import com.matano.sauty.ProfileFragment;

/**
 * Created by matano on 17/3/17.
 */

public class Pager extends FragmentPagerAdapter
{
    //integer to count number of tabs
    int tabCount;
    Context context;
    SautyUser sautyUser;

    //constructor

    public Pager(FragmentManager fm, int tabCount, Context context, SautyUser sautyUser)
    {
        super(fm);
        this.tabCount = tabCount;
        this.context = context;
        this.sautyUser = sautyUser;
    }

    @Override
    public Fragment getItem(int position)
    {
        //Return the current tabs
        switch (position)
        {
            case 0:
                return PostFragment.newInstance(sautyUser);

            case 1:
                return DiscoveryFragment.newInstance(sautyUser);

            case 2:
                return ProfileFragment.newInstance(sautyUser);

            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        return tabCount;
    }
}
