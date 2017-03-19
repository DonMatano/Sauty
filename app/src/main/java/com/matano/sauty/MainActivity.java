package com.matano.sauty;

import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.matano.sauty.Model.SautyUser;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener
{
    TabLayout tabLayout;
    ViewPager viewPager;
    final int SIGN_IN = 11;
    final static String TAG = MainActivity.class.getSimpleName();
    SautyUser user;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth firebaseAuth;
    private boolean alreadySignIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        initializeAuthStateListener();



    }

    @Override
    protected void onStart()
    {
        Log.d(TAG, "onStart of MainActivity");
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
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
        if (authStateListener != null)
        {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy of MainActivity");
        super.onDestroy();
    }

    private void initializeAuthStateListener()
    {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null )
                {
                    if (!alreadySignIn)
                    {
                        alreadySignIn = true;
                        initializeTabLayout();
                    }
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    showSignInActivity();
                }
                // ...
            }
        };
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
                tabLayout.getTabCount(), this);

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
       firebaseAuth.getInstance().signOut();
    }

    private void showSignInActivity()
    {
        Intent signInActivity = new Intent(this, SignInActivity.class);
        startActivityForResult(signInActivity, SIGN_IN);
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
    }
}
