package com.starfang.fragments.progress.row;

import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.ContentLoadingProgressBar;

import java.text.DecimalFormat;
import java.text.MessageFormat;

public class ProgressRowAnimation extends Animation {
    private final ContentLoadingProgressBar progressBar;
    private final AppCompatTextView text_value;
    private final float to;
    private final float from;

    ProgressRowAnimation(ContentLoadingProgressBar progressBar, AppCompatTextView text_value, float to) {
        this.text_value = text_value;
        this.progressBar = progressBar;
        this.to = to;
        this.from = progressBar.getProgress();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float value = from + (to - from) * interpolatedTime;
        int progress = (int) value;
        progressBar.setProgress(progress);
        text_value.setText(MessageFormat.format("{0}%", new DecimalFormat("#.##").format(value)));
    }
}
