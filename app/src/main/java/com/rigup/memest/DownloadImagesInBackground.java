package com.rigup.memest;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class DownloadImagesInBackground extends AsyncTask<Integer,Integer,String> {
    private WeakReference<MainActivity> activityWeakReference;




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


    }

    @Override
    protected String doInBackground(Integer... integers) {
        for(int i=0; i<integers[0];i++){
            publishProgress((i*100)/integers[0]);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return "Finish";
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        MainActivity activity =activityWeakReference.get();
        if(activity == null || activity.isFinishing()){
            return;
        }
        Log.i("onPregress", String.valueOf(values[0]));
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.i("onPregress", s);
        MainActivity activity =activityWeakReference.get();
        if(activity == null || activity.isFinishing()){
            return;
        }
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();

    }


}
