package com.rigup.memest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.graphics.Color;
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
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private void filter(String text) {



    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.searchBar);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search Here!");

        final SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete)searchView.findViewById(R.id.search_src_text);
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
            boolean founded =false;
            for (DataSnapshot postSnapshot : filterUsedData.getChildren()) {
                videoAdapter.clearedImages();
                int count =1;
                Log.i("number of connects", String.valueOf(count));
                count++;
                if (postSnapshot.getKey().toLowerCase().contains(s.toLowerCase()) || postSnapshot.child("Keyword").getValue().toString().contains(s.toLowerCase())) {
                    progressBar.setVisibility(View.VISIBLE);
                    founded= true;
                    imagesurl.add(postSnapshot.child("thumbnail").getValue().toString());
                    videoUrl.add(postSnapshot.child("url").getValue().toString());
                }


            }

            if(founded){
                try {
                    totalDownlaodImages = 0;
                    downloadImagesInBackground = new DownloadImagesInBackground(MainActivity.this, videoUrl);
                    downloadImagesInBackground.execute(imagesurl);


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
            case R.id.savedMemes:
                Toast.makeText(this, "Saved Memes", Toast.LENGTH_SHORT).show();
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
    private boolean isLoading=true;
    private int pastVisibleItems,visibleItemCount,totalItemCount,previosTotal = 0;
    private  int view_threshold = 10;
    ArrayList<String> autoCompleteSearchList;
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
        autoCompleteSearchList = new ArrayList<>();
        downloadedImagesArray = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        videoUrl = new ArrayList<>();
        searchView = findViewById(R.id.searchBar);

        fetchVideoUrlFromDatabase();

    }

    public void fetchVideoUrlFromDatabase(){

        FirebaseDatabase.getInstance().getReference().child("Memes Video").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                filterUsedData = snapshot;
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    autoCompleteSearchList.add(postSnapshot.getKey().toLowerCase());
                    Log.i("asdfasdf", postSnapshot.child("url").getValue().toString());
                    imagesurl.add(postSnapshot.child("thumbnail").getValue().toString());
                    videoUrl.add(postSnapshot.child("url").getValue().toString());
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
}










