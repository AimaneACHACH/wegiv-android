package com.aimane.wegiv;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ArticleDetailsFragment extends Fragment {

    Article article = new Article(); //sent by recyclerview when the article is clicked
    ImageView article_image_iv, publisher_image_iv, home_iv, share_iv, save_iv;
    TextView article_name_tv, article_date_tv, article_location_tv, article_description_tv, publisher_name_tv;
    RecyclerView similar_articles_rv;
    Button contact_btn, buy_btn;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;

    FirebaseAuth auth;
    StorageReference storageReference;
    List<Article> similarArticlesList = new ArrayList<>();

    DatabaseReference database;

    public ArticleDetailsFragment() {
        // Required empty public constructor
    }

    public ArticleDetailsFragment(Article article) {
        this.article = article;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_article_details, container, false);

        article_name_tv = view.findViewById(R.id.article_name_tv);
        article_date_tv = view.findViewById(R.id.article_date_tv);
        article_location_tv = view.findViewById(R.id.article_location_tv);
        article_description_tv = view.findViewById(R.id.article_description_tv);
        publisher_name_tv = view.findViewById(R.id.publisher_name_tv);

        publisher_image_iv = view.findViewById(R.id.publisher_image_iv);
        article_image_iv = view.findViewById(R.id.article_image_iv);

        home_iv = view.findViewById(R.id.home_iv);
        share_iv = view.findViewById(R.id.share_iv);
        save_iv = view.findViewById(R.id.save_iv);

        similar_articles_rv = view.findViewById(R.id.similar_articles_rv);

        contact_btn = view.findViewById(R.id.contact_btn);
        buy_btn = view.findViewById(R.id.buy_btn);

        //ARTICLE INFO

        //test
        //Toast.makeText(getActivity(), currentArticle.toString(), Toast.LENGTH_SHORT).show();

        article_name_tv.setText(article.getTitle());
        article_location_tv.setText(article.getLocation());
        article_description_tv.setText(article.getDescription());
        //Expiration date
        try {
            String[] expirationDate = article.getExpirationDate().split("#");
            article_date_tv.setText(expirationDate[0] + " at " + expirationDate[1]);
        } catch (Exception e) {
            article_date_tv.setText("Doesn't expire");
        }

        //article image
        storageReference = FirebaseStorage.getInstance().getReference("ArticleImages/"+article.getId());
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri downloadUrl)
            {
                Glide.with(getActivity())
                        .load(downloadUrl.toString())
                        .placeholder(R.drawable.bg_image)
                        .error(R.drawable.bg_image)
                        .into(article_image_iv);
            }
        });

        //publisher info
        database = FirebaseDatabase.getInstance().getReference("User/" + article.getPublisher() + "/firstName");
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String firstName = dataSnapshot.getValue(String.class);
                    publisher_name_tv.setText(firstName);
                } else {
                    //article no longer exists
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database = FirebaseDatabase.getInstance().getReference("User/" + article.getPublisher() + "/lastName");
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String lastName = dataSnapshot.getValue(String.class);
                    publisher_name_tv.setText(publisher_name_tv.getText() + " " + lastName);
                } else {
                    //article no longer exists
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        storageReference = FirebaseStorage.getInstance().getReference("ProfileImages/"+article.getPublisher());
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri downloadUrl)
            {
                Glide.with(getActivity())
                        .load(downloadUrl.toString())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(publisher_image_iv);
            }
        });

        //Article Points
        if (article.getPoints() == null || article.getPoints().equals("0")) {
            buy_btn.setText("Free");
        } else {
            buy_btn.setText(article.getPoints() + " Pts");
        }


        //RECYCLER VIEW
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        similar_articles_rv.setLayoutManager(layoutManager);

        adapter = new RecyclerViewAdapterMyArticles(similarArticlesList, getActivity());
        similar_articles_rv.setAdapter(adapter);

        //retrieve data from firebase and put it in similarArticlesList
        database = FirebaseDatabase.getInstance().getReference("Article");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                similarArticlesList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Article thisArticle = dataSnapshot.getValue(Article.class);
                    try {
                        if (!similarArticlesList.contains(thisArticle) && article.getCategory().equals(thisArticle.getCategory()) && !article.getId().equals(thisArticle.getId())) {
                            similarArticlesList.add(thisArticle);
                            adapter.notifyDataSetChanged();
                        }
                    } catch(Exception e) {
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //User
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        String user_id = currentUser.getUid();

        //Balance
        /*
        database = FirebaseDatabase.getInstance().getReference("User/"+id+"/balance");
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Integer balance = dataSnapshot.getValue(Integer.class);
                    user_balance_tv.setText(balance);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
         */

        //isFavorite
        database = FirebaseDatabase.getInstance().getReference("User/" + user_id +"/favorites/"+article.getId());
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    save_iv.setImageURI(Uri.parse("android.resource://com.aimane.wegiv/drawable/ic_saved"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Save click listener
        save_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()){
                            database.child(article.getId()).setValue(article.getId());
                            save_iv.setImageURI(Uri.parse("android.resource://com.aimane.wegiv/drawable/ic_saved"));
                            Toast.makeText(getActivity(), "Added to favorites.", Toast.LENGTH_SHORT).show();
                        } else {
                            database.child(article.getId()).removeValue();
                            save_iv.setImageURI(Uri.parse("android.resource://com.aimane.wegiv/drawable/ic_not_saved"));
                            Toast.makeText(getActivity(), "Favorite article removed.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        home_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity mainActivityView = (MainActivity) getActivity();
                mainActivityView.HomeFragment();
            }
        });

        share_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    PackageManager packageManager = getActivity().getPackageManager();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    String url = "https://api.whatsapp.com/send?text=" + URLEncoder.encode("I'm interested in the article you listed.", "UTF-8");
                    i.setPackage("com.whatsapp");
                    i.setData(Uri.parse(url));
                    if (i.resolveActivity(packageManager) != null) {
                        startActivity(i);
                    }else {
                        Toast.makeText(getActivity(), "An error occured.", Toast.LENGTH_SHORT).show();
                    }
                } catch(Exception e) {
                    Toast.makeText(getActivity(), "An error occured.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}