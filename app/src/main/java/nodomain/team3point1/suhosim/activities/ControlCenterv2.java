/*  Copyright (C) 2016-2019 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, Johannes Tysiak, Taavi Eomäe

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
package nodomain.team3point1.suhosim.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;





import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import de.cketti.library.changelog.ChangeLog;
import nodomain.team3point1.suhosim.GBApplication;
import nodomain.team3point1.suhosim.R;
import nodomain.team3point1.suhosim.activities.charts.AbstractChartFragment;
import nodomain.team3point1.suhosim.activities.charts.LiveActivityFragment;
import nodomain.team3point1.suhosim.adapter.GBDeviceAdapterv2;
import nodomain.team3point1.suhosim.devices.DeviceManager;
import nodomain.team3point1.suhosim.impl.GBDevice;
import nodomain.team3point1.suhosim.util.AndroidUtils;
import nodomain.team3point1.suhosim.util.GB;
import nodomain.team3point1.suhosim.util.Prefs;

import static nodomain.team3point1.suhosim.GBApplication.getContext;

//TODO: extend AbstractGBActivity, but it requires actionbar that is not available
public class ControlCenterv2 extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GBActivity {

    //needed for KK compatibility
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private DeviceManager deviceManager;

    private GBDeviceAdapterv2 mGBDeviceAdapter;
    private RecyclerView deviceListView;
    private FloatingActionButton fab;

    private boolean isLanguageInvalid = false;

    public static final int MENU_REFRESH_CODE=1;

    private static PhoneStateListener fakeStateListener;

    private SharedPreferences user;


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (Objects.requireNonNull(action)) {
                case GBApplication.ACTION_LANGUAGE_CHANGE:
                    setLanguage(GBApplication.getLanguage(), true);
                    break;
                case GBApplication.ACTION_QUIT:
                    finish();
                    break;
                case DeviceManager.ACTION_DEVICES_CHANGED:
                    refreshPairedDevices();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AbstractGBActivity.init(this, AbstractGBActivity.NO_ACTIONBAR);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controlcenterv2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //메뉴에 대한 기본설정
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.controlcenter_navigation_drawer_open, R.string.controlcenter_navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        //end of material design boilerplate
        deviceManager = ((GBApplication) getApplication()).getDeviceManager();

        deviceListView = findViewById(R.id.deviceListView);
        deviceListView.setHasFixedSize(true);
        deviceListView.setLayoutManager(new LinearLayoutManager(this));

        List<GBDevice> deviceList = deviceManager.getDevices();
        mGBDeviceAdapter = new GBDeviceAdapterv2(this, deviceList);

        deviceListView.setAdapter(this.mGBDeviceAdapter);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchDiscoveryActivity();
            }
        });

        showFabIfNeccessary();

        /* uncomment to enable fixed-swipe to reveal more actions

        ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT , ItemTouchHelper.RIGHT) {
            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if(dX>50)
                    dX = 50;
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                GB.toast(getBaseContext(), "onMove", Toast.LENGTH_LONG, GB.ERROR);

                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                GB.toast(getBaseContext(), "onSwiped", Toast.LENGTH_LONG, GB.ERROR);

            }

            @Override
            public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                        RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                        int actionState, boolean isCurrentlyActive) {
            }
        });

        swipeToDismissTouchHelper.attachToRecyclerView(deviceListView);
        */

        registerForContextMenu(deviceListView);

        IntentFilter filterLocal = new IntentFilter();
        filterLocal.addAction(GBApplication.ACTION_LANGUAGE_CHANGE);
        filterLocal.addAction(GBApplication.ACTION_QUIT);
        filterLocal.addAction(DeviceManager.ACTION_DEVICES_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filterLocal);

        refreshPairedDevices();

        /*
         * Ask for permission to intercept notifications on first run.
         */

        //처음화면 설정
        Prefs prefs = GBApplication.getPrefs();
        if (prefs.getBoolean("firstrun", true)) {

            Intent enableIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            Intent newIntent = new Intent(ControlCenterv2.this, AboutYours.class);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(enableIntent);
            startActivity(newIntent);


        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }

        ChangeLog cl = createChangeLog();
        if (cl.isFirstRun()) {
            cl.getLogDialog().show();
        }

        GBApplication.deviceService().start();

        if (GB.isBluetoothEnabled() && deviceList.isEmpty() && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startActivity(new Intent(this, DiscoveryActivity.class));
        } else {
            GBApplication.deviceService().requestDeviceInfo();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLanguageInvalid) {
            isLanguageInvalid = false;
            recreate();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterForContextMenu(deviceListView);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MENU_REFRESH_CODE) {
            showFabIfNeccessary();
        }
    }


    ///////////////////////메뉴 선택 시작////////////////////////////////
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        switch (item.getItemId()) {
            case R.id.action_user:
                Intent userIntent = new Intent(this, AboutYoursSave.class);
                startActivityForResult(userIntent, MENU_REFRESH_CODE);
                return true;

            /*case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, MENU_REFRESH_CODE);
                return true;

            case R.id.device_action_discover:
                launchDiscoveryActivity();
                return true;*/
            case R.id.action_stop:
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(ControlCenterv2.this);
                alert_confirm.setMessage("착용 해제 하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Timer timer = new Timer();
                                timer.schedule( new TimerTask()
                                                {
                                                    public void run()
                                                    {
                                                        user = getContext().getSharedPreferences("User", MODE_PRIVATE);
                                                        String numberA = user.getString("phone", null);
                                                        SmsManager sm = SmsManager.getDefault();
                                                        String messageText= "사용자가 미착용한 지 5초가 경과하였습니다.";
                                                        sm.sendTextMessage(numberA, null, messageText, null, null);
                                                    }
                                                }
                                        , 5000);
                            }
                        }).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'No'
                                return;
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();

                return true;


            case R.id.action_quit:
                GBApplication.quit();
                return true;
        }
        return true;
    }
    ///////////////////////메뉴 선택 끝////////////////////////////////



    private ChangeLog createChangeLog() {
        String css = ChangeLog.DEFAULT_CSS;
        css += "body { "
                + "color: " + AndroidUtils.getTextColorHex(getBaseContext()) + "; "
                + "background-color: " + AndroidUtils.getBackgroundColorHex(getBaseContext()) + ";" +
                "}";
        return new ChangeLog(this, css);
    }

    private void launchDiscoveryActivity() {
        startActivity(new Intent(this, DiscoveryActivity.class));
    }

    private void refreshPairedDevices() {
        mGBDeviceAdapter.notifyDataSetChanged();
    }

    private void showFabIfNeccessary() {
        if (GBApplication.getPrefs().getBoolean("display_add_device_fab", true)) {
            fab.show();
        } else {
            if (deviceListView.getChildCount() < 1) {
                fab.show();
            } else {
                fab.hide();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermissions() {
        List<String> wantedPermissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.BLUETOOTH);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED)
        //    wantedPermissions.add(Manifest.permission.READ_CONTACTS);
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED)
        //    wantedPermissions.add(Manifest.permission.CALL_PHONE);
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED)
        //    wantedPermissions.add(Manifest.permission.READ_CALL_LOG);
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED)
        //    wantedPermissions.add(Manifest.permission.READ_PHONE_STATE);
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_DENIED)
        //    wantedPermissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.RECEIVE_SMS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_SMS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.SEND_SMS);
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        //    wantedPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        //if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_DENIED)
        //    wantedPermissions.add(Manifest.permission.READ_CALENDAR);
        // ^@^ 앱 실행 시 위치 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        try {
        //    if (ContextCompat.checkSelfPermission(this, Manifest.permission.MEDIA_CONTENT_CONTROL) == PackageManager.PERMISSION_DENIED)
        //        wantedPermissions.add(Manifest.permission.MEDIA_CONTENT_CONTROL);
        } catch (Exception ignored){
        }

        if (!wantedPermissions.isEmpty())
            ActivityCompat.requestPermissions(this, wantedPermissions.toArray(new String[0]), 0);

        // HACK: On Lineage we have to do this so that the permission dialog pops up
        if (fakeStateListener == null) {
            fakeStateListener = new PhoneStateListener();
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            telephonyManager.listen(fakeStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            telephonyManager.listen(fakeStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    public void setLanguage(Locale language, boolean invalidateLanguage) {
        if (invalidateLanguage) {
            isLanguageInvalid = true;
        }
        AndroidUtils.setLanguage(this, language);
    }



}

