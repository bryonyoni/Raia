package com.bry.raia.Adapters;

import android.animation.Animator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.raia.Activities.MainActivity;
import com.bry.raia.Activities.ViewPostActivity;
import com.bry.raia.Constants;
import com.bry.raia.Models.Announcement;
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
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;

public class MainActivityPostItemAdapter extends RecyclerView.Adapter<MainActivityPostItemAdapter.ViewHolder> {
    private List<Post> mPosts;
    private Activity mActivity;
    OnBottomReachedListener onBottomReachedListener;
    private boolean canAnimateLoadingScreens,canAnimateImageLoadingScreens;
    private Bitmap postImage;
    private Bitmap postImageBack;


    public MainActivityPostItemAdapter(List<Post> mPosts, Activity activity) {
        this.mPosts = mPosts;
        this.mActivity = activity;
    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener){
        this.onBottomReachedListener = onBottomReachedListener;
    }

    public void addPostToList(Post item){
        mPosts.add(item);
    }



    @NonNull
    @Override
    public MainActivityPostItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View recipeView = inflater.inflate(R.layout.main_activity_post_item, viewGroup, false);
        return new MainActivityPostItemAdapter.ViewHolder(recipeView);
    }


    @Override
    public void onBindViewHolder(@NonNull final MainActivityPostItemAdapter.ViewHolder viewHolder, int i) {
        final Post post = mPosts.get(i);

        canAnimateLoadingScreens = true;
        if(post.getPostType().equals(Constants.ANNOUNCEMENTS)){
            //its a announcement
            Announcement announcement = post.getAnnouncement();
            Log.e("PostItemAdapter","Post is an announcement: "+announcement.getAnnouncementTitle());
            viewHolder.announcementCardView.setVisibility(View.VISIBLE);
            viewHolder.announcementUploaderNameTextView.setText(announcement.getUploaderUsername());
            if(announcement.getCounty()!=null) {
                viewHolder.announcementCountyNameTextView.setText(announcement.getCounty().getCountyName());
                viewHolder.announcementDetailsTextView.setText(announcement.getAnnouncementTitle());
            }

            canAnimateImageLoadingScreens = true;
            startImageLoadingAnimations(viewHolder);
            loadImageFromFirebase(viewHolder,post);
//            BlurPostBackTask bp = new BlurPostBackTask();
//            bp.setFields(post,viewHolder.petitionImageViewBack,viewHolder.petitionImageView);

            viewHolder.announcementImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Variables.postToBeViewed  = post;
                    BitmapDrawable bitmapDrawable = ((BitmapDrawable) viewHolder.announcementImageView.getDrawable());
                    Bitmap bitmap = bitmapDrawable .getBitmap();
                    Variables.image = bitmap;
                    BitmapDrawable bitmapDrawableBack = ((BitmapDrawable) viewHolder.announcementPostImageViewBack.getDrawable());
                    Bitmap bitmapBack = bitmapDrawableBack.getBitmap();
                    Variables.imageBack = bitmapBack;
                    Variables.postToBeViewedImageBackground = Variables.blurredBacks.get(post.getAnnouncement().getAnnouncementId());

//                    Intent i = new Intent(mActivity, ViewPostActivity.class);
//                    mActivity.startActivity(i);

                    LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(Constants.SHOW_VIEW_POST));
                }
            });


        }
        else if(post.getPostType().equals(Constants.PETITIONS)){
            //its a petition
            final Petition petition = post.getPetition();
            Log.e("PostItemAdapter","Post is a petition: "+petition.getPetitionTitle());
            viewHolder.petitionCardView.setVisibility(View.VISIBLE);
            viewHolder.petitionUploaderNameTextView.setText(petition.getUploaderUsername());
            viewHolder.petitionCountyTextView.setText(petition.getCounty().getCountyName());
            viewHolder.petitionDetailsTextView.setText(petition.getPetitionTitle());

//            viewHolder.petitionImageView.setImageBitmap(petition.getPetitionBitmap());
//            BlurPostBackTask bp = new BlurPostBackTask();

            canAnimateImageLoadingScreens = true;
            startImageLoadingAnimations(viewHolder);
            loadImageFromFirebase(viewHolder,post);
//            bp.setFields(post,viewHolder.announcementPostImageViewBack,viewHolder.announcementImageView);

            viewHolder.numberSignedTextView.setText(String.format("%d signed", petition.getSignatures().size()));

            long percentage = (petition.getSignatures().size()/petition.getPetitionSignatureTarget())*100;
            Log.e("PostItemAdapter","percentage: "+(int)percentage);
            int per = (int) percentage;
            viewHolder.petitionPercentageProgressView.setProgress(per);


            if(new SharedPreferenceManager(mActivity).hasUserSignedPetition(petition)){
                viewHolder.signTextView.setAlpha(0.4f);
            }

            viewHolder.signTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!new SharedPreferenceManager(mActivity).hasUserSignedPetition(petition)) {
                        updatePetitionDataInSharedPreferencesAndFirebase(petition);
                        viewHolder.signTextView.setAlpha(0.4f);
                    }
                }
            });


            viewHolder.petitionImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Variables.postToBeViewed  = post;
                    BitmapDrawable bitmapDrawable = ((BitmapDrawable) viewHolder.petitionImageView.getDrawable());
                    Bitmap bitmap = bitmapDrawable .getBitmap();
                    Variables.image = bitmap;
                    BitmapDrawable bitmapDrawableBack = ((BitmapDrawable) viewHolder.petitionImageViewBack.getDrawable());
                    Bitmap bitmapBack = bitmapDrawableBack.getBitmap();
                    Variables.imageBack = bitmapBack;
                    Variables.postToBeViewedImageBackground = Variables.blurredBacks.get(post.getPetition().getPetitionId());

