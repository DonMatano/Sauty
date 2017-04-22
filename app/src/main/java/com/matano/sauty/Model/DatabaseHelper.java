package com.matano.sauty.Model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matano on 22/3/17.
 */

public class DatabaseHelper
{
    private static DatabaseHelper dbHelper;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
    private final String TAG = DatabaseHelper.class.getSimpleName();
    private SautyUser sautyUser;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();






    public static DatabaseHelper getInstance()
    {
        if (dbHelper == null)
        {
            dbHelper = new DatabaseHelper();
        }


        return dbHelper;
    }

    private DatabaseHelper()
    {
    }

    public StorageReference getStorageReference()
    {
        return storageReference;
    }

    public DatabaseReference getRootDatabaseRef()
    {
        return rootDatabaseRef;
    }

    //DatabaseHelper Interfaces

    public interface userFinishedSettingListener
    {
        void onSuccess(SautyUser user);
        void onFailed();
    }

    public interface photoUploadToStorageListener
    {
        void onPhotoUploadSuccess(String downloadUrl);
        void onPhotoUploadFailed();
    }

    public interface imageAddedListener
    {
        void onImageAddedSuccess(String imageUID);
        void onImageAddedFailed();
    }

    public interface postAddedListener
    {
        void onPostAddedSuccess();
        void onPostAddedFailed();
    }

    public interface commentAddedListener
    {
        void onCommentAddedSuccess();
        void onCommentAddedFailed();
    }

    public interface userUpdatedListener
    {
        void onUserUpdatedSuccess();
        void onUserAddedFailed();
    }

    public interface UserGottenListener
    {
        void onUserGotten(SautyUser user);
    }

    public interface ImageGottenListener
    {
        void onImageGotten(SautyImage image);
    }

    public interface OnIsPostLikedListener
    {
        void postLikedByUser();
        void postNotLikedByUser();
    }

    public interface OnIsCommentLikedListener
    {
        void commentLikedByUser();
        void commentNotLikedByUser();
    }

    public interface OnIsUserFollowedListener
    {
        void userFollowedByUser();
        void userNotFollowedByUser();
    }

    public interface UpdatePostLikeCountListener
    {
        void onPostCountUpdatedSuccessfully();
    }

    public interface UpdatePostCommentCountListener
    {
        void onPostCommentCountUpdatedSuccessfully();
    }

    public interface PostGottenListener
    {
        void onPostGotten(Post post);
    }

    public interface FollowUnfollowListener
    {
        void onUserFollowedUnfollowed(String unfollowedFollowedText);
    }




