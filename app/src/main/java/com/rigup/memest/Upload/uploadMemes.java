//package com.rigup.memest.Upload;
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.provider.MediaStore;
//
//public class uploadMemes extends AsyncTask<Uri,Void, String> {
//    Uri selectedVideo;
//    Activity mainActivity;
//    public uploadMemes(Uri data, Activity activity) {
//        selectedVideo = data;
//        mainActivity = activity;
//    }
//
//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//        if(mainActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
//            mainActivity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
//        }
//    }
//
//    @Override
//    protected String doInBackground(Uri... uris) {
//        return null;
//    }
//}
