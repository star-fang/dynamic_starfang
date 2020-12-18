package com.starfang.fragments.progress.legacy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.starfang.R;
import com.starfang.StarfangConstants;

public class ProgressFragment extends Fragment {

    public static ProgressFragment newInstance(String defaultText) {
        ProgressFragment progressFragment = new ProgressFragment();
        Bundle bundle = new Bundle();
        bundle.putString(StarfangConstants.EXTRA_PROGRESS_TEXT, defaultText);
        progressFragment.setArguments(bundle);
        return progressFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_progress, container, false);

        final AppCompatTextView text_title = root.findViewById(R.id.text_progress_title);
        final AppCompatImageButton button_quit = root.findViewById(R.id.button_quit_progress);
        final ContentLoadingProgressBar progressBar = root.findViewById(R.id.progress);
        final AppCompatTextView text_below = root.findViewById(R.id.text_progress_below);
        final AppCompatTextView text_above = root.findViewById(R.id.text_progress_above);

        final ProgressViewModel progressViewModel = new ViewModelProvider(this).get(ProgressViewModel.class);
        final LifecycleOwner owner = getViewLifecycleOwner();
        progressViewModel.getTitleText().observe(owner, text_title::setText);

        progressViewModel.getProgress().observe(owner, progress -> {
            if (progress == 0) {
                progressBar.setProgress(progress);
            } else if (progress <= 100 && progress > 0) {
                ProgressBarAnimation animation = new ProgressBarAnimation(
                        progressBar, progressViewModel
                        , progress
                );
                animation.setDuration(1000);
                root.startAnimation(animation);

            } else {
                progressBar.setProgress(100);
            }
        });

        progressViewModel.getIndeterminate().observe(owner, progressBar::setIndeterminate);

        progressViewModel.getAboveText().observe(owner, text_above::setText);
        progressViewModel.getBelowText().observe(owner, text_below::setText);

        progressViewModel.getTopStartText().observe(owner, (
                (AppCompatTextView) root.findViewById(R.id.text_progress_top_start))::setText);

        progressViewModel.getTopEndText().observe(owner, (
                (AppCompatTextView) root.findViewById(R.id.text_progress_top_end))::setText);

        progressViewModel.getQuitVisibility().observe(owner, button_quit::setVisibility);

        button_quit.setOnClickListener(v -> {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.getSupportFragmentManager().beginTransaction().remove(this).commit();
            }
        });

        Bundle args = getArguments();
        if (args != null) {
            text_title.setText(args.getString(StarfangConstants.EXTRA_PROGRESS_TEXT));
        }


        return root;
    }

}
