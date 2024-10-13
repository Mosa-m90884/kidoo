package com.example.kiddo.Login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.*;

import com.example.kiddo.Controller.FirebaseManager;
import com.example.kiddo.HomeActivity;
import com.example.kiddo.Model.Parent;
import com.example.kiddo.R;
import com.example.kiddo.parent.parentDashboard;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private ProgressBar progressBar;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseManager = new FirebaseManager();

        emailEditText = findViewById(R.id.login_email);
        passwordEditText = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
       progressBar = findViewById(R.id.progressBar2);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // الانتقال إلى الصفحة الرئيسية عند الضغط على الزر
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();

                if (validateInputs(email, password)) {
                    signIn(email, password);
                }
            }
        });
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            return false;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            return false;
        }
        return true;
    }

    private void signIn(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);

        firebaseManager.signIn(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String result = task.getResult(); // نحصل على نوع المستخدم
                        if (result.equals("parent")) {
                            progressBar.setVisibility(View.GONE); // إخفاء شريط التحميل

                            startActivity(new Intent(LoginActivity.this, parentDashboard.class));
                        } else if (result.equals("children")) {
                            progressBar.setVisibility(View.GONE); // إخفاء شريط التحميل

                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        } else {
                            progressBar.setVisibility(View.GONE); // إخفاء شريط التحميل

                            Toast.makeText(LoginActivity.this, "البريد الإلكتروني غير مسجل.", Toast.LENGTH_SHORT).show();
                        }
                        //finish();
                    } else {
                        progressBar.setVisibility(View.GONE); // إخفاء شريط التحميل

                        // إذا فشل تسجيل الدخول
                        Toast.makeText(LoginActivity.this, "فشل تسجيل الدخول: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}