package com.android.github.souravbera.e_commerce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.github.souravbera.e_commerce.Prevalent.Prevalent;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivitySettings extends AppCompatActivity {

    private CircleImageView profileImageView;
    private EditText fullNameEdittxt, userPhoneEdittxt, addressEdittxt;
    private TextView profileChangeTextBtn, closeTextBtn, saveTextbtn;

    private Uri imageUri;
    private String myUrl= "";
    private StorageTask uploadTask;
    private StorageReference storageProfilePictureRef;
    private String checker= "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profileImageView= findViewById(R.id.settings_profile_image);
        fullNameEdittxt= findViewById(R.id.settings_fullname);
        userPhoneEdittxt= findViewById(R.id.settings_phone_number);
        addressEdittxt= findViewById(R.id.settings_address);
        profileChangeTextBtn= findViewById(R.id.profile_image_change_btn);
        closeTextBtn= findViewById(R.id.close_setting_btn);
        saveTextbtn= findViewById(R.id.update_account_settings);

        userInfoDisplay(profileImageView, fullNameEdittxt, userPhoneEdittxt, addressEdittxt);

        closeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        saveTextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checker.equals("clicked"))
                {
                    userInfoSaved();
                }
                else
                {
                    updateOnlyUserInfo();
                }
            }
        });

        profileChangeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                checker="clicked";
                CropImage.activity(imageUri)
                        .setAspectRatio(1, 1)
                        .start(ActivitySettings.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode== RESULT_OK && data!= null)
        {
            CropImage.ActivityResult result= CropImage.getActivityResult(data);
            imageUri= result.getUri();

            profileImageView.setImageURI(imageUri);

        }
        else{
            Toast.makeText(this, "Error, Try Again..",Toast.LENGTH_SHORT).show();

            startActivity(new Intent(ActivitySettings.this,ActivitySettings.class));
            finish();

        }
    }

    private void updateOnlyUserInfo()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");

        HashMap<String , Object> userMap= new HashMap<>();
        userMap.put("name", fullNameEdittxt.getText().toString());
        userMap.put("address", userPhoneEdittxt.getText().toString());
        userMap.put("phone", addressEdittxt.getText().toString());
        ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);

        startActivity(new Intent(ActivitySettings.this, HomeActivity.class));
        Toast.makeText(ActivitySettings.this,"Profile Info Update Successfuly." , Toast.LENGTH_SHORT).show();
        finish();
    }

    private void userInfoSaved()
    {
        if(TextUtils.isEmpty(fullNameEdittxt.getText().toString()))
        {
            Toast.makeText(this, "Name is mandatory.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(addressEdittxt.getText().toString()))
        {
            Toast.makeText(this, "Address is mandatory.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(userPhoneEdittxt.getText().toString()))
        {
            Toast.makeText(this, "Phone Number is mandatory.", Toast.LENGTH_SHORT).show();
        }
        else if(checker.equals("clicked")){
            UploadImage();
        }
    }

    private void UploadImage()
    {
        final ProgressDialog progressDialog= new ProgressDialog(this);
        progressDialog.setTitle("Update Profile");
        progressDialog.setMessage("Please Wait, While we are updating your Account information");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if(imageUri != null){

            final StorageReference fileRef= storageProfilePictureRef
                    .child(Prevalent.currentOnlineUser.getPhone() + ".jpg");

            uploadTask = fileRef.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception
                {
                    if(!task.isSuccessful())
                    {
                        throw task.getException();
                    }

                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                 if(task.isSuccessful())
                 {
                     Uri downloadUri = task.getResult();
                     myUrl= downloadUri.toString();

                     DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");

                     storageProfilePictureRef= FirebaseStorage.getInstance().getReference().child("Profile pictures");

                     HashMap<String , Object> userMap= new HashMap<>();
                     userMap.put("name", fullNameEdittxt.getText().toString());
                     userMap.put("address", userPhoneEdittxt.getText().toString());
                     userMap.put("phone", addressEdittxt.getText().toString());
                     userMap.put("image", myUrl);
                     ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);
                     progressDialog.dismiss();

                     startActivity(new Intent(ActivitySettings.this, HomeActivity.class));
                     Toast.makeText(ActivitySettings.this,"Profile Info Update Successfuly." , Toast.LENGTH_SHORT).show();
                     finish();

                 }
                 else{
                     progressDialog.dismiss();
                     Toast.makeText(ActivitySettings.this, "Error.", Toast.LENGTH_SHORT).show();
                     finish();
                 }
                }
            });
        }

    }

    private void userInfoDisplay(final CircleImageView profileImageView, final EditText fullNameEdittxt, final EditText userPhoneEdittxt, final EditText addressEdittxt) {
        DatabaseReference UserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhone());

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.child("image").exists())
                    {
                        String image= dataSnapshot.child("image").getValue().toString();
                        String name= dataSnapshot.child("name").getValue().toString();
                        String phone= dataSnapshot.child("password").getValue().toString();
                        String address= dataSnapshot.child("address").getValue().toString();

                        Picasso.get().load(image).into(profileImageView);
                        fullNameEdittxt.setText(name);
                        userPhoneEdittxt.setText(phone);
                        addressEdittxt.setText(address);


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}