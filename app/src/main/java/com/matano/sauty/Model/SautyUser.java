package com.matano.sauty.Model;

import android.net.Uri;

/**
 * Created by matano on 18/3/17.
 */

public class SautyUser
{
    private String userName;
    private Uri userProfilePic;
    private String userEmail;
    private String userUid;

    public SautyUser(String userName , String userUid)
    {
        this.userName = userName;
        this.userUid = userUid;
    }

    public SautyUser(String userName, Uri userProfilePic, String userEmail, String userUid)
    {
        this.userName = userName;
        this.userProfilePic = userProfilePic;
        this.userEmail = userEmail;
        this.userUid = userUid;
    }

    public SautyUser(String userName, Uri userProfilePic, String userUid)
    {
        this.userName = userName;
        this.userProfilePic = userProfilePic;
        this.userUid = userUid;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public Uri getUserProfilePic()
    {
        return userProfilePic;
    }

    public void setUserProfilePic(Uri userProfilePic)
    {
        this.userProfilePic = userProfilePic;
    }

    public String getUserEmail()
    {
        return userEmail;
    }

    public void setUserEmail(String userEmail)
    {
        this.userEmail = userEmail;
    }

    public String getUserUid()
    {
        return userUid;
    }

    public void setUserUid(String userUid)
    {
        this.userUid = userUid;
    }
}
