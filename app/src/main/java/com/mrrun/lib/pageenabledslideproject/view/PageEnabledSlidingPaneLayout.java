package com.mrrun.lib.pageenabledslideproject.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * 全局页面划动返回Layout 仿IOS
 * 兼容其他滑动事件;
 * SupportSwipeBack设置页面是否支持滑动返回;
 * 设置背景颜色或者透明;
 *
 * @author lipin
 * @version 1.0
 * @date 2019.07.01
 */
public class PageEnabledSlidingPaneLayout extends FrameLayout {

    private static final String TAG = PageEnabledSlidingPaneLayout.class.getSimpleName();

    private FrameLayout mDecorView;

    private Activity mActivity;
    /**
     * 页面是否支持滑动返回
     * 默认支持
     * 如果想不支持，例如app的首页，只需要设置false即可
     */
    private boolean mSupportSwipeBack = true;
    /**
     * 手指按下点是否在检测范围内
     */
    private boolean mInCheckValidBorder = false;
    /**
     * 页面是否滑动过
     */
    private boolean mHasSleded = false;
    /**
     * 最小触发事件的划动距离
     */
    private int mTouchSlop;
    /**
     * 屏幕宽度
     */
    private int mScreenWidth;
    /**
     * DecorView第一个子View的背景色
     */
    private int mBgColor = android.R.color.white;
    private final static float sFactorScreenWidth = 0.5f;
    /**
     * 页面的手势监听
     */
    private GestureDetector mGestureDetector;
    /**
     * 滑到左边的动画
     */
    private ObjectAnimator mScrollLeftAnimator;
    /**
     * 页面滑动结束监听
     */
    private OnSlidingFinishListener mOnSlidingFinishListener;

