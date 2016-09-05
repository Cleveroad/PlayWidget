package com.cleveroad.sample;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cleveroad.play_widget.PlayLayout;
import com.cleveroad.play_widget.VisualizerShadowChanger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    public static final String EXTRA_SELECT_TRACK = "EXTRA_SELECT_TRACK";
    public static final String EXTRA_FILE_URIS = "EXTRA_FILE_URIS";
    private static final long UPDATE_INTERVAL = 20;
    private static final int MY_PERMISSIONS_REQUEST_READ_AUDIO = 11;

    private PlayLayout mPlayLayout;
    private VisualizerShadowChanger mShadowChanger;
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private boolean preparing;
    private int playingIndex = -1;
    private boolean paused;
    private final List<MusicItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_main);
        mPlayLayout = (PlayLayout) findViewById(R.id.revealView);
        mPlayLayout.setOnButtonsClickListener(new PlayLayout.OnButtonsClickListenerAdapter() {
            @Override
            public void onPlayButtonClicked() {
                playButtonClicked();
            }

            @Override
            public void onSkipPreviousClicked() {
                onPreviousClicked();
                if (!mPlayLayout.isOpen()) {
                    mPlayLayout.startRevealAnimation();
                }
            }

            @Override
            public void onSkipNextClicked() {
                onNextClicked();
                if (!mPlayLayout.isOpen()) {
                    mPlayLayout.startRevealAnimation();
                }
            }

            @Override
            public void onShuffleClicked() {
                Toast.makeText(MainActivity.this, "Stub", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRepeatClicked() {
                Toast.makeText(MainActivity.this, "Stub", Toast.LENGTH_SHORT).show();
            }
        });
        mPlayLayout.setOnProgressChangedListener(new PlayLayout.OnProgressChangedListener() {
            @Override
            public void onPreSetProgress() {
                stopTrackingPosition();
            }

            @Override
            public void onProgressChanged(float progress) {
                Log.i("onProgressChanged", "Progress = " + progress);
                mediaPlayer.seekTo((int) (mediaPlayer.getDuration() * progress));
                startTrackingPosition();
            }

        });

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayLayout.fastOpen();
        selectNewTrack(getIntent());
    }

    private void checkVisualiserPermissions() {
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
        if (mShadowChanger == null) {
            mShadowChanger = VisualizerShadowChanger.newInstance(mediaPlayer.getAudioSessionId());
            mShadowChanger.setEnabledVisualization(true);
            mPlayLayout.setShadowProvider(mShadowChanger);
            Log.i("startVisualiser", "startVisualiser " + mediaPlayer.getAudioSessionId());
        }
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
        stopTrackingPosition();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
        super.onDestroy();
    }

    private void selectNewTrack(Intent intent) {
        if (preparing) {
            return;
        }
        if (intent.hasExtra(EXTRA_FILE_URIS)) {
            addNewTracks(intent);
        }
        MusicItem item = intent.getParcelableExtra(EXTRA_SELECT_TRACK);
        if (item == null && playingIndex == -1 || playingIndex != -1 && items.get(playingIndex).equals(item)) {
            if (mediaPlayer.isPlaying()) {
                mPlayLayout.startDismissAnimation();
            } else {
                mPlayLayout.startRevealAnimation();
            }
            return;
        }
        playingIndex = items.indexOf(item);
        startCurrentTrack();
    }

    private void setImageForItem() {
        Glide.with(this)
                .load(items.get(playingIndex).albumArtUri())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .error(R.drawable.white_centered_bordered_song_note_image)
                .into(imageTarget);
    }

    private void startCurrentTrack() {
        setImageForItem();
        if (mediaPlayer.isPlaying() || paused) {
            mediaPlayer.stop();
            paused = false;
        }
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(this, items.get(playingIndex).fileUri());
            mediaPlayer.prepareAsync();
            preparing = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addNewTracks(Intent intent) {
        MusicItem playingItem = null;
        if (playingIndex != -1)
            playingItem = items.get(playingIndex);
        items.clear();
        Parcelable[] items = intent.getParcelableArrayExtra(EXTRA_FILE_URIS);
        for (Parcelable item : items) {
            if (item instanceof MusicItem)
                this.items.add((MusicItem) item);
        }
        if (playingItem == null) {
            playingIndex = -1;
        } else {
            playingIndex = this.items.indexOf(playingItem);
        }
        if (playingIndex == -1 && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        preparing = false;
        mediaPlayer.start();
        stopTrackingPosition();
        startTrackingPosition();
        checkVisualiserPermissions();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (playingIndex == -1) {
//            audioWidget.controller().stop();
            if (mPlayLayout != null) {
                mPlayLayout.startDismissAnimation();
            }
            return;
        }
        playingIndex++;
        if (playingIndex >= items.size()) {
            playingIndex = 0;
            if (items.size() == 0) {
//                audioWidget.controller().stop();
                return;
            }
        }
        startCurrentTrack();
    }

    public void onNextClicked() {
        if (items.size() == 0)
            return;
        playingIndex++;
        if (playingIndex >= items.size()) {
            playingIndex = 0;
        }
        startCurrentTrack();
    }

    public void onPreviousClicked() {
        if (items.size() == 0)
            return;
        playingIndex--;
        if (playingIndex < 0) {
            playingIndex = items.size() - 1;
        }
        startCurrentTrack();
    }

    private void startTrackingPosition() {
        timer = new Timer("MainActivity Timer");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                MediaPlayer tempMediaPlayer = mediaPlayer;
                if (tempMediaPlayer != null && tempMediaPlayer != null && tempMediaPlayer.isPlaying()) {

                    mPlayLayout.setPostProgress((float) tempMediaPlayer.getCurrentPosition() / tempMediaPlayer.getDuration());
                }

            }
        }, UPDATE_INTERVAL, UPDATE_INTERVAL);
    }


    private void stopTrackingPosition() {
        if (timer == null)
            return;
        timer.cancel();
        timer.purge();
        timer = null;
    }

    private void playButtonClicked() {
        if (mPlayLayout == null) {
            return;
        }
        if (mPlayLayout.isOpen()) {
            mediaPlayer.pause();
            mPlayLayout.startDismissAnimation();
        } else {
            mediaPlayer.start();
            mPlayLayout.startRevealAnimation();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        preparing = true;
        return false;
    }

    private SimpleTarget<GlideDrawable> imageTarget = new SimpleTarget<GlideDrawable>() {

        @Override
        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
            mPlayLayout.setImageDrawable(resource);
        }

        @Override
        public void onLoadFailed(Exception e, Drawable errorDrawable) {
            super.onLoadFailed(e, errorDrawable);
            mPlayLayout.setImageDrawable(errorDrawable);
        }
    };
}
