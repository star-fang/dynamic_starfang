package com.starfang.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starfang.R;
import com.starfang.SignInActivity;
import com.starfang.realm.transaction.SyncTask;
import com.starfang.ui.dynamic.DynamicSpreadSheetFragment;

public class HomeFragment extends Fragment {

    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        final AppCompatTextView text_provider_id = root.findViewById(R.id.text_provider_id);
        final AppCompatTextView text_uid = root.findViewById(R.id.text_uid);
        final AppCompatTextView text_name = root.findViewById(R.id.text_name);
        final AppCompatTextView text_mail = root.findViewById(R.id.text_mail);
        final AppCompatButton button_sign_out = root.findViewById(R.id.button_sign_out);
        final AppCompatImageView image_profile = root.findViewById(R.id.image_profile);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            text_name.setText(user.getDisplayName());
            text_mail.setText(user.getEmail());
            text_uid.setText(user.getUid());
            image_profile.setImageURI(user.getPhotoUrl());
            text_provider_id.setText(user.getProviderId());
        }

        button_sign_out.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            LoginManager.getInstance().logOut();
            Activity activity = getActivity();
            if( activity != null ) {
                startActivity(new Intent(activity, SignInActivity.class));
                activity.finish();
            }
        });

        final AppCompatButton button_sync = root.findViewById(R.id.button_sync);
        button_sync.setOnClickListener(v -> {
            new SyncTask(mContext).execute();
        });

        final AppCompatButton button_show = root.findViewById(R.id.button_show);
        button_show.setOnClickListener(v -> {
            DynamicSpreadSheetFragment.newInstance().show(getParentFragmentManager(),"show");
        });
        return root;
    }
}
