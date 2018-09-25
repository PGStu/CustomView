package com.pg.custom.application.playview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.pg.custom.application.CUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放缩略图控件
 */

public class PlayView extends FrameLayout {

    private Context mContext;

    // 持续时间
    private long mDurationTimeUs;
    // 是否隐藏Bar
    private boolean mShadowBar = true;

    private List<Bitmap> mBitmapList = new ArrayList<>();
    private RangeSelectionBar mRangeSelectionBar;

    /**
     * 时间轴总长
     */
    private int mTotalWidth = 0;
    /**
     * Bar宽度
     */
    private int mBarWidth = CUtils.dip2px(20);
    /**
     * Bar高度
     */
    private int selectionBarHeight = 0;
    /**
     * 缩略图列表滑动距离 X方向
     */
    private int mScrollDX = 0;
    /**
     * 外边框高度
     */
    private int mOutLineWidth = CUtils.dip2px(4);
    private int mScreenWidth;
    /**
     * 在父控件中的起始位置
     */
    private int mInParentLeft = 0;
    public HorizontalThumbnailView horizontalThumbnailView;
    private PlayCursorView playCursorView;

    /**
     * 光标颜色
     */
    private int mPointerColorId = Color.RED;
    /**
     * 封面矩形选择框颜色
     */
    private int mCoverOutBorderColor = Color.GREEN;

    /**
     * 播放指针所在位置的百分比
     */
    private float mPlayPointerPositionTimePercent;
    /**
     * 播放指针所在位置对应的时间
     */
    private float mPlayPointerPositionTime;

    /**
     * 控件播放到的时间的百分比
     */
    private float playPositionTimePercent;
    /**
     * 控件播放到的时间
     */
    private float playPositionTime;

    /**
     * 封面矩形选择框起始时间百分比
     */
    private float mCoverStartTimePercent;
    /**
     * 封面矩形选择框结束时间百分比
     */
    private float mCoverEndTimePercent;
    /**
     * 封面矩形选择框起始时间
     */
    private float mCoverStartTime;
    /**
     * 封面矩形选择框结束时间
     */
    private float mCoverEndTime;

    /**
     * 左右bar选择的时间长百分比
     */
    private float mSelectTimePercent;
    /**
     * 左右bar选择的起始时间百分比
     */
    private float mStartTimePercent;
    /**
     * 左右bar选择的结束时间百分比
     */
    private float mEndTimePercent;
    /**
     * 左右bar选择的起始时间
     */
    private float mStartTime;
    /**
     * 左右bar选择的结束时间
     */
    private float mEndTime;
    /**
     * 左右bar选择的时间长
     */
    private float mSelectTime;
    private LineViewType mLineViewType;
    private float mTotalTime;

    /**
     * DrawColorRect  画颜色矩形方块  如魔法特效时
     * DrawPointer  画可移动的播放指针   第一次编辑时
     * IsCoverRect  画封面选择矩形
     * CanRolling   指示针固定（如固定于屏幕中央） 控件可移动
     */
    public enum LineViewType {
        DrawColorRect,
        DrawPointer,
        IsCoverRect,
        CanRolling
    }

    public PlayView(@NonNull Context context) {
        this(context, null);
    }

