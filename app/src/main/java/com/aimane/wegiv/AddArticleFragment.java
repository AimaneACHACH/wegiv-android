package com.aimane.wegiv;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sagarkoli.chetanbottomnavigation.chetanBottomNavigation;

import java.net.URLEncoder;
import java.util.UUID;


public class AddArticleFragment extends Fragment {
    Spinner category_spinner;
    chetanBottomNavigation bottomNavigation;

    ImageView article_pic;
    EditText article_title_et, description_et, points_et, expiration_date_et, expiration_time_et, location_et;
    Switch does_expire_sw;
    TextView expiration_tv, error_publish_tv;
    ProgressBar publish_progressBar;

    Button publish_article_btn, cancel_publish_btn;

    String Id, ImgPath, Title, Category, Description, Points, PublishingDate, ExpirationDate, Location, Publisher;
    Boolean doesExpire;

    FirebaseAuth auth;
    DatabaseReference mDatabase;

    FirebaseStorage storage;
    StorageReference storageRef;

    public static final int home = 1;
    public static final int fav = 2;
    public static final int add = 3;
    public static final int profile = 4;
    public static final int chat = 5;

    Uri uri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_article, container, false);

        //SPINNER
        category_spinner = view.findViewById(R.id.category_spinner);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getActivity(), R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category_spinner.setAdapter(adapter1);
        category_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        Category = "Food";
                        break;
                    case 1:
                        Category = "Clothes";
                        break;
                    case 2:
                        Category = "Drugs";
                        break;
                    case 3:
                        Category = "Utilities";
                        break;
                    case 4:
                        Category = "Electronics";
                        break;
                    default:
                        Category = "Others";
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //BOTTOM NAV
        bottomNavigation = view.findViewById(R.id.addArticle_navBar);

        bottomNavigation.add(new chetanBottomNavigation.Model(home, R.drawable.ic_cart));
        bottomNavigation.add(new chetanBottomNavigation.Model(fav, R.drawable.ic_heart));
        bottomNavigation.add(new chetanBottomNavigation.Model(add, R.drawable.ic_add));
        bottomNavigation.add(new chetanBottomNavigation.Model(profile, R.drawable.ic_person));
        bottomNavigation.add(new chetanBottomNavigation.Model(chat, R.drawable.ic_chat));

        bottomNavigation.setCount(chat, "10");

        bottomNavigation.setOnShowListener(new chetanBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(chetanBottomNavigation.Model item) {

            }
        });

        bottomNavigation.setOnClickMenuListener(new chetanBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(chetanBottomNavigation.Model item) {
                //Toast.makeText(getActivity(), "item clicked", Toast.LENGTH_SHORT).show();
                MainActivity mainActivityView = (MainActivity) getActivity();
                switch (item.getId()) {
                    case home:
                        mainActivityView.HomeFragment();
                        break;

                    case fav:
                        mainActivityView.FavoritesFragment();
                        break;

                    case add:
                        mainActivityView.AddArticleFragment();
                        break;

                    case profile:
                        mainActivityView.ProfileFragment();
                        break;

                    case chat:
                        try{
                            PackageManager packageManager = getActivity().getPackageManager();
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            String url = "https://api.whatsapp.com/send?text=" + URLEncoder.encode("I'm interested in the article you listed.", "UTF-8");
                            i.setPackage("com.whatsapp");
                            i.setData(Uri.parse(url));
                            if (i.resolveActivity(packageManager) != null) {
                                startActivity(i);
                            }else {
                                Toast.makeText(mainActivityView, "An error occured.", Toast.LENGTH_SHORT).show();
                            }
                        } catch(Exception e) {
                            Toast.makeText(mainActivityView, "An error occured.", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    default:
                        break;
                }
            }
        });

        bottomNavigation.setOnReselectListener(new chetanBottomNavigation.ReselectListener() {
            @Override
            public void onReselectItem(chetanBottomNavigation.Model item) {

            }
        });

        bottomNavigation.show(add, true);

        /*
        //IMAGE PICKER
        article_pic = view.findViewById(R.id.article_pic);
        article_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(getActivity())
                        .cropSquare()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });
        */

        //READ FORM INFO
        article_title_et = view.findViewById(R.id.article_title_et);
        description_et = view.findViewById(R.id.description_et);
        points_et = view.findViewById(R.id.points_et);
        expiration_date_et = view.findViewById(R.id.expiration_date_et);
        expiration_time_et = view.findViewById(R.id.expiration_time_et);
        location_et = view.findViewById(R.id.location_et);

        does_expire_sw = view.findViewById(R.id.does_expires_sw);

        publish_article_btn = view.findViewById(R.id.puclish_article_btn);
        cancel_publish_btn = view.findViewById(R.id.cancel_publish_btn);

        error_publish_tv = view.findViewById(R.id.error_publish_tv);
        expiration_tv = view.findViewById(R.id.expiration_tv);
        publish_progressBar = view.findViewById(R.id.publish_progressBar);

        //...
        Id = UUID.randomUUID().toString();

        //ImgPath = uri != null ? uri.toString() : "";

        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        PublishingDate = today.monthDay + "/" + today.month + "/" + today.year + "#" + today.format("%H:%M");

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        Publisher = currentUser.getUid();  //get uploader's id

        //Switcher
        does_expire_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    expiration_tv.setTextColor(Color.parseColor("#F44336"));
                    expiration_date_et.setEnabled(true);
                    expiration_time_et.setEnabled(true);
                } else {
                    expiration_tv.setTextColor(Color.parseColor("#B76C6C6C"));
                    expiration_date_et.setEnabled(false);
                    expiration_time_et.setEnabled(false);
                }
            }
        });

        //publish
        publish_article_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Title = article_title_et.getText().toString();
                Description = description_et.getText().toString();
                Points = points_et.getText().toString();
                ExpirationDate = expiration_date_et.getText().toString() + "#" + expiration_time_et.getText().toString();
                Location = location_et.getText().toString();

                doesExpire = does_expire_sw.isChecked();

                if (TextUtils.isEmpty(ImgPath) || TextUtils.isEmpty(Title) || TextUtils.isEmpty(Description) || TextUtils.isEmpty(Points) || TextUtils.isEmpty(Location) || (doesExpire && ExpirationDate.equals("#"))) {
                    //Toast.makeText(getActivity(), Id + "@" + ImgPath + "@" + Title + "@" + Category + "@" + Description + "@" + Points + "@" + PublishingDate + "@" + ExpirationDate + "@" + Location + "@" + Publisher , Toast.LENGTH_SHORT).show();
                    error_publish_tv.setVisibility(View.VISIBLE);
                    publish_progressBar.setVisibility(View.INVISIBLE);
                } else {
                    //System.out.println("All attributes are not empty.");
                    error_publish_tv.setVisibility(View.INVISIBLE);
                    publish_progressBar.setVisibility(View.VISIBLE);

                    Article new_article = new Article(Id, Title, Category, Description, Points, PublishingDate, ExpirationDate, Location, Publisher, doesExpire);
                    mDatabase = FirebaseDatabase.getInstance().getReference("Article");
                    mDatabase.child(Id).setValue(new_article).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //Toast.makeText(getActivity(), "Article Listed Successfully.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Something went wrong.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    // Upload Image
                    storage = FirebaseStorage.getInstance();
                    storageRef = storage.getReference();
                    StorageReference riversRef = storageRef.child("ArticleImages/"+Id);
                    UploadTask uploadTask = riversRef.putFile(uri);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Toast.makeText(getActivity(), "Uploading failed.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Handle successful uploads
                            Toast.makeText(getActivity(), "Article Listed Successfully.", Toast.LENGTH_SHORT).show();
                            MainActivity mainActivityView = (MainActivity) getActivity();
                            mainActivityView.HomeFragment();
                        }
                    });
                }
            }
        });

        //cancel
        cancel_publish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Canceled.", Toast.LENGTH_SHORT).show();
                MainActivity mainActivityView = (MainActivity) getActivity();
                mainActivityView.HomeFragment();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //IMAGE PICKER
        article_pic = view.findViewById(R.id.article_pic);
        article_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(AddArticleFragment.this)
                        .cropSquare()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uri = data.getData();
        ImgPath = uri.toString();
        article_pic.setImageURI(uri);
    }
}