package com.matano.sauty;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.matano.sauty.Model.SautyUser;

/**
 * Created by matano on 17/3/17.
 */

public class ProfileFragment extends Fragment
{
    private SautyUser user;
    private ImageView userProfile;
    private TextView userName;
    private TextView followersCount;
    private TextView followingCount;

    public static ProfileFragment newInstance(SautyUser sautyUser)
    {

        Bundle args = new Bundle();
        args.putParcelable("user", sautyUser);

        ProfileFragment fragment = new ProfileFragment();
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
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        userProfile = (ImageView) v.findViewById(R.id.user_profile_pic);
        userName = (TextView) v.findViewById(R.id.userNameTextView);
        followersCount = (TextView) v.findViewById(R.id.followersCount);
        followingCount = (TextView) v.findViewById(R.id.followingCount);

        initiateLayout();

        return v;
    }

    private void initiateLayout()
    {
        userProfile.setImageURI(user.getUserProfilePic());
        userName.setText(user.getUserName());
        followersCount.setText(String.valueOf(user.getUserFollowersCount()));
        followingCount.setText(String.valueOf(user.getUserFollowingCount()));
    }
}
