package com.bry.raia.Activities;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bry.raia.Adapters.MainActivityPostItemAdapter;
import com.bry.raia.Adapters.ViewPostActivityCommentItemAdapter;
import com.bry.raia.Constants;
import com.bry.raia.Models.Announcement;
import com.bry.raia.Models.Comment;
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
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ViewPostActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = ViewPostActivity.class.getSimpleName();
    private Context mContext;
    private Post mPost;

    @Bind(R.id.previousActivityImageView) ImageView previousActivityImageView;
    @Bind(R.id.postTypeTextView) TextView postTypeTextView;
    @Bind(R.id.userNameTextView) TextView userNameTextView;
    @Bind(R.id.postTitleTextView) TextView postTitleTextView;

    @Bind(R.id.announcementCardView) CardView announcementCardView;
    @Bind(R.id.announcementPostImageViewBack) ImageView announcementPostImageViewBack;
    @Bind(R.id.announcementImageView) ImageView announcementImageView;
    @Bind(R.id.announcementBlurProgressBar) ProgressBar announcementBlurProgressBar;
    @Bind(R.id.loadingContainerLinearLayout) LinearLayout loadingContainerLinearLayout;
    private boolean canAnimateLoadingScreens = false;

    @Bind(R.id.pollCardView)CardView pollCardView;
    @Bind(R.id.pollUploaderNameTextView)TextView pollUploaderNameTextView;
    @Bind(R.id.pollCountyNameTextView)TextView pollCountyNameTextView;
    @Bind(R.id.pollDetailsTextView)TextView pollDetailsTextView;
    @Bind(R.id.option1LinearLayout)LinearLayout option1LinearLayout;
    @Bind(R.id.pollOption1CheckBox)CheckBox pollOption1CheckBox;
    @Bind(R.id.option1PercentageTextView)TextView option1PercentageTextView;
    @Bind(R.id.option1PercentageBarView)View option1PercentageBarView;
    @Bind(R.id.option2LinearLayout) LinearLayout option2LinearLayout;
    @Bind(R.id.option2CheckBox)CheckBox option2CheckBox;
    @Bind(R.id.option2PercentageTextView)TextView option2PercentageTextView;
    @Bind(R.id.option2PercentageBar)View option2PercentageBar;
    @Bind(R.id.option3LinearLayout)LinearLayout option3LinearLayout;
    @Bind(R.id.option3CheckBox)CheckBox option3CheckBox;
    @Bind(R.id.option3PercentageTextView)TextView option3PercentageTextView;
    @Bind(R.id.option3PercentageView)View option3PercentageView;
    @Bind(R.id.option4LinearLayout)LinearLayout option4LinearLayout;
    @Bind(R.id.option4CheckBox)CheckBox option4CheckBox;
    @Bind(R.id.option4PercentageTextView)TextView option4PercentageTextView;
    @Bind(R.id.option4PercentageBarView)View option4PercentageBarView;

    @Bind(R.id.petitionUiLinearLayout) LinearLayout petitionUiLinearLayout;
    @Bind(R.id.petitionImageViewBack) ImageView petitionImageViewBack;
    @Bind(R.id.petitionImageView) ImageView petitionImageView;
    @Bind(R.id.petitionBlurProgressBar) ProgressBar petitionBlurProgressBar;
    @Bind(R.id.petitionPercentageView) View petitionPercentageView;
    @Bind(R.id.numberSignedTextView) TextView numberSignedTextView;
    @Bind(R.id.signTextView) TextView signButtonTextView;

    @Bind(R.id.addCommentEditText) EditText addCommentEditText;
    @Bind(R.id.sendCommentImageView) ImageView sendCommentImageView;
    @Bind(R.id.commentsRecyclerView) RecyclerView commentsRecyclerView;
    @Bind(R.id.noCommentsTextView) TextView noCommentsTextView;
    ViewPostActivityCommentItemAdapter vpActivityCommentAdapter;
    List<Comment> allComments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);
        mContext = this.getApplicationContext();
        ButterKnife.bind(this);

        mPost = Variables.postToBeViewed;
        setData();
        loadComments();
    }

    private void loadComments() {
        String postId;

        if(mPost.getPostType().equals(Constants.ANNOUNCEMENTS)){
            postId = mPost.getAnnouncement().getAnnouncementId();
        }else if(mPost.getPostType().equals(Constants.PETITIONS)){
            postId = mPost.getPetition().getPetitionId();
        }else {
            postId = mPost.getPoll().getPollId();
        }

        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS).child(postId);
        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Comment> allComments = new ArrayList<>();
                if(dataSnapshot.exists()){
                    for(DataSnapshot commentSnap:dataSnapshot.getChildren()){
                        Comment comment = commentSnap.getValue(Comment.class);
                        if(commentSnap.child(Constants.REPLIES).exists()){
                            //some replies exist
                            for(DataSnapshot replySnap:dataSnapshot.child(Constants.REPLIES).getChildren()){
                                Comment reply = replySnap.getValue(Comment.class);
                                comment.addReply(reply);
                            }
                        }
                        allComments.add(comment);
                    }
                }

                loadCommentsIntoRecyclerView(allComments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setData() {
        if(mPost.getPostType().equals(Constants.ANNOUNCEMENTS)) {
            //its a announcement
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;

            Announcement announcement = mPost.getAnnouncement();
            postTypeTextView.setText(getString(R.string.announcement));
            announcementCardView.setVisibility(View.VISIBLE);
            userNameTextView.setText(String.format("By %s to %s", announcement.getUploaderUsername(), announcement.getCounty().getCountyName()));
            postTitleTextView.setText(announcement.getAnnouncementTitle());

            announcementImageView.setImageBitmap(announcement.getAnnouncementBitmap());
            announcementPostImageViewBack.setImageBitmap(Variables.postToBeViewedImageBackground);
        }else if(mPost.getPostType().equals(Constants.PETITIONS)){
            //its a petition
            final Petition petition = mPost.getPetition();
            petitionUiLinearLayout.setVisibility(View.VISIBLE);
            postTypeTextView.setText(getString(R.string.petition));
            userNameTextView.setText(String.format("By %s to %s", petition.getUploaderUsername(), petition.getCounty().getCountyName()));
            postTitleTextView.setText(petition.getPetitionTitle());

            petitionImageView.setImageBitmap(petition.getPetitionBitmap());
            petitionImageViewBack.setImageBitmap(Variables.postToBeViewedImageBackground);
            numberSignedTextView.setText(String.format("%d signed", petition.getSignatures().size()));

            long percentage = (petition.getSignatures().size()/petition.getPetitionSignatureTarget())*100;
            if(percentage<100){
                //just sets the translation to a fraction of the number of people left to sign, multiplied by the petition bar width
                int barWidth = Utils.dpToPx(Constants.POST_CARD_VIEW_WIDTH-20);
                int translation = (int)(((100-(percentage/100))/100)*barWidth);
                petitionPercentageView.setTranslationX(translation);
            }else{
                petitionPercentageView.setTranslationX(0);
            }

            signButtonTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updatePetitionDataInSharedPreferencesAndFirebase(petition);
                    signButtonTextView.setVisibility(View.INVISIBLE);
                }
            });

            if(new SharedPreferenceManager(mContext).hasUserSignedPetition(petition)){
                signButtonTextView.setVisibility(View.INVISIBLE);
            }

        }else {
            //its a poll
            final Poll poll = mPost.getPoll();
            pollCardView.setVisibility(View.VISIBLE);
            pollUploaderNameTextView.setText(poll.getUploaderUsername());
            pollCountyNameTextView.setText(poll.getCounty().getCountyName());
            pollDetailsTextView.setText(poll.getPollTitle());

            /*To-Do: add logic for auto setting previously checked data*/

            pollOption1CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);

                    poll.getPollOptions().get(0).addVote();
                    updatePollDataInSharedPrefAndFirebase(poll, poll.getPollOptions().get(0));
                    setPollData(poll, true);
                }
            });

            option2CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);

                    poll.getPollOptions().get(1).addVote();
                    updatePollDataInSharedPrefAndFirebase(poll, poll.getPollOptions().get(1));
                    setPollData(poll, true);
                }
            });

            option3CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);

                    poll.getPollOptions().get(2).addVote();
                    updatePollDataInSharedPrefAndFirebase(poll, poll.getPollOptions().get(2));
                    setPollData(poll,  true);
                }
            });

            option4CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);

                    poll.getPollOptions().get(3).addVote();
                    updatePollDataInSharedPrefAndFirebase(poll, poll.getPollOptions().get(3));
                    setPollData(poll, true);
                }
            });
            setPollData(poll,  false);
            //I know, this is bad code, I admit. I could have done better and have no reason not to, but fuck it you know, I'm super stressed so this is how its gonna be for now.
            if (new SharedPreferenceManager(mContext).hasUserVotedInPoll(poll)) {
                PollOption po = new SharedPreferenceManager(mContext).getWhichPollOptionSelected(poll);
                if (poll.getPollOptions().get(0).getOptionId().equals(po.getOptionId())) {
                    pollOption1CheckBox.setChecked(true);
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);
                } else if (poll.getPollOptions().size() > 1 && poll.getPollOptions().get(1).getOptionId().equals(po.getOptionId())) {
                    option2CheckBox.setChecked(true);
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);
                } else if (poll.getPollOptions().size() > 2 && poll.getPollOptions().get(2).getOptionId().equals(po.getOptionId())) {
                    option3CheckBox.setChecked(true);
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);
                } else if (poll.getPollOptions().size() > 3 && poll.getPollOptions().get(3).getOptionId().equals(po.getOptionId())) {
                    option4CheckBox.setChecked(true);
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);
                }
            }
        }
    }

    private void setPollData(Poll poll, boolean isShowingResult){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        int totalVotes = 0;

        int barWidth = Utils.dpToPx(width-Utils.dpToPx(60));
        for(PollOption op:poll.getPollOptions()){
            totalVotes+=op.getVotes();
        }
        //option 1
        option1LinearLayout.setVisibility(View.VISIBLE);
        pollOption1CheckBox.setText(poll.getPollOptions().get(0).getOptionText());
        long option1Percentage = (poll.getPollOptions().get(0).getVotes()/totalVotes)*100;
        if(isShowingResult){
            option1PercentageTextView.setText(option1Percentage+"%");
            int translation1 = (int)(((100-(option1Percentage/100))/100)*barWidth);
            option1PercentageBarView.setTranslationX(translation1);
        }else{
            option1PercentageTextView.setText("0%");
            option1PercentageBarView.setTranslationX(0);
        }

        if(poll.getPollOptions().get(1)!=null) {
            //option 2
            option2LinearLayout.setVisibility(View.VISIBLE);
            option2CheckBox.setText(poll.getPollOptions().get(1).getOptionText());
            long option2Percentage = (poll.getPollOptions().get(1).getVotes() / totalVotes) * 100;
            if(isShowingResult){
                option2PercentageTextView.setText(option2Percentage + "%");
                int translation2 = (int) (((100 - (option2Percentage / 100)) / 100) * barWidth);
                option2PercentageBar.setTranslationX(translation2);
            }else{
                option2PercentageTextView.setText("0%");
                option2PercentageBar.setTranslationX(0);
            }

        }

        if(poll.getPollOptions().get(2)!=null) {
            //option 3
            option3LinearLayout.setVisibility(View.VISIBLE);
            option3CheckBox.setText(poll.getPollOptions().get(2).getOptionText());
            long option3Percentage = (poll.getPollOptions().get(2).getVotes() / totalVotes) * 100;
            if(isShowingResult){
                option3PercentageTextView.setText(option3Percentage + "%");
                int translation3 = (int) (((100 - (option3Percentage / 100)) / 100) * barWidth);
                option3PercentageView.setTranslationX(translation3);
            }else{
                option3PercentageTextView.setText("0%");
                option3PercentageView.setTranslationX(0);
            }

        }

        if(poll.getPollOptions().get(3)!=null) {
            //option 4
            option4LinearLayout.setVisibility(View.VISIBLE);
            option4CheckBox.setText(poll.getPollOptions().get(3).getOptionText());
            long option4Percentage = (poll.getPollOptions().get(3).getVotes() / totalVotes) * 100;
            if(isShowingResult){
                option4PercentageTextView.setText(option4Percentage + "%");
                int translation4 = (int) (((100 - (option4Percentage / 100)) / 100) * barWidth);
                option4PercentageBarView.setTranslationX(translation4);
            }else{
                option4PercentageTextView.setText("0%");
                option4PercentageBarView.setTranslationX(0);
            }

        }
    }

    private void updatePollDataInSharedPrefAndFirebase(Poll poll, PollOption po){
        new DatabaseManager(mContext,"").recordPollVote(poll,po).updatePollOptionData(poll,po);
        new SharedPreferenceManager(mContext).recordPollVote(poll.getPollId(),po);
    }

    private void updatePetitionDataInSharedPreferencesAndFirebase(Petition p){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String name = new SharedPreferenceManager(mContext).loadNameInSharedPref();
        long timestamp = Calendar.getInstance().getTimeInMillis();
        PetitionSignature signature = new PetitionSignature(uid,name,email,timestamp);

        new DatabaseManager(mContext,"").recordPetitionSignature(p).updatePetitionSignatureData(p,signature);

        new SharedPreferenceManager(mContext).recordPetition(p.getPetitionId());
    }

    @Override
    public void onClick(View view) {
        if(view.equals(previousActivityImageView)){
            finish();
        }else if(view.equals(sendCommentImageView)){
            addComment();
        }
    }

    private void addComment() {
        String postId;

        if(mPost.getPostType().equals(Constants.ANNOUNCEMENTS)){
            postId = mPost.getAnnouncement().getAnnouncementId();
        }else if(mPost.getPostType().equals(Constants.PETITIONS)){
            postId = mPost.getPetition().getPetitionId();
        }else {
            postId = mPost.getPoll().getPollId();
        }

        String commentText = addCommentEditText.getText().toString().trim();
        if(!commentText.equals("")){
            addCommentEditText.setText("");

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Comment comment = new Comment(commentText,uid,new SharedPreferenceManager(mContext).loadNameInSharedPref());

            DatabaseReference replyRef = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS).child(postId);
            DatabaseReference pushRef = replyRef.push();
            String commentId = pushRef.getKey();
            comment.setCommentId(commentId);

            pushRef.setValue(comment);
            addNewCommentToRecyclerView(comment);

        }else{
            addCommentEditText.setError(getResources().getString(R.string.say_something));
        }
    }

    private void loadCommentsIntoRecyclerView(List<Comment> loadedComments) {
        if(loadedComments.isEmpty()){
            noCommentsTextView.setVisibility(View.VISIBLE);
            commentsRecyclerView.setVisibility(View.GONE);
        }else{
            vpActivityCommentAdapter = new ViewPostActivityCommentItemAdapter( ViewPostActivity.this,loadedComments, true,mPost);
            commentsRecyclerView.setAdapter(vpActivityCommentAdapter);
            commentsRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            allComments = loadedComments;
        }
    }

    private void addNewCommentToRecyclerView(Comment comment){
        allComments.add(comment);
        vpActivityCommentAdapter.addComment(comment);
        vpActivityCommentAdapter.notifyItemInserted(allComments.size()-1);
        vpActivityCommentAdapter.notifyDataSetChanged();
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

    private void hideCommentsLoadingAnimations(){
        canAnimateLoadingScreens = false;
        loadingContainerLinearLayout.setVisibility(View.GONE);
        commentsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showCommentsLoadingAnimations(){
        canAnimateLoadingScreens = true;
        loadingContainerLinearLayout.setVisibility(View.VISIBLE);
        commentsRecyclerView.setVisibility(View.GONE);
        startLoadingAnimations();
    }

}
