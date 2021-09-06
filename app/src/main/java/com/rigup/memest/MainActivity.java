package com.rigup.memest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.VideoView;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
///////////////////////////////////////////////////////


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
    ArrayList<VideoModel> arrayListVideos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void init(){
        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewLayoutManager = new GridLayoutManager(getApplicationContext(),2);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        arrayListVideos = new ArrayList<>();
        fetchVideoUrlFromDatabase();
    }

    public void fetchVideoUrlFromDatabase(){

            FirebaseDatabase.getInstance().getReference().child("Memes Video").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Log.i("asdfasdf", postSnapshot.child("url").getValue().toString());
                        VideoModel videoModel = new VideoModel();

                        videoModel.setStr_path(postSnapshot.child("url").getValue().toString());
                        arrayListVideos.add(videoModel);
                    }
                    VideoAdapter videoAdapter = new VideoAdapter(getApplicationContext(), arrayListVideos, MainActivity.this);
                    recyclerView.setAdapter(videoAdapter);


//                    Log.i("arrayListVideos", arrayListVideos.toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


//                    addChildEventListener(new ChildEventListener() {
//                @Override
//                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//
//
//                }
//
//                @Override
//                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
//                @Override
//                public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
//                @Override
//                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {}
//
//
//            });
//










    }

}
