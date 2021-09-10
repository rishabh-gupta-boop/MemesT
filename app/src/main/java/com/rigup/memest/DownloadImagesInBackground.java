package com.rigup.memest;


import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.rigup.memest.Adapter.VideoAdapter;
import com.rigup.memest.Model.VideoModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class DownloadImagesInBackground extends AsyncTask<ArrayList<String>, ArrayList<VideoModel>,ArrayList<VideoModel>>  {
    private WeakReference<MainActivity> activityWeakReference;
    private int totalDownlaodedImages = 10;

    public DownloadImagesInBackground(MainActivity activity) {
        activityWeakReference = new WeakReference<MainActivity>(activity);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        MainActivity activity =activityWeakReference.get();
        if(activity == null || activity.isFinishing()){
            return;
        }
        Log.i("onPregress", "started");
        activity.progressBar.setVisibility(View.VISIBLE);



    }

    @Override
    protected ArrayList<VideoModel> doInBackground(ArrayList<String>... arrayLists) {
        ArrayList<VideoModel> downlaodedImagesArray = new ArrayList<>();
        if(arrayLists[0].size()>0){
            for(int i=0;i<totalDownlaodedImages;i++){
                if(arrayLists[0].get(i)!=null){
                    try {
                        Bitmap bitmap = retriveVideoFrameFromVideo(arrayLists[0].get(i));

                        if (bitmap != null) {
                            VideoModel videoModel = new VideoModel();
                            videoModel.setDownloadImages(bitmap);
                            downlaodedImagesArray.add(videoModel);
//                            publishProgress(downlaodedImagesArray);

                        }
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }

                }
            }
        }

        return downlaodedImagesArray;
    }

    @Override
    protected void onProgressUpdate(ArrayList<VideoModel>... values) {
        super.onProgressUpdate(values);


    }

    @Override
    protected void onPostExecute(ArrayList<VideoModel> arrayList) {
        super.onPostExecute(arrayList);
        MainActivity activity =activityWeakReference.get();
        if(activity == null || activity.isFinishing()){
            return;
        }
        activity.progressBar.setVisibility(View.GONE);
        VideoAdapter videoAdapter;
        videoAdapter = new VideoAdapter(activity, arrayList, activity);
        activity.recyclerView.setAdapter(videoAdapter);
    }


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


}
