package com.matano.sauty.Model;

/**
 * Created by matano on 18/3/17.
 */

public class SautyImage
{
    private String imageUrl;
    private String imageUID;
    private int imageLikes;
    private int imageSaves;
    private int imageShares;
    private String originalPoster;

    public SautyImage()
    {
    }

    public SautyImage(String imageUrl, String originalPoster, String imageUID)
    {
        this.imageUrl = imageUrl;
        this.originalPoster = originalPoster;
        this.imageUID = imageUID;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }



    public String getImageUID()
    {
        return imageUID;
    }

    public void setImageUID(String imageUID)
    {
        this.imageUID = imageUID;
    }



    public int getImageLikes()
    {
        return imageLikes;
    }

    public void setImageLikes(int imageLikes)
    {
        this.imageLikes = imageLikes;
    }

    public int getImageSaves()
    {
        return imageSaves;
    }

    public void setImageSaves(int imageSaves)
    {
        this.imageSaves = imageSaves;
    }

    public int getImageShares()
    {
        return imageShares;
    }

    public void setImageShares(int imageShares)
    {
        this.imageShares = imageShares;
    }

    public String getOriginalPoster()
    {
        return originalPoster;
    }

    public void setOriginalPoster(String originalPoster)
    {
        this.originalPoster = originalPoster;
    }
}
