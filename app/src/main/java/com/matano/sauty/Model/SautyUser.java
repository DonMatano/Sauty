package com.matano.sauty.Model;

import android.app.Application;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.matano.sauty.MainActivity;
import com.matano.sauty.R;

import java.util.ArrayList;

/**
 * Created by matano on 18/3/17.
 */

public class SautyUser implements Parcelable
{
    private String userName;
    private String userProfilePic;
    private String userEmail;
    private String userUid;
    private int userFollowersCount = 0;
    private int userFollowingCount = 0;
    private int age;
    private String userStatus;
    private String userLikedPostsUID;
    private String userSharedPostsUID;

    

    public SautyUser()
    {
    }

    public SautyUser(String userName , String userUid)
    {
        this.userName = userName;
        this.userUid = userUid;
    }

    public SautyUser(String userName, String userProfilePic, String userEmail, String userUid)
    {
        this.userName = userName;
        this.userProfilePic = userProfilePic;
        this.userEmail = userEmail;
        this.userUid = userUid;
    }

    public SautyUser(String userName, String userProfilePic, String userUid)
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

    public String getUserProfilePic()
    {
        return userProfilePic;
    }

    public void setUserProfilePic(String userProfilePic)
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

    public String getUserLikedPostsUID()
    {
        return userLikedPostsUID;
    }

    public void setUserLikedPostsUID(String userLikedPostsUID)
    {
        this.userLikedPostsUID = userLikedPostsUID;
    }

    public String getUserSharedPostsUID()
    {
        return userSharedPostsUID;
    }

    public void setUserSharedPostsUID(String userSharedPostsUID)
    {
        this.userSharedPostsUID = userSharedPostsUID;
    }

    public int getAge()
    {
        return age;
    }

    public void setAge(int age)
    {
        this.age = age;
    }

    public String getUserStatus()
    {
        return userStatus;
    }

    public void setUserStatus(String userStatus)
    {
        this.userStatus = userStatus;
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
        dest.writeString(this.userProfilePic);
        dest.writeString(this.userEmail);
        dest.writeString(this.userUid);
        dest.writeInt(this.userFollowersCount);
        dest.writeInt(this.userFollowingCount);
        dest.writeInt(this.age);
        dest.writeString(this.userStatus);
        dest.writeString(this.userLikedPostsUID);
        dest.writeString(this.userSharedPostsUID);
    }

    protected SautyUser(Parcel in)
    {
        this.userName = in.readString();
        this.userProfilePic = in.readString();
        this.userEmail = in.readString();
        this.userUid = in.readString();
        this.userFollowersCount = in.readInt();
        this.userFollowingCount = in.readInt();
        this.age = in.readInt();
        this.userStatus = in.readString();
        this.userLikedPostsUID = in.readString();
        this.userSharedPostsUID = in.readString();
    }

    public static final Creator<SautyUser> CREATOR = new Creator<SautyUser>()
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
