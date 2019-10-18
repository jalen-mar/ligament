package com.gemini.jalen.ligament.widget;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.gemini.jalen.ligament.R;
import com.gemini.jalen.ligament.util.AppUtil;

public class Toast implements Runnable {
    private static android.widget.Toast toast;
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static Context applicationContext;
    private static Toast task;
    private static int resid;
    private static int messageID;
    private static int gravity;
    private static final long duration = 2000L;

    public static void init(Context context) {
        Resources resources = Resources.getSystem();
        init(context, resources.getIdentifier("transient_notification", "layout", "android"),
                resources.getIdentifier("message", "id", "android"), -1);
    }

    public static void init(Context context, int layoutId, int msgId, int gravity) {
        applicationContext = context.getApplicationContext();
        task = new Toast();
        resid = layoutId == 0 ? R.layout.dialog_toast : layoutId;
        messageID = msgId == 0 ? R.id.toast_text : msgId;
        Toast.gravity = gravity;
    }

    public static Context getApplicationContext() {
        if (applicationContext == null) {
            init(AppUtil.getApplicationContext());
        }
        return applicationContext;
    }

    public static void show(String message) {
        show(getApplicationContext(), message, duration);
    }

    public static void show(Context context, String message) {
        show(context, message, duration);
    }

    public static void show(Context context, String message, long duration) {
        if (toast != null) {
            handler.removeCallbacks(task);
            hide();
        }

        LayoutInflater inflate = LayoutInflater.from(context);
        View view = inflate.inflate(resid, null);
        toast = new android.widget.Toast(context);
        if (gravity != -1) {
            toast.setGravity(gravity, 0, 0);
        }
        toast.setView(view);
        toast.setDuration(android.widget.Toast.LENGTH_LONG);
        TextView tv = view.findViewById(messageID);
        tv.setText(message);
        toast.show();
        handler.postDelayed(task, duration);
    }

    public static void hide() {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }

    }

    public void run() {
        hide();
    }
}