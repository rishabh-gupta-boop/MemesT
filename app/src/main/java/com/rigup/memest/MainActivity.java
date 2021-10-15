package com.rigup.memest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.app.FragmentManager;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.rigup.memest.Adapter.VideoAdapter;
import com.rigup.memest.Model.VideoModel;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity{
    home myhome = new home();
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
        ArrayAdapter<String> newsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, home.autoCompleteSearchList);

        searchAutoComplete.setAdapter(newsAdapter);

        // Listen to search view item on click event.
        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                String queryString=(String)adapterView.getItemAtPosition(itemIndex);
                searchAutoComplete.setText("" + queryString);
                filteredSearch(queryString);

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


                return false;
            }





        });


        return super.onCreateOptionsMenu(menu);
    }

    public void filteredSearch( String s){
        if(s!=""){
            home.imagesurl.clear();
            home.videoUrl.clear();
            boolean founded =false;
            for (DataSnapshot postSnapshot : home.filterUsedData.getChildren()) {
                home.videoAdapter.clearedImages();
                int count =1;
                Log.i("number of connects", String.valueOf(count));
                count++;
                if (postSnapshot.getKey().toLowerCase().contains(s.toLowerCase()) || postSnapshot.child("Keyword").getValue().toString().contains(s.toLowerCase())) {
                    Log.i("thisisit", "yesss");
                    home.progressBar.setVisibility(View.VISIBLE);
                    founded= true;
                    home.imagesurl.add(postSnapshot.child("thumbnail").getValue().toString());
                    home.videoUrl.add(postSnapshot.child("url").getValue().toString());
                }


            }

            if(founded){
                try {

                    home.totalDownlaodImages = 0;
                    home.downloadImagesInBackground = new DownloadImagesInBackground(myhome, home.videoUrl);
                    home.downloadImagesInBackground.execute(home.imagesurl);


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
                Toast.makeText(MainActivity.this, "Search bar", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.savedMemes:
                Toast.makeText(MainActivity.this, "Saved Memes", Toast.LENGTH_SHORT).show();
                return true;
            default:
                Toast.makeText(MainActivity.this, "nothing Pressed", Toast.LENGTH_SHORT).show();
                return false;
        }

    }






    static  BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigation();


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


//    ---------------------------------------------Bottom Navigation----------------------------------------------------------------------

    public void bottomNavigation(){
        home firstFragment = new home();
        username secondFragment = new username();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.Home:

                        FragmentManager fragmentManager = getFragmentManager();

//                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, firstFragment).commit();
                        return true;

                    case R.id.username:

                        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, secondFragment).commit();
                        return true;


                }

                return false;
            }
        });


        bottomNavigationView.setSelectedItemId(R.id.Home);




    }




}










