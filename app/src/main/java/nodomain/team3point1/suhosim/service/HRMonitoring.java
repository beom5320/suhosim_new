package nodomain.team3point1.suhosim.service;


import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.sql.Timestamp;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import nodomain.team3point1.suhosim.GetLocation;
import nodomain.team3point1.suhosim.GpsTracker;
import nodomain.team3point1.suhosim.activities.HeartRateUtils;
import nodomain.team3point1.suhosim.activities.charts.LiveActivityFragment;
import nodomain.team3point1.suhosim.database.DBHandler;
import nodomain.team3point1.suhosim.impl.GBDevice;
import nodomain.team3point1.suhosim.model.ActivitySample;
import nodomain.team3point1.suhosim.model.DeviceService;

import static java.lang.Boolean.TRUE;
import static nodomain.team3point1.suhosim.GBApplication.getContext;

public class HRMonitoring extends Service {
    private int mHeartRate;
    private int WarningCount = 0;
    private int WarningCount2 = 0;
    private int mMaxHeartRate = 0;
    private double UserMaxHeartRate =  51; //(int)206.9 - (0.67 * 74); //157
    private int UserMinHeartRate = 50;

    private LiveActivityFragment liveActivity;
    private GpsTracker gpsTracker;
    private GetLocation getLocation;

    private Timer timer;
    private int counter;


    public void HRMonitoring() {
        /*
        liveActivity = new LiveActivityFragment();

        Intent i = new Intent(this, LiveActivityFragment.class);
        PendingIntent p = PendingIntent.getActivity(this, 0, i, 0);
        try {
            p.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
        */
/*
        Intent intent = new Intent("act=nodomain.team3point1.suhosim.devices.action.realtime_samples (has extras)");
        Log.d("@@@", "intent: "+intent);
        ActivitySample sample = (ActivitySample) intent.getSerializableExtra(DeviceService.EXTRA_REALTIME_SAMPLE);
        Log.d("@@@", "sample: "+sample);
        addSample(sample);
*/
        //mHeartRate = liveActivity.getCurrentHeartRate();
/*
        Log.d("@@@", "mHeartRate:"+mHeartRate);
        if (mHeartRate > UserMaxHeartRate) {
            WarningCount += 1;
            if (WarningCount == 1) {
                liveActivity = new LiveActivityFragment();
                gpsTracker = new GpsTracker(getContext());

                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                getLocation = new GetLocation();
                String address = getLocation.getCurrentAddress(latitude, longitude);

                SmsManager sm = SmsManager.getDefault();
                String messageText1 = "[수호심 앱에서 긴급 발신된 문자입니다]\n피보호자 OOO님의 심박수가 이상 증세를 보이고 있습니다. 심박수:";//+mHeartRate;
                String messageText2 = "[현재 GPS 위치]\n위도:" + latitude + ", 경도:" + longitude;
                String messageText3 = "[현재 GPS 주소]\n" + address;

                //String messageText2 = "실제상황입니다. 양병일 환자의 심박수가 심정지 전조증상을 보이고 있습니다. ";
                //String messageText3 = "집주소 : 경기도 안양시 만안구 박달2동 XX아파트 XXX동 XXX호";
                //String messageText4 = "나이 : (만)24세 / 수술이력 : [없음] / 혈액형 : [RH+ 호감형]";
                //sm.sendTextMessage("01096631750", null, messageText1, null, null);
                //sm.sendTextMessage("01096631750", null, messageText2, null, null);
                sm.sendTextMessage("01096631750", null, messageText3, null, null);
                //Toast.makeText(getContext(), "119에 문자메시지가 전송되었습니다. ", Toast.LENGTH_SHORT).show();
            }
        }
 */
        return;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                HRMonitoring();
            }
        };

        Timer timer = new Timer();
        timer.schedule(tt, 0, 10000); // 설정한 ms마다 TimerTask tt 실행
        Log.d("@@@", "서비스 실행!");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("@@@","서비스 뒤졌다!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected List<ActivitySample> getSamples(DBHandler db, GBDevice device, int tsFrom, int tsTo) {
        throw new UnsupportedOperationException("no db access supported for live activity");
    }
}
