package com.kai.video.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.video.R;
import com.kai.video.bean.DeviceManager;
import com.kai.video.tool.DialogItemAdapter;
import java.util.ArrayList;
import java.util.List;

public class CustomDialog extends AlertDialog {
    private Context mContext;
    private TextView title;
    private TextView message;
    private RecyclerView recyclerView;
    private String mTitle = "";
    private String mMessage = "";
    private DialogItemAdapter adapter = null;
    private LinearLayoutManager manager = null;
    public CustomDialog(Context context) {
        super(context, R.style.CustomDialog);
        mContext = context;
    }

    @Override
    public void show() {
        try {
            if (!DeviceManager.isTv()){
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                    getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                }else{
                    getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.show();
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (adapter.getCurrent() < 0 || adapter.getCurrent() >= adapter.getItems().size())
                return;
            manager.findViewByPosition(adapter.getCurrent()).findViewById(R.id.item_layout).requestFocus();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog);
        setCanceledOnTouchOutside(true);
        //初始化界面控件
        title = findViewById(R.id.title);
        message = findViewById(R.id.message);
        recyclerView = findViewById(R.id.list);
        title.setText(mTitle);
        message.setText(mMessage);
        if (mMessage.isEmpty()){
            message.setVisibility(View.GONE);
        }
        recyclerView.setLayoutManager(manager);
        adapter.setOnLoadingListener(new DialogItemAdapter.OnLoadListener() {
            @Override
            public void onFinish(DialogItemAdapter.ViewHolder holder) {
                Message message = new Message();
                manager.scrollToPositionWithOffset(adapter.getCurrent(), 0);
                mHandler.sendMessageDelayed(message, 100);
            }
        });
        recyclerView.setAdapter(adapter);
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mDismissListener != null){
                    mDismissListener.onDismiss(dialog);
                }
            }
        });
    }
    Handler handler = new Handler();
    Runnable currentRunnable = new Runnable() {
        @Override
        public void run() {
            manager.findViewByPosition(adapter.getCurrent()).findViewById(R.id.item_layout).requestFocus();
        }
    };
    public void resume(){
        setCurrent();
        show();
    }
    public void setCurrent(int i){
        adapter.setCurrent(i);
        setCurrent();
    }
    public void setCurrent(){
        manager.scrollToPositionWithOffset(adapter.getCurrent(), 0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(currentRunnable, 200);
            }
        }).start();

        //adapter.setCurrent(current);
        //manager.scrollToPosition(current);
    }
    public void addItem(String name, Object o){
        adapter.addItem(name, o);
    }
    public void removeItem(String name){
        adapter.removeItem(name);
    }
    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setMessage(String mMessage) {
        this.mMessage = mMessage;
    }

    public void setOnItemClickListener(DialogItemAdapter.OnItemClickListener itemClickListener){
        adapter.setOnItemClickListener(itemClickListener);
    }
    public void setList(List<String> names, List<Object> objects, int select){
        manager = new LinearLayoutManager(mContext);
        adapter = new DialogItemAdapter(names, objects, select, CustomDialog.this);

    }
    private OnDismissListener mDismissListener = null;

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        mDismissListener = onDismissListener;
    }

    public static class Builder{
        private CustomDialog dialog;
        public Builder(Context context){
            dialog = new CustomDialog(context);
        }
        public Builder setTitle(String title){
            dialog.setTitle(title);
            return this;
        }
        public Builder setMessage(String message){
            dialog.setMessage(message);
            return this;
        }
        public Builder setList(List<String> names, List<Object> objects, int select){
            dialog.setList(names, objects, select);
            return this;
        }
        public Builder setOnItemClickListener(DialogItemAdapter.OnItemClickListener onItemClickListener){
            dialog.setOnItemClickListener(onItemClickListener);
            return this;
        }
        public Builder setOnDismissListener(OnDismissListener onDismissListener){
            dialog.setOnDismissListener(onDismissListener);
            return this;
        }
        public CustomDialog create(){
            dialog.create();
            return dialog;
        }
        public void show(){
            dialog.show();
        }
    }
    public interface OnDismissListener{
        void onDismiss(DialogInterface dialog);
    }
}
