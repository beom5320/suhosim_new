/*  Copyright (C) 2015-2019 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, José Rebelo, Matthieu Baerts

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
package nodomain.team3point1.suhosim.devices;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

import androidx.annotation.NonNull;
import de.greenrobot.dao.query.QueryBuilder;
import nodomain.team3point1.suhosim.GBApplication;
import nodomain.team3point1.suhosim.GBException;
import nodomain.team3point1.suhosim.database.DBHandler;
import nodomain.team3point1.suhosim.database.DBHelper;
import nodomain.team3point1.suhosim.devices.miband.MiBandConst;
import nodomain.team3point1.suhosim.entities.DaoSession;
import nodomain.team3point1.suhosim.entities.Device;
import nodomain.team3point1.suhosim.entities.DeviceAttributesDao;
import nodomain.team3point1.suhosim.impl.GBDevice;
import nodomain.team3point1.suhosim.impl.GBDeviceCandidate;
import nodomain.team3point1.suhosim.util.Prefs;
import static nodomain.team3point1.suhosim.GBApplication.getPrefs;

public abstract class AbstractDeviceCoordinator implements DeviceCoordinator {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDeviceCoordinator.class);

    @Override
    public final boolean supports(GBDeviceCandidate candidate) {
        return getSupportedType(candidate).isSupported();
    }

    @Override
    public boolean supports(GBDevice device) {
        return getDeviceType().equals(device.getType());
    }

    @NonNull
    @Override
    public Collection<? extends ScanFilter> createBLEScanFilters() {
        return Collections.emptyList();
    }

    @Override
    public GBDevice createDevice(GBDeviceCandidate candidate) {
        return new GBDevice(candidate.getDevice().getAddress(), candidate.getName(), getDeviceType());
    }

    @Override
    public void deleteDevice(final GBDevice gbDevice) throws GBException {
        LOG.info("will try to delete device: " + gbDevice.getName());
        if (gbDevice.isConnected() || gbDevice.isConnecting()) {
            GBApplication.deviceService().disconnect();
        }
        Prefs prefs = getPrefs();
        String lastDevice = prefs.getPreferences().getString("last_device_address","");
        if (gbDevice.getAddress() == lastDevice){
            LOG.debug("#1605 removing last device");
            prefs.getPreferences().edit().remove("last_device_address").apply();
        }
        String macAddress = prefs.getPreferences().getString(MiBandConst.PREF_MIBAND_ADDRESS,"");
        if (gbDevice.getAddress() == macAddress){
            LOG.debug("#1605 removing devel miband");
            prefs.getPreferences().edit().remove(MiBandConst.PREF_MIBAND_ADDRESS).apply();
        }
        try (DBHandler dbHandler = GBApplication.acquireDB()) {
            DaoSession session = dbHandler.getDaoSession();
            Device device = DBHelper.findDevice(gbDevice, session);
            if (device != null) {
                deleteDevice(gbDevice, device, session);
                QueryBuilder<?> qb = session.getDeviceAttributesDao().queryBuilder();
                qb.where(DeviceAttributesDao.Properties.DeviceId.eq(device.getId())).buildDelete().executeDeleteWithoutDetachingEntities();
                session.getDeviceDao().delete(device);
            } else {
                LOG.info("device to delete not found in db: " + gbDevice);
            }
        } catch (Exception e) {
            throw new GBException("Error deleting device: " + e.getMessage(), e);
        }
    }

    /**
     * Hook for subclasses to perform device-specific deletion logic, e.g. db cleanup.
     * @param gbDevice the GBDevice
     * @param device the corresponding database Device
     * @param session the session to use
     * @throws GBException
     */
    protected abstract void deleteDevice(@NonNull GBDevice gbDevice, @NonNull Device device, @NonNull DaoSession session) throws GBException;

    @Override
    public boolean allowFetchActivityData(GBDevice device) {
        return device.isInitialized() && !device.isBusy() && supportsActivityDataFetching();
    }

    public boolean isHealthWearable(BluetoothDevice device) {
        BluetoothClass bluetoothClass = device.getBluetoothClass();
        if (bluetoothClass == null) {
            LOG.warn("unable to determine bluetooth device class of " + device);
            return false;
        }
        if (bluetoothClass.getMajorDeviceClass() == BluetoothClass.Device.Major.WEARABLE
            || bluetoothClass.getMajorDeviceClass() == BluetoothClass.Device.Major.UNCATEGORIZED) {
            int deviceClasses =
                    BluetoothClass.Device.HEALTH_BLOOD_PRESSURE
                    | BluetoothClass.Device.HEALTH_DATA_DISPLAY
                    | BluetoothClass.Device.HEALTH_PULSE_RATE
                    | BluetoothClass.Device.HEALTH_WEIGHING
                    | BluetoothClass.Device.HEALTH_UNCATEGORIZED
                    | BluetoothClass.Device.HEALTH_PULSE_OXIMETER
                    | BluetoothClass.Device.HEALTH_GLUCOSE;

            return (bluetoothClass.getDeviceClass() & deviceClasses) != 0;
        }
        return false;
    }

    @Override
    public int getBondingStyle(GBDevice device) {
        return BONDING_STYLE_ASK;
    }

    @Override
    public boolean supportsActivityTracks() {
        return false;
    }

    @Override
    public boolean supportsMusicInfo() {
        return false;
    }

    public boolean supportsLedColor() {
        return false;
    }

    @Override
    public boolean supportsRgbLedColor() {
        return false;
    }

    @Override
    public int[] getColorPresets() {
        return new int[0];
    }

    @Override
    public boolean supportsUnicodeEmojis() { return false; }

    @Override
    public int[] getSupportedDeviceSpecificSettings(GBDevice device) {
        return null;
    }

}
