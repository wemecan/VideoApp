package com.kai.video.tool;

import android.graphics.Color;
import android.util.Log;

import org.json.JSONArray;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.Danmaku;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.danmaku.parser.android.JSONSource;

public class MyDanmakuParser extends BaseDanmakuParser {

    @Override
    protected IDanmakus parse() {
        if (mDataSource != null) {
            LogUtil.i("tag", "弹幕数据加载");
            JSONSource jsonSource = (JSONSource) mDataSource;
            return doParse(jsonSource.data());
        }
        LogUtil.i("tag", "弹幕数据失效");
        return new Danmakus();
    }

    @Override
    public BaseDanmakuParser load(IDataSource<?> source) {
        return super.load(source);
    }

    /**
     * @param danmakuListData 弹幕数据
     *                        传入的数组内包含普通弹幕，会员弹幕，锁定弹幕。
     * @return 转换后的Danmakus
     */
    private Danmakus doParse(JSONArray danmakuListData) {
        Danmakus danmakus = new Danmakus();
        if (danmakuListData == null || danmakuListData.length() == 0) {
            LogUtil.i("tag", "弹幕格式有误");
            return danmakus;
        }
        try {
            danmakus = _parse(danmakuListData, danmakus);
        }catch (Exception e){
            e.printStackTrace();
        }

        return danmakus;
    }

    private Danmakus _parse(JSONArray jsonArray, Danmakus danmakus){
        LogUtil.i("tag", "正在解析弹幕");
        if (danmakus == null) {
            danmakus = new Danmakus();
        }
        if (jsonArray == null || jsonArray.length() == 0) {
            return danmakus;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONArray list = jsonArray.getJSONArray(i);
                //Log.d("TAG", list.toString());
                if (list.length() > 0) {
                    float textSize = 14; // 字体大小
                    int t = list.getInt(1);
                    int type = 1; // 弹幕类型
                    if (t == 1) {
                        type = Danmaku.TYPE_FIX_TOP;
                        textSize = 16;
                    }
                    else if (t == 0)
                        type = Danmaku.TYPE_SCROLL_RL;
                    if (type == 7)
                        // FIXME : hard code
                        // TODO : parse advance danmaku json
                        continue;
                    long time = list.getLong(0); // 出现时间
                    int color = list.getInt(2) | 0xFF000000; // 颜色


                    BaseDanmaku item = mContext.mDanmakuFactory.createDanmaku(type, mContext);
                    if (item != null) {
                        item.setTime(time * 1000);

                        item.textSize = textSize * (mDispDensity - 0.6f);
                        item.textColor = color;
                        item.textShadowColor = color <= Color.BLACK ? Color.WHITE : Color.BLACK;
                        item.index = i;
                        item.flags = mContext.mGlobalFlagValues;
                        item.setTimer(mTimer);
                        item.text = list.getString(4).replace("&nbsp;","");
                        danmakus.addItem(item);
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        LogUtil.i("tag", String.valueOf(danmakus.size()));
        return danmakus;
    }


}
