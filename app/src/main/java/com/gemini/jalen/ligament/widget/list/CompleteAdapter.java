package com.gemini.jalen.ligament.widget.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.gemini.jalen.ligament.widget.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class CompleteAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final LayoutInflater inflater;
    private final List<Object> list;
    private int header, footer, item;

    public CompleteAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
        this.list = new ArrayList<>();
        this.header = this.footer = this.item = 0;
    }

    public boolean isHeaderView(int position) {
        return position < header;
    }

    private boolean isItemView(int position) {
        return !(isHeaderView(position) | isFooterView(position));
    }

    public boolean isFooterView(int position) {
        return position >= getItemCount() - footer;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int resId) {
        View view = inflater.inflate(resId, parent, false);
        ViewHolder holder;
        if (resId == getEmptyView()) {
            holder = RecyclerHolder.newInstance(view);
        } else {
            holder = RecyclerHolder.newInstance(DataBindingUtil.bind(view));
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (isHeaderView(position)) {
            onBindHeadHolder(holder, position);
            return;
        }
        if (item == 0) {
            return;
        }
        if (isItemView(position)) {
            onBindItemHolder(holder,position - header);
            return;
        }
        if (isFooterView(position)) {
            onBindFootHolder(holder, footer - getItemCount() + position);
        }
    }

    protected void onBindHeadHolder(ViewHolder holder, int position) {}

    protected void onBindItemHolder(ViewHolder holder, int position) {}

    public void onBindFootHolder(ViewHolder holder, int position) {}

    @Override
    public final int getItemViewType(int position) {
        if (isHeaderView(position)) {
            return getHeaderView(position);
        }

        if (isFooterView(position)) {
            return getFooterView(footer - getItemCount() + position);
        }

        if (item == 0) {
            return getEmptyView();
        }

        return getItemView(position - header);
    }

    protected int getHeaderView(int position) {
        return 0;
    }

    protected int getItemView(int position) {
        return 0;
    }

    protected int getFooterView(int position) {
        return 0;
    }

    public int getEmptyView() {
        return com.gemini.jalen.ligament.R.layout.item_empty;
    }

    @Override
    public int getItemCount() {
        int count = list.size();
        if (item == 0) {
            count++;
        }
        return count;
    }

    @Override
    public long getItemId(int position) {
        return position == -1 ? 5 : super.getItemId(position);
    }

    public <T> T getItem(int index) {
        T result = null;
        if (index < item) {
            result = (T) list.get(index + header);
        }
        return result;
    }

    public void addItem(List<Object> list) {
        int index = this.list.size() - footer;
        this.list.addAll(index, list);
        item += list.size();
        if (index + footer == 0) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(index, list.size());
        }
    }

    public void addItem(Object obj) {
        int index = this.list.size() - footer;
        this.list.add(index, obj);
        item++;
        notifyItemInserted(index);
    }

    public void clear() {
        int count = this.list.size() - footer;
        List tempData = new ArrayList(count);
        for (int i = header; i < count; i++) {
            tempData.add(this.list.get(i));
        }
        this.list.removeAll(tempData);
        notifyItemRangeRemoved(header, count - header);
    }

    public <T> T getHeadItem(int index) {
        T result = null;
        if (index < header) {
            result = (T) list.get(index);
        }
        return result;
    }

    public void addHeadItem(List<Object> list) {
        int count = this.list.size();
        this.list.addAll(header, list);
        if (count == 0) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(header, list.size());
        }
        header += list.size();
    }

    public CompleteAdapter addHeadItem(Object obj) {
        this.list.add(header, obj);
        notifyItemInserted(header++);
        return this;
    }

    public <T> T getFootItem(int index) {
        T result = null;
        if (index < footer) {
            result = (T) list.get(header + item + index);
        }
        return result;
    }

    public void addFootItem(List<Object> list) {
        int index = this.list.size();
        this.list.addAll(list);
        if (index == 0) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(index, list.size());
        }
        footer += list.size();
    }

    public void addFootItem(Object obj) {
        int index = this.list.size();
        this.list.add(obj);
        notifyItemInserted(index);
        footer++;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager _GridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup _SpanSizeLookup = _GridLayoutManager.getSpanSizeLookup();
            _GridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if(!isItemView(position)) {
                        return _GridLayoutManager.getSpanCount();
                    }
                    if (_SpanSizeLookup != null) {
                        return _SpanSizeLookup.getSpanSize(position);
                    }
                    return 1;
                }
            });
            _GridLayoutManager.setSpanCount(_GridLayoutManager.getSpanCount());
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        if(!isItemView(holder.getLayoutPosition())) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) lp;
                params.setFullSpan(true);
            }
        }
        super.onViewAttachedToWindow(holder);
    }
}

