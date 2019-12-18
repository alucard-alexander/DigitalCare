package com.example.digitalcare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.digitalcare.ConstantsFile.Constants;
import com.example.digitalcare.Services.LocationService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private EditText email, pass;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private RadioGroup radioGroup;
    private RadioButton radioButton1, radioButton2;
    private String emailStr, passStr;
    public TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.editText7);
        pass = findViewById(R.id.editText9);
        progressDialog = new ProgressDialog(this);
        radioGroup = findViewById(R.id.radioGroup);
        radioButton1 = findViewById(R.id.radioButton);
        radioButton2 = findViewById(R.id.radioButton2);
        textView = findViewById(R.id.textView2);
        mAuth = FirebaseAuth.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE);
        String type = sharedPreferences.getString(Constants.TYPE, "");
        String childID = sharedPreferences.getString(Constants.CHILD_ID, "");


        if (type.equals("CHILD") && !childID.equals("") && mAuth != null) {
            Intent i = new Intent(this, LockedScreenSignOut.class);
            Toast.makeText(this, type, Toast.LENGTH_SHORT).show();
            startActivity(i);
        } else if (type.equals("PARENT") && mAuth.getUid() != null) {
            Intent i = new Intent(this, Main2Activity.class);
            Toast.makeText(this, type, Toast.LENGTH_SHORT).show();
            startActivity(i);
        }

    }


    public void loginUser(View view) {

        emailStr = email.getText().toString();
        passStr = pass.getText().toString();
        progressDialog.setMessage("Trying to login");
        progressDialog.setCancelable(false);
        progressDialog.show();
        if (!emailStr.isEmpty()) {
            if (!passStr.isEmpty()) {
                mAuth.signInWithEmailAndPassword(emailStr, passStr)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Intent i;
                                if (task.isSuccessful()) {
                                    progressDialog.hide();
                                    SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();

                                    if (radioButton1.isChecked()) {
                                        i = new Intent(Login.this, Main2Activity.class);
                                        progressDialog.hide();
                                        editor.putString(Constants.TYPE, "PARENT");
                                        editor.apply();
                                        startActivity(i);
                                    } else if (radioButton2.isChecked()) {
                                        editor.putString(Constants.TYPE, "CHILD");
                                        //editor.putString(Constants.ID,mAuth.getUid());

                                        editor.apply();
                                        i = new Intent(Login.this, ChildHome.class);
                                        progressDialog.hide();
                                        startActivity(i);
                                        //Toast.makeText(Login.this, "Child", Toast.LENGTH_SHORT).show();
                                    } else {
                                        progressDialog.hide();
                                        Toast.makeText(Login.this, "Please select Type", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    progressDialog.hide();
                                    Toast.makeText(Login.this, "Sign in failed, Try registered user Name and password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                progressDialog.hide();
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            }
        } else {
            progressDialog.hide();
            Toast.makeText(this, "Please Enter registered email", Toast.LENGTH_SHORT).show();
        }

    }

    public void registerPage(View view) {
        Intent i = new Intent(this, Registration.class);
        startActivity(i);
    }

    public void forgotPassword(View view) {
        Intent i = new Intent(this, ForgotPassword.class);
        startActivity(i);
    }

    public void reset1(View view) {
        email.setText("");
        pass.setText("");
    }
}
