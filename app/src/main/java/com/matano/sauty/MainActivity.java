package com.matano.sauty;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.matano.sauty.View.Pager;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener
{
    TabLayout tabLayout;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeTabLayout();

    }

    public void initializeTabLayout()
    {
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

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

        viewPager = (ViewPager) findViewById(R.id.pager);

        Pager tabPagerAdapter = new Pager(getSupportFragmentManager(),
                tabLayout.getTabCount(), this);

        //Adding adapter to pager
        viewPager.setAdapter(tabPagerAdapter);
        viewPager.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(tabLayout)
        );
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
