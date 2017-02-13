package xiao.free.horizontalrefreshlayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/**
 * Created by robincxiao on 2017/2/6.
 * 1.View布局问题，onMeasure、onLayout
 * 2.滑动冲突问题
 * 2.1事件拦截
 * 2.2拖动效果
 * 3.释放时自动归位问题
 * 值得注意的问题：
 * 1.onlayout中header初始化
 * 2.onTouchEvent的返回值
 */

public class HorizontalRefreshLayout extends FrameLayout {
    private static final int DURATION = 150;
    private Context context;
    private RefreshHeader leftRefreshHeader;
    private RefreshHeader rightRefreshHeader;
    private View mTargetView;
    private View leftHeaderView;
    private View rightHeaderView;
    private RefreshCallBack refreshCallback;
    private int touchSlop;
    private int dragMarginPx;
    private int leftHeaderWidth;
    private int rightHeaderWidth;
    //最大拖动距离
    private int dragMaxHeaderWidth;
    private int mLastInterceptX;
    private int mLastInterceptY;
    private int mLastX;
    private int mLastY;

    private float mTargetTranslationX = 0;
    //header状态，当前显示是左边header、右边header
    private int headerState = -1;
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    //刷新状态
    private static final int REFRESH_STATE_IDLE = 0;
    private static final int REFRESH_STATE_START = 1;
    private static final int REFRESH_STATE_DRAGGING = 2;
    private static final int REFRESH_STATE_READY_TO_RELEASE = 3;
    private static final int REFRESH_STATE_REFRESHING = 4;
    private int refreshState = REFRESH_STATE_IDLE;


    public HorizontalRefreshLayout(Context context) {
        super(context);

        init();
    }

    public HorizontalRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public HorizontalRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public void setRefreshCallback(RefreshCallBack callback) {
        refreshCallback = callback;
    }

