package com.kai.video;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.jeffmony.downloader.VideoDownloadManager;
import com.jeffmony.downloader.listener.IDownloadInfosCallback;
import com.jeffmony.downloader.model.VideoTaskItem;
import com.kai.video.bean.BaseActivity;
import com.kai.video.bean.DeliverVideoTaskItem;
import com.kai.video.bean.DeviceManager;
import com.kai.video.tool.DownloadItemAdapter;
import com.kai.video.tool.LogUtil;
import com.kai.video.tool.SPUtils;
import com.kai.video.view.TvTabLayout;

import java.util.List;

public class DownloadActivity extends BaseActivity {
    private IDownloadInfosCallback callback;
    private TvTabLayout tabs;
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private LinearLayoutManager layoutManager;
    private LocalReceiver receiver;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private DownloadItemAdapter adapter;
    private VideoDownloadManager downloadManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        if (DeviceManager.getDevice() == DeviceManager.DEVICE_TV){

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().setStatusBarColor(getColor(R.color.colorPrimary));
            }
        }
        setContentView(R.layout.activity_download);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        receiver = new LocalReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.kai.video.LOCAL_BROADCAST1");
        localBroadcastManager.registerReceiver(receiver, intentFilter);
        recyclerView = findViewById(R.id.video_list);
        progressBar = findViewById(R.id.progress);
        downloadManager = VideoDownloadManager.getInstance();
        callback = new IDownloadInfosCallback() {
            @Override
            public void onDownloadInfos(List<VideoTaskItem> items) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (adapter == null){
                                progressBar.setVisibility(View.INVISIBLE);
                                layoutManager = new LinearLayoutManager(DownloadActivity.this);
                                recyclerView.setLayoutManager(layoutManager);
                                adapter = new DownloadItemAdapter(DownloadActivity.this, items);
                                ((SimpleItemAnimator)recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
                                adapter.setOnItemClickListener(new DownloadItemAdapter.OnItemClickListener() {
                                    @Override
                                    public void onClick(VideoTaskItem item, int position, int action) {
                                        if (action == DownloadItemAdapter.ACTION_RESUME) {
                                            downloadManager.resumeDownload(item.getUrl());

                                        } else if (action == DownloadItemAdapter.ACTION_DELETE) {
                                            if (position == adapter.getItemCount())
                                                return;
                                            adapter.getItems().remove(position);
                                            adapter.notifyItemRemoved(position);
                                            downloadManager.deleteVideoTask(item.getUrl(), true);
                                        } else if (action == DownloadItemAdapter.ACTION_PLAY) {
                                            Intent intent = new Intent(DownloadActivity.this, InfoActivity.class);
                                            intent.putExtra("name", item.getTitle().split("\\|")[0]);
                                            intent.putExtra("url", item.getTitle().split("\\|")[1]);
                                            intent.putExtra("direct", true);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        } else if (action == DownloadItemAdapter.ACTION_PLAY_WITH_DOWNLOADING) {
                                            downloadManager.pauseDownloadTask(item.getUrl());
                                            Intent intent = new Intent(DownloadActivity.this, InfoActivity.class);
                                            intent.putExtra("name", item.getTitle().split("\\|")[0]);
                                            intent.putExtra("url", item.getTitle().split("\\|")[1]);
                                            intent.putExtra("direct", true);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        } else  if (action == DownloadItemAdapter.ACTION_PUASE){
                                            downloadManager.pauseDownloadTask(item.getUrl());
                                        }
                                    }
                                });
                                recyclerView.setAdapter(adapter);
                            }else {
                                adapter.setItems(items);
                                adapter.notifyDataSetChanged();
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                });



            }

        };
        downloadManager.fetchDownloadItems(callback);
        //downloadManager.fetchDownloadItems();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onDestroy() {
        downloadManager.removeDownloadInfosCallback(callback);
        localBroadcastManager.unregisterReceiver(receiver);
        super.onDestroy();
    }
    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (adapter!=null){
                DeliverVideoTaskItem deliverVideoTaskItem = (DeliverVideoTaskItem) intent.getSerializableExtra("item");
                adapter.notifyDataChanged(DeliverVideoTaskItem.unpack(deliverVideoTaskItem));
            }
        }
    }
}