    /**
     * 页面的手势监听
     */
    public class PageGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "手指按下点：" + e.getRawX());
            if (checkValidBorder(e)) {
            }
            return super.onDown(e);
        }

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // 在范围内
            if (mInCheckValidBorder) {
            } else {
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
            // distanceX > 0左滑手势, distanceX < 0右滑手势
            if (distanceX > 0 || distanceX < 0) {
                float mOldX = e1.getX(), mOldY = e1.getY();
                float deltaX = mOldX - e2.getX(), deltaY = mOldY - e2.getY();
                mHasSleded = true;
                Log.d(TAG, "滑动距离：" + distanceX);
                if (Math.abs(deltaX - mTouchSlop) > 0) {
                    mDecorView.scrollBy((int) distanceX, 0);
                }

            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    class WrapperScroller {

        private View targetView;

        private float scrollx;

        public WrapperScroller(View targetView) {
            this.targetView = targetView;
        }

        public void setScrollx(float scrollx) {
            this.scrollx = scrollx;
            Scroll();
        }

        private void Scroll() {
            if (targetView != null) {
                targetView.setScrollX((int) scrollx);
            }
        }
    }

    public interface OnSlidingFinishListener {
        void onSildingFinish();

        void onSildeLeftFinish();
    }

    public void setOnSildingFinishListener(OnSlidingFinishListener onSildingFinishListener) {
        this.mOnSlidingFinishListener = onSildingFinishListener;
    }

    public PageEnabledSlidingPaneLayout(@NonNull Context context) {
        this(context, null);
    }

    public PageEnabledSlidingPaneLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageEnabledSlidingPaneLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mActivity = (Activity) context;
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mScroller = new Scroller(context);
        this.mGestureDetector = new GestureDetector(getContext(), new PageGestureListener());
        init();
    }

    private void init() {
        initView();
        initData();
    }

    private void initData() {
        this.mScreenWidth = getScreenWidth();
    }

    private void initView() {
        this.mDecorView = (FrameLayout) this.mActivity.getWindow().getDecorView();
        /**
         * 因为Activity主题使用了Transparent.Theme，所以这里要设置DecorView加载的第一个View背景色，不然使用者未设置背景色Activity会透出底部
         */
        View decorChildView = this.mDecorView.getChildAt(0);
        decorChildView.setBackgroundResource(mBgColor);
        this.mDecorView.removeView(decorChildView);
        this.addView(decorChildView);
        this.mDecorView.addView(this);

        this.mDecorView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (mSupportSwipeBack) {
                    if (mGestureDetector.onTouchEvent(motionEvent)) {
                        return true;
                    }
                }
                // 处理手势结束
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        if (mSupportSwipeBack) {
                            endGesture();
                        }
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 拦截手势事件
     * 避免滑动冲突
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mSupportSwipeBack) {
            Log.d(TAG, "onInterceptTouchEvent：" + ev.getRawX());
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    boolean hasInterceptTouchEvent = checkValidBorder(ev);
                    Log.d(TAG, "hasInterceptTouchEvent：" + hasInterceptTouchEvent);
                    return checkValidBorder(ev);
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 页面是否支持滑动返回
     *
     * @param supportSwipeBack
     * @return
     */
    public PageEnabledSlidingPaneLayout setSupportSwipeBack(boolean supportSwipeBack) {
        this.mSupportSwipeBack = supportSwipeBack;
        return this;
    }

    /**
     * DecorView第一个子View的背景色,提供这个方法是因为有些页面需要其他颜色或者透明
     *
     * @param colorId
     * @return
     */
    public PageEnabledSlidingPaneLayout setBgColor(int colorId) {
        this.mBgColor = colorId;
        this.getChildAt(0).setBackgroundColor(getContext().getResources().getColor(mBgColor));
        return this;
    }

    /**
     * 手势结束
     */
    private void endGesture() {
        mInCheckValidBorder = false;
        // 页面滑动过
        if (mHasSleded) {
            int scrolEndPointX = mDecorView.getScrollX();
            // 滑动结束点X坐标小于屏幕宽度的因字数
            Log.d(TAG, "滑动结束点X坐标 = " + Math.abs(scrolEndPointX));
            Log.d(TAG, "mScreenWidth = " + Math.abs(mScreenWidth * sFactorScreenWidth));
            if (Math.abs(scrolEndPointX) <= mScreenWidth * sFactorScreenWidth) {
                scrolOrigin();
            } else {
                scrollFinish();
            }
        }
    }

    /**
     * 滑动结束
     */
    private void scrollFinish() {
        if (mOnSlidingFinishListener != null) {
            mOnSlidingFinishListener.onSildingFinish();
        }
        mHasSleded = false;
    }

    /**
     * 滑到原起地址
     */
    private void scrolOrigin() {
        final float scrollXdelta = mDecorView.getScrollX();
        Log.d(TAG, "滑到原起地址 scrollXdelta =" + scrollXdelta);
        mScrollLeftAnimator = ObjectAnimator.ofFloat(new WrapperScroller(mDecorView), "scrollx", scrollXdelta, 0);
        mScrollLeftAnimator.setInterpolator(new DecelerateInterpolator());
        mScrollLeftAnimator.setDuration(450);
        mScrollLeftAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mOnSlidingFinishListener != null) {
                    mOnSlidingFinishListener.onSildeLeftFinish();
                }
                mHasSleded = false;
            }
        });
        mScrollLeftAnimator.start();
    }

    /**
     * 滑动类
     */
    private Scroller mScroller;

    /**
     * 获得屏幕的宽度
     *
     * @return
     */
    private int getScreenWidth() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowMgr = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowMgr.getDefaultDisplay().getRealMetrics(dm);
        // 获取宽度
        return dm.widthPixels;
    }

    private static final float sChecValidBorderLeft = 0;

    private static final float sChecValidBorderRight = 30;

    /**
     * 响应区域，x事件拦截区域
     *
     * @param ev 手指按下点
     */
    private boolean checkValidBorder(MotionEvent ev) {
        if (sChecValidBorderLeft <= ev.getRawX() && ev.getRawX() <= sChecValidBorderRight) {
            return mInCheckValidBorder = true;
        } else {
            return mInCheckValidBorder = false;
        }
    }
}
