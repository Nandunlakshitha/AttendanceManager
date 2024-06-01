package com.example.attendancemanager;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class attendancedetails extends AppCompatActivity {

    private LinearLayout layoutAttendanceDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendancedetails);

        layoutAttendanceDetails = findViewById(R.id.layoutAttendanceDetails);

        // Retrieve subject ID and name from intent
        String subjectId = getIntent().getStringExtra("subjectId");
        String subjectName = getIntent().getStringExtra("subjectName");

        // Reference to Firebase Realtime Database for attendance data
        DatabaseReference attendanceReference = FirebaseDatabase.getInstance().getReference("attendance");

        // ValueEventListener to retrieve attendance data from Firebase
        attendanceReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                layoutAttendanceDetails.removeAllViews(); // Clear previous views

                // Iterate through each student's attendance data
                for (DataSnapshot facultySnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot departmentSnapshot : facultySnapshot.getChildren()) {
                        for (DataSnapshot yearSnapshot : departmentSnapshot.getChildren()) {
                            for (DataSnapshot semesterSnapshot : yearSnapshot.getChildren()) {
                                for (DataSnapshot studentSnapshot : semesterSnapshot.getChildren()) {
                                    String studentId = studentSnapshot.getKey();
                                    int totalClasses = 0;
                                    int attendedClasses = 0;

                                    // Iterate through each date's attendance data
                                    for (DataSnapshot dateSnapshot : studentSnapshot.getChildren()) {
                                        String attendanceStatus = dateSnapshot.child(subjectName).getValue(String.class);
                                        if (attendanceStatus != null) {
                                            totalClasses++;
                                            if (attendanceStatus.equals("present")) {
                                                attendedClasses++;
                                            }
                                        }
                                    }

                                    // Calculate attendance percentage
                                    double attendancePercentage = (totalClasses > 0) ? (attendedClasses * 100.0 / totalClasses) : 0;

                                    // Create a new TextView for each student's attendance details
                                    TextView textView = new TextView(attendancedetails.this);
                                    textView.setText("Student ID: " + studentId + "\nAttendance: " + String.format("%.2f", attendancePercentage) + "%");
                                    textView.setTextSize(18);
                                    textView.setPadding(16, 16, 16, 16);

                                    // Set margins for the TextView
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                    );
                                    params.setMargins(16, 16, 16, 16);
                                    textView.setLayoutParams(params);

                                    // Add the TextView to the LinearLayout
                                    layoutAttendanceDetails.addView(textView);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
}
