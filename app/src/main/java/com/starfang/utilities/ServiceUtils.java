package com.starfang.utilities;

import android.app.ActivityManager;
import android.content.Context;

public class ServiceUtils {
    public static boolean isServiceExist(Context context, Class<?> serviceClass, boolean checkForeground) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    if (checkForeground) {
                        if (service.foreground) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
