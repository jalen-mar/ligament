package com.gemini.jalen.ligament.databinding;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WindowModel extends ViewModel {
    private MutableLiveData<Event> status;
    private MutableLiveData<Boolean> loading;
    private ObservableBoolean refresh;
    private ObservableBoolean canLoad;

    public WindowModel() {
        status = new MutableLiveData<>();
        loading = new MutableLiveData<>();
        refresh = new ObservableBoolean(false);
        canLoad = new ObservableBoolean(true);
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

    public ObservableBoolean getCanLoad() {
        return canLoad;
    }

    public void load() {
        loading.setValue(true);
    }

    public void loadCompleted() {
        loading.setValue(false);
    }

    public boolean isRefresh() {
        return refresh.get();
    }

    public void refresh(boolean refresh) {
        this.refresh.set(refresh);
    }

    public void doAction(String type, Object... params) {
        status.postValue(new Event(type, params));
    }
}
