package com.example.g6_findly.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.g6_findly.Fragments.ActionBottomDialogFragment;
import com.example.g6_findly.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class EditProfilePicture extends AppCompatActivity implements ActionBottomDialogFragment.ItemClickListener{

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    ImageView imageView;
    Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_picture);
        imageView= findViewById(R.id.profile_picture);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // get user's profile picture
            Picasso.get()
                    .load(user.getPhotoUrl())
                    .resize(500, 500)
                    .centerCrop()
                    .into(imageView);
        } else {
            // if the user is not authenticated, redirect to Login page
            Intent i = new Intent(this, Login.class);
            startActivity(i);
        }
        hideActionBar();
        ActionBottomDialogFragment addPhotoBottomDialogFragment =
                ActionBottomDialogFragment.newInstance();
        addPhotoBottomDialogFragment.show(getSupportFragmentManager(),
                ActionBottomDialogFragment.TAG);

    }

    public void hideActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    public void showBottomSheet(View view) {
        ActionBottomDialogFragment addPhotoBottomDialogFragment =
                ActionBottomDialogFragment.newInstance();
        addPhotoBottomDialogFragment.show(getSupportFragmentManager(),
                ActionBottomDialogFragment.TAG);
    }

    @Override public void onItemClick(String tag) {
        action(tag);
    }

    public void action(String tag){
        switch (tag){
            case "take_picture":
                Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
                break;

            case "gallery":
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);
                break;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imageView.setImageBitmap(selectedImage);
                        changeProfilePicture();
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        imageUri = data.getData();
                        imageView.setImageURI(imageUri);
                        changeProfilePicture();
                    }
                    break;
            }
        }
    }


    public void changeProfilePicture(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        StorageReference imagesRef = storageRef.child("users/"+uid);

        // Get the data from an ImageView as bytes
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();

        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(EditProfilePicture.this, "Error adding the image. Try again", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });



        storageRef.child("users/"+uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL

                Log.d("TAG","LINK:"+ uri);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(uri)
                    .build();

                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("TAG", "User profile updated.");
                                }
                            }
                        });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("TAG","ERROR ADDING IMAGE");
            }
        });
    }

    public void navigateBack(View v){
        finish();
    }
}
