package com.matano.sauty;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.matano.sauty.Model.SautyUser;

/**
 * Created by matano on 17/3/17.
 */

public class ProfileFragment extends Fragment
{
    private SautyUser user;
    private ImageView userProfile;
    private TextView userName;
    private TextView userStatus;
    private TextView followersCount;
    private TextView followingCount;
    private EditButtonListener editButtonListener;
    private Button editProfileButton;
    Context context;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try
        {
            editButtonListener = (EditButtonListener) context;
            this.context = context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement EditButtonListener");
        }
    }

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
        userStatus = (TextView) v.findViewById(R.id.statusTextView);
        followersCount = (TextView) v.findViewById(R.id.followersCount);
        followingCount = (TextView) v.findViewById(R.id.followingCount);
        editProfileButton = (Button) v.findViewById(R.id.editProfileButton);
        editProfileButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editButtonListener.onEditButtonClicked();
            }
        });


        initiateLayout();

        return v;
    }

    private void initiateLayout()
    {
        Glide.with(getContext()).load(Uri.parse(user.getUserProfilePic()))
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(userProfile);
        userName.setText(user.getUserName());

        if (user.getUserStatus() != null)
        {
            userStatus.setText(user.getUserStatus());
        }

        followersCount.setText(String.valueOf(user.getUserFollowersCount()));
        followingCount.setText(String.valueOf(user.getUserFollowingCount()));
    }

    interface EditButtonListener
    {
        void onEditButtonClicked();
    }
}
