package com.bry.raia.Services;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bry.raia.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class DatabaseManager {
    private final String TAG = DatabaseManager.class.getSimpleName();
    private Context mContext;
    private String BROADCAST_RECEIVER = "";

    public DatabaseManager(Context context, String receiver){
        this.mContext = context;
        this.BROADCAST_RECEIVER = receiver;
    }

    public DatabaseManager setUpNewUserInFirebase(String username, String email){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS).child(uid);
        usernameRef.child(Constants.USERNAME).setValue(username);
        usernameRef.child(Constants.EMAIL).setValue(email);
        usernameRef.child(Constants.TIME_OF_SIGNUP).setValue(Long.toString(Calendar.getInstance().getTimeInMillis()));

        return this;
    }

    public DatabaseManager loadUserDataFromFirebase(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS).child(uid);
        usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child(Constants.USERNAME).getValue(String.class);
                String email = dataSnapshot.child(Constants.EMAIL).getValue(String.class);

                new SharedPreferenceManager(mContext).setEmailInSharedPref(email);
                new SharedPreferenceManager(mContext).setNameInSharedPref(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return this;
    }
}
