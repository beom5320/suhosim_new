package nodomain.team3point1.suhosim.service;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.Uri;

import java.util.ArrayList;
import java.util.UUID;

import nodomain.team3point1.suhosim.impl.GBDevice;
import nodomain.team3point1.suhosim.model.Alarm;
import nodomain.team3point1.suhosim.model.CalendarEventSpec;
import nodomain.team3point1.suhosim.model.CallSpec;
import nodomain.team3point1.suhosim.model.CannedMessagesSpec;
import nodomain.team3point1.suhosim.model.MusicSpec;
import nodomain.team3point1.suhosim.model.MusicStateSpec;
import nodomain.team3point1.suhosim.model.NotificationSpec;
import nodomain.team3point1.suhosim.model.WeatherSpec;

class TestDeviceSupport extends AbstractDeviceSupport {

    TestDeviceSupport() {
    }

    @Override
    public void setContext(GBDevice gbDevice, BluetoothAdapter btAdapter, Context context) {
        super.setContext(gbDevice, btAdapter, context);
    }

    @Override
    public boolean connect() {
        gbDevice.setState(GBDevice.State.INITIALIZED);
        gbDevice.sendDeviceUpdateIntent(getContext());
        return true;
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean useAutoConnect() {
        return false;
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {

    }

    @Override
    public void onDeleteNotification(int id) {

    }

    @Override
    public void onSetTime() {

    }

    @Override
    public void onSetAlarms(ArrayList<? extends Alarm> alarms) {

    }

    @Override
    public void onSetCallState(CallSpec callSpec) {

    }

    @Override
    public void onSetCannedMessages(CannedMessagesSpec cannedMessagesSpec) {

    }

    @Override
    public void onSetMusicState(MusicStateSpec stateSpec) {

    }

    @Override
    public void onSetMusicInfo(MusicSpec musicSpec) {

    }

    @Override
    public void onInstallApp(Uri uri) {

    }

    @Override
    public void onAppInfoReq() {

    }

    @Override
    public void onAppStart(UUID uuid, boolean start) {

    }

    @Override
    public void onAppDelete(UUID uuid) {

    }

    @Override
    public void onAppConfiguration(UUID appUuid, String config, Integer id) {

    }

    @Override
    public void onAppReorder(UUID[] uuids) {

    }

    @Override
    public void onFetchRecordedData(int dataTypes) {

    }

    @Override
    public void onReset(int flags) {

    }

    @Override
    public void onHeartRateTest() {

    }

    @Override
    public void onFindDevice(boolean start) {

    }

    @Override
    public void onSetConstantVibration(int intensity) {

    }

    @Override
    public void onScreenshotReq() {

    }

    @Override
    public void onEnableRealtimeSteps(boolean enable) {

    }

    @Override
    public void onEnableHeartRateSleepSupport(boolean enable) {

    }

    @Override
    public void onSetHeartRateMeasurementInterval(int seconds) {

    }

    @Override
    public void onAddCalendarEvent(CalendarEventSpec calendarEventSpec) {

    }

    @Override
    public void onDeleteCalendarEvent(byte type, long id) {

    }

    @Override
    public void onEnableRealtimeHeartRateMeasurement(boolean enable) {

    }

    @Override
    public void onSendConfiguration(String config) {

    }

    @Override
    public void onReadConfiguration(String config) {

    }

    @Override
    public void onTestNewFunction() {

    }

    @Override
    public void onSendWeather(WeatherSpec weatherSpec) {

    }

    @Override
    public void onSetFmFrequency(float frequency) {

    }

    @Override
    public void onSetLedColor(int color) {

    }
}
