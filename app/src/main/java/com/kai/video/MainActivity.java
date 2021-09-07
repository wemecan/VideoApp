package com.kai.video;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.jeffmony.downloader.VideoDownloadConfig;
import com.jeffmony.downloader.VideoDownloadManager;
import com.jeffmony.downloader.common.DownloadConstants;
import com.jeffmony.downloader.listener.DownloadListener;
import com.jeffmony.downloader.model.VideoTaskItem;
import com.kai.video.bean.Api;
import com.kai.video.bean.BaseActivity;
import com.kai.video.bean.DeliverVideoTaskItem;
import com.kai.video.bean.DeviceManager;
import com.kai.video.bean.History;
import com.kai.video.bean.LoginTool;
import com.kai.video.view.TabViews;
import com.kai.video.tool.ActivityCollector;
import com.kai.video.tool.DialogItemAdapter;
import com.kai.video.tool.LogUtil;
import com.kai.video.tool.SPUtils;
import com.kai.video.ui.main.PlaceholderFragment;
import com.kai.video.ui.main.SectionsPagerAdapter;
import com.kai.video.view.CustomDialog;
import com.kai.video.view.TvTabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnFocusChangeListener{
    private int side = 0;
    private TabViews tabViews;
    private int position = 0;
    private TvTabLayout tabs;
    ViewPager viewPager;
    private LocalReceiver localReceiver;
    private IntentFilter intentFilter;
    private LocalBroadcastManager localBroadcastManager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private String createNotificationChannel(String channelID, String channelNAME, int level) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channelID, channelNAME, level);
            manager.createNotificationChannel(channel);
            return channelID;
        } else {
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoDownloadManager.getInstance().pauseAllDownloadTasks();
    }

    private int count = 0;
    public static int ACTION_UPDATE = 0;
    private DownloadListener mListener = new DownloadListener() {

        @Override
        public void onDownloadDefault(VideoTaskItem item) {}

        @Override
        public void onDownloadPending(VideoTaskItem item) {}

        @Override
        public void onDownloadPrepare(VideoTaskItem item) {}

        @Override
        public void onDownloadStart(VideoTaskItem item) {
            String channelId = createNotificationChannel("video_download", "视频下载通知", NotificationManager.IMPORTANCE_LOW);
            NotificationCompat.Builder notification = new NotificationCompat.Builder(MainActivity.this, channelId)
                    .setContentTitle(item.getTitle().split("\\|")[0])
                    .setContentText("开始下载")
                    //.setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setProgress(100, 0, false)
                    .setAutoCancel(true);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
            notificationManager.notify(count, notification.build());
            item.setGroupName(String.valueOf(count));
            count++;
            //nBuilder.setContentIntent(pIntent);
            //nBuilder.addAction(android.R.drawable.stat_notify_call_mute, "go to", pIntent);

        }

        @Override
        public void onDownloadProgress(VideoTaskItem item) {
            if (localBroadcastManager != null){
                Intent intent = new Intent("com.kai.video.LOCAL_BROADCAST1");
                intent.putExtra("item", DeliverVideoTaskItem.pack(item));
                localBroadcastManager.sendBroadcast(intent);
            }
            Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            String channelId = createNotificationChannel("video_download", "视频下载通知", NotificationManager.IMPORTANCE_LOW);
            NotificationCompat.Builder notification = new NotificationCompat.Builder(MainActivity.this, channelId)
                    .setContentTitle(item.getTitle().split("\\|")[0])
                    .setContentText("正在下载：" + item.getPercentString())
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setProgress(100, (int) item.getPercent(), false)
                    .setAutoCancel(true);
            if (item.getPercent() > 99){
                notification.setContentText("下载完成，正在转码");
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
            notificationManager.notify(Integer.parseInt(item.getGroupName()), notification.build());
        }

        @Override
        public void onDownloadSpeed(VideoTaskItem item) {

        }

        @Override
        public void onDownloadPause(VideoTaskItem item) {
            if (localBroadcastManager != null){
                Intent intent = new Intent("com.kai.video.LOCAL_BROADCAST1");
                intent.putExtra("action", ACTION_UPDATE);
                intent.putExtra("item", DeliverVideoTaskItem.pack(item));
                localBroadcastManager.sendBroadcast(intent);
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
            notificationManager.cancel(Integer.parseInt(item.getGroupName()));
        }

        @Override
        public void onDownloadError(VideoTaskItem item) {
            if (localBroadcastManager != null){
                Intent intent = new Intent("com.kai.video.LOCAL_BROADCAST1");
                intent.putExtra("action", ACTION_UPDATE);
                intent.putExtra("item", DeliverVideoTaskItem.pack(item));
                localBroadcastManager.sendBroadcast(intent);
            }
        }

        @Override
        public void onDownloadSuccess(VideoTaskItem item) {
            if (localBroadcastManager != null){
                Intent intent = new Intent("com.kai.video.LOCAL_BROADCAST1");
                intent.putExtra("action", ACTION_UPDATE);
                intent.putExtra("item", DeliverVideoTaskItem.pack(item));
                localBroadcastManager.sendBroadcast(intent);
            }
            Intent intent = new Intent(MainActivity.this, InfoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("name", item.getTitle().split("\\|")[0]);
            intent.putExtra("url", item.getTitle().split("\\|")[1]);
            PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            String channelId = createNotificationChannel("video_download", "视频下载通知", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationCompat.Builder notification = new NotificationCompat.Builder(MainActivity.this, channelId)
                    .setContentTitle(item.getTitle().split("\\|")[0])
                    .setContentText("下载完成")
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setProgress(100, (int) item.getPercent(), false)
                    .setAutoCancel(true);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
            notificationManager.notify(Integer.parseInt(item.getGroupName()), notification.build());
        }
    };

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        //super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!isTaskRoot())
        {
            final Intent intent = getIntent();
            final String intentAction = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent
                    .ACTION_MAIN))
            {
                finish();
                return;
            }
        }
        super.onCreate(savedInstanceState);

        VideoDownloadManager.getInstance().setGlobalDownloadListener(mListener);
        VideoDownloadManager.getInstance().setIgnoreAllCertErrors(true);
        File file =  new File(getExternalFilesDir("video").getAbsolutePath());
        if (!file.exists()) {
            file.mkdir();
        }
        VideoDownloadConfig config = new VideoDownloadManager.Build(this)
                .setCacheRoot(file.getAbsolutePath())
                .setTimeOut(DownloadConstants.READ_TIMEOUT, DownloadConstants.CONN_TIMEOUT)
                .setConcurrentCount(DownloadConstants.CONCURRENT)
                .setIgnoreCertErrors(true)
                .setShouldM3U8Merged(true)
                .buildConfig();
        VideoDownloadManager.getInstance().initConfig(config);
        Api.loadApis();
        DeviceManager.init(MainActivity.this);
        setTheme(R.style.AppTheme);
        if (DeviceManager.getDevice() == DeviceManager.DEVICE_TV){
            requestWindowFeature(Window.FEATURE_NO_TITLE);

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_main);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            LogUtil.d("TAG", "Running on a TV Device");
        }else if (DeviceManager.getDevice() == DeviceManager.DEVICE_PAD){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                MainActivity.this.getWindow().setStatusBarColor(MainActivity.this.getColor(R.color.colorPrimary));
            }
            setContentView(R.layout.activity_main);

        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                MainActivity.this.getWindow().setStatusBarColor(MainActivity.this.getColor(R.color.colorPrimary));
            }
            setContentView(R.layout.activity_main_none_phone);
        }
        MyPlayerManager.setKernelDefault(DeviceManager.isTv());
        String permissions[] = new String[]{
                Permission.SYSTEM_ALERT_WINDOW,
                Permission.WRITE_EXTERNAL_STORAGE,
                Permission.NOTIFICATION_SERVICE
        };
        if (DeviceManager.isTv())
            permissions = new String[]{
                    Permission.WRITE_EXTERNAL_STORAGE
            };
        boolean hasPermission = XXPermissions.isGranted(this, permissions);
        if (!hasPermission) {
            final String[] ps = permissions;
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("权限请求")
                    .setMessage("为了APP的更好体验，请授予必要权限")
                    .setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton("立即授予", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            XXPermissions.setScopedStorage(true);
                            XXPermissions.with(MainActivity.this)
                                    .permission(ps)
                                    .request(new OnPermissionCallback() {

                                        @Override
                                        public void onGranted(List<String> permissions, boolean all) {
                                            if (all) {
                                                //toast("获取录音和日历权限成功");
                                            } else {
                                                //toast("获取部分权限成功，但部分权限未正常授予");
                                            }
                                        }

                                        @Override
                                        public void onDenied(List<String> permissions, boolean never) {
                                            if (never) {
                                                //toast("被永久拒绝授权，请手动授予录音和日历权限");
                                                // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                                XXPermissions.startPermissionActivity(MainActivity.this, permissions);
                                            } else {
                                                //toast("获取录音和日历权限失败");
                                            }
                                        }
                                    });
                        }
                    }).create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus();
        }
        //setContentView(R.layout.activity_main);
        initViews();
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.application.LOCAL_BROADCAST");
        localReceiver = new LocalReceiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);


        }
    private void initViews() {
        SearchView searchView = findViewById(R.id.search_bar);
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
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("wd",s);
                MainActivity.this.startActivity(intent);
                return true;
            }
            // 搜索框文本改变事件
            @Override
            public boolean onQueryTextChange(String s) {
                // 文本内容是空就让 recyclerView 填充全部数据 // 可以是其他容器 如listView

                return false;
            }
        });

        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Toast.makeText(MainActivity.this, hasFocus + "", Toast.LENGTH_SHORT).show();
            }
        });
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(sectionsPagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //Toast.makeText(MainActivity.this, getTabView(position).getId() + "", Toast.LENGTH_SHORT).show();
                PlaceholderFragment fragment = sectionsPagerAdapter.getCurrentFragment();
                if (DeviceManager.isTv() && !tabViews.isFocused()) {
                    fragment.scrollItemToTop();

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabs = (TvTabLayout) findViewById(R.id.tabs);
        tabs.setOnFocusChangeListener(this);
        tabs.setupWithViewPager(viewPager);
        tabViews = new TabViews(tabs, new TabViews.OnTabFocusItemListener() {
            @Override
            public void onUnfocused() {
                sectionsPagerAdapter.getCurrentFragment().scrollItemToTop();
            }
        });
        ImageButton userButton = (ImageButton)findViewById(R.id.user);
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserSetting();
            }
        });
    }
    public boolean isTabFocused(){
        return tabViews.isFocused();
    }
    public void clearTabFocus(){
        tabViews.clearFocus();
    }
    public int getCurrentPage(){
        return viewPager.getCurrentItem();
    }
    private AlertDialog loginDialog = null;
    public void showUserSetting(){

        String username = SPUtils.get(this).getValue("username", "");
        new CustomDialog.Builder(this)
                .setTitle("用户设置")
                .setMessage("登录/查看观影历史/查看离线缓存")
                .setList(Arrays.asList(new String[]{username.isEmpty()?"未登录":"当前用户："+ username, "用户观看历史", "用户缓存记录"}), null, -1)
                .setOnItemClickListener(new DialogItemAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(String item, Object o, int which, CustomDialog dialog) {
                        if (which == 0){
                            View view = View.inflate(MainActivity.this, R.layout.dialog_login, null);

                            view.findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String username = ((EditText)view.findViewById(R.id.et_user)).getText().toString();
                                    String password = ((EditText)view.findViewById(R.id.et_password)).getText().toString();
                                    LoginTool.login(MainActivity.this, username, password, new LoginTool.OnLogin() {
                                        @Override
                                        public void success() {
                                            if (loginDialog!= null){
                                                loginDialog.hide();
                                                SPUtils.get(MainActivity.this).putValue("username", username);
                                                Snackbar.make(viewPager, "登录成功", Snackbar.LENGTH_LONG).setAction("Action", null).setBackgroundTint(getResources().getColor(R.color.colorPrimary)).show();
                                            }
                                        }

                                        @Override
                                        public void fail() {
                                            Snackbar.make(viewPager, "登录失败", Snackbar.LENGTH_LONG).setAction("Action", null).setBackgroundTint(getResources().getColor(R.color.colorPrimary)).show();
                                        }
                                    });

                                }
                            });
                            view.findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ((EditText)view.findViewById(R.id.et_user)).setText("");
                                    ((EditText)view.findViewById(R.id.et_password)).setText("");
                                }
                            });
                            ((EditText)view.findViewById(R.id.et_user)).addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    String text = s.toString();
                                    if (text.endsWith("\n")){
                                        ((EditText)view.findViewById(R.id.et_user)).setText(text.replace("\n", "").replace("\r", ""));
                                        ((EditText)view.findViewById(R.id.et_password)).requestFocus();
                                    }
                                }
                            });

                            ((EditText)view.findViewById(R.id.et_password)).addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    String text = s.toString();
                                    if (text.endsWith("\n")){
                                        InputMethodManager manager = ((InputMethodManager)MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE));

                                        ((EditText)view.findViewById(R.id.et_password)).setText(text.replace("\n", "").replace("\r", ""));
                                        ((Button)view.findViewById(R.id.btn_login)).requestFocus();
                                        if (manager != null)
                                            manager.hideSoftInputFromWindow(((EditText)view.findViewById(R.id.et_password)).getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                                    }
                                }
                            });
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this).setView(view);
                            loginDialog = builder.create();
                            loginDialog.show();
                        }else if (which == 1){
                            if (!username.isEmpty()){
                                showHistory();
                            }
                        }else if (which == 2){
                            showDownloaded();
                        }
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus){
            //ShadowDrawable.setShadowDrawable(v, 20, Color.WHITE, 10, 0, 0);
            LogUtil.i("TAG","onFocusChange" + v.getId());
            if (v.getId() == R.id.view_pager){
                LogUtil.i("TAG","onFocusChange");
            }

            //设置焦点框的位置和动画
            //Tools.focusAnimator(v,onFousView);
        }
    }

    private long mLastKeyDownTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if (!tabViews.isFocused() && DeviceManager.isTv()){
                if (sectionsPagerAdapter.getCurrentFragment().inBanner()){
                    sectionsPagerAdapter.getCurrentFragment().exitBanner();
                }else
                    tabViews.focusCurrent();
                return true;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("是否退出程序");
            builder.setCancelable(true);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    ActivityCollector.finishAll();
                    //System.exit(0);
                }
            });
            //设置反面按钮
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();     //创建AlertDialog对象
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus();


            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU){
            showUserSetting();

            return false;
        }
        long current = System.currentTimeMillis();
        boolean dispatch;
        if (current - mLastKeyDownTime < 200) {
            dispatch= true;
        } else {
            dispatch= super.onKeyDown(keyCode, event);
            mLastKeyDownTime = current;
        }
        return dispatch;
    }
    private void showDownloaded(){
        Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
        startActivity(intent);
    }

    private void showHistory(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray hitems = History.getAll(MainActivity.this).getJSONArray("history");
                    String hnames[] = new String[hitems.length()];
                    String hurls[] = new String[hitems.length()];
                    for (int i = 0; i < hitems.length(); i++){
                        JSONObject object = hitems.getJSONObject(i);
                        hnames[i] = object.getString("name");
                        hurls[i] = object.getString("url");
                    }
                    final String[] hnames1 = hnames;
                    final String[] hurls1 = hurls;
                   MainActivity.this.runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           new CustomDialog.Builder(MainActivity.this)
                                   .setTitle("观看历史")
                                   .setList(Arrays.asList(hnames), null, -1)
                                   .setOnItemClickListener(new DialogItemAdapter.OnItemClickListener() {
                                       @Override
                                       public void onClick(String item, Object o, int position, CustomDialog dialog) {
                                           Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                                           intent.putExtra("name", hnames1[position].replaceAll("第-?\\d+集",""));
                                           intent.putExtra("url", hurls1[position]);
                                           MainActivity.this.startActivity(intent);
                                           dialog.dismiss();
                                       }
                                   })
                                   .create()
                                   .show();
                       }
                   });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();


    }

    class LocalReceiver extends BroadcastReceiver{
        private  int SIDE_LEFT = -1;
        private int SIDE_NONE = 0;
        private int SIDE_RIGHT = 1;

        @Override
        public void onReceive(Context context, Intent intent) {
            int side = intent.getIntExtra("side", 0);
            if (side != 0 && MainActivity.this.side == side){
                MainActivity.this.side = side;
                MainActivity.this.position = intent.getIntExtra("position", 0);
                int index = viewPager.getCurrentItem();
                index = index + side;
                if (index > 3) {
                    index = 3;
                    return;
                }
                if (index < 0) {
                    index = 0;
                    return;
                }
                viewPager.setCurrentItem(index);

            }

        }
    }
}