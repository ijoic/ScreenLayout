package com.ijoic.screenlayout;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 屏布局-父视图
 *
 * @author ijoic 963505345@qq.com
 */
public class ScreenLayoutParent extends NestedScrollView {

  public ScreenLayoutParent(Context context) {
    this(context, null);
  }

  public ScreenLayoutParent(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ScreenLayoutParent(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void measureChild(View child, int parentWidthMeasureSpec,
                              int parentHeightMeasureSpec) {
    ViewGroup.LayoutParams lp = child.getLayoutParams();

    int childWidthMeasureSpec;
    int childHeightMeasureSpec;

    childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, getPaddingLeft()
            + getPaddingRight(), lp.width);

    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(parentHeightMeasureSpec), MeasureSpec.UNSPECIFIED);

    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
  }

  @Override
  protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
                                         int parentHeightMeasureSpec, int heightUsed) {
    final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

    final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
            getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin
                    + widthUsed, lp.width);
    final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(parentHeightMeasureSpec), MeasureSpec.UNSPECIFIED);

    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
  }
}
