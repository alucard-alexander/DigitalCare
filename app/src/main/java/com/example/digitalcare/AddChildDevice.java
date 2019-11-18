package com.example.digitalcare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ComponentActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.digitalcare.Bean.ChildDetails;
import com.example.digitalcare.ConstantsFile.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AddChildDevice extends AppCompatActivity {

    private Uri imageUri = null;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private GeoPoint geoPoint;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    private boolean gpsEnabled = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child_device);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void chooseFile(View view) {
        if (ContextCompat.checkSelfPermission(AddChildDevice.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        } else {
            ActivityCompat.requestPermissions(AddChildDevice.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.PERMISSION_REQUESET_READ_EXTERNAL_STORAGE);
        }
    }


    //File choosing

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
        } else if (requestCode == Constants.PERMISSIONS_REQUEST_ENABLE_GPS) {

            LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!enabled) {

                Toast.makeText(this, "Please Enable GPS", Toast.LENGTH_SHORT).show();
                Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(enableGpsIntent, Constants.PERMISSIONS_REQUEST_ENABLE_GPS);
            } else {
                gpsEnabled = true;
            }
        } else {
            Toast.makeText(this, "NON of Permission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.PERMISSION_REQUESET_READ_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        } else if (requestCode == Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Check if enabled and if not send user to the GPS settings
            if (!enabled) {


                Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(enableGpsIntent, Constants.PERMISSIONS_REQUEST_ENABLE_GPS);
                Toast.makeText(this, "Enable GPS inorder to use the app", Toast.LENGTH_SHORT).show();

            } else {
                gpsEnabled = true;
            }


        } else if (requestCode == Constants.PERMISSIONS_REQUEST_ENABLE_GPS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "GPS enabled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Read Permission should be granted inorder to upload File", Toast.LENGTH_SHORT).show();
        }
    }
    //file choosing code ends here

    //Granting permission

    public void grantLocationGPSPermission(View view) {


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!enabled) {
                Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(enableGpsIntent, Constants.PERMISSIONS_REQUEST_ENABLE_GPS);
            } else {
                gpsEnabled = true;
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    public void saveData(View view) {
        try {
            EditText ed5 = findViewById(R.id.editText5);
            if (ed5.getText().toString() == null) {
                Toast.makeText(this, "Please enter child's name", Toast.LENGTH_SHORT).show();
            } else if (!gpsEnabled) {
                Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show();
            } else if (imageUri == null) {
                Toast.makeText(this, "Please select profile image and enable ", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    progressDialog = new ProgressDialog(this);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setTitle("Updating Profile...Plz be patience");
                    progressDialog.setProgress(0);
                    progressDialog.show();

                    progressDialog.setCancelable(false);

                    fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful()) {
                                Location location = task.getResult();

                                Log.d("latt", String.valueOf(location.getLatitude()));
                                //Log.d("latt", String.valueOf(geoPoint.getLatitude()));
                                geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                                progressDialog.setProgress(10);
                            }
                        }
                    });

                    //firestore

                    ChildDetails childUserVar = new ChildDetails(ed5.getText().toString(), mAuth.getUid(), geoPoint);
                    db.collection("child")
                            .add(childUserVar)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    //Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                    //Toast.makeText(AddChildDevice.this, documentReference.getId(), Toast.LENGTH_SHORT).show();
                                    progressDialog.setProgress(50);
                                    final String documentID = documentReference.getId();
                                    Uri file = imageUri;
                                    String arg = "childDp/" + documentReference.getId() + ".jpg";
                                    final StorageReference riversRef = mStorageRef.child(arg);
                                    riversRef.putFile(file)
                                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    // Get a URL to the uploaded content
                                                    // taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                                    progressDialog.setProgress(70);
                                                    Task<Uri> downloadUrl1 = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                                    downloadUrl1.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            progressDialog.setProgress(90);
                                                            Toast.makeText(AddChildDevice.this, uri.toString(), Toast.LENGTH_SHORT).show();
                                                            Map<String, Object> data = new HashMap<>();
                                                            data.put("dpDownloadUrl", uri.toString());
                                                            progressDialog.setProgress(95);
                                                            db.collection("child").document(documentID)
                                                                    .set(data, SetOptions.merge())
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                                            progressDialog.hide();
                                                                            Toast.makeText(AddChildDevice.this, "Success heeerrreeeeeeeeeee", Toast.LENGTH_SHORT).show();

                                                                            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE);
                                                                            SharedPreferences.Editor editor = sharedPreferences.edit();



                                                                            editor.putString(Constants.TYPE, "CHILD");
                                                                            //editor.putString(Constants.ID,mAuth.getUid());
                                                                            editor.putString(Constants.CHILD_ID, documentID);
                                                                            editor.apply();
                                                                            Intent i = new Intent(AddChildDevice.this, MapsActivity.class);
                                                                            startActivity(i);
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            progressDialog.hide();
                                                                            Toast.makeText(AddChildDevice.this, "An error occured check your internet connection", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });

                                                        }
                                                    });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    progressDialog.hide();
                                                    Toast.makeText(AddChildDevice.this, "An error occured check your internet connection", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.hide();
                                    Toast.makeText(AddChildDevice.this, "An error occured check your internet connection", Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (Exception e) {
                    Log.d("errorrrrr", e.getMessage());
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void resetDelete(View view) {
        EditText ed5 = findViewById(R.id.editText5);
        ed5.setText("");
        imageUri = null;
        Toast.makeText(this, "Image and name has been reset", Toast.LENGTH_SHORT).show();
    }


}
