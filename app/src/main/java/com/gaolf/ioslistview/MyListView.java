package com.gaolf.ioslistview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by gaolf on 15/7/14.
 */
public class MyListView extends ListView {
    private int transY; // overScroll给的delta不等于手指划过的距离，直接根据delta来setTranslation有卡顿感，反复测试感觉delta大概是实际距离的1.7倍左右，因此实际控制位置时拿这个值的一半作为有效值。
    // transY = sum(overScrollDeltaY) + sum(2 * touchDeltaY).
    // translationY = transY / 2.
    private boolean isAnimating = false;
    private boolean isAllowDragOver = true;
    private OnDragOverListener dragOverListener;
    private int overDragDistance;
    private float lastY;


    private boolean isRefreshing;

    private OnScrollListener delegateOnScrollListener;

    public interface OnDragOverListener {
        // called when action_cancel or action_up happen while translationY is beyond than overDragDistance
        void onDragOver();
        // called when overScroll up or back.
        void onDrag(float translationY);
    }

    public MyListView(Context context) {
        super(context);
        init();
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        super.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (delegateOnScrollListener != null) {
                    delegateOnScrollListener.onScrollStateChanged(view, scrollState);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!isRefreshing) {
                    if (transY > 0 && isAllowDragOver) {
                        setSelection(0);
                    }
                }
                if (delegateOnScrollListener != null) {
                    delegateOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                }
            }
        });
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        if (!isTouchEvent || isRefreshing) {
            return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
        }
        if (deltaY < 0 && isAllowDragOver) {
            if (!isAnimating) {
                transY -= deltaY;
                setTranslationY(transY / 2);
                if (dragOverListener != null) {
                    dragOverListener.onDrag(getTranslationY());
                }
            }
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isRefreshing) {
            return super.onTouchEvent(ev);
        }
        if (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP) {
            lastY = 0;
            if (transY > 0) {
                if (getTranslationY() > overDragDistance) {
                    // refresh
                    isRefreshing = true;
                    if (dragOverListener != null) {
                        dragOverListener.onDragOver();
                        dragOverListener.onDrag(getTranslationY());
                    }
                    isAnimating = true;
                    animate().translationY(overDragDistance).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            if (dragOverListener != null) {
                                dragOverListener.onDrag(getTranslationY());
                            }
                        }
                    }).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            isAnimating = false;
                        }
                    }).start();
                    transY = 200;
                } else {
                    // not refreshing, animate back
                    isAnimating = true;
                    animate().translationY(0).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            if (dragOverListener != null) {
                                dragOverListener.onDrag(getTranslationY());
                            }
                        }
                    }).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            isAnimating = false;
                        }
                    }).start();
                    transY = 0;
                }
                return true;
            }
            return super.onTouchEvent(ev);
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (!isAnimating && transY > 0) {
                if (lastY != 0) {
                    float dY = (lastY - ev.getY()) * 2f;
                    if (dY > 0) {
                        transY -= dY;
                        setTranslationY(transY / 2);
                        dragOverListener.onDrag(getTranslationY());
                    }
                } else {
                    // ignore this.
                }
            }
            lastY = ev.getY();
            return super.onTouchEvent(ev);
        } else {
            return super.onTouchEvent(ev);
        }
    }



    public void setOnDragOverListener(int overDragDistance, OnDragOverListener listener) {
        this.overDragDistance = overDragDistance;
        this.dragOverListener = listener;
    }

    public void enableOverDrag(boolean enabled) {
        this.isAllowDragOver = enabled;
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        this.delegateOnScrollListener = l;
    }

    public void endRefresh() {
        isAnimating = true;
        isRefreshing = false;
        animate().translationY(0).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (dragOverListener != null) {
                    dragOverListener.onDrag(getTranslationY());
                }
            }
        }).withEndAction(new Runnable() {
            @Override
            public void run() {
                isAnimating = false;
                transY = 0;
            }
        }).start();
    }
}
