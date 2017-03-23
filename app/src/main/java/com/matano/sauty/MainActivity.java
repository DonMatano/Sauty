package com.matano.sauty;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.matano.sauty.Model.DatabaseHelper;
import com.matano.sauty.Model.SautyUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener
    , DatabaseHelper.userFinishedSettingListener
{
    TabLayout tabLayout;
    ViewPager viewPager;
    final int SIGN_IN = 11;
    final int FIRE_UI_SIGN_IN = 55;
    final static String TAG = MainActivity.class.getSimpleName();
    SautyUser user;
    private FirebaseAuth firebaseAuth;
//    private boolean alreadySignIn = false;
//    private boolean alreadyInitialized = false;
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



        //initializeAuthStateListener();


    }

    @Override
    protected void onStart()
    {
        Log.d(TAG, "onStart of MainActivity");
        super.onStart();
        //firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG, "onStart of MainActivity");
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        Log.d(TAG, "onPause of MainActivity");
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop of MainActivity");
        super.onStop();

    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy of MainActivity");
//        if (authStateListener != null)
//        {
//            firebaseAuth.removeAuthStateListener(authStateListener);
//        }
        super.onDestroy();
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
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        //Adding the tabs using addTab() method

        tabLayout.addTab(tabLayout.newTab()
                .setText(getText(R.string.post_tab_title)));

        tabLayout.addTab(tabLayout.newTab()
                .setText(getText(R.string.discovery_tab_title)));

        tabLayout.addTab(tabLayout.newTab()
                .setText(getText(R.string.profile_tab_title)));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //Adding onTabSelectedListener to swipe views
        tabLayout.addOnTabSelectedListener(this);

        viewPager = (ViewPager) findViewById(R.id.pager);

        Pager tabPagerAdapter = new Pager(getSupportFragmentManager(),
                tabLayout.getTabCount(), this, user);

        //Adding adapter to pager
        viewPager.setAdapter(tabPagerAdapter);
        viewPager.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(tabLayout)
        );

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab)
    {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab)
    {

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

    private void firebaseAuthWithGoogle(String signInAccount)
    {
        Log.d(TAG, "firebaseAuthWithGoogle: " + signInAccount);

        AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        Log.d(TAG, "signInWithCredential:onComplete: " + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode == SIGN_IN)
        {
            if (resultCode == RESULT_OK)
            {
                firebaseAuthWithGoogle(data.getStringExtra("account"));
            }
            else if (resultCode == RESULT_CANCELED)
            {
                showSignInActivity();
            }
        }

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
