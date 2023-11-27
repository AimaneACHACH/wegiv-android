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

public class RecyclerViewAdapterMyArticles extends RecyclerView.Adapter<RecyclerViewAdapterMyArticles.ContactViewHolder> {


    List<Article> articlesList;
    Context context;
    StorageReference storageReference;
    FirebaseAuth auth;

    DatabaseReference database;

    public RecyclerViewAdapterMyArticles(List<Article> articlesList, Context context) {
        this.articlesList = articlesList;
        this.context = context;
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView article_name_tv;
        ImageView article_image_iv;
        ConstraintLayout parentLayout;
        CardView cardView;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            article_name_tv = itemView.findViewById(R.id.my_article_name_tv);

            article_image_iv = itemView.findViewById(R.id.my_article_image_iv);

            parentLayout = itemView.findViewById(R.id.my_article_view_layout);

            cardView = itemView.findViewById(R.id.cardview);
        }
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_article_view, parent, false);
        ContactViewHolder holder = new ContactViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, @SuppressLint("RecyclerView") final int position) {

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

        holder.parentLayout.setOnClickListener(view -> {
            //Show article details page
            MainActivity mainActivityView = (MainActivity) context;
            mainActivityView.ArticleDetailsFragment(articlesList.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return articlesList.size();
    }
}
