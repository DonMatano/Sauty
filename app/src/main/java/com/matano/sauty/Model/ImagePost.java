package com.matano.sauty.Model;

/**
 * Created by matano on 23/3/17.
 */

public class ImagePost extends Post
{
    public ImagePost(String posterId, String imageUID, String timeStamp)
    {
        super();
        setPosterId(posterId);
        setImageUID(imageUID);
        setTimeStamp(timeStamp);
    }

    public ImagePost(String posterId, String imageUID, String postDesc, String timestamp)
    {
        super();
        setPosterId(posterId);
        setImageUID(imageUID);
        setPostDesc(postDesc);
        setTimeStamp(timestamp);
    }
}
