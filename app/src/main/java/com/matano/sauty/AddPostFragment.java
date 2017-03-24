package com.matano.sauty;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by matano on 23/3/17.
 */

public class AddPostFragment extends Fragment implements View.OnClickListener
{
    private ImageView addPhotoImageView;
    private EditText descEditText;
    private Button addPostButton;
    private final int PICK_IMAGE_REQUEST = 6;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

    }

    public static AddPostFragment newInstance()
    {

        Bundle args = new Bundle();

        AddPostFragment fragment = new AddPostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.add_post_fragment, container, false);
        addPhotoImageView = (ImageView) v.findViewById(R.id.addPhotoImageView);
        addPhotoImageView.setOnClickListener(this);
        descEditText = (EditText) v.findViewById(R.id.descEditText);
        addPostButton = (Button) v.findViewById(R.id.post_OK_button);


        return v;
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.addPhotoImageView)
        {
            getPhotoFromPhone();
        }

        if (v.getId() == R.id.post_OK_button)
        {
            addPost();
        }
    }

    private void addPost()
    {

    }

    private void getPhotoFromPhone()
    {
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null
                && data.getData() != null) {

            addPhotoImageView.setImageDrawable(null);
            Uri imageUri = data.getData();

            Glide.with(getContext()).load(imageUri)
                    .crossFade()
                    .into(addPhotoImageView);
        }
    }
}
