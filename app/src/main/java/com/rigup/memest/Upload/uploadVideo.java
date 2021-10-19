package com.rigup.memest.Upload;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.installations.FirebaseInstallationsRegistrar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.rigup.memest.R;
import com.vincent.videocompressor.VideoCompress;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class uploadVideo extends AppCompatActivity {
    Uri selectedVideo;
    EditText videoNameEditText;
    EditText videoTagsEditText;

    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    FirebaseStorage firebaseStorage;
    ProgressBar uploadingProgressBar;
    Button uplaodButton;
    ArrayList<String> tags;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);
        videoNameEditText = findViewById(R.id.videoNameEditText);
        videoTagsEditText = findViewById(R.id.videoTagsEditText);
        uploadingProgressBar = findViewById(R.id.uploadingProgressBar);

        uplaodButton = findViewById(R.id.uploadButton);
        firebaseAuth =FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();


        Intent intent = getIntent();
        selectedVideo = intent.getParcelableExtra("selectedVideo");
        videoNameEditText.setText((String) (new File(selectedVideo.getLastPathSegment())).getName()
                .subSequence(0,(new File(selectedVideo.getLastPathSegment())).getName().indexOf(".")));



        uplaodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoNameEditText.getText().toString().equals("")  || videoTagsEditText.getText().toString().equals("")){
                    Toast.makeText(uploadVideo.this, "Please type title and  tags", Toast.LENGTH_SHORT).show();
                }else{
                    //Uploaded video name
                    String compressVideoName= UUID.randomUUID().toString()+".mp4";


                    //compress file name given
                    File fiddle = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+File.separator+"MemesT");
                    if(!fiddle.mkdir()){
                        fiddle.mkdir();
                    }
                    String outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            .getAbsolutePath() +File.separator+"Memest"+ File.separator + compressVideoName;
                    tags = new ArrayList<String>((Arrays.asList(videoTagsEditText.getText().toString().split("\\s*,\\s*"))));
                    //prossing user input video and upload it in storage and database and with suitable name;
                    VideoCompress.compressVideoLow(selectedVideo.getLastPathSegment(), outputDir, new VideoCompress.CompressListener() {
                        @Override
                        public void onStart() {
                            uploadingProgressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onSuccess() {

                            //////////////////////////////////////////Uploading in firebase storage and database/////////////////////////////////////////////////

                            try {

                                firebaseStorage.getReference().child("videos").child(compressVideoName).putStream(new FileInputStream(new File(outputDir)))
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull @NotNull Exception e) {
                                                Toast.makeText(uploadVideo.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        //get downlaod url of the video in firebase

                                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                ////////////////////////////////////////////////Updatation in database //////////////////////////////////////////////////////////////////
                                                String contentId = UUID.randomUUID().toString();
                                                firebaseDatabase.getReference().child("users").child(firebaseAuth.getCurrentUser().getUid()).child(contentId)
                                                        .child("title").setValue(videoNameEditText.getText().toString());

                                                firebaseDatabase.getReference().child("users").child(firebaseAuth.getCurrentUser().getUid()).child(contentId)
                                                        .child("tags").setValue(videoTagsEditText.getText().toString());

                                                firebaseDatabase.getReference().child("users").child(firebaseAuth.getCurrentUser().getUid()).child(contentId)
                                                        .child("videourl").setValue(uri.toString());

                                                /////////////////Getting thumbnail, uplaod in firebase and get their url////////////////////////////////////////////////////////
                                                try {
                                                    //set thumb nail to database and storage
                                                    thumbnailProcessing(uri.toString(),contentId);

                                                } catch (Exception e) {
                                                    Toast.makeText(uploadVideo.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                    uploadingProgressBar.setVisibility(View.GONE);
                                                    Log.i("this is eriir", e.toString());
                                                } catch (Throwable throwable) {
                                                    Toast.makeText(uploadVideo.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                    uploadingProgressBar.setVisibility(View.GONE);
                                                    Log.i("this is eriir", throwable.toString());
                                                }
                                            }
                                        });


                                    }
                                });

                            } catch (Exception e) {
                                Toast.makeText(uploadVideo.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                uploadingProgressBar.setVisibility(View.GONE);
                                Log.i("this is eriir", e.toString());
                            }

                            ////////////////////////////////////////End of Uploading in firebase storage and database/////////////////////////////////////////////////

                        }
                        @Override
                        public void onFail() {
                            Toast.makeText(uploadVideo.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onProgress(float percent) {
                            Log.i("this is convertying", String.valueOf(percent));

                        }

                    });



                }
            }
        });










    }

    //retriveVideoFrameFROMvIDEO
    public static Bitmap retriveVideoFrameFromVideo(String videoPath) throws Throwable {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.getMessage());

        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }

    //Thumbnail storing in FirebaseStorage and then get their link and put into firebaseDatabase
    public void thumbnailProcessing(String url,String contentId) throws Throwable {
        String thumbnailName = UUID.randomUUID().toString()+".jpg";
        Bitmap thumbnail = retriveVideoFrameFromVideo(url);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.PNG,10,stream);
        byte[] byteArray = stream.toByteArray();
        firebaseStorage.getReference().child("Thumbnail").child(thumbnailName).putBytes(byteArray)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri turi) {

                                firebaseDatabase.getReference().child("users").child(firebaseAuth.getCurrentUser().getUid()).child(contentId)
                                        .child("thumbnailurl").setValue(turi.toString());
                                uploadingProgressBar.setVisibility(View.GONE);
                                Toast.makeText(uploadVideo.this, "Successfull uploaded", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }


                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(uploadVideo.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                uploadingProgressBar.setVisibility(View.GONE);
            }
        });


        }


}
