package com.gemini.jalen.ligament.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class TimerView extends AppCompatTextView implements Runnable, LifecycleObserver {
    private long time;
    private long unit;
    private long delay;
    private String value;
    private Handler handler = new Handler();
    private boolean started;
    private String prefix;
    private String suffix;
    private Callback callback;

    public TimerView(Context context) {
        this(context, null);
    }
    public TimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TimerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void start(long time, long unit, long delay) {
        start(time, unit, delay, "", "秒");
    }

    public void start(long time, long unit, long delay, String prefix, String suffix) {
        if(!started) {
            callback = callback == null ? new Callback() {
                @Override
                public String value(long time) {
                    return TimerView.this.prefix + time + TimerView.this.suffix;
                }

                @Override
                public String value() {
                    return value;
                }
            } : callback;
            started = true;
            this.prefix = prefix;
            this.suffix = suffix;
            this.time = time + unit;
            this.unit = unit;
            this.delay = delay;
            value = getText().toString();
            handler.post(this);
        }
    }

    @Override
    public void run() {
        if (callback != null) {
            time -= unit;
            if (time > 0) {
                setText(callback.value(time));
                handler.postDelayed(this, delay);
            } else {
                setText(callback.value());
                started = false;
            }
        }
    }


    public boolean isRunning() {
        return started;
    }

    public interface Callback {
        String value(long time);
        String value();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void stop() {
        handler.removeCallbacks(this);
        callback = null;
        handler = null;
    }
}
