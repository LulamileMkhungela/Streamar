package com.example.streamar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class RegLoginActivity extends AppCompatActivity {

    private static final String TAG = "MyLoginActivity";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;
    private TextView signIn, signUp, signIn_signUp_txt, forgot_password;
    private Button signIn_signUp_btn;
    private EditText etEmail, etPassword;
    private String email, password;
    private RelativeLayout rootLayout;
    private ProgressBar progressBar;
    private Boolean isLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_login);

        signIn = findViewById(R.id.sign_in);
        signUp = findViewById(R.id.sign_up);
        signIn_signUp_txt = findViewById(R.id.signin_signup_txt);
        forgot_password = findViewById(R.id.txt_forgot_password_login);
        signIn_signUp_btn = findViewById(R.id.sign_in_sign_up_btn);
        etEmail = findViewById(R.id.et_email_login);
        etPassword = findViewById(R.id.et_password_login);
        rootLayout = findViewById(R.id.root_login);
        progressBar = findViewById(R.id.progress_bar_login);

        progressBar.setVisibility(View.GONE);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            Intent i = new Intent(RegLoginActivity.this, ActivityHomeNavigation.class);
            startActivity(i);
        }

        signIn.setTextColor(Color.parseColor("#FFFFFF"));
        signIn.setBackgroundColor(Color.parseColor("#42B883"));

        signIn_signUp_btn.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            if (isLogin) {
                email = etEmail.getText().toString().trim();
                password = etPassword.getText().toString().trim();
                Log.d(TAG, "onClick: " + email + "\n" + password);
                if (TextUtils.isEmpty(email)) {
                    Snackbar snackbar = Snackbar
                            .make(rootLayout, "Oops,Please Enter Email address", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Snackbar snackbar = Snackbar
                            .make(rootLayout, "Oops,Please Enter password", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegLoginActivity.this, task -> {
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            progressBar.setVisibility(View.GONE);
                            if (!task.isSuccessful()) {
                                // there was an error
                                if (password.length() < 6) {
                                    etPassword.setError(getString(R.string.minimum_password));
                                } else {
                                    Snackbar snackbar = Snackbar
                                            .make(rootLayout, "Oops,Please make sure your passwords are correct", Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }
                            } else {
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    if (user.getEmail() != null) {
                                        final String email = user.getEmail();
                                        FirebaseInstanceId.getInstance().getInstanceId()
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful() && task1.getResult() != null) {
                                                        String token = task1.getResult().getToken();
                                                        DocumentReference userProfile = db.
                                                                collection(FinderContract.USER_DETAILS).document(email);
                                                        userProfile.update("regToken", token);
                                                    }
                                                });
                                    }
                                    Intent i = new Intent(RegLoginActivity.this, ActivityHomeNavigation.class);
                                    startActivity(i);
                                    Snackbar snackbar = Snackbar
                                            .make(rootLayout, "Hooray,Welcome", Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }
                                finish();
                            }
                        });
            } else {
                SignUpMethod();
            }
        });

        signIn.setOnClickListener(view -> {
            isLogin = true;
            signIn.setTextColor(Color.parseColor("#FFFFFF"));
            signIn.setBackgroundColor(Color.parseColor("#42B883"));
            signUp.setTextColor(Color.parseColor("#E7E7E7"));
            signUp.setBackgroundResource(R.drawable.bordershape);
            signIn_signUp_txt.setText(R.string.login_swipe);
            signIn_signUp_btn.setText(R.string.login_button_text);
            forgot_password.setVisibility(View.VISIBLE);
        });

        signUp.setOnClickListener(view -> {
            isLogin = false;
            signUp.setBackgroundColor(Color.parseColor("#42B883"));
            signIn.setBackgroundResource(R.drawable.bordershape);
            signIn_signUp_txt.setText(R.string.register_swipe);
            signIn_signUp_btn.setText(R.string.register_button_text);
            forgot_password.setVisibility(View.INVISIBLE);
        });

        forgot_password.setOnClickListener(v -> {
            Intent i = new Intent(RegLoginActivity.this, ActivityForgotPassword.class);
            startActivity(i);
        });
    }

    private void SignUpMethod() {
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Snackbar snackbar = Snackbar.make(rootLayout, "Oops,Please Enter Email address", Snackbar.LENGTH_LONG);
            snackbar.show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Snackbar snackbar = Snackbar.make(rootLayout, "Oops,Please Enter  password",
                    Snackbar.LENGTH_LONG);
            snackbar.show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (password.length() < 6) {
            Snackbar snackbar = Snackbar.make(rootLayout, "Oops,Password too short,enter 6 minimum characters",
                    Snackbar.LENGTH_LONG);
            snackbar.show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        //create user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    progressBar.setVisibility(View.GONE);
                    if (!task.isSuccessful()) {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Snackbar snackbar = Snackbar.make(rootLayout,
                                    "Oops,User with this  Email address,already exists", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        } else {
                            Snackbar snackbar = Snackbar.make(rootLayout,
                                    "Sign Up Failed", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    } else {
                        Snackbar snackbar = Snackbar.make(rootLayout, "User Created With This Email Address", Snackbar.LENGTH_LONG);
                        snackbar.show();

                        Intent intent = new Intent(RegLoginActivity.this, ActivityGetStarted.class);
                        startActivity(intent);
                    }
                });
    }
}
