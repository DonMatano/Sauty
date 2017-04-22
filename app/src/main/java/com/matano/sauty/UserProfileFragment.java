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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.matano.sauty.Model.DatabaseHelper;
import com.matano.sauty.Model.Post;
import com.matano.sauty.Model.SautyImage;
import com.matano.sauty.Model.SautyUser;

/**
 * Created by matano on 19/4/17.
 */

public class UserProfileFragment extends Fragment
        implements  DatabaseHelper.UserGottenListener
{
    ImageView userProfileImageView;
    TextView userNameTextView;
    TextView userFollowersCountTextView;
    TextView userFollowingCountTextView;
    Button userFollowUnfollowingButton;
    TextView userStatusTextView;
    RecyclerView userPostsRecyclerView;
    FirebaseRecyclerAdapter<Post, PostHolder> recyclerAdapter;
    DatabaseHelper databaseHelper;
    FeedFragment.PostClickedListener postClickedListener;
    FeedFragment.UserProfileClickedListener userProfileClickedListener;
    FirebaseAuth firebaseAuth;
    SautyUser user;
    Context context;

    public static UserProfileFragment newInstance(String userID)
    {

        Bundle args = new Bundle();
        args.putString("userID", userID);

        UserProfileFragment fragment = new UserProfileFragment();
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
            postClickedListener = (FeedFragment.PostClickedListener) context;
            userProfileClickedListener = (FeedFragment.UserProfileClickedListener) context;
            this.context = context;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString() +
                    " must implement OnFabClickedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        databaseHelper = DatabaseHelper.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.user_profile_view, container, false);
        userProfileImageView = (ImageView) v.findViewById(R.id.userFullProfileImage);
        userNameTextView = (TextView) v.findViewById(R.id.userNametvuserprofileName);
        userFollowersCountTextView = (TextView) v.findViewById(R.id.followerstvuserprofile);
        userFollowingCountTextView = (TextView) v.findViewById(R.id.followingTvUserprofile);
        userFollowUnfollowingButton = (Button) v.findViewById(R.id.followUnfollowButton);
        userStatusTextView = (TextView) v.findViewById(R.id.userStatusTvuserprofile);
        userPostsRecyclerView = (RecyclerView) v.findViewById(R.id.user_profile_view_recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        userPostsRecyclerView.setLayoutManager(layoutManager);

        databaseHelper.getUser(getArguments().getString("userID"), this);

        return v;
    }

    @Override
    public void onDestroy()
    {
        recyclerAdapter.cleanup();
        super.onDestroy();
    }



    private void initLayout()
    {
        Glide.with(getContext())
                .load(user.getUserProfilePic())
                .placeholder(R.drawable.com_facebook_profile_picture_blank_square)
                .crossFade(5)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(userProfileImageView);


        userNameTextView.setText(user.getUserName());
        //Todo // FIXME: 15/4/17 get String from comment node
        userFollowersCountTextView.setText(getResources()
                .getQuantityString(R.plurals.numberOfFollowers,
                        user.getUserFollowersCount(), user.getUserFollowersCount()));
        userFollowingCountTextView.setText(getResources().getString(R.string.number_of_following
        , user.getUserFollowingCount()));
        userStatusTextView.setText(user.getUserStatus());

        if (!user.getUserUid().equals(firebaseAuth.getCurrentUser().getUid()))
        {
            databaseHelper.isUserFollowed(user.getUserUid(), new DatabaseHelper.OnIsUserFollowedListener()
            {
                @Override
                public void userFollowedByUser()
                {
                    userFollowUnfollowingButton.setText(getString(R.string.unfollow_text));
                }

                @Override
                public void userNotFollowedByUser()
                {
                    userFollowUnfollowingButton.setText(getString(R.string.follow_text));

                }
            });

            userFollowUnfollowingButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (userFollowUnfollowingButton.getText().toString().equals("FOLLOW"))
                    {
                        //Follow the user
                        databaseHelper.followUser(user.getUserUid(), new DatabaseHelper.FollowUnfollowListener()
                        {
                            @Override
                            public void onUserFollowedUnfollowed(String unfollowedFollowedText)
                            {
                                userFollowUnfollowingButton.setText(getString(R.string.unfollow_text));
                            }
                        });
                    }
                    else if (userFollowUnfollowingButton.getText().toString().equals("UNFOLLOW"))
                    {
                        //Unfollow the user
                        databaseHelper.unfollowUser(user.getUserUid(), new DatabaseHelper.FollowUnfollowListener()
                        {
                            @Override
                            public void onUserFollowedUnfollowed(String unfollowedFollowedText)
                            {
                                userFollowUnfollowingButton.setText(getString(R.string.follow_text));
                            }
                        });
                    }
                }
            });
        }
        else
        {
            //User is checking own Profile. Don't show follow button;
            userFollowUnfollowingButton.setVisibility(View.INVISIBLE);
        }

        initRecyclerView();

    }

    private void initRecyclerView()
    {
        if (firebaseAuth.getCurrentUser() != null)
        {
            Query keyRef = databaseHelper.getRootDatabaseRef().child(
                    "/userPosts/" + user.getUserUid())
                    .orderByChild("invertedDateCreated");

            recyclerAdapter = new FirebaseRecyclerAdapter<Post, PostHolder>(
                    Post.class, R.layout.post_feed_view, PostHolder.class,
                    keyRef)
            {

                //Don't even Ask what the fuck is happening below.

                @Override
                protected void populateViewHolder(final PostHolder postHolder, final Post post, final int position)
                {
                    //Adding a listener to know when a User has been gotten in the database.

                    databaseHelper.isPostLiked(post, new DatabaseHelper.OnIsPostLikedListener()
                    {
                        @Override
                        public void postLikedByUser()
                        {
                            postHolder.likeImageButton.setImageResource(R.drawable.ic_thumb_up_clicked);
                            postHolder.likeImageButton.setActivated(true);
                        }

                        @Override
                        public void postNotLikedByUser()
                        {
                            postHolder.likeImageButton.setImageResource(R.drawable.ic_thumb_up_unclicked);
                            postHolder.likeImageButton.setActivated(false);
                        }
                    });

                    //We now populate the view
                    postHolder.setPosterProfilePic(user.getUserProfilePic(), context);
                    postHolder.setPosterProfileName(user.getUserName());
                    postHolder.setPostDescriptionTextView(post.getPostDesc());
                    //getting the image
                    if(post.getImageUID() != null)
                    {
                        databaseHelper.getImage(post.getImageUID(), new DatabaseHelper.ImageGottenListener()
                        {
                            //Called when image is gotten back
                            @Override
                            public void onImageGotten(SautyImage image)
                            {

                                postHolder.setPostImage(image.getImageUrl(), context);
                            }
                        });
                    }
                    else
                    {
                        postHolder.postImageView.setVisibility(View.GONE);
                    }



                    postHolder.likeImageButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            //If Activated the post Is Liked so Unlike it else add it to count.
                            if (!postHolder.likeImageButton.isActivated())
                            {
                                //Like Image and add to Liked Post.
                                postHolder.likeImageButton.setImageResource(R.drawable.ic_thumb_up_clicked);
                                postHolder.likeImageButton.setActivated(true);
                                databaseHelper.likeButtonClicked(post, "like", new DatabaseHelper.UpdatePostLikeCountListener()
                                {
                                    @Override
                                    public void onPostCountUpdatedSuccessfully()
                                    {
                                        databaseHelper.addUserToPostLikedUser(post);
                                    }
                                });
                            }
                            else
                            {
                                postHolder.likeImageButton.setImageResource(R.drawable.ic_thumb_up_unclicked);
                                postHolder.likeImageButton.setActivated(false);
                                databaseHelper.likeButtonClicked(post, "unlike", new DatabaseHelper.UpdatePostLikeCountListener()
                                {
                                    @Override
                                    public void onPostCountUpdatedSuccessfully()
                                    {
                                        databaseHelper.removeUserFromPostLikedUser(post);
                                    }
                                });

                            }
                        }
                    });

                    //Todo // FIXME: 15/4/17 get String from comment node
                    postHolder.commentTextView.setText(getResources()
                            .getQuantityString(R.plurals.numberOfCommentsAvailable,
                                    post.getPostCommentCount(), post.getPostCommentCount()));

                    postHolder.commentTextView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            postClickedListener.postClicked(post);
                        }
                    });

                    postHolder.likesCountTextView.setText(String.valueOf(post.getPostLikes()));

                    postHolder.postImageView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            postClickedListener.postClicked(post);
                        }
                    });

                    postHolder.postDescriptionTextView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            postClickedListener.postClicked(post);
                        }
                    });

