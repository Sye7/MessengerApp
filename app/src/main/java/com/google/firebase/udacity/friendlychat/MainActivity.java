/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.udacity.friendlychat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Bitmap bitmapML;

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final String FRIENDLY_MSG_LENGTH_KEY ="friendly_msg_length";
    public static final String DEFAULT_SEND_BUTTON = "send_button";
    public static final int RC_SIGN_IN = 1;
    public static final int RC_PHOTO_PICKER =2;
    public static final int MACHINE_lEARN =3;


    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    private String mUsername;

    // Firebase instance var
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessageDatabaseReference;

    private ChildEventListener childEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotoStorageRef;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseRemoteConfig mFirebaseRemoteConfigForSendButton;

    // new for tt

    private TextToSpeech textToSpeechSystem;
     static String language;
    private ImageButton lang;
    private SpeechRecognizer mySpeechRecognizer;


    //tt

    @Override
    protected void onDestroy() {

        textToSpeechSystem.shutdown();
        super.onDestroy();

    }


    // text to speech

    public void say()
    {
        super.onStart();

        textToSpeechSystem = new TextToSpeech(this, ttsInitResult -> {
            if (TextToSpeech.SUCCESS == ttsInitResult) {

                language = language.toLowerCase();
                String textToSay = mMessageEditText.getText().toString();

                switch(language)
                {
                    case "us":
                        textToSpeechSystem.setLanguage(Locale.US);
                        break;

                    case "uk":
                        textToSpeechSystem.setLanguage(Locale.UK);
                        break;

                    case "italy":
                        textToSpeechSystem.setLanguage(Locale.ITALIAN);
                        break;

                    case "china":
                        textToSpeechSystem.setLanguage(Locale.CHINESE);
                        break;

                    case "france":
                        textToSpeechSystem.setLanguage(Locale.FRANCE);
                        break;

                    case "japan":
                        textToSpeechSystem.setLanguage(Locale.JAPANESE);
                        break;

                    case "korea":
                        textToSpeechSystem.setLanguage(Locale.KOREAN);
                        break;

                    default:
                        textToSpeechSystem.setLanguage(new Locale("hin"));
                        break;

                }

                textToSpeechSystem.speak(textToSay + "Okay ", TextToSpeech.QUEUE_ADD,null, null);
                textToSpeechSystem.playSilentUtterance(5000,TextToSpeech.QUEUE_ADD,null);


            }

        });


    }



    private void initailzeSpeechRecognizer() {

        // check speech recognizer is availabe


        if(SpeechRecognizer.isRecognitionAvailable(this)){

            mySpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mySpeechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int error) {

                }

                @Override
                public void onResults(Bundle results) {

                    ArrayList<String> result = results.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                    );



                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                    ArrayList<String> al = partialResults.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION

                    );

                    StringBuilder ans= new StringBuilder();

                    ans.append(al.get(0));

                    mMessageEditText.setText(ans.toString() );


                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });

        }
        Toast.makeText(this, "com", Toast.LENGTH_SHORT).show();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsername = ANONYMOUS;


        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mFirebaseRemoteConfigForSendButton = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mMessageDatabaseReference =mFirebaseDatabase.getReference().child("messages");
        mChatPhotoStorageRef = mFirebaseStorage.getReference().child("image_photos");



        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);


        lang = (ImageButton) findViewById(R.id.language);
        //audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);



        //yas

        initailzeSpeechRecognizer();


        Button boom =(Button) findViewById(R.id.boom);
        boom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
                mySpeechRecognizer.startListening(intent);

            }
        });

        lang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                     String[] bhasha = {"US","UK","ITALY","CHINA","KOREA","FRANCE","JAPAN"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Pick a Country for Language");
                builder.setItems(bhasha, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // user click on colour 'which'
                        language = bhasha[which];
                    }
                });
                builder.show();
                Toast.makeText(MainActivity.this, ""+ language, Toast.LENGTH_SHORT).show();

            }
        });



        boom.setOnLongClickListener(v -> {

            say();

            return false;
        });






        // Initialize message ListView and its adapter
        final List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fire an intent to show an image picker
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent,"Complete Action Using"),RC_PHOTO_PICKER);

            }
        });

        Button mlButton = (Button) findViewById(R.id.machineL);
        mlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent,"Complete Action Using"),MACHINE_lEARN);


            }
        });

        Button bang = (Button) findViewById(R.id.bang);
        bang.setOnClickListener(v -> {

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmapML);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();


            // pass the image

            Task<FirebaseVisionText> result =
                    detector.processImage(image)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    // Task completed successfully
                                    // ...
                                    mMessageEditText.setText(firebaseVisionText.getText());
                                    Toast.makeText(MainActivity.this, "yipeee" + firebaseVisionText.getText(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                            Toast.makeText(MainActivity.this, "Better luck next Time", Toast.LENGTH_SHORT).show();
                                        }
                                    });

        });


        // Enable Send button when there's text to send
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
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send messages on click

                final FriendlyMessage message = new FriendlyMessage(mMessageEditText.getText().toString(),mUsername,null);

                mMessageDatabaseReference.push().setValue(message, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        if(databaseError == null)
                            Toast.makeText(MainActivity.this, "Successful" , Toast.LENGTH_SHORT).show();

                        else
                            Toast.makeText(MainActivity.this, "Save Failed" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                // Clear input box
                mMessageEditText.setText("");


            }
        });



        //  firebase ui and auth


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    // signed in
                    onSignedInInitialize(user.getDisplayName());

                } else{

                    // signed out

                    onSignedOutCleanup();

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.AnonymousBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

//     Remote config

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(FRIENDLY_MSG_LENGTH_KEY, DEFAULT_MSG_LENGTH_LIMIT);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);
        fetchConfig();



        FirebaseRemoteConfigSettings configSettingsForSendButton = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfigForSendButton.setConfigSettings(configSettingsForSendButton);

        Map<String, Object> defaultConfigMapForSendButton = new HashMap<>();
        defaultConfigMap.put(DEFAULT_SEND_BUTTON, "Send");
        mFirebaseRemoteConfig.setDefaults(defaultConfigMapForSendButton);
        fetchConfig();
    }

    public void fetchConfig(){

        long cacheExpiration = 3600;

        if(mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()){

            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mFirebaseRemoteConfig.activateFetched();
                        applyRetrievedLengthLimit();
                    }
                });

        // for send button

        long cacheExpirationSend = 3600;

        if(mFirebaseRemoteConfigForSendButton.getInfo().getConfigSettings().isDeveloperModeEnabled()){

            cacheExpirationSend = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpirationSend)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mFirebaseRemoteConfig.activateFetched();
                        changeSendButton();
                    }
                });
    }

    public void changeSendButton(){

        String buttonName = mFirebaseRemoteConfigForSendButton.getString(DEFAULT_SEND_BUTTON);
        mSendButton.setText(buttonName);
    }

    public void applyRetrievedLengthLimit(){

        Long friendly_msg_length = mFirebaseRemoteConfig.getLong(FRIENDLY_MSG_LENGTH_KEY);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(friendly_msg_length.intValue())});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                //sign out
                AuthUI.getInstance().signOut(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);

        detachDbReadListener();
        mMessageAdapter.clear();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void onSignedInInitialize(String username){

        mUsername = username;
        attachDbReadListener();


    }
    private void onSignedOutCleanup(){

        mUsername = ANONYMOUS;
        mMessageAdapter.clear();
        detachDbReadListener();


    }

    static   Uri downloadUri;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){

             if(resultCode == RESULT_OK)
                Toast.makeText(this, "Sign in", Toast.LENGTH_SHORT).show();
             else {
                Toast.makeText(this, "Sign in Cancel", Toast.LENGTH_SHORT).show();
                finish();
              }
            } else if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK){
                Uri selectedImageUri = data.getData();



            //Get a reference to store file at image_photos/<FileName>
                   final StorageReference photoRef =  mChatPhotoStorageRef.child(selectedImageUri.getLastPathSegment());


                   //Upload file to firebase storage
            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(MainActivity.this, "image uploaded", Toast.LENGTH_SHORT).show();

                    photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            // got image url
                             downloadUri = uri;

                            FriendlyMessage friendlyMessage =
                                    new FriendlyMessage(null,mUsername,downloadUri.toString());
                            mMessageDatabaseReference.push().setValue(friendlyMessage);

                        }


                    });



                }
            });
        }
        else if(requestCode == MACHINE_lEARN && resultCode ==RESULT_OK){

            Uri selectedImageUriM = data.getData();

            //machine l
            try {
                bitmapML = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUriM);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //...

        }

        }


    private  void detachDbReadListener() {

        if (childEventListener != null) {
            mMessageDatabaseReference.removeEventListener(childEventListener);
            childEventListener = null;
        }
    }

    private void attachDbReadListener() {

        if (childEventListener == null) {
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                    mMessageAdapter.add(friendlyMessage);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }

            };
            mMessageDatabaseReference.addChildEventListener(childEventListener);

        }
    }
}


//  gs://friendlychat-d59d8.appspot.com