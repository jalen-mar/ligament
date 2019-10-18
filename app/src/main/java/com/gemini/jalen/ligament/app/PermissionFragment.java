package com.gemini.jalen.ligament.app;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

public class PermissionFragment extends LifecycleFragment {
    public static final int REQUEST_INSTALL = 5;
    public static final int REQUEST_LOCATION = 6;
    private static final String PERMISSION_FRAGMENT_TAG = "com.gemini.jalen.ligament.permission_fragment";

    protected static void injectIfNeededIn(AppCompatActivity activity) {
        FragmentManager manager = activity.getSupportFragmentManager();
        if (manager.findFragmentByTag(PERMISSION_FRAGMENT_TAG) == null) {
            manager.beginTransaction().add(new PermissionFragment(), PERMISSION_FRAGMENT_TAG).commit();
            manager.executePendingTransactions();
        }
    }

    @Override
    public Context getContext() {
        Context context = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context = super.getContext();
        }
        if(context == null) {
            context = getActivity();
        }
        return context;
    }

    protected boolean usedLocService() {
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    protected void requestLocService() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        if (!PERMISSION_FRAGMENT_TAG.equals(getTag())) {
            startActivityForResult(intent, REQUEST_LOCATION);
        } else {
            getActivity().startActivityForResult(intent, REQUEST_LOCATION);
        }
    }

    private boolean checkPermission(String permission) {
        return getActivity().checkPermission(permission, Process.myPid(),
                Process.myUid()) == PackageManager.PERMISSION_GRANTED;
    }

    private List<String> checkPermission(String[] permissions) {
        List<String> authorities = new ArrayList<>();
        for (String permission : permissions) {
            if (!checkPermission(permission)) {
                authorities.add(permission);
            }
        }
        return authorities;
    }

    protected void requestPermission(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> authorities;
            if ((authorities = checkPermission(permissions)).size() == 0) {
                onRequestPermissionsResult(requestCode,  new String[0], new int[0]);
            } else {
                permissions = new String[authorities.size()];
                requestPermissions(authorities.toArray(permissions), requestCode);
            }
        } else {
            onRequestPermissionsResult(requestCode, new String[0], new int[0]);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public final void onRequestPermissionsResult(int requestCode, String[] permissions, int[] result) {
        for (int i = 0; i < result.length; i++) {
            if (result[i] != PackageManager.PERMISSION_GRANTED) {
                boolean reset = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    reset = shouldShowRequestPermissionRationale(permissions[i]);
                }
                if (reset){
                    requestPermission(permissions, requestCode);
                } else {
                    onPermissionsResult(requestCode, permissions, result);
                }
                return;
            }
        }
        onPermissionsResult(requestCode, new String[0], new int[0]);
    }

    public void onPermissionsResult(int requestCode, String[] permissions, int[] result) {
        getActivity().onRequestPermissionsResult(requestCode,  permissions, result);
    }

    protected boolean canInstall() {
        boolean can = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            can = getContext().getPackageManager().canRequestPackageInstalls();
        } else {
            ContentResolver resolver = getContext().getContentResolver();
            if (Settings.Secure.getInt(resolver, Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 0) {
                Settings.Secure.putInt(resolver, Settings.Secure.INSTALL_NON_MARKET_APPS, 1);
            }
        }
        return can;
    }

    @TargetApi(Build.VERSION_CODES.O)
    protected void requestInstall() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:" + getContext().getPackageName()));
        if (!PERMISSION_FRAGMENT_TAG.equals(getTag())) {
            startActivityForResult(intent, REQUEST_INSTALL);
        } else {
            getActivity().startActivityForResult(intent, REQUEST_INSTALL);
        }
    }

    static PermissionFragment get(AppCompatActivity activity) {
        return (PermissionFragment) activity.getSupportFragmentManager().findFragmentByTag(PERMISSION_FRAGMENT_TAG);
    }
}
