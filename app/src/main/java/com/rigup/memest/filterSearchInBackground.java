package com.rigup.memest;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
    protected void onPreExecute() {
        super.onPreExecute();


    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(String... strings) {



        return null;
    }
}
