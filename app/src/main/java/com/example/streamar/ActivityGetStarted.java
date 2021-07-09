package com.example.streamar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityGetStarted extends AppCompatActivity {

    private static final String TAG = "ActivityGetStarted";
    private static final int REQUEST_CODE_ICON = 19;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    EditText etDateOfBirth;
    private CircleImageView profileImage;
    private CollectionReference UsersDetailsRef;
    private StorageReference imageRef;
    private RadioGroup rgGender;
    private String email, url, regToken;
    private Uri imageUri;
    private ProgressBar progressBar;
    private TextView progressView;
    private Toast toast;
    private Methods methods;
    private int lYear, lMonth, lDay;
    private Date limit, dob;
    DatePickerDialog datePickerDialog;
    Button termsButton, privacyButton;
    private EditText etFullName, etCellphone, etLocated;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);
        setTitle("Get Started");

        Button create_profile = findViewById(R.id.btn_create_profile_get_started);
        profileImage = findViewById(R.id.image_getting_started);
        etFullName = findViewById(R.id.et_fullname_get_started);
        etCellphone = findViewById(R.id.et_cell_number_get_started);
        etDateOfBirth = findViewById(R.id.et_date_of_birth_get_started);
        etLocated = findViewById(R.id.et_location_get_started);
        LinearLayout layoutImage = findViewById(R.id.image_layout_getting_started);
        rgGender = findViewById(R.id.rg_gender_get_started);
        progressBar = findViewById(R.id.progress_bar_get_started);
        progressView = findViewById(R.id.txt_progress_get_started);
        UsersDetailsRef = db.collection(FinderContract.USER_DETAILS);

        methods = new Methods(this);
        progressBar.setVisibility(View.GONE);
        Calendar c = Calendar.getInstance();
        lYear = c.get(Calendar.YEAR) - 16;
        lMonth = c.get(Calendar.MONTH);
        lDay = c.get(Calendar.DAY_OF_MONTH);
        c.set(Calendar.YEAR, lYear);
        c.set(Calendar.MONTH, lMonth);
        c.set(Calendar.DAY_OF_MONTH, lDay);
        limit = c.getTime();
        etDateOfBirth.setCursorVisible(false);
        etDateOfBirth.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                openCalendar();
            }
        });
        etDateOfBirth.setOnClickListener(v -> {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            openCalendar();
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null)
            email = auth.getCurrentUser().getEmail();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("EditProfile")) {

            if ("yes".equals(intent.getStringExtra("EditProfile"))) {
                UsersDetailsRef.document(email).get().addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot != null) {

                        UserDetails userInfo = documentSnapshot.toObject(UserDetails.class);
                        if (userInfo != null) {
                            etFullName.setText(userInfo.getFullName());
                            etDateOfBirth.setText(userInfo.getDateOfBirth());
                            etCellphone.setText(userInfo.getCellphone());
                            etLocated.setText(userInfo.getLocated());

                            if (userInfo.getGender().equals("Female")) {
                                rgGender.findViewById(R.id.rb_female).performClick();
                            } else {

                                rgGender.findViewById(R.id.rb_male).performClick();
                            }
                            url = userInfo.getImageUrl();
                            Picasso.get().load(url).error(R.drawable.ic_person).into(profileImage);
                        }
                    }
                });
            }
        }

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        regToken = task.getResult().getToken();
                    }
                });

        layoutImage.setOnClickListener(view -> openGallery());

        create_profile.setOnClickListener(view -> CreateUser());
    }

    private void CreateUser() {
        String fullName = etFullName.getText().toString().trim();
        String located = etLocated.getText().toString().trim();
        String dateOfBirth = etDateOfBirth.getText().toString().trim();
        String cellphoneNumber = etCellphone.getText().toString().trim();
        String gender = onRadioButtonClicked(rgGender);

        if (fullName.length() < 6) {
            Toast.makeText(this, "Whoops,Please insert fullname", Toast.LENGTH_SHORT).show();
            etFullName.requestFocus();
            return;
        }

        if (located.length() < 5) {
            Toast.makeText(this, "Whoops,Please insert your Location", Toast.LENGTH_SHORT).show();
            etLocated.requestFocus();
            return;
        }

        if (dateOfBirth.isEmpty()) {
            Toast.makeText(this, "Whoops,Please insert date of birth", Toast.LENGTH_SHORT).show();
            etDateOfBirth.requestFocus();
            return;
        }
        if (dob == null || dob.after(limit)) {
            Toast.makeText(this, "Oops,Please you must be at least 16 years to use Find App", Toast.LENGTH_SHORT).show();
            etDateOfBirth.requestFocus();
            return;
        }

        if (cellphoneNumber.length() < 10) {
            Toast.makeText(this, "Whoops,Please insert 10 digits of cellphone number", Toast.LENGTH_SHORT).show();
            etCellphone.requestFocus();
            return;
        }

        if (gender == null || gender.isEmpty()) {
            Toast.makeText(this, "Whoops,Please select your gender", Toast.LENGTH_SHORT).show();
            return;
        }

        UsersDetailsRef.document(email).set(new UserDetails(fullName, located, gender, dateOfBirth, url, cellphoneNumber, regToken))
                .addOnSuccessListener(aVoid -> uploadImage());
    }

    public String onRadioButtonClicked(RadioGroup view) {
        String answer = null;
        // Is the button now checked?
        RadioButton button = findViewById(view.getCheckedRadioButtonId());
        if (button == null) {
            return null;
        }
        boolean checked = button.isChecked();

        // Check which radio button was clicked
        switch (button.getId()) {
            case R.id.rb_female:
                if (checked)
                    answer = "Female";
                break;
            case R.id.rb_male:
                if (checked)
                    answer = "Male";
                break;
            default:
                answer = null;
        }
        return answer;
    }

    private void openGallery() {
        if (methods.checkIfAlreadyHavePermission()) {
            ActivityCompat.requestPermissions(ActivityGetStarted.this, new String[]{Manifest.permission
                    .WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ICON);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_ICON);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ICON && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_ICON);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_ICON && resultCode ==
                RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Log.d(TAG, "onActivityResult: " + imageUri.toString());
            Glide.with(ActivityGetStarted.this.getApplicationContext()).load(imageUri)
                    .thumbnail(0.2f).error(R.drawable.ic_person).into(profileImage);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadImage() {
        imageRef = FirebaseStorage.getInstance()
                .getReference(email + " Icon" + ".jpg");
        if (imageUri != null) {
            imageRef.putFile(imageUri).addOnProgressListener(taskSnapshot -> {
                double percent = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                percent = RoundOff(percent, 1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar.setProgress((int) percent, true);
                } else {
                    progressBar.setProgress((int) percent);
                }
                progressView.setVisibility(View.VISIBLE);
                String progressText = percent + "%";
                progressView.setText(progressText);

            }).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    if (task.getException() != null)
                        throw task.getException();
                }
                return imageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    url = String.valueOf(downloadUri);
                    UsersDetailsRef.document(email).update("imageUrl", url);
                    progressView.setVisibility(View.GONE);
                    showToast("Hooray,Your profile has been updated succesfully!");
                    Intent i = new Intent(ActivityGetStarted.this, ActivityHomeNavigation.class);
                    startActivity(i);
                    finish();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        showToast("uploading icon failed: "
                                + Objects.requireNonNull(task.getException()).getMessage());
                    }
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            Intent i = new Intent(ActivityGetStarted.this, ActivityHomeNavigation.class);
            startActivity(i);
            finish();
        }
    }

    public void showToast(CharSequence text) {
        if (toast == null)
            toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        else
            toast.setText(text);
        toast.show();
    }

    public Double RoundOff(Double val, int decimals) {
        return new BigDecimal(val.toString()).setScale(decimals, RoundingMode.HALF_UP).doubleValue();
    }
    public void buttonGetStartedPrivacyPolicy(View view) {
        privacyButton = findViewById(R.id.btn_privacyPolicy_get_started);
        privacyButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://drive.google.com/file/d/1mwDNh0nABDQN-VDnZO4UdYxHg332r8Co/view?usp=sharing"));
            startActivity(intent);
        });

    }

    public void onClickTerms(View view) {
        termsButton = findViewById(R.id.btn_terms_get_started);
        termsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://drive.google.com/file/d/1VXRRdoVVMTN9Ke3OZOiCFtvPFUvcKNcG/view?usp=sharing"));
            startActivity(intent);
        });
    }

    private void openCalendar() {
        datePickerDialog = new DatePickerDialog(this, R.style.DialogTheme, (view, year, month, day) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            dob = calendar.getTime();
            String myDob = DateFormat.getDateInstance().format(calendar.getTime());
            etDateOfBirth.setText(myDob);
            InputMethodManager imm = (InputMethodManager) ActivityGetStarted.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etDateOfBirth.getWindowToken(), 0);
            }
            etCellphone.requestFocus();
        }, lYear, lMonth, lDay);
        datePickerDialog.show();
    }
}


