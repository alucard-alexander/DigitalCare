package com.example.digitalcare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private EditText email,pass;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    String emailStr,passStr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email =  findViewById(R.id.editText7);
        pass = findViewById(R.id.editText9);
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        /*if (mAuth.getCurrentUser().getEmail() != null) {
            FirebaseUser user= mAuth.getCurrentUser();
            Toast.makeText(this, user.getEmail(), Toast.LENGTH_SHORT).show();

        }*/
        if (mAuth.getUid() != null){
            Intent i = new Intent(Login.this,Main2Activity.class);
            startActivity(i);
        }
        Toast.makeText(this, mAuth.getUid(), Toast.LENGTH_SHORT).show();
    }

    public void loginUser(View view){
        progressDialog.setMessage("Trying to login");
        progressDialog.show();
        if (!email.getText().toString().isEmpty()){
            if (!pass.getText().toString().isEmpty()){
                //Toast.makeText(this, "Working", Toast.LENGTH_SHORT).show();
                /*SharedPreferences sharedPreferences = getSharedPreferences(MyConstants.sharedPreferencekey,MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();*/
                //SaveSha
                emailStr = email.getText().toString();
                passStr = pass.getText().toString();
                mAuth.signInWithEmailAndPassword(emailStr, passStr)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    //Log.d(TAG, "signInWithEmail:success");
                                    /*FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);*/
                                    //Toast.makeText(Login.this, "Signed in", Toast.LENGTH_SHORT).show();

                                    Intent i = new Intent(Login.this,Main2Activity.class);
                                    startActivity(i);
                                    progressDialog.hide();
                                    Toast.makeText(Login.this, "Success", Toast.LENGTH_SHORT).show();

                                } else {
                                    // If sign in fails, display a message to the user.
                                    /*Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);*/
                                    progressDialog.hide();
                                    Toast.makeText(Login.this, "Sign in failed, Try registered user Name and password", Toast.LENGTH_SHORT).show();
                                }

                                // ...
                            }
                        });

            }else {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Please Enter registered email", Toast.LENGTH_SHORT).show();
        }

    }
}