//                    Intent i = new Intent(mActivity, ViewPostActivity.class);
//                    mActivity.startActivity(i);

                    LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(Constants.SHOW_VIEW_POST));

                }
            });

        }
        else if(post.getPostType().equals(Constants.POLLS)){
            //its a poll
            final Poll poll = post.getPoll();
            Log.e("PostItemAdapter","Post is a poll: "+poll.getPollTitle()+ " type: "+post.getPostType());
            viewHolder.pollCardView.setVisibility(View.VISIBLE);
            viewHolder.pollUploaderNameTextView.setText(poll.getUploaderUsername());
            viewHolder.pollCountyNameTextView.setText(poll.getCounty().getCountyName());
            viewHolder.pollDetailsTextView.setText(poll.getPollTitle());

            if(new SharedPreferenceManager(mActivity).hasUserVotedInPoll(poll)){
                PollOption po = new SharedPreferenceManager(mActivity).getWhichPollOptionSelected(poll);
                if(poll.getPollOptions().get(0).getOptionId().equals(po.getOptionId())){
                    viewHolder.pollOption1CheckBox.setChecked(true);
                    viewHolder.pollOption1CheckBox.setEnabled(false);
                    viewHolder.option2CheckBox.setEnabled(false);
                    viewHolder.option3CheckBox.setEnabled(false);
                    viewHolder.option4CheckBox.setEnabled(false);
                }else if(poll.getPollOptions().size()>1 && poll.getPollOptions().get(1).getOptionId().equals(po.getOptionId())){
                    viewHolder.option2CheckBox.setChecked(true);
                    viewHolder.pollOption1CheckBox.setEnabled(false);
                    viewHolder.option2CheckBox.setEnabled(false);
                    viewHolder.option3CheckBox.setEnabled(false);
                    viewHolder.option4CheckBox.setEnabled(false);
                }else if(poll.getPollOptions().size()>2 && poll.getPollOptions().get(2).getOptionId().equals(po.getOptionId())){
                    viewHolder.option3CheckBox.setChecked(true);
                    viewHolder.pollOption1CheckBox.setEnabled(false);
                    viewHolder.option2CheckBox.setEnabled(false);
                    viewHolder.option3CheckBox.setEnabled(false);
                    viewHolder.option4CheckBox.setEnabled(false);
                }else if(poll.getPollOptions().size()>3 && poll.getPollOptions().get(3).getOptionId().equals(po.getOptionId())){
                    viewHolder.option4CheckBox.setChecked(true);
                    viewHolder.pollOption1CheckBox.setEnabled(false);
                    viewHolder.option2CheckBox.setEnabled(false);
                    viewHolder.option3CheckBox.setEnabled(false);
                    viewHolder.option4CheckBox.setEnabled(false);
                }
            }

            viewHolder.pollOption1CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    viewHolder.pollOption1CheckBox.setEnabled(false);
                    viewHolder.option2CheckBox.setEnabled(false);
                    viewHolder.option3CheckBox.setEnabled(false);
                    viewHolder.option4CheckBox.setEnabled(false);

                    poll.getPollOptions().get(0).addVote();
                    updatePollDataInSharedPrefAndFirebase(poll,poll.getPollOptions().get(0));
                    setPollData(poll,viewHolder,true);
                }
            });

            viewHolder.option2CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    viewHolder.pollOption1CheckBox.setEnabled(false);
                    viewHolder.option2CheckBox.setEnabled(false);
                    viewHolder.option3CheckBox.setEnabled(false);
                    viewHolder.option4CheckBox.setEnabled(false);

                    poll.getPollOptions().get(1).addVote();
                    updatePollDataInSharedPrefAndFirebase(poll,poll.getPollOptions().get(1));
                    setPollData(poll,viewHolder,true);
                }
            });

            viewHolder.option3CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    viewHolder.pollOption1CheckBox.setEnabled(false);
                    viewHolder.option2CheckBox.setEnabled(false);
                    viewHolder.option3CheckBox.setEnabled(false);
                    viewHolder.option4CheckBox.setEnabled(false);

                    poll.getPollOptions().get(2).addVote();
                    updatePollDataInSharedPrefAndFirebase(poll,poll.getPollOptions().get(2));
                    setPollData(poll,viewHolder,true);
                }
            });

            viewHolder.option4CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    viewHolder.pollOption1CheckBox.setEnabled(false);
                    viewHolder.option2CheckBox.setEnabled(false);
                    viewHolder.option3CheckBox.setEnabled(false);
                    viewHolder.option4CheckBox.setEnabled(false);

                    poll.getPollOptions().get(3).addVote();
                    updatePollDataInSharedPrefAndFirebase(poll,poll.getPollOptions().get(3));
                    setPollData(poll,viewHolder,true);
                }
            });
            setPollData(poll,viewHolder,false);

            viewHolder.pollCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Variables.postToBeViewed  = post;
