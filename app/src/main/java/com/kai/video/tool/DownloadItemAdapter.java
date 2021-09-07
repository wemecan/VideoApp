package com.kai.video.tool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jeffmony.downloader.model.VideoTaskItem;
import com.kai.video.InfoActivity;
import com.kai.video.MainActivity;
import com.kai.video.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class DownloadItemAdapter extends RecyclerView.Adapter<DownloadItemAdapter.ViewHolder> {
    private Context context;
    private List<VideoTaskItem> items = new ArrayList<>();
    private OnItemClickListener onItemClickListener = null;
    int current = 0;

    public void setItems(List<VideoTaskItem> items) {
        this.items = items;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        float percentage;
        String mark = "";
        TextView speed;
        TextView title;
        TextView state;
        ProgressBar percent;
        RelativeLayout container;
        public ViewHolder(View view){
            super(view);
            speed = (TextView) view.findViewById(R.id.speed);
            title = (TextView) view.findViewById(R.id.title);
            state = (TextView) view.findViewById(R.id.state);
            percent = (ProgressBar) view.findViewById(R.id.percent);
            container = (RelativeLayout) view.findViewById(R.id.list_container);
        }

        public void setMark(String mark) {
            this.mark = mark;
        }

        public String getMark() {
            return mark;
        }

        public float getPercentage() {
            return percentage;
        }

        public void setPercentage(float percentage) {
            this.percentage = percentage;
        }

    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    public DownloadItemAdapter(Context context, List<VideoTaskItem> items){
        this.context = context;
        this.items = items;
    }

    public List<VideoTaskItem> getItems() {
        return items;
    }

    public int getCurrent() {
        return current;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_download, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    holder.container.requestFocus();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoTaskItem item = items.get(position);

        //如果发现容器位置符合
        if (holder.getMark().equals(item.getTitle())){
            //如果进度没有发生变化甚至倒退，那么就忽略当前更新
            if (item.getPercent() - holder.getPercentage() < 0.85 && item.getPercent() < 100 && item.getTaskState() == 3)
                return;
        }
        holder.speed.setText("");
        holder.percent.setProgress((int) item.getPercent());
        holder.title.setText(item.getTitle().split("\\|")[0]);
        holder.setMark(item.getTitle());
        holder.setPercentage(item.getPercent());
        switch (item.getTaskState()){
            case 0: holder.state.setText("获取信息中");break;
            case 1: holder.state.setText("下载准备中");break;
            case -1: holder.state.setText("缓存排队中");break;
            case 2:holder.state.setText("开始缓存");break;
            case 3:
                if (item.getPercent() == 100 && item.isHlsType())
                    holder.state.setText("视频转码中");
                else
                    holder.state.setText(item.getPercentString());
                holder.speed.setText(item.getSpeedString());
                holder.setPercentage((int) item.getPercent());
                break;
            case 4:holder.state.setText("边下边播");
                holder.speed.setText(item.getSpeedString());
                break;
            case 5:
                if (item.isHlsType()){
                    holder.state.setText("视频转码中,请稍候");
                }else
                    holder.state.setText("缓存结束");break;
            case 6:holder.state.setText("缓存出错");break;
            case 7:
                holder.state.setText("缓存暂停");break;
            case 8:holder.state.setText("空间不足");break;
        }
        if (item.getTaskState() == 5 && item.getFilePath().endsWith("m3u8")){
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setTitle("选项")
                            .setItems(new String[]{"继续缓存"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which){
                                        case 0: onItemClickListener.onClick(item, position, ACTION_RESUME);break;
                                    }
                                }
                            }).create().show();
                }
            });
        }else if (item.getTaskState() == 5 && !item.getFilePath().endsWith("m3u8")){
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setTitle("选项")
                            .setItems(new String[]{"删除缓存", "立即播放"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which){
                                        case 0: onItemClickListener.onClick(item, position, ACTION_DELETE);break;
                                        case 1: onItemClickListener.onClick(item, position, ACTION_PLAY);break;
                                    }
                                }
                            }).create().show();
                }
            });
        }
        else if (item.getTaskState() == 4){
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setTitle("选项")
                            .setItems(new String[]{"暂停缓存", "删除缓存", "边下边播"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which){
                                        case 0: onItemClickListener.onClick(item, position, ACTION_PUASE);break;
                                        case 1: onItemClickListener.onClick(item, position, ACTION_DELETE);break;
                                        case 2: onItemClickListener.onClick(item, position, ACTION_PLAY_WITH_DOWNLOADING);break;
                                    }
                                }
                            }).create().show();
                }
            });
        }else if (item.getTaskState() == 3){
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setTitle("选项")
                            .setItems(new String[]{"暂停缓存", "删除缓存"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which){
                                        case 0: onItemClickListener.onClick(item, position, ACTION_PUASE);break;
                                        case 1: onItemClickListener.onClick(item, position, ACTION_DELETE);break;
                                    }
                                }
                            }).create().show();
                }
            });
        }
        else {
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setTitle("选项")
                            .setItems(new String[]{"继续缓存", "删除缓存"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which){
                                        case 0: onItemClickListener.onClick(item, position, ACTION_RESUME);break;
                                        case 1: onItemClickListener.onClick(item, position, ACTION_DELETE);break;
                                    }
                                }
                            }).create().show();
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
    public static int ACTION_RESUME = 0;
    public static int ACTION_PUASE = 1;
    public static int ACTION_PLAY = 2;
    public static int ACTION_PLAY_WITH_DOWNLOADING = 3;
    public static int ACTION_DELETE = 4;
    public interface OnItemClickListener{
        void onClick(VideoTaskItem item, int position, int action);
    }
    public void notifyDataChanged(VideoTaskItem item) {
        for (int index = 0; index < getItemCount(); index++) {
            if (items.get(index).getTitle().equals(item.getTitle())) {
                items.set(index, item);
                notifyItemChanged(index);
            }
        }
    }
}
