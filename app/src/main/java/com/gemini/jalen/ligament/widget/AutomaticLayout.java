package com.gemini.jalen.ligament.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;

import com.gemini.jalen.ligament.R;

import java.util.ArrayList;

public class AutomaticLayout extends ViewGroup {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private int orientation;
    private int column;
    private boolean isEqually;
    private ArrayList<ArrayList<View>> children = new ArrayList<>();

    public AutomaticLayout(Context context) {
        this(context, null);
    }

    public AutomaticLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutomaticLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AutomaticLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutomaticLayout);
        orientation = a.getInt(R.styleable.AutomaticLayout_automatic_orientation, HORIZONTAL);
        column = a.getInt(R.styleable.AutomaticLayout_column, 0);
        isEqually = a.getBoolean(R.styleable.AutomaticLayout_isEqually, false);
        a.recycle();
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public void setOrientation(int orientation) {
        if (this.orientation != orientation) {
            this.orientation = orientation;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        children.clear();
        if (orientation == HORIZONTAL) {
            measureHorizontal(widthMeasureSpec, heightMeasureSpec);
        } else {
//            measureVertical(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        int width = 0, lineWidth = 0, lineHeight = 0;
        int height = getPaddingBottom() + getPaddingTop();
        int max = size - getPaddingLeft() - getPaddingRight();
        int item = column > 0 ? max / column : 0;
        int count = getChildCount();
        ArrayList<View> line = new ArrayList<>();
        ArrayList<Integer> rowWidth = null;

        int index = 0;
        if (item > 0 && isEqually) {
            rowWidth = new ArrayList<>();
            int padding = 0;
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == GONE) continue;
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                padding += (params.leftMargin + params.rightMargin);
                if (++index >= column) {
                    rowWidth.add((max - padding) / column);
                    padding = index = 0;
                }
            }
            if (count % column != 0) {
                rowWidth.add((max - padding) / column);
            }
            index = 0;
        }

        for(int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
            int childMeasureSpec = widthMeasureSpec;
            if (item > 0) {
                if (rowWidth == null) {
                    params.width = item - params.leftMargin - params.rightMargin;
                } else {
                    params.width = rowWidth.get(index++ / column);
                }
                childMeasureSpec = MeasureSpec.makeMeasureSpec(params.width, MeasureSpec.EXACTLY);
            }
            measureChild(child, childMeasureSpec, heightMeasureSpec);
            int childWidth = params.leftMargin + params.rightMargin + child.getMeasuredWidth();
            int childHeight = params.topMargin + params.bottomMargin + child.getMeasuredHeight();
            lineWidth += childWidth;
            if(lineWidth > max) {
                height += lineHeight;
                width = Math.max(width, lineWidth - childWidth);
                lineWidth = childWidth;
                lineHeight = childHeight;
                children.add(line);
                line = new ArrayList<>();
            } else {
                lineHeight = Math.max(lineHeight, childHeight);
            }

            line.add(child);

            if(i == count - 1) {
                height += lineHeight;
            }
        }

        if(line.size() > 0) children.add(line);
        if(mode == MeasureSpec.EXACTLY)
            width = size;
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (orientation == HORIZONTAL) {
            layoutHorizontal();
        } else {
//            layoutVertical();
        }
    }

    private void layoutHorizontal() {
        int rowCount = children.size();
        int paddingStart = getPaddingLeft();
        int childTop = getPaddingTop();
        for(int r = 0; r < rowCount; r++) {
            ArrayList<View> row = children.get(r);
            int columnCount = row.size();
            int childLeft = paddingStart;
            int lineHeight = 0;
            for(int c = 0; c < columnCount; c++) {
                View child = row.get(c);
                if (child.getVisibility() == GONE) continue;
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                child.layout(childLeft + params.leftMargin, childTop + params.topMargin,
                        childLeft + params.leftMargin + child.getMeasuredWidth(),
                        childTop + params.topMargin + child.getMeasuredHeight());
                childLeft += params.leftMargin + child.getMeasuredWidth() + params.rightMargin;

                lineHeight = Math.max(lineHeight, params.topMargin + params.bottomMargin + child.getMeasuredHeight());
            }
            childTop += lineHeight;
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        LayoutParams params = new MarginLayoutParams(getContext(), attrs);
        return params;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
    }
}

