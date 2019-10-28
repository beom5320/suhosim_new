package nodomain.team3point1.suhosim.test;

import org.junit.Assert;
import org.junit.Test;

import nodomain.team3point1.suhosim.service.btle.GattCharacteristic;

public class MiscTest extends TestBase {
    @Test
    public void testGattCharacteristic() {
        String desc = GattCharacteristic.lookup(GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT, "xxx");
        Assert.assertEquals("heart rate control point", desc);
    }
}
