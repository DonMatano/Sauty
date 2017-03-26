package com.matano.sauty;

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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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
    FirebaseIndexRecyclerAdapter<Post , PostHolder> recyclerAdapter;
    FabButtonClickedListener listener;
    Context context;

    public static FeedFragment newInstance(SautyUser sautyUser)
    {
        
        Bundle args = new Bundle();
        args.putParcelable("user", sautyUser);
        
        FeedFragment fragment = new FeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try
        {
            listener = (FabButtonClickedListener) context;
            this.context = context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement OnArticleSelectedListener");
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
                    "/usersWalls/" + firebaseAuth.getCurrentUser().getUid());

            Query dataRef = databaseHelper.getRootDatabaseRef().child(
                    "/posts/");

            recyclerAdapter = new FirebaseIndexRecyclerAdapter<Post, PostHolder>(
                    Post.class, R.layout.post_view, PostHolder.class,
                    keyRef, dataRef)
            {

                //Don't even Ask what the fuck is happening below.

                @Override
                protected void populateViewHolder(final PostHolder postHolder, final Post post, int position)
                {
                    //Adding a listener to know when a User has been gotten in the database.

                    DatabaseHelper.UserGottenListener userGottenListener = new DatabaseHelper.UserGottenListener()
                    {
                        //Called when User is gotten
                        @Override
                        public void onUserGotten(SautyUser user)
                        {
                            //After User is gotten we get the Imaged
                            DatabaseHelper.ImageGottenListener imageGottenListener = new DatabaseHelper.ImageGottenListener()
                            {
                                //Called when image is gotten back
                                @Override
                                public void onImageGotten(SautyImage image)
                                {
                                    //We now populate the view
                                    postHolder.setPosterProfilePic(sautyUser.getUserProfilePic() , context);
                                    postHolder.setPosterProfileName(sautyUser.getUserName());
                                    postHolder.setPostImage(image.getImageUrl(), context);
                                    postHolder.setPostDescriptionTextView(post.getPostDesc());
                                }
                            };
                            //getting the image
                            databaseHelper.getImage(post.getImageUID(), imageGottenListener);
                        }
                    };
                    //getting the user
                    databaseHelper.getUser(post.getPosterId(), userGottenListener);

                }
            };

            recycler.setAdapter(recyclerAdapter);

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
        ImageButton saveImageButton;
        TextView postDescriptionTextView;

        public PostHolder(View itemView)
        {
            super(itemView);
            posterProfileName = (TextView) itemView.findViewById(R.id.posterProfileName);
            posterProfilePic = (ImageView) itemView.findViewById(R.id.posterProfilePic);
            postImageView = (ImageView) itemView.findViewById(R.id.postImageView);
            likeImageButton = (ImageButton) itemView.findViewById(R.id.likeImageButton);
            shareImageButton = (ImageButton) itemView.findViewById(R.id.shareImageButton);
            saveImageButton = (ImageButton) itemView.findViewById(R.id.saveImageButton);
            postDescriptionTextView = (TextView) itemView.findViewById(R.id.post_text_description);
        }


        void setPosterProfileName(String profileName)
        {
            posterProfileName.setText(profileName);
        }

        void setPostImage(String downloadImage, Context context)
        {
            Glide.with(context)
                    .load(downloadImage)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(postImageView);
        }

        void setPostDescriptionTextView(String postDescription)
        {
            postDescriptionTextView.setText(postDescription);
        }

        void setPosterProfilePic(String profilePic, Context context)
        {
            Glide.with(context).
                    load(profilePic)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(posterProfilePic);
        }



    }


}
