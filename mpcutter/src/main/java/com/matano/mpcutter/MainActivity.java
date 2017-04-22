package com.matano.mpcutter;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.matano.mpcutter.soundfile.SoundFile;

import java.io.File;
import java.io.IOException;

import io.apptik.widget.MultiSlider;

public class MainActivity extends AppCompatActivity
{
    Button selectAudioButton;
    Button trimAudioButton;
    Button playButton;
    boolean isPlaying;
    MultiSlider multiSlider;
    SoundFile soundFile;
    SamplePlayer samplePlayer;
    boolean isPaused;
    MultiSlider.Thumb playThumb;
    Handler handler;
    final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectAudioButton =(Button) findViewById(R.id.ChooseAudioButton);

        selectAudioButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //startSongListChooserActivity();
                Intent intent_upload = new Intent();
                intent_upload.setType("audio/*");
                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent_upload,1);
            }
        });

        isPlaying = false;
        isPaused = false;



        playButton = (Button) findViewById(R.id.playButton);
//        playButton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                if (!isPlaying)
//                {
//                    if (isPaused)
//                    {
//                        handler.notify();
//                        isPaused = false;
//                    }
//                    moveSeeker();
//                    playButton.setText("Pause");
//                    isPlaying = true;
//                }
//                else
//                {
//                    try
//                    {
//                        handler.wait();
//                        isPlaying = false;
//                        isPaused = true;
//
//                    }
//                    catch (InterruptedException e)
//                    {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        handler = new Handler();

        trimAudioButton = (Button) findViewById(R.id.trimAudioButton);


        multiSlider = (MultiSlider) findViewById(R.id.mySlider);

        initSlider();

        askPermission();
    }

    private void initSlider()
    {
        multiSlider.setMax(100);
        multiSlider.clearThumbs();
        multiSlider.setDrawThumbsApart(false);
        final MultiSlider.Thumb thumb1 = multiSlider.addThumb(0);
        final MultiSlider.Thumb thumb2 = multiSlider.addThumb(15).setMax(15);
        playThumb = multiSlider.addThumb(0);


        multiSlider.setOnThumbValueChangeListener(new MultiSlider.OnThumbValueChangeListener()
        {
            @Override
            public void onValueChanged(MultiSlider multiSlider, MultiSlider.Thumb thumb, int thumbIndex, int value)
            {
                //Todo set the second thumb to have to move well when slid forward
                if (thumb.equals(thumb1))
                {
                   // if (thumb2.getValue() <= (multiSlider.getMax() - 15))
                    //{
                        thumb2.setMax(thumb.getValue() + 15);
                    //}
//                    if (thumb2.getValue() > (multiSlider.getMax() - 15))
//                    {
//                        thumb2.setMax(multiSlider.getMax());
//                    }
                }

//                if (thumb.equals(thumb2))
//                {
//                    if(value == thumb2.getMax() && value < multiSlider.getMax())
//                    {
//                        thumb1.setValue(thumb1.getValue() + 1);
//                        thumb.setValue(value + 1);
//                    }
//                }
                Log.d(TAG, "ThumbIndex: " + thumbIndex + ", ThumbValue: " + value);
            }
        });

    }

    void moveSeeker()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (!isPlaying)
                {
                    while (multiSlider.getMax() > playThumb.getValue())
                    {
                        playThumb.setValue(playThumb.getValue() + 1);
                    }
                    handler.postDelayed(this, 1000);

                    playButton.setText("Pause");

                }
            }

        });

        isPlaying = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case 1:
            {
                //If request is cancelled  the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    return;
                }
                else {
                    finishAffinity();
                }
            }
        }
    }

    void setSoundFile(Uri uri)
    {
        //Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        //        song.getId());

        Log.d(TAG, uri.toString());

        String path = null;
         // SDK >= 11 && SDK < 19
         if (Build.VERSION.SDK_INT < 19)
         {
             path = RealPathUtils.getRealPathFromURI_API11to18(MainActivity.this, uri);
         }
            // SDK > 19 (Android 4.4)
        else
         {
             path = RealPathUtils.getRealPathFromURI_API19(MainActivity.this, uri);
         }
        Log.d(TAG, "File Path: " + path);
        // Get the file instance
        File file = new File(path);
        Log.d(TAG, "File Absolute Path:    " + file.getPath());
        Log.d(TAG, "File Absolute Path:    " + file.getAbsolutePath());

        SoundFile.ProgressListener listener = new SoundFile.ProgressListener()
        {
            @Override
            public boolean reportProgress(double fractionComplete)
            {
                return false;
            }
        };

        try
        {
            soundFile = SoundFile.create(file.getAbsolutePath(), listener);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (SoundFile.InvalidInputException e)
        {
            e.printStackTrace();
            Log.d(TAG, "Invalid file");
        }

        if (soundFile != null)
        {
            Log.d(TAG, "soundFile AvgBitrate: " + soundFile.getAvgBitrateKbps());
            Log.d(TAG, "soundFile Samples: " + soundFile.getNumSamples());
        }



    }

    public void askPermission()
    {
        //Check if app has permission to read external storage
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            //Request for permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);


        }
    }

    private void startSongListChooserActivity()
    {
        Intent intent = new Intent(this, SongListChooser.class);
        startActivityForResult(intent, 5);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            if (requestCode == 5)
            {
                Song song = data.getParcelableExtra("song");
                //setSoundFile(song);
            }

            if (requestCode == 1)
            {
                setSoundFile(data.getData());
            }
        }
    }
}
