package com.example.kiddo.parent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kiddo.Controller.FirebaseManager;
import com.example.kiddo.R;
import com.example.kiddo.Tasks.ArrangeBedActivity;


public class parentDashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_parent_dashboard);

        Button supervision = findViewById(R.id.supervision);
        Button signOut = findViewById(R.id.parentSignout);

        supervision.setOnClickListener(v -> {
            Intent intent = new Intent(this, childrenActivity.class);
            startActivity(intent);
            // يمكنك إضافة المنطق لتنفيذ المهمة هنا
        });

}}