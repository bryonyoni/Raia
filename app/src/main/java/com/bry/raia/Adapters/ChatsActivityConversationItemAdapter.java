package com.bry.raia.Adapters;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bry.raia.Constants;
import com.bry.raia.Models.Chat;
import com.bry.raia.Models.Message;
import com.bry.raia.Models.User;
import com.bry.raia.R;
import com.bry.raia.Variables;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ChatsActivityConversationItemAdapter extends RecyclerView.Adapter<ChatsActivityConversationItemAdapter.ViewHolder>{
    private Context mContext;
    private List<Chat> allMyChats;
    private boolean canShowUploaderImage;

    public ChatsActivityConversationItemAdapter(Context con, List<Chat>allChats){
        this.mContext = con;
        this.allMyChats = allChats;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ChatsActivityConversationItemAdapter.ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.my_chats_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Chat chat = allMyChats.get(i);
        Message lastMessage;
        if(!chat.getAllMessages().isEmpty()){
            lastMessage = chat.getAllMessages().get(chat.getAllMessages().size()-1);
            viewHolder.lastMessageTextView.setText(lastMessage.getMessageText());
        }

        viewHolder.userNameTextView.setText(chat.getUser().getName());


        if(Variables.blurredBacks.containsKey(chat.getUser().getUId())){
            viewHolder.userImageView.setImageBitmap(Variables.blurredBacks.get(chat.getUser().getUId()));
        }else{
            startUploaderImageLoadingAnimations(viewHolder);
            loadUserImageFromFirebase(chat.getUser(),viewHolder);
        }

        startUploaderImageLoadingAnimations(viewHolder);
        loadUserImageFromFirebase(chat.getUser(),viewHolder);

        viewHolder.chatRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Variables.chatToOpen = chat;
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.OPEN_CHAT));
            }
        });

    }

    private void loadUserImageFromFirebase(final User user, final ChatsActivityConversationItemAdapter.ViewHolder viewHolder){
        DatabaseReference imageRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS)
                .child(user.getUId()).child(Constants.IMAGE_AVATAR);
        imageRef = FirebaseDatabase.getInstance().getReference(Constants.IMAGE_AVATAR)
                .child(user.getUId());
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

    private void generateBitmaps(final ChatsActivityConversationItemAdapter.ViewHolder viewHolder, final User user){
        ChatsActivityConversationItemAdapter.BlurPostBackTask bl = new ChatsActivityConversationItemAdapter.BlurPostBackTask();
        bl.setFields(user,viewHolder);
        bl.execute();
    }

    private class BlurPostBackTask extends AsyncTask<String, Void, String> {
        private User user;
        private Bitmap Image;
        private ChatsActivityConversationItemAdapter.ViewHolder viewHolder;

        public void setFields(User post, ChatsActivityConversationItemAdapter.ViewHolder viewHolder){
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
//            stopImageLoadingAnimations();
//            viewHolder.userImageView.setImageBitmap(Image);
            Variables.blurredBacks.put(user.getUId(),Image);

            Glide.with(mContext).load(bitmapToByte(Image)).asBitmap().centerCrop()
                    .into(new BitmapImageViewTarget(viewHolder.userImageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            try {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
//                                Bitmap.createScaledBitmap(resource,100,100,false));
                                circularBitmapDrawable.setCircular(true);
                                viewHolder.userImageView.setImageDrawable(circularBitmapDrawable);
                                stopImageLoadingAnimations();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }

    }

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }

    private void startUploaderImageLoadingAnimations(final ChatsActivityConversationItemAdapter.ViewHolder viewHolder){
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
        return allMyChats.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        View newMessagesView;
        ImageView userImageView;
        TextView userNameTextView;
        TextView lastMessageTextView;
        RelativeLayout chatRelativeLayout;

        ViewHolder(View itemView) {
            super(itemView);
            newMessagesView = itemView.findViewById(R.id.newMessagesView);
            userImageView = itemView.findViewById(R.id.userMImageView);

            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);

            chatRelativeLayout = itemView.findViewById(R.id.chatRelativeLayout);
        }
    }
}
