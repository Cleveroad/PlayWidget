package com.cleveroad.play_widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cleveroad.play_widget.internal.DiffuserView;
import com.cleveroad.play_widget.internal.ProgressLineView;
import com.cleveroad.play_widget.internal.RoundRectImageView;
import com.cleveroad.play_widget.internal.Utils;

/**
 * PlayLayout View implementation
 */
public class PlayLayout extends RelativeLayout implements OnShadowChangeListener {

    public static final int DEFAULT_DURATION = 430;
    public static final float SMALL_SHADOW_OPACITY = 0.75f;
    private static final int PROGRESS_LINE_ALPHA_ANIMATION_DURATION = 350;

    private static final float BIG_DIFFUSER_MIN_SHADOW_PERCENT = 0.25f;
    private static final float MEDIUM_DIFFUSER_MIN_SHADOW_PERCENT = 0.25f;
    private static final float SMALL_DIFFUSER_MIN_SHADOW_PERCENT = 0.3f;

    private ShadowPercentageProvider mShadowProvider;

    private ShadowDrawable mBigShadowDrawable;
    private ShadowDrawable mMediumShadowDrawable;
    private ShadowDrawable mSmallShadowDrawable;

    private AnimatorSet mRevealAnimatorSet = null;
    private AnimatorSet mDismissAnimatorSet = null;

    private FloatingActionButton mPlayButton;

    private int mDuration = DEFAULT_DURATION;
    private RelativeLayout mRlImagesContainer;
    private RoundRectImageView mIvBackground;
    private DiffuserView mBigDiffuserImageView;
    private DiffuserView mMediumDiffuserImageView;
    private ImageView mSmallDiffuserImageView;
    private ProgressLineView mProgressLineView;

    private float mSmallDiffuserFullSize;
    private int mBigDiffuserShadowWidth;
    private int mMediumDiffuserShadowWidth;
    private int mSmallDiffuserShadowWidth;
    private float mRadiusPercentage = 0.0f;

    private int mButtonsSize;

    private int mDiffusersPadding;

    private ImageView mIvShuffle;
    private ImageView mIvSkipPrevious;
    private ImageView mIvSkipNext;
    private ImageView mIvRepeat;

    private @Nullable OnButtonsClickListener mClickListener;
    private @Nullable OnButtonsLongClickListener mLongClickListener;

    public PlayLayout(Context context) {
        this(context, null);
    }

    public PlayLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlayLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        LayoutInflater.from(context).inflate(R.layout.pw_reveal_view_content, this, true);
        mRlImagesContainer = (RelativeLayout) this.findViewById(R.id.pw_rlImagesContainer);

        mPlayButton = (FloatingActionButton) this.findViewById(R.id.pw_playButton);
        mIvBackground = (RoundRectImageView) this.findViewById(R.id.pw_ivBackground);
        mBigDiffuserImageView = (DiffuserView) this.findViewById(R.id.pw_ivBigDiffuser);
        mMediumDiffuserImageView = (DiffuserView) this.findViewById(R.id.pw_ivMediumDiffuser);
        mSmallDiffuserImageView = (ImageView) this.findViewById(R.id.pw_ivSmallDiffuser);
        mProgressLineView = (ProgressLineView) this.findViewById(R.id.pw_vProgressLine);

        mIvShuffle = (ImageView) this.findViewById(R.id.pw_ivShuffle);
        mIvSkipPrevious = (ImageView) this.findViewById(R.id.pw_ivSkipPrevious);
        mIvSkipNext = (ImageView) this.findViewById(R.id.pw_ivSkipNext);
        mIvRepeat = (ImageView) this.findViewById(R.id.pw_ivRepeat);

        initListeners();

        if (!isInEditMode()) {
            mProgressLineView.setAlpha(0.0f);
        }
        TypedArray typedArrayValues = context.obtainStyledAttributes(attrs, R.styleable.PlayWidget);

        mBigDiffuserShadowWidth = typedArrayValues.getDimensionPixelSize(R.styleable.PlayWidget_pw_big_diffuser_shadow_width, context.getResources().getDimensionPixelSize(R.dimen.pw_big_diffuser_shadow_width));
        mMediumDiffuserShadowWidth = typedArrayValues.getDimensionPixelSize(R.styleable.PlayWidget_pw_medium_diffuser_shadow_width, context.getResources().getDimensionPixelSize(R.dimen.pw_medium_diffuser_shadow_width));
        mSmallDiffuserShadowWidth = typedArrayValues.getDimensionPixelSize(R.styleable.PlayWidget_pw_small_diffuser_shadow_width, context.getResources().getDimensionPixelSize(R.dimen.pw_small_diffuser_shadow_width));

        mButtonsSize = typedArrayValues.getDimensionPixelSize(R.styleable.PlayWidget_pw_buttons_size, context.getResources().getDimensionPixelSize(R.dimen.pw_image_item_size));

        mBigDiffuserImageView.setShadowSize(mBigDiffuserShadowWidth);
        mMediumDiffuserImageView.setShadowSize(mMediumDiffuserShadowWidth);

        mProgressLineView.setEnabled(typedArrayValues.getBoolean(R.styleable.PlayWidget_pw_progress_line_enabled, true));

        mDiffusersPadding = typedArrayValues.getDimensionPixelSize(R.styleable.PlayWidget_pw_diffusers_padding, context.getResources().getDimensionPixelSize(R.dimen.pw_default_diffusers_padding));

        mProgressLineView.setPadding(typedArrayValues.getDimensionPixelSize(R.styleable.PlayWidget_pw_progress_line_padding, context.getResources().getDimensionPixelSize(R.dimen.pw_default_progress_line_padding)));

        int progressCompleteLineStrokeWidth = typedArrayValues.getDimensionPixelSize(R.styleable.PlayWidget_pw_progress_complete_line_stroke_width, context.getResources().getDimensionPixelSize(R.dimen.pw_progress_complete_line_stroke_width));
        int progressLineStrokeWidth = typedArrayValues.getDimensionPixelSize(R.styleable.PlayWidget_pw_progress_line_stroke_width, context.getResources().getDimensionPixelSize(R.dimen.pw_progress_line_stroke_width));
        int progressBallRadius = typedArrayValues.getDimensionPixelSize(R.styleable.PlayWidget_pw_progress_ball_radius, context.getResources().getDimensionPixelSize(R.dimen.pw_progress_ball_radius));

        if (progressBallRadius * 2.0f < progressCompleteLineStrokeWidth
                || progressBallRadius * 2.0f < progressLineStrokeWidth) {
            throw new IllegalStateException("Progress ball radius cannot be less then complete line stroke or line stroke");
        }
        mProgressLineView.setProgressCompleteLineStrokeWidth(progressCompleteLineStrokeWidth);
        mProgressLineView.setProgressBallRadius(progressBallRadius);
        mProgressLineView.setProgressLineStrokeWidth(progressLineStrokeWidth);

        Drawable d = typedArrayValues.getDrawable(R.styleable.PlayWidget_pw_image_src);
        if (d != null) {
            setImageDrawable(d);
        }

