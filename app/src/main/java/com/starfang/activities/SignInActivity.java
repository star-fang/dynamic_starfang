package com.starfang.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.request.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.starfang.R;
import com.starfang.StarfangConstants;
import com.starfang.fragments.progress.legacy.ProgressFragment;
import com.starfang.utilities.CipherUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "FANG_ACT_SIGN_IN";
    private static final int RC_SIGN_IN_WITH_GOOGLE = 9001;
    private static final int RC_LOGIN_WITH_FACEBOOK = 8008;
    private static final String PARAM_IV = "iv";
    private static final String PARAM_EE_UID = "ee_uid";
    private static final String PARAM_OT_ID = "ot_id";
    private static final String ECHO_OT_ID_ENC = "ot_id_enc";
    private static final String ECHO_OT_IV = "ot_iv";
    private static final String ECHO_EE_ID = "ee_id";
    private static final String ECHO_EE_PRIV = "ee_priv_id";
    private static final String ECHO_NEW_IV = "new_iv";
    private static final String ECHO_STATUS = "status";
    private static final String ECHO_MESSAGE = "message";
    private static final String STATUS_SUCCESS = "succ";

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;

    // Facebook login callback
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_in);

        mCallbackManager = CallbackManager.Factory.create();

        // Assign fields
        final SignInButton mSignInWithGoogleButton = findViewById(R.id.sign_in_with_google_button);
        final LoginButton mLoginWithFacebookButton = findViewById(R.id.login_with_facebook_button);

        setGoogleSignInButtonText(mSignInWithGoogleButton, R.string.google_sign_in);
        mSignInWithGoogleButton.setOnClickListener(v -> signInWithGoogle());

        mLoginWithFacebookButton.setReadPermissions("email", "public_profile");
        mLoginWithFacebookButton.setLoginText(getString(R.string.facebook_login));
        mLoginWithFacebookButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    private void signInWithGoogle() {
        // Configure Google Sign In
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        final GoogleSignInClient mSignInClient = GoogleSignIn.getClient(this, gso);
        Intent intent = mSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN_WITH_GOOGLE);
    }

    /*
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error xhas occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN_WITH_GOOGLE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null && result.isSuccess()) {
                // Google Sign-In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                if (account != null) {
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                    signInWithCredential(GoogleAuthProvider.getCredential(account.getIdToken(), null), resultCode);
                }
            } else {
                // Google Sign-In failed
                Log.e(TAG, "Google Sign-In failed.");
            }
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "facebook access token: " + token);
        signInWithCredential(FacebookAuthProvider.getCredential(token.getToken()), RC_LOGIN_WITH_FACEBOOK);
    }

    protected void removeFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .remove(fragment)
                .commit();
    }

    private void signInWithCredential(AuthCredential credential, int rc) {

        final ProgressFragment progressFragment = ProgressFragment.newInstance("로그인 중");
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, progressFragment)
                .commit();


        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "signInWithCredential", task.getException());
                        Toast.makeText(SignInActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        removeFragment(progressFragment);
                        if (rc == RC_LOGIN_WITH_FACEBOOK)
                            LoginManager.getInstance().logOut();
                    } else {
                        Log.d(TAG, "i am working");

                        Runnable runnable = () -> {

                            final String cipher_algorithm = getString(R.string.cipher_algorithm);
                            final String cipher_instance = getString(R.string.cipher_instance);

                            try {

                                final String stringKey = CipherUtils.getSecureSharedPreferences(this)
                                        .getString(StarfangConstants.SECURE_PREF_SECRET_KEY, null);


                                if (stringKey != null) {
                                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                    if (user != null) {
                                        final String uid = user.getUid();
                                        RequestFuture<JSONObject> requestLoginFuture = RequestFuture.newFuture();

                                        Map<String, String> requestParams = new HashMap<>();
                                        byte[] firstIv= CipherUtils.generateIv(cipher_instance);
                                        requestParams.put(PARAM_IV, Base64.encodeToString(firstIv, Base64.DEFAULT));

                                        JsonObjectRequest rLoginRequest = new JsonObjectRequest(
                                                Request.Method.POST,
                                                getString(R.string.url_sync_default) + getString(R.string.php_login_request),
                                                new JSONObject(requestParams),
                                                requestLoginFuture, requestLoginFuture) {

                                            /* 2020-05-19 POST vs GET
                                            post request data는 body, get request data는 header에 있음
                                            */
                                            @Override
                                            public Map<String, String> getHeaders() {
                                                Map<String, String> headers = new HashMap<>();
                                                headers.put("Content-Type", "application/json; charset=utf-8");
                                                //Log.d(TAG, "header? " + headers.toString());
                                                return headers;
                                            }

                                        };

                                        rLoginRequest.setRetryPolicy(new DefaultRetryPolicy(
                                                6000,
                                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                                        Volley.newRequestQueue(this).add(rLoginRequest);

                                        try {
                                            JSONObject rLoginEcho = requestLoginFuture.get(20, TimeUnit.SECONDS);
                                            Log.d(TAG,"rLoginEcho: " + rLoginEcho);
                                            try {
                                                String message = rLoginEcho.getString(ECHO_MESSAGE);
                                                Snackbar.make(findViewById(android.R.id.content).getRootView(), message, Snackbar.LENGTH_LONG).show();
                                            } catch (JSONException ignore) {
                                            }
                                            try {
                                                if (rLoginEcho.getString(ECHO_STATUS).equals(STATUS_SUCCESS)) {
                                                    byte[] otId_enc = Base64.decode(rLoginEcho.getString(ECHO_OT_ID_ENC), Base64.DEFAULT);
                                                    String otId = new String(CipherUtils.decrypt(
                                                            cipher_algorithm
                                                            , cipher_instance
                                                            , stringKey, otId_enc, firstIv), StandardCharsets.UTF_8);

                                                    byte[] otIv = Base64.decode(rLoginEcho.getString(ECHO_OT_IV), Base64.DEFAULT);
                                                    byte[] eUid = CipherUtils.encrypt(cipher_algorithm, cipher_instance,
                                                            stringKey, uid.getBytes(StandardCharsets.UTF_8), otIv);
                                                    RequestFuture<JSONObject> certifyFuture = RequestFuture.newFuture();

                                                    Map<String, String> certifyParams = new HashMap<>();
                                                    certifyParams.put(PARAM_EE_UID, Base64.encodeToString(eUid, Base64.DEFAULT));
                                                    certifyParams.put(PARAM_OT_ID, otId);

                                                    JsonObjectRequest cLoginRequest = new JsonObjectRequest(
                                                            Request.Method.POST,
                                                            getString(R.string.url_sync_default) + getString(R.string.php_login_certify),
                                                            new JSONObject(certifyParams),
                                                            certifyFuture, certifyFuture) {


                                                        @Override
                                                        public Map<String, String> getHeaders() {
                                                            Map<String, String> headers = new HashMap<>();
                                                            headers.put("Content-Type", "application/json; charset=utf-8");
                                                            return headers;
                                                        }
                                                    };

                                                    cLoginRequest.setRetryPolicy(new DefaultRetryPolicy(
                                                            6000,
                                                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                                                    Volley.newRequestQueue(this).add(cLoginRequest);

                                                    JSONObject cLoginEcho = certifyFuture.get(20, TimeUnit.SECONDS);
                                                    Log.d(TAG,"cLoginEcho: " + cLoginEcho);
                                                    try {
                                                        String message = cLoginEcho.getString(ECHO_MESSAGE);
                                                        Snackbar.make(findViewById(android.R.id.content).getRootView(), message, Snackbar.LENGTH_LONG).show();
                                                    } catch (JSONException ignore) {
                                                    }

                                                    if (cLoginEcho.getString(ECHO_STATUS).equals(STATUS_SUCCESS)) {
                                                        byte[] e_id = Base64.decode(cLoginEcho.getString(ECHO_EE_ID), Base64.DEFAULT);
                                                        byte[] e_priv_id = Base64.decode(cLoginEcho.getString(ECHO_EE_PRIV), Base64.DEFAULT);
                                                        String new_iv = cLoginEcho.getString(ECHO_NEW_IV);


                                                        String id = new String(CipherUtils.decrypt(
                                                                cipher_algorithm,
                                                                cipher_instance,
                                                                stringKey,
                                                                e_id,
                                                                otIv
                                                        ), StandardCharsets.UTF_8);

                                                        String privilege = new String(CipherUtils.decrypt(
                                                                cipher_algorithm,
                                                                cipher_instance,
                                                                stringKey,
                                                                e_priv_id,
                                                                otIv
                                                        ), StandardCharsets.UTF_8);

                                                        SharedPreferences sharedPreferences = getSharedPreferences(StarfangConstants.SHARED_PREF_STORE, MODE_PRIVATE);
                                                        if (sharedPreferences.edit().putString(StarfangConstants.PREF_ID_KEY, id).commit()
                                                                && sharedPreferences.edit().putString(StarfangConstants.PREF_PRIVILEGE_KEY, privilege).commit()
                                                                && sharedPreferences.edit().putString(StarfangConstants.PREF_IV_KEY, new_iv).commit()
                                                        ) {
                                                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                                            finish();
                                                            return;
                                                        }

                                                    } // if certify login succ
                                                } // if request login succ
                                                signOut(rc, progressFragment);
                                            } catch (JSONException e) {
                                                Log.e(TAG, Log.getStackTraceString(e));
                                                signOut(rc, progressFragment);
                                            }
                                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                                            Log.e(TAG, Log.getStackTraceString(e));
                                            signOut(rc, progressFragment);
                                        }


                                    } // if user != null
                                } // if stringKey != null
                            } catch (GeneralSecurityException | IOException e) {
                                Log.e(TAG, Log.getStackTraceString(e));
                                signOut(rc, progressFragment);
                            }


                        };

                        AsyncTask.execute(runnable);
                    }
                });
    }

    protected void setGoogleSignInButtonText(SignInButton signInButton, int resId) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(resId);
                return;
            }
        }
    }

    protected void signOut(int rc, Fragment loading) {
        mFirebaseAuth.signOut();
        if (rc == RC_LOGIN_WITH_FACEBOOK)
            LoginManager.getInstance().logOut();
        //Toast.makeText(this,"sign-in failure",Toast.LENGTH_LONG).show();
        removeFragment(loading);

        Log.d(TAG, "sign out");
    }


}