//                    postHolder.posterProfileName.setOnClickListener(new View.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(View v)
//                        {
//                            userProfileClickedListener.onUserProfileClicked(post.getPosterId());
//                        }
//                    });
//
//                    postHolder.posterProfilePic.setOnClickListener(new View.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(View v)
//                        {
//                            userProfileClickedListener.onUserProfileClicked(post.getPosterId());
//                        }
//                    });

                }
            };


            userPostsRecyclerView.setAdapter(recyclerAdapter);
        }
    }


    @Override
    public void onUserGotten(SautyUser user)
    {
        this.user = user;
        initLayout();
    }



    private static class PostHolder extends RecyclerView.ViewHolder
    {
        TextView posterProfileName;
        ImageView posterProfilePic;
        ImageView postImageView;
        ImageButton likeImageButton;
        ImageButton shareImageButton;
        TextView postDescriptionTextView;
        TextView commentTextView;
        TextView likesCountTextView;

        public PostHolder(View itemView)
        {
            super(itemView);
            posterProfileName = (TextView) itemView.findViewById(R.id.postFeedposterProfileName);
            posterProfilePic = (ImageView) itemView.findViewById(R.id.fullPostposterProfilePic);
            postImageView = (ImageView) itemView.findViewById(R.id.postfeedImageView);
            likeImageButton = (ImageButton) itemView.findViewById(R.id.feedlikeImageButton);
            likeImageButton.setActivated(false);
            shareImageButton = (ImageButton) itemView.findViewById(R.id.postfeedshareImageButton);
            postDescriptionTextView = (TextView) itemView.findViewById(R.id.post_feed_text_description);
            commentTextView = (TextView) itemView.findViewById(R.id.commentTextView);
            likesCountTextView = (TextView) itemView.findViewById(R.id.likesCounttextView);
        }


        void setPosterProfileName(String profileName)
        {
            posterProfileName.setText(profileName);
        }

        void setPostImage(String downloadImage, Context context)
        {
            postImageView.getWidth();

            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(downloadImage);
            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(ref)
                    .placeholder(R.drawable.image_placeholder)
                    .crossFade(5)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .priority(Priority.HIGH)
                    .into(postImageView);
        }

        void setPostDescriptionTextView(String postDescription)
        {
            postDescriptionTextView.setText(postDescription);
        }

        void setPosterProfilePic(String profilePic, Context context)
        {
            //StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(profilePic);
            Glide.with(context)
                    .load(profilePic)
                    .placeholder(R.drawable.com_facebook_profile_picture_blank_square)
                    .thumbnail(0.5f)
                    .crossFade(5)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(posterProfilePic);

        }
    }
}
