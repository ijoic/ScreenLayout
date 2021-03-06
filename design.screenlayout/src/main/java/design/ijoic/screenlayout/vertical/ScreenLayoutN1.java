package design.ijoic.screenlayout.vertical;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import design.ijoic.screenlayout.R;

/**
 * 屏布局
 *
 * @author ijoic 963505345@qq.com
 */
public class ScreenLayoutN1 extends LinearLayout {
  // grade contents:
  // N1: layout control

  /**
   * 构造函数
   *
   * @param context 上下文
   * @param attrs 属性列表
   */
  public ScreenLayoutN1(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    Log.e("screen_layout", ">> ----- measure ----- <<");
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    Log.e("screen_layout", ">. height [" + getMeasuredHeight() + "]");
    int screenHeight = MeasureSpec.getSize(heightMeasureSpec);
    Log.e("screen_layout", ">. parent height [" + screenHeight + "]");
    measureScreen(screenHeight);
  }

  private void measureScreen(int screenHeight) {
    Log.e("screen_layout", ">> ----- measure ----- <<");
    int screenWidth = getMeasuredWidth();
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
    Log.e("screen_layout", ">. screen height [" + screenHeight + "]");
    Log.e("screen_layout", ">. total height [" + totalHeight + "]");
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
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    Log.e("screen_layout", ">> ----- layout ----- <<");
    super.onLayout(changed, left, top, right, bottom);
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
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
}
