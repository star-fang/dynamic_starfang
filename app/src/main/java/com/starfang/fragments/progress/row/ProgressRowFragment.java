package com.starfang.fragments.progress.row;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

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

public class ProgressRowFragment extends Fragment {

    public static final String KEY_PROGRESS_ROW_TITLE = "progress_title";

    public static ProgressRowFragment newInstance(String title, int marginTop) {
        ProgressRowFragment progressFragment = new ProgressRowFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("margin_top",marginTop);
        bundle.putString(KEY_PROGRESS_ROW_TITLE, title);
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

        final View root = inflater.inflate(R.layout.fragment_progress_row, container, false);

        final AppCompatTextView text_title = root.findViewById(R.id.text_progress_title);
        final AppCompatImageButton button_quit = root.findViewById(R.id.button_quit_progress);
        final ContentLoadingProgressBar progressBar = root.findViewById(R.id.progress_bar);
        final AppCompatTextView text_step = root.findViewById(R.id.text_progress_step);
        final AppCompatTextView text_detail = root.findViewById(R.id.text_progress_detail);
        final AppCompatTextView text_value = root.findViewById(R.id.text_progress_value);

        final ProgressRowViewModel progressViewModel = new ViewModelProvider(this).get(ProgressRowViewModel.class);
        final LifecycleOwner owner = getViewLifecycleOwner();


        Bundle args = getArguments();
        if(args != null ) {
            text_title.setText(args.getString(KEY_PROGRESS_ROW_TITLE));
            //ViewGroup.LayoutParams layoutParams = root.getLayoutParams();
            //if( layoutParams instanceof  FrameLayout.LayoutParams) {
            //    ((FrameLayout.LayoutParams) layoutParams).setMargins(0,args.getInt("margin_top"),0,0);
            //}
        }

        progressViewModel.getProgressLiveData().observe(owner, progress -> {
            if (progress == 0) {
                progressBar.setProgress(progress);
            } else if (progress <= 100 && progress > 0) {
                ProgressRowAnimation animation = new ProgressRowAnimation(
                        progressBar, text_value, progress
                );
                animation.setDuration(1000);
                root.startAnimation(animation);

            } else {
                progressBar.setProgress(100);
            }
        });

        progressViewModel.getIndeterminate().observe(owner, progressBar::setIndeterminate);

        progressViewModel.getStepTextLiveData().observe(owner, text_step::setText);
        progressViewModel.getDetailTextLiveData().observe(owner, text_detail::setText);
        progressViewModel.getRemoveFragment().observe(owner, remove -> {
            if( remove ) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.getSupportFragmentManager().beginTransaction().remove(this).commit();
                }
            }
        });


        return root;
    }

}
