package com.gemini.jalen.ligament.util;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;

import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    public static final String mobileRegex = "^0?(13[0-9]|15[012356789]|18[0-9]|14[57]|17[03678])[0-9]{8}";
    public static final String emailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
    public static final String carIDRegex = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}";
    private static final String ipRegex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." +
            "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
    private static final int[] WI = new int[]{7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1};
    private static final int[] VI = new int[]{1, 0, 88, 9, 8, 7, 6, 5, 4, 3, 2};
    private static int[] AI = new int[18];
    private static String[] NUMBER = { "零", "一", "二", "三", "四", "五", "六", "七", "八", "九" };
    private static String[] UNIT = { "十", "百", "千", "万", "十", "百", "千", "亿", "十", "百", "千" };

    public static String encodeMobile(String mobile) {
        return isMobile(mobile) ? encode(0, 5, mobile) : mobile;
    }

    public static String encode(int start, int end, String value) {
        start += 3;
        end += 2;
        if (between(value,end + 1, Integer.MAX_VALUE)) {
            int len = end - start;
            StringBuffer buff = new StringBuffer();
            for (int i = 0; i < len; i++)
                buff.append('*');
            return value.substring(0, start) + buff.toString() + value.substring(end);
        }
        return value;
    }

    public static boolean isEmpty(String value) {
        boolean empty = false;
        if (value == null || value.trim().length() == 0) {
            empty = true;
        }

        return empty;
    }

    public static int length(String val) {
        int len = 0;
        if (!isEmpty(val)) {
            len = val.length();
        }

        return len;
    }

    public static boolean between(String value, int min, int max) {
        boolean result = false;
        if (!isEmpty(value)) {
            int len = value.length();
            if (len >= min && len <= max) {
                result = true;
            }
        }

        return result;
    }

    public static boolean isMobile(String mobile) {
        boolean result = false;
        if (!isEmpty(mobile)) {
            result = matcher(mobile, mobileRegex);
        }

        return result;
    }

    public static boolean matcher(String val, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(val);
        return matcher.matches();
    }

    public static boolean isEmail(String email){
        boolean result = false;
        if(!isEmpty(email)) {
            result = matcher(email, emailRegex);
        }
        return result;
    }

    public static boolean isIP(String ip) {
        boolean result = false;
        if(!isEmpty(ip)) {
            result = matcher(ip, ipRegex);
        }
        return result;
    }

    public static boolean isLicensePlate(String id) {
        boolean result = false;
        if(!isEmpty(id)) {
            result = matcher(id, carIDRegex);
        }
        return result;
    }

    public static boolean isBankCard(String cardId) {
        boolean result = false;
        if(!isEmpty(cardId)) {
            char bit = getBankCardCheckCode(cardId.substring(0, cardId.length() - 1));
            if (bit == 'N') {
                return false;
            }
            result = cardId.charAt(cardId.length() - 1) == bit;
        }
        return result;
    }

    private static char getBankCardCheckCode(String nonCheckCodeCardId) {
        if(nonCheckCodeCardId == null || nonCheckCodeCardId.trim().length() == 0
                || !nonCheckCodeCardId.matches("\\d+")) {
            return 'N';
        }
        char[] chs = nonCheckCodeCardId.trim().toCharArray();
        int luhmSum = 0;
        for(int i = chs.length - 1, j = 0; i >= 0; i--, j++) {
            int k = chs[i] - '0';
            if(j % 2 == 0) {
                k *= 2;
                k = k / 10 + k % 10;
            }
            luhmSum += k;
        }
        return (luhmSum % 10 == 0) ? '0' : (char)((10 - luhmSum % 10) + '0');
    }

    public static boolean verify(String id) {
        if(!isEmpty(id)) {
            if (id.length() == 15) {
                id = uptoeighteen(id);
            }
            if (id.length() != 18) {
                return false;
            }
            String verify = id.substring(17, 18);
            if (verify.equals(getVerify(id))) {
                return true;
            }
        }
        return false;
    }

    public SpannableStringBuilder build(String val, int color, int start, int end) {
        SpannableStringBuilder result = new SpannableStringBuilder(val);
        return build(result, color, start, end);
    }

    public SpannableStringBuilder build(SpannableStringBuilder val, int color, int start, int end) {
        ForegroundColorSpan span = new ForegroundColorSpan(color);
        val.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return val;
    }

    private static String uptoeighteen(String id) {
        StringBuffer eighteen = new StringBuffer(id);
        eighteen = eighteen.insert(6, "19");
        return eighteen.toString();
    }

    private static String getVerify(String id) {
        int remain = 0;
        if (id.length() == 18) {
            id = id.substring(0, 17);
        }
        if (id.length() == 17) {
            int sum = 0;
            for (int i = 0; i < 17; i++) {
                String k = id.substring(i, i + 1);
                AI[i] = Integer.valueOf(k);
            }
            for (int i = 0; i < 17; i++) {
                sum += WI[i] * AI[i];
            }
            remain = sum % 11;
        }
        return remain == 2 ? "X" : String.valueOf(VI[remain]);
    }

    public static String upper(String val) {
        String result = val;
        if (!isEmpty(val)) {
            char initial = val.charAt(0);
            if (Character.isLetter(initial) && !Character.isUpperCase(initial)) {
                result = Character.toUpperCase(initial) + val.substring(1);
            }
        }
        return result;
    }

    public static String getFull(String val) {
        List<Translator.Token> tokens = Translator.getInstance().get(val);
        if (tokens == null || tokens.size() == 0) {
            return val;
        }
        StringBuffer result = new StringBuffer();
        for (Translator.Token token : tokens) {
            if (token.type == Translator.Token.PINYIN) {
                result.append(token.target);
            } else {
                result.append(token.source);
            }
        }
        return result.toString();
    }

    public static String getFirst(String val, int length) {
        List<Translator.Token> tokens = Translator.getInstance().get(val);
        if (tokens == null || tokens.size() == 0) {
            return val;
        }
        StringBuffer result = new StringBuffer();
        for (Translator.Token token : tokens) {
            if (token.type == Translator.Token.PINYIN) {
                result.append(token.target.charAt(0));
            } else {
                result.append("#");
            }
            if (--length == 0)
                break;
        }
        return result.toString();
    }

    public static String toChinese(int number) {
        StringBuffer result = new StringBuffer();
        String target = Integer.toString(Math.abs(number));
        int n = target.length();
        for (int i = 0; i < n; i++) {
            int num = target.charAt(i) - '0';
            if (i != n - 1 && num != 0) {
                result.append(NUMBER[num] + UNIT[n - 2 - i]);
            } else {
                result.append(NUMBER[num]);
            }
        }
        return result.toString();
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String getId(DeviceStorage storage) {
        String deviceId = storage.getDeviceId();
        if (!isEmpty(deviceId)) {
            return deviceId;
        }
        deviceId = getDeviceMac(storage.getApplication());
        if (!isEmpty(deviceId)) {
            return save(storage, deviceId, false);
        }
        TelephonyManager manager = ContextCompat.getSystemService(storage.getApplication(), TelephonyManager.class);
        deviceId = manager.getDeviceId();
        if (!isEmpty(deviceId)) {
            return save(storage, deviceId, true);
        }
        deviceId = manager.getSubscriberId();
        if (!isEmpty(deviceId)) {
            return save(storage, deviceId, true);
        }
        deviceId = manager.getSimSerialNumber();
        if (!isEmpty(deviceId)) {
            return save(storage, deviceId, true);
        }
        deviceId = Settings.Secure.getString(storage.getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);
        if (isEmpty(deviceId) || "9774d56d682e549c".equals(deviceId)) {
            return save(storage, new UUID(System.currentTimeMillis(), new Random().nextLong()).toString(), true);
        } else {
            return save(storage, deviceId, true);
        }
    }

    private static String getDeviceMac(Context context) {
        String mac;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = getDeviceMacV22(context);
        } else {
            mac = getDeviceMac();
        }
        return mac;
    }

    private static String getDeviceMac() {
        try {
            Enumeration<NetworkInterface> result = NetworkInterface.getNetworkInterfaces();
            while (result.hasMoreElements()) {
                NetworkInterface networkInterface = result.nextElement();
                byte[] address = networkInterface.getHardwareAddress();
                if (address == null || address.length == 0){
                    continue;
                }
                StringBuilder buff = new StringBuilder();
                for (byte b: address){
                    buff.append(String.format("%02X:",b));
                }
                if (buff.length() > 0){
                    buff.deleteCharAt(buff.length()-1);
                }
                return buff.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("MissingPermission")
    private static String getDeviceMacV22(Context context) {
        String mac = null;
        WifiManager wifi = ContextCompat.getSystemService(context, WifiManager.class);
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (info != null) {
            mac = info.getMacAddress();
            if (!isEmpty(mac)) {
                mac = mac.toUpperCase(Locale.ENGLISH);
            }
        }
        return mac;
    }

    private static String save(DeviceStorage storage, String deviceId, boolean format) {
        StringBuffer buff = new StringBuffer();
        if (format) {
            int len = deviceId.length() / 6;
            int code = 0;
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < len; j++) {
                    code += deviceId.charAt(i * len + j);
                }
                buff.append(String.format("%02X:",  code % 255));
            }
        } else {
            buff.append(deviceId);
        }
        storage.saveDeviceId(buff.toString());
        return deviceId;
    }

    public interface DeviceStorage {
        String getDeviceId();
        void saveDeviceId(String deviceId);
        Application getApplication();
    }
}
