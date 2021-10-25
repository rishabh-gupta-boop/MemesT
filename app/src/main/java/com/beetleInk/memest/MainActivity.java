package com.beetleInk.memest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.beetleInk.memest.Adapter.VideoAdapter;
import com.beetleInk.memest.Model.VideoModel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;




public class MainActivity extends AppCompatActivity {
    SearchView.SearchAutoComplete searchAutoComplete;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.searchBar);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setQueryHint("Search Here!");

        searchAutoComplete = (SearchView.SearchAutoComplete)searchView.findViewById(R.id.search_src_text);
//        searchAutoComplete.setBackgroundColor(Color.WHITE);
//        searchAutoComplete.setTextColor(Color.BLACK);
        searchAutoComplete.setDropDownBackgroundResource(android.R.color.white);
        searchAutoComplete.setThreshold(1);
        // Create a new ArrayAdapter and add data to search auto complete object.
        ArrayAdapter<String> newsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, autoCompleteSearchList);

        searchAutoComplete.setAdapter(newsAdapter);

        // Listen to search view item on click event.
        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                String queryString=(String)adapterView.getItemAtPosition(itemIndex);
                searchAutoComplete.setText("" + queryString);
                filteredSearch(queryString);
//                Toast.makeText(MainActivity.this, "you clicked " + queryString, Toast.LENGTH_LONG).show();
            }
        });



        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                filteredSearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                recordedQuery=s;

                return false;
            }





        });


        return super.onCreateOptionsMenu(menu);
    }

    public void filteredSearch( String s){
        if(s!=""){

            imagesurl.clear();
            videoUrl.clear();
            videoNamee.clear();
            boolean founded =false;
            for (DataSnapshot postSnapshot : filterUsedData.getChildren()) {
                videoAdapter.clearedImages();
                int count =1;
                Log.i("number of connects", String.valueOf(count));
                count++;
                if (postSnapshot.getKey().toLowerCase().contains(s.toLowerCase()) || postSnapshot.child("keyword").getValue().toString().contains(s.toLowerCase())) {
                    progressBar.setVisibility(View.VISIBLE);
                    founded= true;
                    imagesurl.add(postSnapshot.child("thumbnail url").getValue().toString());
                    videoUrl.add(postSnapshot.child("video url").getValue().toString());
                    videoNamee.add(postSnapshot.child("video id").getValue().toString());
                }


            }

            if(founded){
                try {
                    totalDownlaodImages = 0;
                    downloadImagesInBackground = new DownloadImagesInBackground(MainActivity.this, videoUrl);
                    downloadImagesInBackground.execute(imagesurl);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(MainActivity.this, "No Result found", Toast.LENGTH_SHORT).show();
            }



        }else{

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.searchBar:
                Toast.makeText(this, "Search bar", Toast.LENGTH_SHORT).show();
                return true;

            case android.R.id.home:
                searchAutoComplete.setText(null);
                totalDownlaodImages = 0;
                videoAdapter.clearedImages();
                imagesurl.clear();
                videoUrl.clear();
                videoNamee.clear();
                progressBar.setVisibility(View.VISIBLE);
                fetchVideoUrlFromDatabase();
                return true;
            default:
                Toast.makeText(this, "nothing Pressed", Toast.LENGTH_SHORT).show();
                return false;
        }

    }






    RecyclerView recyclerView;
    RecyclerView.LayoutManager recyclerViewLayoutManager;
    ArrayList<String> imagesurl;
    int totalDownlaodImages = 0;
    ProgressBar progressBar;
    ProgressBar bottomProgressBar;
    String recordedQuery;
    GridLayoutManager layoutManager;
    DownloadImagesInBackground downloadImagesInBackground;
    ArrayList<VideoModel> downloadedImagesArray;
    VideoAdapter videoAdapter;
    ArrayList<String> videoUrl;
    SearchView searchView;
    DataSnapshot filterUsedData;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    private boolean isLoading=true;
    private int pastVisibleItems,visibleItemCount,totalItemCount,previosTotal = 0;
    private  int view_threshold = 10;
    ArrayList<String> autoCompleteSearchList;
    ArrayList<String> videoNamee;
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
        videoNamee = new ArrayList<String>();
        recyclerViewLayoutManager = new GridLayoutManager(this,2);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        autoCompleteSearchList = new ArrayList<>();
        downloadedImagesArray = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        videoUrl = new ArrayList<>();
        searchView = findViewById(R.id.searchBar);
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();


        fetchVideoUrlFromDatabase();




    }
    //get the database links and name and send to downloading these images and then arrayAdapter to set in recyclerView
    public void fetchVideoUrlFromDatabase(){

        FirebaseDatabase.getInstance().getReference().child("Attempted Memes Video").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                filterUsedData = snapshot;
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    autoCompleteSearchList.add(postSnapshot.getKey().toLowerCase());
                    imagesurl.add(postSnapshot.child("thumbnail url").getValue().toString());
                    videoUrl.add(postSnapshot.child("video url").getValue().toString());
                    videoNamee.add(postSnapshot.child("video id").getValue().toString());
                }


                try {
                    downloadImagesInBackground = new DownloadImagesInBackground(MainActivity.this, videoUrl);
                    downloadImagesInBackground.execute(imagesurl);

                } catch (Exception e) {
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
                    ScrollDownResult scrollDownResult = new ScrollDownResult(MainActivity.this,videoUrl);
                    scrollDownResult.execute(imagesurl);
                    Log.i("running", downloadImagesInBackground.getStatus().toString());

                }

              }
    //Get the Firebase Storage url and title and set into database.
    public void getDownload(){
        DatabaseReference memesVideoDatabase = FirebaseDatabase.getInstance().getReference().child("Attempted Memes Video");


        FirebaseStorage.getInstance().getReference().child("Memes Video").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference prefix : listResult.getPrefixes()) {
                    Log.i("this is prefix", prefix.getName());
                    FirebaseStorage.getInstance().getReference().child("Memes Video").child(prefix.getName())
                            .listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            for (StorageReference item : listResult.getItems()) {
                                // All the items under listRef.
                                item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String url = uri.toString();
                                        memesVideoDatabase.child(item.getName().substring(0, item.getName().indexOf("."))).child("video url").setValue(url);
                                        memesVideoDatabase.child(item.getName().substring(0, item.getName().indexOf("."))).child("video id").setValue(UUID.randomUUID().toString()+".mp4");
                                        memesVideoDatabase.child(item.getName().substring(0, item.getName().indexOf("."))).child("video title").setValue(item.getName()
                                                .indexOf(0,item.getName().indexOf(".")));

                                        try {
                                            thumbnailProcessing(uri.toString(),item.getName().substring(0, item.getName().indexOf(".")));
                                        } catch (Throwable throwable) {
                                            throwable.printStackTrace();
                                            Log.i("something wrong", throwable.toString());
                                        }
                                        memesVideoDatabase.child(item.getName().substring(0, item.getName().indexOf("."))).child("keyword").setValue(prefix.getName());

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
                            memesVideoDatabase.child(item.getName().substring(0,item.getName().indexOf("."))).child("video url").setValue(url);
                            memesVideoDatabase.child(item.getName().substring(0,item.getName().indexOf("."))).child("keyword").setValue("");
                            memesVideoDatabase.child(item.getName().substring(0, item.getName().indexOf("."))).child("video id").setValue(UUID.randomUUID().toString()+".mp4");
                            memesVideoDatabase.child(item.getName().substring(0, item.getName().indexOf("."))).child("video title").setValue(item.getName()
                                    .indexOf(0,item.getName().indexOf(".")));
                            try {
                                thumbnailProcessing(uri.toString(),item.getName().substring(0, item.getName().indexOf(".")));
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                                Log.i("something wrong", throwable.toString());
                            }

                        }
                    });


                }
            }
        });




    }

    //Recieve url of video extract it thumbnail and return a bitmap
    public  Bitmap retriveVideoFrameFromVideo(String videoPath) throws Throwable {
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


    //Upload this bitmap to the firebase storage and firebase database
    public void thumbnailProcessing(String url,String contentId) throws Throwable {
        String thumbnailName = UUID.randomUUID().toString()+".webp";
        Bitmap thumbnail = retriveVideoFrameFromVideo(url);

        //Bitmap convert into byte array...
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.WEBP,0,stream);
        byte[] byteArray = stream.toByteArray();






        firebaseStorage.getReference().child("AttemptImages").child(thumbnailName).putBytes(byteArray)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i("Url",taskSnapshot.getUploadSessionUri().toString());
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri turi) {

                                firebaseDatabase.getReference().child("Attempted Memes Video").child(contentId)
                                        .child("thumbnail url").setValue(turi.toString());
                                Log.i("Url Getting noErro", turi.toString());

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull  Exception e) {
                                Log.i("Url getting error", e.toString());
                            }
                        });
                    }


                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {
                Log.i("Url getting error whole", e.toString());
            }
        });


    }


}










