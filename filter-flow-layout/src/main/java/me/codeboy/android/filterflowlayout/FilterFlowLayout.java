package me.codeboy.android.filterflowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * 含有宽度过滤功能的流式布局
 * <p>
 * 将不满足宽度范围的childView隐藏掉
 * 以根据比例或者具体值进行设置
 * </p>
 * <p>
 * 宽度优先级：
 * 代码动态设置  > xml比例设置 > xml精确值设置
 * </p>
 * Created by yuedong.lyd on 7/6/15.
 */
public class FilterFlowLayout extends ViewGroup {

    private int mGravity = Gravity.START;
    private final List<List<View>> mLines = new ArrayList<List<View>>();
    private final List<Integer> mLineHeights = new ArrayList<Integer>();
    private final List<Integer> mLineMargins = new ArrayList<Integer>();
    private float minChildWidth = 0; //最小child宽度
    private float maxChildWidth = 0; //最大child宽度
    private float minWidthRatio = -1; //最小child宽度比值
    private float maxWidthRatio = -1; //最大child宽度比值
    private int horizontalGap = 0; //水平空隙
    private int verticalGap = 0; //垂直空隙
    private int maxLines = Integer.MAX_VALUE; //最大行数
    private final static float DELTA = 0.001f;  //误差允许范围
    private final static float MAX_RATIO = 1.001f;  //比例误差允许最大范围
    private List<View> hiddenViews = new ArrayList<View>(); //代码隐藏的View

    public FilterFlowLayout(Context context) {
        this(context, null);
    }

