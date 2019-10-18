package com.gemini.jalen.ligament.widget.list;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gemini.jalen.ligament.util.MeasureUtil;

public class RecycleViewDivider extends RecyclerView.ItemDecoration {
    public static final int TYPE_DP = 0;
    public static final int TYPE_PX = 1;

    private Drawable drawable;
    private int size;

    public RecycleViewDivider(Context context, Drawable drawable, int dp) {
        this(context, drawable, dp, TYPE_DP);
    }

    public RecycleViewDivider(Context context, Drawable drawable, int size, int type) {
        this.drawable = drawable;
        switch (type) {
            case TYPE_DP: {
                this.size = MeasureUtil.dp2px(context, size);
            }
            break;
            case TYPE_PX: {
                this.size = size;
            }
            break;
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        drawHorizontal(c, parent);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(0, 0, 0, size);
    }

    private void drawHorizontal(Canvas c, RecyclerView parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int left = child.getLeft();
            int top = child.getBottom();
            int right = child.getRight();
            int bottom = top + size;
            drawable.setBounds(left, top, right, bottom);
            drawable.draw(c);
        }
    }
}

