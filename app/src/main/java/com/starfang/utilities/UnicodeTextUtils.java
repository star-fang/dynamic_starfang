package com.starfang.utilities;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.primitives.Ints;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnicodeTextUtils {

    private static final String TAG = "FANG_UTIL_TEXT";

    public static String subText(@NonNull String text, int startPos, int length, String suffix) {

        int textLength = textLength(text);
        Log.d(TAG, "textLength: "  + textLength);
        Log.d(TAG, "text.length(): "  + text.length());
        if (textLength  > length) {
            StringBuilder sb = new StringBuilder();
            int n = 0;
            List<Integer> codePointList = new ArrayList<>();
            for (int i = 0; i < text.length(); n++) {
                // 가★☆나다■ (2,3)  => ☆나다
                int cp = text.codePointAt(i);
                //Log.d(TAG, "co at: (" + i + "): "  + cp);
                int charCount = Character.charCount(cp);
                i += charCount;
                if (n >= startPos && n < (startPos + length)) {
                    codePointList.add(cp);
                } // if
            } // for

            int[] codePoints = Ints.toArray(codePointList);

            String subText = new String(codePoints, 0, codePoints.length);
            if (suffix != null)
                subText += suffix;
            return subText;
        } else {
            return text;
        }


    }

    public static byte[] utf8(Integer[] codePoints) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] cpBytes = new byte[6];
        for (int cp : codePoints) {
            if (cp < 0) {
                throw new IllegalStateException("negative code point");
            } else if (cp < 0x80) {
                baos.write(cp);
            } else {
                int bi = 0;
                int lastPrefix = 0xC0;
                int lastMask = 0x1F;
                for (; ; ) {
                    int b = 0x80 | (cp & 0x3F);
                    cpBytes[bi] = (byte) b;
                    ++bi;
                    if ((cp & ~lastMask) == 0 || bi > 4) {
                        cpBytes[bi] = (byte) (lastPrefix | cp);
                        ++bi;
                        break;
                    }
                    lastPrefix = 0x80 | (lastPrefix >> 1);
                    lastMask >>= 1;
                }
                while (bi > 0) {
                    --bi;
                    baos.write(cpBytes[bi]);
                }
            }

        }
        return baos.toByteArray();
    }

    public static int textLength(@NonNull String text) {
        int n = 0;
        for (int i = 0; i < text.length(); ++n) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
        }
        return n;
    }

    public static String mapToQueryString(Map<String,String> map) throws UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();

        if( map.size() > 0 )
            stringBuilder.append("?");

        for(Map.Entry<String, String> entry: map.entrySet() ) {
            stringBuilder.append(URLEncoder.encode(entry.getKey(),"UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(),"UTF-8"))
                    .append("&");
        }
        return stringBuilder.toString();
    }
}
