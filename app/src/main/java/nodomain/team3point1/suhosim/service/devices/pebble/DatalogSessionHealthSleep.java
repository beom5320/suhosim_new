/*  Copyright (C) 2016-2019 0nse, Andreas Shimokawa, Carsten Pfeiffer,
    Daniele Gobbetti

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
package nodomain.team3point1.suhosim.service.devices.pebble;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import nodomain.team3point1.suhosim.GBApplication;
import nodomain.team3point1.suhosim.database.DBHandler;
import nodomain.team3point1.suhosim.database.DBHelper;
import nodomain.team3point1.suhosim.deviceevents.GBDeviceEvent;
import nodomain.team3point1.suhosim.entities.DaoSession;
import nodomain.team3point1.suhosim.entities.PebbleHealthActivityOverlay;
import nodomain.team3point1.suhosim.entities.PebbleHealthActivityOverlayDao;
import nodomain.team3point1.suhosim.impl.GBDevice;
import nodomain.team3point1.suhosim.util.GB;

class DatalogSessionHealthSleep extends DatalogSessionPebbleHealth {

    private static final Logger LOG = LoggerFactory.getLogger(DatalogSessionHealthSleep.class);

    DatalogSessionHealthSleep(byte id, UUID uuid, int timestamp, int tag, byte item_type, short item_size, GBDevice device) {
        super(id, uuid, timestamp, tag, item_type, item_size, device);
        taginfo = "(Health - sleep " + tag + " )";
    }

    @Override
    public GBDeviceEvent[] handleMessage(ByteBuffer datalogMessage, int length) {
        LOG.info("DATALOG " + taginfo + GB.hexdump(datalogMessage.array(), datalogMessage.position(), length));

        if (!isPebbleHealthEnabled()) {
            return null;
        }

        int initialPosition = datalogMessage.position();
        int beginOfRecordPosition;
        short recordVersion; //probably

        if (0 != (length % itemSize))
            return null;//malformed message?

        int recordCount = length / itemSize;
        SleepRecord[] sleepRecords = new SleepRecord[recordCount];
        byte[] tempRecord = new byte[itemSize];

        for (int recordIdx = 0; recordIdx < recordCount; recordIdx++) {
            beginOfRecordPosition = initialPosition + recordIdx * itemSize;
            datalogMessage.position(beginOfRecordPosition);//we may not consume all the bytes of a record
            datalogMessage.get(tempRecord);

            sleepRecords[recordIdx] = new SleepRecord(tempRecord);
        }

        store(sleepRecords);
        return new GBDeviceEvent[]{null};
    }

    private void store(SleepRecord[] sleepRecords) {
        try (DBHandler dbHandler = GBApplication.acquireDB()) {
            DaoSession session = dbHandler.getDaoSession();
            Long userId = DBHelper.getUser(session).getId();
            Long deviceId = DBHelper.getDevice(getDevice(), session).getId();

            PebbleHealthActivityOverlayDao overlayDao = session.getPebbleHealthActivityOverlayDao();

            List<PebbleHealthActivityOverlay> overlayList = new ArrayList<>();
            for (SleepRecord sleepRecord : sleepRecords) {
                //TODO: check the firmware version and don't use the sleep record if overlay is available?
                overlayList.add(new PebbleHealthActivityOverlay(sleepRecord.bedTimeStart, sleepRecord.bedTimeEnd, sleepRecord.type, deviceId, userId, sleepRecord.getRawData()));
            }
            overlayDao.insertOrReplaceInTx(overlayList);
        } catch (Exception ex) {
            LOG.debug(ex.getMessage());
        }
    }

    private class SleepRecord {
        byte[] knownVersions = {1};
        short version;
        int type = 1; //sleep, hardcoded as we don't get other info
        int offsetUTC; //probably
        int bedTimeStart;
        int bedTimeEnd;
        int deepSleepSeconds;
        byte[] rawData;

        SleepRecord(byte[] rawData) {
            this.rawData = rawData;
            ByteBuffer record = ByteBuffer.wrap(rawData);
            record.order(ByteOrder.LITTLE_ENDIAN);


            this.version = record.getShort();
            //TODO: check supported versions?
            this.offsetUTC = record.getInt();
            this.bedTimeStart = record.getInt();
            this.bedTimeEnd = record.getInt();
            this.deepSleepSeconds = record.getInt();
        }

        byte[] getRawData() {
            if (storePebbleHealthRawRecord()) {
                return rawData;
            }
            return null;
        }
    }

}