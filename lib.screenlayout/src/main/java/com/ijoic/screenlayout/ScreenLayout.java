package com.ijoic.screenlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * 屏布局
 *
 * @author ijoic 963505345@qq.com
 */
public class ScreenLayout extends LinearLayout {
  // grade contents:
  // N1: layout control
  // N2: scroll support
  // N3: fling support

  // scroll
  private int maxScrollY;
  private GestureDetector gestureDetector;
  private Scroller scroller;

  /**
   * 构造函数
   *
   * @param context 上下文
   * @param attrs 属性列表
   */
  public ScreenLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    gestureDetector = new GestureDetector(context, new GestureListener(), null);
    scroller = new Scroller(context);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    measureScreen(heightMeasureSpec);
  }

  private void measureScreen(int heightMeasureSpec) {
    int screenWidth = getMeasuredWidth();
    int screenHeight = MeasureSpec.getSize(heightMeasureSpec);
    final int count = getChildCount();

    boolean screenBegin = false;
    boolean measureSkip;
    int usedScreenHeight = 0;
    int totalHeight = 0;

    for (int i = 0; i < count; ++i) {
      final View child = getChildAt(i);

      if (child == null) {
        continue;
      }
      LayoutParams lp = (LayoutParams) child.getLayoutParams();
      measureSkip = child.getVisibility() == View.GONE;

      if (lp.screenBegin) {
        // 当前子视图有屏幕开始标记：
        // 1. 忽略之前的屏幕开始计数；
        // 2. 如果子视图本身同时包含屏幕结束标记，终止屏幕计数；如果包含结束标记的同时，子视图的高度
        //    布局方式为MATCH_SCREEN，让子视图单独占满一屏的高度；
        // 3. 如果子视图本身不包含屏幕结束标记，正常开始计数，重置已使用屏幕高度；

        if (lp.screenEnd) {
          if (lp.screenEndMatch && !measureSkip) {
            measureChild(child, lp, screenWidth, screenHeight);
          }
          screenBegin = false;
          usedScreenHeight = 0;
        } else {
          screenBegin = true;
          usedScreenHeight = child.getMeasuredHeight();
        }

      } else if (lp.screenEnd) {
        // 当前子视图没有屏幕开始标记，但有屏幕结束标记：
        // 1. 如果子视图带有填满屏幕剩余空间标记，计数开始：填满未使用高度，计数未开始：填满整屏高度；
        // 2. 重置屏幕开始计数状态

        if (lp.screenEndMatch && !measureSkip) {
          measureChild(child, lp, screenWidth, screenBegin ? Math.max(screenHeight - usedScreenHeight, 0) : screenHeight);
        }
        screenBegin = false;
        usedScreenHeight = 0;

      } else {
        // 当前子视图既没有屏幕开始标记，又没有屏幕结束标记：
        // 1. 如果屏幕开始计数状态，累加子视图高度；

        if (screenBegin && !measureSkip) {
          usedScreenHeight += child.getMeasuredHeight();
        }
      }
      if (!measureSkip){
        totalHeight += child.getMeasuredHeight();
      }
    }

    // 更新测量高度
    updateMeasureHeight(totalHeight, screenWidth, screenHeight);
  }

  private void updateMeasureHeight(int totalHeight, int screenWidth, int screenHeight) {
    maxScrollY = Math.max(totalHeight - screenHeight, 0);
    setMeasuredDimension(screenWidth, totalHeight);
  }

  private void measureChild(@NonNull View child, @NonNull LayoutParams lp, int screenWidth, int height) {
    int widthSpec;

    if (lp.width == LayoutParams.MATCH_PARENT) {
      widthSpec = MeasureSpec.makeMeasureSpec(screenWidth - lp.leftMargin - lp.rightMargin, MeasureSpec.EXACTLY);
    } else if (lp.width == LayoutParams.WRAP_CONTENT) {
      widthSpec = MeasureSpec.makeMeasureSpec(screenWidth - lp.leftMargin - lp.rightMargin, MeasureSpec.AT_MOST);
    } else {
      widthSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
    }

    child.measure(widthSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
  }

  @Override
  public void computeScroll() {
    if (scroller.computeScrollOffset()) {
      scrollTo(scroller.getCurrX(), scroller.getCurrY());
    }
  }

  /**
   * 行情详细布局-布局参数
   */
  public static class LayoutParams extends LinearLayout.LayoutParams {

    /**
     * 屏幕开始
     */
    private boolean screenBegin;

    /**
     * 屏幕结束
     */
    private boolean screenEnd;

    /**
     * 屏幕结束-占满屏幕剩余空间
     */
    private boolean screenEndMatch;

    /**
     * 构造函数
     *
     * @param c 上下文
     * @param attrs 属性列表
     */
    private LayoutParams(Context c, AttributeSet attrs) {
      super(c, attrs);
      TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.ScreenLayoutParams);

      screenBegin = a.getBoolean(R.styleable.ScreenLayoutParams_screen_begin, false);
      screenEnd = a.getBoolean(R.styleable.ScreenLayoutParams_screen_end, false);
      screenEndMatch = a.getBoolean(R.styleable.ScreenLayoutParams_screen_end_match, false);

      a.recycle();
    }
  }

  private class GestureListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onDown(MotionEvent e) {
      if (!scroller.isFinished()) {
        scroller.abortAnimation();
      }
      return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      int oldScrollY = getScrollY();
      int newScrollY = oldScrollY + (int) (distanceY + 0.5f);

      if (newScrollY < 0) {
        newScrollY = 0;
      } else if (newScrollY > maxScrollY) {
        newScrollY = maxScrollY;
      }
      if (oldScrollY != newScrollY) {
        scrollTo(0, newScrollY);
        return true;
      }
      return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      scroller.fling(getScrollX(), getScrollY(), 0, -(int) (velocityY + 0.5f), 0, 0, 0, maxScrollY);
      return true;
    }
  }
}
