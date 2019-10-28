/*  Copyright (C) 2018-2019 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti

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
package nodomain.team3point1.suhosim.activities.devicesettings;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.team3point1.suhosim.R;
import nodomain.team3point1.suhosim.activities.AbstractGBActivity;
import nodomain.team3point1.suhosim.devices.DeviceCoordinator;
import nodomain.team3point1.suhosim.impl.GBDevice;
import nodomain.team3point1.suhosim.util.DeviceHelper;


public class DeviceSettingsActivity extends AbstractGBActivity implements
        PreferenceFragmentCompat.OnPreferenceStartScreenCallback {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceSettingsActivity.class);

    GBDevice device;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        device = getIntent().getParcelableExtra(GBDevice.EXTRA_DEVICE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);
        if (savedInstanceState == null) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(DeviceSpecificSettingsFragment.FRAGMENT_TAG);
            if (fragment == null) {
                DeviceCoordinator coordinator = DeviceHelper.getInstance().getCoordinator(device);
                fragment = DeviceSpecificSettingsFragment.newInstance(device.getAddress(), coordinator.getSupportedDeviceSpecificSettings(device));
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, fragment, DeviceSpecificSettingsFragment.FRAGMENT_TAG)
                    .commit();

        }
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller, PreferenceScreen preferenceScreen) {
        DeviceCoordinator coordinator = DeviceHelper.getInstance().getCoordinator(device);

        PreferenceFragmentCompat fragment = DeviceSpecificSettingsFragment.newInstance(device.getAddress(), coordinator.getSupportedDeviceSpecificSettings(device));
        Bundle args = fragment.getArguments();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
        fragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, fragment, preferenceScreen.getKey())
                .addToBackStack(preferenceScreen.getKey())
                .commit();
        return true;
    }
}