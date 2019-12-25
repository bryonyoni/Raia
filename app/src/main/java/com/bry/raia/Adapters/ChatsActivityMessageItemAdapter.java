package com.bry.raia.Adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bry.raia.Constants;
import com.bry.raia.Models.Message;
import com.bry.raia.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatsActivityMessageItemAdapter extends RecyclerView.Adapter<ChatsActivityMessageItemAdapter.ViewHolder>{
    private Context mContext;
    private List<Message> allMessages;


    public ChatsActivityMessageItemAdapter(Context context, List<Message> allMessages){
        this.allMessages = allMessages;
        this.mContext = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View recipeView = inflater.inflate(R.layout.message_item, viewGroup, false);
        return new ChatsActivityMessageItemAdapter.ViewHolder(recipeView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final Message message = allMessages.get(i);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(message.getSenderId().equals(uid)){
            //sent by user
            viewHolder.ReceivedMessageCard.setVisibility(View.GONE);
            viewHolder.SentMessageCard.setVisibility(View.VISIBLE);
            viewHolder.sentMessageTextView.setText(message.getMessageText());
        }else{
            viewHolder.ReceivedMessageCard.setVisibility(View.VISIBLE);
            viewHolder.SentMessageCard.setVisibility(View.GONE);
            viewHolder.receivedMessageTextView.setText(message.getMessageText());
        }

        LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String code = intent.getExtras().getString("code");
                if(code.equals(Constants.SENDING)){
                    //message is being sent
                    viewHolder.sendingText.setVisibility(View.VISIBLE);
                    viewHolder.mSentImage.setVisibility(View.INVISIBLE);
                }else if(code.equals(Constants.SENT)){
                    //message is sent
                    viewHolder.mSentImage.setVisibility(View.VISIBLE);
                    viewHolder.sendingText.setVisibility(View.GONE);

                }
            }
        },new IntentFilter(message.getMessageId()));
    }

    @Override
    public int getItemCount() {
        return allMessages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        CardView  SentMessageCard;
        TextView sentMessageTextView;
        ImageView SentMessageImage;
        ImageView mSentImage;
        TextView sendingText;

        LinearLayout notSentLayout;
        CardView ReceivedMessageCard;
        TextView receivedMessageTextView;
        ImageView ReceivedMessageImage;

        ViewHolder(View itemView) {
            super(itemView);
            SentMessageCard = itemView.findViewById(R.id.SentMessageCard);
            sentMessageTextView = itemView.findViewById(R.id.sentMessageTextView);
            SentMessageImage = itemView.findViewById(R.id.SentMessageImage);
            mSentImage = itemView.findViewById(R.id.mSentImage);

            sendingText = itemView.findViewById(R.id.sendingText);
            notSentLayout = itemView.findViewById(R.id.notSentLayout);
            ReceivedMessageCard = itemView.findViewById(R.id.ReceivedMessageCard);
            receivedMessageTextView = itemView.findViewById(R.id.receivedMessageTextView);
            ReceivedMessageImage = itemView.findViewById(R.id.ReceivedMessageImage);

        }
    }
}
