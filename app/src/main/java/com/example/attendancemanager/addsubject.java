package com.example.attendancemanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class addsubject extends AppCompatActivity {
    public class Subject {
        private String subjectId;
        private String subjectName;
        private String lecturerName;
        private String username;
        private String password;

        // Constructor
        public Subject(String subjectId, String subjectName, String lecturerName, String username, String password) {
            this.subjectId = subjectId;
            this.subjectName = subjectName;
            this.lecturerName = lecturerName;
            this.username = username;
            this.password = password;
        }

        // Getter methods
        public String getSubjectId() {
            return subjectId;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public String getLecturerName() {
            return lecturerName;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addsubject);

        // Get references to EditText fields
        EditText editTextSubjectName = findViewById(R.id.editTextSubjectName);
        EditText editTextLecturerName = findViewById(R.id.editTextLecturerName);
        EditText editTextUsername = findViewById(R.id.editTextUsername);
        EditText editTextPassword = findViewById(R.id.editTextPassword);

        // Get a reference to the "Add Subject" button
        Button buttonAddSubject = findViewById(R.id.buttonAddSubject);
        buttonAddSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve text from EditText fields
                String subjectName = editTextSubjectName.getText().toString().trim();
                String lecturerName = editTextLecturerName.getText().toString().trim();
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Validate that all fields are filled
                if (subjectName.isEmpty() || lecturerName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    // Show an error message if any field is empty
                    Toast.makeText(getApplicationContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Write data to Firebase Realtime Database
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("subjects");
                    String subjectId = databaseReference.push().getKey(); // Generate a unique key for the subject
                    Subject subject = new Subject(subjectId, subjectName, lecturerName, username, password);
                    databaseReference.child(subjectId).setValue(subject);

                    // Clear the EditText fields after adding the subject
                    editTextSubjectName.setText("");
                    editTextLecturerName.setText("");
                    editTextUsername.setText("");
                    editTextPassword.setText("");

                    // Show a success message
                    Toast.makeText(getApplicationContext(), "Subject added successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
