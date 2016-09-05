package com.cleveroad.play_widget.internal;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.cleveroad.play_widget.PlayLayout;
import com.cleveroad.play_widget.R;

public class ProgressLineView extends ImageView {

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
    private float mProgress = 0.0f;
    private int mAllowedTouchRadius = 10;
    private boolean mHandledTouch = false;
    private PlayLayout.OnProgressChangedListener mProgressChangedListener;

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
        mAllowedTouchRadius = getContext().getResources().getDimensionPixelSize(R.dimen.pw_progress_line_view_touch_radius);
        mRect = new RectF();
        mProgressPointCenter = new PointF();

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setStrokeWidth(mProgressLineStrokeWidth);
        setProgressLineColor(ContextCompat.getColor(getContext(), R.color.pw_progress_line_color));

        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        mProgressPaint.setStrokeWidth(mProgressCompleteLineStrokeWidth);
        setProgressCompleteColor(ContextCompat.getColor(getContext(), R.color.pw_progress_complete_color));

        mProgressBallPaint = new Paint();
        mProgressBallPaint.setStyle(Paint.Style.FILL);
        mProgressBallPaint.setAntiAlias(true);
        mProgressBallPaint.setStrokeWidth(1);
        setProgressBallColor(ContextCompat.getColor(getContext(), R.color.pw_progress_ball_color));
    }

    public void setProgress(float progress) {
        if (!mHandledTouch) {
            mProgress = Utils.betweenZeroOne(progress);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        recalculateRect();
    }

    private void recalculateRect() {
        mRect.set(
                mProgressBallRadius + mPadding,
                mProgressBallRadius + mPadding,
                getWidth() - mProgressBallRadius - mPadding,
                getWidth() - mProgressBallRadius - mPadding
        );
        mProgressCurveRadius = (mRect.right - mRect.left) / 2.0f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int halfSize = getWidth() / 2;
        float x_center_offset = event.getX() - halfSize;
        float y_center_offset = halfSize - event.getY();
        double touchPointDistanceToCenter = Math.sqrt(y_center_offset * y_center_offset + x_center_offset * x_center_offset);
        double tanAngle = Math.atan2(y_center_offset, x_center_offset) * 180.0 / Math.PI;

        if (tanAngle < 0) {
            tanAngle *= -1;
        } else {
            tanAngle = 360 - tanAngle;
        }
        if (tanAngle < 90) {
            tanAngle += 360;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mProgressChangedListener != null) {
                mProgressChangedListener.onPreSetProgress();
            }
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mHandledTouch) {
                if (mProgressChangedListener != null) {
                    mProgressChangedListener.onProgressChanged(mProgress);
                }
            }
            mHandledTouch = false;
            return super.onTouchEvent(event);
        }

        if (!mHandledTouch && (tanAngle < BEGIN_PROGRESS_DEGREE || tanAngle > END_PROGRESS_DEGREE)) {
            return super.onTouchEvent(event);
        }

        float radius = mProgressCurveRadius;
        if (mHandledTouch || (touchPointDistanceToCenter > radius - mAllowedTouchRadius && touchPointDistanceToCenter < radius + mAllowedTouchRadius)) {

            double progressAngle = tanAngle - BEGIN_PROGRESS_DEGREE;
            if (tanAngle < BEGIN_PROGRESS_DEGREE) {
                progressAngle = 0.0;
            } else if (tanAngle > END_PROGRESS_DEGREE) {
                progressAngle = END_PROGRESS_DEGREE - BEGIN_PROGRESS_DEGREE;
            }
            mProgress = (float) (progressAngle / (END_PROGRESS_DEGREE - BEGIN_PROGRESS_DEGREE));
            mHandledTouch = true;
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        float fullProgressDegree = (180.0f + ADDITIONAL_PROGRESS_DEGREE * 2) * mProgress;
        if (fullProgressDegree>0.05f) {
            canvas.drawArc(mRect, BEGIN_PROGRESS_DEGREE, fullProgressDegree, false, mProgressPaint);
        }
        float ballPositionAngle = BEGIN_PROGRESS_DEGREE + fullProgressDegree;
        float progressBalX = mProgressCurveRadius + mProgressBallRadius + mPadding + (float) (mProgressCurveRadius * Math.cos(ballPositionAngle * Math.PI / 180.0f));
        float progressBalY = mProgressCurveRadius + mProgressBallRadius + mPadding + (float) (mProgressCurveRadius * Math.sin(ballPositionAngle * Math.PI / 180.0f));
        mProgressPointCenter.set(progressBalX, progressBalY);

        mProgressBallRectF.set(
                mProgressPointCenter.x - mProgressBallRadius,
                mProgressPointCenter.y - mProgressBallRadius,
                mProgressPointCenter.x + mProgressBallRadius,
                mProgressPointCenter.y + mProgressBallRadius
        );
        canvas.drawArc(mRect, ballPositionAngle, END_PROGRESS_DEGREE - ballPositionAngle, false, mLinePaint);
        canvas.drawCircle(mProgressPointCenter.x, mProgressPointCenter.y, mProgressBallRadius, mProgressBallPaint);
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
        int defaultTouchRadius = getContext().getResources().getDimensionPixelSize(R.dimen.pw_progress_line_view_touch_radius);
        mAllowedTouchRadius = defaultTouchRadius < mProgressBallRadius ? (int) mProgressBallRadius : defaultTouchRadius;
    }

    public float getProgressBallRadius() {
        return mProgressBallRadius;
    }

    public
    @ColorInt
    int getProgressBallColor() {
        return mProgressBallPaint.getColor();
    }

    public
    @ColorInt
    int getProgressCompleteLineColor() {
        return mProgressPaint.getColor();
    }

    public
    @ColorInt
    int getProgressLineColor() {
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

    public void setPadding(float padding) {
        mPadding = padding;
        recalculateRect();
    }

    public float getPadding() {
        return mPadding;
    }

    /**
     * Set progressChangedListener
     *
     * @param progressChangedListener PlayLayout.OnProgressChangedListener listener for the event;
     */
    public void setOnProgressChangedListener(@Nullable PlayLayout.OnProgressChangedListener progressChangedListener) {
        mProgressChangedListener = progressChangedListener;
    }
}
