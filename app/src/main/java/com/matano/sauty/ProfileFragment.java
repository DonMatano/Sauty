package com.matano.sauty;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by matano on 17/3/17.
 */

public class ProfileFragment extends Fragment
{
    public static ProfileFragment newInstance()
    {

        Bundle args = new Bundle();

        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
