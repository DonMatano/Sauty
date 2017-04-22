package com.matano.mpcutter;

/**
 * Created by matano on 31/3/17.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;


public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder>
{
    private ArrayList<Song> songsArrayList;

    final String TAG = SongsAdapter.class.getSimpleName();
    private RadioButton lastCheckedButton = null;
    private int lastCheckedPosition = 0;
    songClickedListener listener;


    //When Adapter is created we hand it an ArrayList of Songs
    public SongsAdapter(ArrayList<Song> songsArrayList, songClickedListener listener)
    {
        this.songsArrayList = songsArrayList;
        this.listener = listener;
    }


    public interface songClickedListener
    {
        void onSongClicked(int pos);
    }

    @Override
    public SongsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SongsAdapter.ViewHolder holder, int position)
    {

        holder.songTitle.setText(songsArrayList.get(position).getTitle());
        holder.songArtist.setText(songsArrayList.get(position).getArtist());
        // TODO: add radioButton.

        //holder.radioButton.setTag(position);

//        holder.radioButton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                RadioButton raButt = (RadioButton) v;
//                int clickPos = ((Integer)raButt.getTag()).intValue();
//
//                if(raButt.isChecked())
//                {
//                    if(lastCheckedButton != null)
//                    {
//                        lastCheckedButton.setChecked(false);
//                    }
//
//                    lastCheckedButton = raButt;
//                     lastCheckedPosition = clickPos;
//                }
//                else
//                    lastCheckedButton = null;
//            }
//        });



    }



    @Override
    public int getItemCount()
    {
        return songsArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView songTitle;
        TextView songArtist;
        RadioButton radioButton;

        ViewHolder(View itemView)
        {
            super(itemView);


            songTitle = (TextView) itemView.findViewById(R.id.songTitle_textView);
            songArtist = (TextView) itemView.findViewById(R.id.song_Artist_textView);
            radioButton = (RadioButton) itemView.findViewById(R.id.songPickedRadioButton);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    listener.onSongClicked(getAdapterPosition());
                }
            });
        }
    }




}

