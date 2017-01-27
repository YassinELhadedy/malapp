package com.google.firebase.codelab.friendlychat;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class DialogVideo extends AppCompatActivity {
    VideoView videoViewd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Uncomment the below code to Set the message and title from the strings.xml file
        //builder.setMessage(R.string.dialog_message) .setTitle(R.string.dialog_title);

        //Setting message manually and performing action on button click
        LayoutInflater inflater = DialogVideo.this.getLayoutInflater();

        View view = inflater.inflate(R.layout.activity_custom_dfrag,null);
        videoViewd=(VideoView)view.findViewById(R.id.videoView);
        videoViewd.setVideoPath("http://clips.vorwaerts-gmbh.de/VfE_html5.mp4");

        MediaController media=new MediaController(getApplication().getApplicationContext());
        media.setAnchorView(videoViewd);
        videoViewd.setMediaController(media);
        videoViewd.start();
        builder.setView(view)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button
                        dialog.cancel();
                    }
                });

        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("Are you sure you want to do this?");
        alert.show();
        setContentView(R.layout.activity_custom_dfrag);

    }
}
