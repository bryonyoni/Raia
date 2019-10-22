package com.bry.raia.Activities;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.raia.Adapters.MainActivityPostItemAdapter;
import com.bry.raia.Constants;
import com.bry.raia.Models.Announcement;
import com.bry.raia.Models.MyRecyclerView;
import com.bry.raia.Models.Petition;
import com.bry.raia.Models.PetitionSignature;
import com.bry.raia.Models.Poll;
import com.bry.raia.Models.PollOption;
import com.bry.raia.Models.Post;
import com.bry.raia.R;
import com.bry.raia.Services.DatabaseManager;
import com.bry.raia.Services.SharedPreferenceManager;
import com.bry.raia.Services.Utils;
import com.bry.raia.Variables;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
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

    @Bind(R.id.searchLinearLayout) LinearLayout searchLinearLayout;
    @Bind(R.id.backImageView) ImageView backImageView;
    @Bind(R.id.searchCountyEditText) EditText searchCountyEditText;
    @Bind(R.id.selectedCountiesRecyclerView) RecyclerView selectedCountiesRecyclerView;
    @Bind(R.id.loadNewPostsLoaderLinearLayout) LinearLayout loadNewPostsLoaderLinearLayout;
    @Bind(R.id.allCountiesRecyclerView) RecyclerView allCountiesRecyclerView;

    @Bind(R.id.accountImageView) ImageView accountImageView;
    @Bind(R.id.uploadPostImageView) ImageView uploadPostImageView;
    @Bind(R.id.messagesImageView) ImageView messagesImageView;

    @Bind(R.id.loadedPostsRecyclerView) MyRecyclerView loadedPostsRecyclerView;
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
    private int mAnimationTime = 300;

    @Bind(R.id.viewPostRelativeLayout) RelativeLayout viewPostRelativeLayout;
    private boolean isViewPostShowing = false;
    @Bind(R.id.postTypeTextView) TextView postTypeTextView;
    @Bind(R.id.userNameTextView) TextView userNameTextView;
    @Bind(R.id.postTitleTextView) TextView postTitleTextView;
    @Bind(R.id.announcementCardView) LinearLayout announcementCardView;
    @Bind(R.id.announcementPostImageViewBack) ImageView announcementPostImageViewBack;
    @Bind(R.id.announcementImageView) ImageView announcementImageView;
    @Bind(R.id.AnnouncementTitleTextView) TextView AnnouncementTitleTextView;

    @Bind(R.id.petitionUiLinearLayout) LinearLayout petitionUiLinearLayout;
    @Bind(R.id.petitionImageViewBack) ImageView petitionImageViewBack;
    @Bind(R.id.petitionImageView) ImageView petitionImageView;
    @Bind(R.id.numberSignedTextView) TextView numberSignedTextView;
    @Bind(R.id.petitionPercentageView) ProgressBar petitionPercentageView;
    @Bind(R.id.signTextView) TextView signTextView;
    @Bind(R.id.PetitionTitleTextView) TextView PetitionTitleTextView;

    @Bind(R.id.pollRelativeLayout) RelativeLayout pollRelativeLayout;
    @Bind(R.id.option1LinearLayout) LinearLayout option1LinearLayout;
    @Bind(R.id.pollOption1CheckBox)CheckBox pollOption1CheckBox;
    @Bind(R.id.option1PercentageTextView)TextView option1PercentageTextView;
    @Bind(R.id.option1PercentageBarView)ProgressBar option1PercentageBarView;
    @Bind(R.id.option2LinearLayout)LinearLayout option2LinearLayout;
    @Bind(R.id.option2CheckBox)CheckBox option2CheckBox;
    @Bind(R.id.option2PercentageTextView)TextView option2PercentageTextView;
    @Bind(R.id.option2PercentageBarView)ProgressBar option2PercentageBar;
    @Bind(R.id.option3LinearLayout)LinearLayout option3LinearLayout;
    @Bind(R.id.option3CheckBox)CheckBox option3CheckBox;
    @Bind(R.id.option3PercentageTextView)TextView option3PercentageTextView;
    @Bind(R.id.option3PercentageBarView)ProgressBar option3PercentageView;
    @Bind(R.id.option4LinearLayout)LinearLayout option4LinearLayout;
    @Bind(R.id.option4CheckBox)CheckBox option4CheckBox;
    @Bind(R.id.option4PercentageTextView)TextView option4PercentageTextView;
    @Bind(R.id.option4PercentageBarView)ProgressBar option4PercentageBarView;

    @Bind(R.id.pollVoteCountTextView) TextView pollVoteCountTextView;
    @Bind(R.id.PollTitleTextView) TextView PollTitleTextView;
    private boolean isPresetingCheckButons = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mContext = getApplicationContext();

        setClickListeners();
        loadPosts();

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToViewPost,
                new IntentFilter(Constants.SHOW_VIEW_POST));
    }

    private void setClickListeners() {
        uploadPostImageView.setOnClickListener(this);
        messagesImageView.setOnClickListener(this);

        filterImageView.setOnClickListener(this);
        feedbackImageView.setOnClickListener(this);
        accountImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.equals(uploadPostImageView)){
            startActivity(new Intent(MainActivity.this, UploadPostActivity.class));
        }else if(v.equals(messagesImageView)){

        }else if(v.equals(filterImageView)){

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
                if(p.getAnnouncement().getAnnouncementBitmap()==null){}
//                    p.getAnnouncement().setAnnouncementBitmap(decodeFromFirebaseBase64(p.getAnnouncement().getEncodedAnnouncementImage()));
            }else if(p.getPostType().equals(Constants.PETITIONS)){
                //its a petition
                if(p.getPetition().getPetitionBitmap()==null){}
//                    p.getPetition().setPetitionBitmap(decodeFromFirebaseBase64(p.getPetition().getEncodedPetitionImage()));

            }

        }
    }

    private void loadPostsIntoRecyclerView() {
        Log.e("MainActivity","Number of items: "+allLoadedPosts.size());
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
        if(allLoadedPosts.isEmpty()){

        }
//        setUpOverScrollForLoadMorePosts();
    }

    private void setUpOverScrollForLoadMorePosts(){
        swipeTopGestureDetector = new GestureDetector(mContext, new MySwipeForReloadGestureListener());

        loadedPostsRecyclerView.setOnScrollChangedCallback(new MyRecyclerView.OnScrollChangedCallback() {
            @Override
            public void onScroll(int l, int t, int oldl, int oldt) {
                isAtTopOfPage = t<=1;
            }
        });

        loadedPostsRecyclerView.setOnTouchEvent(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (swipeTopGestureDetector.onTouchEvent(event)) {
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (isDownSwipingBoi) {
                        isDownSwipingBoi = false;
                        restoreScrollView();
                    }
                }
                return false;
            }
        });
        restoreScrollView();
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
        final int duration = 800;

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



    private int y_deltaBoi;
    private boolean isDownSwipingBoi = false;
    private GestureDetector swipeTopGestureDetector;
    private boolean isAtTopOfPage = true;
    private int prevPos = 0;

    class MySwipeForReloadGestureListener extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
