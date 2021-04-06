package com.example.ecommerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ecommerce.Model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText inputPhoneNumber;
    private EditText inputPassword;
    private Button loginButton;
    private ProgressDialog loadingBar;

    private String parentDbName = "Users";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputPhoneNumber = (EditText) findViewById(R.id.login_phone_number_input);
        inputPassword = (EditText) findViewById(R.id.login_password_input);
        loginButton = (Button) findViewById(R.id.login_btn);
        loadingBar = new ProgressDialog(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String phone = inputPhoneNumber.getText().toString();
        String password = inputPassword.getText().toString();

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please write your phone", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please write your password", Toast.LENGTH_LONG).show();
        } else {
            loadingBar.setTitle("Login Account");
            loadingBar.setMessage("Please wait, while we are checking the credentials");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            AllowAccessToAccount(phone, password);
        }
    }

    private void AllowAccessToAccount(String phone, String password) {
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child(parentDbName).child(phone).exists()){

                    Users usersData = snapshot.child(parentDbName).child(phone).getValue(Users.class);

                    if (usersData.getPhone().equals(phone)){
                        if (usersData.getPassword().equals(password)){
                            Toast.makeText(LoginActivity.this, "logged successfully", Toast.LENGTH_SHORT);
                            loadingBar.dismiss();

                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                        }
//                        else{
//                            loadingBar.dismiss();
//                            Toast.makeText(LoginActivity.this,"Bad credentials", Toast.LENGTH_SHORT);
//                        }
                    }
//                    else{
//                        loadingBar.dismiss();
//                        Toast.makeText(LoginActivity.this,"Bad credentials", Toast.LENGTH_SHORT);
//                    }
                }
                else{
                    Toast.makeText(LoginActivity.this,"Account with this " + phone + "number do not exist", Toast.LENGTH_SHORT);
                    loadingBar.dismiss();
                    Toast.makeText(LoginActivity.this,"Bad credentials", Toast.LENGTH_SHORT);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}