    public void getUser(String userUID, final UserGottenListener listener)
    {
        DatabaseReference userRef = rootDatabaseRef.child("/users/" + userUID);

        userRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                if (dataSnapshot != null)
                {
                    Log.d(TAG, dataSnapshot.getKey());
                    SautyUser user = dataSnapshot.getValue(SautyUser.class);
                    listener.onUserGotten(user);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public void getImage(String imageUID, final ImageGottenListener listener)
    {
        DatabaseReference userRef = rootDatabaseRef.child("/images/" + imageUID);

        userRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                if (dataSnapshot != null)
                {
                    Log.d(TAG, dataSnapshot.getKey());
                    SautyImage image = dataSnapshot.getValue(SautyImage.class);
                    listener.onImageGotten(image);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public void getPost(final String postId, final PostGottenListener listener)
    {
        DatabaseReference postRef = rootDatabaseRef.child("/posts/"+postId);
        postRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                Log.d(TAG, dataSnapshot.toString());
                Post post = dataSnapshot.getValue(Post.class);

                if(post != null)
                {
                    listener.onPostGotten(post);
                    Log.d(TAG, post.toString());
                }
                else
                {
                    Log.d(TAG, "Failed to get Post of ID:  "+ postId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }


    public void addNewUserInDatabase(FirebaseUser firebaseUser, final userFinishedSettingListener listener)
    {
        if (firebaseAuth.getCurrentUser() != null)
        {
            boolean alreadyInitialized = false;
            for (UserInfo profile : firebaseUser.getProviderData())
            {
                if (!alreadyInitialized)
                {
                    // Id of the provider (ex: google.com)
                    //String providerId = profile.getProviderId();

                    // UID specific to the provider
                    //String uid = profile.getUid();

                    // Name, email address, and profile photo Url
                    String name = profile.getDisplayName();
                    String email = profile.getEmail();
                    String photoUrl = String.valueOf(profile.getPhotoUrl());

                    sautyUser = new SautyUser(name, photoUrl, email, firebaseUser.getUid());

                    Map<String , Object> userMap = new HashMap<>();
                    userMap.put("users/" + firebaseUser.getUid(), sautyUser);

                    rootDatabaseRef.updateChildren(userMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        //user successfully added.
                                        listener.onSuccess(sautyUser);
                                        Log.d(TAG,"User successfully added to database");

                                    }

                                    else
                                    {
                                        //user failed
                                        listener.onFailed();
                                        Log.d(TAG, "User addition to database failed", task.getException());
                                    }
                                }
                            });
                    alreadyInitialized = true;
                }
            }
        }
    }

    public void setSautyUser(final FirebaseUser firebaseUser, final userFinishedSettingListener listener)
    {
        if (firebaseUser != null)
        {
            rootDatabaseRef.child("users/"+firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            Log.d(TAG, dataSnapshot.getKey());
                            Log.d(TAG, dataSnapshot.toString());
                            sautyUser =  dataSnapshot.getValue(SautyUser.class);

                            listener.onSuccess(sautyUser);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {
                            listener.onFailed();
                        }
                    });
        }
    }

    public void addImage(String downloadUri, final imageAddedListener listener)
    {
        if (firebaseAuth.getCurrentUser() != null)
        {
            DatabaseReference imageRef = rootDatabaseRef.child("images");

            //Get Unique Key for the Image
            final String imageUid = imageRef.push().getKey();
            SautyImage image = new SautyImage(downloadUri, firebaseAuth.getCurrentUser().getUid()
            , imageUid);

            Map<String , Object> childUpdates = new HashMap<>();
            childUpdates.put("/images/" + imageUid, image);
            childUpdates.put("/usersUploadedImages/" + firebaseAuth.getCurrentUser().getUid()
            + "/" + imageUid, image);

            rootDatabaseRef.updateChildren(childUpdates).addOnCompleteListener(
                    new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                listener.onImageAddedSuccess(imageUid);
                            }
                            else
                            {
                                listener.onImageAddedFailed();
                            }
                        }
                    }
            );

        }
    }

    public void addComment(String commentText, Post post, final commentAddedListener listener)
    {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null)
        {
            Comment comment;

            DatabaseReference commentRef = rootDatabaseRef.child("comments");
            String commentUID = commentRef.push().getKey();

            comment = new Comment(commentUID, commentText, firebaseUser.getUid());
            comment.setInvertedDateCreated(System.currentTimeMillis() * -1);

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/comments/" + commentUID, comment);
            childUpdates.put("/postComments/"+ post.getPostId() + "/" + commentUID,  comment);

            rootDatabaseRef.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        Log.d(TAG, "Successfully Added Comment");
                        listener.onCommentAddedSuccess();
                    }
                    else
                    {
                        Log.d(TAG, "Failed to add Comment");
                        listener.onCommentAddedFailed();
                    }
                }
            });
        }
    }

    public void addNewPost(String imageUID, String descText, final postAddedListener listener)
    {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
        {
            final Post post;

            DatabaseReference postRef = rootDatabaseRef.child("posts");
            final String postUID = postRef.push().getKey();

            if (!TextUtils.isEmpty(descText))
            {
                //Post has a description
                 post = new Post(postUID, firebaseUser.getUid(),
                         descText);
            }
            else
            {
                //post Doesn't have a description;
                 post = new Post(postUID, firebaseUser.getUid());
            }

            post.setImageUID(imageUID);
            post.setInvertedDateCreated(System.currentTimeMillis() * -1);

            Map<String , Object> childUpdates = new HashMap<>();

            childUpdates.put("/posts/" + postUID , post);
            childUpdates.put("/userPosts/" + firebaseUser.getUid() +"/" + postUID, post);
            childUpdates.put("/usersWalls/" + firebaseUser.getUid() + "/" + postUID, post);

            rootDatabaseRef.updateChildren(childUpdates).addOnCompleteListener(
                    new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                listener.onPostAddedSuccess();
                                updateFollowerWall(post);
                            }
                            else
                            {
                                Log.d(TAG, "Failed to add new Post", task.getException());
                            }
                        }
                    }
            );
        }

    }

    public void updateUser(SautyUser user, final userUpdatedListener listener)
    {
        DatabaseReference userRef = rootDatabaseRef.child("/users/"+ user.getUserUid());
        userRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    listener.onUserUpdatedSuccess();
                }

            }
        });
    }


    public void uploadProfilePhoto(Uri imageUri, String type, final photoUploadToStorageListener listener)
    {
        if (firebaseAuth.getCurrentUser() != null  && type != null)
        {
            StorageReference uploadingImageReferencePoint = storageReference
                    .child(firebaseAuth.getCurrentUser().getUid() +"/profilePic/"+ "userProfile"
                            + "." + type);

            uploadingImageReferencePoint.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                    {

                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            if (taskSnapshot != null)
                            {
                                @SuppressWarnings("VisibleForTests")
                                String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                                listener.onPhotoUploadSuccess(downloadUrl);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            listener.onPhotoUploadFailed();
                        }
                    });
        }

    }


    public void uploadPhoto(Uri imageUri, String type, final photoUploadToStorageListener listener)
    {
        if (firebaseAuth.getCurrentUser() != null  && type != null)
        {
            StorageReference uploadingImageReferencePoint = storageReference
                    .child(firebaseAuth.getCurrentUser().getUid() +"/images/"+ System.currentTimeMillis()
                    + "." + type);

            uploadingImageReferencePoint.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                    {

                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            if (taskSnapshot != null)
                            {
                                @SuppressWarnings("VisibleForTests")
                                String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                                listener.onPhotoUploadSuccess(downloadUrl);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            listener.onPhotoUploadFailed();
                        }
                    });
        }

    }

    //Todo Recurse this method
    public void likeButtonClicked(final Post post, final String toAdd, final UpdatePostLikeCountListener likeCountListener)
    {
       // ArrayList<DatabaseReference> listOfDatabaseRef
        DatabaseReference postLikeRef = rootDatabaseRef.child("posts/"+ post.getPostId());

        postLikeRef.runTransaction(new Transaction.Handler()
        {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData)
            {
                Post post = mutableData.getValue(Post.class);
                if (post == null)
                {
                    return Transaction.success(mutableData);
                }

                if (toAdd.trim().equals("like"))
                {
                    post.setPostLikes(post.getPostLikes() + 1);
                    post.setInvertedLikes(post.getInvertedLikes() - 1);

                }

                if (toAdd.trim().equals("unlike"))
                {
                    post.setPostLikes(post.getPostLikes() -1);
                    post.setInvertedLikes(post.getInvertedLikes() + 1);
                }

                mutableData.setValue(post);
                Log.d(TAG, Transaction.success(mutableData).toString());
                return Transaction.success(mutableData);

            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot)
            {
                // Transaction completed
                likeCountListener.onPostCountUpdatedSuccessfully();
                Log.d(TAG, "first postTransaction:onComplete:" + databaseError);

                //Start second Transaction
                DatabaseReference postLikesRef = rootDatabaseRef.child("/usersWalls/" +
                        post.getPosterId() + "/" + post.getPostId());

                postLikesRef.runTransaction(new Transaction.Handler()
                {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData)
                    {
                        Post post = mutableData.getValue(Post.class);
                        if (post == null)
                        {
                            return Transaction.success(mutableData);
                        }

                        if (toAdd.trim().equals("like"))
                        {
                            post.setPostLikes(post.getPostLikes() + 1);
                            post.setInvertedLikes(post.getInvertedLikes() - 1);

                        }

                        if (toAdd.trim().equals("unlike"))
                        {
                            post.setPostLikes(post.getPostLikes() -1);
                            post.setInvertedLikes(post.getInvertedLikes() + 1);
                        }

                        mutableData.setValue(post);
                        Log.d(TAG, Transaction.success(mutableData).toString());
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot)
                    {
                        Log.d(TAG, "second postTransaction:onComplete:" + databaseError);

                        //Start third Transaction
                        DatabaseReference postLikesRef = rootDatabaseRef.child("/userPosts/" +
                                post.getPosterId() + "/" + post.getPostId());

                        postLikesRef.runTransaction(new Transaction.Handler()
                        {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData)
                            {
                                Post post = mutableData.getValue(Post.class);
                                if (post == null)
                                {
                                    return Transaction.success(mutableData);
                                }

                                if (toAdd.trim().equals("like"))
                                {
                                    post.setPostLikes(post.getPostLikes() + 1);
                                    post.setInvertedLikes(post.getInvertedLikes() - 1);

                                }

                                if (toAdd.trim().equals("unlike"))
                                {
                                    post.setPostLikes(post.getPostLikes() -1);
                                    post.setInvertedLikes(post.getInvertedLikes() + 1);
                                }

                                mutableData.setValue(post);
                                Log.d(TAG, Transaction.success(mutableData).toString());
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot)
                            {
                                Log.d(TAG, "third postTransaction:onComplete:" + databaseError);
                                getPost(post.getPostId(), new PostGottenListener()
                                {
                                    @Override
                                    public void onPostGotten(Post post)
                                    {
                                        updateFollowerWall(post);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }


//    public void setInvertedLikes(Post post)
//    {
//        HashMap<String, Object> childUpdates = new HashMap<>();
//        post.setInvertedLikes(post.getPostLikes() * -1);;
//        childUpdates.put("/posts/" + post.getPostId() + "/invertedLikes" , post.getInvertedLikes());
//        childUpdates.put("/userPosts/" + post.getPosterId() +"/" + post.getPostId() + "/invertedLikes",
//                post.getInvertedLikes());
//        childUpdates.put("/usersWalls/" + post.getPosterId() + "/" + post.getPostId() + "/invertedLikes",
//                post.getInvertedLikes());
//
//
//        rootDatabaseRef.updateChildren(childUpdates).addOnCompleteListener(
//                new OnCompleteListener<Void>()
//                {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task)
//                    {
//                        if (task.isSuccessful())
//                        {
//                            Log.d(TAG, "Successfully added inverted Likes");
//                        }
//                        else
//                        {
//                            Log.d(TAG, "Failed to add inverted Likes", task.getException());
//                        }
//                    }
//                }
//        );
//    }

    //Todo Recurse this method
    public void likeButtonClicked(final Comment comment, final Post post, final String toAdd,
                                  final UpdatePostLikeCountListener likeCountListener)
    {
        // ArrayList<DatabaseReference> listOfDatabaseRef
        DatabaseReference postLikeRef = rootDatabaseRef.child("comments/"+ comment.getCommentId()+"/commentLikes");

        postLikeRef.runTransaction(new Transaction.Handler()
        {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData)
            {
                Integer likes = mutableData.getValue(Integer.class);
                if (likes == null)
                {
                    return Transaction.success(mutableData);
                }

                if (toAdd.trim().equals("like"))
                {
                    likes++;

                }

                if (toAdd.trim().equals("unlike"))
                {
                    likes--;
                }

                mutableData.setValue(likes);
                Log.d(TAG, Transaction.success(mutableData).toString());
                return Transaction.success(mutableData);

            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot)
            {
                // Transaction completed
                likeCountListener.onPostCountUpdatedSuccessfully();
                Log.d(TAG, "first postTransaction:onComplete:" + databaseError);

                //Start Second Transaction
                DatabaseReference postLikesRef = rootDatabaseRef.child("/postComments/"
                        + post.getPostId() + "/" + comment.getCommentId()+ "/commentLikes");

                postLikesRef.runTransaction(new Transaction.Handler()
                {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData)
                    {
                        Integer likes = mutableData.getValue(Integer.class);
                        if (likes == null)
                        {
                            return Transaction.success(mutableData);
                        }

                        if (toAdd.trim().equals("like"))
                        {
                            likes++;

                        }

                        if (toAdd.trim().equals("unlike"))
                        {
                            likes--;
                        }

                        mutableData.setValue(likes);
                        Log.d(TAG, Transaction.success(mutableData).toString());
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot)
                    {
                        Log.d(TAG, "second postTransaction:onComplete:" + databaseError);
                    }
                });
            }
        });
    }

    //Todo Recurse this method
    public void addCommentCount(final Post post, final UpdatePostCommentCountListener commentCountListener)
    {
        // ArrayList<DatabaseReference> listOfDatabaseRef
        DatabaseReference postCommentCountRef = rootDatabaseRef.child("posts/"+ post.getPostId()+"/postCommentCount");

        postCommentCountRef.runTransaction(new Transaction.Handler()
        {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData)
            {
                Integer commentCount = mutableData.getValue(Integer.class);
                if (commentCount == null)
                {
                    return Transaction.success(mutableData);
                }

                commentCount++;

                mutableData.setValue(commentCount);
                Log.d(TAG, Transaction.success(mutableData).toString());
                return Transaction.success(mutableData);

            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot)
            {
                // Transaction completed
                commentCountListener.onPostCommentCountUpdatedSuccessfully();
                Log.d(TAG, "first postTransaction:onComplete:" + databaseError);

                //Start second Transaction
                DatabaseReference postCommentCountRef = rootDatabaseRef.child("/usersWalls/" +
                        firebaseAuth.getCurrentUser().getUid() + "/" + post.getPostId()
                        + "/postCommentCount");

                postCommentCountRef.runTransaction(new Transaction.Handler()
                {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData)
                    {
                        Integer commentCount = mutableData.getValue(Integer.class);
                        if (commentCount == null)
                        {
                            return Transaction.success(mutableData);
                        }

                        commentCount++;

                        mutableData.setValue(commentCount);
                        Log.d(TAG, Transaction.success(mutableData).toString());
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot)
                    {
                        Log.d(TAG, "second postTransaction:onComplete:" + databaseError);

                        //Start third Transaction
                        DatabaseReference postCommentCountRef = rootDatabaseRef.child("/userPosts/" +
                                firebaseAuth.getCurrentUser().getUid() + "/" + post.getPostId()
                                + "/postCommentCount");

                        postCommentCountRef.runTransaction(new Transaction.Handler()
                        {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData)
                            {
                                Integer commentCount = mutableData.getValue(Integer.class);
                                if (commentCount == null)
                                {
                                    return Transaction.success(mutableData);
                                }

                                commentCount++;

                                mutableData.setValue(commentCount);
                                Log.d(TAG, Transaction.success(mutableData).toString());
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot)
                            {
                                Log.d(TAG, "third postTransaction:onComplete:" + databaseError);
                                getPost(post.getPostId(), new PostGottenListener()
                                {
                                    @Override
                                    public void onPostGotten(Post post)
                                    {
                                        updateFollowerWall(post);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void updateFollowerCount(String userId, final String toAddSubtract)
    {
        DatabaseReference followerCountRef = rootDatabaseRef.child("/users/" + userId);

        followerCountRef.runTransaction(new Transaction.Handler()
        {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData)
            {
                SautyUser user = mutableData.getValue(SautyUser.class);
                if (user == null)
                {
                    return Transaction.success(mutableData);
                }

                if (toAddSubtract.equals("add"))
                {
                    user.setUserFollowersCount(user.getUserFollowersCount() + 1);

                    user.setInvertedFollowersCount(user.getInvertedFollowersCount() - 1);
                }
                else if (toAddSubtract.equals("subtract"))
                {
                    user.setUserFollowersCount(user.getUserFollowersCount() - 1);

                    user.setInvertedFollowersCount(user.getInvertedFollowersCount() + 1);
                }

                mutableData.setValue(user);
                return Transaction.success(mutableData);

            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "Follower count updated: " + databaseError);
            }
        });

    }

    private void updateFollowingCount(String userId, final String toAddSubtract)
    {
        DatabaseReference followingCountRef = rootDatabaseRef.child("/users/" + userId);

        followingCountRef.runTransaction(new Transaction.Handler()
        {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData)
            {
                SautyUser user = mutableData.getValue(SautyUser.class);
                if (user == null)
                {
                    return Transaction.success(mutableData);
                }

                if (toAddSubtract.equals("add"))
                {
                    user.setUserFollowingCount(user.getUserFollowingCount() + 1);
                    user.setInvertedFollowingCount(user.getInvertedFollowingCount() - 1);
                }

                else if (toAddSubtract.equals("subtract"))
                {
                    user.setUserFollowingCount(user.getUserFollowingCount() - 1);

                    user.setInvertedFollowingCount(user.getInvertedFollowingCount() + 1);
                }
                mutableData.setValue(user);
                return Transaction.success(mutableData);

            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "Following count added: " + databaseError);
            }
        });

    }

    public void addUserToPostLikedUser(Post post)
    {
        DatabaseReference userLikedPost = rootDatabaseRef.child("usersLikingPost/"+post.getPostId()
        + "/" + sautyUser.getUserUid());

        userLikedPost.setValue(true).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Log.d(TAG, "User added to post's liked users Successfully");
                }
                else
                {
                    Log.d(TAG, "User failed to be added to post's liked users.");
                }
            }
        });
    }

    public void addUserToCommentLikedUser(Comment comment)
    {
        DatabaseReference userLikedComment = rootDatabaseRef.child("usersLikingComment/"+
                comment.getCommentId() + "/" + sautyUser.getUserUid());
        userLikedComment.setValue(true).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                    Log.d(TAG,"User added to Comment's Liked users Successfully");
                else
                    Log.d(TAG, "User failed to be added to comment's liked users");
            }
        });
    }

//    public void removePostFromLikedPost(Post post)
//    {
//        DatabaseReference likedPost = rootDatabaseRef.child("usersLikedPosts/" + firebaseAuth.getCurrentUser()
//                .getUid() + "/" + post.getPostId());
//        likedPost.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>()
//        {
//            @Override
//            public void onComplete(@NonNull Task<Void> task)
//            {
//                if (task.isSuccessful())
//                {
//                    Log.d(TAG, "Post removed from user Liked Posts Successfully");
//                }
//                else
//                {
//                    Log.d(TAG, "Post failed to remove from user liked Posts Successfully");
//                }
//            }
//        });
//    }

    public void removeUserFromPostLikedUser(Post post)
    {
        DatabaseReference userLikedPost = rootDatabaseRef.child("usersLikingPost/" + post.getPostId()
                +"/"+ sautyUser.getUserUid());

        userLikedPost.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>()
    {
        @Override
        public void onComplete(@NonNull Task<Void> task)
        {
            if (task.isSuccessful())
            {
                Log.d(TAG, "Post removed from user Liked Posts Successfully");
            }
            else
            {
                Log.d(TAG, "Post failed to remove from user liked Posts Successfully");
            }
        }
    });
    }

    public void removeUserFromCommentLikedUser(Comment comment)
    {
        DatabaseReference userLikedComment = rootDatabaseRef.child("usersLikingComment/" + comment.getCommentId()
        + "/"+sautyUser.getUserUid());
        userLikedComment.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                    Log.d(TAG, "User Removed from user Liked Post Successfully");
                else
                    Log.d(TAG, "User Failed to be removed from user liked Post");
            }
        });
    }

    public void isPostLiked(Post post , final OnIsPostLikedListener listener)
    {
       if (firebaseAuth != null)
        {
            DatabaseReference userLikedPost = rootDatabaseRef.child("/usersLikingPost/" +
            post.getPostId()+ "/" + firebaseAuth.getCurrentUser().getUid());

            userLikedPost.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.getValue() == null)
                    {
                        listener.postNotLikedByUser();
                    }
                    else
                    {
                        listener.postLikedByUser();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }
    }

    public void isCommentLiked(Comment comment, final OnIsCommentLikedListener listener)
    {
        if (firebaseAuth != null)
        {
            DatabaseReference userLikedComment = rootDatabaseRef.child("/usersLikingComment/" +
            comment.getCommentId() + "/" + firebaseAuth.getCurrentUser().getUid());

            userLikedComment.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.getValue() == null)
                        listener.commentNotLikedByUser();
                    else
                        listener.commentLikedByUser();
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }
    }

    public void isUserFollowed(String userID, final OnIsUserFollowedListener userFollowedListener)
    {
        if (firebaseAuth != null)
        {
            DatabaseReference followerPath = rootDatabaseRef.child("/userFollowing/" + firebaseAuth
                    .getCurrentUser().getUid() + "/" + userID);

            followerPath.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.getValue() == null)
                    {
                        userFollowedListener.userNotFollowedByUser();
                    }
                    else
                    {
                        userFollowedListener.userFollowedByUser();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }
    }

    public void followUser(final String followId, final FollowUnfollowListener followUnfollowListener)
    {
        if (firebaseAuth != null)
        {
            HashMap<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/usersFollowers/" + followId + "/" + firebaseAuth.getCurrentUser().getUid()
            , firebaseAuth.getCurrentUser().getUid());
            childUpdates.put("/userFollowing/" + firebaseAuth.getCurrentUser().getUid()+ "/" + followId, followId);
            rootDatabaseRef.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        followUnfollowListener.onUserFollowedUnfollowed("UNFOLLOW");
                        updateFollowerCount(followId, "add");
                        updateFollowingCount(firebaseAuth.getCurrentUser().getUid(), "add");
                        updateWallWithNewFollowPost(followId);
                    }
                    else
                    {
                        Log.d(TAG, "Follow User failed.");
                    }
                }
            });
        }
    }




    private void updateFollowerWall(final Post post)
    {
        DatabaseReference followersRef = rootDatabaseRef.child("/usersFollowers/" + post.getPosterId());
        followersRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                ArrayList<String> followerIdArrayList;
                if (dataSnapshot!= null)
                {
                    followerIdArrayList = new ArrayList<String>();
                    for (DataSnapshot followerSnap: dataSnapshot.getChildren())
                    {
                        followerIdArrayList.add(followerSnap.getValue(String.class));
                    }

                    HashMap<String, Object> followersIDMap = new HashMap<String, Object>();

                    for (String followerId: followerIdArrayList)
                    {
                        followersIDMap.put("/usersWalls/" + followerId + "/" + post.getPostId(), post);
                    }

                    rootDatabaseRef.updateChildren(followersIDMap).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                Log.d(TAG, "UserWall Successfully Updated");
                            }
                            else
                            {
                                Log.d(TAG, "UserWall Failed to be updated");
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public void unfollowUser(final String unfollowId, final FollowUnfollowListener followUnfollowListener)
    {
        if (firebaseAuth != null)
        {
            HashMap<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/usersFollowers/" + unfollowId + "/" + firebaseAuth.getCurrentUser().getUid()
            , null);
            childUpdates.put("/userFollowing/" + firebaseAuth.getCurrentUser().getUid() + "/" + unfollowId, null);
            rootDatabaseRef.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        followUnfollowListener.onUserFollowedUnfollowed("FOLLOW");
                        updateFollowerCount(unfollowId, "subtract");
                        updateFollowingCount(firebaseAuth.getCurrentUser().getUid(), "subtract");
                        removePostToWall(unfollowId);
                    }
                    else
                    {
                        Log.d(TAG, "Unfollowing User failed.");
                    }
                }
            });
        }
    }

    private void updateWallWithNewFollowPost(String followId)
    {
        DatabaseReference followWallRef = rootDatabaseRef.child("/userPosts/" + followId);

        followWallRef.orderByChild("invertedDateCreated")
                .addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                ArrayList<Post> postArrayString = new ArrayList<Post>();
                if (dataSnapshot != null)
                {
                    postArrayString = new ArrayList<Post>();
                    for (DataSnapshot postSnap : dataSnapshot.getChildren())
                    {

                        postArrayString.add(postSnap.getValue(Post.class));

                    }
                }

                updatePostToWall(postArrayString);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void removePostToWall(final String followID)
    {
        DatabaseReference userWallRef = rootDatabaseRef.child("/usersWalls/" + firebaseAuth.getCurrentUser()
        .getUid());

        userWallRef.orderByChild("posterId").equalTo(followID).addListenerForSingleValueEvent(
                new ValueEventListener()
                {
                    ArrayList<Post> postArrayList = new ArrayList<Post>();
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot != null)
                        {
                            for (DataSnapshot userSnap: dataSnapshot.getChildren())
                            {
                                postArrayList.add(userSnap.getValue(Post.class));
                            }

                            HashMap<String, Object> hashMap = new HashMap<String, Object>();

                            for (Post post : postArrayList)
                            {
                                hashMap.put("/usersWalls/" + firebaseAuth.getCurrentUser().getUid()
                                + "/" + post.getPostId(), null);
                            }

                            rootDatabaseRef.updateChildren(hashMap).addOnCompleteListener(
                                    new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Log.d(TAG, followID + " posts has been removed" +
                                                        "successfully.");
                                            }
                                            else
                                            {
                                                Log.d(TAG, followID + " posts has failed to been " +
                                                        "removed");
                                            }
                                        }
                                    }
                            );
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                }
        );
    }

    private void updatePostToWall(ArrayList<Post> postArrayList)
    {
        HashMap<String, Object> updateMap = new HashMap<>();

        for (Post post : postArrayList)
        {
            updateMap.put("/usersWalls/" + firebaseAuth.getCurrentUser().getUid() +
                    "/" + post.getPostId(), post);
        }

        rootDatabaseRef.updateChildren(updateMap).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Log.d(TAG, "Successfully added following post to user's wall");
                }
                else
                {
                    Log.d(TAG, "Failed to add following post to user's wall");
                }
            }
        });
    }

}

