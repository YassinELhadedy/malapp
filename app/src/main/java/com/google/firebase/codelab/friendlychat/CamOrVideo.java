package com.google.firebase.codelab.friendlychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class CamOrVideo extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_or_video);
        Bundle extras = getIntent().getExtras();
        if(extras.containsKey("camera")) {
            //Do stuff because extra has been added
            Toast.makeText(CamOrVideo.this,"cam",Toast.LENGTH_SHORT).show();
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
        else {
            Toast.makeText(CamOrVideo.this,"video",Toast.LENGTH_SHORT).show();
        }


    }
}
