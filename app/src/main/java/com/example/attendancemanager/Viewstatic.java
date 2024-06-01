package com.example.attendancemanager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Viewstatic extends AppCompatActivity {

    private LinearLayout linearLayoutSubjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_viewstatic);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        linearLayoutSubjects = findViewById(R.id.linearLayoutSubjects);

        // Reference to Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("subjects");

        // ValueEventListener to retrieve data from Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                linearLayoutSubjects.removeAllViews(); // Clear previous views

                // Iterate through each subject in the dataSnapshot
                for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                    // Get subject data
                    String subjectId = subjectSnapshot.getKey();
                    String subjectName = subjectSnapshot.child("subjectName").getValue(String.class);
                    String lecturerName = subjectSnapshot.child("lecturerName").getValue(String.class);

                    // Create a new TextView for each subject
                    TextView textView = new TextView(Viewstatic.this);
                    textView.setText("Subject: " + subjectName + "\nLecturer: " + lecturerName);
                    textView.setTextSize(18);
                    textView.setPadding(16, 16, 16, 16);
                    textView.setBackgroundColor(Color.parseColor("#707070"));
                    textView.setTextColor(Color.WHITE);

                    // Set margins for the TextView
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(16, 16, 16, 16);
                    textView.setLayoutParams(params);

                    // Set click listener for the TextView
                    textView.setOnClickListener(v -> {
                        Intent intent = new Intent(Viewstatic.this, attendancedetails.class);
                        intent.putExtra("subjectId", subjectId);
                        intent.putExtra("subjectName", subjectName);
                        startActivity(intent);
                    });


                    // Add the TextView to the LinearLayout
                    linearLayoutSubjects.addView(textView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
}
