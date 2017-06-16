package com.matano.sauty;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;
import com.matano.sauty.Model.DatabaseHelper;
import com.matano.sauty.Model.SautyUser;

/**
 * Created by matano on 12/4/17.
 */

public class PopularFragment extends Fragment
{
    RecyclerView popularUsersRecyclerView;
    FirebaseRecyclerAdapter<SautyUser, UserHolder> recyclerAdapter;
    Context context;
    FirebaseAuth firebaseAuth;
    FeedFragment.UserProfileClickedListener userProfileClickedListener;
    DatabaseHelper databaseHelper;

    public static PopularFragment newInstance()
    {

        Bundle args = new Bundle();

        PopularFragment fragment = new PopularFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        this.context = context;
        try
        {
            userProfileClickedListener = (FeedFragment.UserProfileClickedListener) context;
            this.context = context;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString() +
                    " must implement userProfileListener");
        }
        databaseHelper = DatabaseHelper.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_popular_users, container, false);
        popularUsersRecyclerView = (RecyclerView) v.findViewById(R.id.popularUserRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        popularUsersRecyclerView.setLayoutManager(layoutManager);

        showPopularUsers();

        return v;
    }

    @Override
    public void onDestroy()
    {
        recyclerAdapter.cleanup();
        super.onDestroy();
    }

    private void showPopularUsers()
    {
        if (firebaseAuth != null)
        {
            Query keyRef = databaseHelper.getRootDatabaseRef().child(
                    "/users/").orderByChild("userFollowersCount");

            recyclerAdapter = new FirebaseRecyclerAdapter<SautyUser, UserHolder>(SautyUser.class,
                    R.layout.popular_view, UserHolder.class, keyRef)
            {
                @Override
                protected void populateViewHolder(final UserHolder viewHolder, final SautyUser sautyUser, int position)
                {
                    Glide.with(context)
                            .load(sautyUser.getUserProfilePic())
                            .placeholder(R.drawable.com_facebook_profile_picture_blank_square)
                            .thumbnail(0.5f)
                            .crossFade(5)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(viewHolder.profilePicImageView);

                    viewHolder.profilePicImageView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            userProfileClickedListener.onUserProfileClicked(sautyUser.getUserUid());
                        }
                    });

                    viewHolder.userNameTextView.setText(sautyUser.getUserName());
                    viewHolder.userNameTextView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            userProfileClickedListener.onUserProfileClicked(sautyUser.getUserUid());
                        }
                    });


                    //check if viewHolder is user's account
                    if (!sautyUser.getUserUid().equals(firebaseAuth.getCurrentUser().getUid()))
                    {
                        //Not user view so show Follow button
                        databaseHelper.isUserFollowed(sautyUser.getUserUid(), new DatabaseHelper.OnIsUserFollowedListener()
                        {
                            @Override
                            public void userFollowedByUser()
                            {
                                viewHolder.followUnfollowButton.setText(getString(R.string.unfollow_text));
                            }

                            @Override
                            public void userNotFollowedByUser()
                            {
                                viewHolder.followUnfollowButton.setText(getString(R.string.follow_text));

                            }
                        });

                        viewHolder.followUnfollowButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                if (viewHolder.followUnfollowButton.getText().toString().equals("FOLLOW"))
                                {
                                    //Follow the user
                                    databaseHelper.followUser(sautyUser.getUserUid(), new DatabaseHelper.FollowUnfollowListener()
                                    {
                                        @Override
                                        public void onUserFollowedUnfollowed(String unfollowedFollowedText)
                                        {
                                             viewHolder.followUnfollowButton.setText(getString(R.string.unfollow_text));
                                        }
                                    });
                                }
                                else if (viewHolder.followUnfollowButton.getText().toString().equals("UNFOLLOW"))
                                {
                                    //Unfollow the user
                                    databaseHelper.unfollowUser(sautyUser.getUserUid(), new DatabaseHelper.FollowUnfollowListener()
                                    {
                                        @Override
                                        public void onUserFollowedUnfollowed(String unfollowedFollowedText)
                                        {
                                            viewHolder.followUnfollowButton.setText(getString(R.string.follow_text));
                                        }
                                    });
                                }
                            }
                        });
                    }
                    else
                    {
                        //User is checking own Profile. Don't show follow button;
                        viewHolder.followUnfollowButton.setVisibility(View.INVISIBLE);
                    }
                }
            };

            popularUsersRecyclerView.setAdapter(recyclerAdapter);
        }
    }

    public static class UserHolder extends RecyclerView.ViewHolder
    {
        ImageView profilePicImageView;
        TextView userNameTextView;
        Button followUnfollowButton;

        public UserHolder(View itemView)
        {
            super(itemView);

            profilePicImageView = (ImageView) itemView.findViewById(R.id.popularProfilePicImageV);
            userNameTextView = (TextView) itemView.findViewById(R.id.userNamePopularTextV);
            followUnfollowButton = (Button) itemView.findViewById(R.id.popularFollowButton);

        }

    }

}

