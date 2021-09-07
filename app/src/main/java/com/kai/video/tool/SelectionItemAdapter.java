package com.kai.video.tool;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.video.R;

import org.json.JSONArray;
import org.json.JSONObject;


public class SelectionItemAdapter extends RecyclerView.Adapter<SelectionItemAdapter.ViewHolder> {
    private JSONArray array = new JSONArray();
    private onListener onListener = null;
    int current = 0;
    static class ViewHolder extends RecyclerView.ViewHolder{
        Button title;
        TextView vip;
        Context context;
        RelativeLayout relativeLayout;
        public ViewHolder(View view){
            super(view);
            vip = (TextView)view.findViewById(R.id.vip);
            title = (Button) view.findViewById(R.id.title);
            relativeLayout = (RelativeLayout)view.findViewById(R.id.main);
        }
        public void addContext(Context context){
            this.context = context;
        }

        public Context getContext() {
            return context;
        }
    }
    public SelectionItemAdapter(JSONArray array){

        this.array = array;
    }

    public JSONArray getArray() {
        return array;
    }

    public int getCurrent() {
        return current;
    }

    public void setOnListener(SelectionItemAdapter.onListener onListener) {
        this.onListener = onListener;
        for(int i = 0; i < array.length(); i++){
            try {
                JSONObject obj = array.getJSONObject(i);
                if (obj.getBoolean("current")) {
                    current = i;
                    LogUtil.d("TAG", current + "");
                    onListener.onEnsure(current);
                    break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    public void change(int position){
        try {
            if (array.length() == 0)
                return;
            JSONObject currentObj = array.getJSONObject(current);
            currentObj.put("current", false);
            JSONObject newObj = array.getJSONObject(position);
            newObj.put("current", true);
            array.put(current, currentObj);
            array.put(position, newObj);
            notifyItemChanged(current);
            notifyItemChanged(position);
            current = position;
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selection_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    holder.title.requestFocus();
            }
        });
        holder.addContext(parent.getContext());
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int position =holder.getAdapterPosition();
                    change(position);
                    onListener.onClick(position);
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
            JSONObject object = array.getJSONObject(position);
            holder.title.setBackgroundResource(R.drawable.button_item_selector);
            if (object.getBoolean("current")){
                holder.title.setBackgroundResource(R.drawable.selection_item_selector_active);
            }
            holder.title.setText(object.getString("title"));
            holder.vip.setText("");
            holder.vip.setBackground(null);
            switch (object.getInt("type")){
                case 0:break;
                case 1:holder.vip.setText("预告");
                    holder.vip.setBackgroundResource(R.drawable.yugao);
                    break;
                case 2:holder.vip.setText("会员");
                    holder.vip.setBackgroundResource(R.drawable.vip);
                    break;
                case 3:holder.vip.setText("点播");
                    holder.vip.setBackgroundResource(R.drawable.dianbo);
                    break;

            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    public int getItemCount() {
        return array.length();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
    public interface onListener{
        void onEnsure(int currentPosition);
        void onClick(int position);
    }

}
