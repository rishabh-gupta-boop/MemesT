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
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.rigup.memest.R;
import com.vincent.videocompressor.VideoCompress;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class uploadVideo extends AppCompatActivity {
    Uri selectedVideo;
    EditText videoNameEditText;
    EditText videoTagsEditText;
    static ProgressBar uploadingProgressBar;
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
        Intent intent = getIntent();
        selectedVideo = intent.getParcelableExtra("selectedVideo");
        videoNameEditText.setText((String) (new File(selectedVideo.getLastPathSegment())).getName()
                .subSequence(0,(new File(selectedVideo.getLastPathSegment())).getName().indexOf(".")));







        uplaodButton = findViewById(R.id.uploadButton);
        uplaodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoNameEditText.getText().toString().equals("")  || videoTagsEditText.getText().toString().equals("")){
                    Toast.makeText(uploadVideo.this, "Please type title and  tags", Toast.LENGTH_SHORT).show();
                }else{

                    tags = new ArrayList<String>((Arrays.asList(videoTagsEditText.getText().toString().split("\\s*,\\s*"))));
                    Log.i("this is string", tags.toString());
                    uploadingVideo asdf = new uploadingVideo(uploadVideo.this, videoNameEditText, tags,selectedVideo);
                    asdf.execute();
                }
            }
        });





    }


}
//Uplaod video in firebase storage and get download url
class uploadingVideo extends AsyncTask<Void, Void, String>{

    Activity uploadVideoActivity;
    EditText videoNameEditText;
    ArrayList<String> videoTagsEditText=new ArrayList<>();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    Uri selectedVideo;
    private String outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getAbsolutePath() + File.separator + "VID_.mp4";
    public uploadingVideo(Activity activity, EditText videoTitle, ArrayList<String> videoTags, Uri selectedVid) {
        uploadVideoActivity = activity;
        videoNameEditText = videoTitle;
        videoTagsEditText = videoTags;
        selectedVideo = selectedVid;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        uploadVideo.uploadingProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected String doInBackground(Void... voids) {

        //Compress video into lower size--------------------------------------------------------------------------------------------

        VideoCompress.compressVideoLow(selectedVideo.getLastPathSegment(), outputDir, new VideoCompress.CompressListener() {
            @Override
            public void onStart() {
                Log.i("startttttt", "started");
            }

            @Override
            public void onSuccess() {
                Log.i("startttttt", "success");
            }

            @Override
            public void onFail() {
                Log.i("startttttt", "failed");
            }

            @Override
            public void onProgress(float percent) {
                Log.i("startttttt", String.valueOf(percent)+"progress");
            }
        });

//        try {
//
//
//            firebaseStorage.getReference().child(UUID.randomUUID().toString()+".mp4").putStream(new FileInputStream(new File(selectedVideo.getLastPathSegment())))
//            .addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull @NotNull Exception e) {
//                    Log.i("faileddd", e.toString());
//                }
//            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    //get downlaod url of the video in firebase
//                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            Log.i("this is test",uri.toString());
//                        }
//                    });
//
//
//                }
//            });
//
//        } catch (Exception e) {
//            Log.i("asdfasdf", e.toString() );
//        }
        return null;

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        uploadVideo.uploadingProgressBar.setVisibility(View.GONE);
        Toast.makeText(uploadVideoActivity, "Memes Uploaded", Toast.LENGTH_SHORT).show();
    }


}