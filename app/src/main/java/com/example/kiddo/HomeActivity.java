package com.example.kiddo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kiddo.Controller.FirebaseManager;
import com.example.kiddo.Tasks.ArrangeBedActivity;
import com.example.kiddo.Tasks.ReadHadithActivity;
import com.example.kiddo.Tasks.ReadQuranActivity;

public class HomeActivity extends AppCompatActivity {
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // إخفاء AppBar
        }

        Button buttonArrangeBed = findViewById(R.id.buttonArrangeBed);
        Button buttonReadQuran = findViewById(R.id.buttonReadQuran);
        Button buttonReadHadith = findViewById(R.id.buttonReadHadith);
        Button buttonArrangeCloset = findViewById(R.id.buttonArrangeCloset);
        TextView textpoint = findViewById(R.id.textPoints);
        firebaseManager = new FirebaseManager();
        String childId =  firebaseManager.getCurrentUserId();; // استبدل هذا بمعرف الطفل الصحيح
        // استرجاع النقاط
        firebaseManager.getChildPoints(childId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Long points = task.getResult();
textpoint.setText("لديك"+points+"نقطة");
                        // يمكنك تحديث واجهة المستخدم هنا بناءً على قيمة النقاط
                    }
                });
        buttonArrangeBed.setOnClickListener(v -> {
            Toast.makeText(this, "تم اختيار: ترتيب السرير", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ArrangeBedActivity.class);
            startActivity(intent);
            // يمكنك إضافة المنطق لتنفيذ المهمة هنا
        });

        buttonReadQuran.setOnClickListener(v -> {
            Toast.makeText(this, "تم اختيار: قراءة القرآن", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ReadQuranActivity.class);
            startActivity(intent);
            // يمكنك إضافة المنطق لتنفيذ المهمة هنا
        });

        buttonReadHadith.setOnClickListener(v -> {
            Toast.makeText(this, "تم اختيار: قراءة الحديث الشريف", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ReadHadithActivity.class);
            startActivity(intent);

            // يمكنك إضافة المنطق لتنفيذ المهمة هنا
        });

        buttonArrangeCloset.setOnClickListener(v -> {
            Toast.makeText(this, "تم اختيار: ترتيب الدولاب", Toast.LENGTH_SHORT).show();
            // يمكنك إضافة المنطق لتنفيذ المهمة هنا
        });
    }

}