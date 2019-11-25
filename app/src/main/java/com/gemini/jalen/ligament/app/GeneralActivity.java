package com.gemini.jalen.ligament.app;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.gemini.jalen.ligament.databinding.Event;
import com.gemini.jalen.ligament.databinding.WindowModel;
import com.gemini.jalen.ligament.util.StatusBarUtil;

public abstract class GeneralActivity<T extends ViewDataBinding> extends AppCompatActivity {
    public static final int REQUEST_INSTALL = PermissionFragment.REQUEST_INSTALL;
    public static final int REQUEST_LOCATION = PermissionFragment.REQUEST_LOCATION;

    private View contentView;
    protected T binder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionFragment.injectIfNeededIn(this);
        initView();
    }

    @Override
    protected void onDestroy() {
        if (binder != null) {
            binder.unbind();
        }
        super.onDestroy();
    }

    private void initView() {
        Window window = getWindow();
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        ViewGroup parentView = decorView.findViewById(android.R.id.content);
        contentView = getContentView(parentView);
        if (contentView != null) {
            createStatusView(window, decorView);
            setContentView(contentView, contentView.getLayoutParams());
        }
    }

    private void createStatusView(Window window, ViewGroup decorView) {
        if ((window.getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN)
                != WindowManager.LayoutParams.FLAG_FULLSCREEN) {
            StatusBarUtil.injectStatusView(window, decorView, contentView,
                    getStatusBackground((ViewGroup) contentView));
        }
    }

    protected T getBinder() {
        return binder;
    }

    protected boolean isSupportDataBinding() {
        return true;
    }

    @MainThread
    public ViewModelProvider createViewModelProvider(ViewModelProvider.Factory factory) {
        return PermissionFragment.createViewModel(this, factory);
    }

    @MainThread
    public <M extends WindowModel> M wrap(final M vm) {
        M result = (M) createViewModelProvider(new ViewModelProvider.Factory() {
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
                    it.invoke(GeneralActivity.this);
                }
            }
        });
        return result;
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

    protected View getContentView(ViewGroup parentView) {
        int resId = getLayout();
        View contentView = null;
        if (resId != View.NO_ID) {
            contentView = getLayoutInflater().inflate(getLayout(), parentView, false);
            if (isSupportDataBinding()) {
                binder = DataBindingUtil.bind(contentView);
            }
        }
        return contentView;
    }

    protected void requestPermission(String[] permissions, int requestCode) {
        PermissionFragment.get(this).requestPermission(permissions, requestCode);
    }
}
