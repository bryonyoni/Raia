package com.bry.raia.Services;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.bry.raia.Constants;
import com.bry.raia.Models.Announcement;
import com.bry.raia.Models.Petition;
import com.bry.raia.Models.Poll;
import com.google.android.gms.tasks.OnSuccessListener;
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

    public DatabaseManager uploadAnnouncement(Announcement announcement){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String name = new SharedPreferenceManager(mContext).loadNameInSharedPref();

        DatabaseReference uploadRef = FirebaseDatabase.getInstance().getReference(Constants.ANNOUNCEMENTS);
        DatabaseReference dbRef = uploadRef.push();
        String pushRef = dbRef.getKey();

        announcement.setUploaderId(uid);
        announcement.setUploaderEmail(email);
        announcement.setUploaderUsername(name);
        announcement.setAnnouncementId(pushRef);

        DatabaseReference imageRef = FirebaseDatabase.getInstance().getReference(Constants.UPLOAD_IMAGES).child(pushRef);
        imageRef.setValue(announcement.getEncodedAnnouncementImage());

        DatabaseReference myuploadRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS).child(uid).child(Constants.UPLOADED_ANNOUNCEMENTS);
        DatabaseReference mydbRef = myuploadRef.push();
        mydbRef.setValue(pushRef);

        announcement.setEncodedAnnouncementImage("");
        dbRef.setValue(announcement).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(BROADCAST_RECEIVER));
            }
        });


        return this;
    }

    public DatabaseManager uploadPetition(Petition petition){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String name = new SharedPreferenceManager(mContext).loadNameInSharedPref();

        DatabaseReference uploadRef = FirebaseDatabase.getInstance().getReference(Constants.PETITIONS);
        DatabaseReference dbRef = uploadRef.push();
        String pushRef = dbRef.getKey();

        petition.setUploaderId(uid);
        petition.setUploaderEmail(email);
        petition.setUploaderUsername(name);
        petition.setPetitionId(pushRef);

        DatabaseReference imageRef = FirebaseDatabase.getInstance().getReference(Constants.UPLOAD_IMAGES).child(pushRef);
        imageRef.setValue(petition.getEncodedPetitionImage());

        DatabaseReference myuploadRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS).child(uid).child(Constants.UPLOADED_PETITIONS);
        DatabaseReference mydbRef = myuploadRef.push();
        mydbRef.setValue(pushRef);

        petition.setEncodedPetitionImage("");
        dbRef.setValue(petition).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(BROADCAST_RECEIVER));
            }
        });

        return this;
    }

    public DatabaseManager uploadPoll(Poll poll){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String name = new SharedPreferenceManager(mContext).loadNameInSharedPref();

        DatabaseReference uploadRef = FirebaseDatabase.getInstance().getReference(Constants.POLLS);
        DatabaseReference dbRef = uploadRef.push();
        String pushRef = dbRef.getKey();

        poll.setUploaderId(uid);
        poll.setUploaderEmail(email);
        poll.setUploaderUsername(name);
        poll.setPollId(pushRef);

        DatabaseReference myuploadRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS).child(uid).child(Constants.UPLOADED_POLLS);
        DatabaseReference mydbRef = myuploadRef.push();
        mydbRef.setValue(pushRef);

        dbRef.setValue(poll).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(BROADCAST_RECEIVER));
            }
        });

        return this;
    }
}
