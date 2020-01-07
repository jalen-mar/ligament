package com.gemini.jalen.ligament.widget.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.gemini.jalen.ligament.R;
import com.gemini.jalen.ligament.widget.ViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class RecyclerAdapter<T, V extends ViewHolder> extends RecyclerView.Adapter<V> implements DataHolder {
    public final List<T> list;
    private int[] resId;
    private int emptyView;
    public final LayoutInflater inflater;

    public RecyclerAdapter(LayoutInflater inflater) {
        this(inflater, new ArrayList<T>());
    }

    public RecyclerAdapter(LayoutInflater inflater, List<T> list) {
        this.inflater = inflater;
        this.list = list;
        emptyView = R.layout.item_empty;
    }

    public RecyclerAdapter(LayoutInflater inflater, int... resId) {
        this(inflater, new ArrayList<T>());
        this.resId = resId;
    }

    public RecyclerAdapter(LayoutInflater inflater, List<T> list, int... resId) {
        this(inflater, list);
        this.resId = resId;
    }

    public int getEmptyView() {
        return emptyView;
    }

    @NonNull
    @Override
    public V onCreateViewHolder(@NonNull ViewGroup parent, int resId) {
        View view = inflater.inflate(resId, parent, false);
        ViewHolder holder;
        if (resId == getEmptyView()) {
            holder = RecyclerHolder.newInstance(view);
        } else {
            try {
                holder = RecyclerHolder.newInstance(DataBindingUtil.bind(view));
            } catch (Exception e) {
                holder = RecyclerHolder.newInstance(view);
            }
        }
        return (V) holder;
    }

    @Override
    public final void onBindViewHolder(@NonNull V holder, int position) {
        if (list.size() > position) {
            convert(holder, position);
        }
    }

    public abstract void convert(@NonNull V holder, int position);

    @Override
    public final int getItemViewType(int position) {
        if (list.size() > position) {
            Object obj = list.get(position);
            return resId[obj instanceof AdapterItem ? ((AdapterItem) obj).toIndex(obj) : 0];
        } else {
            return getEmptyView();
        }
    }

    @Override
    public int getItemCount() {
        int count = list.size();
        if (count == 0) {
            count += 1;
        }
        return count;
    }

    @Override
    public long getItemId(int position) {
        return position == -1 ? 5 : super.getItemId(position);
    }

    @Override
    public void addItem(Object obj) {
        list.add((T) obj);
        notifyItemInserted(list.size());
    }

    @Override
    public void addItem(List<Object> list) {
        int index = this.list.size();
        this.list.addAll((Collection<? extends T>) list);
        notifyItemRangeInserted(index, list.size());
    }

    public void update(Object obj) {
        notifyItemChanged(list.indexOf(obj));
    }

    public void delete(Object obj) {
        int index = list.indexOf(obj);
        list.remove(obj);
        notifyItemRemoved(index);
    }

    @Override
    public void clear() {
        int count = list.size();
        list.clear();
        notifyItemRangeRemoved(0, count);
    }

    @Override
    public void moveItem(int from, int to) {
        if (from < to) {
            for (int i = from; i < to; i++) {
                Collections.swap(list, i, i + 1);
            }
        } else {
            for (int i = from; i > to; i--) {
                Collections.swap(list, i, i - 1);
            }
        }
        notifyItemMoved(from, to);
    }
}
