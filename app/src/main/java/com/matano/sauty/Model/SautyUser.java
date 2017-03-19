package com.matano.sauty.Model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by matano on 18/3/17.
 */

public class SautyUser implements Parcelable
{
    private String userName;
    private Uri userProfilePic;
    private String userEmail;
    private String userUid;
    private int userFollowersCount = 0;
    private int userFollowingCount = 0;
//    private ArrayList<SautyUser> followers;
//    private ArrayList<SautyUser> following;

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

    public int getUserFollowersCount()
    {
        return userFollowersCount;
    }

    public void setUserFollowersCount(int userFollowersCount)
    {
        this.userFollowersCount = userFollowersCount;
    }

    public int getUserFollowingCount()
    {
        return userFollowingCount;
    }

    public void setUserFollowingCount(int userFollowingCount)
    {
        this.userFollowingCount = userFollowingCount;
    }


    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.userName);
        dest.writeParcelable(this.userProfilePic, flags);
        dest.writeString(this.userEmail);
        dest.writeString(this.userUid);
        dest.writeInt(this.userFollowersCount);
        dest.writeInt(this.userFollowingCount);
    }

    protected SautyUser(Parcel in)
    {
        this.userName = in.readString();
        this.userProfilePic = in.readParcelable(Uri.class.getClassLoader());
        this.userEmail = in.readString();
        this.userUid = in.readString();
        this.userFollowersCount = in.readInt();
        this.userFollowingCount = in.readInt();
    }

    public static final Parcelable.Creator<SautyUser> CREATOR = new Parcelable.Creator<SautyUser>()
    {
        @Override
        public SautyUser createFromParcel(Parcel source)
        {
            return new SautyUser(source);
        }

        @Override
        public SautyUser[] newArray(int size)
        {
            return new SautyUser[size];
        }
    };
}