//                    Intent i = new Intent(mActivity, ViewPostActivity.class);
//                    mActivity.startActivity(i);

                    LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(Constants.SHOW_VIEW_POST));

                }
            });

        }

        if(mPosts.size()-1==i){
            //its the last element so showing loading animation below
            canAnimateLoadingScreens = true;
            viewHolder.loadingContainerLinearLayout.setVisibility(View.GONE);
//            startLoadingAnimations(viewHolder.loadingContainerLinearLayout);

            LocalBroadcastManager.getInstance(mActivity).registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    canAnimateLoadingScreens = false;
                    viewHolder.loadingContainerLinearLayout.setVisibility(View.GONE);
                    LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(this);
                }
            },new IntentFilter("LOADING-SCREEN"));

        }else{
            canAnimateLoadingScreens = false;
            viewHolder.loadingContainerLinearLayout.setVisibility(View.GONE);
        }

    }

    private void loadImageFromFirebase(final ViewHolder viewHolder,final Post post){
        String id = "";
        if(post.getPostType().equals(Constants.ANNOUNCEMENTS)){
            id = post.getAnnouncement().getAnnouncementId();
        }else if(post.getPostType().equals(Constants.PETITIONS)){
            id = post.getPetition().getPetitionId();
        }

        DatabaseReference imageRef = FirebaseDatabase.getInstance().getReference(Constants.UPLOAD_IMAGES).child(id);
        imageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String image = dataSnapshot.getValue(String.class);
                if(post.getPostType().equals(Constants.ANNOUNCEMENTS)){
                    post.getAnnouncement().setEncodedAnnouncementImage(image);
                }else if(post.getPostType().equals(Constants.PETITIONS)){
                    post.getPetition().setEncodedPetitionImage(image);
                }

                generateBitmaps(viewHolder,post);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void generateBitmaps(final ViewHolder viewHolder,final Post post){
        BlurPostBackTask bl = new BlurPostBackTask();
        bl.setFields(post,viewHolder);
        bl.execute();
    }


    private void startLoadingAnimations(final LinearLayout loadingContainerLinearLayout){
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
                                            startLoadingAnimations(loadingContainerLinearLayout);
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

    private void startImageLoadingAnimations(final ViewHolder viewHolder){
        final float alpha = 0f;
        final int duration = 600;

        final float alphaR = 0.4f;
        final int durationR = 600;

        if(canAnimateImageLoadingScreens) {
            viewHolder.announcementPostImageViewBack.setVisibility(View.VISIBLE);

            viewHolder.announcementPostImageViewBack.animate().alpha(alpha).setDuration(duration).setInterpolator(new LinearInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            viewHolder.announcementPostImageViewBack.animate().alpha(alphaR).setDuration(durationR).setInterpolator(new LinearInterpolator())
                                    .setListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animator) {
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
                    }).start();

            viewHolder.petitionImageViewBack.animate().alpha(alpha).setDuration(duration).setInterpolator(new LinearInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            viewHolder.petitionImageViewBack.animate().alpha(alphaR).setDuration(durationR).setInterpolator(new LinearInterpolator())
                                    .setListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animator) {
                                            startImageLoadingAnimations(viewHolder);
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
                    }).start();
        }else{
            viewHolder.petitionImageViewBack.setVisibility(View.VISIBLE);
            viewHolder.petitionImageViewBack.clearAnimation();
            viewHolder.announcementPostImageViewBack.clearAnimation();

//            viewHolder.announcementPostImageViewBack.setAlpha(0.25f);
//            viewHolder.petitionImageViewBack.setAlpha(0.25f);
        }

    }

    private void stopImageLoadingAnimations(final ViewHolder viewHolder){
        canAnimateImageLoadingScreens = false;
    }

    private void setPollData(Poll poll, ViewHolder viewHolder, boolean isShowingResult){
        isShowingResult = new SharedPreferenceManager(mActivity).hasUserVotedInPoll(poll);

        int totalVotes = 0;
        int barWidth = Utils.dpToPx(Constants.POST_CARD_VIEW_WIDTH-20);
        for(PollOption op:poll.getPollOptions()){
            totalVotes+=op.getVotes();
        }
        viewHolder.option1LinearLayout.setVisibility(View.GONE);
        viewHolder.option2LinearLayout.setVisibility(View.GONE);
        viewHolder.option3LinearLayout.setVisibility(View.GONE);
        viewHolder.option4LinearLayout.setVisibility(View.GONE);
        //option 1
        viewHolder.pollOption1CheckBox.setText(poll.getPollOptions().get(0).getOptionText());
        viewHolder.option1LinearLayout.setVisibility(View.VISIBLE);

        if(totalVotes==0) viewHolder.pollVoteCountTextView.setText(mActivity.getResources().getString(R.string.zero_votes));
        else if(totalVotes==1)viewHolder.pollVoteCountTextView.setText(mActivity.getResources().getString(R.string.one_vote));
        else viewHolder.pollVoteCountTextView.setText(totalVotes+mActivity.getResources().getString(R.string.votes));

        int div = totalVotes;
        if(div==0) div=1;
        int option1Percentage = (int)((poll.getPollOptions().get(0).getVotes()/(div))*100);
        if(isShowingResult){
            viewHolder.option1PercentageTextView.setText(option1Percentage+"%");
            viewHolder.option1PercentageBarView.setProgress(option1Percentage);
        }else{
            viewHolder.option1PercentageTextView.setText("0%");
            viewHolder.option1PercentageBarView.setProgress(option1Percentage);
        }

        if(poll.getPollOptions().size()>1) {
            //option 2
            viewHolder.option2CheckBox.setText(poll.getPollOptions().get(1).getOptionText());
            viewHolder.option2LinearLayout.setVisibility(View.VISIBLE);
            int option2Percentage = (int)(poll.getPollOptions().get(1).getVotes() / div) * 100;
            if(isShowingResult){
                viewHolder.option2PercentageTextView.setText(option2Percentage + "%");
                viewHolder.option2PercentageBar.setProgress(option2Percentage);
            }else{
                viewHolder.option2PercentageTextView.setText("0%");
                viewHolder.option2PercentageBar.setProgress(option2Percentage);
            }

        }

        if(poll.getPollOptions().size()>2) {
            //option 3
            viewHolder.option3CheckBox.setText(poll.getPollOptions().get(2).getOptionText());
            viewHolder.option3LinearLayout.setVisibility(View.VISIBLE);
            int option3Percentage = (int)(poll.getPollOptions().get(2).getVotes() / div) * 100;
            if(isShowingResult){
                viewHolder.option3PercentageTextView.setText(option3Percentage + "%");
                viewHolder.option3PercentageView.setProgress(option3Percentage);
            }else{
                viewHolder.option3PercentageTextView.setText("0%");
                viewHolder.option3PercentageView.setProgress(option3Percentage);
            }

        }

        if(poll.getPollOptions().size()>3) {
            //option 4
            viewHolder.option4LinearLayout.setVisibility(View.VISIBLE);
            viewHolder.option4CheckBox.setText(poll.getPollOptions().get(3).getOptionText());
            int option4Percentage = (int)(poll.getPollOptions().get(3).getVotes() / div) * 100;
            if(isShowingResult){
                viewHolder.option4PercentageTextView.setText(option4Percentage + "%");
                viewHolder.option4PercentageBarView.setProgress(option4Percentage);
            }else{
                viewHolder.option4PercentageTextView.setText("0%");
                viewHolder.option4PercentageBarView.setProgress(option4Percentage);
            }

        }
    }


    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    private class BlurPostBackTask extends AsyncTask<String, Void, String> {
        private Post post;
        private Bitmap blurredBackImage;
        private Bitmap backImage;
        private ViewHolder viewHolder;

        public void setFields(Post post, ViewHolder viewHolder){
            this.post = post;
            this.viewHolder = viewHolder;
        }

        @Override
        protected String doInBackground(String... strings) {
            String image;
            if(post.getPostType().equals(Constants.ANNOUNCEMENTS)){
                image = post.getAnnouncement().getEncodedAnnouncementImage();
            }else{
                image = post.getPetition().getEncodedPetitionImage();
            }
            byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
            backImage = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
            if(post.getPostType().equals(Constants.ANNOUNCEMENTS)){
                post.getAnnouncement().setAnnouncementBitmap(backImage);
            }else{
                post.getPetition().setPetitionBitmap(backImage);
            }
            blurredBackImage = fastBlur(backImage,0.7f,27);



            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            stopImageLoadingAnimations(viewHolder);
            if(post.getPostType().equals(Constants.ANNOUNCEMENTS)) {
                Variables.blurredBacks.put(post.getAnnouncement().getAnnouncementId(),blurredBackImage);
                viewHolder.announcementPostImageViewBack.setImageBitmap(blurredBackImage);
                viewHolder.announcementImageView.setImageBitmap(post.getAnnouncement().getAnnouncementBitmap());
                viewHolder.announcementPostImageViewBack.setAlpha(0.25f);
                postImage = backImage;
                postImageBack = blurredBackImage;
            }else if(post.getPostType().equals(Constants.PETITIONS)){
                Variables.blurredBacks.put(post.getPetition().getPetitionId(),blurredBackImage);
                viewHolder.petitionImageViewBack.setImageBitmap(blurredBackImage);
                viewHolder.petitionImageView.setImageBitmap(post.getPetition().getPetitionBitmap());
                viewHolder.petitionImageViewBack.setAlpha(0.25f);
                postImage = backImage;
                postImageBack = blurredBackImage;
            }
        }

    }

    private Bitmap fastBlur(Bitmap sentBitmap, float scale, int radius) {

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
//        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

//        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }



    class ViewHolder extends RecyclerView.ViewHolder{
       ImageView userImageView;
       LinearLayout loadingContainerLinearLayout;

       //Announcement part
       CardView announcementCardView;
       TextView announcementUploaderNameTextView;
       TextView announcementCountyNameTextView;
       TextView announcementDetailsTextView;
       ImageView announcementPostImageViewBack;
       ImageView announcementImageView;
       ProgressBar announcementBlurProgressBar;

       //Poll part
       CardView pollCardView;
       TextView pollUploaderNameTextView;
       TextView pollCountyNameTextView;
       TextView pollDetailsTextView;

       LinearLayout option1LinearLayout;
       CheckBox pollOption1CheckBox;
       TextView option1PercentageTextView;
       ProgressBar option1PercentageBarView;

       LinearLayout option2LinearLayout;
       CheckBox option2CheckBox;
       TextView option2PercentageTextView;
       ProgressBar option2PercentageBar;

       LinearLayout option3LinearLayout;
       CheckBox option3CheckBox;
       TextView option3PercentageTextView;
       ProgressBar option3PercentageView;

       LinearLayout option4LinearLayout;
       CheckBox option4CheckBox;
       TextView option4PercentageTextView;
       ProgressBar option4PercentageBarView;

       TextView pollVoteCountTextView;

       //petitionPart
       CardView petitionCardView;
       TextView petitionUploaderNameTextView;
       TextView petitionCountyTextView;
       TextView petitionDetailsTextView;
       ImageView petitionImageViewBack;
       ImageView petitionImageView;
       ProgressBar petitionBlurProgressBar;
       ProgressBar petitionPercentageProgressView;
       TextView numberSignedTextView;
       TextView signTextView;


       ViewHolder(View itemView) {
            super(itemView);

           loadingContainerLinearLayout = itemView.findViewById(R.id.loadingContainerLinearLayout);

            //announcement ui
            announcementCardView = itemView.findViewById(R.id.announcementCardView);
            announcementUploaderNameTextView = itemView.findViewById(R.id.announcementUploaderNameTextView);
            announcementCountyNameTextView = itemView.findViewById(R.id.announcementCountyNameTextView);
            announcementDetailsTextView = itemView.findViewById(R.id.announcementDetailsTextView);
            announcementPostImageViewBack = itemView.findViewById(R.id.announcementPostImageViewBack);
            announcementImageView = itemView.findViewById(R.id.announcementImageView);
            announcementBlurProgressBar = itemView.findViewById(R.id.announcementBlurProgressBar);

            //poll ui
            pollCardView = itemView.findViewById(R.id.pollCardView);
            pollUploaderNameTextView = itemView.findViewById(R.id.pollUploaderNameTextView);
            pollCountyNameTextView = itemView.findViewById(R.id.pollCountyNameTextView);
            pollDetailsTextView = itemView.findViewById(R.id.pollDetailsTextView);

            option1LinearLayout = itemView.findViewById(R.id.option1LinearLayout);
            pollOption1CheckBox = itemView.findViewById(R.id.pollOption1CheckBox);
            option1PercentageTextView = itemView.findViewById(R.id.option1PercentageTextView);
            option1PercentageBarView = itemView.findViewById(R.id.option1PercentageBarView);

            option2LinearLayout = itemView.findViewById(R.id.option2LinearLayout);
            option2CheckBox = itemView.findViewById(R.id.option2CheckBox);
            option2PercentageTextView = itemView.findViewById(R.id.option2PercentageTextView);
            option2PercentageBar = itemView.findViewById(R.id.option2PercentageBarView);

            option3LinearLayout = itemView.findViewById(R.id.option3LinearLayout);
            option3CheckBox = itemView.findViewById(R.id.option3CheckBox);
            option3PercentageTextView = itemView.findViewById(R.id.option3PercentageTextView);
            option3PercentageView = itemView.findViewById(R.id.option3PercentageView);

            option4LinearLayout = itemView.findViewById(R.id.option4LinearLayout);
            option4CheckBox = itemView.findViewById(R.id.option4CheckBox);
            option4PercentageTextView = itemView.findViewById(R.id.option4PercentageTextView);
            option4PercentageBarView = itemView.findViewById(R.id.option4PercentageBarView);

           pollVoteCountTextView = itemView.findViewById(R.id.pollVoteCountTextView);

            //petition ui
            petitionCardView = itemView.findViewById(R.id.petitionCardView);
            petitionUploaderNameTextView = itemView.findViewById(R.id.petitionUploaderNameTextView);
            petitionCountyTextView = itemView.findViewById(R.id.petitionCountyTextView);
            petitionDetailsTextView = itemView.findViewById(R.id.petitionDetailsTextView);
            petitionImageViewBack = itemView.findViewById(R.id.petitionImageViewBack);
            petitionImageView = itemView.findViewById(R.id.petitionImageView);
            petitionBlurProgressBar = itemView.findViewById(R.id.petitionBlurProgressBar);
            petitionPercentageProgressView = itemView.findViewById(R.id.petitionPercentageProgressView);
            numberSignedTextView = itemView.findViewById(R.id.numberSignedTextView);
            signTextView = itemView.findViewById(R.id.signTextView);

        }
    }

    public interface OnBottomReachedListener {
        void onBottomReached(int position);
    }

    private void updatePollDataInSharedPrefAndFirebase(Poll poll, PollOption po){
        new DatabaseManager(mActivity,"").recordPollVote(poll,po).updatePollOptionData(poll,po);
        new SharedPreferenceManager(mActivity).recordPollVote(poll.getPollId(),po);
    }

    private void updatePetitionDataInSharedPreferencesAndFirebase(Petition p){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String name = new SharedPreferenceManager(mActivity).loadNameInSharedPref();
        long timestamp = Calendar.getInstance().getTimeInMillis();
        PetitionSignature signature = new PetitionSignature(uid,name,email,timestamp);

        new DatabaseManager(mActivity,"").recordPetitionSignature(p).updatePetitionSignatureData(p,signature);

        new SharedPreferenceManager(mActivity).recordPetition(p.getPetitionId());
    }
}
