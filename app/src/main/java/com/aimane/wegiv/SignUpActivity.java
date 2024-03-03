package com.aimane.wegiv;

import static com.aimane.wegiv.R.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SignUpActivity extends AppCompatActivity {

    ImageView profile_pic;
    Uri uri;
    Button cancel_btn, create_account_btn;
    ProgressBar progressBar;
    EditText first_name_et, last_name_et, school_et, phone_et, address_et, email_et, password_et;
    TextView error_tv;
    RadioGroup gender_rd;
    RadioButton selected_gender;
    String id, firstName, lastName, profilePic, gender, address, phoneNumber, schoolName, email, password;
    DatabaseReference mDatabase;
    FirebaseAuth auth;

    FirebaseStorage storage;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();

        //Profile Pic part
        profile_pic = findViewById(R.id.profile_pic);
        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(SignUpActivity.this)
                        .cropSquare()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });

        //Form
        first_name_et = findViewById(R.id.first_name_et);
        last_name_et = findViewById(R.id.last_name_et);
        gender_rd = findViewById(R.id.gender_rd);
        school_et = findViewById(R.id.school_et);
        phone_et = findViewById(R.id.phone_et);
        address_et = findViewById(R.id.address_et);
        email_et = findViewById(R.id.signup_email_et);
        password_et = findViewById(R.id.signup_password_et);

        error_tv = findViewById(R.id.error_tv);

        progressBar = findViewById(R.id.signUp_progressBar);

        //id = UUID.randomUUID().toString();

        //CREATE ACCOUNT
        create_account_btn = findViewById(R.id.create_account_btn);
        create_account_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstName = first_name_et.getText().toString();
                lastName = last_name_et.getText().toString();
                profilePic = uri != null ? uri.toString() : "";

                //GENDER
                // get selected radio button from radioGroup
                int selectedId = gender_rd.getCheckedRadioButtonId();
                // find the radiobutton by returned id
                selected_gender = findViewById(selectedId);
                gender = selected_gender.getText().toString();

                schoolName = school_et.getText().toString();
                phoneNumber = phone_et.getText().toString();
                address = address_et.getText().toString();
                email = email_et.getText().toString();
                password = password_et.getText().toString();

                if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(profilePic) || TextUtils.isEmpty(schoolName) || TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(address) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    progressBar.setVisibility(View.INVISIBLE);
                    error_tv.setVisibility(View.VISIBLE);

                } else {
                    error_tv.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);

                    //test favorites
                    //List<String> favorites = new ArrayList<>();
                    //favorites.add("7fa95ff9-776a-4924-a912-436bf23e67dc");
                    //favorites.add("ef88237b-82f1-4aba-807d-2b62f49a05f1");

                    User new_user = new User(id, firstName, lastName, gender, address, phoneNumber, schoolName, email, password, null, 0);
                    mDatabase = FirebaseDatabase.getInstance().getReference("User");

                    //ADD USER TO AUTH
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // success
                                        //Toast.makeText(SignUpActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();

                                        // sign in new user
                                        auth.signInWithEmailAndPassword(email, password)
                                                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        if (task.isSuccessful()) {
                                                            //sign in success

                                                            //user id (must be the same as in auth database)
                                                            FirebaseUser currentUser = auth.getCurrentUser();
                                                            id = currentUser.getUid();

                                                            //ADD INFO TO USER DATABASE
                                                            mDatabase.child(id).setValue(new_user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    // Upload Image
                                                                    storage = FirebaseStorage.getInstance();
                                                                    storageRef = storage.getReference();

                                                                    StorageReference ref = storageRef.child("ProfileImages/"+id);
                                                                    UploadTask uploadTask = ref.putFile(uri);

                                                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception exception) {
                                                                            // Handle unsuccessful uploads
                                                                            Toast.makeText(SignUpActivity.this, "Uploading failed.", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                            // Handle successful uploads
                                                                            //Toast.makeText(SignUpActivity.this, "Profile Picture Uploaded Successfully.", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }
                                                            });

                                                            //set display name and profile pic
                                                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(firstName + " " + lastName).setPhotoUri(uri).build();
                                                            user.updateProfile(profileUpdates);

                                                            Toast.makeText(SignUpActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            // fails
                                                            Toast.makeText(SignUpActivity.this, "Something went wrong. Try again.", Toast.LENGTH_SHORT).show();
                                                            progressBar.setVisibility(View.INVISIBLE);

                                                        }
                                                    }
                                                });
                                    } else {
                                        // fails, display a message to the user.
                                        Toast.makeText(SignUpActivity.this, "Something went wrong. Try again.", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.INVISIBLE);

                                    }
                                }
                            });

                }
            }
        });

        //CANCEL
        cancel_btn = findViewById(R.id.cancel_btn);
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uri = data.getData();
        profile_pic.setImageURI(uri);
    }
}