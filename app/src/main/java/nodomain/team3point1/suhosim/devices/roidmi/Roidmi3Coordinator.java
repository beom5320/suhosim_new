/*  Copyright (C) 2017-2019 Daniele Gobbetti, João Paulo Barraca, José
    Rebelo, tiparega

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
package nodomain.team3point1.suhosim.devices.roidmi;

import android.bluetooth.BluetoothDevice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import androidx.annotation.NonNull;
import nodomain.team3point1.suhosim.impl.GBDeviceCandidate;
import nodomain.team3point1.suhosim.model.DeviceType;

public class Roidmi3Coordinator extends RoidmiCoordinator {
    private static final Logger LOG = LoggerFactory.getLogger(Roidmi3Coordinator.class);

    @NonNull
    @Override
    public DeviceType getSupportedType(GBDeviceCandidate candidate) {
        try {
            BluetoothDevice device = candidate.getDevice();
            String name = device.getName();

            if (name != null && name.contains("Roidmi Music Blue C")) {
                return DeviceType.ROIDMI3;
            }
        } catch (Exception ex) {
            LOG.error("unable to check device support", ex);
        }

        return DeviceType.UNKNOWN;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.ROIDMI3;
    }

    @Override
    public boolean supportsRgbLedColor() {
        return true;
    }
}
