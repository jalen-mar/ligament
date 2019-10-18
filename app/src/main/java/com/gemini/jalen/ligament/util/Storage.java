package com.gemini.jalen.ligament.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class Storage implements StringUtil.DeviceStorage {
    private Application application;

    public Storage(Application application) {
        this.application = application;
    }

    public SharedPreferences getGlobalStorage() {
        return application.getSharedPreferences("APP_STORAGE", Context.MODE_PRIVATE);
    }

    public String getCurrentUserId() {
        return getGlobalStorage().getString("current_user_id", "");
    }

    public SharedPreferences getUserStorage() {
        return application.getSharedPreferences(getCurrentUserId(), Context.MODE_PRIVATE);
    }

    public String getCurrentUserToken() {
        return getUserStorage().getString("current_user_authentication", null);
    }

    public void save(String userId, String token) {
        getGlobalStorage().edit().putString("current_user_id", userId).apply();
        getUserStorage().edit().putString("current_user_authentication", token).apply();
    }

    public void delete() {
        getUserStorage().edit().clear().apply();
        getGlobalStorage().edit().remove("current_user_id").apply();
    }

    @Override
    public String getDeviceId() {
        return getGlobalStorage().getString("APP_DEVICE_ID", null);
    }

    @Override
    public void saveDeviceId(String deviceId) {
        getGlobalStorage().edit().putString("APP_DEVICE_ID", deviceId).apply();
    }

    @Override
    public Application getApplication() {
        return application;
    }
}
