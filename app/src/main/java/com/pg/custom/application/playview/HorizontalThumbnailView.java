package com.pg.custom.application.playview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Scroller;

import com.pg.custom.application.CUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 视频缩略图显示控件
 */
public class HorizontalThumbnailView extends View {

    /**
     * 10张图片的宽度
     */
    protected int mTenBitmapWidth;
    /**
     * 所有图片的宽度
     */
    protected int mTotalBitmapWidth;
    /**
     * 总宽度
     */
    protected int mRectWidth;
    /**
     * 高度
     */
    protected int mRectHeight;

    public Scroller mScroller;
    /**
     * 图片集合
     */
    private List<Bitmap> bitmapList = new ArrayList<>();
    //    private int subValue = CUtils.dip2px(40+10+10);
    private int subValue = CUtils.dip2px(40);

    private int startLineX = CUtils.dip2px(20 + 20);
    public int mScreenWidth;


    /**
     * 定时器
     */
    private Timer mTimer;
    private TimerTask mTimerTask;

    /**
     * 每一次位移的像素
     */
    private int mEachMovePx = 30;

    /**
     * 总共移动的距离 像素
     */
    public int movedDistancePx;

    /**
     * 画颜色矩形的paint
     */
    private Paint mColorRectPaint;
    /**
     * 阴影画笔
     */
    private Paint mShadowPaint;
    /**
     * 开始画颜色矩形的时候所处的位置X坐标
     */
    private int drawCurrentX;

    /**
     * 所画的颜色矩形的集合
     */
    private List<Rect> rectList = new ArrayList<>();

    /**
     * 颜色矩形 画笔集合
     */
    private List<Paint> colorPaintList = new ArrayList<>();


    /**
     * 初始化 是否需要 画颜色矩形
     */
    public boolean mNeedDrawColorRect = false;

    /**
     * 是否 开始 画颜色矩形
     */
    public boolean mBeginDrawColorRect = false;

    /**
     * 上次滚动后的  X坐标
     */
    protected int mScrollLastX;
    /**
     * 光标在最左边
     */
    private boolean isInLeft = true;

    /**
     * 光标在最右边
     */
    private boolean isInRight = false;
    /**
     * 控件移动的距离
     */
    private int selfMoveX = 0;

    /**
     * 控件自身上次移动的距离
     */
    private int lastSelfMoveX = 0;
    /**
     * 正在播放状态
     */
    private boolean isPlaying = false;

    public int leftMargin;

    /**
     * 颜色矩形的宽度
     */
    public int mItemRectWidth = 0;

    /**
     * 画颜色矩形时 所需的颜色
     */
    public int mColorId = 0;

    /**
     * 控件是否可以移动
     */
    private boolean mCanMoved = false;
    /**
     * 是否需要画播放光标
     */
    private boolean mNeedDrawPointer = false;

    /**
     * 阴影颜色
     */
    private int shadowColor = 0xAAAAAAAA;


    /**
     * 封面选择外边框框宽度
     */
    private int mCoverBorderWidth = 5;

    /**
     * 封面选择框宽度
     */
    private int mCoverRectWidth = 30;

    /**
     * 处于封面选择框状态
     */
    private boolean mIsCoverRect = false;

    /**
     * 封面矩形移动距离
     */
    private int mCoverRectMoveX = 0;
    private Paint mBitmapPaint;
    private Paint mPaintCoverOutBorder;
    private RectF mShadowRectF = new RectF();
    private RectF mCoverRect = new RectF();
    private RectF mCoverBorderRectF = new RectF();
    private float mTotalTime;
    private long mTimerEachTime;

    public HorizontalThumbnailView(Context context) {
        super(context);
        getScreenWidth(context);
        init(null);
    }

