package com.gemini.jalen.ligament.app;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.gemini.jalen.ligament.databinding.Event;
import com.gemini.jalen.ligament.databinding.WindowModel;
import com.gemini.jalen.ligament.util.StatusBarUtil;

public class GeneralFragment<T extends ViewDataBinding> extends PermissionFragment {
    private View contentView;
    private T binder;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        container = new FrameLayout(getContext());
        int resId = getLayout();
        if (resId != View.NO_ID) {
            contentView = inflater.inflate(resId, container, false);
            if (isSupportDataBinding()) {
                binder = DataBindingUtil.bind(contentView);
            }
            container.addView(contentView);
            StatusBarUtil.injectStatusView(null, container, contentView, getStatusBackground((ViewGroup) contentView));
        }
        return container;
    }

    @Override
    public void onDestroyView() {
        if (binder != null) {
            binder.unbind();
        }
        super.onDestroyView();
    }

    protected Drawable getStatusBackground(ViewGroup view) {
        Drawable drawable;
        if (view.getChildCount() > 0) {
            drawable = view.getChildAt(0).getBackground();
            if (drawable == null) {
                drawable = view.getBackground();
            }
        } else {
            drawable = view.getBackground();
        }
        if (drawable == null) {
            drawable = new ColorDrawable(Color.TRANSPARENT);
        } else {
            drawable = drawable.getConstantState().newDrawable();
        }
        return drawable;
    }

    protected int getLayout() {
        return View.NO_ID;
    }

    protected boolean isSupportDataBinding() {
        return true;
    }

    protected T getBinder() {
        return binder;
    }

    @MainThread
    public <M extends WindowModel> M lease(Class<M> cls) {
        M result = createViewModel(getActivity()).get(cls);
        result.getStatus().observe(this, new Observer<Event>() {
            @Override
            public void onChanged(@Nullable Event it) {
                if (it != null) {
                    it.invoke(GeneralFragment.this);
                }
            }
        });
        return result;
    }

    @MainThread
    public <M extends WindowModel> M wrap(final M vm) {
        M result = (M) createViewModel(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) vm.init();
            }
        }).get(vm.getClass());
        result.getStatus().observe(this, new Observer<Event>() {
            @Override
            public void onChanged(@Nullable Event it) {
                if (it != null) {
                    it.invoke(GeneralFragment.this);
                }
            }
        });
        return result;
    }
}
