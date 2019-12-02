package com.example.digitalcare;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.digitalcare.ConstantsFile.Constants;
import com.example.digitalcare.Services.LocationService;
import com.example.digitalcare.Services.ParentService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Main2Activity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    String ImageURL = "https://firebasestorage.googleapis.com/v0/b/digitalcare-d88ac.appspot.com/o/parent-with-children-logo_1243717.jpg?alt=media&token=1b3f3e0b-5780-4a72-9dd4-2e6dd905529f";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main2);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);


            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            NavigationView navigationView = findViewById(R.id.nav_view);
            // changing the email
            View headerView = navigationView.getHeaderView(0);
            TextView navUsername = (TextView) headerView.findViewById(R.id.textViewNavBarID);
            final TextView nameUser123 = (TextView) headerView.findViewById(R.id.nameNavBar);
            FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
            //changing the DP
            final ImageView profilePictureView = (ImageView) headerView.findViewById(R.id.imageViewNavBar);
            //final String ImageUrl;
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("person")
                    .document(FirebaseAuth.getInstance().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                ImageURL = document.getString("dpDownloadUrl");
                                nameUser123.setText(document.getString("name"));
                               // Log.d("ggggggggg1", ImageURL);
                                Picasso.with(Main2Activity.this).load(ImageURL).into(profilePictureView);

                            }
                        }
                    });

            //Log.d("ggggggggg", ImageURL);


            navUsername.setText(current.getEmail());
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_home, R.id.nav_gallery)
                    .setDrawerLayout(drawer)
                    .build();
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
            TextView te = findViewById(R.id.textViewNavBarID);


            //te.setText("working");
       /* Intent serviceIntent  = new Intent(this, LocationService.class);
        stopService(serviceIntent);*/

        }catch (Exception e){
            Log.d("eeeeeee", e.getMessage());
        }

    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case  R.id.action_signout: {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constants.TYPE, "");
                Intent i = new Intent(this,Login.class);
                startActivity(i);
                return true;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PERMISSIONS_REQUEST_ENABLE_GPS){
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_gallery);
            fragment.onActivityResult(requestCode,resultCode,data);
        }
    }
}
