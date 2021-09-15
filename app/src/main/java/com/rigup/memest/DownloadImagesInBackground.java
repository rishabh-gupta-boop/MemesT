package com.rigup.memest;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.rigup.memest.Adapter.VideoAdapter;
import com.rigup.memest.Model.VideoModel;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class DownloadImagesInBackground extends AsyncTask<ArrayList<String>, ArrayList<VideoModel>,ArrayList<VideoModel>> {
    private WeakReference<MainActivity> activityWeakReference;
    private int totalDownlaodedImages = 10;
    private Object IndexOutOfBoundsException;

    public DownloadImagesInBackground(MainActivity activity) {
        activityWeakReference = new WeakReference<MainActivity>(activity);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }





    }

    @Override
    protected ArrayList<VideoModel> doInBackground(ArrayList<String>... arrayLists) {
        ArrayList<VideoModel> downlaodedImagesArray = new ArrayList<>();
        MainActivity activity = activityWeakReference.get();
        totalDownlaodedImages += activity.totalDownlaodImages;
        for (int i = activity.totalDownlaodImages; i < totalDownlaodedImages; i++) {
            activity.totalDownlaodImages++;

                try {
                    URL url = new URL(arrayLists[0].get(i));
                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                    InputStream in = urlConnection.getInputStream();
                    Bitmap myBitMap = BitmapFactory.decodeStream(in);
                    VideoModel videoModel = new VideoModel();
                    videoModel.setDownloadImages(myBitMap);
                    downlaodedImagesArray.add(videoModel);

                } catch (Exception e) {
                    e.printStackTrace();
                    downlaodedImagesArray.add(null);
                }


        }



        return downlaodedImagesArray;
    }





    @Override
    protected void onPostExecute(ArrayList<VideoModel> arrayList) {
        super.onPostExecute(arrayList);
        MainActivity activity =activityWeakReference.get();

        if(activity == null || activity.isFinishing()){
            return;
        }

        if(totalDownlaodedImages>10 && arrayList.size()>=10){
            for(int i=0; i<arrayList.size();i++){
                if(arrayList.get(i)!=null){
                    activity.videoAdapter.addImagess(arrayList.get(i));
                    activity.bottomProgressBar.setVisibility(View.GONE);

                }
            }


        }



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
