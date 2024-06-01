package com.example.attendancemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class login extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;

    // Dummy admin credentials for demonstration
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leclogin);

        Button btn3 = (Button) findViewById(R.id.button3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(login.this, Viewstatic.class));
            }
        });
        // Get references to EditText fields and login button
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Validate input fields
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(login.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Authenticate the user
                if (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
                    // Admin login
                    startActivity(new Intent(login.this, adddetails.class));
                    finish(); // Close the current activity
                } else {
                    // Lecturer login
                    authenticateLecturer(username, password);
                }
            }
        });
    }

    private void authenticateLecturer(String username, String password) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("subjects");

        // Query the subjects where username matches
        Query query = databaseReference.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isAuthenticated = false;
                for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve subject data
                    String subjectName = subjectSnapshot.child("subjectName").getValue(String.class);
                    String subjectUsername = subjectSnapshot.child("username").getValue(String.class);
                    String subjectPassword = subjectSnapshot.child("password").getValue(String.class);

                    // Check if the retrieved password matches the lecturer's password
                    if (subjectUsername != null && subjectUsername.equals(username) && subjectPassword != null && subjectPassword.equals(password)) {
                        // Username and password match, lecturer authenticated
                        isAuthenticated = true;

                        // Start LecHomeActivity and pass the subject name
                        Intent intent = new Intent(login.this, lechome.class);
                        intent.putExtra("subjectName", subjectName);
                        startActivity(intent);
                        finish(); // Close the current activity
                        break; // Exit loop after successful authentication
                    }
                }

                if (!isAuthenticated) {
                    // Show error message if lecturer is not authenticated
                    Toast.makeText(getApplicationContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
