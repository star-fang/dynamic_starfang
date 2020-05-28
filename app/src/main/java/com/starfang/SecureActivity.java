package com.starfang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import org.apache.http.util.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.SecretKey;

public class SecureActivity extends AppCompatActivity {

    private static final String TAG = "FANG_CAT_SECURE";
    private static final String KEY_FILE = "fangcat.bks";
    private static final String KEY_EXT = "BKS";
    private static final String KEY_ALIAS = "128bitkey";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_secure);

        final AppCompatButton button_secure_prompt = findViewById(R.id.button_secure_prompt);
        final AppCompatEditText et_store_pw = findViewById(R.id.et_store_pw);
        final AppCompatEditText et_key_pw = findViewById(R.id.et_key_pw);
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    "secureFang",
                    masterKeyAlias,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String storePw = sharedPreferences.getString("storePw", null);
            String keyPw = sharedPreferences.getString("keyPw", null);

            if( storePw != null && keyPw != null ) {
                checkPassword(storePw, keyPw, sharedPreferences, false);
            } else {
                button_secure_prompt.setOnClickListener( v-> {
                    CharSequence storePwSeq = et_store_pw.getText();
                    CharSequence keyPwSeq = et_key_pw.getText();
                    if( !TextUtils.isEmpty(storePwSeq) && !TextUtils.isEmpty(keyPwSeq)) {
                        checkPassword(storePwSeq.toString(), keyPwSeq.toString(), sharedPreferences, true);
                    }
                });
            }


        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG,Log.getStackTraceString(e));
        }


    }

    private void checkPassword( String storePw, String keyPw, SharedPreferences sharedPreferences, boolean savePw) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEY_EXT);
            char[] keyStorePassword = storePw.toCharArray();
            InputStream keyStoreData = getAssets().open(KEY_FILE);

            try {
                keyStore.load(keyStoreData, keyStorePassword);


            char[] keyPassword = keyPw.toCharArray();
            KeyStore.ProtectionParameter entryPassword =
                    new KeyStore.PasswordProtection(keyPassword);

            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(
                    KEY_ALIAS, entryPassword);
            SecretKey secretKey = secretKeyEntry.getSecretKey();

            String encodedSecret = Base64.encodeToString(secretKey.getEncoded(), android.util.Base64.DEFAULT);

            if( sharedPreferences.edit().putString(StarfangConstants.SECURE_PREF_SECRET_KEY, encodedSecret).commit() ) {
                Log.d(TAG, "storePw: " + storePw);
                Log.d(TAG, "keyPw: " + keyPw);
                if( savePw ) {
                    if( sharedPreferences.edit().putString("storePw", storePw).commit()
                        && sharedPreferences.edit().putString("keyPw", keyPw).commit() ) {
                        Toast.makeText(this,"암호 저장 완료", Toast.LENGTH_SHORT).show();
                    }
                }
                startActivity(new Intent( this, MainActivity.class ));
                finish();
            }

            } catch ( IOException | CertificateException | UnrecoverableEntryException e ) {
                Toast.makeText(this, "암호 입력 오류", Toast.LENGTH_LONG).show();
                Log.e(TAG, Log.getStackTraceString(e));
            }


        } catch (KeyStoreException | IOException | NoSuchAlgorithmException e) {
            Toast.makeText(this, "암호 모듈 오류 발생", Toast.LENGTH_LONG).show();
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
