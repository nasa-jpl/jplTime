package gov.nasa.jpl.time;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class EpochRelativeTimeTest {
    @Before
    public void setUp(){
        TimeTest.setupSpice();
        EpochRelativeTime.addEpoch("test", Time.getDefaultReferenceTime());
        EpochRelativeTime.addEpoch("Hello_there", new Time("2020-001T00:00:00"));
        EpochRelativeTime.addEpoch("third", new Time("2022-001T00:00:00"));
        EpochRelativeTime.addEpoch("gps_test", new Time("2021-001T00:00:00"));
    }

    @Test
    public void constructorTest(){
        EpochRelativeTime t = new EpochRelativeTime("test +        00:05:00");
        assertEquals(Time.getDefaultReferenceTime().add(new Duration("00:05:00")), t);
        EpochRelativeTime t2 = new EpochRelativeTime("test - 00:05:00");
        assertEquals(Time.getDefaultReferenceTime().subtract(new Duration("00:05:00")), t2);
        EpochRelativeTime t3 = new EpochRelativeTime("Hello_there + 00:00:00");
        assertEquals(new Time("2020-001T00:00:00"), t3);
        EpochRelativeTime t4 = new EpochRelativeTime("Hello_there + 1T00:00:00");
        assertEquals(new Time("2020-002T00:00:00"), t4);
        EpochRelativeTime t5 = new EpochRelativeTime("Hello_there + 48:00:00");
        assertEquals(new Time("2020-003T00:00:00"), t5);

        // you can make a relative time a pure absolute time using this constructor!
        Time abs_t = new Time(t);
        assertEquals("2000-001T00:05:00.000000", abs_t.toString());
    }

    @Test
    public void testToString(){
        EpochRelativeTime t = new EpochRelativeTime("Hello_there+00:05:00");

        assertEquals("Hello_there+00:05:00", t.toString(0));
        assertEquals("2020-001T00:05:00", t.toUTC(0));

        EpochRelativeTime t2 = new EpochRelativeTime("test-00:05:00");
        assertEquals("test-00:05:00.000", t2.toString(3));

        EpochRelativeTime t3 = new EpochRelativeTime("Hello_there - -1T00:00:00");
        assertEquals("Hello_there+1T00:00:00", t3.toString(0));
    }

    @Test
    public void comparisons(){
        EpochRelativeTime t = new EpochRelativeTime("test+00:05:00");
        EpochRelativeTime t2 = new EpochRelativeTime("test-00:05:00");

        assertTrue(t.greaterThan(t2));
        assertEquals(1, t.compareTo(t2));
    }

    @Test
    public void math(){
        Duration.setDefaultOutputPrecision(0);

        EpochRelativeTime t = new EpochRelativeTime("test+00:05:00");
        t = t.subtract(new Duration("00:10:00"));
        assertEquals("test-00:05:00", t.toString());

        EpochRelativeTime t2 = t.add(new Duration("5T00:00:00"));
        assertEquals("test+4T23:55:00", t2.toString());

        Duration d = t2.subtract(t);
        assertEquals("5T00:00:00", d.toString());
    }

    @Test
    public void otherTimeMethods(){
        Time absTime = new Time("2020-001T00:00:00");
        Time relTime = new EpochRelativeTime("Hello_there + 00:00:00");
        Time relTime2 = new EpochRelativeTime("third", Duration.ZERO_DURATION);
        Time relTime3 = new EpochRelativeTime("Hello_there + 13:00:00");
        Time relTime4 = new EpochRelativeTime("gps_test", Duration.ZERO_DURATION);

        assertEquals(absTime.toET(), relTime.toET(), 0.000000000000001);
        assertEquals(Time.SCET2ERT(absTime, -189900), Time.SCET2ERT(relTime, -189900));
        assertEquals("JD 2458849.500", relTime.toJulian(3));
        assertEquals("1/0694267269-12059", relTime2.toSCLK(-168));
        assertEquals(new Time("2020-001T00:00:00"), relTime3.getMidnightUTC());
        assertEquals("2021-001T00:00:18.000", relTime4.toGPS(3));
    }

    @Test
    public void relativeTimeAsEpoch(){
        EpochRelativeTime.addEpoch("meta_relative", new EpochRelativeTime("Hello_there + 02:30:00"));
        assertEquals("2020-001T02:30:45", new EpochRelativeTime("meta_relative+00:00:45").toUTC(0));
        assertEquals("2020-01-01T02:30:45", new EpochRelativeTime("meta_relative+00:00:45").toISOC(0));
    }

    @Test
    public void getRelativeTimeFromAbsolute(){
        Time t1 = new Time("2020-003T00:00:00");
        EpochRelativeTime check1 = new EpochRelativeTime(t1, "Hello_there");
        assertEquals("Hello_there+2T00:00:00", check1.toString(0));
    }

    @Test
    public void readEpochCVF(){
        try {
            EpochRelativeTime.readEpochCVF("src/test/resources/gov/nasa/jpl/time/cvf/dc075a_tf.r4.cvf");
            assertEquals(new Time("2014-231T02:44:00"), new Time(new EpochRelativeTime("CRU_seqbound_001", Duration.ZERO_DURATION)));
            assertEquals(new Time("2014-240T18:00:11"), new Time(new EpochRelativeTime("CRU_tv_003", Duration.ZERO_DURATION)));
            assertEquals(new Time("2014-258T10:38:04"), new Time(new EpochRelativeTime("CRU_comm_002", Duration.ZERO_DURATION)));
            assertEquals(new Time("2014-300T08:00:00"), new Time(new EpochRelativeTime("CRU_coast_004", Duration.ZERO_DURATION)));

            EpochRelativeTime.readEpochCVF("src/test/resources/gov/nasa/jpl/time/cvf/yx302_SCI.r0.cvf");
            assertEquals(new Time("2029-349T11:45:00"), new Time(new EpochRelativeTime("PB1_DLTERM_096", Duration.ZERO_DURATION)));
            assertEquals(new Time("2029-349T10:53:25"), new Time(new EpochRelativeTime("SPTG_NADIR_TURN", Duration.ZERO_DURATION)));
            assertEquals(new Time("2029-349T14:45:00"), new Time(new EpochRelativeTime("IMGA_CADENCE2_START", Duration.ZERO_DURATION)));

        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getEpochCVFString(){
        EpochRelativeTime.addEpoch("relative", new EpochRelativeTime("Hello_there+07:42:00"));
        String cvfString = EpochRelativeTime.getEpochCVFString(Arrays.asList("test", "Hello_there", "gps_test", "relative"), "", true);
        assertTrue(cvfString.contains("/Hello_there\n\"const\" 2020-001T00:00:00"));
        assertTrue(cvfString.contains("/gps_test\n\"const\" 2021-001T00:00:00"));
        assertTrue(cvfString.indexOf("test") < cvfString.indexOf("gps_test"));
        assertTrue(cvfString.contains("/relative\n\"const\" Hello_there+07:42:00"));

        String cvfString2 = EpochRelativeTime.getEpochCVFString(EpochRelativeTime.getEpochs().keySet(), "MPST\n", false);
        assertTrue(cvfString2.contains("MPST"));
        assertTrue(cvfString2.indexOf("gps_test") < cvfString2.indexOf("test"));
    }

    @Test
    public void getAbsoluteOrRelativeTime(){
        Time abs = EpochRelativeTime.getAbsoluteOrRelativeTime("2020-003T00:00:00");
        Time rel = EpochRelativeTime.getAbsoluteOrRelativeTime("Hello_there+2T00:00:00");
        assertFalse(abs instanceof EpochRelativeTime);
        assertTrue( rel instanceof EpochRelativeTime);

        try {
            Time nil = EpochRelativeTime.getAbsoluteOrRelativeTime("fail please");
            fail();
        }
        catch(RuntimeException e){
            assertTrue(e.getMessage().contains("could not be parsed into either an absolute or relative time"));
        }
    }

    @Test
    public void removeEpoch(){
        EpochRelativeTime.addEpoch("relative", new EpochRelativeTime("Hello_there+07:42:00"));
        assertTrue(EpochRelativeTime.getEpochs().containsKey("relative"));
        assertTrue(EpochRelativeTime.getEpochs().containsKey("Hello_there"));
        EpochRelativeTime.removeEpoch("Hello_there");
        assertFalse(EpochRelativeTime.getEpochs().containsKey("relative"));
        assertFalse(EpochRelativeTime.getEpochs().containsKey("Hello_there"));
    }
}
