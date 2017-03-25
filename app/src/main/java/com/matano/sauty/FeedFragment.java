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

import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.matano.sauty.Model.DatabaseHelper;
import com.matano.sauty.Model.Post;
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
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
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

    private void isTherePosts()
    {
        if (firebaseAuth.getCurrentUser() != null)
        {
            DatabaseReference userFeedRef = databaseHelper.getRootDatabaseRef().child("userWalls");
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
//        recyclerAdapter = new FirebaseIndexRecyclerAdapter<Post, PostHolder>(
//                Post.class, android.R.layout.two_line_list_item, PostHolder.class,
//
//        )
//        {
//            @Override
//            protected void populateViewHolder(PostHolder viewHolder, Post model, int position)
//            {
//
//            }
//        }
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
        ImageView postImageView;
        ImageButton likeImageButton;
        ImageButton shareImageButton;
        ImageButton saveImageButton;
        TextView postDescriptionTextView;

        public PostHolder(View itemView)
        {
            super(itemView);
            posterProfileName = (TextView) itemView.findViewById(R.id.posterProfileName);
            postImageView = (ImageView) itemView.findViewById(R.id.postImageView);
            likeImageButton = (ImageButton) itemView.findViewById(R.id.likeImageButton);
            shareImageButton = (ImageButton) itemView.findViewById(R.id.shareImageButton);
            saveImageButton = (ImageButton) itemView.findViewById(R.id.saveImageButton);
            postDescriptionTextView = (TextView) itemView.findViewById(R.id.post_text_description);
        }
    }


}
