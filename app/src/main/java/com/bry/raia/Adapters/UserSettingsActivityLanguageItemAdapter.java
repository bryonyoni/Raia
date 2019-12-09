package com.bry.raia.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bry.raia.Constants;
import com.bry.raia.Models.Language;
import com.bry.raia.R;

import java.util.ArrayList;
import java.util.List;

public class UserSettingsActivityLanguageItemAdapter extends RecyclerView.Adapter<UserSettingsActivityLanguageItemAdapter.ViewHolder> {
    private List<Language> mLanguages = new ArrayList<>();
    private Activity mActivity;

    public UserSettingsActivityLanguageItemAdapter(List<Language> Languages, Activity acc){
        this.mActivity = acc;
        this.mLanguages = Languages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View recipeView = inflater.inflate(R.layout.user_settings_language_item, viewGroup, false);
        return new UserSettingsActivityLanguageItemAdapter.ViewHolder(recipeView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Language language = mLanguages.get(i);
        viewHolder.languageName.setText((i+1)+". "+language.getName());

        viewHolder.languageName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Constants.SELECTED_LANGUAGE);
                intent.putExtra("language",language.getName());
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLanguages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView languageName;

        ViewHolder(View itemView) {
            super(itemView);
            languageName = itemView.findViewById(R.id.languageName);
        }
    }
}
