package com.kai.video.tool;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kai.video.InfoActivity;
import com.kai.video.R;
import com.kai.video.bean.DeviceManager;
import com.kai.video.bean.GlideApp;
import com.kai.video.view.ScrollTextView;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class VideoItemAdapter extends RecyclerView.Adapter<VideoItemAdapter.ViewHolder> {
    private OnFinishListener onFinishListener;
    private OnFocusListner onFocusListner;
    private List<Element> items = new ArrayList<>();
    private boolean focus;
    private LocalBroadcastManager localBroadcastManager;

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    public void setOnFocusListner(OnFocusListner onFocusListner) {
        this.onFocusListner = onFocusListner;
    }

    public List<Element> getItems() {
        return items;
    }
    static class BannerHolder extends ViewHolder{
        Banner banner;
        boolean init;

        public boolean isInit() {
            return init;
        }

        public void setInit(boolean init) {
            this.init = init;
        }

        public BannerHolder(View view){
            super(view);
            banner = (Banner) view.findViewById(R.id.banner);
        }
    }
    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView posterView;
        ScrollTextView videoTitle;
        Context context;
        boolean isBanner = false;
        RelativeLayout relativeLayout;
        ImageView logoView;
        ImageView logo;
        boolean first = false;
        public ViewHolder(View view){
            super(view);
            relativeLayout = (RelativeLayout)view.findViewById(R.id.main);
            posterView = (ImageView)view.findViewById(R.id.poster);
            videoTitle = (ScrollTextView) view.findViewById(R.id.title);
            try {
                if (DeviceManager.isTv()){
                    videoTitle.setMaxEms(8);
                    videoTitle.setEllipsize(TextUtils.TruncateAt.END);
                }else{
                    videoTitle.setMaxEms(6);
                    videoTitle.setEllipsize(TextUtils.TruncateAt.END);
                }
            }catch (Exception e){

            }

            logo = (ImageView)view.findViewById(R.id.logo);
            logoView = (ImageView) view.findViewById(R.id.logo_view);
        }

        public boolean isBanner() {
            return isBanner;
        }

        public void setBanner(boolean banner) {
            isBanner = banner;
        }

        public void setFirst(boolean first) {
            this.first = first;
        }

        public boolean isFirst() {
            return first;
        }

        public void addContext(Context context){
            this.context = context;
        }

        public Context getContext() {
            return context;
        }
    }
    private Context mContext;
    public VideoItemAdapter(List<Element> items, Context context, boolean focus){
        this.mContext = context;
        this.items = items;
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        this.focus = focus;

    }

    public boolean isFirstRow(int itemPosition){
        int row = DeviceManager.isTv()?7:4;
        int first = itemPosition - itemPosition%row;
        return getHeader(first)!=null;
    }
    public String getAction(int position){
        if (position > items.size()-1)
            return null;
        String type = null;
        Element element = items.get(position);
        int itemType = getItemViewType(position);
        if (itemType == 1 ){
            if (element == null)
                return type;
            String url = element.getElementsByTag("a").attr("abs:href").replaceAll("\"","").replaceAll("\\\\","");
            if (url.contains("v.qq.com"))
                type = "tencent";
            else if (url.contains("www.iqiyi.com"))
                type = "iqiyi";
            else if (url.contains("www.bilibili.com"))
                type = "bilibili";
            else if (url.contains("www.mgtv.com"))
                type = "mgtv";

        }else if (itemType == 2){
            type = element.attr("actionk");
        }
        return type;

    }
    public String getHeader(int itemPosition) {
        if (itemPosition > items.size()-1)
            return null;
        if (items.get(itemPosition) == null)
            return null;
        if (items.get(itemPosition).hasAttr("actionk"))
            return items.get(itemPosition).attr("actionk");
        else
            return null;
    }
    public boolean hasReachHeader(int itemPosition){
        int offset = (DeviceManager.isTv()?7:4);
        int end = itemPosition - offset;
        if (end < 0)
            end = 0;
        for (int i = itemPosition; i > itemPosition - offset; i--){
            if (getHeader(i)!=null) {
                return true;
            }
        }
        return false;
    }
    public int getExactPosition(int itemPosition, int direction){
        if (itemPosition > items.size()-1)
            return itemPosition;
        int div = 1;
        if (direction == View.FOCUS_UP)
            div = -1;
        if (getItemViewType(itemPosition) == 2)
            return itemPosition + div;
        int offset = (DeviceManager.isTv()?7:4)*div;
        for (int i = itemPosition;i != itemPosition + offset; i+=div){
            if (getHeader(i)!=null) {
                return i;
            }
        }
        return itemPosition + offset;
    }


    @Override
    public int getItemViewType(int position) {
        if (position > items.size()-1)
            return 0;
        try {
            if (items.get(position) == null){
                return 0;
            }else if (items.get(position).attr("action").equals("actionl")){
                return 2;
            }else
                return 1;
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.videopic_none, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            holder.itemView.setFocusable(false);
            holder.itemView.setFocusableInTouchMode(false);
            return holder;
        }
        else if (viewType == 2) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.videopic_banner, parent, false);
            final BannerHolder holder = new BannerHolder(view);
            holder.setBanner(true);
            holder.banner.setDelayTime(3000);
            holder.banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE);
            holder.banner.setIndicatorGravity(BannerConfig.RIGHT);
            holder.banner.setImageLoader(new ImageLoader() {
                @Override
                public void displayImage(Context context, Object path, ImageView imageView) {
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    GlideApp.with(context)
                            .asDrawable()
                            .load(path)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .dontAnimate()
                            .into(imageView);
                }
            });
            holder.itemView.setFocusable(true);
            holder.itemView.setFocusableInTouchMode(false);
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus){
                        holder.relativeLayout.requestFocus();
                    }
                }
            });
            return holder;
        }
        else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.videopic, parent, false);
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

                    int position = holder.getAdapterPosition();
                    Element element = items.get(position);
                    String url = element.getElementsByTag("a").attr("abs:href").replaceAll("\"","").replaceAll("\\\\","");
                    if (url.contains("121.5.20.185"))
                        url = url.substring(url.indexOf("=")+1);
                    Intent intent = new Intent(parent.getContext(), InfoActivity.class);
                    intent.putExtra("name", element.getElementsByClass("t").text());
                    intent.putExtra("url", url);
                    parent.getContext().startActivity(intent);

                }
            });
            return holder;
        }


    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Element element = items.get(position);
        final int type = getItemViewType(position);
        if (type == 0)
            return;
        else if (type == 1){
            String url = element.getElementsByTag("a").attr("abs:href").replaceAll("\"","").replaceAll("\\\\","");
            holder.videoTitle.stopScroll();
            holder.relativeLayout.setOnFocusChangeListener(null);
            holder.relativeLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                    String type = "";
                    if (url.contains("v.qq.com"))
                        type = "tencent";
                    else if (url.contains("www.iqiyi.com"))
                        type = "iqiyi";
                    else if (url.contains("www.bilibili.com"))
                        type = "bilibili";
                    else if (url.contains("www.mgtv.com"))
                        type = "mgtv";
                    final String t = type;
                    if (hasFocus){
                        holder.videoTitle.startScroll();
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                if (onFocusListner!=null && DeviceManager.isTv())
                                    onFocusListner.onFocus(t, position);
                            }
                        });
                    }else {
                        holder.videoTitle.stopScroll();
                    }
                }
            });

            if (url.contains("v.qq.com"))
                GlideApp.with(holder.itemView).load(R.drawable.tencent).dontAnimate().into(holder.logo);
            else if (url.contains("www.iqiyi.com"))
                GlideApp.with(holder.itemView).load(R.drawable.iqiyi).dontAnimate().into(holder.logo);
            else if (url.contains("www.bilibili.com"))
                GlideApp.with(holder.itemView).load(R.drawable.bilibili).dontAnimate().into(holder.logo);
            else if (url.contains("www.mgtv.com"))
                GlideApp.with(holder.itemView).load(R.drawable.mgtv).dontAnimate().into(holder.logo);

            GlideApp.with(holder.getContext())
                    .asDrawable()
                    .load(element.getElementsByTag("img").first().attr("data-original").replaceAll("\"","").replaceAll("\\\\","")).placeholder(R.drawable.loading)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerInside()
                    .dontAnimate()
                    .into(holder.posterView);
            holder.videoTitle.setText(element.getElementsByClass("t").text());
            if (position == 0 && focus){
                holder.setFirst(true);

            }
        }else if (type == 2){
            ((BannerHolder)holder).banner.stopAutoPlay();
            List<String> tiles = new ArrayList<>();
            List<String> pics = new ArrayList<>();
            for (Element e : items.get(position).getElementsByTag("li")) {
                tiles.add(e.attr("title"));
                pics.add(e.attr("background"));

            }
            if (((BannerHolder) holder).isInit()){
                ((BannerHolder) holder).banner.update(pics, tiles);
                ((BannerHolder) holder).banner.startAutoPlay();
            }else {
                ((BannerHolder) holder).banner.setImages(pics);
                ((BannerHolder)holder).banner.setBannerTitles(tiles);
                try {
                    ((BannerHolder)holder).banner.start();
                }catch (Exception e){
                    e.printStackTrace();
                }

                ((BannerHolder)holder).banner.startAutoPlay();
            }

            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((BannerHolder) holder).banner.stopAutoPlay();
                    ((BannerHolder)holder).banner.requestFocus();
                }
            });

            ((BannerHolder)holder).banner.setOnBannerListener(new OnBannerListener() {
                @Override
                public void OnBannerClick(int position1) {
                    Intent intent = new Intent(mContext, InfoActivity.class);
                    intent.putExtra("name", "");
                    intent.putExtra("url", items.get(position).getElementsByTag("li").get(position1).attr("href"));
                    mContext.startActivity(intent);
                }
            });
            holder.relativeLayout.setOnFocusChangeListener(null);
            holder.relativeLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        ((BannerHolder) holder).banner.startAutoPlay();
                        if (onFocusListner != null && DeviceManager.isTv()) {
                            onFocusListner.onFocus(items.get(position).attr("actionk"), position);
                        }
                    }
                }
            });
        }

    }

    boolean finish = false;
    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        if (!finish){
            onFinishListener.onFinish();
            finish = true;
        }
        if (holder.isBanner()){
            ((BannerHolder)holder).banner.startAutoPlay();
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder.isBanner()){
            ((BannerHolder)holder).banner.stopAutoPlay();
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        if (items.get(position) == null)
            return position;
        return items.get(position).hashCode();
    }

    public interface OnFinishListener{
        void onFinish();
    }
    public interface OnFocusListner{
        void onFocus(String type, int position);
    }
}
