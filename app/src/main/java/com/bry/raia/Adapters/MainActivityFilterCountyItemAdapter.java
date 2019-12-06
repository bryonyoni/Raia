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
import com.bry.raia.R;

import java.util.List;

public class MainActivityFilterCountyItemAdapter extends RecyclerView.Adapter<MainActivityFilterCountyItemAdapter.ViewHolder>{
    private List<County> mCounties;
    private Activity mActivity;

    public MainActivityFilterCountyItemAdapter(List<County> counties, Activity acc){
        this.mActivity = acc;
        this.mCounties = counties;
    }

    @NonNull
    @Override
    public MainActivityFilterCountyItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View recipeView = inflater.inflate(R.layout.county_item_view, viewGroup, false);
        return new MainActivityFilterCountyItemAdapter.ViewHolder(recipeView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MainActivityFilterCountyItemAdapter.ViewHolder viewHolder, int i){
        final County county = mCounties.get(i);
        viewHolder.countyName.setText((i+1)+". "+county.getName());

        viewHolder.countyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Constants.SELECTED_COUNTY);
                intent.putExtra("county",county.getName());
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);
            }
        });
    }

    @Override
    public int getItemCount(){
        return mCounties.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView countyName;

        ViewHolder(View itemView) {
            super(itemView);
            countyName = itemView.findViewById(R.id.countyName);
        }
    }

}
