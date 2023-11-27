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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
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

public class HomeFragment extends Fragment {

    chetanBottomNavigation bottomNavigation;
    String category;
    RecyclerView articles_rv;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;

    TextView no_results_tv;

    Spinner category_spinner;

    List<Article> articlesList = new ArrayList<>();

    DatabaseReference database;
    //StorageReference storageReference;

    Toolbar toolbar;

    public static final int home = 1;
    public static final int fav = 2;
    public static final int add = 3;
    public static final int profile = 4;
    public static final int chat = 5;

    //ActivityMainBinding binding;
    //ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //RECYCLER VIEW
        articles_rv = view.findViewById(R.id.articles_rv);
        layoutManager = new LinearLayoutManager(getActivity());
        articles_rv.setLayoutManager(layoutManager);

        articles_rv.setHasFixedSize(true);

        adapter = new RecyclerViewAdapter(articlesList, getActivity());
        articles_rv.setAdapter(adapter);

        //SPINNER
        category_spinner = view.findViewById(R.id.category_spinner);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getActivity(), R.array.filter_categories_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category_spinner.setAdapter(adapter1);
        category_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                no_results_tv.setVisibility(View.VISIBLE);
                articlesList.clear();
                switch (i) {
                    case 1:
                        category = "Food";
                        break;
                    case 2:
                        category = "Clothes";
                        break;
                    case 3:
                        category = "Drugs";
                        break;
                    case 4:
                        category = "Utilities";
                        break;
                    case 5:
                        category = "Electronics";
                        break;
                    case 6:
                        category = "Others";
                        break;
                    default:
                        category = "All";
                        break;
                }
                database = FirebaseDatabase.getInstance().getReference("Article");
                database.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Article article = dataSnapshot.getValue(Article.class);
                            try {
                                if (category.equals("All")) {
                                    no_results_tv.setVisibility(View.INVISIBLE);
                                    articlesList.add(article);
                                } else if (!articlesList.contains(article) && article.getCategory().equals(category)) {
                                    no_results_tv.setVisibility(View.INVISIBLE);
                                    articlesList.add(article);
                                }
                            } catch (Exception e) {
                                //
                            }

                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                no_results_tv.setVisibility(View.VISIBLE);
                articlesList.clear();
                database = FirebaseDatabase.getInstance().getReference("Article");
                database.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Article article = dataSnapshot.getValue(Article.class);
                            if (!articlesList.contains(article)) {
                                no_results_tv.setVisibility(View.INVISIBLE);
                                articlesList.add(article);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        //No favorites text
        no_results_tv = view.findViewById(R.id.no_results_tv);
        if (adapter.getItemCount() == 0) {
            no_results_tv.setVisibility(View.VISIBLE);
        } else {
            no_results_tv.setVisibility(View.INVISIBLE);
        }


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

        bottomNavigation.show(home, true);

        // Inflate the layout for this fragment
        return view;
    }


}