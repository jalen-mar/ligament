package com.gemini.jalen.ligament.widget.list;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;

import com.gemini.jalen.ligament.widget.ViewHolder;

public class RecyclerHolder<T extends ViewDataBinding> extends ViewHolder {
    public final T binder;

    public RecyclerHolder(@NonNull T binder) {
        super(binder.getRoot());
        this.binder = binder;
    }

    public static ViewHolder newInstance(Object obj) {
        if (obj instanceof ViewDataBinding) {
            return new RecyclerHolder((ViewDataBinding) obj);
        } else {
            return new ViewHolder((View) obj);
        }
    }
}
