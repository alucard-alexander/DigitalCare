package com.example.digitalcare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.digitalcare.ConstantsFile.Constants;
import com.example.digitalcare.Services.LocationService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class LockedScreenSignOut extends AppCompatActivity {

    private Intent serviceIntent;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locked_screen_sign_out);
        startLocationService();


        /*SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.TYPE,"");
        editor.putString(Constants.CHILD_ID,"");
        editor.apply();

        Intent i = new Intent(this,Login.class);
        startActivity(i);*/

    }


    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            serviceIntent  = new Intent(this, LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                LockedScreenSignOut.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }


    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.digitalcare.Services.LocationService".equals(service.service.getClassName())) {
                return true;
            }
        }
        //Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    public void checkPassword(View view){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing out this device ");
        progressDialog.setCancelable(false);
        progressDialog.show();
        EditText editText = findViewById(R.id.editText6);
        String pass = editText.getText().toString();
        if (pass.isEmpty()){
            Toast.makeText(this, "Please Enter your password", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            FirebaseAuth mAuth;

            mAuth = FirebaseAuth.getInstance();
            //mAuth.signInWithEmailAndPassword(mAuth.getCurrentUser().getEmail(), pass);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            AuthCredential credential = EmailAuthProvider.getCredential(mAuth.getCurrentUser().getEmail(),pass);
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                //Toast.makeText(LockedScreenSignOut.this, "Working", Toast.LENGTH_SHORT).show();
                                signout();
                            }else{
                                progressDialog.hide();
                                Toast.makeText(LockedScreenSignOut.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            //Toast.makeText(this, String.valueOf(user.getProviderId()), Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }



    }


    public void signout() {

        if (isLocationServiceRunning()) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                //LockedScreenSignOut.this.stopService(serviceIntent);
                getApplicationContext().stopService(serviceIntent);

            } else {

                getApplicationContext().stopService(serviceIntent);
                //stopService(serviceIntent);
            }



        }


        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String childID = sharedPreferences.getString(Constants.CHILD_ID,"");

        db.collection("child").document(childID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

        db.collection("Allowed_Markers").document(childID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String name = "childDp/"+childID+".jpg";
        StorageReference desertRef = storageRef.child(name);

        desertRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(LockedScreenSignOut.this, "Deleted your photo", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LockedScreenSignOut.this, "Failed to delete your photo", Toast.LENGTH_SHORT).show();
                    }
                });

        //StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://digitalcare-d88ac.appspot.com/childDp/ZVnwAljAjCrJ27BM1IRn.jpg");

        Toast.makeText(this, "Successfully deleted", Toast.LENGTH_SHORT).show();

        editor.putString(Constants.TYPE,"");
        editor.putString(Constants.CHILD_ID,"");
        editor.apply();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();;
        mAuth.signOut();

        Intent i = new Intent(this,Login.class);
        startActivity(i);


    }

}
