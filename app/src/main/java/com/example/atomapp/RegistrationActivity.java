package com.example.atom_task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegistrationActivity extends AppCompatActivity{

    final String TAG = "Registration Activity :";

    // navigation buttons
    private MaterialButton home_navigation_button;
    private ImageButton back_btn;

    String user_name;
    private EditText person_name;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private DatabaseReference mDatabase;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        initialiseFields();
        clickListeners();
    }

    private void initialiseFields(){
        person_name = findViewById(R.id.edit_txt_person_name);
        home_navigation_button = findViewById(R.id.home_navigation_button);
        back_btn = findViewById(R.id.backbtn);

        mAuth = FirebaseAuth.getInstance();
        currentUser  = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        final FirebaseUser user = currentUser;
        getUserDetail(user);

    }

    private void clickListeners(){
        home_navigation_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMainActivity();

            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLoginActivity();
            }
        });
    }

    private void startMainActivity(){
        user_name = person_name.getText().toString();
        if (user_name.trim().equals("")){
            Toast.makeText(getApplicationContext(),"Please Enter User Name",Toast.LENGTH_SHORT).show();
        }else {
            Boolean check = _save_username_inDB();
            if (!check){
                Toast.makeText(getApplicationContext(),"Something went wrong",Toast.LENGTH_SHORT).show();
            }else {
                Intent main_intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(main_intent);
                finish();
            }
        }
    }

    private Boolean _save_username_inDB(){
        final Boolean[] check = {true};
        uid = currentUser.getUid();
        DatabaseReference uidRef = mDatabase.child(uid);
        uidRef.child("name").setValue(user_name).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()){
                    check[0] = false;
                }
            }
        });
        return check[0];
    }

    // Start Login Activity
    private void startLoginActivity(){
        Intent login_intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(login_intent);
        finish();
    }

    // Update the activity detail
    private void getUserDetail(FirebaseUser user) {
        uid = user.getUid();
        DatabaseReference uidRef = mDatabase.child(uid);
        getUserFromDatabase(uidRef);
        Log.d(TAG,"Username : " + user_name);
    }

    // Get User Detial from Realtime database
    private void getUserFromDatabase(DatabaseReference ref){
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") )){
                    String name = dataSnapshot.child("name").getValue().toString();
                    user_name = name;
                    person_name.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}