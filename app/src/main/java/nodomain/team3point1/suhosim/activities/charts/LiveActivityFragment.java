/*  Copyright (C) 2015-2019 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, Dikay900, Pavel, Pavel Elagin

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nodomain.team3point1.suhosim.activities.charts;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.SmsManager;
import android.app.PendingIntent;
import android.os.Vibrator;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import nodomain.team3point1.suhosim.GBApplication;
import nodomain.team3point1.suhosim.GetLocation;
import nodomain.team3point1.suhosim.GpsTracker;
import nodomain.team3point1.suhosim.R;
import nodomain.team3point1.suhosim.activities.HeartRateUtils;
import nodomain.team3point1.suhosim.database.DBHandler;
import nodomain.team3point1.suhosim.impl.GBDevice;
import nodomain.team3point1.suhosim.model.ActivitySample;
import nodomain.team3point1.suhosim.model.ActivityUser;
import nodomain.team3point1.suhosim.model.DeviceService;
import nodomain.team3point1.suhosim.util.GB;

import static android.content.Context.MODE_PRIVATE;

public class LiveActivityFragment extends AbstractChartFragment {
    private static final Logger LOG = LoggerFactory.getLogger(LiveActivityFragment.class);
    private static final int MAX_STEPS_PER_MINUTE = 300;
    private static final int MIN_STEPS_PER_MINUTE = 60;
    private static final int RESET_COUNT = 10; // reset the max steps per minute value every 10s

    private BarEntry totalStepsEntry;
    private BarEntry stepsPerMinuteEntry;
    private BarDataSet mStepsPerMinuteData;
    private BarDataSet mTotalStepsData;
    private LineDataSet mHistorySet;
    private BarLineChartBase mStepsPerMinuteHistoryChart;
    private CustomBarChart mStepsPerMinuteCurrentChart;
    private CustomBarChart mTotalStepsChart;
    private TextView mMaxHeartRateView;

    private final Steps mSteps = new Steps();
    private ScheduledExecutorService pulseScheduler;
    private int maxStepsResetCounter;
    private LineDataSet mHeartRateSet;
    private int mHeartRate;
    private int WarningCount = 0;
    private int WarningCount2 = 0;
    private int mMaxHeartRate = 0;
    private double UserMaxHeartRate =  60; //(int)206.9 - (0.67 * 74); //157
    private int UserMinHeartRate = 50;
    private TimestampTranslation tsTranslation;
    private int NoSBCount=0;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private GpsTracker gpsTracker;
    private GetLocation getLocation;

    private class Steps {
        private int steps;
        private int lastTimestamp;
        private int currentStepsPerMinute;
        private int maxStepsPerMinute;
        private int lastStepsPerMinute;

        public int getStepsPerMinute(boolean reset) {
            lastStepsPerMinute = currentStepsPerMinute;
            int result = currentStepsPerMinute;
            if (reset) {
                currentStepsPerMinute = 0;
            }
            return result;
        }

        public int getTotalSteps() {
            return steps;
        }

        public int getMaxStepsPerMinute() {
            return maxStepsPerMinute;
        }

        public void updateCurrentSteps(int stepsDelta, int timestamp) {
            try {
                if (steps == 0) {
                    steps += stepsDelta;
                    lastTimestamp = timestamp;
                    return;
                }

                int timeDelta = timestamp - lastTimestamp;
                currentStepsPerMinute = calculateStepsPerMinute(stepsDelta, timeDelta);
                if (currentStepsPerMinute > maxStepsPerMinute) {
                    maxStepsPerMinute = currentStepsPerMinute;
                    maxStepsResetCounter = 0;
                }
                steps += stepsDelta;
                lastTimestamp = timestamp;
            } catch (Exception ex) {
                GB.toast(LiveActivityFragment.this.getContext(), ex.getMessage(), Toast.LENGTH_SHORT, GB.ERROR, ex);
            }
        }

        private int calculateStepsPerMinute(int stepsDelta, int seconds) {
            if (stepsDelta == 0) {
                return 0; // not walking or not enough data per mills?
            }
            if (seconds <= 0) {
                throw new IllegalArgumentException("delta in seconds is <= 0 -- time change?");
            }

            int oneMinute = 60;
            float factor = oneMinute / seconds;
            int result = (int) (stepsDelta * factor);
            if (result > MAX_STEPS_PER_MINUTE) {
                // ignore, return previous value instead
                result = lastStepsPerMinute;
            }
            return result;
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case DeviceService.ACTION_REALTIME_SAMPLES: {
                    ActivitySample sample = (ActivitySample) intent.getSerializableExtra(DeviceService.EXTRA_REALTIME_SAMPLE);
                    addSample(sample);
                    break;
                }
            }
        }
    };

    private void addSample(ActivitySample sample) {
        int heartRate = sample.getHeartRate();
        int timestamp = tsTranslation.shorten(sample.getTimestamp());
        if (HeartRateUtils.getInstance().isValidHeartRateValue(heartRate)) {
            setCurrentHeartRate(heartRate, timestamp);
        }
        int steps = sample.getSteps();
        if (steps > 0) {
            addEntries(steps, timestamp);
        }

        if(sample.getHeartRate() <= 0)
        {
            NoSBCount += 1;
            Toast.makeText(getContext(), NoSBCount+"카운트1 "+mHeartRate, Toast.LENGTH_SHORT).show();
        }
        else {
            NoSBCount = 0;
            Toast.makeText(getContext(), NoSBCount+"카운트2 "+mHeartRate + heartRate, Toast.LENGTH_SHORT).show();
        }

    }

    private int translateTimestampFrom(Intent intent) {
        return translateTimestamp(intent.getLongExtra(DeviceService.EXTRA_TIMESTAMP, System.currentTimeMillis()));
    }

    private int translateTimestamp(long tsMillis) {
        int timestamp = (int) (tsMillis / 1000); // translate to seconds
        return tsTranslation.shorten(timestamp); // and shorten
    }
    /*
        private void SendSMS(String phonenumber, String message) {

            SmsManager smsManager = SmsManager.getDefault();
            String sendTo = phonenumber;
            String myMessage = message;
            smsManager.sendTextMessage(sendTo, null, myMessage, null, null);
            Toast.makeText(SMSSender.this, "전송되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
        */
    /*여기가 심박수 */
    /*
       1.독거노인의 평균연령 찾기

       2. 독거노인 평균연령대 최대심박수 찾기
       Jackson et al 최대 심박수 측정 공식
       HRmax = 206.9 - (0.67 x 나이[74] = 157.32)
       HRmin = 40
       3. 생년월일 default값을 평균연령으로 설정
       4. if문사용 최대 심박수 넘겼을 경우 토스트 출력(임시 나중에는 문자 발송)
       ---------------------------
       심박수 측정 불가 상태일 경우
       1. 진동 or 핸드폰으로 알려야 함
       2. ??..??...??????????????!!?
     */

    private SharedPreferences user;
    private Context mContext;
    private void setCurrentHeartRate(int heartRate, int timestamp) {


        user = getContext().getSharedPreferences("User", MODE_PRIVATE);
        String nameL = user.getString("name", null);

        String birthL = user.getString("birth", null);

        String addressL = user.getString("address", null);

        String genderL = user.getString("gender", null);

        String bloodL = user.getString("blood", null);

        String medicalL = user.getString("medical", null);

        String drugL = user.getString("drug", null);

        String surgeryL = user.getString("surgery", null);

        String numberL = user.getString("phone", null);




        addHistoryDataSet(true);
        mHeartRate = heartRate;
        if (mMaxHeartRate < mHeartRate) {
            mMaxHeartRate = mHeartRate;
        }
        if (mHeartRate > UserMaxHeartRate)
        {
            WarningCount += 1;

            // GPS 위치 확인 및 주소 변환
            gpsTracker = new GpsTracker(getContext());
            getLocation = new GetLocation();
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            String address = getLocation.getCurrentAddress(latitude, longitude);

            if (WarningCount == 1) // 최초 발생 시 보호자에게 문자 전송
            {
                SmsManager sm = SmsManager.getDefault();
                String messageText1 = "[수호심 앱에서 긴급 발신된 문자입니다]\n"+nameL+"님이 심정지 전조증상을 보이고 있습니다.\n현재 심박수: "+mHeartRate;
                String messageText2 = "[현재 GPS 위치]\n위도: "+latitude+", 경도: "+longitude;
                String messageText3 = "[현재 GPS 주소]\n"+address;
                sm.sendTextMessage(numberL, null, messageText1, null, null);
                sm.sendTextMessage(numberL, null, messageText2, null, null);
                sm.sendTextMessage(numberL, null, messageText3, null, null);
            }

            else if (WarningCount == 10) // 10회 발생 시 119에 문자 전송
            {
                SmsManager sm = SmsManager.getDefault();
                String messageText1 = "[심박수 모니터링 앱에서 긴급 발신된 문자입니다.]";
                String messageText2 = "실제 상황입니다. "+nameL+"님이 심정지 전조증상을 보이고 있습니다.\n현재 심박수: "+mHeartRate;
                String messageText3 = "자택주소: "+addressL;
                String messageText4 = "성별: "+genderL+"\n생년월일: "+birthL+"\n혈액형: "+bloodL+"\n수술이력: "+surgeryL;
                String messageText5 = "병력사항: "+medicalL+"\n보호자 연락처: "+numberL;
                String messageText6 = "[현재 GPS 위치]\n위도: "+latitude+"\n경도: "+longitude;
                String messageText7 = "[현재 GPS 주소]\n"+address;

                sm.sendTextMessage("01096631750", null, messageText1, null, null);
                sm.sendTextMessage("01096631750", null, messageText2, null, null);
                sm.sendTextMessage("01096631750", null, messageText3, null, null);
                sm.sendTextMessage("01096631750", null, messageText4, null, null);
                sm.sendTextMessage("01096631750", null, messageText5, null, null);
                sm.sendTextMessage("01096631750", null, messageText6, null, null);
                sm.sendTextMessage("01096631750", null, messageText7, null, null);
            }

        }
        /*
        if (mHeartRate > 10 && mHeartRate < UserMinHeartRate)
        {
            WarningCount2 += 1;
            if (WarningCount2 == 10)
            {
                SmsManager sm = SmsManager.getDefault();
                String messageText = "(경고!) 피보호자의 심박수가 최저 심박수 밑으로 내려갔습니다.";
                sm.sendTextMessage("01040360567", null, messageText+mHeartRate, null, null);
                Toast.makeText(getContext(), "문자메시지가 전송되었습니다. ", Toast.LENGTH_SHORT).show();
                WarningCount2 = 0;
            }
        }
         */
        if (WarningCount > 10 && UserMinHeartRate > mHeartRate) //count가 5이상이고 최소심박수(50)가 현재 심박수보다 높을 때
        {
            /*
            String Name = "";
            String Add = "";
            String Phone="";

            String messageText = "(경고!) 피보호자의 심박수가 이상 증세를 보이고 있습니다. 상태를 확인해주세요";
            // 119 전용
            String messageText_119_1 = "실제상황입니다. [세글자]환자의 심박수가 심정지 전조증상을 보이고 있습니다. "
            String messageText_119_2 = "집주소 : []"
            String messageText_119_3 = "나이 : (만)[75]세 / 수술이력 : [] / 혈액형 : []"
            SmsManager sm = SmsManager.getDefault();
            sm.sendTextMessage(a, null, messageText, null, null);
            sm.sendTextMessage("01040360567", null, messageText, null, null);
            Toast.makeText(getContext(), "문자메시지가 전송되었습니다.", Toast.LENGTH_SHORT).show();
            */
            WarningCount = 0;
        }

        mMaxHeartRateView.setText(getContext().getString(R.string.live_activity_max_heart_rate, heartRate, mMaxHeartRate));
    }

    /* 심박수 측정 불가능 할 때 -1로 저장*/
    private int getCurrentHeartRate() {
        int result = mHeartRate;
        mHeartRate = -1;

        return result;
    }

    private void addEntries(int steps, int timestamp) {
        mSteps.updateCurrentSteps(steps, timestamp);
        if (++maxStepsResetCounter > RESET_COUNT) {
            maxStepsResetCounter = 0;
            mSteps.maxStepsPerMinute = 0;
        }
        // Or: count down the steps until goal reached? And then flash GOAL REACHED -> Set stretch goal
        LOG.info("Steps: " + steps + ", total: " + mSteps.getTotalSteps() + ", current: " + mSteps.getStepsPerMinute(false));

//        addEntries();
    }

    private void addEntries(int timestamp) {
        mTotalStepsChart.setSingleEntryYValue(mSteps.getTotalSteps());
        YAxis stepsPerMinuteCurrentYAxis = mStepsPerMinuteCurrentChart.getAxisLeft();
        int maxStepsPerMinute = mSteps.getMaxStepsPerMinute();
//        int extraRoom = maxStepsPerMinute/5;
//        buggy in MPAndroidChart? Disable.
//        stepsPerMinuteCurrentYAxis.setAxisMaxValue(Math.max(MIN_STEPS_PER_MINUTE, maxStepsPerMinute + extraRoom));
        LimitLine target = new LimitLine(maxStepsPerMinute);
        stepsPerMinuteCurrentYAxis.removeAllLimitLines();
        stepsPerMinuteCurrentYAxis.addLimitLine(target);

        int stepsPerMinute = mSteps.getStepsPerMinute(true);
        mStepsPerMinuteCurrentChart.setSingleEntryYValue(stepsPerMinute);

        if (!addHistoryDataSet(false)) {
            return;
        }

        ChartData data = mStepsPerMinuteHistoryChart.getData();
        if (stepsPerMinute < 0) {
            stepsPerMinute = 0;
        }
        mHistorySet.addEntry(new Entry(timestamp, stepsPerMinute));
        int hr = getCurrentHeartRate();
        if (hr > HeartRateUtils.getInstance().getMinHeartRate()) {
            mHeartRateSet.addEntry(new Entry(timestamp, hr));
        }
    }

    private boolean addHistoryDataSet(boolean force) {
        if (mStepsPerMinuteHistoryChart.getData() == null) {
            // ignore the first default value to keep the "no-data-description" visible
            if (force || mSteps.getTotalSteps() > 0) {
                LineData data = new LineData();
                data.addDataSet(mHistorySet);
                data.addDataSet(mHeartRateSet);
                mStepsPerMinuteHistoryChart.setData(data);
                return true;
            }
            return false;
        }
        return true;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        IntentFilter filterLocal = new IntentFilter();
        filterLocal.addAction(DeviceService.ACTION_REALTIME_SAMPLES);
        tsTranslation = new TimestampTranslation();

        View rootView = inflater.inflate(R.layout.fragment_live_activity, container, false);

        mStepsPerMinuteCurrentChart = rootView.findViewById(R.id.livechart_steps_per_minute_current);
        mTotalStepsChart = rootView.findViewById(R.id.livechart_steps_total);
        mStepsPerMinuteHistoryChart = rootView.findViewById(R.id.livechart_steps_per_minute_history);

        totalStepsEntry = new BarEntry(1, 0);
        stepsPerMinuteEntry = new BarEntry(1, 0);

        mStepsPerMinuteData = setupCurrentChart(mStepsPerMinuteCurrentChart, stepsPerMinuteEntry, getString(R.string.live_activity_current_steps_per_minute));
        mStepsPerMinuteData.setDrawValues(true);
        mStepsPerMinuteData.setValueTextColor(DESCRIPTION_COLOR);
        mTotalStepsData = setupTotalStepsChart(mTotalStepsChart, totalStepsEntry, getString(R.string.live_activity_total_steps));
        mTotalStepsData.setDrawValues(true);
        mTotalStepsData.setValueTextColor(DESCRIPTION_COLOR);
        setupHistoryChart(mStepsPerMinuteHistoryChart);
        mMaxHeartRateView = rootView.findViewById(R.id.livechart_max_heart_rate);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filterLocal);

        return rootView;
    }

    @Override
    public void onPause() {
        enableRealtimeTracking(false);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        enableRealtimeTracking(true);
    }

    private ScheduledExecutorService startActivityPulse() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                FragmentActivity activity = LiveActivityFragment.this.getActivity();
                if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pulse();
                        }
                    });
                }
            }
        }, 0, getPulseIntervalMillis(), TimeUnit.MILLISECONDS);
        return service;
    }

    private void stopActivityPulse() {
        if (pulseScheduler != null) {
            pulseScheduler.shutdownNow();
            pulseScheduler = null;
        }
    }

    /**
     * Called in the UI thread.
     */
    private void pulse() {
        addEntries(translateTimestamp(System.currentTimeMillis()));

        LineData historyData = (LineData) mStepsPerMinuteHistoryChart.getData();
        if (historyData == null) {
            return;
        }

        historyData.notifyDataChanged();
        mTotalStepsData.notifyDataSetChanged();
        mStepsPerMinuteData.notifyDataSetChanged();
        mStepsPerMinuteHistoryChart.notifyDataSetChanged();

        renderCharts();

        // have to enable it again and again to keep it measureing
        GBApplication.deviceService().onEnableRealtimeHeartRateMeasurement(true);
    }

    private int getPulseIntervalMillis() {
        return 1000;
    }

    @Override
    protected void onMadeVisibleInActivity() {
        super.onMadeVisibleInActivity();
        enableRealtimeTracking(true);
    }

    private void enableRealtimeTracking(boolean enable) {
        if (enable && pulseScheduler != null) {
            // already running
            return;
        }

        GBApplication.deviceService().onEnableRealtimeSteps(enable);
        GBApplication.deviceService().onEnableRealtimeHeartRateMeasurement(enable); // 이거 안켜주면 심박수 안들어오네
        if (enable) {
            if (getActivity() != null) {
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            pulseScheduler = startActivityPulse();
        } else {
            stopActivityPulse();
            if (getActivity() != null) {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    @Override
    protected void onMadeInvisibleInActivity() {
        enableRealtimeTracking(false);
        super.onMadeInvisibleInActivity();
    }

    @Override
    public void onDestroyView() {
        onMadeInvisibleInActivity();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        super.onDestroyView();
    }

    private BarDataSet setupCurrentChart(CustomBarChart chart, BarEntry entry, String title) {
        mStepsPerMinuteCurrentChart.getAxisLeft().setAxisMaximum(MAX_STEPS_PER_MINUTE);
        return setupCommonChart(chart, entry, title);
    }

    private BarDataSet setupCommonChart(CustomBarChart chart, BarEntry entry, String title) {
        chart.setSinglAnimationEntry(entry);

//        chart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        chart.getXAxis().setDrawLabels(false);
        chart.getXAxis().setEnabled(false);
        chart.getXAxis().setTextColor(CHART_TEXT_COLOR);
        chart.getAxisLeft().setTextColor(CHART_TEXT_COLOR);

        chart.setBackgroundColor(BACKGROUND_COLOR);
        chart.getDescription().setTextColor(DESCRIPTION_COLOR);
        chart.getDescription().setText(title);
//        chart.setNoDataTextDescription("");
        chart.setNoDataText("");
        chart.getAxisRight().setEnabled(false);

        List<BarEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        entries.add(entry);
        colors.add(akActivity.color);
        colors.add(akActivity.color);
        colors.add(akActivity.color);
//        //we don't want labels
//        xLabels.add("");
//        xLabels.add("");
//        xLabels.add("");

        BarDataSet set = new BarDataSet(entries, "");
        set.setDrawValues(false);
        set.setColors(colors);
        BarData data = new BarData(set);
//        data.setGroupSpace(0);
        chart.setData(data);

        chart.getLegend().setEnabled(false);

        return set;
    }

    private BarDataSet setupTotalStepsChart(CustomBarChart chart, BarEntry entry, String label) {
        mTotalStepsChart.getAxisLeft().addLimitLine(new LimitLine(GBApplication.getPrefs().getInt(ActivityUser.PREF_USER_STEPS_GOAL, ActivityUser.defaultUserStepsGoal), "ss")); // TODO: use daily goal - already reached steps
        mTotalStepsChart.getAxisLeft().setAxisMinimum(0);
        mTotalStepsChart.setAutoScaleMinMaxEnabled(true);
        return setupCommonChart(chart, entry, label); // at the moment, these look the same
    }

    private void setupHistoryChart(BarLineChartBase chart) {
        configureBarLineChartDefaults(chart);

        chart.setTouchEnabled(false); // no zooming or anything, because it's updated all the time
        chart.setBackgroundColor(BACKGROUND_COLOR);
        chart.getDescription().setTextColor(DESCRIPTION_COLOR);
        chart.getDescription().setText(getString(R.string.live_activity_steps_per_minute_history));
        chart.setNoDataText(getString(R.string.live_activity_start_your_activity));
        chart.getLegend().setEnabled(false);
        Paint infoPaint = chart.getPaint(Chart.PAINT_INFO);
        infoPaint.setTextSize(Utils.convertDpToPixel(20f));
        infoPaint.setFakeBoldText(true);
        chart.setPaint(infoPaint, Chart.PAINT_INFO);

        XAxis x = chart.getXAxis();
        x.setDrawLabels(true);
        x.setDrawGridLines(false);
        x.setEnabled(true);
        x.setTextColor(CHART_TEXT_COLOR);
        x.setValueFormatter(new SampleXLabelFormatter(tsTranslation));
        x.setDrawLimitLinesBehindData(true);

        YAxis y = chart.getAxisLeft();
        y.setDrawGridLines(false);
        y.setDrawTopYLabelEntry(false);
        y.setTextColor(CHART_TEXT_COLOR);
        y.setEnabled(true);
        y.setAxisMinimum(0);

        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setDrawGridLines(false);
        yAxisRight.setEnabled(true);
        yAxisRight.setDrawLabels(true);
        yAxisRight.setDrawTopYLabelEntry(false);
        yAxisRight.setTextColor(CHART_TEXT_COLOR);

        mHistorySet = new LineDataSet(new ArrayList<Entry>(), getString(R.string.live_activity_steps_history));
        mHistorySet.setAxisDependency(YAxis.AxisDependency.LEFT);
        mHistorySet.setColor(akActivity.color);
        mHistorySet.setDrawCircles(false);
        mHistorySet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        mHistorySet.setDrawFilled(true);
        mHistorySet.setDrawValues(false);

        mHeartRateSet = createHeartrateSet(new ArrayList<Entry>(), getString(R.string.live_activity_heart_rate));
        mHeartRateSet.setDrawValues(false);
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.liveactivity_live_activity);
    }

    @Override
    protected void showDateBar(boolean show) {
        // never show the data bar
        super.showDateBar(false);
    }

    @Override
    protected void refresh() {
        // do nothing, we don't have any db interaction
    }

    @Override
    protected ChartsData refreshInBackground(ChartsHost chartsHost, DBHandler db, GBDevice device) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void updateChartsnUIThread(ChartsData chartsData) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void renderCharts() {
        mStepsPerMinuteCurrentChart.animateY(150);
        mTotalStepsChart.animateY(150);
        mStepsPerMinuteHistoryChart.invalidate();
    }

    @Override
    protected List<ActivitySample> getSamples(DBHandler db, GBDevice device, int tsFrom, int tsTo) {
        throw new UnsupportedOperationException("no db access supported for live activity");
    }

    @Override
    protected void setupLegend(Chart chart) {
        // no legend
    }
}