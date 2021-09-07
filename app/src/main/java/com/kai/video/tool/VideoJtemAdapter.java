package com.kai.video.tool;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.kai.video.InfoActivity;
import com.kai.video.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;


public class VideoJtemAdapter extends RecyclerView.Adapter<VideoJtemAdapter.ViewHolder> {
    private JSONArray items = new JSONArray();
    private ViewHolder firstHolder;
    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView posterView;
        TextView videoTitle;
        Context context;
        RelativeLayout relativeLayout;
        ImageView logoView;
        ImageView logo;
        public ViewHolder(View view){
            super(view);
            relativeLayout = (RelativeLayout)view.findViewById(R.id.main);
            posterView = (ImageView)view.findViewById(R.id.poster);
            videoTitle = (TextView)view.findViewById(R.id.title);
            logo = (ImageView)view.findViewById(R.id.logo);
            logoView = (ImageView) view.findViewById(R.id.logo_view);
        }
        public void addContext(Context context){
            this.context = context;
        }

        public Context getContext() {
            return context;
        }
    }

    public void setItems(JSONArray items) {
        this.items = items;
    }

    public VideoJtemAdapter(JSONArray items){
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.videopic, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.addContext(parent.getContext());
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    holder.relativeLayout.requestFocus();
                }
            }
        });
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int position = holder.getAdapterPosition();
                    JSONObject element = items.getJSONObject(position);
                    String url = element.getString("href");
                    Intent intent = new Intent(parent.getContext(), InfoActivity.class);
                    intent.putExtra("name", element.getString("title"));
                    intent.putExtra("url", url);
                    parent.getContext().startActivity(intent);
                }catch (Exception e){
                    e.printStackTrace();
                }


            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject element = items.getJSONObject(position);
            if (position == 0)
                firstHolder = holder;
            String url = element.getString("href");
            if (url.contains("v.qq.com"))
                Glide.with(holder.itemView).load(R.drawable.tencent).into(holder.logo);
            else if (url.contains("www.iqiyi.com"))
                Glide.with(holder.itemView).load(R.drawable.iqiyi).into(holder.logo);
            else if (url.contains("www.bilibili.com"))
                Glide.with(holder.itemView).load(R.drawable.bilibili).into(holder.logo);
            else if (url.contains("www.mgtv.com"))
                Glide.with(holder.itemView).load(R.drawable.mgtv).into(holder.logo);
            Glide.with(holder.getContext())
                    .asDrawable()
                    .fitCenter()
                    .load(element.getString("pic")).placeholder(R.drawable.loading)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerInside()
                    .into(holder.posterView);
            holder.videoTitle.setText(Jsoup.parse(element.getString("title").replaceAll("\\[.*\\]","")).text());
        }catch (Exception e){

        }


    }

    @Override
    public int getItemCount() {
        return items.length();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public RecyclerView.ViewHolder getFirstHolder() {
        return firstHolder;
    }
}