//            Log.d(TAG, "onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            Log.e(TAG, "onDown: event.getRawX(): " + event.getRawX() + " event.getRawY()" + event.getRawY());

            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) loadedPostsRecyclerView.getLayoutParams();
            y_deltaBoi = Y - lParams.topMargin;

            origX = lParams.leftMargin;
            origY = lParams.topMargin;


            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int Y = (int) e2.getRawY();
            final int X = (int) e2.getRawX();

            if(isAtTopOfPage){
                onTouchScrollView((double)(Y - y_deltaBoi));
            }else restoreScrollView();
            isDownSwipingBoi = true;

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG,"velocityY-"+velocityY);
            if(isAtTopOfPage) {
                if (velocityY>0 && Math.abs(velocityY) > Math.abs(velocityX) && Math.abs(velocityY) > 2800 && Math.abs(velocityY) < 8000 ) {
                    showLoadingScreens();
                }else{
                    restoreScrollView();
                }
            }else restoreScrollView();
            isDownSwipingBoi = false;
            return false;

        }

    }

    private boolean isScrollPosChanged = false;
    private boolean isOptionsExpanded = false;
    private int tranY = Utils.dpToPx(-190);
    private void onTouchScrollView(double pos){
        int trans = (int) ((pos - Utils.dpToPx(10)) * 0.25);
        loadNewPostsLoaderLinearLayout.setVisibility(View.VISIBLE);
        if(!isOptionsExpanded){
            loadNewPostsLoaderLinearLayout.setTranslationY(tranY+trans);
            loadedPostsRecyclerView.setTranslationY(Utils.dpToPx(10)+trans);
        }else{
            loadNewPostsLoaderLinearLayout.setTranslationY(Utils.dpToPx(-120)+trans);
            loadedPostsRecyclerView.setTranslationY(Utils.dpToPx(80)+trans);
        }

    }

    private void restoreScrollView(){
        isOptionsExpanded = false;
        Variables.hasOptionsCardOpen = false;
        loadNewPostsLoaderLinearLayout.animate().setDuration(300).translationY(tranY)
                .setInterpolator(new LinearOutSlowInInterpolator()).start();

        loadNewPostsLoaderLinearLayout.setVisibility(View.VISIBLE);
        loadedPostsRecyclerView.animate().translationY(0).setDuration(300).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
//                RelativeLayout.LayoutParams par = (RelativeLayout.LayoutParams) webViewCardView.getLayoutParams();
//                par.topMargin = Utils.dpToPx(3);
//                webViewCardView.setLayoutParams(par);

                        loadedPostsRecyclerView.setTranslationY(0);
                        loadNewPostsLoaderLinearLayout.setTranslationY(tranY);
                        loadNewPostsLoaderLinearLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

    }

    private void showLoadingScreens(){
        if(isOptionsExpanded){
            restoreScrollView();
            isOptionsExpanded = false;
        }else {
            isOptionsExpanded = true;
            Variables.hasOptionsCardOpen = true;
            loadedPostsRecyclerView.animate().translationY(Utils.dpToPx(100)).setDuration(300).setInterpolator(new LinearOutSlowInInterpolator()).start();

            loadNewPostsLoaderLinearLayout.setVisibility(View.VISIBLE);

            loadNewPostsLoaderLinearLayout.animate().setDuration(300).translationY(Utils.dpToPx(-100))
                    .setInterpolator(new LinearOutSlowInInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            loadNewPostsLoaderLinearLayout.setTranslationY(Utils.dpToPx(-100));
                            loadNewPostsLoaderLinearLayout.setVisibility(View.VISIBLE);
                            loadedPostsRecyclerView.setTranslationY(Utils.dpToPx(100));

//                        RelativeLayout.LayoutParams par = (RelativeLayout.LayoutParams) webViewCardView.getLayoutParams();
//                        par.topMargin = Utils.dpToPx(210);
//                        webViewCardView.setLayoutParams(par);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    }).start();
        }
    }


    private BroadcastReceiver mMessageReceiverToViewPost = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showViewPostPart();
        }
    };

    private void showViewPostPart(){
        isViewPostShowing = true;
        viewPostRelativeLayout.setVisibility(View.VISIBLE);
        viewPostRelativeLayout.animate().alpha(1f).translationY(0).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        viewPostRelativeLayout.setAlpha(1f);
                        viewPostRelativeLayout.setTranslationY(0);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });

        Post mPost = Variables.postToBeViewed;
        if(mPost.getPostType().equals(Constants.ANNOUNCEMENTS)) {
            Announcement announcement = mPost.getAnnouncement();
            postTypeTextView.setText(getString(R.string.announcement));
            userNameTextView.setText(String.format("By %s to %s", announcement.getUploaderUsername(), announcement.getCounty().getCountyName()));
            postTitleTextView.setText(announcement.getAnnouncementTitle());

            announcementCardView.setVisibility(View.VISIBLE);
            petitionUiLinearLayout.setVisibility(View.GONE);
            pollRelativeLayout.setVisibility(View.GONE);
            announcementImageView.setImageBitmap(Variables.image);
            announcementPostImageViewBack.setImageBitmap(Variables.imageBack);

            AnnouncementTitleTextView.setText(announcement.getAnnouncementTitle());
        }else if(mPost.getPostType().equals(Constants.PETITIONS)) {
            final Petition petition = mPost.getPetition();
            postTypeTextView.setText(getString(R.string.petition));
            userNameTextView.setText(String.format("By %s to %s", petition.getUploaderUsername(), petition.getCounty().getCountyName()));
            postTitleTextView.setText(petition.getPetitionTitle());
            PetitionTitleTextView.setText(petition.getPetitionTitle());

            petitionUiLinearLayout.setVisibility(View.VISIBLE);
            announcementCardView.setVisibility(View.GONE);
            pollRelativeLayout.setVisibility(View.GONE);
            petitionImageView.setImageBitmap(Variables.image);
            petitionImageViewBack.setImageBitmap(Variables.imageBack);

            numberSignedTextView.setText(String.format("%d signed", petition.getSignatures().size()));

            long percentage = (petition.getSignatures().size()/petition.getPetitionSignatureTarget())*100;
            petitionPercentageView.setProgress((int)percentage);

            if(new SharedPreferenceManager(mContext).hasUserSignedPetition(petition)){
                signTextView.setAlpha(0.4f);
            }

            signTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!new SharedPreferenceManager(mContext).hasUserSignedPetition(petition)) {
                        updatePetitionDataInSharedPreferencesAndFirebase(petition);
                        signTextView.setAlpha(0.4f);
                    }
                }
            });

        }else{
            //its a poll
            final Poll poll = mPost.getPoll();
            pollRelativeLayout.setVisibility(View.VISIBLE);
            announcementCardView.setVisibility(View.GONE);
            petitionUiLinearLayout.setVisibility(View.GONE);
            postTypeTextView.setText(getString(R.string.poll));
            userNameTextView.setText(String.format("By %s to %s", poll.getUploaderUsername(), poll.getCounty().getCountyName()));
            postTitleTextView.setText(poll.getPollTitle());
            PollTitleTextView.setText(poll.getPollTitle());


            if(new SharedPreferenceManager(this).hasUserVotedInPoll(poll)){
                PollOption po = new SharedPreferenceManager(this).getWhichPollOptionSelected(poll);
                isPresetingCheckButons = true;
                if(poll.getPollOptions().get(0).getOptionId().equals(po.getOptionId())){
                    pollOption1CheckBox.setChecked(true);
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);
                }else if(poll.getPollOptions().size()>1 && poll.getPollOptions().get(1).getOptionId().equals(po.getOptionId())){
                    option2CheckBox.setChecked(true);
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);
                }else if(poll.getPollOptions().size()>2 && poll.getPollOptions().get(2).getOptionId().equals(po.getOptionId())){
                    option3CheckBox.setChecked(true);
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);
                }else if(poll.getPollOptions().size()>3 && poll.getPollOptions().get(3).getOptionId().equals(po.getOptionId())){
                    option4CheckBox.setChecked(true);
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);
                }
                isPresetingCheckButons = false;
            }

            pollOption1CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);

                    if(!isPresetingCheckButons) {
                        poll.getPollOptions().get(0).addVote();
                        updatePollDataInSharedPrefAndFirebase(poll, poll.getPollOptions().get(0));
                        setPollData(poll, true);
                    }
                }
            });

            option2CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);

                    if(!isPresetingCheckButons) {
                        poll.getPollOptions().get(1).addVote();
                        updatePollDataInSharedPrefAndFirebase(poll, poll.getPollOptions().get(1));
                        setPollData(poll, true);
                    }
                }
            });

            option3CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);

                    if(!isPresetingCheckButons) {
                        poll.getPollOptions().get(2).addVote();
                        updatePollDataInSharedPrefAndFirebase(poll, poll.getPollOptions().get(2));
                        setPollData(poll, true);
                    }
                }
            });

            option4CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);

                    if(!isPresetingCheckButons) {
                        poll.getPollOptions().get(3).addVote();
                        updatePollDataInSharedPrefAndFirebase(poll, poll.getPollOptions().get(3));
                        setPollData(poll, true);
                    }
                }
            });
            setPollData(poll,false);
        }
        
    }

    private void setPollData(Poll poll, boolean isShowingResult){
        isShowingResult = new SharedPreferenceManager(mContext).hasUserVotedInPoll(poll);

        int totalVotes = 0;
        int barWidth = Utils.dpToPx(Constants.POST_CARD_VIEW_WIDTH-20);
        for(PollOption op:poll.getPollOptions()){
            totalVotes+=op.getVotes();
        }
        option1LinearLayout.setVisibility(View.GONE);
        option2LinearLayout.setVisibility(View.GONE);
        option3LinearLayout.setVisibility(View.GONE);
        option4LinearLayout.setVisibility(View.GONE);
        //option 1
        pollOption1CheckBox.setText(poll.getPollOptions().get(0).getOptionText());
        option1LinearLayout.setVisibility(View.VISIBLE);

        if(totalVotes==0) pollVoteCountTextView.setText(getResources().getString(R.string.zero_votes));
        else if(totalVotes==1)pollVoteCountTextView.setText(getResources().getString(R.string.one_vote));
        else pollVoteCountTextView.setText(totalVotes+getResources().getString(R.string.votes));

        int div = totalVotes;
        if(div==0) div=1;
        int option1Percentage = (int)((poll.getPollOptions().get(0).getVotes()/(div))*100);
        if(isShowingResult){
            option1PercentageTextView.setText(option1Percentage+"%");
            option1PercentageBarView.setProgress(option1Percentage);
        }else{
            option1PercentageTextView.setText("0%");
            option1PercentageBarView.setProgress(option1Percentage);
        }

        if(poll.getPollOptions().size()>1) {
            //option 2
            option2CheckBox.setText(poll.getPollOptions().get(1).getOptionText());
            option2LinearLayout.setVisibility(View.VISIBLE);
            int option2Percentage = (int)(poll.getPollOptions().get(1).getVotes() / div) * 100;
            if(isShowingResult){
                option2PercentageTextView.setText(option2Percentage + "%");
                option2PercentageBar.setProgress(option2Percentage);
            }else{
                option2PercentageTextView.setText("0%");
                option2PercentageBar.setProgress(option2Percentage);
            }

        }

        if(poll.getPollOptions().size()>2) {
            //option 3
            option3CheckBox.setText(poll.getPollOptions().get(2).getOptionText());
            option3LinearLayout.setVisibility(View.VISIBLE);
            int option3Percentage = (int)(poll.getPollOptions().get(2).getVotes() / div) * 100;
            if(isShowingResult){
                option3PercentageTextView.setText(option3Percentage + "%");
                option3PercentageView.setProgress(option3Percentage);
            }else{
                option3PercentageTextView.setText("0%");
                option3PercentageView.setProgress(option3Percentage);
            }

        }

        if(poll.getPollOptions().size()>3) {
            //option 4
            option4LinearLayout.setVisibility(View.VISIBLE);
            option4CheckBox.setText(poll.getPollOptions().get(3).getOptionText());
            int option4Percentage = (int)(poll.getPollOptions().get(3).getVotes() / div) * 100;
            if(isShowingResult){
                option4PercentageTextView.setText(option4Percentage + "%");
                option4PercentageBarView.setProgress(option4Percentage);
            }else{
                option4PercentageTextView.setText("0%");
                option4PercentageBarView.setProgress(option4Percentage);
            }

        }
    }

    private void updatePollDataInSharedPrefAndFirebase(Poll poll, PollOption po){
        new DatabaseManager(this,"").recordPollVote(poll,po).updatePollOptionData(poll,po);
        new SharedPreferenceManager(this).recordPollVote(poll.getPollId(),po);
    }

    private void updatePetitionDataInSharedPreferencesAndFirebase(Petition p){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String name = new SharedPreferenceManager(this).loadNameInSharedPref();
        long timestamp = Calendar.getInstance().getTimeInMillis();
        PetitionSignature signature = new PetitionSignature(uid,name,email,timestamp);

        new DatabaseManager(this,"").recordPetitionSignature(p).updatePetitionSignatureData(p,signature);

        new SharedPreferenceManager(this).recordPetition(p.getPetitionId());
    }

    private void hideViewPostPart(){
        isViewPostShowing = false;
        viewPostRelativeLayout.animate().alpha(0f).translationY(Utils.dpToPx(180)).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        viewPostRelativeLayout.setAlpha(0f);
                        viewPostRelativeLayout.setTranslationY(Utils.dpToPx(180));
                        viewPostRelativeLayout.setVisibility(View.GONE);
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
    public void onBackPressed(){
        if(isViewPostShowing){
            hideViewPostPart();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToViewPost);
        super.onDestroy();
    }
}