        int bigDiffuserColor = typedArrayValues.getColor(R.styleable.PlayWidget_pw_big_diffuser_color, ContextCompat.getColor(getContext(), R.color.pw_circle_color));
        int mediumDiffuserColor = typedArrayValues.getColor(R.styleable.PlayWidget_pw_medium_diffuser_color, ContextCompat.getColor(getContext(), R.color.pw_circle_color_translucent));

        setProgressLineColor(typedArrayValues.getColor(R.styleable.PlayWidget_pw_progress_line_color, ContextCompat.getColor(getContext(), R.color.pw_progress_line_color)));
        setProgressCompleteColor(typedArrayValues.getColor(R.styleable.PlayWidget_pw_progress_complete_line_color, ContextCompat.getColor(getContext(), R.color.pw_progress_complete_color)));
        setProgressBallColor(typedArrayValues.getColor(R.styleable.PlayWidget_pw_progress_ball_color, ContextCompat.getColor(getContext(), R.color.pw_progress_ball_color)));

        ColorStateList lFabBackgroundTint = typedArrayValues.getColorStateList(R.styleable.PlayWidget_pw_play_button_background_tint);
        if (lFabBackgroundTint != null)
            mPlayButton.setBackgroundTintList(lFabBackgroundTint);

        typedArrayValues.recycle();

        mSmallDiffuserFullSize = context.getResources().getDimensionPixelSize(R.dimen.pw_small_diffuser_size) + mSmallDiffuserShadowWidth * 2;

        //big diffuser
        mIvBackground.setColor(bigDiffuserColor);
        mBigShadowDrawable = new ShadowDrawable(getContext(), true);
        mBigShadowDrawable.hideShadow(false);
        setupDiffuserView(mBigDiffuserImageView, mBigShadowDrawable);

        //medium diffuser
        mMediumDiffuserImageView.setColor(mediumDiffuserColor);
        mMediumDiffuserImageView.setMustDrawRevealAnimation(true);
        mMediumShadowDrawable = new ShadowDrawable(getContext());
        setupDiffuserView(mMediumDiffuserImageView, mMediumShadowDrawable);
        mMediumDiffuserImageView.setScaleX(0.0f);
        mMediumDiffuserImageView.setScaleY(0.0f);

