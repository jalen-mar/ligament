package com.gemini.jalen.ligament.util;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class EventUtil {
    private static Map<Object, MutableLiveData<?>> observables;

    static {
        observables = new HashMap<>();
    }

    public static <T extends Event> void register(LifecycleOwner owner, T event, Observer<T> observer) {
        MutableLiveData<T> data = (MutableLiveData<T>) observables.get(event.getAction());
        if (data == null) {
            observables.put(event.getAction(), data = new EventMutableLiveData<>());
            data.setValue(event);
        }
        data.observe(owner, observer);
    }

    @MainThread
    public static <T extends Event> void post(T obj) {
        MutableLiveData data = observables.get(obj.getAction());
        if (data != null) {
            data.setValue(obj);
        }
    }

    public static <T extends Event> void unregister(LifecycleOwner owner, Object cls) {
        MutableLiveData data = observables.get(cls);
        if (data != null) {
            data.removeObservers(owner);
            if (!data.hasObservers()) {
                observables.remove(data);
            }
        }
    }

    public static class Event {
        private String action;
        private Object params;

        public Event(String action, Object params) {
            this.action = action;
            this.params = params;
        }

        public String getAction() {
            return action;
        }

        public Object getParams() {
            return params;
        }
    }

    private static class ObserverWrapper<T> implements Observer<T> {

        private Observer<T> observer;

        public ObserverWrapper(Observer<T> observer) {
            this.observer = observer;
        }

        @Override
        public void onChanged(@Nullable T t) {
            if (observer != null) {
                if (isCallOnObserve()) {
                    return;
                }
                observer.onChanged(t);
            }
        }

        private boolean isCallOnObserve() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace != null && stackTrace.length > 0) {
                for (StackTraceElement element : stackTrace) {
                    if ("androidx.lifecycle.LiveData".equals(element.getClassName()) &&
                            "observeForever".equals(element.getMethodName())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    static class EventMutableLiveData<T> extends MutableLiveData<T> {
        private Map<Observer, Observer> observerMap = new HashMap<>();

        @Override
        public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
            super.observe(owner, observer);
            try {
                hook(observer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void observeForever(@NonNull Observer<? super T> observer) {
            if (!observerMap.containsKey(observer)) {
                observerMap.put(observer, new ObserverWrapper(observer));
            }
            super.observeForever(observerMap.get(observer));
        }

        @Override
        public void removeObserver(@NonNull Observer<? super T> observer) {
            Observer realObserver;
            if (observerMap.containsKey(observer)) {
                realObserver = observerMap.remove(observer);
            } else {
                realObserver = observer;
            }
            super.removeObserver(realObserver);
        }

        private void hook(@NonNull Observer<? super T> observer) throws Exception {
            //get wrapper's version
            Class<LiveData> classLiveData = LiveData.class;
            Field fieldObservers = classLiveData.getDeclaredField("mObservers");
            fieldObservers.setAccessible(true);
            Object objectObservers = fieldObservers.get(this);
            fieldObservers.setAccessible(false);
            Class<?> classObservers = objectObservers.getClass();
            Method methodGet = classObservers.getDeclaredMethod("get", Object.class);
            methodGet.setAccessible(true);
            Object objectWrapperEntry = methodGet.invoke(objectObservers, observer);
            methodGet.setAccessible(false);
            Object objectWrapper = null;
            if (objectWrapperEntry instanceof Map.Entry) {
                objectWrapper = ((Map.Entry) objectWrapperEntry).getValue();
            }
            if (objectWrapper == null) {
                throw new NullPointerException("Wrapper can not be bull!");
            }
            Class<?> classObserverWrapper = objectWrapper.getClass().getSuperclass();
            Field fieldLastVersion = classObserverWrapper.getDeclaredField("mLastVersion");
            fieldLastVersion.setAccessible(true);
            //get livedata's version
            Field fieldVersion = classLiveData.getDeclaredField("mVersion");
            fieldVersion.setAccessible(true);
            Object objectVersion = fieldVersion.get(this);
            fieldVersion.setAccessible(false);
            //set wrapper's version
            fieldLastVersion.set(objectWrapper, objectVersion);
            fieldLastVersion.setAccessible(false);
        }
    }
}
