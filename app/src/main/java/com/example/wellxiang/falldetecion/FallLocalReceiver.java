package com.example.wellxiang.falldetecion;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class FallLocalReceiver extends BroadcastReceiver implements AMapLocationListener {

    private TextView countingView;
    private Dialog dialog;
    private Timer timer;
    private SharedPreferences sharedPreferences;
    private Vibrator vibrator;
    private boolean isVibrate;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationClientOption;
    public String locationAddress;
    public String locationTime;
    private Context context;
    private final String TAG = "liuweixiang";


    public FallLocalReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "FallLocalReceiver.onReceive()");
        this.context = context;
        showAlertDialog();

        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        isVibrate = sharedPreferences.getBoolean("pre_key_vibrate", true);
        if(isVibrate){
            startVibrate();
        }
        startAlarm();
        startLocation();


    }


    /*
    ????????????
     */
    private void showAlertDialog() {
        countingView = new TextView(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(
                context.getApplicationContext());
        builder.setTitle("????????????");
        builder.setView(countingView);
        builder.setMessage("?????????????????????????????????????????????");
        builder.setIcon(R.drawable.ic_warning);
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                timer.cancel();
                dialog.dismiss();
                if(isVibrate){
                    stopVibrate();
                }
                stopAlarm();
                Intent startIntent = new Intent(context, FallDetectionService.class);
                context.startForegroundService(startIntent);
            }
        });

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        countDown();
        dialog.show();
        Log.d(TAG, "dialog.create()");
    }

    /*
    ?????????
     */
    private void countDown() {
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            int countTime = 10;
            @Override
            public void run() {
                if(countTime > 0){
                    countTime --;
                }
                Message msgTime = handler.obtainMessage();
                msgTime.arg1 = countTime;
                handler.sendMessage(msgTime);
            }
        };
        timer.schedule(timerTask, 50, 1000);
    }

    public Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.arg1 > 0){
                //?????????????????????
                countingView.setText("                         " + msg.arg1 + "??????????????????");
            }else{
                //???????????????????????????
                if(dialog != null){
                    dialog.dismiss();
                    if(isVibrate){
                        stopVibrate();
                    }
                    stopAlarm();
                    sendSMS(locationAddress, locationTime);
                    return;
                }
                timer.cancel();
            }
        }
    };

    /*
    ????????????
     */
    private void startVibrate(){
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 500, 100, 500};
        vibrator.vibrate(pattern, 2);
    }
    /*
    ????????????
     */
    private void stopVibrate(){
        vibrator.cancel();
    }

    /*
    ??????????????????
     */
    private void startAlarm(){
        String ringtone = sharedPreferences.getString("pre_key_alarm" , null);
        Log.d(TAG, ringtone + "");
        Uri ringtoneUri = Uri.parse(ringtone);

        mediaPlayer = MediaPlayer.create(context, ringtoneUri);
        mediaPlayer.setLooping(true);//????????????
        mediaPlayer.start();
    }
    /*
    ??????????????????
     */
    private void stopAlarm(){
        mediaPlayer.stop();
    }

    private void sendSMS(String address, String time){
        //?????????????????????
        SmsManager smsManager = SmsManager.getDefault();

        String name = sharedPreferences.getString("pre_key_name", null);
        String phoneNum = sharedPreferences.getString("pre_key_phone", null);
        String smsContent = time + name + "???" + address + "??????????????????";
        smsManager.sendTextMessage(phoneNum, null, smsContent ,null, null);
        Toast.makeText(context, "??????????????????", Toast.LENGTH_SHORT).show();

    }



    private void startLocation(){
        Log.d(TAG, "FallLocalReceiver.startLocation()");
        AMapLocationClient.updatePrivacyShow(context,true,true);
        AMapLocationClient.updatePrivacyAgree(context,true);
        try {
            locationClient = new AMapLocationClient(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //?????????????????????
        locationClientOption = new AMapLocationClientOption();
        //?????????????????????AMapLocationMode.Hight_Accuracy?????????????????????
        locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //????????????3s???????????????????????????????????????
        //??????setOnceLocationLatest(boolean b)?????????true??????????????????SDK???????????????3s?????????????????????????????????????????????????????????true???setOnceLocation(boolean b)????????????????????????true???????????????????????????false???
        locationClientOption.setOnceLocationLatest(true);

        //??????????????????
        locationClient.setLocationListener(this);
        //????????????
        locationClient.startLocation();
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //?????????????????????????????????????????????
                locationAddress = amapLocation.getAddress();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                Date date = new Date(amapLocation.getTime());
                locationTime = df.format(date);//????????????
            } else {
                //??????????????????ErrCode???????????????errInfo???????????????????????????????????????
                Log.e("AmapError","location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    }


}
