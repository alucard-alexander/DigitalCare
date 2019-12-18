package com.example.digitalcare.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;


import com.example.digitalcare.CheckForLocationUpdates;
import com.example.digitalcare.ConstantsFile.Constants;
import com.example.digitalcare.Main2Activity;
import com.example.digitalcare.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class LocationService extends Service {

    private static final String TAG = "LocationService";


    private FusedLocationProviderClient mFusedLocationClient;
    private final static long UPDATE_INTERVAL = 4 * 1000;  /* 4 secs */
    private final static long FASTEST_INTERVAL = 2000; /* 2 sec */

    private LocationRequest mLocationRequestHighAccuracy;
    private LocationCallback locationCallback;

    private final int SEND_SMS_PERMISSION_REQUEST_CODE = 555;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentText("").build();

            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE);

        getLocation();
        return START_NOT_STICKY;
    }

    private void getChildLocation() {
        //run();
        //Looper.myLooper();

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("child")
                .whereEqualTo("p_id", FirebaseAuth.getInstance().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (final QueryDocumentSnapshot document1 : task.getResult()) {
                                //Log.d(TAG, document.getId() + " => " + document.getData());
                                //Log.d("IIDDDD", document.getId());
                                final QueryDocumentSnapshot documentInside = document1;


                                break;
                            }
                        } else {
                            //Log.w(TAG, "Error getting documents.", task.getException());
                        }
                        /*mLocationRequestHighAccuracy = new LocationRequest();
                        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
                        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);
                        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, locationCallback,
                                Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed*/
                    }
                });


    }


    private void getLocation() {

        try {

            // ---------------------------------- LocationRequest ------------------------------------
            // Create the location request to start receiving updates
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    Log.d(TAG, "onLocationResult: got location result.");

                    Location location = locationResult.getLastLocation();

                    if (location != null) {
                            /*User user = ((UserClient)(getApplicationContext())).getUser();
                            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                            UserLocation userLocation = new UserLocation(user, geoPoint, null);
                            saveUserLocation(userLocation);*/

                        Map<String, Object> data = new HashMap<>();
                        //data.put("dpDownloadUrl", uri.toString());
                        final GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        data.put("geoPoint", geoPoint);

                        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE);
                        String childID = sharedPreferences.getString(Constants.CHILD_ID, "");

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("child").document(childID)
                                .set(data, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                       // Toast.makeText(LocationService.this, "Success", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LocationService.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        boolean inside = false;
                        db.collection("Allowed_Markers")
                                .document(childID)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        Map<String, Object> map = documentSnapshot.getData();
                                        boolean inside = false;
                                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                                            GeoPoint point = (GeoPoint) entry.getValue();
                                            String name = entry.getKey();
                                            GeoPoint point1 = geoPoint;
                                            float results[] = new float[10];
                                            Location.distanceBetween(point.getLatitude(), point.getLongitude(), point1.getLatitude(), point1.getLongitude(), results);
                                            Log.d("distance from " + name + " is :", String.valueOf(results[0]));
                                            //Toast.makeText(LocationService.this, String.valueOf(results[0]), Toast.LENGTH_SHORT).show();
                                            if (results[0] < 5000) {
                                                //Show notification hererere
                                                Log.d("boundary", "You are out of boundary");
                                                inside = true;
                                                break;
                                            }

                                        }
                                        if (!inside){
                                            sendSMS();
                                        }




                                    }

                                });



                        String str1 = String.valueOf(location.getLatitude()) + " " + String.valueOf(location.getLongitude());
                        //Toast.makeText(LocationService.this, str1, Toast.LENGTH_SHORT).show();
                    }
                }
            };

            mLocationRequestHighAccuracy = new LocationRequest();
            mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
            mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);


            // new Google API SDK v11 uses getFusedLocationProviderClient(this)
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "getLocation: stopping the location service.");
                stopSelf();
                return;
            }
            Log.d(TAG, "getLocation: getting location information.");
            mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, locationCallback,
                    Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed

        } catch (Exception e) {
            Log.d("got", "getLocation: ");
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSMS(){


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("person").document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(documentSnapshot.getString("mobile"),null,"your child is out of boundary",null,null);
                    }
                });
    }




    /*private void saveUserLocation(final UserLocation userLocation){

        try{
            DocumentReference locationRef = FirebaseFirestore.getInstance()
                    .collection(getString(R.string.collection_user_locations))
                    .document(FirebaseAuth.getInstance().getUid());

            locationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: \ninserted user location into database." +
                                "\n latitude: " + userLocation.getGeo_point().getLatitude() +
                                "\n longitude: " + userLocation.getGeo_point().getLongitude());
                    }
                }
            });
        }catch (NullPointerException e){
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
            Log.e(TAG, "saveUserLocation: NullPointerException: "  + e.getMessage() );
            stopSelf();
        }

    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();

        //timer.cancel();
        //task.cancel();
        //stopSelf();
        //mFusedLocationClient.removeLocationUpdates(mLocationRequestHighAccuracy);
        mFusedLocationClient.removeLocationUpdates(locationCallback);
        Log.i("stopped", "onCreate() , service stopped...");
    }

}
