package com.example.digitalcare.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


import com.example.digitalcare.ConstantsFile.Constants;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class LocationService extends Service {

    private static final String TAG = "LocationService";

    private FusedLocationProviderClient mFusedLocationClient;
    private final static long UPDATE_INTERVAL = 400 * 1000;  /* 4 secs */
    private final static long FASTEST_INTERVAL = 2000; /* 2 sec */

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
        getLocation();
        return START_NOT_STICKY;
    }

    private LocationRequest mLocationRequestHighAccuracy;
    private LocationCallback locationCallback;

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
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        data.put("geoPoint", geoPoint);

                        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE);
                        String childID = sharedPreferences.getString(Constants.CHILD_ID, "");

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("child").document(childID)
                                .set(data, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(LocationService.this, "Success", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LocationService.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                });

                        String str1 = String.valueOf(location.getLatitude()) + " " + String.valueOf(location.getLongitude());
                        Toast.makeText(LocationService.this, str1, Toast.LENGTH_SHORT).show();
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
