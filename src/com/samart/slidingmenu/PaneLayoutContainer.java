package com.samart.slidingmenu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class PaneLayoutContainer extends FrameLayout {
    public static final int MAX_FADE = 255;

    public PaneLayoutContainer(final Context context) {
        this(context, null, 0);
    }


    public void setFadeFactor(final int fadeFactor) {
        if (fadeFactor < 0)
            mFadeFactor = 0;
        else if (fadeFactor > MAX_FADE)
            mFadeFactor = MAX_FADE;
        else
            mFadeFactor = fadeFactor;
        invalidate();
    }

    private int mFadeFactor;
    private final Paint mFadePaint = new Paint();

    @Override
    protected void dispatchDraw(final Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mFadeFactor > 0) {
            mFadePaint.setColor(Color.argb(mFadeFactor, 0, 0, 0));
            canvas.drawRect(0, 0, getWidth(), getHeight(), mFadePaint);
        }
    }

    public PaneLayoutContainer(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaneLayoutContainer(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }
}
