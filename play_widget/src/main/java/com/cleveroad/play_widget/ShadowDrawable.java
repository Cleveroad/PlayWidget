package com.cleveroad.play_widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

/**
 * Drawable that draw shadow for diffusers. provide methods for changing shadow size
 */
class ShadowDrawable extends Drawable {
    private static final int SHADOW_ANIMATION_DURATION = 350;

    private float mShadowSizeMultiplier = 0.5f;
    private final Paint mCornerShadowPaint;
    private final RectF mContentBounds;
    private float mCornerRadius;
    private Path mCornerShadowPath;

    private float mFinalShadowSize;
    private float mShadowSize;
    private float mRawShadowSize;

    private boolean mDirty = true;

    private final int mShadowStartColor;
    private final int mShadowMiddleColor;
    private final int mShadowEndColor;

    public ShadowDrawable(Context context) {
        this(context, false);
    }

    public ShadowDrawable(Context context, boolean hard) {
        if (hard) {
            mShadowStartColor = ContextCompat.getColor(context, R.color.pw_shadow_start_color_hard);
            mShadowMiddleColor = ContextCompat.getColor(context, R.color.pw_shadow_mid_color_hard);
            mShadowEndColor = ContextCompat.getColor(context, R.color.pw_shadow_end_color_hard);
        } else {
            mShadowStartColor = ContextCompat.getColor(context, R.color.pw_shadow_start_color);
            mShadowMiddleColor = ContextCompat.getColor(context, R.color.pw_shadow_mid_color);
            mShadowEndColor = ContextCompat.getColor(context, R.color.pw_shadow_end_color);
        }

        mCornerShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mCornerShadowPaint.setStyle(Paint.Style.FILL);
        mContentBounds = new RectF();
    }

    public void setup(float radius, float pShadowSize) {
        mCornerRadius = Math.round(radius) - pShadowSize;
        mFinalShadowSize = pShadowSize;
        setShadowSize(pShadowSize);
    }

    /**
     * Casts the value to an even integer.
     */
    private static int toEven(float value) {
        int i = Math.round(value);
        return (i % 2 == 1) ? i - 1 : i;
    }

    @Override
    public void setAlpha(int alpha) {
        mCornerShadowPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    /**
     * set shadow size multiplier allow change shadow size in percents.
     * @param multiplier size in percents;
     */
    public void setShadowSizeMultiplier(float multiplier) {
        mShadowSizeMultiplier = multiplier;
        setShadowSize(mFinalShadowSize, true);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mDirty = true;
    }

    void setShadowSize(float pShadowSize) {
        setShadowSize(pShadowSize, true);
    }

    void setShadowSize(float pShadowSize, boolean hardSet) {
        if (pShadowSize < 0) {
            throw new IllegalArgumentException("invalid shadow size");
        }
        pShadowSize = toEven(pShadowSize);
        if (mRawShadowSize == pShadowSize && !hardSet) {
            return;
        }
        mRawShadowSize = pShadowSize;
        mShadowSize = Math.round(pShadowSize * mShadowSizeMultiplier);
        mDirty = true;
        invalidateSelf();
    }

    @Override
    public boolean getPadding(@NonNull Rect padding) {
        int vOffset = (int) Math.ceil(mRawShadowSize * mShadowSizeMultiplier);
        int hOffset = (int) Math.ceil(mRawShadowSize);
        padding.set(hOffset, vOffset, hOffset, vOffset);
        return true;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mDirty) {
            buildComponents(getBounds());
            mDirty = false;
        }
        drawShadow(canvas);
    }

    private void drawShadow(Canvas canvas) {
        final int rotateSaved = canvas.save();
        // LT
        int saved = canvas.save();
        canvas.translate(mContentBounds.left + mCornerRadius, mContentBounds.top + mCornerRadius);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        canvas.restoreToCount(saved);
        // RB
        saved = canvas.save();
        canvas.translate(mContentBounds.right - mCornerRadius, mContentBounds.bottom - mCornerRadius);
        canvas.rotate(180f);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        canvas.restoreToCount(saved);
        // LB
        saved = canvas.save();
        canvas.translate(mContentBounds.left + mCornerRadius, mContentBounds.bottom - mCornerRadius);
        canvas.rotate(270f);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);

        canvas.restoreToCount(saved);
        // RT
        saved = canvas.save();
        canvas.translate(mContentBounds.right - mCornerRadius, mContentBounds.top + mCornerRadius);
        canvas.rotate(90f);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);

        canvas.restoreToCount(saved);
        canvas.restoreToCount(rotateSaved);
    }

    private void buildShadowCorners() {
        RectF innerBounds = new RectF(-mCornerRadius, -mCornerRadius, mCornerRadius, mCornerRadius);
        RectF outerBounds = new RectF(innerBounds);
        outerBounds.inset(-mShadowSize, -mShadowSize);

        if (mCornerShadowPath == null) {
            mCornerShadowPath = new Path();
        } else {
            mCornerShadowPath.reset();
        }
        mCornerShadowPath.setFillType(Path.FillType.EVEN_ODD);
        mCornerShadowPath.moveTo(-mCornerRadius, 0);
        mCornerShadowPath.rLineTo(-mShadowSize, 0);
        // outer arc
        mCornerShadowPath.arcTo(outerBounds, 180f, 90f, false);
        // inner arc
        mCornerShadowPath.arcTo(innerBounds, 270f, -90f, false);
        mCornerShadowPath.close();

        float shadowRadius = -outerBounds.top;
        if (shadowRadius > 0f) {
            float startRatio = mCornerRadius / shadowRadius;
            float midRatio = startRatio + ((1f - startRatio) / 2f);
            RadialGradient gradient = new RadialGradient(0, 0, shadowRadius,
                    new int[]{0, mShadowStartColor, mShadowMiddleColor, mShadowEndColor},
                    new float[]{0f, startRatio, midRatio, 1f},
                    Shader.TileMode.CLAMP);
            mCornerShadowPaint.setShader(gradient);
        }
    }

    private void buildComponents(Rect bounds) {
        final float verticalOffset = mRawShadowSize;
        mContentBounds.set(bounds.left + mRawShadowSize, bounds.top + verticalOffset,
                bounds.right - mRawShadowSize, bounds.bottom - verticalOffset);
        mCornerRadius = (mContentBounds.bottom - mContentBounds.top) / 2;

        buildShadowCorners();
    }

    public void showShadow(boolean animated) {
        showShadow(animated, 1.0f);
    }

    public void showShadow(boolean animated, float percentage) {
        percentage = Math.min(1.0f, Math.max(0.0f, percentage));
        if (animated) {
            ObjectAnimator.ofInt(this, "alpha", 0, (int) (255 * percentage)).setDuration(SHADOW_ANIMATION_DURATION).start();
        } else {
            setAlpha((int) (255 * percentage));
        }
    }

    public void hideShadow(boolean animated) {
        hideShadow(animated, 1.0f, null);
    }

    public void hideShadow(boolean animated, float percentage, Animator.AnimatorListener listener) {
        percentage = Math.min(1.0f, Math.max(0.0f, percentage));
        if (animated) {
            Animator animator = ObjectAnimator.ofInt(this, "alpha", (int) (255 * percentage), 0).setDuration(SHADOW_ANIMATION_DURATION);
            if (listener != null) {
                animator.addListener(listener);
            }
            animator.start();
        } else {
            setAlpha(0);
        }
    }

}