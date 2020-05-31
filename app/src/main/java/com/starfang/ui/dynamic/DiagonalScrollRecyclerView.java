package com.starfang.ui.dynamic;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import androidx.recyclerview.widget.RecyclerView;

public class DiagonalScrollRecyclerView extends HorizontalScrollView {
    private static final String TAG = "FANG_DSR";


    private RecyclerView recyclerView = null;

    private float downX = 0f;
    private float downY = 0f;
    private float furthestDistanceMovedPx = 0f;

    public DiagonalScrollRecyclerView(Context context) {
        super(context);
    }

    public DiagonalScrollRecyclerView(Context context, AttributeSet attrs ) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if( this.getChildCount() != 0 ) {
            super.onTouchEvent((event));
            recyclerView.onTouchEvent(event);
        }

        switch( event.getAction() ) {

            case MotionEvent.ACTION_DOWN:
                downX = event.getRawX();
                downY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                //Log.d(TAG,"move distance : "  + furthestDistanceMovedPx);
                if (recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 0 && isClick(furthestDistanceMovedPx)) {


                    try {
                        View v = findChildAtLocation(recyclerView, (int) event.getRawX(), (int) event.getRawY());
                        if( v != null ) {
                            v.performClick();
                            //Log.d(TAG, v.toString());
                        }
                    } catch (Exception e) {
                        Log.e(TAG,Log.getStackTraceString(e));
                    }

                }
                furthestDistanceMovedPx = 0f;
                break;
            case MotionEvent.ACTION_MOVE:
                float distanceFromStart = pxDistance(event.getRawX(), event.getRawY(), downX, downY);
                if (distanceFromStart > furthestDistanceMovedPx) {
                    furthestDistanceMovedPx = distanceFromStart;
                }
                break;
            default:
        }
        return true;
    }

    private View findChildAtLocation(ViewGroup v, int x, int y) {
        for(int i = 0; i < v.getChildCount(); i++) {
            int[] childCroods = new int[2];
            View child = v.getChildAt(i);

            if(child != null) {
                child.getLocationOnScreen(childCroods);
                Rect childArea = new Rect(childCroods[0],childCroods[1],
                        childCroods[0] + child.getWidth(),
                        childCroods[1] + child.getHeight());
                if(childArea.contains(x,y)) {
                    if( child instanceof ViewGroup) {
                        child = findChildAtLocation((ViewGroup)child, x, y);
                    }

                    return child;
                }
            }
        }
        return null;
    }

    public void setRecyclerView( RecyclerView recyclerView ) {
        this.recyclerView = recyclerView;
    }




    private float pxToDP(float px ) {
        return px / getResources().getDisplayMetrics().density;
    }

    private float pxDistance( float x1, float y1, float x2, float y2 ) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    private boolean isClick(float furthestDistanceMovedPx) {
        return pxToDP(furthestDistanceMovedPx) < 15;
    }
}

