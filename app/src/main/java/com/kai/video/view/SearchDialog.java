package com.kai.video.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.leanback.widget.SearchEditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kai.video.MainActivity;
import com.kai.video.R;
import com.kai.video.SearchActivity;
import com.kai.video.bean.DeviceManager;
import com.kai.video.tool.DialogItemAdapter;

import java.util.List;

public class SearchDialog extends AlertDialog {
    private Context mContext;
    private SearchEditText searchEditText;
    public SearchDialog(Context context) {
        super(context, R.style.CustomDialog);
        mContext = context;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_search);
        setCanceledOnTouchOutside(true);
        searchEditText = findViewById(R.id.editer);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        searchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if ((actionId == 0 || actionId == 3) && event != null) {
                    //点击搜索要做的操作
                    Intent intent = new Intent(mContext, SearchActivity.class);
                    intent.putExtra("wd",searchEditText.getText().toString());
                    mContext.startActivity(intent);
                }
                return false;
            }
        });
        //初始化界面控件

    }

}
