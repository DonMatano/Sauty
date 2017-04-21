package com.matano.sauty;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.matano.sauty.Model.Comment;
import com.matano.sauty.Model.DatabaseHelper;
import com.matano.sauty.Model.Post;
import com.matano.sauty.Model.SautyImage;
import com.matano.sauty.Model.SautyUser;

/**
 * Created by matano on 16/4/17.
 */

public class FullPostFragment extends Fragment
    implements DatabaseHelper.PostGottenListener
{
    DatabaseHelper databaseHelper;
    FirebaseAuth firebaseAuth;
    Post post;
    final static String TAG = FullPostFragment.class.getSimpleName();

    ImageView posterProfilePic;
    TextView  posterUserName;
    ImageView postImage;
    ImageButton likeImageButton;
    ImageButton shareImageButton;
    TextView likeCountTextView;
    TextView shareCountTextView;
    TextView commentsCountTextView;
    EditText commentEditText;
    Button postCommentButton;
    TextView postDescTextView;
    RecyclerView commentRecyclerView;
    FirebaseRecyclerAdapter<Comment, CommentHolder> commentAdapter;

    FeedFragment.UserProfileClickedListener userProfileClickedListener;

    public static FullPostFragment newInstance(String postId)
    {

        Bundle args = new Bundle();
        args.putString("postId", postId);

        FullPostFragment fragment = new FullPostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try
        {
            userProfileClickedListener = (FeedFragment.UserProfileClickedListener) context;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString() +
                    " must implement userProfileListener");
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
        View v = inflater.inflate(R.layout.full_post_view, container, false);
        posterProfilePic = (ImageView) v.findViewById(R.id.fullPostposterProfilePic);
        posterUserName = (TextView) v.findViewById(R.id.posterProfileName);
        postImage = (ImageView) v.findViewById(R.id.postfeedImageView);
        likeImageButton = (ImageButton) v.findViewById(R.id.feedlikeImageButton);
        shareImageButton = (ImageButton) v.findViewById(R.id.postfeedshareImageButton);
        likeCountTextView = (TextView) v.findViewById(R.id.likesCounttextView);
        commentsCountTextView = (TextView) v.findViewById(R.id.commentTextView);
        commentEditText = (EditText) v.findViewById(R.id.userCommentEditText);
        postCommentButton = (Button) v.findViewById(R.id.commentButton);
        postDescTextView = (TextView) v.findViewById(R.id.post_feed_text_description);
        commentRecyclerView = (RecyclerView) v.findViewById(R.id.commentsRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        commentRecyclerView.setLayoutManager(layoutManager);

        databaseHelper.getPost(getArguments().getString("postId"), this);

        return v;

    }

    //DatabaseHelper PostGotten Listener
    @Override
    public void onPostGotten(Post post)
    {
        this.post = post;
        initLayout();
    }

    private void initLayout()
    {
        databaseHelper.getUser(post.getPosterId(), new DatabaseHelper.UserGottenListener()
        {
            @Override
            public void onUserGotten(SautyUser user)
            {
                Glide.with(getActivity())
                        .load(user.getUserProfilePic())
                        .placeholder(R.drawable.com_facebook_profile_picture_blank_square)
                        .thumbnail(0.5f)
                        .crossFade(5)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(posterProfilePic);

                posterUserName.setText(user.getUserName());
            }
        });

        if (post.getImageUID() != null)
        {
            databaseHelper.getImage(post.getImageUID(), new DatabaseHelper.ImageGottenListener()
            {
                @Override
                public void onImageGotten(SautyImage image)
                {
                    StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(image.getImageUrl());
                    Glide.with(getContext())
                            .using(new FirebaseImageLoader())
                            .load(ref)
                            .placeholder(R.drawable.image_placeholder)
                            .crossFade(5)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .priority(Priority.HIGH)
                            .into(postImage);
                }
            });
        }
        else
        {
            postImage.setVisibility(View.GONE);
        }

        databaseHelper.isPostLiked(post, new DatabaseHelper.OnIsPostLikedListener()
        {
            @Override
            public void postLikedByUser()
            {
                likeImageButton.setImageResource(R.drawable.ic_thumb_up_clicked);
                likeImageButton.setActivated(true);
            }

            @Override
            public void postNotLikedByUser()
            {
                likeImageButton.setImageResource(R.drawable.ic_thumb_up_unclicked);
                likeImageButton.setActivated(false);
            }
        });

                likeImageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //If Activated the post Is Liked so Unlike it else add it to count.
                if (!likeImageButton.isActivated())
                {
                    //Like Image and add to Liked Post.
                    likeImageButton.setImageResource(R.drawable.ic_thumb_up_clicked);
                    likeImageButton.setActivated(true);
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
                    likeImageButton.setImageResource(R.drawable.ic_thumb_up_unclicked);
                    likeImageButton.setActivated(false);
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

        likeCountTextView.setText(String.valueOf(post.getPostLikes()));

        postDescTextView.setText(post.getPostDesc());

        posterProfilePic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                userProfileClickedListener.onUserProfileClicked(post.getPosterId());
            }
        });

        postImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                userProfileClickedListener.onUserProfileClicked(post.getPosterId());
            }
        });

        postCommentButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!TextUtils.isEmpty(commentEditText.getText().toString().trim()));
                {
                    addComment(commentEditText.getText().toString().trim());
                }
            }
        });



        initRecyclerView();

        commentsCountTextView.setText(String.valueOf(post.getPostCommentCount()));

    }

    private void addComment(String comment)
    {
        databaseHelper.addComment(comment, post, new DatabaseHelper.commentAddedListener()
        {
            @Override
            public void onCommentAddedSuccess()
            {
                Toast.makeText(getContext(), "Comment Added.", Toast.LENGTH_SHORT).show();
                databaseHelper.addCommentCount(post, new DatabaseHelper.UpdatePostCommentCountListener()
                {
                    @Override
                    public void onPostCommentCountUpdatedSuccessfully()
                    {
                        Log.d(TAG, "Successfully Added Comment");
                    }
                });
                commentEditText.setText("");
            }

            @Override
            public void onCommentAddedFailed()
            {
                Toast.makeText(getContext(), "Comment Failed To be Added", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initRecyclerView()
    {
        if (firebaseAuth.getCurrentUser() != null)
        {
            Query keyRef = databaseHelper.getRootDatabaseRef().child(
                    "/postComments/"+post.getPostId());

            commentAdapter = new FirebaseRecyclerAdapter<Comment, CommentHolder>(
                    Comment.class, R.layout.comment_view, CommentHolder.class,
                    keyRef)
            {

                //Don't even Ask what the fuck is happening below.

                @Override
                protected void populateViewHolder(final CommentHolder commentHolder, final Comment comment, int position)
                {
                    databaseHelper.isCommentLiked(comment, new DatabaseHelper.OnIsCommentLikedListener()
                    {
                        @Override
                        public void commentLikedByUser()
                        {
                            commentHolder.likeImageButton.setImageResource(R.drawable.ic_thumb_up_clicked);
                            commentHolder.likeImageButton.setActivated(true);
                        }

                        @Override
                        public void commentNotLikedByUser()
                        {
                            commentHolder.likeImageButton.setImageResource(R.drawable.ic_thumb_up_unclicked);
                            commentHolder.likeImageButton.setActivated(false);
                        }
                    });
                    //Adding a listener to know when a User has been gotten in the database.

                    DatabaseHelper.UserGottenListener userGottenListener = new DatabaseHelper.UserGottenListener()
                    {
                        //Called when User is gotten
                        @Override
                        public void onUserGotten(final SautyUser user)
                        {
                            commentHolder.commentorName.setText(user.getUserName());
                        }
                    };
                    //getting the user
                    databaseHelper.getUser(comment.getCommenterId(), userGottenListener);

                    commentHolder.likeImageButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            //If Activated the post Is Liked so Unlike it else add it to count.
                            if (!commentHolder.likeImageButton.isActivated())
                            {
                                //Like Image and add to Liked Post.
                                commentHolder.likeImageButton.setImageResource(R.drawable.ic_thumb_up_clicked);
                                commentHolder.likeImageButton.setActivated(true);
                                databaseHelper.likeButtonClicked(comment, post, "like", new DatabaseHelper.UpdatePostLikeCountListener()
                                {
                                    @Override
                                    public void onPostCountUpdatedSuccessfully()
                                    {
                                        databaseHelper.addUserToCommentLikedUser(comment);
                                    }
                                });
                            }
                            else
                            {
                                commentHolder.likeImageButton.setImageResource(R.drawable.ic_thumb_up_unclicked);
                                commentHolder.likeImageButton.setActivated(false);
                                databaseHelper.likeButtonClicked(comment, post, "unlike", new DatabaseHelper.UpdatePostLikeCountListener()
                                {
                                    @Override
                                    public void onPostCountUpdatedSuccessfully()
                                    {
                                        databaseHelper.removeUserFromCommentLikedUser(comment);
                                    }
                                });

                            }
                        }
                    });

                    commentHolder.commentorName.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            userProfileClickedListener.onUserProfileClicked(post.getPosterId());
                        }
                    });


                    commentHolder.commentTextDesc.setText(comment.getCommentText());
                    commentHolder.commentLikesCount.setText(String.valueOf(comment.getCommentLikes()));

                }
            };

            commentRecyclerView.setAdapter(commentAdapter);

        }
    }

    private static class CommentHolder extends RecyclerView.ViewHolder
    {
        TextView commentorName;
        TextView commentTextDesc;
        ImageButton likeImageButton;
        TextView commentLikesCount;
        public CommentHolder(View itemView)
        {
            super(itemView);
            commentorName = (TextView)itemView.findViewById(R.id.commentViewCommentorNameTextView);
            commentTextDesc = (TextView) itemView.findViewById(R.id.commentOrCommentTextView);
            likeImageButton = (ImageButton) itemView.findViewById(R.id.likeButton);
            commentLikesCount = (TextView) itemView.findViewById(R.id.likeCounts);
        }
    }
}
