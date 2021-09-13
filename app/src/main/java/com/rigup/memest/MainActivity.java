package com.rigup.memest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.rigup.memest.Adapter.VideoAdapter;
import com.rigup.memest.Model.VideoModel;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.searchBar);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search Here!");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.searchBar:
                Toast.makeText(this, "Search bar", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.savedMemes:
                Toast.makeText(this, "Saved Memes", Toast.LENGTH_SHORT).show();
                return true;
            default:
                Toast.makeText(this, "nothing Pressed", Toast.LENGTH_SHORT).show();
                return false;
        }

    }

    public void getDownload(){
        DatabaseReference memesVideoDatabase = FirebaseDatabase.getInstance().getReference().child("Memes Video");


        FirebaseStorage.getInstance().getReference().child("Memes Video").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference prefix : listResult.getPrefixes()) {
                    Log.i("this is prefix", prefix.getName());
                    FirebaseStorage.getInstance().getReference().child("Memes Video").child(prefix.getName()).listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            for (StorageReference item : listResult.getItems()) {
                                // All the items under listRef.
                                item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String url = uri.toString();
                                        memesVideoDatabase.child(item.getName().substring(0, item.getName().indexOf("."))).child("url").setValue(url);
                                        memesVideoDatabase.child(item.getName().substring(0, item.getName().indexOf("."))).child("Keyword").setValue(prefix.getName());

                                    }
                                });
                            }
                        }
                    });
                }

                for (StorageReference item : listResult.getItems()) {
                    // All the items under listRef.
                        item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            Log.i("download url", url.toString());
                            Log.i("download url", item.getName().toString());
                            memesVideoDatabase.child(item.getName().substring(0,item.getName().indexOf("."))).child("url").setValue(url);
                            memesVideoDatabase.child(item.getName().substring(0,item.getName().indexOf("."))).child("Keyword").setValue("");

                        }
                    });


                }
            }
        });




    }


    RecyclerView recyclerView;
    RecyclerView.LayoutManager recyclerViewLayoutManager;
    ArrayList<String> imagesurl;
    int totalDownlaodImages = 0;
    ProgressBar progressBar;
    ProgressBar bottomProgressBar;
    GridLayoutManager layoutManager;
    DownloadImagesInBackground downloadImagesInBackground;
    ArrayList<VideoModel> downloadedImagesArray;
    boolean initialiseVideoAdapter = false;
    VideoAdapter videoAdapter;
    ArrayList<VideoModel> savedVideoModelsImages;

    //variable for pagination
    private boolean isLoading=true;
    private int pastVisibleItems,visibleItemCount,totalItemCount,previosTotal = 0;
    private  int view_threshold = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = layoutManager.getChildCount();//totol visible item
                totalItemCount = layoutManager.getItemCount();//total present item in your recyler view
                pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                if(dy>0||dy<0){
                    Log.i("pastVisisbleItemCount", String.valueOf(pastVisibleItems));
                    if(isLoading){
                        if(totalItemCount>(pastVisibleItems+visibleItemCount)){

                            bottomProgressBar.setVisibility(View.GONE);
                            isLoading = false;
                            previosTotal=totalItemCount;
                        }
                    }
                    if(!isLoading && (totalItemCount<=(pastVisibleItems+visibleItemCount))){
                        bottomProgressBar.setVisibility(View.VISIBLE);
                        isLoading = true;
                        performPagination();


                    }
                }
            }
        });
    }

    public void init(){
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        bottomProgressBar = findViewById(R.id.bottomProgressBar);
        imagesurl= new ArrayList<String>();
        recyclerViewLayoutManager = new GridLayoutManager(this,2);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        downloadImagesInBackground = new DownloadImagesInBackground(MainActivity.this);
        downloadedImagesArray = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);

        fetchVideoUrlFromDatabase();

    }

    public void fetchVideoUrlFromDatabase(){

        FirebaseDatabase.getInstance().getReference().child("Memes Video").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Log.i("asdfasdf", postSnapshot.child("thumbnail").getValue().toString());
                    imagesurl.add(postSnapshot.child("thumbnail").getValue().toString());

                }


                try {
                    downloadedImagesArray = downloadImagesInBackground.execute(imagesurl).get();
                    videoAdapter = new VideoAdapter(getApplicationContext(), downloadedImagesArray, MainActivity.this);
                    recyclerView.setAdapter(videoAdapter);
                    progressBar.setVisibility(View.GONE);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }



        });




    }

    private void performPagination() {




        Log.i("running", downloadImagesInBackground.getStatus().toString());
                if( downloadImagesInBackground.getStatus() != AsyncTask.Status.RUNNING ||downloadImagesInBackground.getStatus() == AsyncTask.Status.FINISHED ){
                    downloadImagesInBackground = new DownloadImagesInBackground(MainActivity.this);
                    downloadImagesInBackground.execute(imagesurl);


                    Log.i("running", downloadImagesInBackground.getStatus().toString());

                }

              }

}