    public HorizontalThumbnailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getScreenWidth(context);
        init(attrs);
    }

    public HorizontalThumbnailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getScreenWidth(context);
        init(attrs);
    }

    public HorizontalThumbnailView(Context context, AttributeSet attrs, int defStyleAttr, int
            defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getScreenWidth(context);
        init(attrs);
    }

    protected void init(AttributeSet attrs) {
        // 获取自定义属性
        mScroller = new Scroller(getContext());
        // 缩略图画笔
        mBitmapPaint = new Paint();
        mBitmapPaint.setColor(Color.GRAY);
        // 抗锯齿
        mBitmapPaint.setAntiAlias(true);
        // 设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰
        mBitmapPaint.setDither(true);
        // 空心
        mBitmapPaint.setStyle(Paint.Style.STROKE);
        // 文字居中
        mBitmapPaint.setTextAlign(Paint.Align.CENTER);
        mShadowPaint = new Paint();
        mPaintCoverOutBorder = new Paint();

    }

    protected void initVar() {
        mPaintCoverOutBorder.setColor(mCoverOutBorderColor);
        if (mIsCoverRect) {
            subValue = 0;
            leftMargin = mCoverBorderWidth;
            mTenBitmapWidth = mScreenWidth - subValue - mCoverBorderWidth * 2;
            mRectHeight = 80;
            mRectWidth = (mTenBitmapWidth / bitmapList.size()) * (bitmapList.size());
            mTotalBitmapWidth = (mTenBitmapWidth / bitmapList.size()) * (bitmapList.size());
        } else {
            mTenBitmapWidth = mScreenWidth - subValue;
            mRectHeight = 80;
            mRectWidth = (mTenBitmapWidth / bitmapList.size()) * (bitmapList.size());
            mTotalBitmapWidth = (mTenBitmapWidth / bitmapList.size()) * (bitmapList.size());
            leftMargin = startLineX;
        }
        float eachTime = (mTotalTime * mEachMovePx) / mTotalBitmapWidth;
        mTimerEachTime = Float.valueOf(eachTime).longValue();
    }

    private void getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context
                .WINDOW_SERVICE);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        mScreenWidth = displaymetrics.widthPixels;
    }

    public void setInitValue(List<Bitmap> list, float totalTime, int startLineX, int pointerColor, int coverOutBorder) {
        bitmapList = list;
        mTotalTime = totalTime;
        this.startLineX = startLineX;
        this.mPointerColor = pointerColor;
        this.mCoverOutBorderColor = coverOutBorder;
        initVar();
    }

    /**
     * 初始化控件显示内容
     *
     * @param needDrawColorRect 画颜色矩形
     * @param needDrawPointer   画播放指针
     * @param isCoverRect       封面选择框矩形
     * @param canMoved          控件可以移动
     */
    public void setDrawWhat(boolean needDrawColorRect, boolean needDrawPointer, boolean
            isCoverRect, boolean canMoved) {
        mNeedDrawColorRect = needDrawColorRect;
        mNeedDrawPointer = needDrawPointer;
        mIsCoverRect = isCoverRect;
        mCanMoved = canMoved;
        initVar();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.makeMeasureSpec(mRectHeight + mCoverBorderWidth * 2, MeasureSpec
                .AT_MOST);
        super.onMeasure(widthMeasureSpec, height);
    }

    public int getTotalBitmapWidth() {
        return mTotalBitmapWidth;
    }

    public int getBitmapHeight() {
        return mRectHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画缩略图
        onDrawBtimap(canvas, mBitmapPaint);
        if (mNeedDrawPointer) {
            onDrawPlayPointer(canvas, mBitmapPaint);
        }

        if (mNeedDrawColorRect) {
            mColorRectPaint = new Paint();
            // 抗锯齿
            mColorRectPaint.setAntiAlias(true);
            // 设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰
            mColorRectPaint.setDither(true);
            mColorRectPaint.setColor(Color.GRAY);
            drawColorRect(canvas);
        }

        if (mIsCoverRect) {
            mShadowPaint.setColor(shadowColor);
            drawCoverRect(canvas);
            //绘制阴影
            drawShadow(canvas, mShadowPaint);
            drawCoverRectOutBorder(canvas, mPaintCoverOutBorder);
        }

    }


    private void drawColorRect(Canvas canvas) {
        if (rectList.size() > 0) {
            for (int i = 0; i < rectList.size(); i++) {
                canvas.drawRect(rectList.get(i), colorPaintList.get(i));
                canvas.save();
            }
        }
    }


    /**
     * 画缩略图
     *
     * @param canvas
     * @param paint
     */
    private void onDrawBtimap(Canvas canvas, Paint paint) {
        paint.setTextSize(mRectHeight / 4);
        RectF rect = new RectF();
        float bitmapWidth = mTenBitmapWidth / bitmapList.size();
        for (int i = 0; i < bitmapList.size(); i++) {
            rect.left = bitmapWidth * i + leftMargin;
            rect.top = mCoverBorderWidth;
            rect.right = bitmapWidth * (i + 1) + leftMargin;
            rect.bottom = mRectHeight;
            canvas.drawBitmap(bitmapList.get(i), null, rect, paint);
        }
    }


    /**
     * 绘制阴影
     *
     * @param canvas
     * @param paint
     */
    private void drawShadow(Canvas canvas, Paint paint) {
        mShadowRectF.left = leftMargin;
        mShadowRectF.top = mCoverBorderWidth;
        mShadowRectF.right = leftMargin + mRectWidth;
        mShadowRectF.bottom = mRectHeight;
        canvas.drawRect(mShadowRectF, paint);

    }

    /**
     * 绘制封面矩形内部
     *
     * @param canvas
     */
    private void drawCoverRect(Canvas canvas) {
        mCoverRect.left = leftMargin + mCoverRectMoveX;
        mCoverRect.top = mCoverBorderWidth;
        mCoverRect.right = leftMargin + mCoverRectWidth + mCoverRectMoveX;
        mCoverRect.bottom = mRectHeight;
        canvas.clipRect(mCoverRect, Region.Op.DIFFERENCE);
    }


    /**
     * 绘制封面矩形边框
     *
     * @param canvas
     * @param paintCover
     */
    private void drawCoverRectOutBorder(Canvas canvas, Paint paintCover) {
        mCoverBorderRectF.left = leftMargin - mCoverBorderWidth + mCoverRectMoveX;
        mCoverBorderRectF.top = 0;
        mCoverBorderRectF.right = leftMargin + mCoverRectWidth + mCoverBorderWidth +
                mCoverRectMoveX;
        mCoverBorderRectF.bottom = mRectHeight + mCoverBorderWidth;
        canvas.drawRect(mCoverBorderRectF, paintCover);
    }

    /**
     * 画播放指针
     *
     * @param canvas
     * @param paint
     */
    private void onDrawPlayPointer(Canvas canvas, Paint paint) {
        paint.setColor(mPointerColor);
        paint.setStrokeWidth(6);
        canvas.drawLine(leftMargin + movedDistancePx, 0,
                leftMargin + movedDistancePx, mRectHeight, paint);
    }

    private int mPointerColor = Color.RED;
    private int mCoverOutBorderColor = Color.GREEN;

    /**
     * 开始画颜色矩形
     *
     * @param colorId
     */
    public void startDrawColorRect(int colorId) {
        mColorId = colorId;
        mColorRectPaint.setColor(mColorId);
        colorPaintList.add(mColorRectPaint);
        mBeginDrawColorRect = true;
        drawCurrentX = movedDistancePx;
        if (drawCurrentX == mTotalBitmapWidth) {
            drawCurrentX = 0;

        }
        Rect rect = new Rect();
        rect.left = drawCurrentX + leftMargin;
        rect.top = 0;
        rect.right = drawCurrentX + leftMargin + mItemRectWidth;
        rect.bottom = mRectHeight;
        rectList.add(rect);
        invalidate();
    }

    /**
     * 停止画颜色矩形
     */
    public void stopDrawColorRect() {
        mBeginDrawColorRect = false;
        stopPlay();
    }

    /**
     * 删除 最近一次画的颜色矩形
     */
    public void removeColorRect() {
        stopPlay();
        if (rectList.size() > 0) {
            int size = rectList.size() - 1;
            Rect rectRemove = rectList.get(size);
            scrollBy(-movedDistancePx + (rectRemove.left - leftMargin), 0);
            if (mScrollListener != null) {
                mScrollListener.onScaleScroll(-movedDistancePx + (rectRemove.left - leftMargin));
            }
            movedDistancePx = movedDistancePx + (-movedDistancePx + (rectRemove.left - leftMargin));
            selfMoveX = movedDistancePx;
            if (selfMoveX > 0) {
                isInLeft = false;
            }
            rectList.remove(size);
            if (mOnSelfScrollingListener != null) {
                mOnSelfScrollingListener.onSelfScrolling(movedDistancePx);
            }
            invalidate();
        }

    }


    private OnDistanceChangeListener mScrollListener;

    public interface OnDistanceChangeListener {
        void onScaleScroll(int scale);
    }

    /**
     * 设置滑动距离改变监听接口
     *
     * @param onSlideListener
     */
    public void setOnDistanceChangeListener(OnDistanceChangeListener onSlideListener) {
        this.mScrollListener = onSlideListener;
    }


    private OnSelfScrollingListener mOnSelfScrollingListener;

    public interface OnSelfScrollingListener {
        void onSelfScrolling(int moveDistance);
    }

    /**
     * 设置控件滚动距离监听
     *
     * @param onSelfScrollingListener
     */
    public void setOnSelfScrollingListener(OnSelfScrollingListener onSelfScrollingListener) {
        this.mOnSelfScrollingListener = onSelfScrollingListener;

    }


    private OnCoverRectMoveListener mOnCoverRectMoveListener;

    public interface OnCoverRectMoveListener {
        void onCoverRectMove(int movex, int coverRectWidth);
    }

    /**
     * 设置封面选择视图 矩形移动监听
     *
     * @param onCoverRectMoveListener
     */
    public void setOnCoverRectMoveListener(OnCoverRectMoveListener onCoverRectMoveListener) {
        this.mOnCoverRectMoveListener = onCoverRectMoveListener;
    }


    private OnPlayPointerChangeListener mOnPlayPointerChangeListener;

    public interface OnPlayPointerChangeListener {
        void onPlayPointerChange(int moveDistance);
    }

    /**
     * 设置播放指针 位置改变监听
     *
     * @param onPlayPointerChangeListener
     */
    public void setOnPlayPointerChangeListener(OnPlayPointerChangeListener
                                                       onPlayPointerChangeListener) {
        this.mOnPlayPointerChangeListener = onPlayPointerChangeListener;
    }

    private OnStopPlayListener mOnStopPlayListener;

    public interface OnStopPlayListener {
        void stopPlay();
    }

    /**
     * 设置停止播放监听
     *
     * @param onStopPlayListener
     */
    public void setOnStopPlayListener(OnStopPlayListener onStopPlayListener) {
        this.mOnStopPlayListener = onStopPlayListener;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();//获取触摸位置
        if (isPlaying) stopPlay();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (mNeedDrawPointer) { //有播放指针时
                    if ((int) event.getX() < leftMargin) {
                        movedDistancePx = 0;
                    } else if ((int) event.getX() > mTotalBitmapWidth) {
                        movedDistancePx = mTotalBitmapWidth;
                    } else {
                        movedDistancePx = (int) event.getX();
                    }
                    if (mOnPlayPointerChangeListener != null) {
                        mOnPlayPointerChangeListener.onPlayPointerChange(movedDistancePx);
                    }
                    invalidate();
                    return true;
                } else if (mIsCoverRect) {
                    if ((int) event.getX() < leftMargin) {
                        movedDistancePx = 0;
                    } else if ((int) event.getX() > mTotalBitmapWidth - mCoverRectWidth) {
                        mCoverRectMoveX = mTotalBitmapWidth - mCoverRectWidth;
                    } else {
                        mCoverRectMoveX = (int) event.getX();
                    }
                    if (mOnCoverRectMoveListener != null) {
                        mOnCoverRectMoveListener.onCoverRectMove(mCoverRectMoveX, mCoverRectWidth);
                    }
                    invalidate();
                    return true;

                } else {

                    if (mCanMoved) {
                        if (mScroller != null && !mScroller.isFinished()) {
                            mScroller.abortAnimation();
                        }
                        mScrollLastX = x;
                        return true;
                    }
                }

            case MotionEvent.ACTION_MOVE:
                if (mNeedDrawPointer) {
                    if ((int) event.getX() < leftMargin) {
                        movedDistancePx = 0;
                    } else if ((int) event.getX() > mTotalBitmapWidth) {
                        movedDistancePx = mTotalBitmapWidth;
                    } else {
                        movedDistancePx = (int) event.getX();
                    }
                    if (mOnPlayPointerChangeListener != null) {
                        mOnPlayPointerChangeListener.onPlayPointerChange(movedDistancePx);
                    }
                    invalidate();
                    return false;
                } else if (mIsCoverRect) {
                    if ((int) event.getX() < leftMargin) {
                        mCoverRectMoveX = 0;
                    } else if ((int) event.getX() > mTotalBitmapWidth - mCoverRectWidth) {
                        mCoverRectMoveX = mTotalBitmapWidth - mCoverRectWidth;
                    } else {
                        mCoverRectMoveX = (int) event.getX();
                    }
                    if (mOnCoverRectMoveListener != null) {
                        mOnCoverRectMoveListener.onCoverRectMove(mCoverRectMoveX, mCoverRectWidth);
                    }
                    invalidate();
                    return true;
                } else {
                    if (mCanMoved) {
                        int dataX = mScrollLastX - x;
                        if (isInLeft) {
                            if (dataX <= 0) {
                                movedDistancePx = 0;
                            } else {
                                movedDistancePx = selfMoveX = selfMoveX + dataX;
                                scrollBy(dataX, 0);
                                if (mScrollListener != null) {
                                    mScrollListener.onScaleScroll(dataX);
                                }
                                isInLeft = false;
                                mScrollLastX = x;
                            }
                        } else {
                            if (isInRight) {
                                if (dataX < 0) {
                                    movedDistancePx = selfMoveX = selfMoveX + dataX;

                                    if (selfMoveX < 0) {
                                        scrollBy(dataX - selfMoveX, 0);
                                        if (mScrollListener != null) {
                                            mScrollListener.onScaleScroll(dataX - selfMoveX);
                                        }
                                        isInLeft = true;
                                        isInRight = false;
                                        mScrollLastX = x;
                                    } else {
                                        isInRight = false;
                                        scrollBy(dataX, 0);
                                        if (mScrollListener != null) {
                                            mScrollListener.onScaleScroll(dataX);
                                        }
                                        mScrollLastX = x;
                                    }
                                }
                            } else {
                                if (dataX <= 0) {
                                    lastSelfMoveX = selfMoveX;
                                    movedDistancePx = selfMoveX = lastSelfMoveX + dataX;
                                    if (selfMoveX < 0) {
                                        scrollBy(-lastSelfMoveX, 0);
                                        if (mScrollListener != null) {
                                            mScrollListener.onScaleScroll(-lastSelfMoveX);
                                        }
                                        movedDistancePx = selfMoveX = 0;
                                        isInLeft = true;
                                        isInRight = false;
                                        mScrollLastX = x;
                                    } else {
                                        scrollBy(dataX, 0);
                                        if (mScrollListener != null) {
                                            mScrollListener.onScaleScroll(dataX);
                                        }
                                        mScrollLastX = x;
                                    }
                                } else {

                                    movedDistancePx = selfMoveX = selfMoveX + dataX;
                                    if (selfMoveX > mTotalBitmapWidth) {
                                        scrollBy(dataX - (selfMoveX - mTotalBitmapWidth), 0);
                                        if (mScrollListener != null) {
                                            mScrollListener.onScaleScroll(dataX - (selfMoveX -
                                                    mTotalBitmapWidth));
                                        }
                                        movedDistancePx = selfMoveX = mTotalBitmapWidth;
                                        isInRight = true;
                                        isInLeft = false;
                                        mScrollLastX = x;
                                    } else {
                                        scrollBy(dataX, 0);
                                        if (mScrollListener != null) {
                                            mScrollListener.onScaleScroll(dataX);
                                        }
                                        mScrollLastX = x;
                                    }
                                }
                            }
                        }
                        if (mOnSelfScrollingListener != null) {
                            mOnSelfScrollingListener.onSelfScrolling(movedDistancePx);
                        }
                        invalidate();
                        return true;
                    }
                }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                return false;
        }
        return false;
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (movedDistancePx <= mTotalBitmapWidth) {
                    if (mTotalBitmapWidth - movedDistancePx < mEachMovePx) {
                        if (mCanMoved) {
                            scrollBy(mTotalBitmapWidth - movedDistancePx, 0);
                            if (mScrollListener != null) {
                                mScrollListener.onScaleScroll(mTotalBitmapWidth - movedDistancePx);
                            }
                            if (mBeginDrawColorRect) {
                                stopPlay();
                                if (rectList.size() > 0) {
                                    if ((rectList.get(rectList.size() - 1).width() + rectList.get
                                            (rectList.size() - 1).left - leftMargin) >=
                                            mTotalBitmapWidth) {

                                    } else {
                                        rectList.get(rectList.size() - 1).right = rectList.get
                                                (rectList.size() - 1).right + mTotalBitmapWidth -
                                                movedDistancePx;
                                    }
                                }
                                invalidate();
                                movedDistancePx = movedDistancePx + mTotalBitmapWidth -
                                        movedDistancePx;
                                selfMoveX = movedDistancePx;
                            } else {
                                invalidate();
                                movedDistancePx = movedDistancePx + mTotalBitmapWidth -
                                        movedDistancePx;
                                selfMoveX = movedDistancePx;
                                resetPlay();
                            }
                        } else if (mNeedDrawPointer) {
                            stopPlay();
                            invalidate();
                            movedDistancePx = movedDistancePx + mTotalBitmapWidth -
                                    movedDistancePx;
                            selfMoveX = movedDistancePx;
                        }
                    } else {
                        if (mCanMoved) {
                            scrollBy(mEachMovePx, 0);
                            if (mScrollListener != null) {
                                mScrollListener.onScaleScroll(mEachMovePx);
                            }
                            if (mBeginDrawColorRect) {
                                if (rectList.size() > 0) {
                                    if ((rectList.get(rectList.size() - 1).width() + rectList.get
                                            (rectList.size() - 1).left - leftMargin) >=
                                            mTotalBitmapWidth) {
                                        stopPlay();
                                    } else {
                                        rectList.get(rectList.size() - 1).right = rectList.get
                                                (rectList.size() - 1).right + mEachMovePx;
                                    }
                                }
                            }
                        }
                        invalidate();
                        movedDistancePx = movedDistancePx + mEachMovePx;
                        selfMoveX = movedDistancePx;
                    }
                }
            }
            super.handleMessage(msg);
        }
    };


    /**
     * 复位
     */
    public void resetPlay() {
        if (mCanMoved) {
            scrollBy(-mTotalBitmapWidth, 0);
            if (mScrollListener != null) {
                mScrollListener.onScaleScroll(-mTotalBitmapWidth);
            }
        }
        invalidate();
        movedDistancePx = 0;
    }

    /**
     * 点击播放时， 开始滚动
     */
    public void startPlay() {
        Log.i("movedDistancePx", movedDistancePx + "======movedDistancePx");
        if (movedDistancePx == mRectWidth) {
            resetPlay();
        }
        isPlaying = true;
        //进入时，点击播放，移动的时候光标不在左边
        if (isInLeft) {
            isInLeft = !isInLeft;
        }
        if (isInRight) {
            isInRight = !isInRight;
        }

        if (mTimer == null || mTimerTask == null) {
            initTimer();
            mTimer.schedule(mTimerTask, 0, mTimerEachTime);
        }
    }

    /**
     * 用户点击停止播放时，停止滚动
     */
    public void stopPlay() {
        isPlaying = false;
        mBeginDrawColorRect = false;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (mOnStopPlayListener != null) {
            mOnStopPlayListener.stopPlay();
        }
    }

    /**
     * 初始化定时器
     */
    public void initTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            };
        }
    }

    /**
     * 使用Scroller时需重写
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        // 判断Scroller是否执行完毕
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            // 通过重绘来不断调用computeScroll
            invalidate();
        }
    }


    public void moveToPercent(float percent) {
    }
}
