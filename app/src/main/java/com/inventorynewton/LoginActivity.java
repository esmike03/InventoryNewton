package com.inventorynewton;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    EditText edemail, edpass;
    Button buttonLogin;
    TextView buttonText1, forgot;
    ImageButton googleAuth;
    UserDao userDao;
    int RC_SIGN_IN = 20;
    private ProgressDialog progressDialog;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    public static final String SHARED_PREFS = "sharedPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        userDao = new UserDao(this);

        progressDialog = new ProgressDialog(this);
        // Check for location permission
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        googleAuth = findViewById(R.id.imageButton);
        buttonText1 = findViewById(R.id.textView5);

        edemail = findViewById(R.id.emaillogin);
        edpass = findViewById(R.id.inputpassword);
        buttonLogin = findViewById(R.id.button);

        forgot = findViewById(R.id.forgot);
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });


        checkBox();
        check();



        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = edemail.getText().toString().trim(); // <-- trimmed input
                String password = edpass.getText().toString().trim();

                progressDialog.setTitle("Log In");
                progressDialog.setMessage("please wait...");
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.show();

                if (TextUtils.isEmpty(username)) {
                    edemail.setError("Required!");
                    progressDialog.dismiss();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Password is required!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                } else {
                    String hashedPassword = RegisterActivity.hashPassword(password);

                    boolean isValid = userDao.login(username, hashedPassword);
                    if (isValid) {
                        Toast.makeText(LoginActivity.this, "Sign In Successfully!",
                                Toast.LENGTH_SHORT).show();

                        saveLoginStatus(true);
                        progressDialog.dismiss();

                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid username or password",
                                Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }
        });


        String text =  "Don't have any account? Sign Up";
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        ForegroundColorSpan bGreen = new ForegroundColorSpan(Color.GREEN);
        ssb.setSpan(bGreen, 24, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        buttonText1.setText(ssb);

        buttonText1.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }



    private void checkBox() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String check = sharedPreferences.getString("name", "");
        if(check.equals("true")){
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private void check() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private void saveLoginStatus(boolean isLoggedIn) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.apply();
    }

}