package com.matano.sauty.Model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.Map;

/**
 * Created by matano on 16/4/17.
 */

public class Comment
{
    private String commentId;
    private int commentLikes = 0;
    private String commenterId;
    private String commentText;
    private Long dateCreated;
    private Long invertedDateCreated;

    public Comment()
    {
    }

    public Comment(String commentId, String commentText, String commenterId)
    {
        this.commentId = commentId;
        this.commentText = commentText;
        this.commenterId = commenterId;
    }

    public Map<String, String> getDateCreated()
    {
        return ServerValue.TIMESTAMP;
    }

    public String getCommentId()
    {
        return commentId;
    }

    public void setCommentId(String commentId)
    {
        this.commentId = commentId;
    }

    public int getCommentLikes()
    {
        return commentLikes;
    }

    public void setCommentLikes(int commentLikes)
    {
        this.commentLikes = commentLikes;
    }

    public String getCommenterId()
    {
        return commenterId;
    }

    public void setCommenterId(String commenterId)
    {
        this.commenterId = commenterId;
    }

    public String getCommentText()
    {
        return commentText;
    }

    public void setCommentText(String commentText)
    {
        this.commentText = commentText;
    }

    public Long getInvertedDateCreated()
    {
        return invertedDateCreated;
    }

    public void setInvertedDateCreated(Long invertedDateCreated)
    {
        this.invertedDateCreated = invertedDateCreated;
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
}
