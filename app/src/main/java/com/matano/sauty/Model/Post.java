package com.matano.sauty.Model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by matano on 19/3/17.
 */

public class Post
{
    private String postId;
    private int postLikes = 0;
    private int postShares = 0;
    private String posterId;
    private String postDesc;
    private String imageUID;
    private String audioUID;
    private Long dateCreated;

    public Post()
    {
    }

    public Post(String posterId)
    {
        this.posterId = posterId;
    }

    public Post(String postId, String posterId, String postDesc)
    {
        this.postId = postId;
        this.posterId = posterId;
        this.postDesc = postDesc;
    }

    public Post(String postId, String posterId)
    {
        this.postId = postId;
        this.posterId = posterId;
    }

    public Map<String, String> getDateCreated()
    {
        return ServerValue.TIMESTAMP;
    }


    public String getPostId()
    {
        return postId;
    }

    public void setPostId(String postId)
    {
        this.postId = postId;
    }

    public int getPostLikes()
    {
        return postLikes;
    }

    public void setPostLikes(int postLikes)
    {
        this.postLikes = postLikes;
    }

    public int getPostShares()
    {
        return postShares;
    }

    public void setPostShares(int postShares)
    {
        this.postShares = postShares;
    }

    public String getPosterId()
    {
        return posterId;
    }

    public void setPosterId(String posterId)
    {
        this.posterId = posterId;
    }

    public String getPostDesc()
    {
        return postDesc;
    }

    public void setPostDesc(String postDesc)
    {
        this.postDesc = postDesc;
    }

    public String getImageUID()
    {
        return imageUID;
    }

    public void setImageUID(String imageUID)
    {
        this.imageUID = imageUID;
    }

    public String getAudioUID()
    {
        return audioUID;
    }

    public void setAudioUID(String audioUID)
    {
        this.audioUID = audioUID;
    }

    @Exclude
    public Long getDateCreatedLong()
    {
        return dateCreated;
    }

    public void setDateCreated(Long dateCreated)
    {
        this.dateCreated = dateCreated;
    }

    @Override
    public String toString()
    {
        return getImageUID()+", "+getPostDesc()+", "+getPosterId()+", "+getPosterId();
    }
}
