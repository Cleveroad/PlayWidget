package com.cleveroad.sample;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;

import com.cleveroad.play_widget.PlayLayout;
import com.cleveroad.play_widget.VisualizerShadowChanger;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_AUDIO = 11;

    private PlayLayout mPlayLayout;
    private VisualizerShadowChanger mShadowChanger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPlayLayout = (PlayLayout) findViewById(R.id.revealView);
        mPlayLayout.setOnButtonsClickListener(new PlayLayout.OnButtonsClickListenerAdapter() {
            @Override
            public void onPlayButtonClicked() {
                playButtonClicked();
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
            startVisualiser();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.MODIFY_AUDIO_SETTINGS)) {
                AlertDialog.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            requestPermissions();
                        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                            permissionsNotGranted();
                        }
                    }
                };
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.title_permissions))
                        .setMessage(Html.fromHtml(getString(R.string.message_permissions)))
                        .setPositiveButton(getString(R.string.btn_next), onClickListener)
                        .setNegativeButton(getString(R.string.btn_cancel), onClickListener)
                        .show();
            } else {
                requestPermissions();
            }
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS},
                MY_PERMISSIONS_REQUEST_READ_AUDIO
        );
    }

    private void permissionsNotGranted() {

    }

    private void startVisualiser() {
        mShadowChanger = new VisualizerShadowChanger();
        mPlayLayout.setShadowProvider(mShadowChanger);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_AUDIO) {
            boolean bothGranted = true;
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.RECORD_AUDIO.equals(permissions[i]) || Manifest.permission.MODIFY_AUDIO_SETTINGS.equals(permissions[i])) {
                    bothGranted &= grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }
            if (bothGranted) {
                startVisualiser();
            } else {
                permissionsNotGranted();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mShadowChanger != null) {
            mShadowChanger.setEnabledVisualization(true);
        }
    }

    @Override
    protected void onPause() {
        if (mShadowChanger != null) {
            mShadowChanger.setEnabledVisualization(false);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mShadowChanger != null) {
            mShadowChanger.release();
        }
        super.onDestroy();
    }

    void playButtonClicked() {
        if (mPlayLayout == null) {
            return;
        }
        Intent i = new Intent("com.android.music.musicservicecommand");
        if (mPlayLayout.isOpen()) {
            AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (mAudioManager.isMusicActive()) {
                i.putExtra("command", "pause");
                sendBroadcast(i);
            }
            mPlayLayout.startDismissAnimation();
        } else {

            i.putExtra("command", "play");
            sendBroadcast(i);
            mPlayLayout.startRevealAnimation();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            valueAnimator.setDuration(1200);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mPlayLayout.setProgress((Float) animation.getAnimatedValue());
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {

//                    int[][] states = {
//                            {android.R.attr.state_enabled},
//                            {android.R.attr.state_pressed},
//                    };
//
//                    int[] colors = {
//                            Color.GREEN,
//                            Color.RED,
//                    };
//
//                    ColorStateList colorStateList = new ColorStateList(states, colors);
//
//                    mPlayLayout.setPlayButtonBackgroundTintList(colorStateList);
//                    mPlayLayout.setBigDiffuserColor(Color.argb(100, 200, 150, 50));
//                    mPlayLayout.setMediumDiffuserColor(Color.argb(100, 200, 150, 50));
//
//                    mPlayLayout.setButtonsSize(20);
//                    mPlayLayout.setProgressLineStrokeWidth(30);
//                    mPlayLayout.setProgressCompleteLineStrokeWidth(40);
//                    mPlayLayout.setProgressBallRadius(30);
//
//                    mPlayLayout.setBigDiffuserShadowWidth(8);
//                    mPlayLayout.setMediumDiffuserShadowWidth(8);
//                    mPlayLayout.setSmallDiffuserShadowWidth(8);
//
//                    mPlayLayout.setProgressBallColor(Color.argb(100, 50, 150, 200));
//                    mPlayLayout.setProgressCompleteColor(Color.argb(100, 50, 150, 200));
//                    mPlayLayout.setProgressLineColor(Color.argb(100, 50, 150, 200));
//
//                    mPlayLayout.setDiffusersPadding(20);
//                    mPlayLayout.setProgressLinePadding(20);

                    super.onAnimationEnd(animation);
                }
            });
            valueAnimator.start();
        }

    }
}
