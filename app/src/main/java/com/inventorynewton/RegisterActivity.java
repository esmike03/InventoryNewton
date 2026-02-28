package com.inventorynewton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    Button push;
    TextView buttonText1;

    EditText name, email1, pass1, passcon;
    String  userUserName, userPass, userPassCon;

    UserDao userDao;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        progressDialog = new ProgressDialog(this);
        userDao = new UserDao(this);
        push = findViewById(R.id.create);
        buttonText1 = findViewById(R.id.textView9);

        email1 = findViewById(R.id.inputemail);
        pass1 = findViewById(R.id.inputpass);
        passcon = findViewById(R.id.inputpasscfm);
        push = findViewById(R.id.create);

        push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.setTitle("Creating Account");
                progressDialog.setMessage("please wait...");
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.show();
                userUserName = email1.getText().toString().trim();
                userPass = pass1.getText().toString().trim();
                userPassCon = passcon.getText().toString().trim();

                if (TextUtils.isEmpty(userUserName)) {
                    email1.setError("Required!");
                    progressDialog.dismiss();
                } else if (TextUtils.isEmpty(userPass) || TextUtils.isEmpty(userPassCon)) {
                    Toast.makeText(RegisterActivity.this, "Password is empty!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                } else if (!userPass.equals(userPassCon)) {
                    Toast.makeText(RegisterActivity.this, "Password don't match!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                } else {


                    String hashedPassword = hashPassword(userPass);

                    boolean isRegistered = userDao.register(
                            userUserName,
                            hashedPassword
                    );

                    if (isRegistered) {
                        Toast.makeText(RegisterActivity.this,
                                "Account Created", Toast.LENGTH_SHORT).show();

                        progressDialog.dismiss();

                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Username already been used.", Toast.LENGTH_SHORT).show();

                        progressDialog.dismiss();
                    }
                }
            }


        });


        String text = "Already have an account? Sign In";
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        ForegroundColorSpan bGreen = new ForegroundColorSpan(Color.GREEN);
        ssb.setSpan(bGreen, 24, 32, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        buttonText1.setText(ssb);

        buttonText1.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}
