package com.starfang.realm.transaction;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.ProgressListener;
import com.android.volley.error.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;


public class ProgressRequest extends Request<JSONObject> implements ProgressListener {

    private final static String TAG = "FANG_PROG_REQ";
    private final Listener<JSONObject> mListener;
    private final ProgressListener mProgressListener;
    private final byte[] mRequestBody;

    public ProgressRequest(
            int method, String url, JSONObject jsonRequest
            , Listener<JSONObject> listener
            , ErrorListener errorListener
            , ProgressListener progressListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mProgressListener = progressListener;
        this.mRequestBody = jsonRequest.toString().getBytes();
    }

    @Override
    public byte[] getBody() {
        return mRequestBody;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        Log.d(TAG, "response header: " + response.headers.toString());
        try {
            String jsonString =
                    new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JSONException e) {
            Log.d(TAG, Log.getStackTraceString(e));
            return Response.error(new ParseError(e));
        }

    }

    @Override
    protected void deliverResponse(JSONObject response) {
        if (null != mListener) {
            mListener.onResponse(response);
        }
    }

    @Override
    public String getBodyContentType() {
        return "application/json";
    }

    @Override
    public void onProgress(long transferredBytes, long totalSize) {
        if (mProgressListener != null) {
            mProgressListener.onProgress(transferredBytes, totalSize);
        }

    }
}
