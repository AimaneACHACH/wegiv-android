package com.ziyad.wegiv;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sagarkoli.chetanbottomnavigation.chetanBottomNavigation;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    ImageView user_image_iv;
    TextView user_name_tv, user_email_tv, user_balance_tv, no_listed_articles_tv;
    RecyclerView my_articles_rv;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;

    FirebaseAuth auth;
    List<Article> myArticlesList = new ArrayList<>();

    DatabaseReference database;

    //Bottom nav
    chetanBottomNavigation bottomNavigation;
    public static final int home = 1;
    public static final int fav = 2;
    public static final int add = 3;
    public static final int profile = 4;
    public static final int chat = 5;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        user_image_iv = view.findViewById(R.id.user_image_iv);
        user_name_tv = view.findViewById(R.id.user_name_tv);
        user_email_tv = view.findViewById(R.id.user_email_tv);
        user_balance_tv = view.findViewById(R.id.user_balance_tv);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        user_name_tv.setText(currentUser.getDisplayName());
        user_email_tv.setText(currentUser.getEmail());
        user_image_iv.setImageURI(currentUser.getPhotoUrl());

        String id = currentUser.getUid();

        //Balance
        database = FirebaseDatabase.getInstance().getReference("User/"+id+"/balance");
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Integer balance = dataSnapshot.getValue(Integer.class);
                    user_balance_tv.setText(String.valueOf(balance) + " Pts");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //RECYCLER VIEW
        my_articles_rv = view.findViewById(R.id.my_articles_rv);
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        my_articles_rv.setLayoutManager(layoutManager);

        adapter = new RecyclerViewAdapterMyArticles(myArticlesList, getActivity());
        my_articles_rv.setAdapter(adapter);

        //retrieve data from firebase and put it in myArticlesList
        database = FirebaseDatabase.getInstance().getReference("Article");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Article article = dataSnapshot.getValue(Article.class);
                    if (!myArticlesList.contains(article) && article.getPublisher().equals(id)) {
                        myArticlesList.add(article);
                        no_listed_articles_tv.setVisibility(View.INVISIBLE);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //BOTTOM NAV
        bottomNavigation = view.findViewById(R.id.navBar);

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

        bottomNavigation.show(profile, true);

        //No listed article text
        no_listed_articles_tv = view.findViewById(R.id.no_listed_articles);
        if (adapter.getItemCount() == 0) {
            no_listed_articles_tv.setVisibility(View.VISIBLE);
        } else {
            no_listed_articles_tv.setVisibility(View.INVISIBLE);
        }

        return view;
    }
}