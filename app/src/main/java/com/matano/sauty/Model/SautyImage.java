package com.matano.sauty.Model;

/**
 * Created by matano on 18/3/17.
 */

public class SautyImage
{
    private String imageUrl;
    private String imageName;
    private int imageLikes;
    private int imageSaves;
    private int imageShares;
    private String originalPoster;

    public SautyImage()
    {
    }

    public SautyImage(String imageUrl, String imageName, String originalPoster)
    {
        this.imageUrl = imageUrl;
        this.imageName = imageName;
        this.originalPoster = originalPoster;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public String getImageName()
    {
        return imageName;
    }

    public void setImageName(String imageName)
    {
        this.imageName = imageName;
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
