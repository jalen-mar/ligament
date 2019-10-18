package com.gemini.jalen.ligament.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    public static String save(Context context, Bitmap bitmap, int quality) {
        return save(getImageName(getImagePath(context)), bitmap, quality);
    }

    public static String save(File target, Bitmap bitmap, int quality) {
        try {
            FileOutputStream writer = new FileOutputStream(target);
            BufferedOutputStream buffer = new BufferedOutputStream(writer);
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, buffer);
            buffer.flush();
            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return target.getAbsolutePath();
    }

    public static File getImageName(File file) {
        return new File(file,"IMG_" + System.currentTimeMillis() + ".jpg");
    }

    public static File getImagePath(Context context) {
        File file;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        } else {
            file = new File(context.getFilesDir(), "DCIM");
        }
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }
}
