package com.matano.sauty;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.*;
import com.firebase.ui.auth.BuildConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.matano.sauty.Model.DatabaseHelper;
import com.matano.sauty.Model.SautyUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements
     DatabaseHelper.userFinishedSettingListener, FeedFragment.FabButtonClickedListenter
{

    final int FIRE_UI_SIGN_IN = 55;
    final static String TAG = MainActivity.class.getSimpleName();
    SautyUser user;
    private FirebaseAuth firebaseAuth;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseHelper = DatabaseHelper.getInstance();

        if (firebaseAuth.getCurrentUser() != null)
        {
            //Already signed in
            initializeSautyUser(firebaseAuth.getCurrentUser());
        }
        else
        {
            showSignInActivity();
        }
    }

    private void initializeSautyUser(final FirebaseUser firebaseUser)
    {
        if (firebaseUser != null)
        {
            DatabaseReference userRef = databaseHelper.getRootDatabaseRef().child("users");
            userRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.getValue() !=  null)
                    {
                        //userExists.
                      databaseHelper.setSautyUser(firebaseUser, MainActivity.this);
                    }
                    else
                    {
                        //user doesn't exist. Create new user in database
                        databaseHelper.addNewUserInDatabase(firebaseUser, MainActivity.this);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }
    }

    public void initializeTabLayout()
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.activity_frameLayout, TabFragments.newInstance(user));
        transaction.commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.signOutMenuItem:
            {
                signOut();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut()
    {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        //user is now signed out
                        showSignInActivity();
                    }
                });

    }

    private void showSignInActivity()
    {
        //Intent signInActivity = new Intent(this, SignInActivity.class);
        //startActivityForResult(signInActivity, SIGN_IN);
        startActivityForResult(
                AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                .build(), FIRE_UI_SIGN_IN
        );
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        //FIRE_UI_SIGN is the request code you passed
        if (requestCode == FIRE_UI_SIGN_IN)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            //successfully signed in
            if (resultCode == ResultCodes.OK)
            {
                initializeSautyUser(firebaseAuth.getCurrentUser());
                return;
            }
            else
            {
                //sign in failed
                if (response == null)
                {
                    //User pressed back button
                    Toast.makeText(this, "sign_in_cancelled", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK)
                {
                    Toast.makeText(this, "No internet Connection", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR)
                {
                    Toast.makeText(this, "Unknown Error", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Toast.makeText(this, "Unknown Sign In response", Toast.LENGTH_SHORT).show();
        }
    }

    //FeedFragment Listener

    @Override
    public void onAddPostFabButtonClicked()
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_frameLayout, new AddPostFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }


    //DatabaseHelper Listener
    
    //userSetListeners
    @Override
    public void onSuccess(SautyUser user)
    {
        this.user = user;
        initializeTabLayout();
    }

    @Override
    public void onFailed()
    {
        Toast.makeText(this, "ERROR!...Setting User failed....", Toast.LENGTH_SHORT).show();
    }
}
