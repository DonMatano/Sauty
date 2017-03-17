package com.matano.sauty;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by matano on 17/3/17.
 */

public class PostFragment extends Fragment
{
    public static PostFragment newInstance()
    {
        
        Bundle args = new Bundle();
        
        PostFragment fragment = new PostFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
