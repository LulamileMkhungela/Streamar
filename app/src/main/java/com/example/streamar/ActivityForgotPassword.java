package com.example.streamar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class ActivityForgotPassword extends AppCompatActivity {

    TextView backToLoginBtn, submit_area;
    private EditText inputEmail;
    private LinearLayout rootLayout;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        backToLoginBtn = findViewById(R.id.btn_backToLogin_forget_password);
        submit_area = findViewById(R.id.btn_submit_forget_password);
        inputEmail = findViewById(R.id.et_email_forgot_password);
        rootLayout = findViewById(R.id.root_forgot_password);
        progressBar = findViewById(R.id.progress_bar_forgot_password);

        auth = FirebaseAuth.getInstance();

        backToLoginBtn.setOnClickListener(v -> onBackPressed());

        submit_area.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Snackbar snackbar = Snackbar.make(rootLayout, "Oops,Please Enter Registered  Email address", Snackbar.LENGTH_LONG);
                snackbar.show();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Snackbar snackbar = Snackbar.make(rootLayout,
                                    "Hooray,We have sent you instructions to reset your password, Please check your email to reset your password!",
                                    Snackbar.LENGTH_LONG);
                            snackbar.show();
                            new Handler().postDelayed(ActivityForgotPassword.this::onBackPressed,
                                    2000);
                        } else {
                            Snackbar snackbar = Snackbar.make(rootLayout,
                                    "Oops that Failed to reset your password, Please try again", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                        progressBar.setVisibility(View.GONE);
                    });
        });
    }
}
