package com.matano.mpcutter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by matano on 31/3/17.
 */

public class SongListChooser extends AppCompatActivity
{
    RecyclerView songListRecyclerView;
    RecyclerView.LayoutManager songListRecyclerViewLayoutManager;
    SongsAdapter songsAdapter;
    ArrayList<Song> songsArrayList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_list_layout);

        songListRecyclerView = (RecyclerView) findViewById(R.id.songlist_recyclerview);

        songListRecyclerViewLayoutManager = new LinearLayoutManager(this);
        songListRecyclerView.setLayoutManager(songListRecyclerViewLayoutManager);

        songsArrayList = new ArrayList<>();

        getSongList();

        Collections.sort(songsArrayList, new Comparator<Song>()
        {
            @Override
            public int compare(Song o1, Song o2)
            {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });

        songsAdapter = new SongsAdapter(songsArrayList, new SongsAdapter.songClickedListener()
        {
            @Override
            public void onSongClicked(int pos)
            {
                setResult(RESULT_OK, new Intent().putExtra("song", songsArrayList.get(pos)));
                finish();
            }
        });
        songListRecyclerView.setAdapter(songsAdapter);


    }

    public void getSongList()
    {
        ContentResolver contentResolver = this.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);


        if (cursor == null)
        {
            //query failed
            Toast.makeText(this, "Failed to read storage", Toast.LENGTH_SHORT).show();
        }
        else if (!cursor.moveToFirst())
        {
            // no media found
            Toast.makeText(this, "No audio found on the device", Toast.LENGTH_SHORT).show();
            cursor.close();
        }
        else
        {
            //get columns
            int titleColumn = cursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int idColumn = cursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);

            do
            {
                long thisId = cursor.getLong(idColumn);
                String thisTitle = cursor.getString(titleColumn);
                String thisArtist = cursor.getString(artistColumn);

                songsArrayList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (cursor.moveToNext());
            cursor.close();
        }


    }
}
