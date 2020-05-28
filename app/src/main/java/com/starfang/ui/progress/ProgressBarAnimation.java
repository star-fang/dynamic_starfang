package com.starfang.ui.progress;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.core.widget.ContentLoadingProgressBar;

import java.text.DecimalFormat;
import java.text.MessageFormat;

public class ProgressBarAnimation extends Animation {
    private ContentLoadingProgressBar progressBar;
    private ProgressViewModel viewModel;
    private float to;
    private float from;

    ProgressBarAnimation(ContentLoadingProgressBar progressBar, ProgressViewModel viewModel, float to) {
        this.progressBar = progressBar;
        this.to = to;
        this.from = progressBar.getProgress();
        this.viewModel = viewModel;
        //Log.d("FANG_ANI", "from: " + from + ", to: " + to);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float value = from + (to - from) * interpolatedTime;
        long max = viewModel.getMaxByteValue();
        long cur = (long) (max * (value / 100f));
        int progress = (int) value;
        progressBar.setProgress(progress);
        //Log.d("FANG_ANI", "prog: " + progress);
        viewModel.setAboveText(MessageFormat.format("{0}%", new DecimalFormat("#.##").format(value)));
        viewModel.setBelowText(MessageFormat.format("{0} / {1}", cur, max));

    }
}
