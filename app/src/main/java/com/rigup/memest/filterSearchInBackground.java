package com.rigup.memest;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rigup.memest.Adapter.VideoAdapter;
import com.rigup.memest.Model.VideoModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class filterSearchInBackground extends AsyncTask<String,Void,Void> {
    private WeakReference<MainActivity> activityWeakReference;
    boolean founded=false;

    public filterSearchInBackground(MainActivity activity) {
        activityWeakReference = new WeakReference<MainActivity>(activity);
    }

    @Override
    protected Void doInBackground(String... strings) {


        FirebaseDatabase.getInstance().getReference().child("Memes Video").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> filterVideoUrl = new ArrayList<>();
                ArrayList<String> filterImagesurl = new ArrayList<>();
                MainActivity activity = activityWeakReference.get();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                    if(postSnapshot.getKey().toLowerCase().contains(strings[0].toLowerCase()) || postSnapshot.child("Keyword").getValue().toString().contains(strings[0].toLowerCase())){
                        founded= true;
                        Log.i("query",strings[0].toLowerCase().toString());
                        Log.i("asdfasdf", postSnapshot.child("url").getValue().toString());
                        filterImagesurl.add(postSnapshot.child("thumbnail").getValue().toString());
                        filterVideoUrl.add(postSnapshot.child("url").getValue().toString());
                    }
                    Log.i("query", String.valueOf(founded));

                }

                if(founded){
                    try {

                        DownloadImagesInBackground filteredDownloadImagesInBackground = new DownloadImagesInBackground(activity);
                        activity.totalDownlaodImages = 0;
                        ArrayList<VideoModel> downloadedImagesArray = filteredDownloadImagesInBackground.execute(filterImagesurl).get();
                        VideoAdapter videoAdapter = new VideoAdapter(activity.getApplicationContext(), downloadedImagesArray, activity, filterVideoUrl);
                        activity.recyclerView.setAdapter(videoAdapter);
                        activity.progressBar.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }




            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }



        });
        return null;
    }
}
