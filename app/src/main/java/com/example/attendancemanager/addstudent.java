package com.example.attendancemanager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class addstudent extends AppCompatActivity {

    public class Student {
        private String studentId;
        private String studentName;
        private String registrationNumber;

        // Constructor
        public Student(String studentId, String studentName, String registrationNumber) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.registrationNumber = registrationNumber;
        }

        // Getter methods
        public String getStudentId() {
            return studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getRegistrationNumber() {
            return registrationNumber;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addstudent);

        // Get references to EditText fields
        EditText editTextStudentName = findViewById(R.id.editTextStudentName);
        EditText editTextRegistrationNumber = findViewById(R.id.editTextRegistrationNumber);

        // Get a reference to the "Add Student" button
        Button buttonAddStudent = findViewById(R.id.buttonAddStudent);
        buttonAddStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve text from EditText fields
                String studentName = editTextStudentName.getText().toString().trim();
                String registrationNumber = editTextRegistrationNumber.getText().toString().trim();

                // Validate input
                if (studentName.isEmpty() || registrationNumber.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Write data to Firebase Realtime Database
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("students");
                String studentId = databaseReference.push().getKey(); // Generate a unique key for the student

                if (studentId != null) {
                    Student student = new Student(studentId, studentName, registrationNumber);
                    databaseReference.child(studentId).setValue(student);

                    // Clear EditText fields after adding student
                    editTextStudentName.setText("");
                    editTextRegistrationNumber.setText("");

                    // Show a success message
                    Toast.makeText(getApplicationContext(), "Student added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to add student", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
