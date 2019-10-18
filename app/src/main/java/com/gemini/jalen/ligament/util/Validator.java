package com.gemini.jalen.ligament.util;

import com.gemini.jalen.ligament.widget.Toast;

public class Validator implements ErrorCallback {
    private ErrorCallback callback;

    public static Validator getInstance() {
        return getInstance(null);
    }

    public static Validator getInstance(ErrorCallback callback) {
        return new Validator(callback);
    }

    public Validator isNotEmpty(String value, String msg) {
        return check(!StringUtil.isEmpty(value), msg);
    }

    public Validator isEquals(String value1, String value2, String msg) {
        return check(!(StringUtil.isEmpty(value1) || !value1.equals(value2)), msg);
    }

    public Validator isCheck(boolean value, String msg) {
        return check(value, msg);
    }

    public Validator isMobile(String value, String msg) {
        return check(StringUtil.isMobile(value), msg);
    }

    public Validator isEmail(String value, String msg) {
        return check(StringUtil.isEmail(value), msg);
    }

    public Validator isLicensePlate(String value, String msg) {
        return check(StringUtil.isLicensePlate(value), msg);
    }

    public Validator isBankCard(String value, String msg) {
        return check(StringUtil.isBankCard(value), msg);
    }

    public Validator isIP(String value, String msg) {
        return check(StringUtil.isIP(value), msg);
    }

    public Validator verify(String value, String msg) {
        return check(StringUtil.verify(value), msg);
    }

    public Validator between(String value, int min, int max, String msg) {
        return check(StringUtil.between(value, min, max), msg);
    }

    private Validator check(boolean val, String msg) {
        Validator result = null;
        if (val) {
            result = this;
        } else {
            callback.showError(msg);
        }
        return result;
    }

    public void run(Runnable task) {
        task.run();
    }

    @Override
    public void showError(String msg) {
        Toast.show(msg);
    }

    private Validator(ErrorCallback callback) {
        this.callback = callback == null ? this : callback;
    }
}
