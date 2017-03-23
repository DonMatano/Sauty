package com.matano.sauty.Model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    public SautyUser getSautyUser()
    {
        return sautyUser;
    }

    public interface userFinishedSettingListener
    {
        public void onSuccess(SautyUser user);
        public void onFailed();
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
                          //  Map<String, Object> map;
                            sautyUser =  dataSnapshot.getValue(SautyUser.class);

                            //sautyUser = (SautyUser) map.get(firebaseUser.getUid());
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

}
