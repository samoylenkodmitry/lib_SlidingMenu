package com.samart.slidingmenu;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class MainLayoutContainer extends LinearLayout {


    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (null != mOnSwipeListener) {
            mOnSwipeListener.onPaneSwipe(-getScrollX());
        }
    }

    private OnPaneSwipeListener mOnSwipeListener;

    public MainLayoutContainer(final Context context) {
        this(context, null, 0);
    }

    public MainLayoutContainer(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private final Rect mHitRect = new Rect();

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        getHitRect(mHitRect);
        mHitRect.offset(-getScrollX(), -getScrollY());
        if (mHitRect.contains((int) event.getX(), (int) event.getY()))
            return true;
        return super.onTouchEvent(event);
    }

    public interface OnPaneSwipeListener {
        void onPaneSwipe(int offset);
    }

    public void setOnSwipeListener(final OnPaneSwipeListener listener) {
        mOnSwipeListener = listener;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private MainLayoutContainer(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setOrientation(LinearLayout.HORIZONTAL);
    }
}
