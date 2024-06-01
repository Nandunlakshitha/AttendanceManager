package com.example.attendancemanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.time.LocalDate;
import java.util.List;

public class QR extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private CompoundBarcodeView barcodeView;
    private DatabaseReference databaseReference;
    private String currentSubjectName;
    private LocalDate currentDate;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        barcodeView = findViewById(R.id.barcode_scanner);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Get the subject name passed from the previous activity (lechome)
        currentSubjectName = getIntent().getStringExtra("subjectName");

        // Get the current date
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentDate = LocalDate.now();
        }

        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startBarcodeScanner();
        }
    }

    private void startBarcodeScanner() {
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                // Handle barcode result
                String regNumber = result.getText();
                if (currentSubjectName != null) {
                    // Mark attendance for the current subject
                    markAttendance(regNumber, currentSubjectName);
                } else {
                    Toast.makeText(QR.this, "No subject selected for attendance", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Handle possible result points
            }
        });
    }

    private void giveFeedback() {
        // Vibrate phone
        if (vibrator != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(200);
            }
        }

        // Flash screen (briefly change background color)
        barcodeView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
        barcodeView.postDelayed(() -> barcodeView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent)), 200);
    }

    private void markAttendance(final String regNumber, final String subject) {
        // Retrieve student details from the database
        databaseReference.child("students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean studentExists = false;
                    for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                        String studentRegNumber = studentSnapshot.child("registrationNumber").getValue(String.class);
                        if (studentRegNumber != null && studentRegNumber.equals(regNumber.trim())) {
                            studentExists = true;
                            break;
                        }
                    }
                    if (studentExists) {
                        // Student with the scanned registration number exists
                        markAttendanceForExistingStudent(regNumber, subject);
                    } else {
                        // Scanned registration number not found in the database
                        Toast.makeText(QR.this, "Registration number " + regNumber + " is not registered", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // No student details found in the database
                    Toast.makeText(QR.this, "No student details found in the database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(QR.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markAttendanceForExistingStudent(final String regNumber, final String subject) {
        String date = currentDate.toString();

        // Check if attendance already exists for this registration number, date, and subject
        databaseReference.child("attendance").child(regNumber).child(date).child(subject).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Attendance already marked
                    Toast.makeText(QR.this, "Attendance already marked for today in " + subject, Toast.LENGTH_SHORT).show();
                } else {
                    // Mark attendance
                    databaseReference.child("attendance").child(regNumber).child(date).child(subject).setValue("present")
                            .addOnSuccessListener(aVoid -> {
                                // Successfully marked attendance
                                Toast.makeText(QR.this, "Attendance marked for " + regNumber + " in " + subject, Toast.LENGTH_SHORT).show();
                                giveFeedback(); // Provide feedback only on successful attendance marking
                            })
                            .addOnFailureListener(e -> {
                                // Failed to mark attendance
                                Toast.makeText(QR.this, "Failed to mark attendance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(QR.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBarcodeScanner();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        barcodeView.destroyDrawingCache();
    }
}
