package com.arthenica.ffmpegkit;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
class CameraSupport {
    CameraSupport() {
    }

    static List<String> extractSupportedCameraIds(Context context) {
        ArrayList arrayList = new ArrayList();
        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService("camera");
            if (cameraManager != null) {
                for (String str : cameraManager.getCameraIdList()) {
                    Integer num = (Integer) cameraManager.getCameraCharacteristics(str).get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                    if (num != null && num.intValue() == 2) {
                        android.util.Log.d("ffmpeg-kit", "Detected camera with id " + str + " has LEGACY hardware level which is not supported by Android Camera2 NDK API.");
                    } else if (num != null) {
                        arrayList.add(str);
                    }
                }
            }
        } catch (CameraAccessException e) {
            android.util.Log.w("ffmpeg-kit", "Detecting camera ids failed.", e);
        }
        return arrayList;
    }
}
