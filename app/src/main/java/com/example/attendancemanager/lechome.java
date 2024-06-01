package com.example.attendancemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;

public class lechome extends AppCompatActivity {

    private String currentSubjectName;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lechome);

        // Retrieve subject name from the intent
        currentSubjectName = getIntent().getStringExtra("subjectName");

        // Update TextView6 with the subject name
        TextView textViewSubjectName = findViewById(R.id.textView6);
        textViewSubjectName.setText(currentSubjectName);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Button to start QR code attendance marking
        Button qrButton = findViewById(R.id.qrbutton);
        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startQRAttendance();
            }
        });

        // Button to start manual attendance marking
        Button manualButton = findViewById(R.id.manualbutton);
        manualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startManualAttendance();
            }
        });

        // Button to end marking and update absent students
        Button endMarkingButton = findViewById(R.id.endMarking);
        endMarkingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endMarkingAndUpdateAbsentees();
            }
        });
    }
    @Override
    public void onBackPressed() {
        // Override the back button to navigate back to the login page
        super.onBackPressed();
        startActivity(new Intent(lechome.this, login.class));
        finish(); // Optional: Finish the current activity to prevent it from showing when pressing back again.
    }

    private void startQRAttendance() {
        // Start QR activity and pass the current subject name
        Intent intent = new Intent(lechome.this, QR.class);
        intent.putExtra("subjectName", currentSubjectName);
        startActivity(intent);
    }

    private void startManualAttendance() {
        // Start manual attendance activity and pass the current subject name
        Intent intent = new Intent(lechome.this, manual.class);
        intent.putExtra("subjectName", currentSubjectName);
        startActivity(intent);
    }

    private void endMarkingAndUpdateAbsentees() {
        // Get current date
        LocalDate currentDate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentDate = LocalDate.now();
        }
        String date = currentDate.toString();

        // Get list of all students
        databaseReference.child("students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                        String regNumber = studentSnapshot.child("registrationNumber").getValue(String.class);

                        // Check if attendance is already marked for this student
                        databaseReference.child("attendance").child(regNumber).child(date).child(currentSubjectName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot attendanceSnapshot) {
                                if (!attendanceSnapshot.exists()) {
                                    // Mark student as absent
                                    databaseReference.child("attendance").child(regNumber).child(date).child(currentSubjectName).setValue("absent")
                                            .addOnSuccessListener(aVoid -> {
                                                // Successfully marked absent
                                                Toast.makeText(lechome.this, "Absent marked for " + regNumber, Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                // Failed to mark absent
                                                Toast.makeText(lechome.this, "Failed to mark absent: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle database error
                                Toast.makeText(lechome.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    // No student details found in the database
                    Toast.makeText(lechome.this, "No student details found in the database", Toast.LENGTH_SHORT).show();
                }
                // After marking all absentees, redirect to login page
                redirectToLogin();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(lechome.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToLogin() {
        // Clear any stored user data if necessary (for example, FirebaseAuth sign-out)
        // FirebaseAuth.getInstance().signOut(); // Uncomment this if using FirebaseAuth

        // Redirect to login page
        Intent intent = new Intent(lechome.this, login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

