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
import com.bry.raia.Models.County;
import com.bry.raia.Models.Language;
import com.bry.raia.R;

import java.util.List;

public class UserSettingsActivityCountyItemAdapter extends RecyclerView.Adapter<UserSettingsActivityCountyItemAdapter.ViewHolder> {
    private List<County> mCounties;
    private Activity mActivity;

    public UserSettingsActivityCountyItemAdapter(List<County> counties, Activity acc){
        this.mActivity = acc;
        this.mCounties = counties;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View recipeView = inflater.inflate(R.layout.user_settings_language_item, viewGroup, false);
        return new UserSettingsActivityCountyItemAdapter.ViewHolder(recipeView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final County County = mCounties.get(i);
        viewHolder.countyName.setText((i+1)+". "+County.getName());

        viewHolder.countyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Constants.SELECTED_COUNTY);
                intent.putExtra("county",County.getName());
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCounties.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView countyName;

        ViewHolder(View itemView) {
            super(itemView);
            countyName = itemView.findViewById(R.id.languageName);
        }
    }
}
