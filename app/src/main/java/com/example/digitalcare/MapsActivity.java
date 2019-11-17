package com.example.digitalcare;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.digitalcare.Bean.MarkerDetails;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private EditText editText;
    private String searchString;
    private ProgressDialog progressDialog;
    private LatLng latLng;
    private ArrayList<MarkerDetails> mark = new ArrayList<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        editText = findViewById(R.id.input_search);

        progressDialog = new ProgressDialog(this);
    }

    private void init() {

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER
                ) {
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setTitle("Loading...");
                    Toast.makeText(MapsActivity.this, "Here", Toast.LENGTH_SHORT).show();
                    geoLocate();
                }
                return false;
            }
        });


    }


    private void moveCameraSetMarker(LatLng latLng,String name){
        mMap.addMarker(new MarkerOptions().position(latLng).title(name));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        String thi= String.valueOf(mark.size());
        Toast.makeText(this, thi, Toast.LENGTH_SHORT).show();
    }

    private void geoLocate() {
        int i =0;
        searchString = editText.getText().toString();

        Geocoder geocoder = new Geocoder(this);
        List<Address> list = new ArrayList<>();

        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.d("error", e.getMessage());
            e.printStackTrace();
        }

        if (list.size() > 0) {
            final Address address = list.get(0);

            Log.d("addressss", "address: " + address.toString());
            latLng = new LatLng(address.getLatitude(),address.getLongitude());
            /*childareaname childareaname1 = new childareaname();
            childareaname1.show(getSupportFragmentManager(),"Area name");*/


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Area Name");
            builder.setMessage("Enter Area name! single word is recommended");
            final EditText input123 = new EditText(this);
            builder.setView(input123);

            builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        String txt = input123.getText().toString();
                        //Toast.makeText(MapsActivity.this, txt, Toast.LENGTH_SHORT).show();
                        if (txt != null) {


                            //dialog.dismiss();
                            GeoPoint gp = new GeoPoint(address.getLatitude(), address.getLongitude());
                            MarkerDetails md = new MarkerDetails(txt, gp);
                            mark.add(md);
                            moveCameraSetMarker(latLng, txt);

                        }else {
                            Toast.makeText(MapsActivity.this, "Enter the place name", Toast.LENGTH_SHORT).show();
                            geoLocate();
                        }
                    }catch (Exception e){
                        Toast.makeText(MapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            });


            AlertDialog ad = builder.create();
            ad.show();

            //builder.show();

            //moveCameraSetMarker(latLng,searchString);

            //moveCameraSetMarker(new ,address.getLongitude()),searchString);
        }


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(13.0305, 77.5649);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        mMap.setMyLocationEnabled(true);

        ChildInformationDialog childInformationDialog = new ChildInformationDialog();
        childInformationDialog.show(getSupportFragmentManager(), "example dialog");
        init();
    }


    /*@Override
    public void getTextString(String name) {
        moveCameraSetMarker(latLng,name);

    }*/
}
