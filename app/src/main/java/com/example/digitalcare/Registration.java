package com.example.digitalcare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.digitalcare.ConstantsFile.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Registration extends AppCompatActivity {
    private EditText name, email, pass, mobile,rePass;
    private Intent loginPage;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private String userID;
    private Uri imageUri = null;
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{5,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();
        name = findViewById(R.id.editText);
        email = findViewById(R.id.editText1);
        pass = findViewById(R.id.editText2);
        rePass = findViewById(R.id.editText3);
        mobile = findViewById(R.id.editText4);
        progressDialog = new ProgressDialog(this);
        loginPage = new Intent(this, Login.class);
    }
    public void registerUser(View view) {
        if (!isValidateFields()){
            return;
        }
        if (imageUri == null){
            Toast.makeText(this, "Choose a photo as DP for your account", Toast.LENGTH_SHORT).show();
            return;
        }

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
                                progressDialog.hide();
                                Toast.makeText(Registration.this, "Email already exists", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public boolean isValidateFields(){
        //Checking Empty

        String nameVar,emailVar,passVar,rePassVar,mobileVar;
        nameVar = name.getText().toString();
        emailVar = email.getText().toString();
        passVar = pass.getText().toString();
        rePassVar = rePass.getText().toString();
        mobileVar = mobile.getText().toString();

        if (nameVar.isEmpty()){
            name.setError("Name Field cannot be empty");
            return false;
        }
        if (emailVar.isEmpty()){
            email.setError("Email Field cannot be empty");
            return false;
        }
        if (passVar.isEmpty()){
            pass.setError("Password Field cannot be empty");
            return false;
        }
        if (rePassVar.isEmpty()){
            rePass.setError("Re-type the password");
            return false;
        }
        if (mobileVar.isEmpty()){
            mobile.setError("Mobile Number should be provided");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailVar).matches()){
            email.setError("Email is invalid");
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(passVar).matches()){
            pass.setError("Password is not strong, Enter atleast 1 Upper, 1 Lower and 1 special character");
            return false;
        }

        if (!passVar.equals(rePassVar)){
            rePass.setError("Retype password is not same");
            return false;
        }

        if (mobile.length() < 10){
            mobile.setError("Mobile number should have 10 digits");
            return false;
        }



        return true;
    }


    public void signIn() {
        mAuth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //mAuth.signOut();
                            uploadImage(userID);
                            progressDialog.setProgress(40);
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

    public void uploadImage(final String id){
        Uri file = imageUri;
        String arg = "parentDp/" + id + ".jpg";
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final StorageReference riversRef = mStorageRef.child(arg);
        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        // taskSnapshot.getMetadata().getReference().getDownloadUrl()
                        progressDialog.setProgress(65);
                        Task<Uri> downloadUrl1 = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        downloadUrl1.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                progressDialog.setProgress(80);
                                //Toast.makeText(AddChildDevice.this, uri.toString(), Toast.LENGTH_SHORT).show();
                                //Map<String, Object> data = new HashMap<>();
                                //data.put("dpDownloadUrl", uri.toString());
                                progressDialog.setProgress(90);
                                uploadData(userID,uri.toString());


                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressDialog.hide();
                        Toast.makeText(Registration.this, "An error occured check your internet connection", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void uploadData(String userID,String downloadURL) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user1 = new HashMap<>();
        user1.put("name", name.getText().toString());
        user1.put("mobile", mobile.getText().toString());
        user1.put("dpDownloadUrl",downloadURL);
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

    public void chooseFile123(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.PERMISSION_REQUESET_READ_EXTERNAL_STORAGE);
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.SELECT_FILE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.SELECT_FILE_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Toast.makeText(this, imageUri.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.PERMISSION_REQUESET_READ_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        }
    }


    public void cancel1(View view)
    {
        Intent i = new Intent(this,Login.class);
        startActivity(i);
    }

}
