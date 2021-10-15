package com.rigup.memest;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class username extends Fragment {
    View view;
    Button uploadBotton;
    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    static Button signButton;
    public username(){
        // require a empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if(view==null){
            view = inflater.inflate(R.layout.fragment_username, container, false);

        }

        uploadBotton = view.findViewById(R.id.uploadButton);
        uploadBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Service Not available", Toast.LENGTH_SHORT).show();
            }
        });


        signButton = view.findViewById(R.id.signInButton);
        if(firebaseAuth.getCurrentUser()!=null){
            signButton.setText("Signout");
        }else{
            signButton.setText("SignIn");
        }


        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(signButton.getText().equals("SignIn")){
                    Intent intent = new Intent(getContext(),com.rigup.memest.userAuthentication.RegistrationActivity.class);
                    startActivity(intent);
                }else{
                    firebaseAuth.signOut();

                }


            }
        });



        return  view;
    }





}