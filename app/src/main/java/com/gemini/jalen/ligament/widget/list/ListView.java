package com.gemini.jalen.ligament.widget.list;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class ListView extends RecyclerView {
    public final MenuInfo info = new MenuInfo();

    public ListView(Context context) {
        super(context);
    }

    public ListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean showContextMenuForChild(View view, float x, float y) {
        LayoutManager layoutManager = getLayoutManager();
        if(layoutManager != null) {
            info.position = layoutManager.getPosition(view);
            info.holder = (ViewHolder) view.getTag();
        }
        return super.showContextMenuForChild(view, x, y);
    }

    @Override
    public ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return info;
    }

    public static class MenuInfo implements ContextMenu.ContextMenuInfo {
        private int position = -1;
        private ViewHolder holder;

        public int getPosition() {
            return position;
        }

        public <T extends ViewHolder> T getHolder() {
            return (T) holder;
        }
    }

    @BindingAdapter("app:loadable")
    public static void setLoadable(RecyclerView view, boolean loadable) {
        view.setTag(-1, loadable);
    }

    @InverseBindingAdapter(attribute = "app:loadable", event = "loadingAttrChanged")
    public static boolean isLoadable(RecyclerView view) {
        return (boolean) view.getTag(-1);
    }

    @BindingAdapter(value = {"app:onLoadListener", "loadingAttrChanged"}, requireAll = false)
    public static void setLoadListener(RecyclerView view, final RecyclerLoader listener,
                                       final InverseBindingListener refreshingAttrChanged) {
        OnScrollListener newValue = new OnScrollListener() {
            private int type;
            private int index;
            private int[] values;

            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                Boolean loading = (Boolean) recyclerView.getTag(-1);
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if (loading != null && loading && manager != null) {
                    if (type == 0) {
                        type = manager instanceof LinearLayoutManager ? 1 :
                                manager instanceof StaggeredGridLayoutManager ? 2 : -1;
                        index = (int) recyclerView.getAdapter().getItemId(-1);
                        index = index == -1 ? 1 : index;
                    }
                    if (canLoad(recyclerView.getLayoutManager(), type, index)) {
                        recyclerView.setTag(-1, false);
                        refreshingAttrChanged.onChange();
                        if (listener != null)
                            listener.onLoad();
                    }
                }
            }

            private boolean canLoad(RecyclerView.LayoutManager layoutManager, int type, int index) {
                boolean result = false;
                switch (type) {
                    case 1 : {
                        LinearLayoutManager manager = (LinearLayoutManager) layoutManager;
                        int last = manager.findLastVisibleItemPosition();
                        int count = manager.getItemCount();
                        result = last >= count - index - 1;
                    }
                    break;
                    case 2 : {
                        StaggeredGridLayoutManager manager =
                                (StaggeredGridLayoutManager) layoutManager;
                        if (values == null || values.length != manager.getSpanCount()) {
                            values = new int[manager.getSpanCount()];
                        }
                        int last = 0;
                        int count = manager.getItemCount();
                        for (int value : values) {
                            last = Math.max(value, last);
                        }
                        result = last >= count - index - 1;
                    }
                    break;
                }
                return result;
            }
        };
        view.addOnScrollListener(newValue);
    }
}
