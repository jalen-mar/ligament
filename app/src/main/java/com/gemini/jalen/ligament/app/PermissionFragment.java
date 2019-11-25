package com.gemini.jalen.ligament.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.gemini.jalen.ligament.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PermissionFragment extends LifecycleFragment {
    public static final int REQUEST_INSTALL = 5;
    public static final int REQUEST_LOCATION = 6;
    public static final int REQUEST_BLUETOOTH = 7;
    public static final int STATE_BLUETOOTH_OPEN = 8;
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
        switch (requestCode) {
            case REQUEST_BLUETOOTH: {
                if (permissions.length == 0) {
                    checkBluetoothOpen();
                } else {
                    onPermissionsResult(STATE_BLUETOOTH_OPEN, new String[]{"您已禁用蓝牙相关权限,无法使用蓝牙功能!"}, result);
                }
            }
            break;
            default:
                getActivity().onRequestPermissionsResult(requestCode,  permissions, result);
        }
    }

    static PermissionFragment get(AppCompatActivity activity) {
        return (PermissionFragment) activity.getSupportFragmentManager().findFragmentByTag(PERMISSION_FRAGMENT_TAG);
    }

    //-------------------------------------API------------------------------------
    protected void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermission(new String []{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_BLUETOOTH);
        } else {
            checkBluetoothOpen();
        }
    }

    @SuppressLint("MissingPermission")
    private void checkBluetoothOpen() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            onPermissionsResult(STATE_BLUETOOTH_OPEN, new String[]{"当前设备不支持蓝牙功能!"}, new int[0]);
        } else {
            if (adapter.isEnabled()) {
                adapter.enable();
            } else {
                onPermissionsResult(STATE_BLUETOOTH_OPEN, new String[0], new int[0]);
            }
        }
    }
}
