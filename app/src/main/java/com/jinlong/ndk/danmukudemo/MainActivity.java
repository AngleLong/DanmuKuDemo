package com.jinlong.ndk.danmukudemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

public class MainActivity extends AppCompatActivity {

    /**
     * 弹幕控件
     */
    private DanmakuView mDanmakuView;
    /**
     * 弹幕的上下文
     */
    private DanmakuContext mContext;
    /**
     * 背景的颜色
     */
    private String[] mContentColorBg = {"#0099ff", "#b2d15c", "#b9b9f1", "#f46c77"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDanmuKu();
    }

    /**
     * @author : 贺金龙
     * email : 753355530@qq.com
     * create at 2018/7/12  18:27
     * description : 初始化弹幕库
     */
    private void initDanmuKu() {
        //初始化控件
        mDanmakuView = findViewById(R.id.dv);

        //设置最大显示行数
        HashMap<Integer, Integer> maxLInesPair = new HashMap<>(16);
        maxLInesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 8);
        //设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<>(16);
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
        //创建弹幕上下文
        mContext = DanmakuContext.create();
        //设置一些相关的配置
        mContext.setDuplicateMergingEnabled(false)
                //是否重复合并
                .setScrollSpeedFactor(1.2f)
                //设置文字的比例
                .setScaleTextSize(1.2f)
                //图文混排的时候使用！
                .setCacheStuffer(new MyCacheStuffer(this), mBackgroundCacheStuffer)
                //设置显示最大行数
                .setMaximumLines(maxLInesPair)
                //设置防，null代表可以重叠
                .preventOverlapping(overlappingEnablePair);
        //设置解析器
        if (mDanmakuView != null) {
            BaseDanmakuParser defaultDanmakuParser = getDefaultDanmakuParser();
            //相应的回掉
            mDanmakuView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
                @Override
                public void updateTimer(DanmakuTimer timer) {
                    //定时器更新的时候回掉
                }

                @Override
                public void drawingFinished() {
                    //弹幕绘制完成时回掉
                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {
                    //弹幕展示的时候回掉
                }

                @Override
                public void prepared() {
                    //弹幕准备好的时候回掉，这里启动弹幕
                    mDanmakuView.start();
                }
            });
            mDanmakuView.prepare(defaultDanmakuParser, mContext);
            mDanmakuView.enableDanmakuDrawingCache(true);
        }
    }

    /**
     * @author : 贺金龙
     * email : 753355530@qq.com
     * create at 2018/7/13  11:21
     * description : 缓存相关的内容
     */
    private BaseCacheStuffer.Proxy mBackgroundCacheStuffer = new BaseCacheStuffer.Proxy() {
        @Override
        public void prepareDrawing(BaseDanmaku danmaku, boolean fromWorkerThread) {
            // 根据你的条件检查是否需要需要更新弹幕
        }

        @Override
        public void releaseResource(BaseDanmaku danmaku) {
            //清理相应的数据
            danmaku.tag = null;
        }
    };

    /**
     * @author : 贺金龙
     * email : 753355530@qq.com
     * create at 2018/7/12  18:26
     * description : 添加弹幕的方法
     */
    public void add(View view) {
        addDanmaku(false);
    }

    /**
     * @author : 贺金龙
     * email : 753355530@qq.com
     * create at 2018/7/12  18:31
     * description : 添加弹幕的方法
     */
    private void addDanmaku(boolean islive) {
        //创建一个弹幕对象，这里后面的属性是设置滚动方向的！
        BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null || mDanmakuView == null) {
            return;
        }
        //设置相应的数据
        Bitmap showBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        showBitmap = BitmapUtils.getShowPicture(showBitmap);
        Map<String, Object> map = new HashMap<>(16);
        map.put("content", "这里是显示的内容");
        map.put("bitmap", showBitmap);
        Random random = new Random();
        int randomNum = random.nextInt(mContentColorBg.length);
        map.put("color", mContentColorBg[randomNum]);
        danmaku.tag = map;

        //弹幕显示的文字
        danmaku.text = "这是一条弹幕" + System.nanoTime();
        //设置相应的边距
        danmaku.padding = 5;
        // 可能会被各种过滤器过滤并隐藏显示，若果是本机发送的弹幕，建议设置成1；
        danmaku.priority = 0;
        //是否是直播弹幕
        danmaku.isLive = islive;
        danmaku.setTime(mDanmakuView.getCurrentTime() + 1200);
        //设置文字大小
        danmaku.textSize = 25f;
        //设置文字颜色
        danmaku.textColor = Color.RED;
        //设置阴影的颜色
        danmaku.textShadowColor = Color.WHITE;
        // danmaku.underlineColor = Color.GREEN;
        //设置背景颜色
        danmaku.borderColor = Color.GREEN;
        //添加这条弹幕，也就相当于发送
        mDanmakuView.addDanmaku(danmaku);
    }

    /**
     * @author : 贺金龙
     * email : 753355530@qq.com
     * create at 2018/7/13  11:27
     * description : 添加图文混排的弹幕
     */
    public void addImage(View view) {
        BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);

        if (danmaku == null || mDanmakuView == null) {
            return;
        }

        //设置相应的数据
        Bitmap showBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon);
        showBitmap = BitmapUtils.getShowPicture(showBitmap);
        Map<String, Object> map = new HashMap<>(16);
        map.put("content", "这里是显示的内容");
        map.put("bitmap", showBitmap);
        Random random = new Random();
        int randomNum = random.nextInt(mContentColorBg.length);
        map.put("color", mContentColorBg[randomNum]);

        danmaku.tag = map;
        danmaku.textSize = 0;
        danmaku.padding = 10;
        danmaku.text = "";
        // 一定会显示, 一般用于本机发送的弹幕
        danmaku.priority = 1;
        danmaku.isLive = false;
        danmaku.setTime(mDanmakuView.getCurrentTime());
        danmaku.textColor = Color.WHITE;
        // 重要：如果有图文混排，最好不要设置描边(设textShadowColor=0)，否则会进行两次复杂的绘制导致运行效率降低
        danmaku.textShadowColor = 0;


        mDanmakuView.addDanmaku(danmaku);
    }


    /**
     * @author : 贺金龙
     * email : 753355530@qq.com
     * create at 2018/7/12  18:30
     * description : 最简单的解析器
     */
    public static BaseDanmakuParser getDefaultDanmakuParser() {
        return new BaseDanmakuParser() {
            @Override
            protected IDanmakus parse() {
                return new Danmakus();
            }
        };
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            mDanmakuView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDanmakuView != null) {
            // dont forget release!
            mDanmakuView.release();
            mDanmakuView = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mDanmakuView != null) {
            // dont forget release!
            mDanmakuView.release();
            mDanmakuView = null;
        }
    }
}
