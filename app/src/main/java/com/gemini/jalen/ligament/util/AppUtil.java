package com.gemini.jalen.ligament.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class AppUtil implements Application.ActivityLifecycleCallbacks {
    private static Context applicationContext;
    private static Activity currentActivity;

    public static void register(Application context) {
        applicationContext = context.getApplicationContext();
        context.registerActivityLifecycleCallbacks(new AppUtil());
    }

    /**
     * @since 1.0
     * @return 应用上下文
     */
    @SuppressLint("PrivateApi")
    public static Context getApplicationContext() {
        if (applicationContext == null) {
            try {
                Class<?> cls = Class.forName("android.app.ActivityThread");
                Method method = cls.getMethod("currentActivityThread");
                Object obj = method.invoke(null);
                method = cls.getMethod("getApplication");
                applicationContext = ((Application) method.invoke(obj)).getApplicationContext();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return applicationContext;
    }

    /**
     * @since 1.0
     * @return 获取当前最顶层 Activity
     */
    public static Activity getActivity() {
        return currentActivity;
    }

    /**
     * @since 1.0
     * @param context 上下文
     * @return 版本号
     */
    public static int getVersionCode(Context context) {
        int version;
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(
                    context.getPackageName(),0);
            version = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            version = 0;
        }
        return version;
    }

    /**
     * @since 1.0
     * @param context 上下文
     * @return 版本名
     */
    public static String getVersionName(Context context) {
        String versionName;
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo packInfo = manager.getPackageInfo(
                    context.getPackageName(),0);
            versionName = packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "";
        }
        return versionName;
    }

    /**
     * @since 1.0
     * @param context 上下文
     * @param key Manifest配置信息主键
     * @return Manifest配置信息
     */

    public static String getMetaData(Context context, String key) {
        String name = context.getPackageName();
        PackageManager manager = context.getPackageManager();
        String value;
        try {
            ApplicationInfo info = manager.getApplicationInfo(name,
                    PackageManager.GET_META_DATA);
            value = info.metaData.getString(key);
        } catch (PackageManager.NameNotFoundException e) {
            value = null;
        }
        return value;
    }

    /**
     * @since 1.0
     * @param context 上下文
     * @return 是否为debug运行
     */
    public static boolean debug(Context context) {
        int mode = 0;
        ApplicationInfo info = context.getApplicationInfo();
        if (info != null) {
            mode = info.flags & ApplicationInfo.FLAG_DEBUGGABLE;
        }
        return mode != 0;
    }

    /**
     * @since 1.0
     * @param context 上下文
     * @param targetFile 安装文件
     */
    public static void install(Context context, File targetFile, Runnable task) {
        if (targetFile.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri apkUri;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                apkUri = Uri.parse("file://" + targetFile.toString());
            } else {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                apkUri = FileProvider.getUriForFile(context, context.getPackageName(), targetFile);
            }
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : list) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            context.startActivity(intent);
            if (task != null)
                task.run();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    /**
     * @since 1.0
     * @param context 上下文
     * @param packageName 应用包名
     */
    public static void uninstall(Context context, String packageName) {
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        context.startActivity(intent);
    }

    /**
     * @since 1.0
     * @param context 运行环境
     * @param packageName 包名
     * @return 对应的apk是否安装
     */
    public static boolean avilible(Context context, String... packageName) {
        PackageManager manager = context.getPackageManager();
        List<PackageInfo> list = manager.getInstalledPackages(0);
        if (list != null) {
            List<String> target = Arrays.asList(packageName);
            for (int i = 0; i < list.size(); i++) {
                String name = list.get(i).packageName;
                if (target.contains(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @since 1.0
     * @param context 运行环境
     * @return 手机唯一识别码
     */
    @SuppressLint("MissingPermission")
    public static String getIMEI(Context context) {
        String result;
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            result = manager.getDeviceId();
            if (result == null || result.trim().length() == 0) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            result = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return result;
    }

    /**
     * @since 1.0
     * @param context 运行环境
     * @return 电话卡唯一识别码
     */
    @SuppressLint("MissingPermission")
    public static String getIMSI(Context context) {
        String result;
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            result = manager.getSubscriberId();
            if (result == null || result.trim().length() == 0) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            result = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return result;
    }

    /**
     * @since 1.0
     * @return 获取MAC地址
     */
    public static String getMAC() {
        String result = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                NetworkInterface networkInterface;
                while (interfaces.hasMoreElements()) {
                    networkInterface = interfaces.nextElement();
                    result = bytesToString(networkInterface.getHardwareAddress());
                    if (result != null)
                        break;
                }
            }
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    private static String bytesToString(byte[] bytes) {
        if (bytes != null && bytes.length != 0) {
            StringBuilder buf = new StringBuilder();
            for (byte b : bytes) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            return buf.toString();
        }
        return null;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {}

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (currentActivity == activity) {
            currentActivity = null;
        }
    }
}