        //small diffuser
        mSmallShadowDrawable = new ShadowDrawable(getContext());
        mSmallShadowDrawable.setup(mSmallDiffuserFullSize / 2.0f, mSmallDiffuserShadowWidth);
        setupDiffuserView(mSmallDiffuserImageView, mSmallShadowDrawable);
        mSmallShadowDrawable.hideShadow(false);
    }

    private void initListeners() {
        mIvShuffle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onShuffleClicked();
                }
            }
        });

        mIvSkipPrevious.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onSkipPreviousClicked();
                }
            }
        });

        mIvSkipNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onSkipNextClicked();
                }
            }
        });

        mIvRepeat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onRepeatClicked();
                }
            }
        });

        mPlayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onPlayButtonClicked();
                }
            }
        });


        mIvShuffle.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mLongClickListener != null) {
                    mLongClickListener.onShuffleLongClicked();
                    return true;
                }
                return false;
            }
        });

        mIvSkipPrevious.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mLongClickListener != null) {
                    mLongClickListener.onSkipPreviousLongClicked();
                    return true;
                }
                return false;
            }
        });

        mIvSkipNext.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mLongClickListener != null) {
                    mLongClickListener.onSkipNextLongClicked();
                    return true;
                }
                return false;
            }
        });

        mIvRepeat.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mLongClickListener != null) {
                    mLongClickListener.onRepeatLongClicked();
                    return true;
                }
                return false;
            }
        });

        mPlayButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mLongClickListener != null) {
                    mLongClickListener.onPlayButtonLongClicked();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRlImagesContainer.setMinimumWidth(mPlayButton.getMeasuredWidth()
                + mButtonsSize * 4
                + getContext().getResources().getDimensionPixelSize(R.dimen.pw_image_item_margin) * 4);
        mIvShuffle.getLayoutParams().width = mButtonsSize;
        mIvShuffle.getLayoutParams().height = mButtonsSize;

        mIvSkipPrevious.getLayoutParams().width = mButtonsSize;
        mIvSkipPrevious.getLayoutParams().height = mButtonsSize;

        mIvSkipNext.getLayoutParams().width = mButtonsSize;
        mIvSkipNext.getLayoutParams().height = mButtonsSize;

        mIvRepeat.getLayoutParams().width = mButtonsSize;
        mIvRepeat.getLayoutParams().height = mButtonsSize;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int containerWidth = r - l - getPaddingLeft() - getPaddingRight();
        int containerHeight = b - t - getPaddingTop() - getPaddingBottom() - mPlayButton.getHeight();
        int containerSize = containerWidth < containerHeight ? containerWidth : containerHeight;
        int additionalPadding = (mRlImagesContainer.getMeasuredWidth() - containerSize) / 2;

        mRlImagesContainer.setPadding(0, 0, 0, mPlayButton.getHeight());

        mBigDiffuserImageView.layout(
                mDiffusersPadding + additionalPadding,
                mDiffusersPadding,
                containerSize - mDiffusersPadding + additionalPadding,
                containerSize - mDiffusersPadding
        );

        mProgressLineView.layout(
                additionalPadding,
                0,
                containerSize + additionalPadding,
                containerSize
        );

        float bigDiffuserHalfRadius = (mBigDiffuserImageView.getRight() - mBigDiffuserImageView.getLeft()) / 2.0f;
        mIvBackground.layout(
                mBigDiffuserShadowWidth + mDiffusersPadding + additionalPadding,
                mBigDiffuserShadowWidth + mDiffusersPadding,
                containerSize - mBigDiffuserShadowWidth - mDiffusersPadding + additionalPadding,
                containerSize - mBigDiffuserShadowWidth - mDiffusersPadding
        );

        mBigShadowDrawable.setup(bigDiffuserHalfRadius, mBigDiffuserShadowWidth);
        float mediumDiffuserHalfRadius = (bigDiffuserHalfRadius + mPlayButton.getWidth()) / 2.2f;

        int mediumCircleShift = (int) (bigDiffuserHalfRadius - mediumDiffuserHalfRadius);
        mMediumDiffuserImageView.layout(
                mBigDiffuserImageView.getLeft() + mediumCircleShift,
                mBigDiffuserImageView.getTop() + mediumCircleShift,
                mBigDiffuserImageView.getRight() - mediumCircleShift,
                mBigDiffuserImageView.getBottom() - mediumCircleShift
        );
        mMediumShadowDrawable.setup(mediumDiffuserHalfRadius, mMediumDiffuserShadowWidth);


        int smallCircleShift = (int) (bigDiffuserHalfRadius - mSmallDiffuserFullSize / 2.0f);

        mSmallDiffuserImageView.layout(
                mBigDiffuserImageView.getLeft() + smallCircleShift,
                mBigDiffuserImageView.getTop() + smallCircleShift,
                mBigDiffuserImageView.getRight() - smallCircleShift,
                mBigDiffuserImageView.getBottom() - smallCircleShift
        );

        float smallDiffuserHalfRadius = mSmallDiffuserFullSize / 2.0f;
        mSmallShadowDrawable.setup(smallDiffuserHalfRadius, mSmallDiffuserShadowWidth);

        if (isOpenInner()) {
            mPlayButton.setTranslationY(calculateFabTransitionY());
        }
    }

    @Override
    public void addView(View child) {
        if (child.getId() == R.id.pw_rlMainContainer) {
            super.addView(child);
        }
    }

    @Override
    public void addView(View child, int index) {
        if (child.getId() == R.id.pw_rlMainContainer) {
            super.addView(child, index);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child.getId() == R.id.pw_rlMainContainer) {
            super.addView(child, index, params);
        }
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (child.getId() == R.id.pw_rlMainContainer) {
            super.addView(child, params);
        }
    }

    @Override
    public void addView(View child, int width, int height) {
        if (child.getId() == R.id.pw_rlMainContainer) {
            super.addView(child, width, height);
        }
    }

    /**
     * Set OnButtonsClickListener for layout
     * @param listener -> OnButtonsClickListener
     */
    public void setOnButtonsClickListener(@Nullable OnButtonsClickListener listener) {
        mClickListener = listener;
    }

    /**
     * Set OnButtonsLongClickListener for layout
     * @param listener -> OnButtonsLongClickListener
     */
    public void setOnButtonsLongClickListener(@Nullable OnButtonsLongClickListener listener) {
        mLongClickListener = listener;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setupDiffuserView(View ivDiffuser, Drawable background) {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            ivDiffuser.setBackground(background);
        } else {
            //noinspection deprecation
            ivDiffuser.setBackgroundDrawable(background);
        }
    }

    private int calculateFabTransitionY() {
        return mSmallDiffuserImageView.getTop() + mSmallDiffuserImageView.getHeight() / 2 - mPlayButton.getTop() - mPlayButton.getHeight() / 2 + getPaddingTop();
    }

    public void fastOpen() {
        mIvBackground.setRevealDrawingAlpha(1.0f);
        mRadiusPercentage = 1.0f;
        if (mProgressLineView.isEnabled()) {
            mProgressLineView.setAlpha(1.0f);
        }
        mIvBackground.setRadiusPercentage(mRadiusPercentage);
        revealView();
    }

    public void startRevealAnimation() {
        if (mShadowProvider != null) {
            mShadowProvider.setAllowChangeShadow(true);
        }
        mBigShadowDrawable.setShadowSizeMultiplier(1.0f);
        mMediumShadowDrawable.setShadowSizeMultiplier(1.0f);
        mSmallShadowDrawable.setShadowSizeMultiplier(1.0f);
        mPlayButton.setImageResource(R.drawable.pw_pause);
        mIvBackground.setRevealDrawingAlpha(1.0f);
        mMediumDiffuserImageView.setAlpha(1.0f);
        mBigShadowDrawable.hideShadow(false);
        mSmallShadowDrawable.hideShadow(false);
        mIvBackground.setRevealAnimation(true);
        mMediumDiffuserImageView.setVisibility(View.VISIBLE);

        if (mRevealAnimatorSet == null) {
            mRevealAnimatorSet = new AnimatorSet();
            Animator centerYImageAnimator = ObjectAnimator.ofFloat(mMediumDiffuserImageView, "translationY", getHeight() / 2, 0.0f);
            centerYImageAnimator.setInterpolator(new DecelerateInterpolator(1.25f));
            Animator scaleXImageAnimator = ObjectAnimator.ofFloat(mMediumDiffuserImageView, "scaleX", 0.0f, 1.0f);
            Animator scaleYImageAnimator = ObjectAnimator.ofFloat(mMediumDiffuserImageView, "scaleY", 0.0f, 1.0f);
            Animator alphaImageAnimator = ObjectAnimator.ofFloat(mMediumDiffuserImageView, "alpha", 0.0f, 1.0f);
            alphaImageAnimator.setInterpolator(new AccelerateInterpolator(2));
            Animator radiusPercentageAnimator = ObjectAnimator.ofFloat(this, "radiusPercentage", 0.0f, 1.0f);
            Animator fabTransitionAnimator = ObjectAnimator.ofFloat(mPlayButton, "translationY", 0, calculateFabTransitionY());
            fabTransitionAnimator.setInterpolator(new OvershootInterpolator(0.8f));
            mRevealAnimatorSet.playTogether(
                    centerYImageAnimator,
                    scaleXImageAnimator,
                    scaleYImageAnimator,
                    alphaImageAnimator,
                    radiusPercentageAnimator,
                    fabTransitionAnimator
            );
            mRevealAnimatorSet.setDuration(mDuration);
            mRevealAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mIvBackground.setRevealAnimation(false);
                    mBigShadowDrawable.showShadow(true);
                    mSmallShadowDrawable.showShadow(true, SMALL_SHADOW_OPACITY);
                    ObjectAnimator.ofFloat(mProgressLineView, "alpha", 0.0f, 1.0f).setDuration(PROGRESS_LINE_ALPHA_ANIMATION_DURATION).start();
//                    mProgressLineView.setEnabled(true);
                }
            });
        }
        mRevealAnimatorSet.start();
    }

    private void revealView() {
        mPlayButton.setImageResource(R.drawable.pw_pause);
        mMediumDiffuserImageView.setRadiusPercentage(mRadiusPercentage);
        mMediumDiffuserImageView.setTranslationY(0.0f);
        mMediumDiffuserImageView.setScaleX(1.0f);
        mMediumDiffuserImageView.setScaleY(1.0f);
        mMediumDiffuserImageView.setAlpha(1.0f);
//        mIvBackground.setRevealAnimation(false);
        mBigShadowDrawable.showShadow(false);
        mSmallShadowDrawable.showShadow(false, SMALL_SHADOW_OPACITY);
    }

    public void startDismissAnimation() {
        if (mShadowProvider != null) {
            mShadowProvider.setAllowChangeShadow(false);
        }
        mIvBackground.setDismissAnimation(true);
        mMediumDiffuserImageView.setDismissAnimation(true);
        mBigShadowDrawable.hideShadow(true);
        Animator progressViewDismissAnimator = ObjectAnimator.ofFloat(mProgressLineView, "alpha", 1.0f, 0.0f);
        progressViewDismissAnimator.setDuration(PROGRESS_LINE_ALPHA_ANIMATION_DURATION).start();
        mSmallShadowDrawable.hideShadow(true, SMALL_SHADOW_OPACITY, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Drawable fabPlayDrawable;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    fabPlayDrawable = getResources().getDrawable(R.drawable.pw_play, null);
                } else {
                    //noinspection deprecation
                    fabPlayDrawable = getResources().getDrawable(R.drawable.pw_play);
                }
                mPlayButton.setImageDrawable(fabPlayDrawable);

                if (mDismissAnimatorSet == null) {
                    mDismissAnimatorSet = new AnimatorSet();
                    Animator alphaImageAnimator = ObjectAnimator.ofFloat(mMediumDiffuserImageView, "alpha", 1.0f, 0.0f);
                    alphaImageAnimator.setInterpolator(new DecelerateInterpolator(2));
                    Animator alphaBigDiffuserAnimator = ObjectAnimator.ofFloat(mIvBackground, "revealDrawingAlpha", 1.0f, 0.0f);
                    Animator radiusPercentageAnimator = ObjectAnimator.ofFloat(PlayLayout.this, "radiusPercentage", 1.0f, 0.0f);
                    Animator fabTransitionAnimator = ObjectAnimator.ofFloat(mPlayButton, "translationY", calculateFabTransitionY(), 0);
                    fabTransitionAnimator.setInterpolator(new OvershootInterpolator(0.8f));
                    mDismissAnimatorSet.playTogether(
                            alphaImageAnimator,
                            radiusPercentageAnimator,
                            alphaBigDiffuserAnimator,
                            fabTransitionAnimator
                    );
                    mDismissAnimatorSet.setDuration(mDuration);
                    mDismissAnimatorSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mIvBackground.setDismissAnimation(false);
                            mMediumDiffuserImageView.setDismissAnimation(false);
                        }
                    });
                }
                mDismissAnimatorSet.start();
            }
        });
    }

    /**
     * Set progress for progress line view
     *
     * @param progress the progress in percentage. (0.0 - 1.0f)
     */
    public void setProgress(float progress) {
        if (mProgressLineView.getVisibility() == VISIBLE) {
            mProgressLineView.setProgress(progress);
            mProgressLineView.invalidate();
        }
    }

    /**
     * Set progress for progress line view. This will call setProgress on main thread.
     *
     * @param progress the progress in percentage. (0.0 - 1.0f)
     */
    public void setPostProgress(final float progress) {
        post(new Runnable() {
            @Override
            public void run() {
                setProgress(progress);
            }
        });
    }

    /**
     * Sets a drawable as the content of this ImageView.
     *
     * @param resId the resource identifier of the drawable.
     */
    public void setImageResource(@DrawableRes int resId) {
        mIvBackground.setImageResource(resId);
    }

    /**
     * Sets a drawable as the content of this ImageView.
     *
     * @param drawable the Drawable to set, or null to clear the content.
     */
    public void setImageDrawable(@Nullable Drawable drawable) {
        mIvBackground.setImageDrawable(drawable);
    }

    /**
     * Sets the content of this ImageView to the specified Uri.
     *
     * @param uri the Uri of an image, or null to clear the content
     */
    public void setImageURI(@Nullable Uri uri) {
        mIvBackground.setImageURI(uri);
    }

    /**
     * Sets a Bitmap as the content of this ImageView.
     *
     * @param bm The bitmap to set.
     */
    public void setImageBitmap(Bitmap bm) {
        mIvBackground.setImageBitmap(bm);
    }

    /**
     * Set size for buttons (shuffle, previous, next, repeat)
     *
     * @param buttonsSize Size for buttons
     */
    public void setButtonsSize(int buttonsSize) {
        this.mButtonsSize = buttonsSize;
        requestLayout();
    }

    /**
     * Set size for buttons (shuffle, previous, next, repeat) from resources
     *
     * @param buttonsSizeRes dimen resource size for buttons
     */
    public void setButtonsSizeResource(@DimenRes int buttonsSizeRes) {
        setButtonsSize(getResources().getDimensionPixelSize(buttonsSizeRes));
    }


    /**
     * Enable or disable the progress line view. If enable == false progressLine is GONE
     *
     * @param enabled requested enable state
     */
    public void setProgressEnabled(boolean enabled) {
        mProgressLineView.setEnabled(enabled);
    }

    /**
     * Set shadow width for big diffuser
     *
     * @param shadowWidth shadow width for big diffuser
     */
    public void setBigDiffuserShadowWidth(int shadowWidth) {
        this.mBigDiffuserShadowWidth = shadowWidth;
        mBigDiffuserImageView.setShadowSize(shadowWidth);
        requestLayout();
    }

    /**
     * Set shadow width for big diffuser with dimension
     *
     * @param shadowWidth dimen resource shadow width for big diffuser
     */
    public void setBigDiffuserShadowWidthResource(@DimenRes int shadowWidth) {
        setBigDiffuserShadowWidth(getResources().getDimensionPixelSize(shadowWidth));
    }

    /**
     * Set shadow width for medium diffuser
     *
     * @param shadowWidth shadow width for big diffuser
     */
    public void setMediumDiffuserShadowWidth(int shadowWidth) {
        this.mMediumDiffuserShadowWidth = shadowWidth;
        mMediumDiffuserImageView.setShadowSize(shadowWidth);
        requestLayout();
    }

    /**
     * Set shadow width for big medium with dimension
     *
     * @param shadowWidth dimen resource shadow width for medium diffuser
     */
    public void setMediumDiffuserShadowWidthResource(@DimenRes int shadowWidth) {
        setMediumDiffuserShadowWidth(getResources().getDimensionPixelSize(shadowWidth));
    }

    /**
     * Set shadow width for small diffuser
     *
     * @param shadowWidth shadow width for big diffuser
     */
    public void setSmallDiffuserShadowWidth(int shadowWidth) {
        this.mSmallDiffuserShadowWidth = shadowWidth;
        mSmallDiffuserFullSize = getContext().getResources().getDimensionPixelSize(R.dimen.pw_small_diffuser_size) + shadowWidth * 2;
        requestLayout();
    }

    /**
     * Set shadow width for big small with dimension
     *
     * @param shadowWidth dimen resource shadow width for small diffuser
     */
    public void setSmallDiffuserShadowWidthResource(@DimenRes int shadowWidth) {
        setSmallDiffuserShadowWidth(getResources().getDimensionPixelSize(shadowWidth));
    }

    /**
     * Set big diffuser color
     *
     * @param color Color for big diffuser
     */
    public void setBigDiffuserColor(@ColorInt int color) {
        mIvBackground.setColor(color);
        mIvBackground.invalidate();
    }

    /**
     * Set big diffuser color from color resources
     *
     * @param colorRes Color resource color for big diffuser
     */
    public void setBigDiffuserColorResource(@ColorRes int colorRes) {
        setBigDiffuserColor(ContextCompat.getColor(getContext(), colorRes));
    }

    /**
     * Set medium diffuser color
     *
     * @param color Color for medium diffuser
     */
    public void setMediumDiffuserColor(@ColorInt int color) {
        mMediumDiffuserImageView.setColor(color);
        mMediumDiffuserImageView.invalidate();
    }

    /**
     * Set medium diffuser color from color res
     *
     * @param colorRes Color resource color for medium diffuser
     */
    public void setMediumDiffuserColorResource(@ColorRes int colorRes) {
        setMediumDiffuserColor(ContextCompat.getColor(getContext(), colorRes));
    }

    /**
     * Set fab player background tint
     *
     * @param tint ColorStateList as a background tint for FloatingActionButton
     */
    public void setPlayButtonBackgroundTintList(@Nullable ColorStateList tint) {
        mPlayButton.setBackgroundTintList(tint);
    }


    /**
     * Set padding for progress line
     *
     * @param padding Padding for progress line
     */
    public void setProgressLinePadding(int padding) {
        mProgressLineView.setPadding(padding);
        requestLayout();
    }

    /**
     * Set padding for progress line
     *
     * @param padding Padding for progress line
     */
    public void setProgressLinePadding(float padding) {
        mProgressLineView.setPadding(padding);
        requestLayout();
    }

    /**
     * Set padding for progress line from dimen resources
     *
     * @param paddingRes dimen resource Padding for progress line
     */
    public void setProgressLinePaddingResource(@DimenRes int paddingRes) {
        setProgressLinePadding(getResources().getDimensionPixelSize(paddingRes));
    }

    /**
     * Set padding for diffusers
     *
     * @param padding Padding for diffusers in pixels
     */
    public void setDiffusersPadding(int padding) {
        mDiffusersPadding = padding;
        requestLayout();
    }

    /**
     * Set padding for diffusers from dimen resources
     *
     * @param paddingRes dimen resource Padding for diffusers
     */
    public void setDiffusersPaddingResource(@DimenRes int paddingRes) {
        setDiffusersPadding(getResources().getDimensionPixelSize(paddingRes));
    }

    /**
     * Set radius for progress line ball indicator
     *
     * @param radius Radius for progress line ball indicator
     */
    public void setProgressBallRadius(float radius) {
        mProgressLineView.setProgressBallRadius(radius);
        requestLayout();
    }

    /**
     * Set radius for progress line ball indicator from dimen resources
     *
     * @param radiusRes dimen res Radius for progress line ball indicator
     */
    public void setProgressBallRadiusResource(@DimenRes int radiusRes) {
        setProgressBallRadius(getResources().getDimensionPixelSize(radiusRes));
    }

    /**
     * Set progress complete line stroke width
     *
     * @param strokeWidth width for complete progress line
     */
    public void setProgressCompleteLineStrokeWidth(float strokeWidth) {
        mProgressLineView.setProgressCompleteLineStrokeWidth(strokeWidth);
        requestLayout();
    }

    /**
     * Set progress complete line stroke width from dimen resources
     *
     * @param strokeWidthRes dimen resource width for complete progress line
     */
    public void setProgressCompleteLineStrokeWidthResource(@DimenRes int strokeWidthRes) {
        setProgressCompleteLineStrokeWidth(getResources().getDimensionPixelSize(strokeWidthRes));
    }

    /**
     * Set progress line stroke width
     *
     * @param strokeWidth width for progress line
     */
    public void setProgressLineStrokeWidth(float strokeWidth) {
        mProgressLineView.setProgressLineStrokeWidth(strokeWidth);
        requestLayout();
    }

    /**
     * Set progress line stroke width from dimen resources
     *
     * @param strokeWidthRes dimen resource width for progress line
     */
    public void setProgressLineStrokeWidthResource(@DimenRes int strokeWidthRes) {
        setProgressLineStrokeWidth(getResources().getDimensionPixelSize(strokeWidthRes));
    }

    /**
     * Set progress line  color
     *
     * @param color Color for progress line
     */
    public void setProgressLineColor(@ColorInt int color) {
        mProgressLineView.setProgressLineColor(color);
    }

    /**
     * Set progress line color from resource
     *
     * @param colorRes Color res Color for progress line
     */
    public void setProgressLineColorResource(@ColorRes int colorRes) {
        setProgressLineColor(ContextCompat.getColor(getContext(), colorRes));
    }

    /**
     * Set progress complete line color
     *
     * @param color Color for progress complete line
     */
    public void setProgressCompleteColor(int color) {
        mProgressLineView.setProgressCompleteColor(color);
    }


    /**
     * Set progress complete line color from resource
     *
     * @param colorRes Color res Color for progress complete line
     */
    public void setProgressCompleteColorResource(@ColorRes int colorRes) {
        setProgressCompleteColor(ContextCompat.getColor(getContext(), colorRes));
    }

    /**
     * Set color for progress ball indicator
     *
     * @param color Color for progress ball indicator
     */
    public void setProgressBallColor(int color) {
        color = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
        mProgressLineView.setProgressBallColor(color);
    }

    /**
     * Set color for progress ball indicator from resources
     *
     * @param colorRes Color resource Color for progress ball indicator
     */
    public void setProgressBallColorResource(@ColorRes int colorRes) {
        setProgressBallColor(ContextCompat.getColor(getContext(), colorRes));
    }

    /**
     * Set progressChangedListener
     *
     * @param progressChangedListener PlayLayout.OnProgressChangedListener listener for the event;
     */
    public void setOnProgressChangedListener(@Nullable PlayLayout.OnProgressChangedListener progressChangedListener) {
        mProgressLineView.setOnProgressChangedListener(progressChangedListener);
    }

    private boolean isOpenInner() {
        return mRadiusPercentage > 0.5f;
    }

    /**
     * Check if diffusers open
     *
     * @return true if diffusers open, false otherwise
     */
    public boolean isOpen() {
        return isOpenInner();
    }

    @SuppressWarnings("unused")
    private void setRadiusPercentage(float radiusPercentage) {
        this.mRadiusPercentage = radiusPercentage;
        mMediumDiffuserImageView.setRadiusPercentage(radiusPercentage);
        mIvBackground.setRadiusPercentage(radiusPercentage);
    }

    /**
     * Set shadow provider
     *
     * @param provider ShadowPercentageProvider
     */
    public void setShadowProvider(@NonNull ShadowPercentageProvider provider) {
        //noinspection ConstantConditions
        if (provider==null) {
            throw new IllegalArgumentException("ShadowPercentageProvider cannot be null");
        }
        mShadowProvider = provider;
        provider.setShadowChangerListener(this);
        if (isOpenInner()) {
            provider.setAllowChangeShadow(true);
        }
    }

    /**
     * Set shadow percentages for diffusers.
     *
     * @param bigDiffuserShadowPercentage    shadow percentage for big diffuser (0.0f - 1.0f)
     * @param mediumDiffuserShadowPercentage shadow percentage for medium diffuser (0.0f - 1.0f)
     * @param smallDiffuserShadowPercentage  shadow percentage for small diffuser (0.0f - 1.0f)
     */
    @Override
    public void shadowChanged(float bigDiffuserShadowPercentage, float mediumDiffuserShadowPercentage, float smallDiffuserShadowPercentage) {
        mBigShadowDrawable.setShadowSizeMultiplier(Utils.betweenZeroOne(bigDiffuserShadowPercentage) * (1 - BIG_DIFFUSER_MIN_SHADOW_PERCENT) + BIG_DIFFUSER_MIN_SHADOW_PERCENT);
        mMediumShadowDrawable.setShadowSizeMultiplier(Utils.betweenZeroOne(mediumDiffuserShadowPercentage) * (1 - MEDIUM_DIFFUSER_MIN_SHADOW_PERCENT) + MEDIUM_DIFFUSER_MIN_SHADOW_PERCENT);
        mSmallShadowDrawable.setShadowSizeMultiplier(Utils.betweenZeroOne(smallDiffuserShadowPercentage) * (1 - SMALL_DIFFUSER_MIN_SHADOW_PERCENT) + SMALL_DIFFUSER_MIN_SHADOW_PERCENT);
    }

    /**
     * Getter for ImageView shuffle button
     *
     * @return ImageView shuffle button
     */
    public ImageView getIvShuffle() {
        return mIvShuffle;
    }

    /**
     * Getter for ImageView skip previous button
     *
     * @return ImageView skipPrevious button
     */
    public ImageView getIvSkipPrevious() {
        return mIvSkipPrevious;
    }

    /**
     * Getter for ImageView skip next button
     *
     * @return ImageView skip next button
     */
    public ImageView getIvSkipNext() {
        return mIvSkipNext;
    }

    /**
     * Getter for ImageView repeat button
     *
     * @return ImageView repeat button
     */
    public ImageView getIvRepeat() {
        return mIvRepeat;
    }

    /**
     * Getter for ImageView playButton button
     *
     * @return ImageView play button button
     */
    public FloatingActionButton getPlayButton() {
        return mPlayButton;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mShadowProvider != null) {
            mShadowProvider.setShadowChangerListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mShadowProvider != null) {
            mShadowProvider.setShadowChangerListener(null);
        }
        super.onDetachedFromWindow();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.radiusPercentage = this.mRadiusPercentage;
        ss.progressLineEnabled = mProgressLineView.isEnabled();
        ss.progress = mProgressLineView.getProgress();

        ss.bigDiffuserColor = mIvBackground.getColor();
        ss.mediumDiffuserColor = mMediumDiffuserImageView.getColor();
        ss.playButtonTint = mPlayButton.getBackgroundTintList();

        ss.bigDiffuserShadowWidth = mBigDiffuserShadowWidth;
        ss.mediumDiffuserShadowWidth = mMediumDiffuserShadowWidth;
        ss.smallDiffuserShadowWidth = mSmallDiffuserShadowWidth;

        ss.diffuserPadding = mDiffusersPadding;
        ss.progressViewPadding = mProgressLineView.getPadding();
        ss.buttonSize = mButtonsSize;

        ss.progressBallRadius = mProgressLineView.getProgressBallRadius();
        ss.progressCompleteStrokeWidth = mProgressLineView.getProgressCompleteLineStrokeWidth();
        ss.progressLineStrokeWidth = mProgressLineView.getProgressLineStrokeWidth();

        ss.progressBallColor = mProgressLineView.getProgressBallColor();
        ss.progressCompleteLineColor = mProgressLineView.getProgressCompleteLineColor();
        ss.progressLineColor = mProgressLineView.getProgressLineColor();
        if (mShadowProvider != null) {
            ss.isAllowShadowChanging = mShadowProvider.isAllowChangeShadow();
        }
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        //begin boilerplate code so parent classes can restore state
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        this.mRadiusPercentage = ss.radiusPercentage;
        mProgressLineView.setEnabled(ss.progressLineEnabled);
        if (mProgressLineView.isEnabled() && isOpenInner()) {
            setProgress(ss.progress);
            mProgressLineView.setAlpha(1.0f);
        }

        mIvBackground.setColor(ss.bigDiffuserColor);
        mMediumDiffuserImageView.setColor(ss.mediumDiffuserColor);
        mPlayButton.setBackgroundTintList(ss.playButtonTint);

        setBigDiffuserShadowWidth(ss.bigDiffuserShadowWidth);
        setMediumDiffuserShadowWidth(ss.mediumDiffuserShadowWidth);
        setSmallDiffuserShadowWidth(ss.smallDiffuserShadowWidth);

        setDiffusersPadding(ss.diffuserPadding);
        setProgressLinePadding(ss.progressViewPadding);
        setButtonsSize(ss.buttonSize);

        setProgressBallRadius(ss.progressBallRadius);
        setProgressCompleteLineStrokeWidth(ss.progressCompleteStrokeWidth);
        setProgressLineStrokeWidth(ss.progressLineStrokeWidth);

        setProgressBallColor(ss.progressBallColor);
        setProgressCompleteColor(ss.progressCompleteLineColor);
        setProgressLineColor(ss.progressLineColor);

        mIvBackground.setRevealDrawingAlpha(1.0f);
        mIvBackground.setRadiusPercentage(mRadiusPercentage);

        if (mShadowProvider != null) {
            mShadowProvider.setAllowChangeShadow(ss.isAllowShadowChanging);
        }

        if (isOpen()) {
            revealView();
        }

    }

    private static class SavedState extends BaseSavedState {
        private float radiusPercentage;
        private boolean progressLineEnabled;
        private float progress;

        private int bigDiffuserColor;
        private int mediumDiffuserColor;
        private ColorStateList playButtonTint;

        private int bigDiffuserShadowWidth;
        private int mediumDiffuserShadowWidth;
        private int smallDiffuserShadowWidth;

        private int diffuserPadding;
        private float progressViewPadding;
        private int buttonSize;

        private float progressBallRadius;
        private float progressCompleteStrokeWidth;
        private float progressLineStrokeWidth;

        private int progressBallColor;
        private int progressCompleteLineColor;
        private int progressLineColor;
        private boolean isAllowShadowChanging;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.radiusPercentage = in.readFloat();
            this.progressLineEnabled = in.readInt() == 1;
            this.progress = in.readFloat();

            this.bigDiffuserColor = in.readInt();
            this.mediumDiffuserColor = in.readInt();
            this.playButtonTint = in.readParcelable(ColorStateList.class.getClassLoader());

            this.bigDiffuserShadowWidth = in.readInt();
            this.mediumDiffuserShadowWidth = in.readInt();
            this.smallDiffuserShadowWidth = in.readInt();

            this.diffuserPadding = in.readInt();
            this.progressViewPadding = in.readInt();
            this.buttonSize = in.readInt();

            this.progressBallRadius = in.readFloat();
            this.progressCompleteLineColor = in.readInt();
            this.progressLineStrokeWidth = in.readInt();

            this.progressBallColor = in.readInt();
            this.progressCompleteLineColor = in.readInt();
            this.progressLineColor = in.readInt();
            this.isAllowShadowChanging = in.readInt() == 1;

        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(this.radiusPercentage);
            out.writeInt(this.progressLineEnabled ? 1 : 0);
            out.writeFloat(this.progress);

            out.writeInt(this.bigDiffuserColor);
            out.writeInt(this.mediumDiffuserColor);
            out.writeParcelable(this.playButtonTint, 0);

            out.writeInt(this.bigDiffuserShadowWidth);
            out.writeInt(this.mediumDiffuserShadowWidth);
            out.writeInt(this.smallDiffuserShadowWidth);

            out.writeInt(this.diffuserPadding);
            out.writeFloat(this.progressViewPadding);
            out.writeInt(this.buttonSize);

            out.writeFloat(this.progressBallRadius);
            out.writeFloat(this.progressCompleteLineColor);
            out.writeFloat(this.progressLineStrokeWidth);

            out.writeInt(this.progressBallColor);
            out.writeInt(this.progressCompleteLineColor);
            out.writeInt(this.progressLineColor);
            out.writeInt(this.isAllowShadowChanging ? 1 : 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }


    public static abstract class ShadowPercentageProvider {
        private boolean allowChangeShadow = false;

        private OnShadowChangeListener shadowChangedListener;

        private void setShadowChangerListener(OnShadowChangeListener listener) {
            shadowChangedListener = listener;
        }

        protected void changeShadow(float bigDiffuserShadowPercentage, float mediumDiffuserShadowPercentage, float smallDiffuserShadowPercentage) {
            if (shadowChangedListener != null && isAllowChangeShadow()) {
                shadowChangedListener.shadowChanged(bigDiffuserShadowPercentage, mediumDiffuserShadowPercentage, smallDiffuserShadowPercentage);
            }
        }

        public boolean isAllowChangeShadow() {
            return allowChangeShadow;
        }


        public void setAllowChangeShadow(boolean allowChangeShadow) {
            this.allowChangeShadow = allowChangeShadow;
        }
    }

    /**
     * Interface for sending events about changing of progress by user interaction.
     */
    public interface OnProgressChangedListener {
        void onPreSetProgress();
        void onProgressChanged(float progress);
    }

    /**
     * Interface for buttons click listeners
     */
    public interface OnButtonsClickListener {
        void onShuffleClicked();

        void onSkipPreviousClicked();

        void onSkipNextClicked();

        void onRepeatClicked();

        void onPlayButtonClicked();
    }

    /**
     * Interface for buttons long click listener
     */
    public interface OnButtonsLongClickListener {
        void onShuffleLongClicked();

        void onSkipPreviousLongClicked();

        void onSkipNextLongClicked();

        void onRepeatLongClicked();

        void onPlayButtonLongClicked();
    }

    /**
     * Adapter for buttons click listener
     */
    public static abstract class OnButtonsClickListenerAdapter implements OnButtonsClickListener {

        @Override
        public void onShuffleClicked() {
        }

        @Override
        public void onSkipPreviousClicked() {
        }

        @Override
        public void onSkipNextClicked() {
        }

        @Override
        public void onRepeatClicked() {
        }

        @Override
        public void onPlayButtonClicked() {
        }
    }

    /**
     * Adapter for buttons long click listener
     */
    public static abstract class OnButtonsLongClickListenerAdapter implements OnButtonsLongClickListener {

        @Override
        public void onShuffleLongClicked() {
        }

        @Override
        public void onSkipPreviousLongClicked() {
        }

        @Override
        public void onSkipNextLongClicked() {
        }

        @Override
        public void onRepeatLongClicked() {
        }

        @Override
        public void onPlayButtonLongClicked() {
        }
    }

    /**
     * Builder for creation PlayLayout.
     */
    public static class Builder {
        private PlayLayout playLayout;

        public Builder(Context context) {
            playLayout = new PlayLayout(context);
        }

        /**
         * Set layout params for PlayLayout
         *
         * @param params LayoutParam for playLayout
         * @return
         */
        public Builder setLayoutParams(ViewGroup.LayoutParams params) {
            playLayout.setLayoutParams(params);
            return this;
        }

        /**
         * Sets a drawable as the content of this ImageView.
         *
         * @param resId the resource identifier of the drawable.
         */
        public Builder setImageResource(@DrawableRes int resId) {
            playLayout.setImageResource(resId);
            return this;
        }

        /**
         * Sets a drawable as the content of this ImageView.
         *
         * @param drawable the Drawable to set, or null to clear the content.
         */
        public Builder setImageDrawable(@Nullable Drawable drawable) {
            playLayout.setImageDrawable(drawable);
            return this;
        }

        /**
         * Sets the content of this ImageView to the specified Uri.
         *
         * @param uri the Uri of an image, or null to clear the content
         */
        public Builder setImageURI(@Nullable Uri uri) {
            playLayout.setImageURI(uri);
            return this;
        }

        /**
         * Sets a Bitmap as the content of this ImageView.
         *
         * @param bm The bitmap to set.
         */
        public Builder setImageBitmap(Bitmap bm) {
            playLayout.setImageBitmap(bm);
            return this;
        }

        /**
         * Set size for buttons (shuffle, previous, next, repeat)
         *
         * @param buttonsSize Size for buttons
         */
        public Builder setButtonsSize(int buttonsSize) {
            playLayout.setButtonsSize(buttonsSize);
            return this;
        }

        /**
         * Set size for buttons (shuffle, previous, next, repeat) from resources
         *
         * @param buttonsSizeRes dimen resource size for buttons
         */
        public Builder setButtonsSizeResource(@DimenRes int buttonsSizeRes) {
            playLayout.setButtonsSizeResource(buttonsSizeRes);
            return this;
        }

        /**
         * Enable or disable the progress line view. If enable == false progressLine is GONE
         *
         * @param enabled requested enable state
         */
        public Builder setProgressEnabled(boolean enabled) {
            playLayout.setProgressEnabled(enabled);
            return this;
        }

        /**
         * Set shadow width for big diffuser
         *
         * @param shadowWidth shadow width for big diffuser
         */
        public Builder setBigDiffuserShadowWidth(int shadowWidth) {
            playLayout.setBigDiffuserShadowWidth(shadowWidth);
            return this;
        }

        /**
         * Set shadow width for big diffuser with dimension
         *
         * @param shadowWidth dimen resource shadow width for big diffuser
         */
        public Builder setBigDiffuserShadowWidthResource(@DimenRes int shadowWidth) {
            playLayout.setBigDiffuserShadowWidthResource(shadowWidth);
            return this;
        }

        /**
         * Set shadow width for medium diffuser
         *
         * @param shadowWidth shadow width for big diffuser
         */
        public Builder setMediumDiffuserShadowWidth(int shadowWidth) {
            playLayout.setMediumDiffuserShadowWidth(shadowWidth);
            return this;
        }

        /**
         * Set shadow width for big medium with dimension
         *
         * @param shadowWidth dimen resource shadow width for medium diffuser
         */
        public Builder setMediumDiffuserShadowWidthResource(@DimenRes int shadowWidth) {
            playLayout.setMediumDiffuserShadowWidthResource(shadowWidth);
            return this;
        }

        /**
         * Set shadow width for small diffuser
         *
         * @param shadowWidth shadow width for big diffuser
         */
        public Builder setSmallDiffuserShadowWidth(int shadowWidth) {
            playLayout.setSmallDiffuserShadowWidth(shadowWidth);
            return this;
        }

        /**
         * Set shadow width for big small with dimension
         *
         * @param shadowWidthRes dimen resource shadow width for small diffuser
         */
        public Builder setSmallDiffuserShadowWidthResource(@DimenRes int shadowWidthRes) {
            playLayout.setSmallDiffuserShadowWidthResource(shadowWidthRes);
            return this;
        }

        /**
         * Set big diffuser color
         *
         * @param color Color for big diffuser
         */
        public Builder setBigDiffuserColor(@ColorInt int color) {
            playLayout.setBigDiffuserColor(color);
            return this;
        }

        /**
         * Set big diffuser color from color resources
         *
         * @param colorRes Color resource color for big diffuser
         */
        public Builder setBigDiffuserColorResource(@ColorRes int colorRes) {
            playLayout.setBigDiffuserColorResource(colorRes);
            return this;
        }

        /**
         * Set medium diffuser color
         *
         * @param color Color for medium diffuser
         */
        public Builder setMediumDiffuserColor(@ColorInt int color) {
            playLayout.setMediumDiffuserColor(color);
            return this;
        }

        /**
         * Set medium diffuser color from color res
         *
         * @param colorRes Color resource color for medium diffuser
         */
        public Builder setMediumDiffuserColorResource(@ColorRes int colorRes) {
            playLayout.setMediumDiffuserColorResource(colorRes);
            return this;
        }

        /**
         * Set fab player background tint
         *
         * @param tint ColorStateList as a background tint for FloatingActionButton
         */
        public Builder setPlayButtonBackgroundTintList(@Nullable ColorStateList tint) {
            playLayout.setPlayButtonBackgroundTintList(tint);
            return this;
        }


        /**
         * Set padding for progress line
         *
         * @param padding Padding for progress line
         */
        public Builder setProgressLinePadding(int padding) {
            playLayout.setProgressLinePadding(padding);
            return this;
        }

        /**
         * Set padding for progress line from dimen resources
         *
         * @param paddingRes dimen resource Padding for progress line
         */
        public Builder setProgressLinePaddingResource(@DimenRes int paddingRes) {
            playLayout.setProgressLinePaddingResource(paddingRes);
            return this;
        }

        /**
         * Set padding for diffusers
         *
         * @param padding Padding for diffusers in pixels
         */
        public Builder setDiffusersPadding(int padding) {
            playLayout.setDiffusersPadding(padding);
            return this;
        }

        /**
         * Set padding for diffusers from dimen resources
         *
         * @param paddingRes dimen resource Padding for diffusers
         */
        public Builder setDiffusersPaddingResource(@DimenRes int paddingRes) {
            playLayout.setDiffusersPaddingResource(paddingRes);
            return this;
        }

        /**
         * Set radius for progress line ball indicator
         *
         * @param radius Radius for progress line ball indicator
         */
        public Builder setProgressBallRadius(float radius) {
            playLayout.setProgressBallRadius(radius);
            return this;
        }

        /**
         * Set radius for progress line ball indicator from dimen resources
         *
         * @param radiusRes dimen res Radius for progress line ball indicator
         */
        public Builder setProgressBallRadiusResource(@DimenRes int radiusRes) {
            playLayout.setProgressBallRadiusResource(radiusRes);
            return this;
        }

        /**
         * Set progress complete line stroke width
         *
         * @param strokeWidth width for complete progress line
         */
        public Builder setProgressCompleteLineStrokeWidth(float strokeWidth) {
            playLayout.setProgressCompleteLineStrokeWidth(strokeWidth);
            return this;
        }

        /**
         * Set progress complete line stroke width from dimen resources
         *
         * @param strokeWidthRes dimen resource width for complete progress line
         */
        public Builder setProgressCompleteLineStrokeWidthResource(@DimenRes int strokeWidthRes) {
            playLayout.setProgressCompleteLineStrokeWidthResource(strokeWidthRes);
            return this;
        }

        /**
         * Set progress line stroke width
         *
         * @param strokeWidth width for progress line
         */
        public Builder setProgressLineStrokeWidth(float strokeWidth) {
            playLayout.setProgressLineStrokeWidth(strokeWidth);
            return this;
        }

        /**
         * Set progress line stroke width from dimen resources
         *
         * @param strokeWidthRes dimen resource width for progress line
         */
        public Builder setProgressLineStrokeWidthResource(@DimenRes int strokeWidthRes) {
            playLayout.setProgressLineStrokeWidthResource(strokeWidthRes);
            return this;
        }

        /**
         * Set progress line  color
         *
         * @param color Color for progress line
         */
        public Builder setProgressLineColor(@ColorInt int color) {
            playLayout.setProgressLineColor(color);
            return this;
        }

        /**
         * Set progress line color from resource
         *
         * @param colorRes Color res Color for progress line
         */
        public Builder setProgressLineColorResource(@ColorRes int colorRes) {
            playLayout.setProgressLineColorResource(colorRes);
            return this;
        }

        /**
         * Set progress complete line color
         *
         * @param color Color for progress complete line
         */
        public Builder setProgressCompleteColor(int color) {
            playLayout.setProgressCompleteColor(color);
            return this;
        }

        /**
         * Set progress complete line color from resource
         *
         * @param colorRes Color res Color for progress complete line
         */
        public Builder setProgressCompleteColorResource(@ColorRes int colorRes) {
            playLayout.setProgressCompleteColorResource(colorRes);
            return this;
        }

        /**
         * Set color for progress ball indicator
         *
         * @param color Color for progress ball indicator
         */
        public Builder setProgressBallColor(int color) {
            playLayout.setProgressBallColor(color);
            return this;
        }

        /**
         * Set color for progress ball indicator from resources
         *
         * @param colorRes Color resource Color for progress ball indicator
         */
        public Builder setProgressBallColorResource(@ColorRes int colorRes) {
            playLayout.setProgressBallColor(colorRes);
            return this;
        }

        /**
         * Set progressChanged Listener
         *
         * @param progressChangedListener PlayLayout.OnProgressChangedListener listener for the event;
         */
        public Builder setProgressChangedListener(@Nullable PlayLayout.OnProgressChangedListener progressChangedListener) {
            playLayout.setOnProgressChangedListener(progressChangedListener);
            return this;
        }

        /**
         * Set buttons click listener
         */
        public Builder setOnButtonsClickListener(@Nullable OnButtonsClickListener listener) {
            playLayout.setOnButtonsClickListener(listener);
            return this;
        }

        /**
         * Set buttons long click listener
         */
        public Builder setOnButtonsLongClickListener(@Nullable OnButtonsLongClickListener listener) {
            playLayout.setOnButtonsLongClickListener(listener);
            return this;
        }

        /**
         * Set shadow provider
         */
        public Builder setShadowProvider(@NonNull ShadowPercentageProvider provider) {
            playLayout.setShadowProvider(provider);
            return this;
        }


        /**
         * Create PlayLayout
         *
         * @return PlayLayout Widget
         */
        public PlayLayout build() {
            return playLayout;
        }
    }

}