    public PlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mScreenWidth = getScreenWidth(context);
    }

    private void init() {
        if (mBitmapList.size() > 0 && mTotalTime > 0) {
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.topMargin = mOutLineWidth;
            horizontalThumbnailView = new HorizontalThumbnailView(mContext);
            horizontalThumbnailView.setInitValue(mBitmapList, mTotalTime, mScreenWidth / 2, mPointerColorId, mCoverOutBorderColor);
            horizontalThumbnailView.setLayoutParams(lp);

            horizontalThumbnailView.setDrawWhat(true, false, false, true);//画颜色矩形
            mTotalWidth = horizontalThumbnailView.getTotalBitmapWidth();
            selectionBarHeight = horizontalThumbnailView.getBitmapHeight();

            playCursorView = new PlayCursorView(mContext);
            playCursorView.setValue(mTotalWidth, selectionBarHeight, mOutLineWidth, mScreenWidth / 2, mPointerColorId);
            playCursorView.setLayoutParams(lp);

            // 增加两个Bar
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            lp.gravity = Gravity.CENTER_VERTICAL;
            mRangeSelectionBar = new RangeSelectionBar(mContext);
            mRangeSelectionBar.setValue(mTotalWidth, selectionBarHeight, mScreenWidth / 2 - CUtils.dip2px(20));
            mRangeSelectionBar.setLayoutParams(lp);
            horizontalThumbnailView.setOnStopPlayListener(new HorizontalThumbnailView.OnStopPlayListener() {
                @Override
                public void stopPlay() {
                    if (mOnStopPlayListener != null) {
                        mOnStopPlayListener.stopPlay();
                    }
                }
            });

            if (LineViewType.DrawColorRect == mLineViewType) {//画颜色矩形
                horizontalThumbnailView.setInitValue(mBitmapList, mTotalTime, mScreenWidth / 2 - mInParentLeft, mPointerColorId, mCoverOutBorderColor);
                playCursorView.setValue(mTotalWidth, selectionBarHeight, mOutLineWidth, mScreenWidth / 2 - mInParentLeft, mPointerColorId);
                this.addView(horizontalThumbnailView);
                this.addView(playCursorView);
                horizontalThumbnailView.setOnSelfScrollingListener(new HorizontalThumbnailView.OnSelfScrollingListener() {
                    @Override
                    public void onSelfScrolling(int moveDistance) {
                        playPositionTimePercent = moveDistance / (float) mTotalWidth;
                        playPositionTime = (moveDistance / (float) mTotalWidth) * mTotalTime;
                        if (mOnScrollingPlayPositionListener != null) {
                            mOnScrollingPlayPositionListener.onPlayPosition(playPositionTime, playPositionTimePercent);
                        }
                    }
                });
            }

            if (LineViewType.DrawPointer == mLineViewType) {//画播放指針
                horizontalThumbnailView.setInitValue(mBitmapList, mTotalTime, CUtils.dip2px(20), mPointerColorId, mCoverOutBorderColor);
//            horizontalPlayViewOne.setInitValue(CUtils.dip2px(20),mPointerColorId,mCoverOutBorderColor);
//            horizontalPlayViewOne.setBitmapList(mBitmapList);
//            horizontalPlayViewOne.setTotalTime(mTotalTime);
                mRangeSelectionBar.setValue(mTotalWidth, selectionBarHeight, 0);
                horizontalThumbnailView.setDrawWhat(false, true, false, false);
                this.addView(horizontalThumbnailView);
                this.addView(mRangeSelectionBar);
                mRangeSelectionBar.setOnBarMoveListener(new RangeSelectionBar.OnBarMoveListener() {
                    @Override
                    public void onBarMove(int leftBarX, int rightBarX) {
                        //选择了多长时间
                        mSelectTimePercent = (mTotalWidth - leftBarX - rightBarX) / (float) mTotalWidth;
                        mSelectTime = mSelectTimePercent * mTotalTime;
                        //起始时间位置
                        mStartTimePercent = leftBarX / (float) mTotalWidth;
                        mStartTime = mStartTimePercent * mTotalTime;
                        //结束时间位置
                        mEndTimePercent = (mTotalWidth - rightBarX) / (float) mTotalWidth;
                        mEndTime = mEndTimePercent * mTotalTime;
                        if (mOnSelectTimeChangeListener != null) {
                            mOnSelectTimeChangeListener.onTimeChange(mStartTime, mEndTime, mSelectTime, mStartTimePercent, mEndTimePercent, mSelectTimePercent);
                        }
                    }
                });

                horizontalThumbnailView.setOnPlayPointerChangeListener(new HorizontalThumbnailView.OnPlayPointerChangeListener() {
                    @Override
                    public void onPlayPointerChange(int moveDistance) {
                        mPlayPointerPositionTimePercent = moveDistance / (float) mTotalWidth;
                        mPlayPointerPositionTime = mPlayPointerPositionTimePercent * mTotalTime;
                        if (mOnPlayPointerChangeListener != null) {
                            mOnPlayPointerChangeListener.onPlayPointerPosition(mPlayPointerPositionTime, mPlayPointerPositionTimePercent);
                        }
                    }
                });
            }

            if (LineViewType.IsCoverRect == mLineViewType) {//画封面选择视图
                horizontalThumbnailView.setDrawWhat(false, false, true, false);
                this.addView(horizontalThumbnailView);
                mTotalWidth = horizontalThumbnailView.getTotalBitmapWidth();
                horizontalThumbnailView.setOnCoverRectMoveListener(new HorizontalThumbnailView.OnCoverRectMoveListener() {
                    @Override
                    public void onCoverRectMove(int movex, int coverRectWidth) {
                        mCoverStartTimePercent = movex / (float) mTotalWidth;
                        mCoverStartTime = mCoverStartTimePercent * mTotalTime;
                        mCoverEndTimePercent = (movex + coverRectWidth) / (float) mTotalWidth;
                        mCoverEndTime = mCoverEndTimePercent * mTotalTime;
                        if (mOnSelectCoverTimeListener != null) {
                            mOnSelectCoverTimeListener.onCoverSelectTime(mCoverStartTime, mCoverEndTime, mCoverStartTimePercent, mCoverEndTimePercent);
                        }
                    }
                });
            }
            if (LineViewType.CanRolling == mLineViewType) { //可滚动 有左右bar
                horizontalThumbnailView.setInitValue(mBitmapList, mTotalTime, mScreenWidth / 2 - mInParentLeft, mPointerColorId, mCoverOutBorderColor);
                playCursorView.setValue(mTotalWidth, selectionBarHeight, mOutLineWidth, mScreenWidth / 2 - mInParentLeft, mPointerColorId);
                mRangeSelectionBar.setValue(mTotalWidth, selectionBarHeight, mScreenWidth / 2 - CUtils.dip2px(20) - mInParentLeft);
                horizontalThumbnailView.setDrawWhat(false, false, false, true);
                this.addView(horizontalThumbnailView);
                this.addView(mRangeSelectionBar);
                this.addView(playCursorView);
                horizontalThumbnailView.setOnDistanceChangeListener(new HorizontalThumbnailView
                        .OnDistanceChangeListener() {
                    @Override
                    public void onScaleScroll(int scale) {
                        mRangeSelectionBar.scrollBy(scale, 0);
                        mRangeSelectionBar.setDisatnce(horizontalThumbnailView.movedDistancePx);
                    }
                });
                horizontalThumbnailView.setOnSelfScrollingListener(new HorizontalThumbnailView.OnSelfScrollingListener() {
                    @Override
                    public void onSelfScrolling(int moveDistance) {
                        playPositionTimePercent = moveDistance / (float) mTotalWidth;
                        playPositionTime = (moveDistance / (float) mTotalWidth) * mTotalTime;
                        if (mOnScrollingPlayPositionListener != null) {
                            mOnScrollingPlayPositionListener.onPlayPosition(playPositionTime, playPositionTimePercent);
                        }
                    }
                });
                mRangeSelectionBar.setOnBarMoveListener(new RangeSelectionBar.OnBarMoveListener() {
                    @Override
                    public void onBarMove(int leftBarX, int rightBarX) {
                        //选择了多长时间
                        mSelectTimePercent = (mTotalWidth - leftBarX - rightBarX) / (float) mTotalWidth;
                        mSelectTime = mSelectTimePercent * mTotalTime;
                        //起始时间位置
                        mStartTimePercent = leftBarX / (float) mTotalWidth;
                        mStartTime = mStartTimePercent * mTotalTime;
                        //结束时间位置
                        mEndTimePercent = (mTotalWidth - rightBarX) / (float) mTotalWidth;
                        mEndTime = mEndTimePercent * mTotalTime;
                        if (mOnSelectTimeChangeListener != null) {
                            mOnSelectTimeChangeListener.onTimeChange(mStartTime, mEndTime, mSelectTime, mStartTimePercent, mEndTimePercent, mSelectTimePercent);
                        }
                    }
                });
            }
        }
    }


    /**
     * 防止重绘
     */
    private boolean isFirstIn = false;

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!isFirstIn && mBitmapList.size() > 0 && mTotalTime > 0) {
            int[] location = new int[2];
            getLocationOnScreen(location);
            mInParentLeft = location[0];
            init();
            isFirstIn = true;
        }
    }

    private OnSelectTimeChangeListener mOnSelectTimeChangeListener;

    public interface OnSelectTimeChangeListener {
        void onTimeChange(float startTime, float endTime, float selectTime, float startTimePercent, float endTimePercent, float selectTimePercent);
    }

    /**
     * bar 移动之后 选择的时间范围
     *
     * @param onSelectTimeChangeListener
     */
    public void setOnSelectTimeChangeListener(OnSelectTimeChangeListener onSelectTimeChangeListener) {
        this.mOnSelectTimeChangeListener = onSelectTimeChangeListener;
    }


    private OnSelectCoverTimeListener mOnSelectCoverTimeListener;

    public interface OnSelectCoverTimeListener {
        //封面选择时间
        void onCoverSelectTime(float startTime, float endTime, float startTimePercent, float endTimePercent);
    }

    /**
     * 封面视图下 选择的封面矩形的时间
     *
     * @param onSelectCoverTimeListener
     */
    public void setOnSelectCoverTimeListener(OnSelectCoverTimeListener onSelectCoverTimeListener) {
        this.mOnSelectCoverTimeListener = onSelectCoverTimeListener;
    }

    private OnScrollingPlayPositionListener mOnScrollingPlayPositionListener;

    public interface OnScrollingPlayPositionListener {
        //封面选择时间
        void onPlayPosition(float playPositionTime, float playPositionTimePercent);
    }

    /**
     * 滚动时 播放位置
     *
     * @param onScrollingDistanceListener
     */
    public void setOnScrollingPlayPositionListener(OnScrollingPlayPositionListener onScrollingDistanceListener) {
        this.mOnScrollingPlayPositionListener = onScrollingDistanceListener;
    }

    //滚动时 播放位置
    private OnPlayPointerChangeListener mOnPlayPointerChangeListener;

    public interface OnPlayPointerChangeListener {
        //封面选择时间
        void onPlayPointerPosition(float playPointerPositionTime, float playPointerPositionTimePercent);
    }

    /**
     * 播放指针 位置改变监听
     *
     * @param onPlayPointerChangeListener
     */
    public void setOnPlayPointerChangeListener(OnPlayPointerChangeListener onPlayPointerChangeListener) {
        this.mOnPlayPointerChangeListener = onPlayPointerChangeListener;
    }


    private OnStopPlayListener mOnStopPlayListener;

    public interface OnStopPlayListener {
        void stopPlay();
    }

    /**
     * 停止播放监听
     *
     * @param onStopPlayListener
     */
    public void setOnStopPlayListener(OnStopPlayListener onStopPlayListener) {
        this.mOnStopPlayListener = onStopPlayListener;
    }


    public void moveToPercent(float percent) {
        horizontalThumbnailView.moveToPercent(percent);
    }


    /**
     * 初始化
     *
     * @param initType
     * @param totalTime      总时间
     * @param bitmapList     缩略图集合
     * @param pointerColorId 指针颜色
     */
    public void setInitType(LineViewType initType, float totalTime, List<Bitmap> bitmapList, int pointerColorId) {
        mLineViewType = initType;
        mTotalTime = totalTime;
        this.mBitmapList = bitmapList;
        mPointerColorId = pointerColorId;
    }

    /**
     * 初始化
     *
     * @param initType
     * @param pointerColorId 指针颜色
     */
    public void setInitType(LineViewType initType, int pointerColorId) {
        mLineViewType = initType;
        mPointerColorId = pointerColorId;
    }

    /**
     * 设置缩略图
     *
     * @param bitmapList
     */
    public void setBitmapList(List<Bitmap> bitmapList) {
        this.mBitmapList = bitmapList;
        onWindowFocusChanged(false);
    }

    /**
     * 设置总时间
     *
     * @param totalTime
     */
    public void setTotalTime(float totalTime) {
        mTotalTime = totalTime;
        onWindowFocusChanged(false);
    }

    /**
     * 封面矩形选择框颜色
     *
     * @param coverOutBorderColor
     */
    public void setCoverOutBorderColor(int coverOutBorderColor) {
        mCoverOutBorderColor = coverOutBorderColor;
    }


    /**
     * 点击播放时， 开始滚动
     */
    public void startPlay() {
        horizontalThumbnailView.startPlay();
    }

    /**
     * 开始画色
     */
    public void starDrawColor(int colorId) {
        horizontalThumbnailView.startDrawColorRect(colorId);
        horizontalThumbnailView.startPlay();
    }

    /**
     * 停止画色
     */
    public void stopDrawColor() {
        horizontalThumbnailView.stopDrawColorRect();
    }

    /**
     * 删除画色
     */
    public void removeColor() {
        horizontalThumbnailView.removeColorRect();
    }

    /**
     * 用户点击停止播放时，停止滚动
     */
    public void onPlayStop() {

        horizontalThumbnailView.stopPlay();
    }

    private int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context
                .WINDOW_SERVICE);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }
}
