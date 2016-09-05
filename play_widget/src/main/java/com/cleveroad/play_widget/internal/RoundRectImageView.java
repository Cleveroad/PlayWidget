package com.cleveroad.play_widget.internal;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class RoundRectImageView extends ImageView {

    private Paint mPaint;
    private Paint mMaskPaint;
    private Paint mCirclePaint;
    private
    @ColorInt
    int mColor;
    private Bitmap mAnimationMaskBitmap;
    private Canvas mAnimationMaskCanvas;
    private float mRadiusPercentage = 0.0f;
    private int mColorAlpha;
    private boolean mRevealAnimation = false;
    private boolean mDismissAnimation = false;
    private RectF mRectF = new RectF();
    private int mSize;

    public RoundRectImageView(Context context) {
        this(context, null);
    }

    public RoundRectImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundRectImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RoundRectImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        //without hardware level gpu more
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mColor = Color.argb(100, 200, 50, 50);
        mCirclePaint = new Paint();
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(mColor);
        mCirclePaint.setAntiAlias(true);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLACK);

        mMaskPaint = new Paint();
        mMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    public void setColor(@ColorInt int color) {
        mColor = color;
        mCirclePaint.setColor(color);
        mColorAlpha = Color.alpha(color);
    }

    public
    @ColorInt
    int getColor() {
        return mColor;
    }

    @Override
    public void onDraw(Canvas canvas) {
        mRectF.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getWidth() - getPaddingBottom());
        float halfSize = (mRectF.right - mRectF.left) / 2.0f;
        super.onDraw(canvas);

        if (mRevealAnimation) {
            float radius = halfSize * 1.5f * mRadiusPercentage;
            canvas.drawCircle(mRectF.left + halfSize, mRectF.bottom - halfSize * mRadiusPercentage, radius, mCirclePaint);
        } else if (mDismissAnimation) {
            canvas.drawRoundRect(mRectF, halfSize * mRadiusPercentage, halfSize * mRadiusPercentage, mCirclePaint);
        } else {
            if (mRadiusPercentage > 0.5f) {
                canvas.drawCircle(mRectF.left + halfSize, mRectF.top + halfSize, halfSize, mCirclePaint);
            }
        }
        if (mAnimationMaskCanvas != null) {
            mAnimationMaskCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mAnimationMaskCanvas.drawRoundRect(mRectF,
                    (canvas.getWidth() - getPaddingLeft() - getPaddingRight()) * mRadiusPercentage / 2.0f,
                    (canvas.getWidth() - getPaddingLeft() - getPaddingRight()) * mRadiusPercentage / 2.0f,
                    mPaint
            );

            canvas.drawBitmap(mAnimationMaskBitmap, 0, 0, mMaskPaint);
        }

    }

    public void setRevealDrawingAlpha(float alpha) {
        mCirclePaint.setAlpha((int) (alpha * mColorAlpha));
    }

    public void setRadiusPercentage(float radiusPercentage) {
        this.mRadiusPercentage = radiusPercentage;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int newSize = Math.min(widthSize, heightSize);
        setMeasuredDimension(newSize, newSize);
        if (mAnimationMaskBitmap == null || mSize != newSize) {
            mAnimationMaskBitmap = Bitmap.createBitmap(newSize, newSize, Bitmap.Config.ARGB_4444);
            mAnimationMaskCanvas = new Canvas(mAnimationMaskBitmap);
        }
        mSize = newSize;
    }

    @Override
    protected void onDetachedFromWindow() {
        mAnimationMaskCanvas = null;
        if (mAnimationMaskBitmap != null) {
            mAnimationMaskBitmap.recycle();
            mAnimationMaskBitmap = null;
        }
        super.onDetachedFromWindow();
    }

    public void setRevealAnimation(boolean revealAnimation) {
        this.mRevealAnimation = revealAnimation;
    }

    public void setDismissAnimation(boolean dismissAnimation) {
        this.mDismissAnimation = dismissAnimation;
    }

}
