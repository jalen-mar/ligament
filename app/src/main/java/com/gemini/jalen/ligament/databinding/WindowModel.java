package com.gemini.jalen.ligament.databinding;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WindowModel extends ViewModel {
    private MutableLiveData<Event> status;
    private MutableLiveData<Boolean> loading;
    private ObservableBoolean refresh;
    private ObservableBoolean loadable;
    private int times;

    public WindowModel() {
        status = new MutableLiveData<>();
        loading = new MutableLiveData<>();
        refresh = new ObservableBoolean(false);
        loadable = new ObservableBoolean(true);
        times = 0;
    }

    public WindowModel init() {
        return this;
    }

    public MutableLiveData<Event> getStatus() {
        return status;
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public ObservableBoolean getRefresh() {
        return refresh;
    }

    public ObservableBoolean getLoadable() {
        return loadable;
    }

    public void load() {
        if (0 == times++) {
            loadable.set(false);
            loading.setValue(true);
        }
    }

    public void loadCompleted() {
        if (times != 0 && --times == 0) {
            loadable.set(true);
            loading.setValue(false);
        }
    }

    public boolean isRefreshing() {
        return refresh.get();
    }

    public void unableLoad(boolean unable) {
        loadable.set(unable);
    }

    public void refresh(boolean refresh) {
        loadable.set(!refresh);
        this.refresh.set(refresh);
    }

    public void doAction(String type, Object... params) {
        status.postValue(new Event(type, params));
    }
}
