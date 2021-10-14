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
    private WeakReference<home> activityWeakReference;
    private int totalDownlaodedImages = 10;
    ArrayList<String> videoUrl = new ArrayList<>();

    public DownloadImagesInBackground(home activity, ArrayList<String> VideoUrl) {
        activityWeakReference = new WeakReference<home>(activity);
        videoUrl = VideoUrl;


    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        home activity = activityWeakReference.get();
//         ||







    }

    @Override
    protected ArrayList<VideoModel> doInBackground(ArrayList<String>... arrayLists) {
        ArrayList<VideoModel> downlaodedImagesArray = new ArrayList<>();
        home activity = activityWeakReference.get();
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
        home activity =activityWeakReference.get();

        Log.i("this is not working", arrayList.toString());

        activity.videoAdapter = new VideoAdapter(activity.getContext(), arrayList,  activity.getActivity(),videoUrl );
        activity.recyclerView.setAdapter(activity.videoAdapter);
        activity.progressBar.setVisibility(View.GONE);

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
