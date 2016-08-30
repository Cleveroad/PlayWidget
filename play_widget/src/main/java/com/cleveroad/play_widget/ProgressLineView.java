package com.cleveroad.play_widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.widget.ImageView;

class ProgressLineView extends ImageView {

    private static final int ADDITIONAL_PROGRESS_DEGREE = 20;
    private static final float BEGIN_PROGRESS_DEGREE = 180 - ADDITIONAL_PROGRESS_DEGREE;
    private static final float END_PROGRESS_DEGREE = 360 + ADDITIONAL_PROGRESS_DEGREE;
    private float mProgressCompleteLineStrokeWidth = 20;
    private float mProgressLineStrokeWidth = 16;
    private float mProgressBallRadius = 20;

    private float mProgressCurveRadius;
    private float mPadding = 0;
    private RectF mProgressBallRectF = new RectF();

    private Paint mLinePaint;
    private Paint mProgressPaint;
    private Paint mProgressBallPaint;

    private RectF mRect;
    private PointF mProgressPointCenter;
    private Path mCompleteLinePath;
    private Path mLinePath;
    private float mProgress = 0.0f;

    public ProgressLineView(Context context) {
        this(context, null);
    }

    public ProgressLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressLineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProgressLineView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mRect = new RectF();
        mProgressPointCenter = new PointF();
        mCompleteLinePath = new Path();
        mLinePath = new Path();

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setStrokeWidth(mProgressLineStrokeWidth);
        setProgressLineColor(Utils.getColor(getContext().getResources(), R.color.pw_progress_line_color));

        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        mProgressPaint.setStrokeWidth(mProgressCompleteLineStrokeWidth);
        setProgressCompleteColor(Utils.getColor(getContext().getResources(), R.color.pw_progress_complete_color));

        mProgressBallPaint = new Paint();
        mProgressBallPaint.setStyle(Paint.Style.FILL);
        mProgressBallPaint.setAntiAlias(true);
        mProgressBallPaint.setStrokeWidth(1);
        setProgressBallColor(Utils.getColor(getContext().getResources(), R.color.pw_progress_ball_color));

    }

    public void setProgress(float progress) {
        this.mProgress = Math.min(1.0f, Math.max(0.0f, progress));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mPadding = mProgressBallRadius;

        mRect.set(
                mPadding,
                mPadding,
                getWidth() - mPadding,
                getWidth() - mPadding
        );
        mProgressCurveRadius = (mRect.right - mRect.left) / 2.0f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float fullProgressDegree = (180.0f + ADDITIONAL_PROGRESS_DEGREE * 2) * mProgress;

        mCompleteLinePath.reset();
        mCompleteLinePath.arcTo(mRect, BEGIN_PROGRESS_DEGREE, fullProgressDegree, true);
        canvas.drawPath(mCompleteLinePath, mProgressPaint);

        float ballPositionAngle = BEGIN_PROGRESS_DEGREE + fullProgressDegree;
        float progressBalX = mProgressCurveRadius + mPadding + (float) (mProgressCurveRadius * Math.cos(ballPositionAngle * Math.PI / 180.0f));
        float progressBalY = mProgressCurveRadius + mPadding + (float) (mProgressCurveRadius * Math.sin(ballPositionAngle * Math.PI / 180.0f));
        mProgressPointCenter.set(progressBalX, progressBalY);

        mProgressBallRectF.set(
                mProgressPointCenter.x - mProgressBallRadius,
                mProgressPointCenter.y - mProgressBallRadius,
                mProgressPointCenter.x + mProgressBallRadius,
                mProgressPointCenter.y + mProgressBallRadius
        );
        mLinePath.reset();
        mLinePath.arcTo(mRect, ballPositionAngle, END_PROGRESS_DEGREE - ballPositionAngle);
        canvas.drawPath(mLinePath, mLinePaint);

        canvas.drawCircle(mProgressPointCenter.x, mProgressPointCenter.y, mProgressBallRadius, mProgressBallPaint);
        super.onDraw(canvas);
    }


    public void setProgressLineColor(int progressLineColor) {
        mLinePaint.setColor(progressLineColor);
    }

    public void setProgressCompleteColor(int progressCompleteColor) {
        mProgressPaint.setColor(progressCompleteColor);
    }

    public void setProgressBallColor(int progressBallColor) {
        int color = Color.rgb(Color.red(progressBallColor), Color.green(progressBallColor), Color.blue(progressBallColor));
        mProgressBallPaint.setColor(color);
    }

    public void setProgressCompleteLineStrokeWidth(float progressCompleteLineStrokeWidth) {
        this.mProgressCompleteLineStrokeWidth = progressCompleteLineStrokeWidth;
        mProgressPaint.setStrokeWidth(progressCompleteLineStrokeWidth);
    }

    public float getProgressCompleteLineStrokeWidth() {
        return mProgressCompleteLineStrokeWidth;
    }

    public void setProgressLineStrokeWidth(float progressLineStrokeWidth) {
        this.mProgressLineStrokeWidth = progressLineStrokeWidth;
        mLinePaint.setStrokeWidth(progressLineStrokeWidth);
    }

    public float getProgressLineStrokeWidth() {
        return mProgressLineStrokeWidth;
    }

    public void setProgressBallRadius(float progressBallRadius) {
        this.mProgressBallRadius = progressBallRadius;
    }

    public float getProgressBallRadius() {
        return mProgressBallRadius;
    }

    public @ColorInt int getProgressBallColor() {
        return mProgressBallPaint.getColor();
    }

    public @ColorInt int getProgressCompleteLineColor() {
        return mProgressPaint.getColor();
    }

    public @ColorInt int getProgressLineColor() {
        return mLinePaint.getColor();
    }

    public float getProgress() {
        return mProgress;
    }

    public void setEnabled(boolean enabled) {
        setVisibility(enabled ? VISIBLE : GONE);
    }

    public boolean isEnabled() {
        return getVisibility() == VISIBLE;
    }

}
