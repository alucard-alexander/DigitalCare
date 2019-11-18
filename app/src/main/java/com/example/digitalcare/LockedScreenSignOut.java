package com.example.digitalcare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.digitalcare.ConstantsFile.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class LockedScreenSignOut extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locked_screen_sign_out);
    }

    public void signout(View view){

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
        /*StorageReference storage;
        storage = FirebaseStorage.getInstance().getReference();*/
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String name = "childDP/"+childID+".jpg";
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

        Toast.makeText(this, "Successfully deleted", Toast.LENGTH_SHORT).show();

        editor.putString(Constants.TYPE,"");
        editor.putString(Constants.CHILD_ID,"");
        editor.apply();

        Intent i = new Intent(this,Login.class);
        startActivity(i);
    }

}
