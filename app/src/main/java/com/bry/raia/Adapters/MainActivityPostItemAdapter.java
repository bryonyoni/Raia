package com.bry.raia.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;

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

import java.util.Calendar;
import java.util.List;

public class MainActivityPostItemAdapter extends RecyclerView.Adapter<MainActivityPostItemAdapter.ViewHolder> {
    private List<Post> mPosts;
    private Activity mActivity;
    OnBottomReachedListener onBottomReachedListener;


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

        if(post.getPostType().equals(Constants.ANNOUNCEMENTS)){
            //its a announcement
            Announcement announcement = post.getAnnouncement();
            viewHolder.announcementCardView.setVisibility(View.VISIBLE);
            viewHolder.announcementUploaderNameTextView.setText(announcement.getUploaderUsername());
            viewHolder.announcementCountyNameTextView.setText(announcement.getCounty().getCountyName());
            viewHolder.announcementDetailsTextView.setText(announcement.getAnnouncementTitle());

            viewHolder.announcementImageView.setImageBitmap(announcement.getAnnouncementBitmap());
            BlurPostBackTask bp = new BlurPostBackTask();
            bp.setFields(post,viewHolder.announcementBlurProgressBar,viewHolder.petitionImageViewBack);

            viewHolder.announcementImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Variables.postToBeViewed  = post;
                    Variables.postToBeViewedImageBackground = Variables.blurredBacks.get(post.getAnnouncement().getAnnouncementId());

                    Intent i = new Intent(mActivity, ViewPostActivity.class);
                    mActivity.startActivity(i);
                }
            });


        }else if(post.getPostType().equals(Constants.PETITIONS)){
            //its a petition
            final Petition petition = post.getPetition();
            viewHolder.petitionCardView.setVisibility(View.VISIBLE);
            viewHolder.petitionUploaderNameTextView.setText(petition.getUploaderUsername());
            viewHolder.petitionCountyTextView.setText(petition.getCounty().getCountyName());
            viewHolder.petitionDetailsTextView.setText(petition.getPetitionTitle());

            viewHolder.petitionImageView.setImageBitmap(petition.getPetitionBitmap());
            BlurPostBackTask bp = new BlurPostBackTask();
            bp.setFields(post,viewHolder.petitionBlurProgressBar,viewHolder.announcementPostImageViewBack);

            viewHolder.numberSignedTextView.setText(String.format("%d signed", petition.getSignatures().size()));

            long percentage = (petition.getSignatures().size()/petition.getPetitionSignatureTarget())*100;
            if(percentage<100){
                //just sets the translation to a fraction of the number of people left to sign, multiplied by the petition bar width
                int barWidth = Utils.dpToPx(Constants.POST_CARD_VIEW_WIDTH-20);
                int translation = (int)(((100-(percentage/100))/100)*barWidth);
                viewHolder.petitionPercentageView.setTranslationX(translation);
            }else{
                viewHolder.petitionPercentageView.setTranslationX(0);
            }

            viewHolder.signTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updatePetitionDataInSharedPreferencesAndFirebase(petition);
                    viewHolder.signTextView.setVisibility(View.INVISIBLE);
                }
            });

            if(new SharedPreferenceManager(mActivity).hasUserSignedPetition(petition)){
                viewHolder.signTextView.setVisibility(View.INVISIBLE);
            }

            viewHolder.petitionImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Variables.postToBeViewed  = post;
                    Variables.postToBeViewedImageBackground = Variables.blurredBacks.get(post.getPetition().getPetitionId());

                    Intent i = new Intent(mActivity, ViewPostActivity.class);
                    mActivity.startActivity(i);
                }
            });

        }else{
            //its a poll
            final Poll poll = post.getPoll();
            viewHolder.pollCardView.setVisibility(View.VISIBLE);
            viewHolder.pollUploaderNameTextView.setText(poll.getUploaderUsername());
            viewHolder.pollCountyNameTextView.setText(poll.getCounty().getCountyName());
            viewHolder.pollDetailsTextView.setText(poll.getPollTitle());

            /*To-Do: add logic for auto setting previously checked data*/

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

            viewHolder.pollCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Variables.postToBeViewed  = post;
                    Intent i = new Intent(mActivity, ViewPostActivity.class);
                    mActivity.startActivity(i);
                }
            });

        }

    }

    private void setPollData(Poll poll, ViewHolder viewHolder, boolean isShowingResult){
        int totalVotes = 0;
        int barWidth = Utils.dpToPx(Constants.POST_CARD_VIEW_WIDTH-20);
        for(PollOption op:poll.getPollOptions()){
            totalVotes+=op.getVotes();
        }
        //option 1
        viewHolder.pollOption1CheckBox.setText(poll.getPollOptions().get(0).getOptionText());
        viewHolder.option1LinearLayout.setVisibility(View.VISIBLE);
        long option1Percentage = (poll.getPollOptions().get(0).getVotes()/totalVotes)*100;
        if(isShowingResult){
            viewHolder.option1PercentageTextView.setText(option1Percentage+"%");
            int translation1 = (int)(((100-(option1Percentage/100))/100)*barWidth);
            viewHolder.option1PercentageBarView.setTranslationX(translation1);
        }else{
            viewHolder.option1PercentageTextView.setText("0%");
            viewHolder.option1PercentageBarView.setTranslationX(0);
        }

        if(poll.getPollOptions().get(1)!=null) {
            //option 2
            viewHolder.option2CheckBox.setText(poll.getPollOptions().get(1).getOptionText());
            viewHolder.option2LinearLayout.setVisibility(View.VISIBLE);
            long option2Percentage = (poll.getPollOptions().get(1).getVotes() / totalVotes) * 100;
            if(isShowingResult){
                viewHolder.option2PercentageTextView.setText(option2Percentage + "%");
                int translation2 = (int) (((100 - (option2Percentage / 100)) / 100) * barWidth);
                viewHolder.option2PercentageBar.setTranslationX(translation2);
            }else{
                viewHolder.option2PercentageTextView.setText("0%");
                viewHolder.option2PercentageBar.setTranslationX(0);
            }

        }

        if(poll.getPollOptions().get(2)!=null) {
            //option 3
            viewHolder.option3CheckBox.setText(poll.getPollOptions().get(2).getOptionText());
            viewHolder.option3LinearLayout.setVisibility(View.VISIBLE);
            long option3Percentage = (poll.getPollOptions().get(2).getVotes() / totalVotes) * 100;
            if(isShowingResult){
                viewHolder.option3PercentageTextView.setText(option3Percentage + "%");
                int translation3 = (int) (((100 - (option3Percentage / 100)) / 100) * barWidth);
                viewHolder.option3PercentageView.setTranslationX(translation3);
            }else{
                viewHolder.option3PercentageTextView.setText("0%");
                viewHolder.option3PercentageView.setTranslationX(0);
            }

        }

        if(poll.getPollOptions().get(3)!=null) {
            //option 4
            viewHolder.option4LinearLayout.setVisibility(View.VISIBLE);
            viewHolder.option4CheckBox.setText(poll.getPollOptions().get(3).getOptionText());
            long option4Percentage = (poll.getPollOptions().get(3).getVotes() / totalVotes) * 100;
            if(isShowingResult){
                viewHolder.option4PercentageTextView.setText(option4Percentage + "%");
                int translation4 = (int) (((100 - (option4Percentage / 100)) / 100) * barWidth);
                viewHolder.option4PercentageBarView.setTranslationX(translation4);
            }else{
                viewHolder.option4PercentageTextView.setText("0%");
                viewHolder.option4PercentageBarView.setTranslationX(0);
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
        private ProgressBar announcementBlurProgressBar;
        private ImageView announcementImageView;

        public void setFields(Post post, ProgressBar pBar, ImageView annImageView){
            this.post = post;
            this.announcementBlurProgressBar = pBar;
            this.announcementImageView = annImageView;
        }

        @Override
        protected String doInBackground(String... strings) {
            if(post.getPostType().equals(Constants.ANNOUNCEMENTS)){
                //its a announcement
                Announcement announcement = post.getAnnouncement();
                blurredBackImage = fastBlur(announcement.getAnnouncementBitmap(),0.7f,27);
            }else if(post.getPostType().equals(Constants.PETITIONS)){
                //its a petition
                Petition petition = post.getPetition();
                blurredBackImage = fastBlur(petition.getPetitionBitmap(),0.7f,27);
            }else{
                //its a poll

            }

            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            announcementBlurProgressBar.setVisibility(View.GONE);
            announcementImageView.setImageBitmap(blurredBackImage);

            if(post.getPostType().equals(Constants.ANNOUNCEMENTS)) {
                Variables.blurredBacks.put(post.getAnnouncement().getAnnouncementId(),blurredBackImage);
            }else if(post.getPostType().equals(Constants.PETITIONS)){
                Variables.blurredBacks.put(post.getPetition().getPetitionId(),blurredBackImage);
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
       View option1PercentageBarView;

       LinearLayout option2LinearLayout;
       CheckBox option2CheckBox;
       TextView option2PercentageTextView;
       View option2PercentageBar;

       LinearLayout option3LinearLayout;
       CheckBox option3CheckBox;
       TextView option3PercentageTextView;
       View option3PercentageView;

       LinearLayout option4LinearLayout;
       CheckBox option4CheckBox;
       TextView option4PercentageTextView;
       View option4PercentageBarView;

       //petitionPart
       CardView petitionCardView;
       TextView petitionUploaderNameTextView;
       TextView petitionCountyTextView;
       TextView petitionDetailsTextView;
       ImageView petitionImageViewBack;
       ImageView petitionImageView;
       ProgressBar petitionBlurProgressBar;
       View petitionPercentageView;
       TextView numberSignedTextView;
       TextView signTextView;


       ViewHolder(View itemView) {
            super(itemView);

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
            option2PercentageBar = itemView.findViewById(R.id.option2PercentageBar);

            option3LinearLayout = itemView.findViewById(R.id.option3LinearLayout);
            option3CheckBox = itemView.findViewById(R.id.option3CheckBox);
            option3PercentageTextView = itemView.findViewById(R.id.option3PercentageTextView);
            option3PercentageView = itemView.findViewById(R.id.option3PercentageView);

            option4LinearLayout = itemView.findViewById(R.id.option4LinearLayout);
            option4CheckBox = itemView.findViewById(R.id.option4CheckBox);
            option4PercentageTextView = itemView.findViewById(R.id.option4PercentageTextView);
            option4PercentageBarView = itemView.findViewById(R.id.option4PercentageBarView);

            //petition ui
            petitionCardView = itemView.findViewById(R.id.petitionCardView);
            petitionUploaderNameTextView = itemView.findViewById(R.id.petitionUploaderNameTextView);
            petitionCountyTextView = itemView.findViewById(R.id.petitionCountyTextView);
            petitionDetailsTextView = itemView.findViewById(R.id.petitionDetailsTextView);
            petitionImageViewBack = itemView.findViewById(R.id.petitionImageViewBack);
            petitionImageView = itemView.findViewById(R.id.petitionImageView);
            petitionBlurProgressBar = itemView.findViewById(R.id.petitionBlurProgressBar);
            petitionPercentageView = itemView.findViewById(R.id.petitionPercentageView);
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
