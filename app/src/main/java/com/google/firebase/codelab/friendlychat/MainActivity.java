/**

 */
package com.google.firebase.codelab.friendlychat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Indexables;
import com.google.firebase.appindexing.builders.PersonBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView messengerTextView;
        public CircleImageView messengerImageView;
        public ImageView image;
        public ImageButton record;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messageTextView.setVisibility(View.INVISIBLE);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
            image=(ImageView)itemView.findViewById((R.id.image));
            image.setVisibility(View.INVISIBLE);
            record=(ImageButton)itemView.findViewById(R.id.record);

            record.setVisibility(View.INVISIBLE);


        }
    }

    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_INVITE = 1;
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";

    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> mFirebaseAdapter;
    private ProgressBar mProgressBar;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAnalytics mFirebaseAnalytics;
    private EditText mMessageEditText;
    private AdView mAdView;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private GoogleApiClient mGoogleApiClient;
    private static final int CAMERA_REQUEST = 2;
    private static final int FILE_RESULT_CODE = 5;
    private static final int VIDEO_REQUEST = 3;
    private static final int RESULT_LOAD_IMAGE = 4;
    private StorageReference mStorageRef;
    private MediaRecorder mRecorder;
    private String mFileName=null;
    private String mFileName2=null;
    private ProgressDialog progress;
    private static int id_record=0;
    private MediaPlayer mp;
    private static String speed="Normal";
    TextToSpeech t1;
    private static boolean check_sound=true;
    private final int REQ_CODE_SPEECH_INPUT = 100;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUsername = ANONYMOUS;
        progress=new ProgressDialog(this);

        // Initialize Firebase Auth
       send_user_register();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

       get_from_database();

        // Initialize and request AdMob ad.
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Initialize Firebase Measurement.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mFileName= Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName+="/recorded_audio.3gp";
      //  mFileName2= Environment.getExternalStorageDirectory().getAbsolutePath();
        //mFileName2+="/recorded_video.MPEG_4";

        // Initialize Firebase Remote Config.
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Define Firebase Remote Config Settings.
        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings =
                new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();

        // Define default config values. Defaults are used when fetched config values are not
        // available. Eg: if an error occurred fetching values from the server.
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put("friendly_msg_length", 10L);

        // Apply config settings and default values.
        mFirebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);

        // Fetch remote config.
        fetchConfig();
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

       manage_send_button();
        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUsername,
                        mPhotoUrl,"");
                mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(friendlyMessage);
                mMessageEditText.setText("");
                mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);
            }
        });

        addNotification();

    }

    private void manage_send_button() {
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mSharedPreferences
                .getInt(CodelabPreferences.FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))});
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }

    private void get_from_database() {
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>(
                FriendlyMessage.class,
                R.layout.item_message,
                MessageViewHolder.class,
                mFirebaseDatabaseReference.child(MESSAGES_CHILD)) {

            @Override
            protected FriendlyMessage parseSnapshot(DataSnapshot snapshot) {
                FriendlyMessage friendlyMessage = super.parseSnapshot(snapshot);
                if (friendlyMessage != null) {
                    friendlyMessage.setId(snapshot.getKey());
                }
                return friendlyMessage;
            }

            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, final FriendlyMessage friendlyMessage, final int position) {
                final String post_key=getRef(position).getKey();
                id_record=position;
                viewHolder.record.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.image.setVisibility(View.VISIBLE);
                if(friendlyMessage.getText()!=null){
                    setSpeed();
                    viewHolder.messageTextView.setVisibility(View.VISIBLE);
                viewHolder.messageTextView.setText(friendlyMessage.getText());
                    play_sound(friendlyMessage.getText());}
                Glide.with(MainActivity.this)
                        .load(friendlyMessage.getImage_file())
                        .into(viewHolder.image);
                viewHolder.messengerTextView.setText(friendlyMessage.getName());
                if (friendlyMessage.getRecord()!=null||friendlyMessage.getVideo()!=null){
                    viewHolder.record.setVisibility(View.VISIBLE);


                    viewHolder.record.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(friendlyMessage.getVideo()!=null){
                                download_video(position);
                                Toast.makeText(MainActivity.this,"video"+position,Toast.LENGTH_SHORT).show();
                                 CustomDFrag dialogFragment = CustomDFrag.newInstance(
                                      "Are you sure you want to do this?","http://clips.vorwaerts-gmbh.de/VfE_html5.mp4");
                                dialogFragment.show(getSupportFragmentManager(),"dialog");
                            }
                            else {

                                download_record(position);
                                RecordDialog dialogFragment = RecordDialog.newInstance(
                                        "Are you sure you want to do this?",friendlyMessage.getRecord());
                                dialogFragment.show(getSupportFragmentManager(),"dialog");


                                Toast.makeText(MainActivity.this,"audio"+position,Toast.LENGTH_SHORT).show();
                            }




                        }
                    });
                    viewHolder.record.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            if(friendlyMessage.getVideo()!=null){

                                Toast.makeText(MainActivity.this,"long press video"+position,Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(friendlyMessage.getVideo()));
                                startActivity(intent);

                            }
                            else {
                                Intent intent=new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(friendlyMessage.getRecord()));
                                startActivity(intent);



                                Toast.makeText(MainActivity.this,"long press  audio"+position,Toast.LENGTH_SHORT).show();
                            }
                            return true;

                        }
                    });
                }
                if (friendlyMessage.getPhotoUrl() == null) {
                    viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,
                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(MainActivity.this)
                            .load(friendlyMessage.getPhotoUrl())
                            .into(viewHolder.messengerImageView);



                }



                // write this message to the on-device index
                FirebaseAppIndex.getInstance().update(getMessageIndexable(friendlyMessage));

                // log a view action on it
                FirebaseUserActions.getInstance().end(getMessageViewAction(friendlyMessage));
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    }

    private void play_sound(String read_text) {
        if(check_sound==true){
        t1.speak(read_text, TextToSpeech.QUEUE_FLUSH, null);

        }
        else {

        }
    }

    private void send_user_register() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }
    }

    private Action getMessageViewAction(FriendlyMessage friendlyMessage) {
        return new Action.Builder(Action.Builder.VIEW_ACTION)
                .setObject(friendlyMessage.getName(), MESSAGE_URL.concat(friendlyMessage.getId()))
                .setMetadata(new Action.Metadata.Builder().setUpload(false))
                .build();
    }

    private Indexable getMessageIndexable(FriendlyMessage friendlyMessage) {
        PersonBuilder sender = Indexables.personBuilder()
                .setIsSelf(mUsername == friendlyMessage.getName())
                .setName(friendlyMessage.getName())
                .setUrl(MESSAGE_URL.concat(friendlyMessage.getId() + "/sender"));

        PersonBuilder recipient = Indexables.personBuilder()
                .setName(mUsername)
                .setUrl(MESSAGE_URL.concat(friendlyMessage.getId() + "/recipient"));

        Indexable messageToIndex = Indexables.messageBuilder()
                .setName(friendlyMessage.getText())
                .setUrl(MESSAGE_URL.concat(friendlyMessage.getId()))
                .setSender(sender)
                .setRecipient(recipient)
                .build();

        return messageToIndex;
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        else if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                final View v = MainActivity.this.findViewById(R.id.record);
                if (v != null) {
                    v.setOnTouchListener(new View.OnTouchListener()
                    {

                        @Override
                        public boolean onTouch(View v, MotionEvent event)
                        {
                            if (event.getAction() == MotionEvent.ACTION_DOWN){
                                Log.d("Pressed", "Button pressed");
                            startRecording();}
                            else if (event.getAction() == MotionEvent.ACTION_UP) {

                                Log.d("Released", "Button released");
                                // TODO Auto-generated method stub
                                stopRecording();
                            }
                            return false;
                        }
                    });

                }
            }
        });
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                final View v = MainActivity.this.findViewById(R.id.cam);
                if (v != null) {
                    v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                           registerForContextMenu(v);
                            return false;
                        }
                    });
                }
            }
        });
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                final View v = MainActivity.this.findViewById(R.id.speak);
                if (v != null) {
                    v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            registerForContextMenu(v);
                            return false;
                        }
                    });
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite_menu:
                sendInvitation();
                return true;
            case R.id.crash_menu:
                FirebaseCrash.logcat(Log.ERROR, TAG, "crash caused");
                causeCrash();
                return true;
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mFirebaseUser = null;
                mUsername = ANONYMOUS;
                mPhotoUrl = null;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            case R.id.fresh_config_menu:
                fetchConfig();
                return true;
            case R.id.record:

                return true;
            case R.id.cam:


                return true;
            case R.id.speachtotext:
                promptSpeechInput();

                return true;
            case R.id.speak:

                if(check_sound==true){check_sound=false;
                    Toast.makeText(this,"Sound_speaker turn OFF",Toast.LENGTH_SHORT).show();
                }else {check_sound=true;
                    Toast.makeText(this,"Sound_speaker turn ON",Toast.LENGTH_SHORT).show();}


                return true;
            case R.id.file:
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);

                // CustomDFrag dialogFragment = CustomDFrag.newInstance(
                 //      "Are you sure you want to do this?","http://clips.vorwaerts-gmbh.de/VfE_html5.mp4");
                //dialogFragment.show(getSupportFragmentManager(),"dialog");

                // Intent i=new Intent(MainActivity.this,DialogVideo.class);
                //startActivity(i);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void causeCrash() {
        throw new NullPointerException("Fake null pointer exception");
    }

    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    // Fetch the config to determine the allowed length of messages.
    public void fetchConfig() {
        long cacheExpiration = 3600; // 1 hour in seconds
        // If developer mode is enabled reduce cacheExpiration to 0 so that each fetch goes to the
        // server. This should not be used in release builds.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Make the fetched config available via FirebaseRemoteConfig get<type> calls.
                        mFirebaseRemoteConfig.activateFetched();
                        applyRetrievedLengthLimit();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // There has been an error fetching the config
                        Log.w(TAG, "Error fetching config: " + e.getMessage());
                        applyRetrievedLengthLimit();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Use Firebase Measurement to log that invitation was sent.
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_sent");

                // Check how many invitations were sent and log.
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                Log.d(TAG, "Invitations sent: " + ids.length);
            } else {
                // Use Firebase Measurement to log that invitation was not sent
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_not_sent");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, payload);

                // Sending failed or it was canceled, show failure message to the user
                Log.d(TAG, "Failed to send invitation.");
            }
        }
        else if (requestCode==VIDEO_REQUEST&&resultCode==RESULT_OK){

            progress.setMessage("uploaded....");
            progress.show();
            Uri uri=data.getData();
            StorageReference file_path=mStorageRef.child("video").child("new_video"+id_record+"3gp");
            file_path.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this,"uploaded done",Toast.LENGTH_SHORT).show();
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                   send_video_db(downloadUrl.toString());
                    progress.dismiss();



                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this,"uploaded failure",Toast.LENGTH_SHORT).show();
                    progress.dismiss();

                }
            });





        }
        else if(resultCode==RESULT_CANCELED){
            Intent i =new Intent(MainActivity.this,MainActivity.class);
            startActivity(i);
        }
        else if(resultCode==RESULT_OK&&requestCode==REQ_CODE_SPEECH_INPUT){

            ArrayList<String> result = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mMessageEditText.setText(result.get(0));
           Toast.makeText(this,result.get(0),Toast.LENGTH_SHORT).show();
        }
        else {
            progress.setMessage("uploaded....");
            progress.show();
            Uri uri=data.getData();
            StorageReference file_path=mStorageRef.child("photos").child(uri.getLastPathSegment());
            file_path.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this,"uploaded deone",Toast.LENGTH_SHORT).show();
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    send_image_db(downloadUrl.toString());
                    progress.dismiss();



                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this,"uploaded failure",Toast.LENGTH_SHORT).show();
                    progress.dismiss();

                }
            });



        }

    }

    private void send_image_db(String image) {
        FriendlyMessage friendlyMessage = new FriendlyMessage("", mUsername,
                mPhotoUrl,image);


        mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(friendlyMessage);


    }

    /**
     * Apply retrieved length limit to edit text field. This result may be fresh from the server or it may be from
     * cached values.
     */
    private void applyRetrievedLengthLimit() {
        Long friendly_msg_length = mFirebaseRemoteConfig.getLong("friendly_msg_length");
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(friendly_msg_length.intValue())});
        Log.d(TAG, "FML is: " + friendly_msg_length);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        switch(v.getId()){

            case R.id.cam: /** Start a new Activity MyCards.java */
                menu.setHeaderTitle("Select The Action").setHeaderIcon(R.drawable.ic_add_a_photo_black_24dp);
                menu.add(0, v.getId(), 0, "Camera").setIcon(R.drawable.ic_add_a_photo_black_24dp);//groupId, itemId, order, title
                menu.add(0, v.getId(), 0, "Video").setIcon(R.drawable.ic_switch_video_black_24dp);
                break;

            case R.id.speak: /** AlerDialog when click on Exit */
                menu.setHeaderTitle("Select The Action").setHeaderIcon(R.drawable.ic_speaker_notes_black_24dp);
                menu.add(0, v.getId(), 0, "Very Slow");//groupId, itemId, order, title
                menu.add(0, v.getId(), 0, "Slow");
                menu.add(0, v.getId(), 0, "Normal");
                menu.add(0, v.getId(), 0, "Fast");
                menu.add(0, v.getId(), 0, "Very Fast");



                break;

    }}
    @Override
    public boolean onContextItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.cam:
                if (item.getTitle() == "Camera") {
                    Toast.makeText(getApplicationContext(), "camera ", Toast.LENGTH_LONG).show();
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);

                } else if (item.getTitle() == "Video") {
                    Toast.makeText(getApplicationContext(), "video", Toast.LENGTH_LONG).show();
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
                    startActivityForResult(cameraIntent, VIDEO_REQUEST);
                }
                return true;
            case R.id.speak:
                if(item.getTitle()=="Very Slow"){
                    Toast.makeText(this,"very  Slow spead",Toast.LENGTH_SHORT).show();
                    speed="Very Slow";


                }
                else if (item.getTitle()=="Slow"){
                    Toast.makeText(this," Slow spead",Toast.LENGTH_SHORT).show();
                    speed="Slow";

                }
                else if (item.getTitle()=="Normal"){
                    Toast.makeText(this,"Normal spead",Toast.LENGTH_SHORT).show();
                    speed="Normal";
                }
                else if (item.getTitle()=="Fast"){
                    Toast.makeText(this,"Fast spead",Toast.LENGTH_SHORT).show();
                    speed="Fast";
                }
                else  {
                    Toast.makeText(this,"Very Fast spead",Toast.LENGTH_SHORT).show();
                    speed="Very Fast spead";
                }
                return true;
            default:return true;

        }

    }
    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {

        }

        mRecorder.start();
    }
    private void startRecordingVideo() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName2);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {

        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        upload_record();
    }
    private void stopRecordingVideo() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        upload_Video();
    }

    private void upload_record() {
        progress.setMessage("uploaded....");
        progress.show();

        StorageReference file_path=mStorageRef.child("Audio").child("new_audio"+id_record+".3gp");
        Uri uri=Uri.fromFile(new File(mFileName));
        file_path.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this,"uploaded deone",Toast.LENGTH_SHORT).show();
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                send_record_db(downloadUrl.toString());
                progress.dismiss();



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"uploaded failure",Toast.LENGTH_SHORT).show();
                progress.dismiss();

            }
        });
    }
    private void upload_Video() {
        progress.setMessage("uploaded....");
        progress.show();
        StorageReference file_path=mStorageRef.child("Video").child("new_Video"+id_record+".MPEG_4");
        Uri uri=Uri.fromFile(new File(mFileName2));
        file_path.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this,"uploaded deone",Toast.LENGTH_SHORT).show();
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                send_record_db(downloadUrl.toString());
                progress.dismiss();


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"uploaded failure",Toast.LENGTH_SHORT).show();
                progress.dismiss();

            }
        });
    }


    private void send_record_db(String s) {
        FriendlyMessage friendlyMessage = new FriendlyMessage("", mUsername,
                mPhotoUrl);
        friendlyMessage.setRecord(s);


        mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(friendlyMessage);


    }
    private void send_video_db(String s){
        FriendlyMessage friendlyMessage = new FriendlyMessage("", mUsername,
                mPhotoUrl);
        friendlyMessage.setVideo(s);


        mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(friendlyMessage);


    }
    private void play_recoded(String url){
        try{
            mp.setDataSource(url);
            mp.prepare();


        }catch(Exception e){e.printStackTrace();}

    }
    public void doPositiveClick() {
//---perform steps when user clicks on OK---
        Log.d("DialogFragmentExample", "User clicks on OK");
    }
    public void doNegativeClick() {
//---perform steps when user clicks on Cancel---
        Log.d("DialogFragmentExample", "User clicks on Cancel");
    }
    private void download_record(int pos){
        StorageReference file_path = mStorageRef.child("Audio").child("new_audio"+pos+".3gp");

        File localFile = null;
        try {
            localFile = File.createTempFile("audio", "3pg",getExternalFilesDir(null));

        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this,localFile.getAbsolutePath(),Toast.LENGTH_SHORT).show();

        file_path.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                Toast.makeText(MainActivity.this,"download success",Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }
    private void download_video(int pos){
        StorageReference file_path = mStorageRef.child("video").child("new_video"+"0"+"3gp");

        File localFile = null;
        try {
            localFile = File.createTempFile("video", "3pg",getExternalFilesDir(null));

        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this,localFile.getAbsolutePath(),Toast.LENGTH_SHORT).show();

        file_path.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                Toast.makeText(MainActivity.this,"download success",Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });


    }
    private void setSpeed(){
        if(speed.equals("Very Slow")){
            t1.setSpeechRate(0.1f);
        }
        if(speed.equals("Slow")){
            t1.setSpeechRate(0.5f);
        }
        if(speed.equals("Normal")){
            t1.setSpeechRate(1.0f);//default 1.0
        }
        if(speed.equals("Fast")){
            t1.setSpeechRate(1.5f);
        }
        if(speed.equals("Very Fast")){
            t1.setSpeechRate(2.0f);
        }
        //for setting pitch you may call
        //tts.setPitch(1.0f);//default 1.0
    }
    private void addNotification() {

        Intent notificationIntent = new Intent(this, GameAnimation.class).putExtra("uname",mUsername);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);



        final Context context = getApplicationContext();
        final NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        final Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher) // Needed for the notification to work/show!!
                .setContentTitle("Welcome To chat app")
                .setContentText("Greeting  "+mUsername)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent
                )

                .build();


        notificationManager.notify(0, notification);

    }
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

}
