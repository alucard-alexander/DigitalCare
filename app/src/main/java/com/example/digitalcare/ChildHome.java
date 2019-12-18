package com.example.digitalcare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ChildHome extends AppCompatActivity {
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);
        auth = FirebaseAuth.getInstance();
        //Toast.makeText(this, auth.getUid(), Toast.LENGTH_SHORT).show();
    }

    public void addDevice(View view){
        Intent i = new Intent(this,AddChildDevice.class);
        startActivity(i);
    }

}
