package com.ziyad.wegiv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.List;

public class RecyclerViewAdapterFavorites extends RecyclerView.Adapter<RecyclerViewAdapterFavorites.ContactViewHolder> {


    List<Article> articlesList;
    Context context;
    StorageReference storageReference;
    FirebaseAuth auth;

    DatabaseReference database;

    public RecyclerViewAdapterFavorites(List<Article> articlesList, Context context) {
        this.articlesList = articlesList;
        this.context = context;
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView article_name_tv, pts_tv, location_tv, date_tv;
        ImageView article_image_iv, profile_iv, save_iv, date_iv, premium_iv;
        ConstraintLayout parentLayout;
        CardView cardView;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            article_name_tv = itemView.findViewById(R.id.article_name_tv);
            pts_tv = itemView.findViewById(R.id.pts_tv);
            location_tv = itemView.findViewById(R.id.location_tv);
            date_tv = itemView.findViewById(R.id.date_tv);

            article_image_iv = itemView.findViewById(R.id.article_image_iv);
            profile_iv = itemView.findViewById(R.id.profile_iv);
            save_iv = itemView.findViewById(R.id.save_iv);
            date_iv = itemView.findViewById(R.id.date_iv);
            premium_iv = itemView.findViewById(R.id.premium_iv);

            parentLayout = itemView.findViewById(R.id.article_view_layout);

            cardView = itemView.findViewById(R.id.cardview);
        }
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_view, parent, false);
        ContactViewHolder holder = new ContactViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.premium_iv.setVisibility(View.INVISIBLE);
        holder.pts_tv.setTextColor(Color.parseColor("#AE2E8E"));

        holder.save_iv.setImageURI(Uri.parse("android.resource://com.ziyad.wegiv/drawable/ic_saved"));

        //animation
        holder.cardView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.article_animation));

        if (!TextUtils.isEmpty(articlesList.get(position).getTitle()) && articlesList.get(position).getTitle().length() >= 14) {
            holder.article_name_tv.setText(articlesList.get(position).getTitle().substring(0, 12) + "...");
        } else {
            holder.article_name_tv.setText(articlesList.get(position).getTitle());
        }

        //Article Image
        storageReference = FirebaseStorage.getInstance().getReference("ArticleImages/"+articlesList.get(position).getId());
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri downloadUrl)
            {
                Glide.with(context)
                        .load(downloadUrl.toString())
                        .placeholder(R.drawable.bg_image)
                        .error(R.drawable.bg_image)
                        .into(holder.article_image_iv);
            }
        });

        //Article Publisher
        storageReference = FirebaseStorage.getInstance().getReference("ProfileImages/"+articlesList.get(position).getPublisher());
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri downloadUrl)
            {
                Glide.with(context)
                        .load(downloadUrl.toString())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(holder.profile_iv);
            }
        });

        //Article Points
        if (articlesList.get(position).getPoints() == null || articlesList.get(position).getPoints().equals("0")) {
            holder.premium_iv.setVisibility(View.INVISIBLE);
            holder.pts_tv.setText("Free");
            holder.pts_tv.setTextColor(Color.parseColor("#228B22"));
        } else {
            holder.pts_tv.setTextColor(Color.parseColor("#AE2E8E"));
            holder.premium_iv.setVisibility(View.VISIBLE);
            holder.pts_tv.setText(articlesList.get(position).getPoints() + " Pts");
        }


        //holder.location_tv.setText(articlesList.get(position).getLocation());
        if (!TextUtils.isEmpty(articlesList.get(position).getLocation()) && articlesList.get(position).getLocation().length() >= 14) {
            holder.location_tv.setText(articlesList.get(position).getLocation().substring(0, 14) + "...");
        } else {
            holder.location_tv.setText(articlesList.get(position).getLocation());
        }


        try {
            String[] expirationDate = articlesList.get(position).getExpirationDate().split("#");
            holder.date_tv.setText(expirationDate[0] + " at " + expirationDate[1]);
        } catch (Exception e) {
            holder.date_tv.setText("Doesn't expire");
        }

        //Is favorite
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userId = currentUser.getUid();


        database = FirebaseDatabase.getInstance().getReference("User/" + userId +"/favorites");

        holder.parentLayout.setOnClickListener(view -> {
            //Show article details page
            MainActivity mainActivityView = (MainActivity) context;
            mainActivityView.ArticleDetailsFragment(articlesList.get(position));
        });

        holder.save_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.child(articlesList.get(position).getId()).removeValue();
                Toast.makeText(context, "Favorite article removed.", Toast.LENGTH_SHORT).show();
                articlesList.remove(position);
                notifyDataSetChanged();

            }
        });
    }

    @Override
    public int getItemCount() {
        return articlesList.size();
    }
}
