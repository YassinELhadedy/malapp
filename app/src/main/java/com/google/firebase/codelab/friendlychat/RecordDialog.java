package com.google.firebase.codelab.friendlychat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by Elhadedy on 1/25/2017.
 */

public class RecordDialog extends DialogFragment implements View.OnClickListener {
    private Button playButton;
    private Button prevButton;
    private Button nextButton;
    private MediaPlayer mediaPlayer;
    private TextView text;


    static RecordDialog newInstance(String title, String path) {
        RecordDialog fragment = new RecordDialog();
        Bundle args = new Bundle();
        args.putString("title_record", title);
        args.putString("path_record",path);
        fragment.setArguments(args);
        return fragment;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = getArguments().getString("title_record");
        String path=getArguments().getString("path_record");

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource("/storage/emulated/0/recorded_audio.3gp");//change here in url
           // mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.record,null);
        text = (TextView) view.findViewById(R.id.artistName);


        playButton = (Button) view.findViewById(R.id.playButton);
        prevButton = (Button) view. findViewById(R.id.prevButton);
        nextButton = (Button) view. findViewById(R.id.nextButton);

        playButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_play_circle_filled_black_24dp)
                .setTitle(title).setView(view)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                ((MainActivity)
                                        getActivity()).doPositiveClick();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                ((MainActivity)
                                        getActivity()).doNegativeClick();
                            }
                        }).create();
    }




    @Override
    public void onClick(View view) {
        switch(view.getId()){

            case R.id.playButton:
                //playmusic
                if (mediaPlayer.isPlaying()){
                    pauseMusic();
                }else {
                    playMusic();
                }
                break;
            case R.id.prevButton:
                //do something
                break;
            case R.id.nextButton:
                //do somthing
                break;
        }

    }
    public void playMusic(){

        if (mediaPlayer != null){
            mediaPlayer.start();
            text.setText("Music Playing now...");
            playButton.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
        }
    }

    public void pauseMusic(){
        if (mediaPlayer != null){
            mediaPlayer.pause();
            text.setText("Music Paused!");
            playButton.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
        }
    }

    public void prevMusic(){
        if (mediaPlayer != null){
            //do something
        }
    }

    public void nextMusic(){
        if (mediaPlayer != null){
            //do something
        }
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()){

            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();

    }
}
