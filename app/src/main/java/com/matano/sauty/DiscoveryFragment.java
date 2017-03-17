package com.matano.sauty;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by matano on 17/3/17.
 */

public class DiscoveryFragment extends Fragment
{
    public static DiscoveryFragment newInstance()
    {
        
        Bundle args = new Bundle();
        
        DiscoveryFragment fragment = new DiscoveryFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
