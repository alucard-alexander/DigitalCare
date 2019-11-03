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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {
    private EditText name, email, pass, mobile;
    private Intent loginPage;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();
        name = findViewById(R.id.editText);
        email = findViewById(R.id.editText1);
        pass = findViewById(R.id.editText2);
        mobile = findViewById(R.id.editText4);
        progressDialog = new ProgressDialog(this);
        loginPage = new Intent(this, Login.class);
    }
    public void registerUser(View view) {
        progressDialog.setMessage("Registering");
        progressDialog.show();
        try {
            mAuth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                //uploadData();
                                signIn();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(Registration.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                progressDialog.hide();
                            }
                        }
                    });
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public void signIn() {
        mAuth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //mAuth.signOut();
                            uploadData(userID);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithEmail:failure", task.getException());
                            //Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                            Toast.makeText(Registration.this, "Couldn't sign in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void uploadData(String userID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user1 = new HashMap<>();
        user1.put("name", name.getText().toString());
        user1.put("mobile", mobile.getText().toString());
        db.collection("person").document(userID).set(user1)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Registration.this, "SuccessFully Registered", Toast.LENGTH_SHORT).show();
                        progressDialog.hide();
                        mAuth.signOut();
                        startActivity(loginPage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Registration.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
