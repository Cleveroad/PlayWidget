package com.cleveroad.play_widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.widget.ImageView;

class DiffuserView extends ImageView {

    private float mRadiusPercentage = 0.0f;
    private boolean mDismissAnimation = false;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mRect = new RectF();
    private boolean mMustDrawRevealAnimation = false;
    private int mShadowSize = 0;

    public DiffuserView(Context context) {
        this(context, null);
    }

    public DiffuserView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DiffuserView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DiffuserView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.argb(100, 200, 50, 200));
        mPaint.setAntiAlias(true);
    }

    public void setShadowSize(int shadowSize) {
        mShadowSize = shadowSize;
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    public @ColorInt int getColor() {
        return mPaint.getColor();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mMustDrawRevealAnimation) {
            super.onDraw(canvas);
            return;
        }
        mRect.set(mShadowSize, mShadowSize, getWidth() - mShadowSize, getWidth() - mShadowSize);
        if (canvas.getHeight() < 1) {
            return;
        }

        float halfSize = (mRect.right - mRect.left) / 2.0f;
        if (!mDismissAnimation) {
            canvas.drawCircle(mRect.left + halfSize, mRect.top + halfSize, halfSize, mPaint);
            return;
        }
        if (mDismissAnimation) {
            canvas.drawRoundRect(mRect, halfSize * mRadiusPercentage, halfSize * mRadiusPercentage, mPaint);
        } else {
            canvas.drawCircle(mRect.left + halfSize, mRect.top + halfSize, halfSize, mPaint);
        }

    }

    public void setRadiusPercentage(float radiusPercentage) {
        this.mRadiusPercentage = radiusPercentage;
        invalidate();
    }

    public void setDismissAnimation(boolean dismissAnimation) {
        this.mDismissAnimation = dismissAnimation;
    }

    public void setMustDrawRevealAnimation(boolean mustDrawRevealAnimation) {
        this.mMustDrawRevealAnimation = mustDrawRevealAnimation;
    }

}
