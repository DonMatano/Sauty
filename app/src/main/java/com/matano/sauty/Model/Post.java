package com.matano.sauty.Model;

/**
 * Created by matano on 19/3/17.
 */

public abstract class Post
{
    private String postId;
    private int postLikes = 0;
    private int postShares = 0;
    private String posterId;
    private String postDesc;
    private String imageUID;
    private String audioUID;
    private String timeStamp;

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

    public String getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp)
    {
        this.timeStamp = timeStamp;
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
}
