package com.google.firebase.codelab.friendlychat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class CustomDFrag extends DialogFragment  {
    VideoView videoViewd;

    static CustomDFrag newInstance(String title,String path) {
        CustomDFrag fragment = new CustomDFrag();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("path",path);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        String title = getArguments().getString("title");
        String path=getArguments().getString("path");
        Uri uri=Uri.parse(path);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.activity_custom_dfrag,null);
        videoViewd=(VideoView)view.findViewById(R.id.videoView);
        videoViewd.setVideoPath(path);

        MediaController media=new MediaController(getActivity());
        media.setAnchorView(videoViewd);
        videoViewd.setMediaController(media);
        videoViewd.start();

       // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        //startActivity(intent);

        //this is how you'd get a view from the inflated layout
      //  data = (TextView) view.findViewById(R.id.evo_addhist_data);
        //data.setText("lol");


        //tell the builder that you wan't that inflated layout to show
        // and then set the button (negative/positive in this case) if you want

        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_switch_video_black_24dp)
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


}
