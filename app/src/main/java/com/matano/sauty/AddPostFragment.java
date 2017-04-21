package com.matano.sauty;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.matano.sauty.Model.DatabaseHelper;

/**
 * Created by matano on 23/3/17.
 */

public class AddPostFragment extends Fragment implements View.OnClickListener,
        DatabaseHelper.photoUploadToStorageListener, DatabaseHelper.imageAddedListener,
        DatabaseHelper.postAddedListener
{
    private ImageView addPhotoImageView;
    private EditText descEditText;
    ProgressDialog progressDialog;
    private Button addPostButton;
    private final int PICK_IMAGE_REQUEST = 6;
    Uri imageUri;
    DatabaseHelper databaseHelper;
    onPostAddedListener onPostAddedListener;
    static final String TAG = AddPostFragment.class.getSimpleName();

    public interface onPostAddedListener
    {
        void postAddedSuccessfully();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        databaseHelper = DatabaseHelper.getInstance();

        try
        {
            onPostAddedListener = (onPostAddedListener) context;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString() +
                    " must implement OnArticleSelectedListener");
        }

    }

    public static AddPostFragment newInstance()
    {

        Bundle args = new Bundle();

        AddPostFragment fragment = new AddPostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        imageUri = null;
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

        addPostButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addPost();
            }
        });


        return v;
    }

    @Override
    public void onClick(View v)
    {
        if (v == addPhotoImageView)
        {
            getPhotoFromPhone();
        }
    }

     void addPost()
    {
        progressDialog = new ProgressDialog(getContext());

        //Check if post has image
        if (imageUri != null)
        {
            progressDialog.setTitle(getString(R.string.uploading_photo));
            progressDialog.show();
            ContentResolver cR = getContext().getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = mime.getExtensionFromMimeType(cR.getType(imageUri));
            databaseHelper.uploadPhoto(imageUri , type, this);

        }
        else
        {
            //Post has no image
            progressDialog.setTitle(getString(R.string.uploading__post));
            progressDialog.show();
            databaseHelper.addNewPost(null, descEditText.getText().toString().trim(), this);

        }

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
            imageUri = data.getData();

            Glide.with(getContext()).load(imageUri)
                    .crossFade()
                    .into(addPhotoImageView);
        }
    }

    //Photo upload listeners
    @Override
    public void onPhotoUploadSuccess(String downloadUri)
    {
        progressDialog.setTitle("Adding Image.....");
        databaseHelper.addImage(downloadUri, this);
    }

    @Override
    public void onImageAddedSuccess(String imageUID)
    {
        progressDialog.setTitle("Adding Post.....");
        progressDialog.show();
        databaseHelper.addNewPost(imageUID, descEditText.getText().toString().trim(), this);
    }

    @Override
    public void onImageAddedFailed()
    {
    }

    @Override
    public void onPhotoUploadFailed()
    {
        Toast.makeText(getContext(), getText(R.string.failed_uploading_photo), Toast.LENGTH_SHORT).show();
    }

    //Post added Listeners


    @Override
    public void onPostAddedSuccess()
    {
        progressDialog.dismiss();
        onPostAddedListener.postAddedSuccessfully();

    }

    @Override
    public void onPostAddedFailed()
    {
        progressDialog.dismiss();
        Toast.makeText(getContext(), "Failed to add Post" , Toast.LENGTH_SHORT).show();
    }
}
