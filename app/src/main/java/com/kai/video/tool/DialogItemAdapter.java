package com.kai.video.tool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jeffmony.downloader.model.VideoTaskItem;
import com.kai.video.R;
import com.kai.video.bean.DanmuTool;
import com.kai.video.view.CustomDialog;
import com.kai.video.view.ScrollTextView;

import java.util.ArrayList;
import java.util.List;


public class DialogItemAdapter extends RecyclerView.Adapter<DialogItemAdapter.ViewHolder> {
    private List<String> items = new ArrayList<>();
    private List<Object> objects = new ArrayList<>();
    private OnItemClickListener onItemClickListener = null;
    int current = 0;
    public static class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout itemLayout;
        ScrollTextView item;
        boolean selected = false;
        public ViewHolder(View view){
            super(view);
            item = view.findViewById(R.id.item);
            itemLayout = view.findViewById(R.id.item_layout);
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSelected() {
            return selected;
        }
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public void addItem(String name, Object o){
        items.add(name);
        if (objects != null)
            objects.add(o);
        notifyItemInserted(getItemCount() - 1);
    }
    public void removeItem(String name){
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            if (item.equals(name)){
                items.remove(i);
                objects.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }
    private CustomDialog dialog;
    public DialogItemAdapter(List<String> items, List<Object> objects, int select, CustomDialog dialog){
        this.items = items;
        this.objects = objects;
        current = select;
        this.dialog = dialog;
    }

    public List<String> getItems() {
        return items;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current == this.current)
            return;
        notifyItemChanged(this.current);
        this.current = current;
        notifyItemChanged(this.current);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_dialog, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    holder.itemLayout.requestFocus();
            }
        });
        return holder;
    }
    boolean scrollToPosition = false;
    private OnLoadListener onLoadingListener;

    public void setOnLoadingListener(OnLoadListener onLoadingListener) {
        this.onLoadingListener = onLoadingListener;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (onLoadingListener != null && holder.isSelected() && !scrollToPosition){
            scrollToPosition = true;
            onLoadingListener.onFinish(holder);
        }
    }

    public ViewHolder getCurrentHolder() {
        return currentHolder;
    }

    private ViewHolder currentHolder = null;
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = items.get(position);
        holder.item.setText(name);
        //如果发现容器位置符合
        if (position == 2)
            holder.setSelected(true);
        if (position == current){
            currentHolder = holder;
            holder.itemLayout.setBackgroundResource(R.drawable.dialog_item_background_selected);
        }else {
            holder.itemLayout.setBackgroundResource(R.drawable.dialog_item_background);
        }
        holder.itemLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    holder.item.startScroll();
                }else
                    holder.item.stopScroll();
            }
        });
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current != -1){
                    currentHolder = holder;
                    int currentOld = current;
                    current = position;
                    notifyItemChanged(currentOld);
                    notifyItemChanged(current);
                    currentHolder.itemView.requestFocus();
                }

                if (objects != null && objects.size() > position)
                    onItemClickListener.onClick(items.get(position), objects.get(position), position, dialog);
                else
                    onItemClickListener.onClick(items.get(position), null, position, dialog);
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
    public interface OnLoadListener{
        void onFinish(ViewHolder holder);
    }
    public interface OnItemClickListener{
        void onClick(String item, Object o, int position, CustomDialog dialog);
    }

}
