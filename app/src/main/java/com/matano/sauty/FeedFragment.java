package com.matano.sauty;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.matano.sauty.Model.DatabaseHelper;
import com.matano.sauty.Model.Post;
import com.matano.sauty.Model.SautyImage;
import com.matano.sauty.Model.SautyUser;

/**
 * Created by matano on 17/3/17.
 */

public class FeedFragment extends Fragment
{
    SautyUser sautyUser;
    RecyclerView recycler;
    FloatingActionButton fab;
    ConstraintLayout fragmentConstraintLayout;
    FirebaseAuth firebaseAuth;
    DatabaseHelper databaseHelper;
    FirebaseRecyclerAdapter<Post, PostHolder> recyclerAdapter;
    FabButtonClickedListener listener;
    PostClickedListener postClickedListener;
    UserProfileClickedListener userProfileClickedListener;
    Context context;
    ProgressDialog progressDialog;

    public static FeedFragment newInstance(SautyUser sautyUser)
    {

        Bundle args = new Bundle();
        args.putParcelable("user", sautyUser);

        FeedFragment fragment = new FeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface PostClickedListener
    {
        void postClicked(Post post);
    }

    public interface  UserProfileClickedListener
    {
        void onUserProfileClicked(String userID);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try
        {
            listener = (FabButtonClickedListener) context;
            postClickedListener = (PostClickedListener) context;
            userProfileClickedListener = (UserProfileClickedListener) context;
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
        sautyUser = getArguments().getParcelable("user");
        firebaseAuth = FirebaseAuth.getInstance();
        databaseHelper = DatabaseHelper.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.show();

        View v = inflater.inflate(R.layout.fragment_feed, container, false);
        recycler = (RecyclerView) v.findViewById(R.id.fragment_recycler);
        recycler.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
//        layoutManager.setReverseLayout(true);
        recycler.setLayoutManager(layoutManager);
        fab = (FloatingActionButton) v.findViewById(R.id.add_new_post_fab);

        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onAddPostFabButtonClicked();
            }
        });
        fragmentConstraintLayout = (ConstraintLayout) v.findViewById(R.id.feedsFragmentConstriantLayout);

        isTherePosts();

        return v;
    }

    @Override
    public void onDestroy()
    {
        recyclerAdapter.cleanup();
        super.onDestroy();
    }

    private void isTherePosts()
    {
        if (firebaseAuth.getCurrentUser() != null)
        {
            DatabaseReference userFeedRef = databaseHelper.getRootDatabaseRef().child("/usersWalls/");
            userFeedRef.child(firebaseAuth.getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            if (dataSnapshot.getValue() != null)
                            {
                                //user Feed Available
                                showFeed();
                            }
                            else
                            {
                                //user Feed doesn't exist. Show no Feed TextView
                                showNoFeed();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
        }
    }

    private void showFeed()
    {
        if (firebaseAuth.getCurrentUser() != null)
        {
            Query keyRef = databaseHelper.getRootDatabaseRef().child(
                    "/usersWalls/" + firebaseAuth.getCurrentUser().getUid())
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

                    DatabaseHelper.UserGottenListener userGottenListener = new DatabaseHelper.UserGottenListener()
                    {
                        //Called when User is gotten
                        @Override
                        public void onUserGotten(SautyUser user)
                        {
                            //After User is gotten we fill up the postView
                            postHolder.setPosterProfileName(user.getUserName());
                            postHolder.setPosterProfilePic(user.getUserProfilePic(), context);
                            postHolder.setPostDescriptionTextView(post.getPostDesc());
                        }
                    };

                    //getting the user
                    databaseHelper.getUser(post.getPosterId(), userGottenListener);

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


            recycler.setAdapter(recyclerAdapter);

            progressDialog.dismiss();

            //databaseHelper.getPosts();
        }
    }


    private void showNoFeed()
    {
        recycler.setVisibility(View.GONE);

        TextView noPostTextView =
                (TextView) TextView.inflate(getContext(), R.layout.no_posts_on_wall_textview,
                null);

        fragmentConstraintLayout.addView(noPostTextView);
        progressDialog.dismiss();
    }

    interface FabButtonClickedListener
    {
        void onAddPostFabButtonClicked();

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
