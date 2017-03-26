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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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

    public interface UserGottenListener
    {
        void onUserGotten(SautyUser user);
    }

    public interface ImageGottenListener
    {
        void onImageGotten(SautyImage image);
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

    public void getPosts()
    {
        DatabaseReference postRef = rootDatabaseRef.child("/posts/");
        postRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Log.d(TAG, snapshot.toString());

                    Post post = snapshot.getValue(Post.class);
                    Log.d(TAG, post.toString());
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
                    userMap.put(firebaseUser.getUid(), sautyUser);

                    rootDatabaseRef.child("users").setValue(userMap)
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
            + "/" + imageUid, true);

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

    public void addNewImagePost(String imageUID, String descText, final postAddedListener listener)
    {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
        {
            Post post;

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

            Map<String , Object> childUpdates = new HashMap<>();

            childUpdates.put("/posts/" + postUID , post);
            childUpdates.put("/userPosts/" + firebaseUser.getUid() +"/" + postUID, true);
            childUpdates.put("/usersWalls/" + firebaseUser.getUid() + "/" + postUID, true);

            rootDatabaseRef.updateChildren(childUpdates).addOnCompleteListener(
                    new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                listener.onPostAddedSuccess();
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



}

