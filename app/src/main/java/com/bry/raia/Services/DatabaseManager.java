package com.bry.raia.Services;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.bry.raia.Constants;
import com.bry.raia.Models.Announcement;
import com.bry.raia.Models.County;
import com.bry.raia.Models.Language;
import com.bry.raia.Models.Petition;
import com.bry.raia.Models.PetitionSignature;
import com.bry.raia.Models.Poll;
import com.bry.raia.Models.PollOption;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

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
                String signUpTime = dataSnapshot.child(Constants.TIME_OF_SIGNUP).getValue(String.class);

                new SharedPreferenceManager(mContext).setEmailInSharedPref(email);
                new SharedPreferenceManager(mContext).setNameInSharedPref(name);
                new SharedPreferenceManager(mContext).setSignUpDateInSharedPref(Long.parseLong(signUpTime));

                List<String> usersSignedPetitionsIds = new ArrayList<>();
                for(DataSnapshot petitionIdSnaps:dataSnapshot.child(Constants.MY_SIGNED_PETITIONS).getChildren()){
                    usersSignedPetitionsIds.add(petitionIdSnaps.getValue(String.class));
                }
                new SharedPreferenceManager(mContext).recordAllPetitions(usersSignedPetitionsIds);

                HashMap<String,PollOption> allUserVotedPolls = new LinkedHashMap<>();
                for(DataSnapshot votedPollSnap:dataSnapshot.child(Constants.MY_VOTED_POLLS).getChildren()){
                    String pollId = votedPollSnap.getKey();
                    PollOption pollOption = votedPollSnap.getValue(PollOption.class);

                    allUserVotedPolls.put(pollId,pollOption);
                }

                new SharedPreferenceManager(mContext).recordAllPollVotes(allUserVotedPolls);
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

        DatabaseReference myuploadRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS).child(uid)
                .child(Constants.UPLOADED_ANNOUNCEMENTS);
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

        for(PollOption option: poll.getPollOptions()){
            dbRef.child(Constants.POLL_VOTES).child(option.getOptionId()).setValue(option);
        }

        return this;
    }

    public DatabaseManager updatePollOptionData(Poll p, PollOption optionId){
        DatabaseReference pollRef = FirebaseDatabase.getInstance().getReference(Constants.POLLS).child(p.getPollId()).child(Constants.POLL_VOTES)
                .child(optionId.getOptionId());
        pollRef.setValue(optionId);

        return this;
    }

    public DatabaseManager updatePetitionSignatureData(Petition p, PetitionSignature signature){
        DatabaseReference signatureRef = FirebaseDatabase.getInstance().getReference(Constants.PETITIONS).child(p.getPetitionId())
                .child(Constants.PETITION_SIGNATURES).child(signature.getSignerId());
        signatureRef.setValue(signature);

        return this;
    }

    public DatabaseManager recordPetitionSignature(Petition petition){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS).child(uid);
        usernameRef.child(Constants.MY_SIGNED_PETITIONS).child(petition.getPetitionId()).setValue(petition.getPetitionId());

        return this;
    }

    public DatabaseManager recordPollVote(Poll poll, PollOption pollOption){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS).child(uid);
        usernameRef.child(Constants.MY_VOTED_POLLS).child(poll.getPollId()).setValue(pollOption);

        return this;
    }


    public DatabaseManager updateImageAvatar(String image){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference avatarRef = FirebaseDatabase.getInstance().getReference(Constants.IMAGE_AVATAR).child(uid);
        avatarRef.setValue(image);

        return this;
    }

    public DatabaseManager updatePreferredLanguage(Language language){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference languageRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS).child(uid)
                .child(Constants.SELECTED_LANGUAGE);
        languageRef.setValue(language);

        return this;
    }

    public DatabaseManager updatePreferredCounty(County County){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference languageRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS).child(uid)
                .child(Constants.SELECTED_COUNTY);
        languageRef.setValue(County);

        return this;
    }

}
