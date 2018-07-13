好久没写博客了，也是因为最近公司项目挺忙的！正好这次迭代之后有点时间，所以写写博客，消磨一下时间！
现在很多直播软件都有相应的弹幕功能，以前也没怎么关注，最近正好公司的项目中用到了关于弹幕的内容，所以这里正好记录一下相关的知识！

[B站DanmakuFlameMaster弹幕的相关链接](https://github.com/Bilibili/DanmakuFlameMaster)

# 本文知识点
- DanmakuFlameMaster的集成与简单使用
- DanmakuFlameMaster的进阶使用


## 1. DanmakuFlameMaster的集成与简单使用
> 其实我这个人真的很笨，最初学习这个的时候，在网上找了很多文章！但是我都没怎么看懂，基本上都是把gitHub里面的内容直接粘贴过来的，后来知道怎么弄才大概看明白！或许自己太笨了吧！

### 1.1 DanmakuFlameMaster的集成
> 这个问题挺简单的，没有什么好说的，按照github上面集成就可以了！如果你不需要兼容x86和armv5就不用添加最下面两行的内容了！

```
repositories {
    jcenter()
}

dependencies {
    compile 'com.github.ctiao:DanmakuFlameMaster:0.9.25'
    compile 'com.github.ctiao:ndkbitmap-armv7a:0.9.21'

    # Other ABIs: optional 这个是适配多种架构的，如果你用虚拟机建议加上
    compile 'com.github.ctiao:ndkbitmap-armv5:0.9.21'
    compile 'com.github.ctiao:ndkbitmap-x86:0.9.21'
}
```

### 1.2 DanmakuFlameMaster的简单使用
> 开始的时候需要设置的内容还是很多的，我们一个一个来讲解！

#### 1.2.1 布局文件
> DanmakuFlameMaster使用多种方式(View/SurfaceView/TextureView)实现高效绘制！其中分别对应(DanmakuView/DanmakuSurfaceView/DanmakuTextureView)等相关的View，由于项目中集成的是最简单的弹幕功能，所有就没有使用关于SurfaceView类的弹幕控件！

```
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <Button
        android:id="@+id/btn"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:onClick="addDanmaku"
        android:text="添加弹幕"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <master.flame.danmaku.ui.widget.DanmakuView
        android:id="@+id/dv"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toBottomOf="@id/btn"
        app:layout_constraintVertical_weight="1" />

</android.support.constraint.ConstraintLayout>
```

布局基本上就是看你们项目中的需求，从而确定相应的位置！没有什么好说的！！！

#### 1.2.2 设置相应的属性
> 一些简单的配置，都是DEMO上面有的，注解写的基本上很清楚了，没有什么太多好说的！

```
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
            //图文混排的时候使用！这里可以不用
            .setCacheStuffer(new MyCacheStuffer(mActivity), mBackgroundCacheStuffer)
            //设置显示最大行数
            .setMaximumLines(maxLInesPair)
            //设置防，null代表可以重叠
            .preventOverlapping(overlappingEnablePair);
    //设置解析器
    BaseDanmakuParser defaultDanmakuParser = getDefaultDanmakuParser();
```

其实最开始我们项目中使用这个的时候，所有弹幕都是直接服务器返回的。所以开始的时候，我的想法是通过解析器去处理，但是后来我放弃了！为什么？首先json的解析规则是很复杂的，代码我简单看了看，说实话，能力有限真的没看懂相应的json结构，而且即使看懂我，我还要把服务器的数据，处理成可用的json结构。我觉得这样没有必要，所以就开启了一个线程，添加相应的弹幕了！是不是很机智。。。但是即便是这样上面那个设置解析器的步骤也是不能省略的！

```
    public static BaseDanmakuParser getDefaultDanmakuParser() {
        return new BaseDanmakuParser() {
            @Override
            protected IDanmakus parse() {
                return new Danmakus();
            }
        };
    }
```

因为这里解析器没有什么作用，所以这里直接按照最简单的方法写了一个解析器！代码如上：

 > 基本上上面就涵盖了所有关于弹幕的配置内容了！这里面关于setCacheStuffer()这个属性我之后会进行相应的讲解！这里你就知道有这么个东西就行，它主要是处理非文字类型弹幕的！所以如果你要是纯文字的话可以不设置这个东西！后面会详细讲解这个东西的！


#### 1.2.3 启动相应的弹幕
>  关于启动弹幕，基本上都走的是相应的回调，在弹幕准备好的时候，直接调相应的启动方法就好了！

```
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
```

还有相关的生命周期方法必须设置！重要的事情说三遍，三遍，三遍！
```
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
```

这个时候你会发现你的屏幕上没有任何内容，这就对了！为什么呢？因为你还没添加弹幕呢！！！所有的准备工作都做好了，那么我们就开始添加弹幕吧！按照B站的指示我们这么配置相关的弹幕
```
    private void addDanmaku(boolean islive) {
        //创建一个弹幕对象，这里后面的属性是设置滚动方向的！
        BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null || mDanmakuView == null) {
            return;
        }
        //弹幕显示的文字
        danmaku.text = "这是一条弹幕" + System.nanoTime();
        //设置相应的边距，这个设置的是四周的边距
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
```

这样就成功的发送了一条文字弹幕了，这里说一个问题，最开始的时候，我在想页面增加弹幕的时机？因为不存在按钮进行添加弹幕，那么只有在mDanmakuView.start();之后进行调用，无法在生命周期方法中调用，如果是在生命周期方法中调用的话，会存在弹幕为空，不能添加的问题！切记。。。这样整个流程就穿起来了！


![](https://user-gold-cdn.xitu.io/2018/7/12/1648e2b84acde2ad?w=600&h=1067&f=gif&s=3606635)


## 2. DanmakuFlameMaster的进阶使用

### 2.1 实现自定义弹幕的显示
> 上面那个显示，一般只会用在视频直播的内容上，但是对于有头像的那种弹幕！比如说，产品跑过来说！要不添加一个头像吧！再加点话术，弄的好看点！就像下面这样：

![](https://user-gold-cdn.xitu.io/2018/7/12/1648e2bfafd9b24d?w=265&h=72&f=png&s=7615)

刚开始我在网上找的时候，很多人都说使用**SpannableStringBuilder**去实现，但是我觉得如果使用SpannableStringBuilder实现这个内容的话，很蛋疼的！而且还会特别费尽，如果样式在复杂一点的话，那么就更加困难了！下面我们就来说说关于这个内容的实现！

> 还记得上面说到的关于图文有一个设置吗？**setCacheStuffer(BaseCacheStuffer cacheStuffer, BaseCacheStuffer.Proxy cacheStufferAdapter)** 这个是针对非文字的一些显示样式的设置！因为项目中要实现的就是上面这个样式，所以我仔细研究了一下关于上面这种样式的显示方案！

其实也是很简单的！就是重写**BaseCacheStuffer**类的一些方法而已！怎么实现的呢？其实就是自己绘制每条弹幕所显示的内容，这里其实应该是个策略模式的实现，感兴趣的童鞋可以看看！这里就考验Canvas的一些API的使用了！不会的童鞋可以百度一下！好了，闲扯了这么久了！我们开始吧！

我在开始的时候，讲过说**setCacheStuffer(BaseCacheStuffer cacheStuffer, BaseCacheStuffer.Proxy cacheStufferAdapter)**这个是实现非文字的方法！所以只要你把上面的两个参数搞懂就可以了！
- 参数1：你可以理解为绘制的相应处理
- 参数2：你可以理解为一个相应绘制的回调

我们一个一个去处理：
### 2.1.1 实现相应的绘制
> 绘制的相应操作主要是实现BaseCacheStuffer这个抽象类，所有关于绘制的方法都是你自己进行实现的！所以我说这里之前最好理解一下相应的Canvas这个类！！！当你继承这个抽象类的时候，你必须实现三个相应的方法：

```
public class MyCacheStuffer extends BaseCacheStuffer {
    @Override
    public void measure(BaseDanmaku danmaku, TextPaint paint, boolean fromWorkerThread) {
        //测量的相应方法
    }

    @Override
    public void clearCaches() {
        //用来释放或者清除一些资源
    }

    @Override
    public void drawDanmaku(BaseDanmaku danmaku, Canvas canvas, float left, float top, boolean fromWorkerThread, AndroidDisplayer.DisplayerConfig displayerConfig) {
        //绘制的相应方法
    }
}
```

基本上就是上面的这三个方法，最主要的就是测量和绘制的两个方法！下面就贴一个上面显示内容的实现！

```
public class MyCacheStuffer extends BaseCacheStuffer {

    /**
     * 文字右边间距
     */
    private float RIGHTMARGE;
    /**
     * 文字和头像间距
     */
    private float LEFTMARGE;
    /**
     * 文字和右边线距离
     */
    private int TEXT_RIGHT_PADDING;
    /**
     * 文字大小
     */
    private float TEXT_SIZE;
    /**
     * 头像的大小
     */
    private float IMAGEHEIGHT;

    public MyCacheStuffer(Activity activity) {
        // 初始化固定参数，这些参数可以根据自己需求自行设定
        LEFTMARGE = activity.getResources().getDimension(R.dimen.DIMEN_13PX);
        RIGHTMARGE = activity.getResources().getDimension(R.dimen.DIMEN_22PX);
        IMAGEHEIGHT = activity.getResources().getDimension(R.dimen.DIMEN_60PX);
        TEXT_SIZE = activity.getResources().getDimension(R.dimen.DIMEN_24PX);
    }

    @Override
    public void measure(BaseDanmaku danmaku, TextPaint paint, boolean fromWorkerThread) {
        // 初始化数据
        Map<String, Object> map = (Map<String, Object>) danmaku.tag;
        String content = (String) map.get("content");
        Bitmap bitmap = (Bitmap) map.get("bitmap");

        // 设置画笔
        paint.setTextSize(TEXT_SIZE);

        // 计算名字和内容的长度，取最大值
        float contentWidth = paint.measureText(content);

        // 设置弹幕区域的宽度
        danmaku.paintWidth = contentWidth + IMAGEHEIGHT + LEFTMARGE + RIGHTMARGE;
        // 设置弹幕区域的高度
        danmaku.paintHeight = IMAGEHEIGHT * 2;
    }

    @Override
    public void clearCaches() {

    }

    @Override
    public void drawDanmaku(BaseDanmaku danmaku, Canvas canvas, float left, float top, boolean fromWorkerThread, AndroidDisplayer.DisplayerConfig displayerConfig) {
        // 初始化数据
        Map<String, Object> map = (Map<String, Object>) danmaku.tag;
        String content = (String) map.get("content");
        Bitmap bitmap = (Bitmap) map.get("bitmap");
        String color = (String) map.get("color");

        // 设置画笔
        Paint paint = new Paint();
        paint.setTextSize(TEXT_SIZE);

        //绘制背景
        int textLength = (int) paint.measureText(content);
        //随机数，主要是为了生成不同颜色的背景的
        paint.setColor(Color.parseColor(color));

        //获取图片的宽度
        float rectBgLeft = left;
        float rectBgTop = top;
        float rectBgRight = left + IMAGEHEIGHT + textLength + LEFTMARGE + RIGHTMARGE;
        float rectBgBottom = top + IMAGEHEIGHT;
        canvas.drawRoundRect(new RectF(rectBgLeft, rectBgTop, rectBgRight, rectBgBottom), IMAGEHEIGHT / 2, IMAGEHEIGHT / 2, paint);

        // 绘制头像
        float avatorRight = left + IMAGEHEIGHT;
        float avatorBottom = top + IMAGEHEIGHT;
        canvas.drawBitmap(bitmap, null, new RectF(left, top, avatorRight, avatorBottom), paint);

        // 绘制弹幕内容,文字白色的
        paint.setColor(Color.WHITE);
        float contentLeft = left + IMAGEHEIGHT + LEFTMARGE;
        //计算文字的相应偏移量
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        //为基线到字体上边框的距离,即上图中的top
        float textTop = fontMetrics.top;
        //为基线到字体下边框的距离,即上图中的bottom
        float textBottom = fontMetrics.bottom;

        float contentBottom = top + IMAGEHEIGHT / 2;
        //基线中间点的y轴计算公式
        int baseLineY = (int) (contentBottom - textTop / 2 - textBottom / 2);
        //绘制文字
        canvas.drawText(content, contentLeft, baseLineY, paint);
    }
}
```

忘了说明一下了：我感觉这里面有一个地方很巧妙，就是Tag的设置！可以把许多参数都携带过来，很不错的想法，当然不是我想到的！我也是借鉴别人的。。。一首《无地自容》-->送给自己!

当你添加弹幕的时候也会有改动，但是改动的地方很小，像下面这样！！！

```
    //创建一条弹幕
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
    //设置相应的tag
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
    //添加一条
    mDanmakuView.addDanmaku(danmaku);
```

这样就成功的设置了一条相应的弹幕了！但是我发现一个问题，就是当你这么设置了之后，之前发送文字的逻辑就要重新制定了！其实就是多定义一个类型，根据不同类型进行不同的绘制就好了！很好解决的！这里就不再这里展开说了！

### 2.2 相应的监听问题
> 关于监听，其实就是实现相应的方法，但是还是有必要说明一下，怎么处理！

```
 mDanmakuView.setOnDanmakuClickListener(new IDanmakuView.OnDanmakuClickListener() {
                @Override
                public boolean onDanmakuClick(IDanmakus danmakus) {
                    //点击事件
                    BaseDanmaku latest = danmakus.last();
                    if (null != latest) {
                        Map<String, Object> map = (Map<String, Object>) latest.tag;
                        //获取相应的数据
                        String userId = (String) map.get("content");
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean onDanmakuLongClick(IDanmakus danmakus) {
                    //长按事件
                    return false;
                }

                @Override
                public boolean onViewClick(IDanmakuView view) {
                    //这个我没有尝试，但是应该是内部View的点击事件吧！猜测
                    return false;
                }
            });
        }
```

***
华丽的分割线
***

基本上就解决了我们项目中的问题，其实还有很多问题我没有去处理，这里只是给大家一个简单的案例，如果有什么不对的还希望指出！我及时修改。。。如果有什么不懂的，也可以留言！我尽量帮你解决！！！

忘了，[附上代码地址](https://github.com/AngleLong/DanmuKuDemo)
