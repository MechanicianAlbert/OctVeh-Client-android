package com.octech.octvehclient.rocker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashSet;
import java.util.Set;



/**
 * Created by Albert on 2017/6/28
 */
public class RockerView extends View {

    public interface OnRockListener {
        void onRock(float ratioX, float ratioY);
        void onReset();
    }


    private final Set<OnRockListener> LISTENERS = new HashSet<>();
    private Thread mNotifyThread = new Thread() {
        @Override
        public void run() {
            try {
                boolean lastIsOffset = true;
                while (ViewCompat.isAttachedToWindow(RockerView.this)) {
                    if (mIsTouching) {
                        for (OnRockListener listener : LISTENERS) {
                            listener.onRock(mBallRatioX, mBallRatioY);
                        }
                    } else {
                        if (lastIsOffset) {
                            for (OnRockListener listener : LISTENERS) {
                                listener.onReset();
                            }
                        }
                    }
                    lastIsOffset = mIsTouching;
                    sleep(20);
                }
                clearOnRockListener();
                mNotifyThread = null;
                interrupt();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    private Paint mPaint;
    private int mCircleColor = Color.parseColor("#FF777777");
    private int mLineColor = Color.parseColor("#FF000000");
    private int mBallColor = Color.parseColor("#FFFF0000");

    private float mCircleCenterX;
    private float mCircleCenterY;
    private float mCircleRadius;
    private float mBallRadius;
    private float mBallCenterX;
    private float mBallCenterY;
    private float mRangeRadius;
    private float mHandleWidth;

    private boolean mIsTouching;
    private float mBallRatioX;
    private float mBallRatioY;


    public RockerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
    }

    private void initAttrs(AttributeSet attrs) {

    }

    private void init() {
        initPaint();
        initDimensions();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    private void initDimensions() {
        mCircleCenterX = getMeasuredWidth() / 2.0f;
        mCircleCenterY = getMeasuredHeight() / 2.0f;
        mCircleRadius = Math.min(mCircleCenterX, mCircleCenterY) * 2 / 3;
        mBallCenterX = mCircleCenterX;
        mBallCenterY = mCircleCenterY;
        mBallRadius = mCircleRadius / 2;
        mHandleWidth = (int) (mBallRadius / 2);
        mRangeRadius = mCircleRadius;
        mPaint.setStrokeWidth(mHandleWidth);
    }

    private void calcOffsetAndRatio(float x, float y) {
        float touchOffsetX = x - mCircleCenterX;
        float touchOffsetY = y - mCircleCenterY;
        float touchOffset = (float) Math.pow(Math.pow(touchOffsetX, 2) + Math.pow(touchOffsetY, 2), 0.5);
        float touchRatio = touchOffset / mRangeRadius;
        mBallCenterX = touchOffsetX / Math.max(touchRatio, 1) + mCircleCenterX;
        mBallCenterY = touchOffsetY / Math.max(touchRatio, 1) + mCircleCenterY;
        float ballOffsetX = mBallCenterX - mCircleCenterX;
        float ballOffsetY = mBallCenterY - mCircleCenterY;
        mBallRatioX = ballOffsetX / mRangeRadius;
        mBallRatioY = ballOffsetY / mRangeRadius;
    }


    public void addOnRockListener(OnRockListener onRockListener) {
        LISTENERS.add(onRockListener);
    }

    public void removeOnRockListener(OnRockListener onRockListener) {
        LISTENERS.remove(onRockListener);
    }

    public void clearOnRockListener() {
        LISTENERS.clear();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mNotifyThread.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(mCircleColor);
        canvas.drawCircle(mCircleCenterX, mCircleCenterY, mCircleRadius, mPaint);
        mPaint.setColor(mLineColor);
        canvas.drawCircle((mBallCenterX + mCircleCenterX * 2) / 3, (mBallCenterY + mCircleCenterY * 2) / 3, mHandleWidth / 2, mPaint);
        canvas.drawLine((mBallCenterX + mCircleCenterX * 2) / 3, (mBallCenterY + mCircleCenterY * 2) / 3, mBallCenterX, mBallCenterY, mPaint);
        mPaint.setColor(mBallColor);
        canvas.drawCircle(mBallCenterX, mBallCenterY, mBallRadius, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mIsTouching = event.getAction() != MotionEvent.ACTION_UP;
        float x = mIsTouching ? event.getX() : mCircleCenterX;
        float y = mIsTouching ? event.getY() : mCircleCenterY;
        calcOffsetAndRatio(x, y);
        invalidate();
        return true;
    }


}
