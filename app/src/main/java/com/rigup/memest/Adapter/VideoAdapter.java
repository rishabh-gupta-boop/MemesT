package com.rigup.memest.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.rigup.memest.Model.VideoModel;
import com.rigup.memest.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    Context context;
    ArrayList<VideoModel> downloadedImagesArray;
    Activity activity;
    public VideoAdapter(Context context, ArrayList<VideoModel> downloadedImagesArray, Activity activity){
        this.context = context;
        this.downloadedImagesArray = downloadedImagesArray;
        this.activity = activity;

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
//        holder.imageView.setImageBitmap(downloadedImagesArray.get(position).getDownloadImages());


//        holder.r1_select.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(context, VideoPlayerActivity.class);
//                i.putExtra("videos", arrayListVideos.get(position).getStr_path());
//                activity.startActivity(i);
//            }
//        });
    }

    @Override
    public int getItemCount() {

        Log.i("array list used", String.valueOf(downloadedImagesArray.size()));
//        return downloadedImagesArray.size();
        return 10;
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

    public void addImages(ArrayList<VideoModel> images){
        for(VideoModel im:images){
            downloadedImagesArray.add(im);
        }
        notifyDataSetChanged();
    }

}


