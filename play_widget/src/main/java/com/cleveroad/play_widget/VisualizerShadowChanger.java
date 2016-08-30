package com.cleveroad.play_widget;

import android.media.audiofx.Visualizer;

/**
 * Shadow provider that depends on audio playing in player.
 */
public class VisualizerShadowChanger extends PlayLayout.ShadowPercentageProvider implements Visualizer.OnDataCaptureListener {
    /**
     * Maximum value of dB. Used for controlling wave height percentage.
     */
    private boolean mVisualisationEnabled = false;
    private static final float MAX_DB_VALUE = 45;

    private static final int LOW_FREQUENCY = 300;
    private static final int MID_FREQUENCY = 2500;
    private static final int HIGH_FREQUENCY = 3000;

    private static final float[] SOUND_INDEX_COEFFICIENTS = new float[]{
            LOW_FREQUENCY / 44100f,
            MID_FREQUENCY / 44100f,
            HIGH_FREQUENCY / 44100f,
    };

    private static final float FILTRATION_ALPHA = 0.55f;
    private static final float FILTRATION_BETA = 1 - FILTRATION_ALPHA;
    private float[] mDbsPercentagesConcrete = new float[SOUND_INDEX_COEFFICIENTS.length];
    private Visualizer mVisualizer;

    public VisualizerShadowChanger() {
        mVisualizer = new Visualizer(0);
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(this, Visualizer.getMaxCaptureRate(), false, true);
    }

    /**
     * Enable or disable visualisation. You should enable state on resume and disable on pause.
     *
     * @param enabled requested enable state.
     */
    public void setEnabledVisualization(boolean enabled) {
        mVisualisationEnabled = enabled;
        updateVisualisationEnable();
    }

    public void release() {
        if (mVisualizer != null) {
            mVisualizer.release();
        }
        mVisualizer = null;
    }

    private void updateVisualisationEnable() {
        if (mVisualizer != null) {
            mVisualizer.setEnabled(mVisualisationEnabled && isAllowChangeShadow());
        }
    }

    @Override
    public void setAllowChangeShadow(boolean allowChangeShadow) {
        super.setAllowChangeShadow(allowChangeShadow);
        updateVisualisationEnable();
    }

    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        int dataSize = fft.length / 2 - 1;
        for (int i = 0; i < SOUND_INDEX_COEFFICIENTS.length; i++) {
            int index = (int) (SOUND_INDEX_COEFFICIENTS[i] * dataSize);
            byte real = fft[2 * index];
            byte imag = fft[2 * index + 1];
            long magnitudeSquare = real * real + imag * imag;
            magnitudeSquare = (long) Math.sqrt(magnitudeSquare);
            float dbs = magnitudeToDb(magnitudeSquare);
            float dbPercentage = dbs / MAX_DB_VALUE;
            if (dbPercentage > 1.0f) {
                dbPercentage = 1.0f;
            }
            mDbsPercentagesConcrete[i] = mDbsPercentagesConcrete[i] * FILTRATION_ALPHA + dbPercentage * FILTRATION_BETA;
        }

        changeShadow(mDbsPercentagesConcrete[0], mDbsPercentagesConcrete[1], mDbsPercentagesConcrete[2]);

    }

    private float magnitudeToDb(float squareMag) {
        if (squareMag == 0) {
            return 0;
        }
        return (float) (20 * Math.log10(squareMag));
    }

}