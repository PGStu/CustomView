package com.pg.custom.application.playview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Scroller;

/**
 * 固定位置光标
 */

public class PlayCursorView extends View {

    private int leftMargin = 0;
    Paint mLinePaint = new Paint();
    public PlayCursorView(Context context) {
        super(context);
        init(context);
    }

    public PlayCursorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public PlayCursorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public PlayCursorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int
            defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }


    private void init(Context context){
        mScroller = new Scroller(getContext());
        // 抗锯齿
        mLinePaint.setAntiAlias(true);
        // 设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰
        mLinePaint.setDither(true);
        // 空心
        mLinePaint.setStyle(Paint.Style.STROKE);

        mLinePaint.setStrokeWidth(6);
    }
    private int movedDistancePx = 0;  //移动的像素
    private int mLineHeight = 0;  //播放光标的高度
    private int mOutLineWidth = 0;  //外边框高度
    private int mColorId = Color.RED;
    public void setValue(int totaDistance,int height,int outLineWidth,int leftMargin,int colorId){
        mLineHeight = height;
        mOutLineWidth = outLineWidth;
        this.leftMargin=leftMargin;
        mColorId=colorId;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        onDrawPlayPointer(canvas, mLinePaint);
    }


    /**
     * 画播放指针
     *
     * @param canvas
     * @param paint
     */
    private void onDrawPlayPointer(Canvas canvas, Paint paint) {
        mLinePaint.setColor(mColorId);
        canvas.drawLine(leftMargin + movedDistancePx, 0,
                leftMargin + movedDistancePx, mLineHeight, paint);
    }

    public Scroller mScroller;


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


}
