package com.example.digitalcare.ui.gallery;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.digitalcare.ConstantsFile.Constants;
import com.example.digitalcare.Main2Activity;
import com.example.digitalcare.R;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.validation.Validator;

public class GalleryFragment extends Fragment implements OnMapReadyCallback {

    private GalleryViewModel galleryViewModel;
    GoogleMap map123;
    private Boolean locationPermissionGranted = false;
    private FirebaseFirestore db;
    private int i;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        //final TextView textView = root.findViewById(R.id.text_gallery);
        galleryViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frg);  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap mMap) {
                //Toast.makeText(getActivity(), "Ok", Toast.LENGTH_SHORT).show();
                map123 = mMap;
                if (checkMapServices()) {
                    getLocationPermission();
                    //locationPermissionGranted = true;
                    //

                    if (locationPermissionGranted) {
                        mMap.setMyLocationEnabled(true);
                    }

                    //Adding Markers


                    db = FirebaseFirestore.getInstance();
                    FirebaseAuth mAuth;
                    mAuth = FirebaseAuth.getInstance();
                    db.collection("child")
                            .whereEqualTo("p_id", mAuth.getUid())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {


                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                    Bitmap bitmap1;

                                    if (task.isSuccessful()) {
                                        if (task.getResult().isEmpty()) {
                                            //Toast.makeText(MainActivity.this, "Empty", Toast.LENGTH_SHORT).show();
                                            Toast.makeText(getContext(), "No Markers Added for this user", Toast.LENGTH_SHORT).show();
                                        } else {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                //Log.d("working", document.getId() + " => " + document.getData());
                                                //Log.d("name", document.getString("name"));
                                                //Toast.makeText(MainActivity.this, document.getId(), Toast.LENGTH_SHORT).show();
                                                //Log.d("test", "onComplete: ");
                                                try {


                                                    GeoPoint geoPoint = document.getGeoPoint("geoPoint");
                                                    final LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());

                                                    final QueryDocumentSnapshot document1 = document;

                                                    Glide.with(getContext())
                                                            .asBitmap()
                                                            .load(document1.getString("dpDownloadUrl"))
                                                            .fitCenter()
                                                            .into(new SimpleTarget<Bitmap>() {

                                                                int scaleSize =350;

                                                                public Bitmap resizeImageForImageView(Bitmap bitmap) {
                                                                    Bitmap resizedBitmap = null;
                                                                    int originalWidth = bitmap.getWidth();
                                                                    int originalHeight = bitmap.getHeight();
                                                                    int newWidth = -1;
                                                                    int newHeight = -1;
                                                                    float multFactor = -1.0F;
                                                                    if(originalHeight > originalWidth) {
                                                                        newHeight = scaleSize ;
                                                                        multFactor = (float) originalWidth/(float) originalHeight;
                                                                        newWidth = (int) (newHeight*multFactor);
                                                                    } else if(originalWidth > originalHeight) {
                                                                        newWidth = scaleSize ;
                                                                        multFactor = (float) originalHeight/ (float)originalWidth;
                                                                        newHeight = (int) (newWidth*multFactor);
                                                                    } else if(originalHeight == originalWidth) {
                                                                        newHeight = scaleSize ;
                                                                        newWidth = scaleSize ;
                                                                    }
                                                                    resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
                                                                    return resizedBitmap;
                                                                }

                                                                @Override
                                                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                                    //bitmap1 = resource;
                                                                    /*resource.setWidth(250);
                                                                    resource.setHeight(250);*/
                                                                    resource = resizeImageForImageView(resource);

                                                                    mMap.addMarker(new MarkerOptions().position(latLng).title(document1.getString("name"))
                                                                        .icon(BitmapDescriptorFactory.fromBitmap(resource))
                                                                    );
                                                                }
                                                            });

                                                    //View mCustomMarkerView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);



                                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                                                } catch (Exception e) {
                                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }

                                                try {

                                                    db.collection("Allowed_Markers")
                                                            .document(document.getId())
                                                            .get()
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()){
                                                                        DocumentSnapshot documentSnapshot = task.getResult();
                                                                        Map<String,Object> map  = documentSnapshot.getData();

                                                                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                                                                            //mMap.addMarker(new MarkerOptions().position())
                                                                            GeoPoint geoPoint = (GeoPoint) entry.getValue();
                                                                            LatLng latLng = new LatLng(geoPoint.getLatitude(),geoPoint.getLongitude());
                                                                            String name = entry.getKey();
                                                                            mMap.addMarker(new MarkerOptions().position(latLng).title(name).draggable(false)
                                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                                                            );
                                                                            Circle circle = mMap.addCircle(new CircleOptions()
                                                                            .center(latLng)
                                                                                    .radius(10000)
                                                                                    .strokeColor(Color.RED)
                                                                                    .fillColor(0x220000FF)
                                                                                            .strokeWidth(5)

                                                                            );
                                                                            //Log.d("lllllllllllll", String.valueOf(geoPoint.getLatitude()));
                                                                        }
                                                                    }
                                                                }
                                                            });

                                                }catch (Exception e){
                                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }






                                                /*db.collection("Allowed_Markers")
                                                        .document(document.getId())
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task1) {
                                                                if (task1.getResult() == null) {
                                                                    //Toast.makeText(MainActivity.this, "Empty", Toast.LENGTH_SHORT).show();
                                                                    Toast.makeText(getContext(), "No Markers Added for this user", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    for (QueryDocumentSnapshot document : task1.getResult()) {

                                                                    }
                                                            }
                                                        });*/

                                            }
                                        }
                                    } else {
                                        Log.w("error", "Error getting documents.", task.getException());
                                        //Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();

                                    }
                                }


                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_SHORT).show();
                                }
                            });


                    //Ending adding markers

                }

            }
        });
        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //googleMap.setMyLocationEnabled(true);

    }

    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("This application requires GPS to work properly, enable it!!")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, Constants.PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        Toast.makeText(getContext(), "Working", Toast.LENGTH_SHORT).show();
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());

        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            //Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, Constants.ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(getContext(), "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        //locationPermissionGranted = false;

        switch (requestCode) {
            case Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    map123.setMyLocationEnabled(true);
                }
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.PERMISSIONS_REQUEST_ENABLE_GPS: {
                map123.setMyLocationEnabled(true);
            }
        }
    }


}