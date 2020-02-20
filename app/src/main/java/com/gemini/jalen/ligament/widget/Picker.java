package com.gemini.jalen.ligament.widget;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gemini.jalen.ligament.R;
import com.gemini.jalen.ligament.widget.picker.PickerAdapter;
import com.gemini.jalen.ligament.widget.picker.PickerBean;
import com.gemini.jalen.ligament.widget.picker.PickerCallback;
import com.gemini.jalen.ligament.widget.picker.PickerProvider;

import java.util.ArrayList;
import java.util.List;

public class Picker extends DialogFragment implements View.OnClickListener, TextWatcher {
    private List<PickerBean> ids;
    private PickerProvider provider;
    private SenderHandler sender;
    private PickerCallback callback;
    private boolean multiple;

    private HorizontalScrollView scrollView;
    private ViewGroup toolbar;
    private RecyclerView recycler;
    private boolean filter;

    public Picker() {
        this(false);
    }

    public Picker(boolean filter) {
        this.filter = filter;
        sender = new SenderHandler();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.dialogTheme);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        if (container == null)
            container = window.findViewById(android.R.id.content);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.BOTTOM);
        View view = inflater.inflate(R.layout.dialog_picker, container, false);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        window.setLayout(params.width, params.height);
        view.findViewById(R.id.keyword).setVisibility(filter ? View.VISIBLE : View.GONE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scrollView = view.findViewById(R.id.scroll_layout);
        toolbar = view.findViewById(R.id.picker_toolbar);
        recycler = view.findViewById(R.id.list);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        ((EditText) view.findViewById(R.id.keyword)).addTextChangedListener(this);

        if (multiple) {
            view = view.findViewById(R.id.submit);
            view.setOnClickListener(this);
            view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ids == null) {
            ids = new ArrayList<>();
            provider.loadItem(null, sender);
        }
    }

    public void setCallback(PickerCallback callback) {
        this.callback = callback;
    }

    public void show(FragmentManager manager, PickerProvider provider) {
        show(manager, provider, false);
    }

    public void show(FragmentManager manager, PickerProvider provider, boolean multiple) {
        this.provider = provider;
        this.multiple = multiple;
        super.show(manager, toString());
    }

    @Override
    public void onClick(View v) {
        if (R.id.submit == v.getId()) {
            if (callback.selected(ids)) {
                dismiss();
            }
        } else if (v.getId() == R.id.item_picker_value) {
            PickerBean bean = (PickerBean) v.getTag();
            if (!multiple) {
                TextView titleView = (TextView) toolbar.getChildAt(toolbar.getChildCount() - 1);
                titleView.setText(bean.getName());
                titleView.setOnClickListener(this);
                ids.set(ids.indexOf(null), bean);
                if (callback.selected(ids)) {
                    provider.loadItem(bean, sender);
                }
                scrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            } else {
                ids.add(bean);
                recycler.getAdapter().notifyDataSetChanged();
            }
        } else {
            int index = toolbar.indexOfChild(v);
            int start = index + 1;
            toolbar.removeViews(start, toolbar.getChildCount() - start);
            ArrayList<PickerBean> list = new ArrayList<>();
            for (int i = index; i < ids.size() - 1; i++) {
                list.add(ids.get(i));
            }
            ids.removeAll(list);
            recycler.setAdapter((RecyclerView.Adapter) v.getTag());
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        if (recycler.getAdapter() instanceof PickerAdapter) {
            ((PickerAdapter) recycler.getAdapter()).setKeyword(s.toString());
        }
    }

    class SenderHandler implements PickerProvider.Sender {
        @Override
        public void send(List<PickerBean> list) {
            PickerAdapter adapter = new PickerAdapter(list, getLayoutInflater(), Picker.this, multiple);
            send(adapter);
        }

        private void send(PickerAdapter adapter) {
            getLayoutInflater().inflate(R.layout.item_picker_title, toolbar);
            TextView view = (TextView) toolbar.getChildAt(toolbar.getChildCount() - 1);
            view.setText("请选择");
            view.setTag(adapter);
            if (!multiple) {
                ids.add(null);
            }
            recycler.setAdapter(adapter);
        }
    }
}
