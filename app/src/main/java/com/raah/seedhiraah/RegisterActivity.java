package com.raah.seedhiraah;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.raah.seedhiraah.databinding.ActivityRegisterBinding;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    FirebaseFirestore firebaseFirestore;
    private ActivityRegisterBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String name, email, password, cPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        Window window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.background));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        contants();

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        binding.signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void contants() {

        binding.nameEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.nameEt.setHintTextColor(getResources().getColor(R.color.base));
                    binding.nameEt.setTextColor(getResources().getColor(R.color.base));
                    binding.nameEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.base)));
                    binding.nameEt.setBackground(getResources().getDrawable(R.drawable.selected_edittext_background));
                } else {
                    binding.nameEt.setHintTextColor(getResources().getColor(R.color.unselected));
                    binding.nameEt.setTextColor(getResources().getColor(R.color.unselected));
                    binding.nameEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.unselected)));
                    binding.nameEt.setBackground(getResources().getDrawable(R.drawable.unselected_edittext_background));
                }
            }
        });

        binding.emailEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.emailEt.setHintTextColor(getResources().getColor(R.color.base));
                    binding.emailEt.setTextColor(getResources().getColor(R.color.base));
                    binding.emailEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.base)));
                    binding.emailEt.setBackground(getResources().getDrawable(R.drawable.selected_edittext_background));
                } else {
                    binding.emailEt.setHintTextColor(getResources().getColor(R.color.unselected));
                    binding.emailEt.setTextColor(getResources().getColor(R.color.unselected));
                    binding.emailEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.unselected)));
                    binding.emailEt.setBackground(getResources().getDrawable(R.drawable.unselected_edittext_background));
                }
            }
        });

        binding.passwordEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.passwordEt.setHintTextColor(getResources().getColor(R.color.base));
                    binding.passwordEt.setTextColor(getResources().getColor(R.color.base));
                    binding.passwordEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.base)));
                    binding.passwordEt.setBackground(getResources().getDrawable(R.drawable.selected_edittext_background));
                } else {
                    binding.passwordEt.setHintTextColor(getResources().getColor(R.color.unselected));
                    binding.passwordEt.setTextColor(getResources().getColor(R.color.unselected));
                    binding.passwordEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.unselected)));
                    binding.passwordEt.setBackground(getResources().getDrawable(R.drawable.unselected_edittext_background));
                }
            }
        });

        binding.cPasswordEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.cPasswordEt.setHintTextColor(getResources().getColor(R.color.base));
                    binding.cPasswordEt.setTextColor(getResources().getColor(R.color.base));
                    binding.cPasswordEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.base)));
                    binding.cPasswordEt.setBackground(getResources().getDrawable(R.drawable.selected_edittext_background));
                } else {
                    binding.cPasswordEt.setHintTextColor(getResources().getColor(R.color.unselected));
                    binding.cPasswordEt.setTextColor(getResources().getColor(R.color.unselected));
                    binding.cPasswordEt.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.unselected)));
                    binding.cPasswordEt.setBackground(getResources().getDrawable(R.drawable.unselected_edittext_background));
                }
            }
        });
    }

    private void validateData() {

        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        cPassword = binding.cPasswordEt.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter password!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cPassword)) {
            Toast.makeText(this, "Confirm password!", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(cPassword)) {
            Toast.makeText(this, "Password doesn't match!", Toast.LENGTH_SHORT).show();
        } else {
            createUserAccount();
        }
    }

    private void createUserAccount() {
        progressDialog.setMessage("Creating your account");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                updateUserInfo();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Saving user info");

        long timestamp = System.currentTimeMillis();
        String uid = firebaseAuth.getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("email", email);
        user.put("name", name);
        user.put("password", password);
        user.put("profileImage", "");
        user.put("timestamp", timestamp);

        firebaseFirestore.collection("Users").document(uid).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RegisterActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                firebaseAuth.signOut();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}