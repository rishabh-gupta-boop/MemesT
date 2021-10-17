package com.rigup.memest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import com.rigup.memest.Upload.uploadVideo;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URI;

import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.checkSelfPermission;

public class username extends Fragment {
    View view;
    Button uploadBotton;
    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    static Button signButton;
    private WeakReference<MainActivity> activityWeakReference;
    public username(MainActivity activity){
        activityWeakReference = new WeakReference<MainActivity>(activity);
        // require a empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if(view==null){
            view = inflater.inflate(R.layout.fragment_username, container, false);

        }


        uploadBotton = view.findViewById(R.id.uploadButton);
        uploadBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseAuth.getCurrentUser()!=null){
                    MainActivity activity = activityWeakReference.get();
                    if(checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ){
                        if(checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ){

                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                        }
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

                    }else{
                        Intent uploadIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(uploadIntent, 1);
                    }

                }else{
                    Intent intent = new Intent(getContext(),com.rigup.memest.userAuthentication.RegistrationActivity.class);
                    startActivity(intent);
                }




            }
        });


        signButton = view.findViewById(R.id.signInButton);
        if(firebaseAuth.getCurrentUser()!=null){
            signButton.setText("Signout");
        }else{
            signButton.setText("SignIn");
        }


        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(signButton.getText().equals("SignIn")){
                    Intent intent = new Intent(getContext(),com.rigup.memest.userAuthentication.RegistrationActivity.class);
                    startActivity(intent);
                }else{
                    firebaseAuth.signOut();
                    signButton.setText("SignIn");


                }


            }
        });



        return  view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MainActivity activity = activityWeakReference.get();
        if(requestCode==1 &&  resultCode==RESULT_OK && data!=null){
            Uri selectedVideo = data.getData();

            Intent intent = new Intent(getContext(),com.rigup.memest.Upload.uploadVideo.class);
            intent.putExtra("selectedVideo", selectedVideo);
            Log.i("failedddZ", "asdf");
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){

            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Intent uploadIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(uploadIntent, 1);
            }
        }
    }
}