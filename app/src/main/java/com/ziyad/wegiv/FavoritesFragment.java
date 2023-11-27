package com.ziyad.wegiv;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.sagarkoli.chetanbottomnavigation.chetanBottomNavigation;
import com.ziyad.wegiv.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FavoritesFragment extends Fragment {

    chetanBottomNavigation bottomNavigation;
    TextView no_favorites_tv;

    RecyclerView favorites_rv;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;

    List<Article> articlesList = new ArrayList<>();
    List<String> favoritesList = new ArrayList<>();
    FirebaseAuth auth;

    DatabaseReference database;
    //StorageReference storageReference;

    public static final int home = 1;
    public static final int fav = 2;
    public static final int add = 3;
    public static final int profile = 4;
    public static final int chat = 5;

    //ActivityMainBinding binding;
    //ProgressDialog progressDialog;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favorites, container, false);



        //RECYCLER VIEW
        favorites_rv = view.findViewById(com.ziyad.wegiv.R.id.favorites_rv);
        layoutManager = new LinearLayoutManager(getActivity());
        favorites_rv.setLayoutManager(layoutManager);

        //articles_rv.setHasFixedSize(true);

        adapter = new RecyclerViewAdapterFavorites(articlesList, getActivity());
        favorites_rv.setAdapter(adapter);

        //retrieve FAVORITE ARTICLE from firebase and put them in articlesList
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userId = currentUser.getUid();

        database = FirebaseDatabase.getInstance().getReference("User/" + userId +"/favorites");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        String articleId = dataSnapshot.getValue(String.class);
                        favoritesList.add(articleId);
                        no_favorites_tv.setVisibility(View.INVISIBLE);
                    } catch (com.google.firebase.database.DatabaseException databaseException) {
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        map.forEach((k,v) -> {
                            favoritesList.add(k);
                        });
                        no_favorites_tv.setVisibility(View.INVISIBLE);
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "An error occured", Toast.LENGTH_SHORT).show();
                    }

                    //Toast.makeText(getActivity(), article.toString(), Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        database = FirebaseDatabase.getInstance().getReference("Article");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (String articleId : favoritesList) {
                    Article article = datasnapshot.child(articleId).getValue(Article.class);
                    if (!articlesList.contains(article)) {
                        articlesList.add(article);
                    }
                    //articlesList.add(article);
                    //Toast.makeText(getActivity(), article.toString(), Toast.LENGTH_SHORT).show();
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

        bottomNavigation.show(fav, true);

        //No favorites text
        no_favorites_tv = view.findViewById(R.id.no_favorites_tv);
        if (adapter.getItemCount() == 0) {
            no_favorites_tv.setVisibility(View.VISIBLE);
        } else {
            no_favorites_tv.setVisibility(View.INVISIBLE);
        }

        // Inflate the layout for this fragment
        return view;
    }

}