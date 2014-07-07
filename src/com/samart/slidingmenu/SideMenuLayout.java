package com.samart.slidingmenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Scroller;

public class SideMenuLayout extends ViewGroup
        implements MainLayoutContainer.OnPaneSwipeListener {
    private static final String TAG = SideMenuLayout.class.getName();
    private static final boolean DEBUG = true;
    public static final int SCROLL_DURATION = 200;
    private final PaneLayoutContainer viewPaneContainer;
    private final MainLayoutContainer viewMainContainer;
    private final int slideWidth;
    private final int defaultPaneOffset;
    private final int paneScrollMultiplier;

    public SideMenuLayout(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SideMenuLayout(final Context context) {
        this(context, null, 0);
    }

    public SideMenuLayout(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setClipChildren(false);
        setClipToPadding(false);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SideMenuLayout);
        if (null == a)
            throw new RuntimeException("style attributes doesn't exists");
        slideWidth = a.getDimensionPixelSize(R.styleable.SideMenuLayout_pane_width, 100);
        paneScrollMultiplier = 10;
        touchLeftArea = 60;
        defaultPaneOffset = getMaxScrollPosition() / paneScrollMultiplier;

        final int paneLayout = a.getResourceId(R.styleable.SideMenuLayout_pane_layout, 0);
        final int mainLayout = a.getResourceId(R.styleable.SideMenuLayout_main_layout, 0);

        if (0 == paneLayout)
            throw new RuntimeException("attribute paneLayout missing");
        if (0 == mainLayout)
            throw new RuntimeException("attribute mainLayout missing");

        a.recycle();

        viewPaneContainer = new PaneLayoutContainer(context);
        viewMainContainer = new MainLayoutContainer(context);
        viewMainContainer.setOnSwipeListener(this);

        final LayoutInflater inflater = LayoutInflater.from(context);

        //#1 on background
        inflater.inflate(paneLayout, viewPaneContainer, true);
        addView(viewPaneContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        //#2 on top
        inflater.inflate(mainLayout, viewMainContainer, true);
        addView(viewMainContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        scrollController = new ScrollController(this);
        gestureDetector = new GestureDetector(context, scrollController);
        //gestureDetector.setIsLongpressEnabled(false);
    }

    private final GestureDetector gestureDetector;

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        if (gestureDetector.onTouchEvent(ev))
            return true;
        if (ev.getAction() == MotionEvent.ACTION_UP && scrollController.onUp(ev))
            return true;

        return super.dispatchTouchEvent(ev);
    }

    private final int touchLeftArea;
    private int scrollPosition;

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int childsCount = getChildCount();
        for (int i = 0; i < childsCount; i++) {
            final View view = getChildAt(i);
            if (null == view) continue;
            view.measure(widthMeasureSpec, heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        final int childsCount = getChildCount();
        for (int i = 0; i < childsCount; i++) {
            final View view = getChildAt(i);
            if (null == view) continue;
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        }
    }

    private final ScrollController scrollController;

    private static class ScrollController implements Runnable, GestureDetector.OnGestureListener {
        private final SideMenuLayout layout;
        private final Scroller scroller;

        ScrollController(final SideMenuLayout layout) {
            if (null == layout) {
                throw new IllegalArgumentException("layout == null");
            }
            final Context context = layout.getContext();
            if (null == context) {
                throw new RuntimeException("context is null");
            }
            scroller = new Scroller(context, new AccelerateDecelerateInterpolator());
            this.layout = layout;
        }


        private static final int FADE_MAX = 255;
        private static final int FADE_MIN = 100;

        private void scrollTo(final int x) {
            final int max = layout.getMaxScrollPosition();
            final float k = (max - x) / (float) max;
            final int fadeFactor = (int) ((FADE_MAX - FADE_MIN) * k);
            final int paneTo = x == 0 ?
                    layout.defaultPaneOffset :
                    layout.defaultPaneOffset - x / layout.paneScrollMultiplier;
            layout.viewPaneContainer.scrollTo(paneTo, 0);
            layout.viewMainContainer.scrollTo(-x, 0);
            layout.viewPaneContainer.setFadeFactor(fadeFactor);
        }

        @Override
        public void run() {
            final int x = scroller.getCurrX();
            scrollTo(x);
            if (scroller.computeScrollOffset()) {
                layout.post(this);
            }
        }

        public void scroll(final int start, final int end, final int dur) {
            scroller.forceFinished(true);
            scroller.startScroll(start, 0, end - start, 0, dur);
            layout.post(this);
        }

        private int downX = 0;

        @Override
        public boolean onDown(final MotionEvent e) {
            downX = layout.touchLeftArea - layout.viewMainContainer.getScrollX();
            return false;
        }

        @Override
        public void onShowPress(final MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(final MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
                                final float distanceX, final float distanceY) {
            if (e1.getX() > downX)
                return false;
            if (Math.abs(distanceX) < Math.abs(distanceY))
                return false;
            final int startX = scroller.getCurrX();
            int endX = (int) (startX - distanceX);
            final int max = layout.getMaxScrollPosition();
            if (endX < 0) endX = 0;
            else if (endX > max)
                endX = max;
            scroll(startX, endX, 0);
            //Log.e(TAG, "onScroll ");
            return true;
        }

        @Override
        public void onLongPress(final MotionEvent e) {
        }

        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2,
                               final float velocityX, final float velocityY) {
            if (e1.getX() > downX)
                return false;
            if (Math.abs(velocityX) < Math.abs(velocityY))
                return false;
            final int start = scroller.getCurrX();
            final int end = velocityX < 0 ? 0 : layout.getMaxScrollPosition();
            final int dx = end - start;
            if (dx == 0) return false;
            final int dt = velocityX == 0
                    ? SCROLL_DURATION : Math.abs((int) (800 * dx / velocityX));
            scroll(start, end, dt);
            //Log.e(TAG, "onFling " + start + ' ' + dx + ' ' + dt + ' ' + velocityX);
            return true;
        }

        public boolean onUp(final MotionEvent ev) {
            final int x = scroller.getCurrX();
            if (x > downX)
                return false;
            final int max = layout.getMaxScrollPosition();
            if (x == 0 || x == max)
                return false;
            final int middle = max >> 1;
            if (x > middle) {
                scroll(x, max, SCROLL_DURATION);
            } else {
                scroll(x, 0, SCROLL_DURATION);
            }
            // Log.e(TAG, "onUp");
            return true;
        }
    }


    public int getMaxScrollPosition() {
        return slideWidth;
    }

    public void open() {
        scrollController.scroll(scrollPosition, getMaxScrollPosition(), SCROLL_DURATION);
    }

    public void close() {
        scrollController.scroll(scrollPosition, 0, SCROLL_DURATION);
    }


    public boolean isOpened() {
        return viewMainContainer.getScrollX() != 0;
    }

    public void toggleOpenClose() {
        if (isOpened()) {
            close();
        } else {
            open();
        }
    }

    @Override
    public void onPaneSwipe(final int offset) {
//        Log.e(TAG, "pane swiped " + offset);
        scrollPosition = offset;
    }

}
