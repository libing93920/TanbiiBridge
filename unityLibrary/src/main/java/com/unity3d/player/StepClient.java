package com.unity3d.player;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;

public class StepClient {
    private static final String TAG = "StepClient";
    private static final int REQ_CODE_STEP_COUNTER = 111;
    private final List<OnStepChangeListener> listeners = new ArrayList<>();
    private boolean isInit = false;

    private static final class StepClientHolder {
        static final StepClient instance = new StepClient();
    }

    public static StepClient get() {
        return StepClientHolder.instance;
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                int counter = (int) event.values[0];
                Log.i(TAG, "onSensorChanged:" + counter);
                for (OnStepChangeListener listener : listeners) {
                    listener.onStepChange(counter);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    /**
     * 初始化
     */
    public void init(Context context) {
        if (context == null) {
            Log.e(TAG, "context is null");
            return;
        }
        if (isInit) {
            return;
        }
        if (context.checkSelfPermission(
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
            Log.w(TAG, "checkSelfPermission false");
            return;
        }
        SensorManager sensorManager =
                (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        isInit = true;
    }

    /**
     * 获取系统开机时间
     */
    public long getSystemBootTime() {
        return System.currentTimeMillis() - SystemClock.elapsedRealtime();
    }

    /**
     * 注册监听
     */
    public void registerListener(OnStepChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * 解注册监听
     */
    public void unRegisterListener(OnStepChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * 请求权限
     */
    public void requestPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= 29 && activity.checkSelfPermission(
                Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_DENIED
        ) {
            //ask for permission
            activity.requestPermissions(
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    REQ_CODE_STEP_COUNTER
            );
        }
    }

    /**
     * 权限授权结果
     */
    public void onRequestPermissionsResult(
            Activity activity,
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        if (requestCode == REQ_CODE_STEP_COUNTER) {
            if (Build.VERSION.SDK_INT >= 29) {
                if (activity.checkSelfPermission(
                        Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.i(TAG, "授权成功");
                    init(activity);
                } else {
                    Log.i(TAG, "授权失败");
                }
            }
        }
    }

    public interface OnStepChangeListener {
        void onStepChange(int count);
    }
}
