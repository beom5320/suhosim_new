/*  Copyright (C) 2017-2019 Andreas Shimokawa, Carsten Pfeiffer

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
package nodomain.team3point1.suhosim.service.devices.huami.amazfitcor;

import android.content.Context;
import android.net.Uri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

import nodomain.team3point1.suhosim.devices.huami.HuamiCoordinator;
import nodomain.team3point1.suhosim.devices.huami.HuamiFWHelper;
import nodomain.team3point1.suhosim.devices.huami.HuamiService;
import nodomain.team3point1.suhosim.devices.huami.amazfitcor.AmazfitCorFWHelper;
import nodomain.team3point1.suhosim.devices.huami.amazfitcor.AmazfitCorService;
import nodomain.team3point1.suhosim.service.btle.TransactionBuilder;
import nodomain.team3point1.suhosim.service.devices.huami.amazfitbip.AmazfitBipSupport;

public class AmazfitCorSupport extends AmazfitBipSupport {

    private static final Logger LOG = LoggerFactory.getLogger(AmazfitCorSupport.class);

    @Override
    protected AmazfitCorSupport setDisplayItems(TransactionBuilder builder) {

        Set<String> pages = HuamiCoordinator.getDisplayItems(getDevice().getAddress());
        LOG.info("Setting display items to " + (pages == null ? "none" : pages));
        byte[] command = AmazfitCorService.COMMAND_CHANGE_SCREENS.clone();

        if (pages != null) {
            if (pages.contains("status")) {
                command[1] |= 0x02;
            }
            if (pages.contains("notifications")) {
                command[1] |= 0x04;
            }
            if (pages.contains("activity")) {
                command[1] |= 0x08;
            }
            if (pages.contains("weather")) {
                command[1] |= 0x10;
            }
            if (pages.contains("alarm")) {
                command[1] |= 0x20;
            }
            if (pages.contains("timer")) {
                command[1] |= 0x40;
            }
            if (pages.contains("settings")) {
                command[1] |= 0x80;
            }
            if (pages.contains("alipay")) {
                command[2] |= 0x01;
            }
            if (pages.contains("music")) {
                command[2] |= 0x02;
            }
        }
        builder.write(getCharacteristic(HuamiService.UUID_CHARACTERISTIC_3_CONFIGURATION), command);

        return this;
    }

    @Override
    public HuamiFWHelper createFWHelper(Uri uri, Context context) throws IOException {
        return new AmazfitCorFWHelper(uri, context);
    }
}
