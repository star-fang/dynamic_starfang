package com.starfang.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.starfang.StarfangConstants;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherUtils {
    private final static String TAG = "FANG_CIPHER";

    public static byte[] generateIv(String cipherInstance) throws NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(cipherInstance);
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[cipher.getBlockSize()];
        secureRandom.nextBytes(iv);
        Log.d(TAG,"iv generated");
        return iv;
    }

    public static byte[] encrypt(String algorithm, String cipherInstance, String stringKey, byte[] byteData, byte[] iv) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(cipherInstance);
        Key key = new SecretKeySpec(Base64.decode(stringKey,Base64.DEFAULT), algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(byteData);
    }

    public static byte[] decrypt(String algorithm, String cipherInstance, String stringKey, byte[] encryptedData, byte[] iv) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(cipherInstance);
        Key key = new SecretKeySpec(Base64.decode(stringKey,Base64.DEFAULT), algorithm);;
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(encryptedData);
    }

    public static SharedPreferences getSecureSharedPreferences(Context context) throws GeneralSecurityException, IOException {
        return EncryptedSharedPreferences.create(
                StarfangConstants.SECURE_PREFERENCE_NAME,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    public static boolean checkIv( String cipherInstance, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(cipherInstance);
        return cipher.getBlockSize() == iv.length;
    }
}
