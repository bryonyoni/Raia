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
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bry.raia.Constants;
import com.bry.raia.Models.Announcement;
import com.bry.raia.Models.Petition;
import com.bry.raia.Models.Poll;
import com.bry.raia.Models.Post;
import com.bry.raia.R;
import com.bry.raia.Variables;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserSettingsPostItemAdapter extends RecyclerView.Adapter<UserSettingsPostItemAdapter.ViewHolder> {
    private List<Post> mPosts;
    private Activity mActivity;
    OnBottomReachedListener onBottomReachedListener;
    private boolean canAnimateLoadingScreens,canAnimateImageLoadingScreens;
    private Bitmap postImage;
    private Bitmap postImageBack;


    public UserSettingsPostItemAdapter(List<Post> mPosts, Activity activity) {
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View recipeView = inflater.inflate(R.layout.user_settings_post_item, viewGroup, false);
        return new UserSettingsPostItemAdapter.ViewHolder(recipeView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final Post post = mPosts.get(i);

        if(post.getPostType().equals(Constants.ANNOUNCEMENTS)){
            Announcement announcement = post.getAnnouncement();
            viewHolder.postTypeTextView.setText(mActivity.getResources().getString(R.string.announcement));
            viewHolder.postDetailsTextView.setText(announcement.getAnnouncementTitle());

            startImageLoadingAnimations(viewHolder);
            loadImageFromFirebase(viewHolder,post);

        }else if(post.getPostType().equals(Constants.PETITIONS)){
            final Petition petition = post.getPetition();
            viewHolder.postTypeTextView.setText(mActivity.getResources().getString(R.string.petition));
            viewHolder.postDetailsTextView.setText(petition.getPetitionTitle());

            startImageLoadingAnimations(viewHolder);
            loadImageFromFirebase(viewHolder,post);
        }else if(post.getPostType().equals(Constants.POLLS)){
            final Poll poll = post.getPoll();
            viewHolder.postTypeTextView.setText(mActivity.getResources().getString(R.string.poll));
            viewHolder.postDetailsTextView.setText(poll.getPollTitle());

            viewHolder.imageCardView.setVisibility(View.GONE);
        }

        viewHolder.imageCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder.viewPostLinearLayout.performClick();
            }
        });

        viewHolder.viewPostLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Variables.postToBeViewed = post;
                if(!post.getPostType().equals(Constants.POLLS)) {
                    BitmapDrawable bitmapDrawable = ((BitmapDrawable) viewHolder.ImageView.getDrawable());
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    Variables.image = bitmap;

                    BitmapDrawable bitmapDrawableBack = ((BitmapDrawable) viewHolder.ImageViewBack.getDrawable());
                    Bitmap bitmapBack = bitmapDrawableBack.getBitmap();
                    Variables.imageBack = bitmapBack;
                }
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(Constants.SHOW_MY_VIEW_POST));

            }
        });
    }

    private void loadImageFromFirebase(final ViewHolder viewHolder, final Post post){
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

    private void generateBitmaps(final ViewHolder viewHolder, final Post post){
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
            viewHolder.ImageViewBack.setVisibility(View.VISIBLE);

            viewHolder.ImageViewBack.animate().alpha(alpha).setDuration(duration).setInterpolator(new LinearInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            viewHolder.ImageViewBack.animate().alpha(alphaR).setDuration(durationR).setInterpolator(new LinearInterpolator())
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

            viewHolder.ImageViewBack.animate().alpha(alpha).setDuration(duration).setInterpolator(new LinearInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            viewHolder.ImageViewBack.animate().alpha(alphaR).setDuration(durationR).setInterpolator(new LinearInterpolator())
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
            viewHolder.ImageViewBack.setVisibility(View.VISIBLE);
            viewHolder.ImageViewBack.clearAnimation();
            viewHolder.ImageViewBack.clearAnimation();

//            viewHolder.announcementPostImageViewBack.setAlpha(0.25f);
//            viewHolder.petitionImageViewBack.setAlpha(0.25f);
        }

    }

    private void stopImageLoadingAnimations(final ViewHolder viewHolder){
        canAnimateImageLoadingScreens = false;
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
                viewHolder.ImageViewBack.setImageBitmap(blurredBackImage);
                viewHolder.ImageView.setImageBitmap(post.getAnnouncement().getAnnouncementBitmap());
                viewHolder.ImageViewBack.setAlpha(0.25f);
                postImage = backImage;
                postImageBack = blurredBackImage;
            }else if(post.getPostType().equals(Constants.PETITIONS)){
                Variables.blurredBacks.put(post.getPetition().getPetitionId(),blurredBackImage);
                viewHolder.ImageViewBack.setImageBitmap(blurredBackImage);
                viewHolder.ImageView.setImageBitmap(post.getPetition().getPetitionBitmap());
                viewHolder.ImageViewBack.setAlpha(0.25f);
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

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView ImageViewBack;
        ImageView ImageView;
        TextView postTypeTextView;
        TextView postDetailsTextView;
        LinearLayout takeDownLinearLayout;
        LinearLayout viewPostLinearLayout;
        CardView imageCardView;

        ViewHolder(View itemView) {
            super(itemView);
            ImageViewBack = itemView.findViewById(R.id.ImageViewBack);
            ImageView = itemView.findViewById(R.id.ImageView);
            postTypeTextView = itemView.findViewById(R.id.postTypeTextView);
            postDetailsTextView = itemView.findViewById(R.id.postDetailsTextView);
            takeDownLinearLayout = itemView.findViewById(R.id.takeDownLinearLayout);
            viewPostLinearLayout = itemView.findViewById(R.id.viewPostLinearLayout);
            imageCardView = itemView.findViewById(R.id.imageCardView);
        }
    }

    public interface OnBottomReachedListener {
        void onBottomReached(int position);
    }
}