    private void init() {
        context = getContext();
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(leftHeaderView != null) {
            leftHeaderWidth = leftHeaderView.getMeasuredWidth();
            dragMarginPx = (int) (leftHeaderWidth * 0.6);
            dragMaxHeaderWidth = leftHeaderWidth + dragMarginPx;
        }

        if(rightHeaderView != null) {
            rightHeaderWidth = rightHeaderView.getMeasuredWidth();
            if(dragMarginPx == 0){
                dragMarginPx = (int) (rightHeaderWidth * 0.6);
                dragMaxHeaderWidth = rightHeaderWidth + dragMarginPx;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (getChildCount() == 0) {
            return;
        }

        if (mTargetView == null) {
            findTargetView();
            if (mTargetView == null) {
                return;
            }
        }

        /**
         * 注意：只有状态是IDLE时才初始化刷新header的TranslationX；因为在滑动mTargetView时onLayout会被重新调用，
         * 如果不是在REFRESH_STATE_IDLE状态下设置setTranslationX，则会产生问题
         */
        if (refreshState == REFRESH_STATE_IDLE) {
            if (leftHeaderView != null) {
                leftHeaderView.setTranslationX(-leftHeaderWidth);
            }

            if (rightHeaderView != null) {
                rightHeaderView.setTranslationX(rightHeaderWidth);
            }
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    private void findTargetView() {
        if (mTargetView == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(leftHeaderView) && !child.equals(rightHeaderView)) {
                    mTargetView = child;
                    break;
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = mLastInterceptX = x;
                mLastY = mLastInterceptY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastInterceptX;
                int deltaY = y - mLastInterceptY;

                mLastX = mLastInterceptX = x;
                mLastY = mLastInterceptY = y;

                /**
                 * 注意：需要判断refreshState != REFRESH_STATE_REFRESHING，否则当处于REFRESH_STATE_REFRESHING状态
                 * 再次拖动滑动时，会有些许小瑕疵
                 */
                if (Math.abs(deltaX) > Math.abs(deltaY)) {//判断是否是水平滑动
                    if (leftHeaderView != null && deltaX > 0 && !canChildScrollRight() && refreshState != REFRESH_STATE_REFRESHING) {//手指向右滑动
                        headerState = LEFT;
                        refreshState = REFRESH_STATE_START;
                        leftRefreshHeader.onStart(LEFT, leftHeaderView);

                        return true;
                    } else if (rightHeaderView != null && deltaX < 0 && !canChildScrollLeft() && refreshState != REFRESH_STATE_REFRESHING) {//手指向左滑动
                        headerState = RIGHT;
                        refreshState = REFRESH_STATE_START;
                        rightRefreshHeader.onStart(RIGHT, rightHeaderView);

                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastInterceptX = 0;
                mLastInterceptY = 0;
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        /**
         * 为什么不在onTouchEvent中直接返回true，而是在ACTION_MOVE/ACTION_UP/ACTION_CANCEL返回true，ACTION_DOWN不返回true?
         * 在如下场景下需要这样设计：header正在刷新的时候，用户点击在header上开始drag，但是当header正在刷新的时候，我们并不希望
         * headerview和mTargetView被拖动，因此需要这样去处理。
         */
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastX;

                mLastX = x;
                mLastY = y;

                float dampingDX = deltaX * (1 - Math.abs((mTargetTranslationX / dragMaxHeaderWidth)));  //let drag action has resistance
                mTargetTranslationX += dampingDX;

                if (headerState == LEFT) {
                    if (mTargetTranslationX <= 0) {
                        Log.d("xiao1", "test1");
                        mTargetTranslationX = 0;
                        mTargetView.setTranslationX(0);
                    } else if (mTargetTranslationX >= dragMaxHeaderWidth) {
                        Log.d("xiao1", "test2");
                        mTargetTranslationX = dragMaxHeaderWidth;
                        mTargetView.setTranslationX(mTargetTranslationX);
                    } else {
                        Log.d("xiao1", "test3");

                        mTargetView.setTranslationX(mTargetTranslationX);

                        if (refreshState != REFRESH_STATE_READY_TO_RELEASE && mTargetTranslationX >= leftHeaderWidth) {
                            refreshState = REFRESH_STATE_READY_TO_RELEASE;

                            leftRefreshHeader.onReadyToRelease(leftHeaderView);
                        } else {
                            refreshState = REFRESH_STATE_DRAGGING;
                            //计算出拖动的比率
                            float percent = Math.abs(mTargetTranslationX / leftHeaderWidth);
                            leftRefreshHeader.onDragging(mTargetTranslationX, percent, leftHeaderView);
                        }
                    }

                    leftHeaderView.setTranslationX(-leftHeaderWidth + mTargetTranslationX);
                } else if ((headerState == RIGHT)) {
                    if (mTargetTranslationX >= 0) {
                        Log.d("xiao1", "test4");
                        mTargetTranslationX = 0;
                        mTargetView.setTranslationX(0);
                    } else if (mTargetTranslationX <= -dragMaxHeaderWidth) {
                        Log.d("xiao1", "test5");
                        mTargetTranslationX = -dragMaxHeaderWidth;
                        mTargetView.setTranslationX(mTargetTranslationX);
                    } else {
                        Log.d("xiao1", "test6");
                        mTargetView.setTranslationX(mTargetTranslationX);

                        if (refreshState != REFRESH_STATE_READY_TO_RELEASE && mTargetTranslationX <= -rightHeaderWidth) {
                            refreshState = REFRESH_STATE_READY_TO_RELEASE;
                            rightRefreshHeader.onReadyToRelease(rightHeaderView);
                        } else {
                            refreshState = REFRESH_STATE_DRAGGING;
                            //计算出拖动的比率
                            float percent = Math.abs(mTargetTranslationX / rightHeaderWidth);
                            rightRefreshHeader.onDragging(mTargetTranslationX, percent, rightHeaderView);
                        }
                    }

                    rightHeaderView.setTranslationX(rightHeaderWidth + mTargetTranslationX);
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastX = mLastInterceptX = 0;
                mLastY = mLastInterceptY = 0;

                if (headerState == LEFT) {
                    if (mTargetTranslationX < leftHeaderWidth) {
                        Log.d("xiao1", "test7");
                        smoothRelease();
                    } else {
                        Log.d("xiao1", "test8");
                        smoothLocateToRefresh();
                    }
                } else if (headerState == RIGHT) {
                    if (mTargetTranslationX > -rightHeaderWidth) {
                        Log.d("xiao1", "test9");
                        smoothRelease();
                    } else {
                        Log.d("xiao1", "test10");
                        smoothLocateToRefresh();
                    }
                }
                return true;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 释放滑动
     */
    private void smoothRelease() {
        mTargetView.animate().translationX(0).setDuration(DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //动画结束后reset状态
                        refreshState = REFRESH_STATE_IDLE;
                        headerState = -1;
                        mTargetTranslationX = 0;
                    }
                })
                .start();

        if (headerState == LEFT) {
            if (leftHeaderView != null) {
                leftRefreshHeader.onStart(LEFT, leftHeaderView);//恢复到开始状态
                leftHeaderView.animate().translationX(-leftHeaderWidth).setDuration(DURATION).start();
            }
        } else if (headerState == RIGHT) {
            if (rightHeaderView != null) {
                rightRefreshHeader.onStart(LEFT, rightHeaderView);//恢复到开始状态
                rightHeaderView.animate().translationX(rightHeaderWidth).setDuration(DURATION).start();
            }
        }
    }

    /**
     * 滑动到刷新位置
     */
    private void smoothLocateToRefresh() {
        if (headerState == LEFT && leftHeaderView != null) {
            refreshState = REFRESH_STATE_REFRESHING;

            leftHeaderView.animate().translationX(0).setDuration(DURATION).start();

            leftRefreshHeader.onRefreshing(leftHeaderView);//正在刷新

            mTargetView.animate().translationX(leftHeaderWidth).setDuration(DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mTargetTranslationX = leftHeaderWidth;

                            if (refreshCallback != null) {
                                if (headerState == LEFT) {
                                    refreshCallback.onLeftRefreshing();
                                } else {
                                    refreshCallback.onRightRefreshing();
                                }
                            }
                        }
                    })
                    .start();
        } else if (headerState == RIGHT && rightHeaderView != null) {
            refreshState = REFRESH_STATE_REFRESHING;
            //注意，这里使用的translationXBy
            rightHeaderView.animate().translationXBy(-mTargetTranslationX - rightHeaderWidth).setDuration(DURATION).start();

            rightRefreshHeader.onRefreshing(rightHeaderView);//正在刷新

            mTargetView.animate().translationX(-rightHeaderWidth).setDuration(DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (refreshCallback != null) {
                                if (headerState == LEFT) {
                                    refreshCallback.onLeftRefreshing();
                                } else {
                                    refreshCallback.onRightRefreshing();
                                }
                            }

                            mTargetTranslationX = -rightHeaderWidth;
                        }
                    })
                    .start();
        }
    }

    /**
     * 刷新完成
     */
    public void onRefreshComplete() {
        smoothRelease();
    }

    /**
     * 自动刷新
     *
     * @param leftOrRight HorizontalRefreshLayout.LEFT or HorizontalRefreshLayout.RIGHT
     */
    public void startAutoRefresh(final int leftOrRight) {
        // delay to let the animation smoothly
        //此处需要采用postDelayed将消息方式view的消息队列中，如果直接调用smoothLocateToRefresh可能view还没能完全初始化好，导致mTarget为null
        postDelayed(new Runnable() {
            @Override
            public void run() {
                headerState = leftOrRight;
                smoothLocateToRefresh();
            }
        }, 100);
    }

    private void setLeftHeadView(View view) {
        leftHeaderView = view;
        ((LayoutParams) leftHeaderView.getLayoutParams()).gravity = Gravity.START;
        addView(leftHeaderView, 0);
    }

    private void setRightHeadView(View view) {
        rightHeaderView = view;
        ((LayoutParams) rightHeaderView.getLayoutParams()).gravity = Gravity.END;
        addView(rightHeaderView, 0);
    }

    /**
     * 设置刷新header
     * @param header
     * @param startOrEnd
     */
    public void setRefreshHeader(RefreshHeader header, int startOrEnd) {
        if (startOrEnd == LEFT) {
            leftRefreshHeader = header;
            setLeftHeadView(leftRefreshHeader.getView(this));
        } else if (startOrEnd == RIGHT) {
            rightRefreshHeader = header;
            setRightHeadView(rightRefreshHeader.getView(this));
        }
    }

    /**
     * mTargetView是否还能向右滑动
     *
     * @return
     */
    public boolean canChildScrollRight() {
        return ViewCompat.canScrollHorizontally(mTargetView, -1);
    }

    /**
     * mTargetView是否还能向左滑动
     *
     * @return
     */
    public boolean canChildScrollLeft() {
        return ViewCompat.canScrollHorizontally(mTargetView, 1);
    }


    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.getResources().getDisplayMetrics());
    }
}
