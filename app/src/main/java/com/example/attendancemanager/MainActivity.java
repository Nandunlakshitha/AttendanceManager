package com.example.attendancemanager;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Adding animation to the TextView
        TextView textViewAppName = findViewById(R.id.textViewAppName);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(2000); // 2 seconds
        textViewAppName.startAnimation(fadeIn);

        // Delayed execution for 3 seconds before transitioning to the home activity
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(MainActivity.this, login.class);
                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}
