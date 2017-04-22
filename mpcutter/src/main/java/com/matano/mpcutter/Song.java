package com.matano.mpcutter;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by matano on 31/3/17.
 */


public class Song implements Parcelable
{
    private long id;
    private String title;
    private String artist;

    public Song(long id, String title, String artist)
    {
        this.id = id;
        this.title = title;
        this.artist = artist;
    }

    public long getId()
        {
            return id;
        }

    public String getTitle()
        {
            return title;
        }

    public String getArtist()
        {
            return artist;
        }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeString(this.artist);
    }

    protected Song(Parcel in)
    {
        this.id = in.readLong();
        this.title = in.readString();
        this.artist = in.readString();
    }

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>()
    {
        @Override
        public Song createFromParcel(Parcel source)
        {
            return new Song(source);
        }

        @Override
        public Song[] newArray(int size)
        {
            return new Song[size];
        }
    };
}

