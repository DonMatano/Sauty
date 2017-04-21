package com.matano.sauty;

import android.app.Activity;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.matano.sauty.Model.DatabaseHelper;
import com.matano.sauty.Model.SautyUser;

/**
 * Created by matano on 6/4/17.
 */

public class EditProfileFragment extends Fragment implements DatabaseHelper.photoUploadToStorageListener
    ,DatabaseHelper.userUpdatedListener
{
    private ImageView profilePic;
    private EditText userNameEditText;
    private EditText userStatusEditText;
    private Button saveButton;
    private DatabaseHelper databaseHelper;
    Uri imageUri;
    private final int PICK_IMAGE_REQUEST = 6;
    private SautyUser user;
    ProfileUpdatedListener profileUpdatedListener;


    public static EditProfileFragment newInstance(SautyUser user)
    {

        Bundle args = new Bundle();

        EditProfileFragment fragment = new EditProfileFragment();
        args.putParcelable("User", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        try
        {
           profileUpdatedListener = (ProfileUpdatedListener) context;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString() +
                    " must implement ProfileUpdatedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        user = getArguments().getParcelable("User");
        databaseHelper = DatabaseHelper.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        profilePic = (ImageView) v.findViewById(R.id.profilePicEditImageView);
        profilePic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getPhotoFromPhone();
            }
        });
        userNameEditText = (EditText) v.findViewById(R.id.userNameEditText);
        userStatusEditText = (EditText) v.findViewById(R.id.userStatusEditText);
        saveButton = (Button) v.findViewById(R.id.saveButtonEditFrag);

        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                upload();
            }
        });


        initLayout();

        return v;
    }

    interface ProfileUpdatedListener
    {
        void userSuccessfullyUpdated();
        void userFailedUpdate();
    }

    private void upload()
    {
        if (imageUri != null)
        {
            ContentResolver cR = getContext().getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = mime.getExtensionFromMimeType(cR.getType(imageUri));
            databaseHelper.uploadProfilePhoto(imageUri, type, this);
        }
        else
        {
            if (!userStatusEditText.getText().toString().trim().equals(getString(R.string.no_status_hint)))
            {
                user.setUserStatus(userStatusEditText.getText().toString());
            }

            user.setUserName(userNameEditText.getText().toString());

            databaseHelper.updateUser(user, this);
        }
    }

    private void initLayout()
    {
        Glide.with(getContext()).load(Uri.parse(user.getUserProfilePic()))
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(profilePic);

        userNameEditText.setText(user.getUserName());

        if (user.getUserStatus() != null)
        {
            userStatusEditText.setText(user.getUserStatus());
        }
        else
        {
            userStatusEditText.setText(getText(R.string.no_status_hint));
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

    //Database Helper Photo Upload Listener


    @Override
    public void onPhotoUploadSuccess(String downloadUrl)
    {
        user.setUserProfilePic(downloadUrl);

        if (!userStatusEditText.getText().toString().trim().equals(getString(R.string.no_status_hint)))
        {
            user.setUserStatus(userStatusEditText.getText().toString());
        }

        user.setUserName(userNameEditText.getText().toString());

        databaseHelper.updateUser(user, this);
    }

    @Override
    public void onPhotoUploadFailed()
    {

    }


    //Database Helper user Updated Listener
    @Override
    public void onUserUpdatedSuccess()
    {
        Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
        profileUpdatedListener.userSuccessfullyUpdated();
    }

    @Override
    public void onUserAddedFailed()
    {

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null
                && data.getData() != null) {

            profilePic.setImageDrawable(null);
            imageUri = data.getData();

            Glide.with(getContext()).load(imageUri)
                    .crossFade()
                    .into(profilePic);
        }
    }
}
