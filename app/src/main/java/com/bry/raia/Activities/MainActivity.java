package com.bry.raia.Activities;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bry.raia.Adapters.MainActivityPostItemAdapter;
import com.bry.raia.Constants;
import com.bry.raia.Models.Announcement;
import com.bry.raia.Models.Petition;
import com.bry.raia.Models.PetitionSignature;
import com.bry.raia.Models.Poll;
import com.bry.raia.Models.PollOption;
import com.bry.raia.Models.Post;
import com.bry.raia.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = MainActivity.class.getSimpleName();
    private Context mContext;

    @Bind(R.id.filterImageView) ImageView filterImageView;
    @Bind(R.id.feedbackImageView) ImageView feedbackImageView;
    @Bind(R.id.accountImageView) ImageView accountImageView;

    @Bind(R.id.searchLinearLayout) LinearLayout searchLinearLayout;
    @Bind(R.id.backImageView) ImageView backImageView;
    @Bind(R.id.searchCountyEditText) EditText searchCountyEditText;
    @Bind(R.id.selectedCountiesRecyclerView) RecyclerView selectedCountiesRecyclerView;
    @Bind(R.id.allCountiesRecyclerView) RecyclerView allCountiesRecyclerView;

    @Bind(R.id.PollsImageView) ImageView PollsImageView;
    @Bind(R.id.announcementsImageView) ImageView announcementsImageView;
    @Bind(R.id.uploadPostImageView) ImageView uploadPostImageView;
    @Bind(R.id.petitionsImageView) ImageView petitionsImageView;
    @Bind(R.id.messagesImageView) ImageView messagesImageView;

    @Bind(R.id.loadedPostsRecyclerView) RecyclerView loadedPostsRecyclerView;
    private MainActivityPostItemAdapter mainActivityPostItemAdapter;
    @Bind(R.id.loadFeedProgressBar) ProgressBar loadFeedProgressBar;
    @Bind(R.id.loadingContainerLinearLayout) LinearLayout loadingContainerLinearLayout;
    private boolean canAnimateLoadingScreens = false;

    private List<Post> allLoadedPosts = new ArrayList<>();
    private List<Announcement> allLoadedAnnouncements = new ArrayList<>();
    private boolean hasAnnouncementsLoaded = false;
    private List<Petition> allLoadedPetitions = new ArrayList<>();
    private boolean hasPetitionsLoaded = false;
    private List<Poll> allLoadedPolls = new ArrayList<>();
    private boolean hasPollsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mContext = getApplicationContext();

        setClickListeners();
        loadPosts();
    }

    private void setClickListeners() {
        PollsImageView.setOnClickListener(this);
        announcementsImageView.setOnClickListener(this);
        uploadPostImageView.setOnClickListener(this);
        petitionsImageView.setOnClickListener(this);
        messagesImageView.setOnClickListener(this);

        filterImageView.setOnClickListener(this);
        feedbackImageView.setOnClickListener(this);
        accountImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(PollsImageView)){

        }else if(v.equals(announcementsImageView)){

        }else if(v.equals(uploadPostImageView)){
            startActivity(new Intent(MainActivity.this, UploadPostActivity.class));

        }else if(v.equals(petitionsImageView)){

        }else if(v.equals(messagesImageView)){

        }

        else if(v.equals(filterImageView)){

        }else if(v.equals(feedbackImageView)){

        }else if(v.equals(accountImageView)){

        }
    }


    private void loadPosts() {
        showLoadingAnimations();

        DatabaseReference announcementRef = FirebaseDatabase.getInstance().getReference(Constants.ANNOUNCEMENTS);
        announcementRef.limitToFirst(Constants.POST_LOADING_LIMIT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot snap: dataSnapshot.getChildren()) {
                        Announcement announcement = snap.getValue(Announcement.class);
                        Post p = new Post();
                        p.setAnnouncement(announcement);

                        allLoadedPosts.add(p);
                        allLoadedAnnouncements.add(announcement);
                    }
                }
                hasAnnouncementsLoaded = true;

                if(hasAnnouncementsLoaded && hasPetitionsLoaded && hasPollsLoaded){
                    sortPosts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference petitionsRef = FirebaseDatabase.getInstance().getReference(Constants.PETITIONS);
        petitionsRef.limitToFirst(Constants.POST_LOADING_LIMIT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        Petition petition = snap.getValue(Petition.class);
                        petition.getSignatures().clear();

                        for(DataSnapshot signatureSnap:dataSnapshot.child(Constants.PETITION_SIGNATURES).getChildren()){
                            PetitionSignature s = signatureSnap.getValue(PetitionSignature.class);
                            petition.addSignature(s);
                        }

                        Post p = new Post();
                        p.setPetition(petition);

                        allLoadedPosts.add(p);
                        allLoadedPetitions.add(petition);
                    }
                }
                hasPetitionsLoaded = true;

                if(hasAnnouncementsLoaded && hasPetitionsLoaded && hasPollsLoaded){
                    sortPosts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference pollsRef = FirebaseDatabase.getInstance().getReference(Constants.POLLS);
        pollsRef.limitToFirst(Constants.POST_LOADING_LIMIT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        Poll poll = snap.getValue(Poll.class);
                        poll.getPollOptions().clear();
                        for(DataSnapshot pollVoteSnap: snap.child(Constants.POLL_VOTES).getChildren()){
                            PollOption option = pollVoteSnap.getValue(PollOption.class);
                            poll.getPollOptions().add(option);
                        }
                        Post p = new Post();
                        p.setPoll(poll);

                        allLoadedPosts.add(p);
                        allLoadedPolls.add(poll);
                    }
                }
                hasPollsLoaded = true;

                if(hasAnnouncementsLoaded && hasPetitionsLoaded && hasPollsLoaded){
                    sortPosts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sortPosts() {
        hasPollsLoaded = false;
        hasPetitionsLoaded = false;
        hasAnnouncementsLoaded = false;

        HashMap<Long,Post> postsHashMap = new LinkedHashMap<>();
        for(Post p: allLoadedPosts){
            if(p.getPostType().equals(Constants.ANNOUNCEMENTS)){
                //its a announcement
                Long time = p.getAnnouncement().getAnnouncementCreationTime();
                postsHashMap.put(time,p);

            }else if(p.getPostType().equals(Constants.PETITIONS)){
                //its a petition
                Long time = p.getPetition().getPetitionCreationTime();
                postsHashMap.put(time,p);

            }else{
                //its a poll
                Long time = p.getPoll().getPollCreationTime();
                postsHashMap.put(time,p);
            }

        }

        List<Long> timesToSort = new ArrayList<>(postsHashMap.keySet());
        Collections.sort(timesToSort);

        allLoadedPosts.clear();
        for(Long time:timesToSort) {
            allLoadedPosts.add(postsHashMap.get(time));
        }

        generateBitmapsFromPostTask gbfpt = new generateBitmapsFromPostTask();
        gbfpt.execute("");
    }

    protected class generateBitmapsFromPostTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            loadThePostImagesAsBitmaps();
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            loadPostsIntoRecyclerView();
        }

    }

    private void loadThePostImagesAsBitmaps(){
        for(Post p: allLoadedPosts){
            if(p.getPostType().equals(Constants.ANNOUNCEMENTS)){
                //its a announcement
                if(p.getAnnouncement().getAnnouncementBitmap()==null)
                    p.getAnnouncement().setAnnouncementBitmap(decodeFromFirebaseBase64(p.getAnnouncement().getEncodedAnnouncementImage()));
            }else if(p.getPostType().equals(Constants.PETITIONS)){
                //its a petition
                if(p.getPetition().getPetitionBitmap()==null)
                    p.getPetition().setPetitionBitmap(decodeFromFirebaseBase64(p.getPetition().getEncodedPetitionImage()));

            }else{
                //its a poll

            }

        }
    }

    private void loadPostsIntoRecyclerView() {
        mainActivityPostItemAdapter = new MainActivityPostItemAdapter(allLoadedPosts, MainActivity.this);
        loadedPostsRecyclerView.setAdapter(mainActivityPostItemAdapter);
        loadedPostsRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mainActivityPostItemAdapter.setOnBottomReachedListener(new MainActivityPostItemAdapter.OnBottomReachedListener() {
            @Override
            public void onBottomReached(int position) {
                //when user has scrolled to bottom of list
                loadMorePostItems();
            }
        });

        hideLoadingAnimations();
    }

    private void loadMorePostItems() {
        loadedPostsRecyclerView.setVisibility(View.GONE);
        loadFeedProgressBar.setVisibility(View.VISIBLE);
        final List<Post> morePosts = new ArrayList<>();

        DatabaseReference announcementRef = FirebaseDatabase.getInstance().getReference(Constants.ANNOUNCEMENTS);
        announcementRef.startAt(allLoadedAnnouncements.get(allLoadedAnnouncements.size()-1).getAnnouncementId()).limitToFirst(Constants.POST_LOADING_LIMIT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot snap: dataSnapshot.getChildren()) {
                        Announcement announcement = snap.getValue(Announcement.class);
                        Post p = new Post();
                        p.setAnnouncement(announcement);

                        morePosts.add(p);
                        allLoadedAnnouncements.add(announcement);
                    }
                }
                hasAnnouncementsLoaded = true;

                if(hasAnnouncementsLoaded && hasPetitionsLoaded && hasPollsLoaded){
                    sortNewPostItems(morePosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference petitionsRef = FirebaseDatabase.getInstance().getReference(Constants.PETITIONS);
        petitionsRef.startAt(allLoadedPetitions.get(allLoadedPetitions.size()-1).getPetitionId())
                .limitToFirst(Constants.POST_LOADING_LIMIT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        Petition petition = snap.getValue(Petition.class);
                        petition.getSignatures().clear();

                        for(DataSnapshot signatureSnap:dataSnapshot.child(Constants.PETITION_SIGNATURES).getChildren()){
                            PetitionSignature s = signatureSnap.getValue(PetitionSignature.class);
                            petition.addSignature(s);
                        }

                        Post p = new Post();
                        p.setPetition(petition);

                        morePosts.add(p);
                        allLoadedPetitions.add(petition);
                    }
                }
                hasPetitionsLoaded = true;

                if(hasAnnouncementsLoaded && hasPetitionsLoaded && hasPollsLoaded){
                    sortNewPostItems(morePosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference pollsRef = FirebaseDatabase.getInstance().getReference(Constants.POLLS);
        pollsRef.startAt(allLoadedPolls.get(allLoadedPolls.size()-1).getPollId())
                .limitToFirst(Constants.POST_LOADING_LIMIT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        Poll poll = snap.getValue(Poll.class);
                        poll.getPollOptions().clear();
                        for(DataSnapshot pollVoteSnap: snap.child(Constants.POLL_VOTES).getChildren()){
                            PollOption option = pollVoteSnap.getValue(PollOption.class);
                            poll.getPollOptions().add(option);
                        }
                        Post p = new Post();
                        p.setPoll(poll);

                        morePosts.add(p);
                        allLoadedPolls.add(poll);
                    }
                }
                hasPollsLoaded = true;

                if(hasAnnouncementsLoaded && hasPetitionsLoaded && hasPollsLoaded){
                    sortNewPostItems(morePosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sortNewPostItems(List<Post> morePosts){
        hasPollsLoaded = false;
        hasPetitionsLoaded = false;
        hasAnnouncementsLoaded = false;

        HashMap<Long,Post> postsHashMap = new LinkedHashMap<>();
        for(Post p: morePosts){
            if(p.getPostType().equals(Constants.ANNOUNCEMENTS)){
                //its a announcement
                Long time = p.getAnnouncement().getAnnouncementCreationTime();
                postsHashMap.put(time,p);

            }else if(p.getPostType().equals(Constants.PETITIONS)){
                //its a petition
                Long time = p.getPetition().getPetitionCreationTime();
                postsHashMap.put(time,p);

            }else{
                //its a poll
                Long time = p.getPoll().getPollCreationTime();
                postsHashMap.put(time,p);
            }

        }

        List<Long> timesToSort = new ArrayList<>(postsHashMap.keySet());
        Collections.sort(timesToSort);

        morePosts.clear();
        for(Long time:timesToSort) {
            morePosts.add(postsHashMap.get(time));
        }

        newLoadedPosts = morePosts;
        generateNewBitmapsFromPostTask gbfpt = new generateNewBitmapsFromPostTask();
        gbfpt.execute("");
    }

    List<Post> newLoadedPosts = new ArrayList<>();

    protected class generateNewBitmapsFromPostTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            loadTheNewPostImagesAsBitmaps();
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            loadNewPostsIntoRecyclerView();
        }

    }

    private void loadTheNewPostImagesAsBitmaps() {
        for(Post p: newLoadedPosts){
            if(p.getPostType().equals(Constants.ANNOUNCEMENTS)){
                //its a announcement
                if(p.getAnnouncement().getAnnouncementBitmap()==null)
                    p.getAnnouncement().setAnnouncementBitmap(decodeFromFirebaseBase64(p.getAnnouncement().getEncodedAnnouncementImage()));
            }else if(p.getPostType().equals(Constants.PETITIONS)){
                //its a petition
                if(p.getPetition().getPetitionBitmap()==null)
                    p.getPetition().setPetitionBitmap(decodeFromFirebaseBase64(p.getPetition().getEncodedPetitionImage()));

            }else{
                //its a poll

            }

        }
    }

    private void loadNewPostsIntoRecyclerView() {
        allLoadedPosts.addAll(newLoadedPosts);
        for(Post post:newLoadedPosts) {
            mainActivityPostItemAdapter.addPostToList(post);
            mainActivityPostItemAdapter.notifyItemInserted(newLoadedPosts.size()-1);
            mainActivityPostItemAdapter.notifyDataSetChanged();
        }
    }

    private Bitmap decodeFromFirebaseBase64(String image) {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }


    private void startLoadingAnimations(){
        final float alpha = 0.3f;
        final int duration = 2000;

        final float alphaR = 1f;
        final int durationR = 800;

        if(canAnimateLoadingScreens) {
            loadingContainerLinearLayout.animate().alpha(alpha).setDuration(duration).setInterpolator(new LinearInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            loadingContainerLinearLayout.animate().alpha(alphaR).setDuration(durationR).setInterpolator(new LinearInterpolator())
                                    .setListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animator) {
                                            startLoadingAnimations();
                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationRepeat(Animator animator) {

                                        }
                                    });
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
        }
    }

    private void hideLoadingAnimations(){
        canAnimateLoadingScreens = false;
        loadingContainerLinearLayout.setVisibility(View.GONE);
        loadedPostsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showLoadingAnimations(){
        canAnimateLoadingScreens = true;
        loadingContainerLinearLayout.setVisibility(View.VISIBLE);
        loadedPostsRecyclerView.setVisibility(View.GONE);
        startLoadingAnimations();
    }

}
