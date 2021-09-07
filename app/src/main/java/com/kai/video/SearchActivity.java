package com.kai.video;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import com.kai.video.bean.BaseActivity;
import com.kai.video.bean.DeviceManager;
import com.kai.video.tool.LogUtil;
import com.kai.video.tool.SimpleConductor;
import com.kai.video.tool.VideoJtemAdapter;

import org.json.JSONArray;

public class SearchActivity extends BaseActivity implements View.OnFocusChangeListener {
    SimpleConductor simpleConductor;
    RecyclerView recyclerView;
    SearchView searchView;
    VideoJtemAdapter adapter;
    View progressBar;
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        LogUtil.d("TF", v.getId() + "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        if (DeviceManager.getDevice() == DeviceManager.DEVICE_TV){
            setContentView(R.layout.activity_search);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else if (DeviceManager.getDevice() == DeviceManager.DEVICE_PAD){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().setStatusBarColor(getColor(R.color.colorPrimary));
            }
            setContentView(R.layout.activity_search);
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().setStatusBarColor(getColor(R.color.colorPrimary));
            }
            setContentView(R.layout.activity_search_none_phone);
        }
        simpleConductor = new SimpleConductor();
        Intent intent = getIntent();
        progressBar = (View) findViewById(R.id.progress);
        searchView = (SearchView) findViewById(R.id.search_bar);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && DeviceManager.isTv()) {
                    searchView.clearFocus();
                    searchView.onActionViewCollapsed();
                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {  // 点击软件盘搜索按钮会弹出 吐司
                InputMethodManager manager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (manager != null)
                    manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                adapter.setItems(new JSONArray());
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.VISIBLE);
                simpleConductor.search(s, new SimpleConductor.OnSearchListener() {
                    @Override
                    public void onSearch(JSONArray array) {
                        SearchActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.INVISIBLE);
                                adapter.setItems(array);
                                adapter.notifyDataSetChanged();
                            }
                        });

                    }
                });
                return true;
            }
            // 搜索框文本改变事件
            @Override
            public boolean onQueryTextChange(String s) {
                // 文本内容是空就让 recyclerView 填充全部数据 // 可以是其他容器 如listView

                return false;
            }
        });
        recyclerView = (RecyclerView)findViewById(R.id.video_list);
        simpleConductor.search(intent.getStringExtra("wd"), new SimpleConductor.OnSearchListener() {
            @Override
            public void onSearch(JSONArray array) {
                SearchActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(isScreenOriatationPortrait(SearchActivity.this)?4:6, StaggeredGridLayoutManager.VERTICAL);
                        recyclerView.setLayoutManager(layoutManager);
                        LogUtil.d("TAG", array.toString());
                        adapter = new VideoJtemAdapter(array);
                        recyclerView.setAdapter(adapter);
                        progressBar.setVisibility(View.GONE);
                    }
                });

            }
        });
    }
    public static boolean isScreenOriatationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
}
