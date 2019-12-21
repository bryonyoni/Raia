package com.bry.raia.Adapters;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bry.raia.Constants;
import com.bry.raia.Models.County;
import com.bry.raia.Models.Post;
import com.bry.raia.Models.User;
import com.bry.raia.R;
import com.bry.raia.Variables;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChatActivitySearchedItemAdapter extends RecyclerView.Adapter<ChatActivitySearchedItemAdapter.ViewHolder>{
    private List<User> mUsers;
    private Activity mActivity;
    private boolean canShowUploaderImage;

    public ChatActivitySearchedItemAdapter(List<User> users, Activity acc){
        this.mActivity = acc;
        this.mUsers = users;
    }


    @NonNull
    @Override
    public ChatActivitySearchedItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ChatActivitySearchedItemAdapter.ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.search_user_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatActivitySearchedItemAdapter.ViewHolder viewHolder, final int i) {
        final User user = mUsers.get(i);

        viewHolder.userName.setText(user.getName());

        viewHolder.userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder.userImageView.performClick();
            }
        });

        viewHolder.userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Variables.uploaderImage = null;
                try{
                    BitmapDrawable bitmapDrawableBack = ((BitmapDrawable) viewHolder.userImageView.getDrawable());
                    Bitmap bitmapBack = bitmapDrawableBack.getBitmap();
                    Variables.uploaderImage = bitmapBack;
                }catch (Exception e){
                    e.printStackTrace();
                }


                Intent intent = new Intent(Constants.SELECTED_PERSON);
                intent.putExtra("user",i);
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);
            }
        });

        startUploaderImageLoadingAnimations(viewHolder);

        loadUserImageFromFirebase(user, viewHolder);
    }

    private void loadUserImageFromFirebase(final User user, final ChatActivitySearchedItemAdapter.ViewHolder viewHolder){
        DatabaseReference imageRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS)
                .child(user.getUId()).child(Constants.IMAGE_AVATAR);
        imageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String image = dataSnapshot.getValue(String.class);
                    user.setImageString(image);
                    generateBitmaps(viewHolder, user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void generateBitmaps(final ChatActivitySearchedItemAdapter.ViewHolder viewHolder, final User user){
        ChatActivitySearchedItemAdapter.BlurPostBackTask bl = new ChatActivitySearchedItemAdapter.BlurPostBackTask();
        bl.setFields(user,viewHolder);
        bl.execute();
    }

    private class BlurPostBackTask extends AsyncTask<String, Void, String> {
        private User user;
        private Bitmap Image;
        private ChatActivitySearchedItemAdapter.ViewHolder viewHolder;

        public void setFields(User post, ChatActivitySearchedItemAdapter.ViewHolder viewHolder){
            this.user = post;
            this.viewHolder = viewHolder;
        }

        @Override
        protected String doInBackground(String... strings) {
            String image = user.getImageString();
            byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
            Image = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);

            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            stopImageLoadingAnimations();
            viewHolder.userImageView.setImageBitmap(Image);
        }

    }

    private void startUploaderImageLoadingAnimations(final ChatActivitySearchedItemAdapter.ViewHolder viewHolder){
        final float alpha = 0f;
        final int duration = 600;

        final float alphaR = 1f;
        final int durationR = 600;

        if(canShowUploaderImage) {
            viewHolder.userImageView.setVisibility(View.VISIBLE);

            viewHolder.userImageView.animate().alpha(alpha).setDuration(duration).setInterpolator(new LinearInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            viewHolder.userImageView.animate().alpha(alphaR).setDuration(durationR).setInterpolator(new LinearInterpolator())
                                    .setListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animator) {
                                            startUploaderImageLoadingAnimations(viewHolder);
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
        }

    }

    private void stopImageLoadingAnimations(){
        canShowUploaderImage = false;
    }


    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView userName;
        ImageView userImageView;

        ViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userImageView = itemView.findViewById(R.id.userImageView);
        }
    }
}
