package com.bry.raia.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bry.raia.Constants;
import com.bry.raia.Models.Comment;
import com.bry.raia.Models.Post;
import com.bry.raia.R;
import com.bry.raia.Services.SharedPreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.List;

public class ViewPostActivityCommentItemAdapter extends RecyclerView.Adapter<ViewPostActivityCommentItemAdapter.ViewHolder>{
    private List<Comment> mComments;
    private Post mPost;
    private Activity mActivity;
    private Boolean canAddReplies;
    private boolean isEditTextShowing = false;


    public ViewPostActivityCommentItemAdapter(Activity activity, List<Comment> allComments, boolean canAddReplies, Post mPost){
        this.mActivity = activity;
        this.mComments = allComments;
        this.canAddReplies = canAddReplies;
        this.mPost = mPost;
    }

    public void addComment(Comment com){
        mComments.add(com);
    }

    @NonNull
    @Override
    public ViewPostActivityCommentItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View recipeView = inflater.inflate(R.layout.comment_item_view, viewGroup, false);
        return new ViewPostActivityCommentItemAdapter.ViewHolder(recipeView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewPostActivityCommentItemAdapter.ViewHolder viewHolder, final int i) {
        final Comment comment = mComments.get(i);
        viewHolder.userNameTextView.setText(String.format("By %s", comment.getCommenterName()));
        viewHolder.commentBodyTextView.setText(comment.getCommentText());
        if(comment.getCommentTime()==null)viewHolder.commentTimeTextView.setText("Some Time Ago.");
        else viewHolder.commentTimeTextView.setText(getTimeString(comment.getCommentTime()));
        if(comment.getReplies().size()==1)viewHolder.commentNumberTextView.setText("1 Reply.");
        else viewHolder.commentNumberTextView.setText(comment.getReplies().size()+" Replies");

        if(canAddReplies) {
            viewHolder.replyButtonTextView.setVisibility(View.VISIBLE);
//            viewHolder.addReplyRelativeLayout.setVisibility(View.VISIBLE);
//            viewHolder.loadedRepliesRelativeLayout.setVisibility(View.VISIBLE);


            final ViewPostActivityCommentItemAdapter vpActivityReplyAdapter = new ViewPostActivityCommentItemAdapter( mActivity,comment.getReplies(), false,mPost);
            viewHolder.repliesRecyclerView.setAdapter(vpActivityReplyAdapter);
            viewHolder.repliesRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

            viewHolder.replyButtonTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isEditTextShowing){
                        viewHolder.addReplyRelativeLayout.setVisibility(View.GONE);
                        viewHolder.replyButtonTextView.setText(mActivity.getResources().getString(R.string.reply));
                        isEditTextShowing = false;
                    }else{
                        viewHolder.replyButtonTextView.setText(mActivity.getResources().getString(R.string.cancel));
                        viewHolder.addReplyRelativeLayout.setVisibility(View.VISIBLE);
                        viewHolder.sendReplyImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String postId;

                                if(mPost.getPostType().equals(Constants.ANNOUNCEMENTS)){
                                    postId = mPost.getAnnouncement().getAnnouncementId();
                                }else if(mPost.getPostType().equals(Constants.PETITIONS)){
                                    postId = mPost.getPetition().getPetitionId();
                                }else {
                                    postId = mPost.getPoll().getPollId();
                                }
                                String reply = viewHolder.addReplyEditText.getText().toString().trim();

                                if(!reply.equals("")){
                                    viewHolder.addReplyEditText.setText("");

                                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    Comment newreply = new Comment(reply,uid,new SharedPreferenceManager(mActivity).loadNameInSharedPref());

                                    DatabaseReference replyRef = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS).child(postId).child(Constants.REPLIES);
                                    DatabaseReference pushRef = replyRef.push();
                                    String commentId = pushRef.getKey();
                                    newreply.setCommentId(commentId);

                                    pushRef.setValue(newreply);
                                    comment.addReply(newreply);
                                    vpActivityReplyAdapter.addComment(newreply);
                                    vpActivityReplyAdapter.notifyItemInserted(comment.getReplies().size()-1);
                                    vpActivityReplyAdapter.notifyDataSetChanged();

                                    viewHolder.addReplyRelativeLayout.setVisibility(View.GONE);
                                    viewHolder.replyButtonTextView.setText(mActivity.getResources().getString(R.string.reply));
                                    isEditTextShowing = false;
                                }else{
                                    viewHolder.addReplyEditText.setError(mActivity.getResources().getString(R.string.say_something));
                                }
                            }
                        });

                        isEditTextShowing = true;
                    }

                }
            });
            viewHolder.commentViewLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Constants.SHOW_COMMENT_REPLIES);
                    intent.putExtra(Constants.COMMENT_NO,i);
                    LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);
                }
            });
        }else{
            viewHolder.replyButtonTextView.setVisibility(View.GONE);
            viewHolder.addReplyRelativeLayout.setVisibility(View.GONE);
            viewHolder.loadedRepliesRelativeLayout.setVisibility(View.GONE);
            viewHolder.commentNumberTextView.setVisibility(View.GONE);
        }



    }

    private String getTimeString(long timeInMills){
        long currentTimeInMills = Calendar.getInstance().getTimeInMillis();
        long howLongAgoInMills = (currentTimeInMills-timeInMills);

        if(howLongAgoInMills< (24*60*60*1000)){
            if(howLongAgoInMills< (60*60*1000)){
                if(howLongAgoInMills< (60*1000)){
                    return "Just now.";
                }else{
                    //comment is more than one minute old
                    long minCount = howLongAgoInMills/(60*1000);
                    return minCount+" min.";
                }
            }else{
                //comment is more than an hour ago
                long hrsCount = howLongAgoInMills/(60*60*1000);
                return hrsCount+" hrs.";
            }
        }else{
            //comment is more than a day old
            long daysCount = howLongAgoInMills/(24*60*60*1000);
            return (daysCount+" days.");
        }
    }


    @Override
    public int getItemCount() {
        return mComments.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView userNameTextView;
        TextView commentTimeTextView;
        TextView commentBodyTextView;
        TextView replyButtonTextView;
        TextView commentNumberTextView;

        RelativeLayout replyRelativeLayout;

        RelativeLayout addReplyRelativeLayout;
        EditText addReplyEditText;
        ImageView sendReplyImageView;

        RelativeLayout loadedRepliesRelativeLayout;
        RecyclerView repliesRecyclerView;
        LinearLayout commentViewLinearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            commentViewLinearLayout = itemView.findViewById(R.id.commentViewLinearLayout);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            commentTimeTextView = itemView.findViewById(R.id.commentTimeTextView);
            commentBodyTextView = itemView.findViewById(R.id.commentBodyTextView);
            commentNumberTextView = itemView.findViewById(R.id.commentNumberTextView);
            replyButtonTextView = itemView.findViewById(R.id.replyButtonTextView);
            addReplyRelativeLayout = itemView.findViewById(R.id.addReplyRelativeLayout);
            addReplyEditText = itemView.findViewById(R.id.addReplyEditText);
            sendReplyImageView = itemView.findViewById(R.id.sendReplyImageView);
            loadedRepliesRelativeLayout = itemView.findViewById(R.id.loadedRepliesRelativeLayout);
            repliesRecyclerView = itemView.findViewById(R.id.repliesRecyclerView);
            replyRelativeLayout = itemView.findViewById(R.id.replyRelativeLayout);

        }
    }
}
