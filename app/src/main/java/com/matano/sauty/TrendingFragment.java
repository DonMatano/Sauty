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
 * Created by matano on 12/4/17.
 */

//Todo change the FirebaserecyclerAdapter to deal with it
public class TrendingFragment extends Fragment
{
    RecyclerView trendingPostRecyclerView;
    FirebaseRecyclerAdapter<Post, PostHolder> recyclerAdapter;
    Context context;
    FirebaseAuth firebaseAuth;
    DatabaseHelper databaseHelper;
    FeedFragment.PostClickedListener postClickedListener;
    FeedFragment.UserProfileClickedListener userProfileClickedListener;

    public static TrendingFragment newInstance()
    {

        Bundle args = new Bundle();

        TrendingFragment fragment = new TrendingFragment();
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
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString() +
                    " must implement OnPostClickedListener");
        }

        databaseHelper = DatabaseHelper.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_trending, container, false);

        trendingPostRecyclerView = (RecyclerView) v.findViewById(R.id.trendingPostRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        trendingPostRecyclerView.setLayoutManager(layoutManager);

        showTrendingFeed();

        return v;
    }

    @Override
    public void onDestroy()
    {
        recyclerAdapter.cleanup();
        super.onDestroy();
    }

    private void showTrendingFeed()
    {
        if (firebaseAuth.getCurrentUser() != null)
        {
            Query keyRef = databaseHelper.getRootDatabaseRef().child(
                    "/posts/")
                    .orderByChild("invertedLikes");

            recyclerAdapter = new FirebaseRecyclerAdapter<Post, TrendingFragment.PostHolder>(
                    Post.class, R.layout.post_feed_view, TrendingFragment.PostHolder.class,
                    keyRef)
            {

                //Don't even Ask what the fuck is happening below.

                @Override
                protected void populateViewHolder(final TrendingFragment.PostHolder postHolder, final Post post, int position)
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

                    DatabaseHelper.UserGottenListener userGottenListener = new DatabaseHelper.UserGottenListener()
                    {
                        //Called when User is gotten
                        @Override
                        public void onUserGotten(SautyUser user)
                        {
                            //After User is gotten we get the Imaged
                            //image in post get image
                            if (post.getImageUID() != null)
                            {
                                databaseHelper.getImage(post.getImageUID(),  new DatabaseHelper.ImageGottenListener()
                                {
                                    //Called when image is gotten back
                                    @Override
                                    public void onImageGotten(SautyImage image)
                                    {
                                        //Populate the imageView
                                        postHolder.setPostImage(image.getImageUrl(), context);;
                                    }
                                });
                            }
                            else
                            {
                                postHolder.postImageView.setVisibility(View.GONE);
                            }

                            postHolder.setPosterProfileName(user.getUserName());
                            postHolder.setPosterProfilePic(user.getUserProfilePic(), context);
                            postHolder.setPostDescriptionTextView(post.getPostDesc());
                        }
                    };
                    //getting the user
                    databaseHelper.getUser(post.getPosterId(), userGottenListener);



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

                    postHolder.posterProfileName.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            userProfileClickedListener.onUserProfileClicked(post.getPosterId());
                        }
                    });

                    postHolder.posterProfilePic.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            userProfileClickedListener.onUserProfileClicked(post.getPosterId());
                        }
                    });
                }
            };

            trendingPostRecyclerView.setAdapter(recyclerAdapter);

        }
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
