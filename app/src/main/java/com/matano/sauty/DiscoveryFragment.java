package com.matano.sauty;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioButton;

import com.matano.sauty.Model.SautyUser;

/**
 * Created by matano on 17/3/17.
 */

public class DiscoveryFragment extends Fragment
{
    RadioButton trendingPostRadioBut;
    RadioButton popularRadioBut;
    boolean isTrendingRadioCheckedAlready;
    boolean isPopularRadioCheckedAlready;
    boolean isFirst;
    boolean isToBeReplaced;
    FrameLayout discoverFrameLayout;
    final String POPULAR_FRAG = "Popular Fragment";
    final String TRENDING_FRAG = "Trending Fragment";


    public static DiscoveryFragment newInstance(SautyUser sautyUser)
    {
        
        Bundle args = new Bundle();
        
        DiscoveryFragment fragment = new DiscoveryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        isPopularRadioCheckedAlready = false;
        isTrendingRadioCheckedAlready = false;
        isToBeReplaced = false;
        isFirst = true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_discover, container, false);
        trendingPostRadioBut = (RadioButton) v.findViewById(R.id.trendingPostRadioBut);
        popularRadioBut = (RadioButton) v.findViewById(R.id.popularRadioButton);
        discoverFrameLayout = (FrameLayout) v.findViewById(R.id.discoverFrameLayout);

        trendingPostRadioBut.setChecked(true);
        trendingPostRadioBut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                trendingPostRadioBut.setChecked(true);
                popularRadioBut.setChecked(false);
                showTrendingPosts();
            }
        });

        popularRadioBut.setChecked(false);
        popularRadioBut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popularRadioBut.setChecked(true);
                trendingPostRadioBut.setChecked(false);
                showPopularUsers();
            }
        });

        showTrendingPosts();



        return v;
    }

    //TODO custom the feed Fragment to also be able to show the trending Posts
    private void showTrendingPosts()
    {
        if (trendingPostRadioBut.isChecked() && !isTrendingRadioCheckedAlready)
        {
            if (!isToBeReplaced)
            {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.add(R.id.discoverFrameLayout, TrendingFragment.newInstance(),
                        TRENDING_FRAG);
                transaction.commit();
            }
            else
            {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.discoverFrameLayout,
                        getFragmentManager().findFragmentByTag(TRENDING_FRAG));
                transaction.addToBackStack(null);
                transaction.commit();
            }
            isTrendingRadioCheckedAlready = true;
            isPopularRadioCheckedAlready = false;
        }
    }

    private void showPopularUsers()
    {
        if (popularRadioBut.isChecked() && !isPopularRadioCheckedAlready)
        {
            if (isFirst)
            {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.discoverFrameLayout, PopularFragment.newInstance(), POPULAR_FRAG);
                transaction.addToBackStack(null);
                transaction.commit();
                isToBeReplaced = true;
                isFirst = false;
            }
            else
            {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.discoverFrameLayout,
                        getFragmentManager().findFragmentByTag(POPULAR_FRAG));
                transaction.addToBackStack(null);
                transaction.commit();
            }
            isPopularRadioCheckedAlready = true;
            isTrendingRadioCheckedAlready = false;
        }

    }
}
