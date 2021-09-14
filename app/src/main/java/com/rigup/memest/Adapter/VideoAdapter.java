package com.rigup.memest.Adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.rigup.memest.DownloadImagesInBackground;
import com.rigup.memest.MainActivity;
import com.rigup.memest.Model.VideoModel;
import com.rigup.memest.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static android.content.Context.DOWNLOAD_SERVICE;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    Context context;
    ArrayList<VideoModel> downlaodedImagesArray;
    Activity activity;
    VideoView videoView;
    ArrayList<String> videoUrl;

    public VideoAdapter(Context context,ArrayList<VideoModel>  downlaodedImagesArray, Activity activity, ArrayList<String> videoUrl){
        this.context = context;
        this.downlaodedImagesArray = downlaodedImagesArray;
        this.activity = activity;
        this.videoUrl = videoUrl;


    }




    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_video, parent, false);
        return new VideoAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.isMemoryCacheable();
        holder.imageView.setImageBitmap(downlaodedImagesArray.get(position).getDownloadImages());




        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                final View videoContactView =  LayoutInflater.from(activity).inflate(R.layout.video_popup,null);
                videoView = videoContactView.findViewById(R.id.videoView);
                videoView.setVideoURI(Uri.parse(videoUrl.get(position).toString()));
                Log.i("imageUrll", videoUrl.get(position).toString());
                videoView.requestFocus();
                videoView.start();

                alertDialogBuilder.setView(videoContactView);
                Dialog dialog = alertDialogBuilder.create();
                dialog.show();
            }
        });


        holder.shareButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareVideo SV = new ShareVideo(videoUrl.get(position),context,activity);
                Log.i("video urlll", videoUrl.get(position));
                SV.execute();
            }
        });
    }

    @Override
    public int getItemCount() {

//        Log.i("array list used", String.valueOf(downloadImagesInBackground.size()));
        return downlaodedImagesArray.size();
//        return 20;
    }


    public static  class ViewHolder extends  RecyclerView.ViewHolder{
        ImageView imageView;
        Button shareButtonView;
//        RelativeLayout r1_select;

        public ViewHolder(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_image);
            shareButtonView= itemView.findViewById(R.id.shareButtonView);
//            r1_select = itemView.findViewById(R.id.r1_select);


        }


    }

    public void addImagess(VideoModel images){

        downlaodedImagesArray.add(images);

        notifyDataSetChanged();
    }


}


class ShareVideo extends AsyncTask<String ,String,String>{

    String videourl;
    Context context;
    Activity activity;

    public ShareVideo(String videourl, Context context,Activity activity) {
        this.videourl = videourl;
        this.context = context;
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... strings) {
        sharevideFile(videourl, "video.mp4",activity);
        Log.i("video url", videourl);
        return null;
    }

    @Override
    protected void onPostExecute(String s) {

//        Intent sharintent=new Intent("android.intent.action.SEND");
//        sharintent.setType("video/mp4");
//        sharintent.putExtra("android.intent.extra.STREAM", Uri.parse("content:///sdcard/DIRECTORY_DOWNLOADS/Awesome_Wp_Video/video.mp4"));
//        context.startActivity(Intent.createChooser(sharintent,"share"));

        super.onPostExecute(s);
    }





    private void sharevideFile(String videourl, String name,Activity activity) {

        try {
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.INTERNET},1);
            }

//            String rootDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
//
//            File rootFile = new File(rootDir);

            Uri downloadUri = Uri.parse(videourl);
            Log.i("video URL", videourl);
            DownloadManager downloadManager = (DownloadManager) activity.getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
//            Log.i("dsfasd", context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString());
            request.setDestinationInExternalFilesDir(context,Environment.DIRECTORY_DOWNLOADS,UUID.randomUUID().toString()+".mp4");

            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setVisibleInDownloadsUi(true);
            request.setAllowedOverRoaming(false);
            request.setMimeType("video/mp4");


            downloadManager.enqueue(request);
        } catch (Exception e) {
            Log.d("Error....", e.toString());
        }



    }

}



