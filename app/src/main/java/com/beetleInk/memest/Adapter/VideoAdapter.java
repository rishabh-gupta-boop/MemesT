package com.beetleInk.memest.Adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.beetleInk.memest.Model.VideoModel;
import com.beetleInk.memest.R;

import java.io.File;
import java.util.ArrayList;

import static android.content.Context.DOWNLOAD_SERVICE;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    Context context;
    ArrayList<VideoModel> downlaodedImagesArray;
    Activity activity;
    VideoView videoView;
    ArrayList<String> videoUrl;
    ArrayList<String> videoName;
    float downloadID;
    ProgressBar videoLoadingProgressBar;
    AlertDialog.Builder alertDialogBuilder;
    View videoContactView;
    String contentNamed;
    String videoPathInFileManager;
    Boolean downloadOrShare=null;

    public VideoAdapter(Context context,ArrayList<VideoModel>  downlaodedImagesArray, Activity activity,
                        ArrayList<String> videoUrl, ArrayList<String> name){
        this.context = context;
        this.downlaodedImagesArray = downlaodedImagesArray;
        this.activity = activity;
        this.videoUrl = videoUrl;
        this.videoName = name;


    }




    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_video, parent, false);
        activity.registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        return new VideoAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.isMemoryCacheable();
        Log.i("downloadImagesArray", downlaodedImagesArray.toString());
        if(downlaodedImagesArray.get(position).getDownloadImages()!=null){
            holder.imageView.setImageBitmap(downlaodedImagesArray.get(position).getDownloadImages());
        }



        //compress file name given

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contentNamed = videoName.get(position);
                videoPathInFileManager = activity.getApplicationContext().getExternalFilesDir(null)
                        .getAbsolutePath()+File.separator+"Download"+File.separator+contentNamed;

                try {
                    if(ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.INTERNET},1);
                    }

                    //VideoView Dialog box view---------------------
                    alertDialogBuilder = new AlertDialog.Builder(activity);
                    videoContactView =  LayoutInflater.from(activity).inflate(R.layout.video_popup,null);
                    videoView = videoContactView.findViewById(R.id.videoView);
                    videoLoadingProgressBar = videoContactView.findViewById(R.id.videoLoadingProgressBar);
                    videoLoadingProgressBar.setVisibility(View.VISIBLE);
                    alertDialogBuilder.setView(videoContactView);
                    Dialog dialog = alertDialogBuilder.create();
                    dialog.show();
                    //-----------------------till now


                    //check the download video file found or not, if find then view in VideoView and if not then downlaod it and then show in videoView
                    if(!checkMemesVideoExist(contentNamed)){

                        downlaodMemesVideoFromFirebase(position);
                        downloadOrShare=true;


                    }else{
                        //Alert box for video
                        videoLoadingProgressBar.setVisibility(View.GONE);
                        videoView.setVideoPath(videoPathInFileManager);
                        videoView.requestFocus();

                        videoView.start();



                        ///end alert box
                    }

                    //end of checking on Broadcast Reciever left to show video View




                } catch (Exception e) {
                    Log.d("Error....", e.toString());
                }

                



            }
        });


        holder.shareButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.INTERNET},1);
                }

                contentNamed = videoName.get(position);
                videoPathInFileManager = activity.getApplicationContext().getExternalFilesDir(null)
                        .getAbsolutePath()+File.separator+"Download"+File.separator+contentNamed;
                if(!checkMemesVideoExist(contentNamed)){
                    videoLoadingProgressBar.setVisibility(View.VISIBLE);
                    downlaodMemesVideoFromFirebase(position);
                    downloadOrShare=false;


                }else{
                    //Show a share app options
                    Intent sharintent=new Intent("android.intent.action.SEND");
                    sharintent.setType("video/mp4");
                    sharintent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    //Uri.fromFile(new File(videoPathInFileManager))
                    sharintent.putExtra("android.intent.extra.STREAM", Uri.parse(videoPathInFileManager));
                    activity.startActivity(Intent.createChooser(sharintent,"Send to"));
                }


            }
        });
    }

    @Override
    public int getItemCount() {
        int position = 0;
        for(int i=0; i<downlaodedImagesArray.size();i++){
            if(downlaodedImagesArray.get(i)!=null){
                position++;
            }
        }
        return position;

    }


    public static  class ViewHolder extends  RecyclerView.ViewHolder{
        ImageView imageView;
        Button shareButtonView;



        public ViewHolder(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_image);
            shareButtonView= itemView.findViewById(R.id.shareButtonView);
        }


    }

    public void addImagess(VideoModel images){

        downlaodedImagesArray.add(images);


        notifyDataSetChanged();
    }

    public void clearedImages(){
        downlaodedImagesArray.clear();
        notifyDataSetChanged();
    }

    public void downlaodMemesVideoFromFirebase(int position){
        Uri downloadUri = Uri.parse(videoUrl.get(position));

        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);

        request.setDestinationInExternalFilesDir(context,Environment.DIRECTORY_DOWNLOADS,contentNamed);


        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setVisibleInDownloadsUi(false);
        request.setAllowedOverRoaming(false);
        request.setNotificationVisibility(0);
        request.setMimeType("video/mp4");


        downloadID = downloadManager.enqueue(request);
    }


    public Boolean checkMemesVideoExist(String VideoName){
        File filePath = new File(activity.getApplicationContext().getExternalFilesDir(null).getAbsolutePath()+File.separator+"Download"+File.separator+VideoName);


        if(filePath.exists()){
            return true;
        }else{
            return false;
        }
    }

    //When downloadig of video completed, it's  a listener
    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                videoLoadingProgressBar.setVisibility(View.GONE);
                if(downloadOrShare){
                    videoView.setVideoPath(videoPathInFileManager);
                    videoView.requestFocus();

                    videoView.start();
                }else{
                    //Show a share app options
                    Intent sharintent=new Intent("android.intent.action.SEND");
                    sharintent.setType("video/mp4");
                    sharintent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Log.i("asdfasdfasdf", Uri.parse(videoPathInFileManager).toString());
                    sharintent.putExtra("android.intent.extra.STREAM", Uri.parse(videoPathInFileManager));
                    activity.startActivity(Intent.createChooser(sharintent,"Send to"));
                }




            }
        }


    };
//OnDestroy function is not assigned if arise any problem then checkout the this function.

}





