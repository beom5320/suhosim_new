package nodomain.team3point1.suhosim.test;

import org.junit.Test;

import nodomain.team3point1.suhosim.util.FileUtils;

import static org.junit.Assert.assertEquals;

public class FileUtilsTest extends TestBase {

    @Test
    public void testValidFileName() {
        String tempName = "foo:bar";
        assertEquals("foo_bar", FileUtils.makeValidFileName(tempName));

        tempName = "fo\no::bar";
        assertEquals("fo_o__bar", FileUtils.makeValidFileName(tempName));
    }
}
