package xssoft.club.minio.misc;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.StatFs;

import java.io.File;

public class AndroidStatus {

    private AndroidStatus(){}

    private final static String UNKNOWN_OS_PARAMETER = "Unknown";

    public static String getApiVersion(){
        return Build.VERSION.RELEASE != null ? Build.VERSION.RELEASE : UNKNOWN_OS_PARAMETER;
    }

    public static String getOSVersion(){
        return Build.VERSION.INCREMENTAL != null ? Build.VERSION.INCREMENTAL : UNKNOWN_OS_PARAMETER;
    }

    public static String getKernelVersion(){
        return Build.VERSION.SDK_INT != 0 ? Integer.toString(Build.VERSION.SDK_INT) : UNKNOWN_OS_PARAMETER;
    }

    public static String getBuildNumber(){
        return System.getProperty("os.version") != null ? System.getProperty("os.version") : UNKNOWN_OS_PARAMETER;
    }

    public static String getDeviceName(){
        try{
            return !Build.MODEL.startsWith(Build.MANUFACTURER) ? Build.MANUFACTURER + " " + Build.MODEL : Build.MODEL;
        }
        catch (Exception e){
            return UNKNOWN_OS_PARAMETER;
        }
    }

    public static String getRamValue(Context context){
        try {
            ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memoryInfo);

            return Float.toString(memoryInfo.totalMem);
        }
        catch (Exception e){
            return UNKNOWN_OS_PARAMETER;
        }
    }

    public static String getRomValue(){
        try {
            File path = android.os.Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());

            return Long.toString(stat.getBlockCountLong() * stat.getBlockSizeLong());
        }
        catch (Exception e){
            return UNKNOWN_OS_PARAMETER;
        }
    }

}

