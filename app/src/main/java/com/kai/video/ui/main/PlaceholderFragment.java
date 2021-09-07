package com.kai.video.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.kai.video.MainActivity;
import com.kai.video.R;
import com.kai.video.bean.DeviceManager;
import com.kai.video.bean.GlideApp;
import com.kai.video.tool.FocusGridLayoutManager;
import com.kai.video.tool.LinearTopSmoothScroller;
import com.kai.video.tool.SimpleConductor;
import com.kai.video.tool.VideoItemAdapter;
import com.kai.video.view.GridSpacesItemDecoration;
import com.kai.video.view.SearchDialog;
import com.winton.bottomnavigationview.NavigationView;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import q.rorbin.verticaltablayout.VerticalTabLayout;
import q.rorbin.verticaltablayout.adapter.TabAdapter;
import q.rorbin.verticaltablayout.widget.ITabView;
import q.rorbin.verticaltablayout.widget.TabView;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {
    private String[] actions = {"tv", "film", "cartoon", "zy"};
    private RecyclerView recyclerView;
    private View progressBar;
    private ImageButton searchButton;
    private ImageButton userButton;
    private NavigationView navigationView;
    private FocusGridLayoutManager layoutManager;
    private boolean isViewInitFinished = false;
    private boolean dataLoad = false;
    private VerticalTabLayout tabLayout;
    private int index;
    private VideoItemAdapter adapter;
    private SimpleConductor simpleConductor;
    public PlaceholderFragment(int index){
        super();
        this.index = index;
    }
    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment(index);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        simpleConductor = new SimpleConductor(actions[index]);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = root.findViewById(R.id.video_list);
        progressBar = root.findViewById(R.id.progress);
        isViewInitFinished = true;
        if (!DeviceManager.isTv())
            root.findViewById(R.id.bar_line).setVisibility(View.GONE);
        tabLayout = root.findViewById(R.id.tablayout);
        searchButton = root.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchDialog dialog = new SearchDialog(getContext());
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();
            }
        });
        navigationView = root.findViewById(R.id.navigation);
        userButton = root.findViewById(R.id.user);
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).showUserSetting();
            }
        });
        if(!DeviceManager.isTv()){
            root.findViewById(R.id.tv_bar).setVisibility(View.GONE);

        }else {
            navigationView.setVisibility(View.GONE);
        }
        return root;
    }
    public static boolean isScreenOriatationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
    private Handler focusHandler = new Handler();
    private Runnable focusRunnable = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    View focusView = layoutManager.findViewByPosition(position);
                    if (focusView != null)
                        focusView.findViewById(R.id.main).requestFocus();
                }
            });

        }
    };

    public void scrollItemToTop(){
        try {
            if (DeviceManager.isTv()){
                layoutManager.scrollToPositionWithOffset(position, 0);
                focusHandler.postDelayed(focusRunnable, 500);
            }

        }catch (Exception e){

        }

    }
    private int position = 0;
    public void getData(){
        dataLoad = true;
        new Thread(new Runnable() {
            @Override
            public void run() {

                simpleConductor.get(new SimpleConductor.OnGetListener() {
                    @Override
                    public void onFinish(final List<Element> list) {
                        ((Activity)getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                adapter = new VideoItemAdapter(list, getContext(), index==0);
                                layoutManager = new FocusGridLayoutManager(getContext(), DeviceManager.isPhone()?4:7, adapter);
                                layoutManager.setOnReachHeader(new FocusGridLayoutManager.OnReachHeader() {
                                    @Override
                                    public void onReach(int position) {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((MainActivity)getActivity()).clearTabFocus();
                                                layoutManager.findViewByPosition(position).findViewById(R.id.main).requestFocus();
                                            }
                                        }, 800);

                                    }
                                });
                                recyclerView.setLayoutManager(layoutManager);
                                adapter.setHasStableIds(true);
                                layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                    @Override
                                    public int getSpanSize(int position) {
                                        if (adapter.getItemViewType(position) == 2)
                                            return layoutManager.getSpanCount();
                                        else
                                            return 1;
                                    }
                                });
                                adapter.setOnFocusListner(new VideoItemAdapter.OnFocusListner() {
                                    @Override
                                    public void onFocus(String type, int position) {
                                        PlaceholderFragment.this.position = position;
                                        tabLayout.setTabSelected(simpleConductor.getTypes().indexOf(type), false);
                                    }
                                });
                                adapter.setOnFinishListener(new VideoItemAdapter.OnFinishListener() {
                                    @Override
                                    public void onFinish() {
                                        progressBar.setVisibility(View.GONE);
                                        try {
                                            //如果处于当前页面，会自动
                                            if (index == ((MainActivity)getActivity()).getCurrentPage() && !((MainActivity)getActivity()).isTabFocused()){
                                                scrollItemToTop();
                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }

                                });

                                recyclerView.setAdapter(adapter);
                                GridSpacesItemDecoration decorator = new GridSpacesItemDecoration(getActivity(), adapter, actions[index]);
                                recyclerView.addItemDecoration(decorator);
                                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && !DeviceManager.isTv())
                                recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                                    @Override
                                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                        super.onScrollStateChanged(recyclerView, newState);
                                        try {
                                            String top = adapter.getAction(((GridLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition());
                                            String bottom = adapter.getAction(((GridLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition());
                                            if ((top != null && bottom != null) && (!top.equals(actions[index]) || !bottom.equals(actions[index]))){
                                                navigationView.setOnTabSelectedListener(null);
                                                if (top.equals(actions[index])){
                                                    navigationView.check(simpleConductor.getTypes().indexOf(bottom));
                                                }else {
                                                    navigationView.check(simpleConductor.getTypes().indexOf(top));
                                                }
                                                navigationView.setOnTabSelectedListener(onTabSelectedListener);
                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }

                                    }
                                });
                                initTabs();
                            }
                        });

                    }
                });
            }
        }).start();
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isViewInitFinished  && index != 0){
            if (!dataLoad)
                getData();
        }
    }
    private NavigationView.OnTabSelectedListener onTabSelectedListener;
    private void initTabs(){
        if (DeviceManager.isTv()){
            tabLayout.setTabAdapter(new TabAdapter() {
                @Override
                public int getCount() {
                    return simpleConductor.getTypes().size();
                }

                @Override
                public TabView.TabBadge getBadge(int position) {
                    return null;
                }

                @Override
                public TabView.TabIcon getIcon(int position) {
                    String type = simpleConductor.getTypes().get(position);
                    final float scale = getActivity().getResources().getDisplayMetrics().density;
                    switch (type){
                        case "tencent": return new ITabView.TabIcon.Builder()
                                .setIcon(R.drawable.tencent, R.drawable.tencent)
                                .setIconSize((int)(scale*80+0.5f), (int)(scale*35+0.5f))
                                .build();
                        case "iqiyi": return new ITabView.TabIcon.Builder()
                                .setIcon(R.drawable.iqiyi, R.drawable.iqiyi)
                                .setIconSize((int)(scale*80+0.5f), (int)(scale*35+0.5f))
                                .build();
                        case "mgtv" : return new ITabView.TabIcon.Builder()
                                .setIcon(R.drawable.mgtv, R.drawable.mgtv)
                                .setIconSize((int)(scale*80+0.5f), (int)(scale*35+0.5f))
                                .build();
                        case "bilibili":return new ITabView.TabIcon.Builder()
                                .setIcon(R.drawable.bilibili, R.drawable.bilibili)
                                .setIconSize((int)(scale*80+0.5f), (int)(scale*30+0.5f))
                                .build();
                        case "bilibili1":return new ITabView.TabIcon.Builder()
                                .setIcon(R.drawable.bilibili, R.drawable.bilibili)
                                .setIconSize((int)(scale*80+0.5f), (int)(scale*30+0.5f))
                                .build();

                    }
                    return null;


                }

                @Override
                public TabView.TabTitle getTitle(int position) {
                    return null;
                }

                @Override
                public int getBackground(int position) {
                    return R.drawable.tab_item_selector;
                }
            });
            tabLayout.addOnTabSelectedListener(new VerticalTabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabView tab, int position) {
                    String type = simpleConductor.getTypes().get(position);
                    scrollType(type);
                }

                @Override
                public void onTabReselected(TabView tab, int position) {

                }
            });
        }else {
            List<NavigationView.Model> tabs = new ArrayList<>();
            for (String type : simpleConductor.getTypes()) {
                switch (type){
                    case "tencent": tabs.add(new NavigationView.Model.Builder(R.drawable.tencent,R.drawable.tencent).title("腾讯视频").build());continue;
                    case "iqiyi": tabs.add(new NavigationView.Model.Builder(R.drawable.iqiyi,R.drawable.iqiyi).title("爱奇艺").build());continue;
                    case "mgtv" : tabs.add(new NavigationView.Model.Builder(R.drawable.mgtv,R.drawable.mgtv).title("芒果TV").build());continue;
                    case "bilibili":tabs.add(new NavigationView.Model.Builder(R.drawable.bilibili,R.drawable.bilibili).title("哔哩哔哩").build());continue;
                    case "bilibili1":tabs.add(new NavigationView.Model.Builder(R.drawable.bilibili,R.drawable.bilibili).title("哔哩哔哩").build());continue;
                }
            }
            navigationView.setItems(tabs);
            navigationView.build();
            onTabSelectedListener = new NavigationView.OnTabSelectedListener() {
                @Override
                public void selected(int index, NavigationView.Model model) {
                    String type = simpleConductor.getTypes().get(index);
                    scrollType(type);

                }

                @Override
                public void unselected(int index, NavigationView.Model model) {

                }

            };
            navigationView.setOnTabSelectedListener(onTabSelectedListener);
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //initTabs();
        if(index == 0) {
            getData();
        }

    }
    public boolean inBanner(){
        View currentView;
        try {
            currentView = layoutManager.findViewByPosition(position).findViewById(R.id.main);
        }catch (Exception e){
            return false;
        }
        return adapter.getItemViewType(position)==2 && !currentView.isFocused() && currentView.hasFocus();
    }
    public void exitBanner(){
        layoutManager.findViewByPosition(position).findViewById(R.id.main).requestFocus();
    }
    public Handler scrollHandler = new Handler();
    private void scrollType(String type){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (adapter == null || adapter.getItems() == null)
                    return;;
                List<Element> elements = adapter.getItems();
                for (int i = 0; i < elements.size(); i++) {
                    if (adapter.getItemViewType(i) == 2 && adapter.getItems().get(i).attr("actionk").equals(type)){
                        final int index = i;
                        scrollHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                layoutManager.scrollToPositionWithOffset(index, 0);
                            }
                        });

                        return;
                    }
                }
            }
        }).start();

    }

}