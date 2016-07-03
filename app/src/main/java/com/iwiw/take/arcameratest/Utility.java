package com.iwiw.take.arcameratest;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {
    public static final String TAG = "@] MyApp";
    public static final String BR = System.getProperty("line.separator");
    private static final boolean IS_DETAIL = true;


    public interface IcallBackAction {
        public void callback();
    }

    public static void logDebug(String msg) {
        if (Utility.IS_DETAIL) {
            StackTraceElement callStack = Thread.currentThread().getStackTrace()[3];
            Log.d(TAG + callStack.getFileName() + "#" + callStack.getMethodName() + ":" + callStack.getLineNumber(), msg);
        } else {
            Log.d(TAG, msg);
        }
    }


    public static void logInfo(String msg) {
        if (Utility.IS_DETAIL) {
            StackTraceElement callStack = Thread.currentThread().getStackTrace()[3];
            Log.i(TAG
                    + callStack.getFileName() + "#" + callStack.getMethodName() + ":" + callStack.getLineNumber(), msg);
        } else {
            Log.i(TAG, msg);
        }
    }

    public static void logWarning(String msg) {
        if (Utility.IS_DETAIL) {
            StackTraceElement callStack = Thread.currentThread().getStackTrace()[3];
            Log.w(TAG
                    + callStack.getFileName() + "#" + callStack.getMethodName() + ":" + callStack.getLineNumber(), msg);
        } else {
            Log.w(TAG, msg);
        }
    }

    public static void logError(String msg) {
        if (Utility.IS_DETAIL) {
            StackTraceElement callStack = Thread.currentThread().getStackTrace()[3];
            Log.e(TAG
                    + callStack.getFileName() + "#" + callStack.getMethodName() + ":" + callStack.getLineNumber(), msg);
        } else {
            Log.e(TAG, msg);
        }
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void showToastDetail(Context context, String msg) {

        StackTraceElement callStack = Thread.currentThread().getStackTrace()[3];
        Toast.makeText(context, callStack.getFileName() + "#" + callStack.getMethodName() + ":" + callStack.getLineNumber()
                + BR + msg, Toast.LENGTH_LONG).show();
    }

    public static void registerContent(Activity activity, String path) {
        String[] paths = {path};
        String[] mimeTypes = {"image/jpeg"};
        MediaScannerConnection.scanFile(activity,
                paths,
                mimeTypes,
                null);
    }

    public static boolean getPermissionCamera(Activity activity) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    0);
            return false;
        } else {
            return true;
        }
    }

    public static boolean getPermissionStorage(Activity activity) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    0);
            return false;
        } else {
            return true;
        }
    }

    public static String getSaveDirectory() {
        File dir = new File(Environment.getExternalStorageDirectory().toString() + "/AnimatedPhoto");
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                Utility.logDebug("mkdir " + dir.getPath());
            } else {
                Utility.logError("Error mkdir");
            }
        }
        return dir.getPath();
    }

    public static Uri getSaveFile() {
        final Date date = new Date(System.currentTimeMillis());
        final SimpleDateFormat dataFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        final String filename = dataFormat.format(date) + ".jpg";
        Uri uri = Uri.fromFile(new File(Utility.getSaveDirectory(), filename));
        return uri;
    }

    public static void saveBitmap(Activity activity, Bitmap bitmap, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            registerContent(activity, path);
        } catch (Exception e) {
            logError("Save error");
            showToast(activity, "Failed to save file");
        }
    }

    public static int getBitmapRotation(String filePath) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    degree = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return degree;
    }

    private static int s_rearCameraId = -999;
    public static int getRearCameraId() {
        if(s_rearCameraId != -999) return s_rearCameraId;
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            // 指定したカメラの情報を取得
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                s_rearCameraId = i;
                return s_rearCameraId;
            }
        }
        s_rearCameraId = -1;
        return s_rearCameraId;
    }
    private static int s_frontCameraId = -999;
    public static int getFrontCameraId() {
        if(s_frontCameraId != -999) return s_frontCameraId;
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            // 指定したカメラの情報を取得
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                s_frontCameraId = i;
                return s_frontCameraId;
            }
        }
        s_frontCameraId = -1;
        return s_frontCameraId;
    }


    public static int getRotation(Activity activity) {
        int val = -1;
        Display d = activity.getWindowManager().getDefaultDisplay();
        int rotation = d.getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

}
