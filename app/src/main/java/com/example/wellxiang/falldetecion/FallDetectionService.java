package com.example.wellxiang.falldetecion;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
//import androidx.core.content.LocalBroadcastManager;
import android.util.Log;

public class FallDetectionService extends Service {

    private FallSensorManager fallSensorManager;
    public Fall fall;
    private final int FELL = 0;
//    private final int TIME = 1;
    private boolean running = false;
//    private TextView countingView;
//    private Dialog dialog;
//    private Timer timer;
    private final String TAG = "liuweixiang";
    private DetectThread detectThread;
    private IntentFilter intentFilter;
    private LocalBroadcastManager localBroadcastManager;
    private FallLocalReceiver fallLocalReceiver;

    public FallDetectionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "FallDetectionService.onCreate()");

        fallSensorManager = new FallSensorManager(this);
        fallSensorManager.initSensor();
        fallSensorManager.registerSensor();
        fall = new Fall();
        fall.setThresholdValue(25,5);
        running = true;
        //在通知栏上显示服务运行
        showInNotification();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.broadcast.FALL_LOCAL_BROADCAST");
        fallLocalReceiver = new FallLocalReceiver();
        localBroadcastManager.registerReceiver(fallLocalReceiver, intentFilter);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "FallDetectionService.onStartCommand");
        detectThread = new DetectThread();
        detectThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        fallSensorManager.unregisterSensor();
        localBroadcastManager.unregisterReceiver(fallLocalReceiver);
        super.onDestroy();
    }

    //开一个线程用于检测跌倒
    class DetectThread extends Thread{
        @Override
        public void run() {
            fall.fallDetection();
            Log.d(TAG, "DetectThread.start()");
            while (running) {
                if (fall.isFell()) {
                    Log.e(TAG, "跌倒了");
                    running = false;
                    Message msg = handler.obtainMessage();
                    msg.what = FELL;
                    handler.sendMessage(msg);
                    fall.setFell(false);
                    fall.cleanData();
                    stopSelf();

                }
            }
        }
    }

//    private Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            switch(msg.what){
//                case FELL:
//                    Log.e(TAG, "FELL");
//                    //报警
////                    showAlertDialog();
//                    Intent intent = new Intent("com.broadcast.FALL_LOCAL_BROADCAST");
//                    localBroadcastManager.sendBroadcast(intent);
//
//                    break;
//
//            }
//
//        }
//    };


    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FELL:
                    Log.e(TAG, "FELL");
                    //报警
//                    showAlertDialog();
                    Intent intent = new Intent("com.broadcast.FALL_LOCAL_BROADCAST");
                    localBroadcastManager.sendBroadcast(intent);

                    break;
            }
        }
    };


    /*
    在通知栏上显示服务运行
     */
    private void showInNotification() {
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this,"default")
                .setContentTitle("老人跌到检测")
                .setContentText("老人跌倒检测正在运行")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_app)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_app))
                .setContentIntent(pi)
                .build();
        startForeground(1,notification);
    }

}
