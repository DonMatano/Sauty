package com.matano.sauty.Model;

/**
 * Created by matano on 23/3/17.
 */

public class ImagePost extends Post
{
    public ImagePost(String posterId, String imageUID)
    {
        super();
        setPosterId(posterId);
        setImageUID(imageUID);
    }

    public ImagePost(String posterId, String imageUID, String postDesc)
    {
        super();
        setPosterId(posterId);
        setImageUID(imageUID);
        setPostDesc(postDesc);
    }
}
