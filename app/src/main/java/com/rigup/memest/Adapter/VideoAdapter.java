package com.rigup.memest.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.rigup.memest.DownloadImagesInBackground;
import com.rigup.memest.MainActivity;
import com.rigup.memest.Model.VideoModel;
import com.rigup.memest.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

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




        holder.r1_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                final View videoContactView =  LayoutInflater.from(activity).inflate(R.layout.video_popup,null);
                videoView = videoContactView.findViewById(R.id.videoView);
                videoView.setVideoURI(Uri.parse(videoUrl.get(position)));
                Log.i("imageUrll", videoUrl.get(position));
                videoView.requestFocus();
                videoView.start();

                alertDialogBuilder.setView(videoContactView);
                Dialog dialog = alertDialogBuilder.create();
                dialog.show();

//                Intent i = new Intent(context, VideoPlayerActivity.class);
//                i.putExtra("videos", arrayListVideos.get(position).getStr_path());
//                activity.startActivity(i);
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
        RelativeLayout r1_select;

        public ViewHolder(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_image);
            r1_select = itemView.findViewById(R.id.r1_select);


        }


    }

    public void addImagess(VideoModel images){

        downlaodedImagesArray.add(images);

        notifyDataSetChanged();
    }

    public class videoPopUp extends AsyncTask<String, Void, VideoView> {

        @Override
        protected VideoView doInBackground(String... url) {


            return null;

        }

        @Override
        protected void onPostExecute(VideoView videoView) {
            super.onPostExecute(videoView);

        }
    }

}


