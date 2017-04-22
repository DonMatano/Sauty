package com.matano.mpcutter;

import com.matano.mpcutter.soundfile.SoundFile;

import java.nio.ShortBuffer;

/**
 * Created by matano on 3/4/17.
 */

public class MySamplePlayer extends SamplePlayer
{
    public MySamplePlayer(ShortBuffer samples, int sampleRate, int channels, int numSamples)
    {
        super(samples, sampleRate, channels, numSamples);
    }

    public MySamplePlayer(SoundFile sf)
    {
        super(sf);
    }
}
