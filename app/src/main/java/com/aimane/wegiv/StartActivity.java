package com.aimane.wegiv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity {

    ImageView app_logo, app_name;
    TextView app_slogan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        app_logo = findViewById(R.id.app_logo);
        app_name = findViewById(R.id.app_name);
        app_slogan = findViewById(R.id.app_slogan);

        Animation fade_in = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
        //Animation fade_out = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_out);

        app_logo.setAnimation(fade_in);
        app_name.setAnimation(fade_in);
        app_slogan.setAnimation(fade_in);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(StartActivity.this, LogInActivity.class));
                finish();
            }
        },2000);
    }
}