    public FilterFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FilterFlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FilterFlowLayout,
                defStyle, 0);

        try {
            int index = a.getInt(R.styleable.FilterFlowLayout_android_gravity, -1);
            if (index > 0) {
                setGravity(index);
            }

            //读取最大宽度与最小宽度，有比例与具体值两种方式
            //初始化时读取具体值, 测量时计算比例
            //比例的优先级高于具体值
            minChildWidth = a.getDimension(R.styleable.FilterFlowLayout_minWidth, minChildWidth);
            maxChildWidth = a.getDimension(R.styleable.FilterFlowLayout_maxWidth, maxChildWidth);

            minWidthRatio = a.getFloat(R.styleable.FilterFlowLayout_minWidthRatio, minWidthRatio);
            maxWidthRatio = a.getFloat(R.styleable.FilterFlowLayout_maxWidthRatio, maxWidthRatio);

            maxLines = a.getInt(R.styleable.FilterFlowLayout_maxLines, Integer.MAX_VALUE);

            horizontalGap = (int) (a.getDimension(R.styleable.FilterFlowLayout_horizontalGap,
                    horizontalGap) + 0.5f);
            verticalGap = (int) (a.getDimension(R.styleable.FilterFlowLayout_verticalGap,
                    verticalGap) + 0.5f);

        } finally {
            a.recycle();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() -
                getPaddingRight();
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);


        //计算child真实的最大宽度与最小宽度
        if (minWidthRatio >= 0 && minWidthRatio <= MAX_RATIO) {
            minChildWidth = sizeWidth * minWidthRatio;
        }
        if (maxWidthRatio >= 0 && maxWidthRatio <= MAX_RATIO) {
            maxChildWidth = sizeWidth * maxWidthRatio;
        }

        //如果都没有设置的话，最大宽度将是FilterFlowLayout的宽度
        if (maxWidthRatio < 0 && maxChildWidth == 0) {
            maxChildWidth = sizeWidth;
        }

        minChildWidth -= DELTA;
        maxChildWidth += DELTA;

        int width = 0;
        int height = getPaddingTop() + getPaddingBottom();

        int lineWidth = 0;
        int lineHeight = 0;
        int lineCount = 1;  //行数,控制函数

        int childCount = getChildCount();

        //最大行为0的话,隐藏布局
        if (maxLines == 0) {
            setVisibility(View.GONE);
        }

        //恢复显示属性
        for (View view : hiddenViews) {
            view.setVisibility(View.VISIBLE);
        }

        hiddenViews.clear();

        //换行位置，记录每行第一个元素为位置
        int positionInLine = 0 ;

        //计算FilterFlowLayout的Width与Height
        for (int i = 0; i < childCount; i++) {

            View child = getChildAt(i);
            boolean lastChild = i == childCount - 1;

            if (child.getVisibility() == View.GONE) {

                if (lastChild) {
                    width = Math.max(width, lineWidth);
                    height += lineHeight;
                    lineCount++;
                }

                continue;
            }

            measureChildWithMargins(child, widthMeasureSpec, lineWidth, heightMeasureSpec, height);

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int childWidthMode = MeasureSpec.AT_MOST;
            int childWidthSize = sizeWidth;

            int childHeightMode = MeasureSpec.AT_MOST;
            int childHeightSize = sizeHeight;

            if (lp.width == LayoutParams.MATCH_PARENT) {
                childWidthMode = MeasureSpec.EXACTLY;
                childWidthSize -= lp.leftMargin + lp.rightMargin;
            } else if (lp.width >= 0) {
                childWidthMode = MeasureSpec.EXACTLY;
                childWidthSize = lp.width;
            }

            if (lp.height >= 0) {
                childHeightMode = MeasureSpec.EXACTLY;
                childHeightSize = lp.height;
            } else if (modeHeight == MeasureSpec.UNSPECIFIED) {
                childHeightMode = MeasureSpec.UNSPECIFIED;
                childHeightSize = 0;
            }

            child.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
                    MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));

            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;

            //对于child width不符合的进行隐藏
            if (childWidth < minChildWidth || childWidth > maxChildWidth) {
                child.setVisibility(View.GONE);
                hiddenViews.add(child);
                if (lastChild) {
                    width = Math.max(width, lineWidth);
                    height += lineHeight;
                    lineCount++;
                }
                continue;
            }

            int hGap = getGap(horizontalGap, i - positionInLine);
            int vGap = getGap(verticalGap, i - positionInLine);
            
            //超过ViewGroup宽度，进行换行
            if (lineWidth + childWidth + hGap > sizeWidth) {

                width = Math.max(width, lineWidth);

                height += lineHeight;
                lineCount++;

                if (lineCount > maxLines) {
                    for (int j = i; j < childCount; j++) {
                        View childTmp = getChildAt(j);
                        childTmp.setVisibility(View.GONE);
                        hiddenViews.add(childTmp);
                    }

                    break;
                }

                lineWidth = childWidth;
                lineHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin + vGap ;

                positionInLine = i;

            } else {
                lineWidth = lineWidth + childWidth + hGap;
                lineHeight = Math.max(lineHeight, child.getMeasuredHeight() + lp.topMargin + lp
                        .bottomMargin);
            }

            if (lastChild) {
                width = Math.max(width, lineWidth);
                height += lineHeight;
                lineCount++;
            }
        }

        width += getPaddingLeft() + getPaddingRight();

        setMeasuredDimension((modeWidth == MeasureSpec.EXACTLY) ? sizeWidth : width, (modeHeight
                == MeasureSpec.EXACTLY) ? sizeHeight : height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mLines.clear();
        mLineHeights.clear();
        mLineMargins.clear();

        int width = getWidth();
        int height = getHeight();

        int linesSum = getPaddingTop();

        int lineWidth = 0;
        int lineHeight = 0;
        List<View> lineViews = new ArrayList<View>();

        float horizontalGravityFactor;
        switch ((mGravity & Gravity.HORIZONTAL_GRAVITY_MASK)) {
            case Gravity.LEFT:
            default:
                horizontalGravityFactor = 0;
                break;
            case Gravity.CENTER_HORIZONTAL:
                horizontalGravityFactor = .5f;
                break;
            case Gravity.RIGHT:
                horizontalGravityFactor = 1;
                break;
        }

        //行号
        int lineNumber = 0;
        //换行位置，记录每行第一个元素为位置
        int positionInLine = 0;

        for (int i = 0; i < getChildCount(); i++) {

            View child = getChildAt(i);

            if (child.getVisibility() == View.GONE) {
                continue;
            }

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.bottomMargin + lp.topMargin;

            int hGap = getGap(horizontalGap, i - positionInLine);
            int vGap = getGap(verticalGap, i - positionInLine);

            if (lineWidth + childWidth + hGap > width) {

                mLineHeights.add(lineHeight + vGap);
                mLines.add(lineViews);
                mLineMargins.add((int) ((width - lineWidth) * horizontalGravityFactor) +
                        getPaddingLeft());

                linesSum += lineHeight + vGap;

                lineHeight = 0;
                lineWidth = 0;
                lineViews = new ArrayList<View>();
                lineNumber++;
                positionInLine = i ;
            }

            lineWidth += childWidth + hGap;
            lineHeight = Math.max(lineHeight, childHeight);
            lineViews.add(child);
        }

        int vGap = getGap(verticalGap, lineNumber);
        mLineHeights.add(lineHeight + vGap);
        mLines.add(lineViews);
        mLineMargins.add((int) ((width - lineWidth) * horizontalGravityFactor) + getPaddingLeft());

        linesSum += lineHeight;

        int verticalGravityMargin = 0;
        switch ((mGravity & Gravity.VERTICAL_GRAVITY_MASK)) {
            case Gravity.TOP:
            default:
                break;
            case Gravity.CENTER_VERTICAL:
                verticalGravityMargin = (height - linesSum) / 2;
                break;
            case Gravity.BOTTOM:
                verticalGravityMargin = height - linesSum;
                break;
        }

        int numLines = mLines.size();

        int left;
        int top = getPaddingTop();

        //开始绘制每行对应的Child
        for (int i = 0; i < numLines; i++) {

            lineHeight = mLineHeights.get(i);
            lineViews = mLines.get(i);
            left = mLineMargins.get(i);

            int children = lineViews.size();

            for (int j = 0; j < children; j++) {

                View child = lineViews.get(j);

                if (child.getVisibility() == View.GONE) {
                    continue;
                }

                LayoutParams lp = (LayoutParams) child.getLayoutParams();

                // if height is match_parent we need to remeasure child to line height
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    int childWidthMode = MeasureSpec.AT_MOST;
                    int childWidthSize = lineWidth;

                    if (lp.width == LayoutParams.MATCH_PARENT) {
                        childWidthMode = MeasureSpec.EXACTLY;
                    } else if (lp.width >= 0) {
                        childWidthMode = MeasureSpec.EXACTLY;
                        childWidthSize = lp.width;
                    }

                    child.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
                            MeasureSpec.makeMeasureSpec(lineHeight - lp.topMargin - lp
                                    .bottomMargin, MeasureSpec.EXACTLY));
                }

                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();

                int gravityMargin = 0;

                if (Gravity.isVertical(lp.gravity)) {
                    switch (lp.gravity) {
                        case Gravity.TOP:
                        default:
                            break;
                        case Gravity.CENTER_VERTICAL:
                        case Gravity.CENTER:
                            gravityMargin = (lineHeight - childHeight - lp.topMargin - lp
                                    .bottomMargin ) / 2;
                            break;
                        case Gravity.BOTTOM:
                            gravityMargin = lineHeight - childHeight - lp.topMargin - lp
                                    .bottomMargin;
                            break;
                    }
                }

                child.layout(left + lp.leftMargin, top + lp.topMargin + gravityMargin +
                        verticalGravityMargin, left + childWidth + lp.leftMargin, top +
                        childHeight + lp.topMargin + gravityMargin + verticalGravityMargin);

                //根据位置决定是否添加水平空隙
                left += childWidth + lp.leftMargin + lp.rightMargin + getGap(horizontalGap, j + 1);
            }

            top += lineHeight;
        }

    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    public void setGravity(int gravity) {
        if (mGravity != gravity) {
            if ((gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
                gravity |= Gravity.START;
            }

            if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                gravity |= Gravity.TOP;
            }

            mGravity = gravity;
            requestLayout();
            invalidate();
        }
    }

    public int getGravity() {
        return mGravity;
    }

    /**
     * 设置最大行数
     *
     * @param maxLines 最大行数
     */
    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
        requestLayout();
        invalidate();
    }

    /**
     * 设置最大child view宽度
     *
     * @param maxChildWidth 最大宽度
     */
    public void setMaxChildWidth(float maxChildWidth) {
        this.maxChildWidth = maxChildWidth;
        requestLayout();
        invalidate();
    }

    /**
     * 设置最小child view宽度
     *
     * @param minChildWidth 最小宽度
     */
    public void setMinChildWidth(float minChildWidth) {
        this.minChildWidth = minChildWidth;
        requestLayout();
        invalidate();
    }

    /**
     * 设置水平间距
     *
     * @param horizontalGap 水平间距
     */
    public void setHorizontalGap(int horizontalGap) {
        this.horizontalGap = horizontalGap;
        requestLayout();
        invalidate();
    }

    /**
     * 设置垂直间距
     *
     * @param verticalGap 垂直间距
     */
    public void setVerticalGap(int verticalGap) {
        this.verticalGap = verticalGap;
        requestLayout();
        invalidate();
    }

    /**
     * 获取增加的间距
     *
     * @param gap      间距
     * @param position 位置
     * @return 应该增加的间距
     */
    private int getGap(int gap, int position) {
        return position == 0 ? 0 : gap;
    }

    public static class LayoutParams extends MarginLayoutParams {

        public int gravity = -1;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.FilterFlowLayout);

            try {
                gravity = a.getInt(R.styleable.FilterFlowLayout_android_layout_gravity, -1);
            } finally {
                a.recycle();
            }
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}