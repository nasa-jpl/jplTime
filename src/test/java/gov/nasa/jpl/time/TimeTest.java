package gov.nasa.jpl.time;

import gov.nasa.jpl.lighttime.LightTimeProvider;
import gov.nasa.jpl.lighttime.SPKLightTimeProvider;
import org.junit.Before;
import org.junit.Test;
import spice.basic.CSPICE;
import spice.basic.SpiceErrorException;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static gov.nasa.jpl.time.Time.AM_PM.AM;
import static gov.nasa.jpl.time.Time.AM_PM.PM;
import static gov.nasa.jpl.time.Time.SCLK_PATTERN;
import static org.junit.Assert.*;

public class TimeTest {

    @Before
    public void setUp(){
        setupSpice();
    }

    static void setupSpice(){
        System.loadLibrary("JNISpice");
        try {
            CSPICE.furnsh("kernels/naif0012.tls");
            CSPICE.furnsh("kernels/M2020_SCLKSCET.NOMNM.tsc");
            CSPICE.furnsh("kernels/NSY_SCLKSCET.00013.tsc");
            CSPICE.furnsh("kernels/m2020_lmst_dev00_v3.tsc");
            CSPICE.furnsh("kernels/insight_lmst_ops181206_v1.tsc");
            CSPICE.furnsh("kernels/PSYC_69_SCLKSCET.00000.tsc");
            CSPICE.furnsh("kernels/pck00010.tpc");
            CSPICE.furnsh("kernels/de421.bsp");
            CSPICE.furnsh("kernels/mar097.bsp");
            CSPICE.furnsh("kernels/insight_ls_ops181206_iau2000_v1.bsp");
            CSPICE.furnsh("kernels/insight_atls_ops181206_v1.bsp");
            CSPICE.furnsh("kernels/m2020_ls_dev00_iau2000_v3.bsp");
            CSPICE.furnsh("kernels/m2020_atls_dev00_v3.bsp");
        } catch (SpiceErrorException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void add() {
        Time t = new Time("2000-001T00:00:10");
        Time t2 = t.add(new Duration("00:00:15"));
        assertTrue(t2.equals(new Time("2000-001T00:00:25")));
    }

    @Test
    public void subtract() {
        Duration d = new Duration("00:00:05");
        assertEquals(d, ((new Time("2000-001T00:01:15")).subtract(new Time("2000-001T00:01:10"))));
    }

    @Test
    public void min(){
        Time t1 = new Time("2000-001T00:00:10");
        Time t2 = new Time("2000-001T00:00:20");
        Time t3 = new Time("2000-001T00:00:30");
        Time t4 = new Time("2000-001T00:00:05");

        assertEquals(new Time("2000-001T00:00:10"), Time.min(t1, t2));
        assertEquals(new Time("2000-001T00:00:05"), Time.min(t1, t2, t3, t4));
        assertEquals(new Time("2000-001T00:00:10"), Time.min(t1));
        try{
            Time.min();
            fail();
        }
        catch(NoSuchElementException e){
        }
    }

    @Test
    public void max(){
        Time t1 = new Time("2000-001T00:00:10");
        Time t2 = new Time("2000-001T00:00:20");
        Time t3 = new Time("2000-001T00:00:30");
        Time t4 = new Time("2000-001T00:00:05");

        assertEquals(new Time("2000-001T00:00:20"), Time.max(t1, t2));
        assertEquals(new Time("2000-001T00:00:30"), Time.max(t1, t2, t3, t4));
        assertEquals(new Time("2000-001T00:00:10"), Time.max(t1));
        try{
            Time.max();
            fail();
        }
        catch(NoSuchElementException e){
        }
    }

    @Test
    public void toUTC() {
        Time t = new Time("1970-001T00:00:00.000");
        assertEquals("1970-001T00:00:00.000000", t.toUTC());
        assertEquals("1970-001T00:00:00.000", t.toUTC(3));
        Time t2 = new Time("1970-001T02:00:00.001");
        assertEquals("1970-001T02:00:00.001000", t2.toUTC(6));

        Time t3 = new Time("1970-01-01T00:00:00");
        assertEquals("1970-001T00:00:00.000000", t3.toUTC());
        assertEquals("1970-001T00:00:00", t3.toUTC(0));

        Time t4 = new Time("2019-199T23:59:59.999999650");
        assertEquals("2019-200T00:00:00.000000", t4.toUTC(6));

        Time t5 = new Time("2019-199T20:59:59.999999650");
        assertEquals("2019-199T21:00:00.000", t5.toUTC(3));

        Time t6 = new Time("2019-365T23:59:59.999");
        assertEquals("2020-001T00:00:00", t6.toUTC(0));
    }

    @Test
    public void toUTCFormats(){
        Time t = new Time("2020-002T00:00:00");
        assertEquals("2020-01-02T00:00:00.000000", t.toISOC());
        assertEquals("2020-01-02T00:00:00.000", t.toISOC(3));
        assertEquals("JD 2458850.500", t.toJulian(3));
        assertEquals("2020 JAN 02 00:00:00.000", t.toCalendar(3));
    }

    @Test
    public void toET(){
        Time t = new Time("2019-119T21:04:38.987851");
        assertEquals(609843948.173351, t.toET(), 0.000001);
    }

    @Test
    public void fromET(){
        Time t = Time.fromET(0);
        assertEquals("2000-001T11:58:55.816", t.toUTC(3));
        Time t2 = Time.fromET(634956425.895100);
        assertEquals("2020-045T12:45:56.710", t2.toUTC(3));
        assertEquals(634956425.895100, t2.toET(), 0.000001);
    }

    @Test
    public void fromTDBString(){
        Time t = Time.fromTDBString("2000-01-01T12:00:00");
        assertEquals("2000-001T11:58:55.816", t.toUTC(3));
        // 5 leap seconds passed between J2000 and 2020
        Time t2 = Time.fromTDBString("2020-01-01T12:00:00");
        assertEquals("2020-001T11:58:50.816", t2.toUTC(3));
    }

    @Test
    public void toTAI(){
        Time t = new Time("2019-119T21:04:38.987851");
        try {
            assertEquals(CSPICE.unitim( 609843948.173351, "ET", "TAI"), t.toTAI(), 0.000001);
        } catch (SpiceErrorException e) {
            fail();
        }
    }

    @Test
    public void sclkRegexTest(){
        String badString = "1/069426726912059";
        assertFalse(SCLK_PATTERN.matcher(badString).find());

        String badString2 = "1/0694267269k12059";
        assertFalse(SCLK_PATTERN.matcher(badString2).find());
    }

    @Test
    public void toSCLK(){
        Time t = new Time("2022-001T00:00:00");
        Time.setDefaultSpacecraftId(-168);
        assertEquals("1/0694267269-12059", t.toSCLK());
        assertEquals("1/0694267269-12059", t.toSCLK(-168));

        Time t2 = new Time("2025-050T00:00:00");
        assertEquals("1/0793195269:12059", t2.toSCLK(-69));
    }

    @Test
    public void fromSCLK(){
        Time.setDefaultSpacecraftId(-189);
        Time t1 = Time.fromSCLK("1/0694897269-12345");
        assertEquals("2022-008T07:04:10.190049", t1.toString());

        Time t2 = Time.fromSCLK("1/0694897269-12345", -189);
        assertEquals("2022-008T07:04:10.190049", t2.toString());

        Time t3 = Time.fromSCLK("1/0793195269:12059", -69);
        assertEquals("2025-050T00:00:00.000", t3.toUTC(3)); //can't do all 6 digits because SCLK isn't precise enough

        Time t4 = Time.fromSCLK("1/0793195269 12059", -69);
        assertEquals("2025-050T00:00:00.000", t4.toUTC(3));

        Time t5 = Time.fromSCLK("1/0793195269,12059", -69);
        assertEquals("2025-050T00:00:00.000", t5.toUTC(3));

        Time t6 = Time.fromSCLK("1/0793195269.12059", -69);
        assertEquals("2025-050T00:00:00.000", t6.toUTC(3));
    }

    @Test
    public void toSCLKD(){
        Time.setDefaultSpacecraftId(-168);
        Time t = new Time("2022-001T00:00:00");
        assertEquals(694267269.1840057, t.toSCLKD(), 0.00000001);
        assertEquals(694267269.1840057, t.toSCLKD(-168), 0.00000001);

        Time t2 = new Time("2025-050T00:00:00");
        assertEquals(793195269 + (12059/65536.0), t2.toSCLKD(-69), 0.00000001);
    }

    @Test
    public void fromSCLKD(){
        Time.setDefaultSpacecraftId(-189);
        Time t1 = Time.fromSCLKD(694897269.188370);
        Time t2 = Time.fromSCLKD(694897269.188370, -189);
        assertEquals("2022-008T07:04:10.190049", t1.toString());
        assertEquals("2022-008T07:04:10.190049", t2.toString());

        Time t3 = Time.fromSCLKD(793195269 + (12059/65536.0), -69);
        assertEquals("2025-050T00:00:00.000", t3.toUTC(3)); //can't do all 6 digits because SCLK isn't precise enough
    }

    @Test
    public void toMidnightUTC(){
        Time t = new Time("1900-060T12:45:21");
        assertEquals(new Time("1900-060T00:00:00"), t.getMidnightUTC());

        Time t2 = new Time("1995-256T23:59:59.999");
        assertEquals(new Time("1995-256T00:00:00"), t2.getMidnightUTC());

        Time t3 = new Time("2022-300T07:21:00");
        assertEquals(new Time("2022-300T00:00:00"), t3.getMidnightUTC());
    }

    @Test
    public void getTimeOfDay(){
        Time t = new Time("1900-060T12:45:21");
        Duration expected = new Duration("12:45:21");
        assertEquals(expected, t.getTimeOfDay());

        Time t2 = new Time("1995-256T23:59:59.999");
        Duration expected2 = new Duration("23:59:59.999");
        assertEquals(expected2, t2.getTimeOfDay());
    }

    @Test
    public void toLMST(){
        Time.setDefaultSpacecraftId(-168);
        Time t = new Time("2022-001T00:00:00");
        assertEquals("Sol-0308M02:59:38.698040", t.toLMST());
        assertEquals("Sol-0308M02:59:39",     t.toLMST(0));
        assertEquals("Sol-0308M02:59:38.7",   t.toLMST(-168, 1));
        assertEquals("Sol-0308M02:59:38.70",  t.toLMST(2));
        assertEquals("Sol-0308M02:59:38.698", t.toLMST(-168, 3));
    }

    @Test
    public void toLmstAmPm() {
        Time.setDefaultSpacecraftId(-168);
        Time am1 = Time.fromLMST("Sol-0100M00:00:00");
        Time am2 = Time.fromLMST("Sol-0100M05:00:00");
        Time am3 = Time.fromLMST("Sol-0100M10:50:10.123");
        Time am4 = Time.fromLMST("Sol-0100M11:59:59.999");

        Time pm1 = Time.fromLMST("Sol-0100M12:00:00");
        Time pm2 = Time.fromLMST("Sol-0100M17:00:00");
        Time pm3 = Time.fromLMST("Sol-0100M22:50:10.123");
        Time pm4 = Time.fromLMST("Sol-0100M23:59:59.999");

        assertEquals(AM, am1.toLmstAmPm());
        assertEquals(AM, am2.toLmstAmPm());
        assertEquals(AM, am3.toLmstAmPm());
        assertEquals(AM, am4.toLmstAmPm());

        assertEquals(PM, pm1.toLmstAmPm());
        assertEquals(PM, pm2.toLmstAmPm());
        assertEquals(PM, pm3.toLmstAmPm());
        assertEquals(PM, pm4.toLmstAmPm());
    }

    @Test
    public void toUtcAmPm() {
        Time am1 = new Time("2019-150T00:00:00");
        Time am2 = new Time("2019-150T05:00:00");
        Time am3 = new Time("2019-150T10:50:10.123");
        Time am4 = new Time("2019-150T11:59:59.999");

        Time pm1 = new Time("2019-150T12:00:00");
        Time pm2 = new Time("2019-150T17:00:00");
        Time pm3 = new Time("2019-150T22:50:10.123");
        Time pm4 = new Time("2019-150T23:59:59.999");

        assertEquals(AM, am1.toUtcAmPm());
        assertEquals(AM, am2.toUtcAmPm());
        assertEquals(AM, am3.toUtcAmPm());
        assertEquals(AM, am4.toUtcAmPm());

        assertEquals(PM, pm1.toUtcAmPm());
        assertEquals(PM, pm2.toUtcAmPm());
        assertEquals(PM, pm3.toUtcAmPm());
        assertEquals(PM, pm4.toUtcAmPm());
    }

    @Test
    public void toSolNumber(){
        Time.setDefaultSpacecraftId(-168);

        Time t1 = new Time("2022-001T00:00:00");
        assertEquals(308, t1.toSolNumber());

        Time t2 = Time.fromLMST("Sol-0026M09:25:00.54", -189);
        assertEquals(26, t2.toSolNumber(-189));
    }

    @Test
    public void toFractionalSols(){
        Time.setDefaultSpacecraftId(-189);

        Time t1 = Time.fromLMST("Sol-0026M06:00:00", -189);
        assertEquals(26.25, t1.toFractionalSols(), 0.00000001);

        Time t2 = Time.fromLMST("Sol-0026M12:00:00", -189);
        assertEquals(26.5, t2.toFractionalSols(-189), 0.00000001);

    }

    @Test
    public void fromLMST(){
        Time.setDefaultSpacecraftId(-168);

        Time t1 = Time.fromLMST("Sol-050M00:00:00");
        assertEquals("2021-100T18:41:52.028", t1.toUTC(3));
        Time t2 = Time.fromLMST("Sol-050M00:00:00.000");
        assertEquals("2021-100T18:41:52.028", t2.toUTC(3));

        Time t3 = Time.fromLMST("Sol-050M00:00:00.1", -168);
        assertEquals("2021-100T18:41:52.131", t3.toUTC(3));
        Time t4 = Time.fromLMST("000150 12:34:56.789", -189);
        assertEquals("2019-119T21:04:38.987851", t4.toString());

        Time t5 = Time.fromLMST(t1.toLMST());
        assertEquals(t1, t5);
    }

    @Test
    public void toLTST(){
        Time.setDefaultSpacecraftId(-168);
        Time.setDefaultLstBodyId(499);
        Time.setDefaultLstBodyFrame("IAU_MARS");

        Time t1 = new Time("2022-001T00:00:00");
        assertEquals("Sol-0308T03:32:42",t1.toLTST());

        Time t2 = new Time("2019-119T21:04:38.987851");
        assertEquals("Sol-0150T12:04:24", t2.toLTST(-189));
    }



    @Test
    public void toTimezone(){
        Time t1 = new Time("2019 Aug 9 02:00:00");
        assertEquals(DayOfWeek.THURSDAY, t1.toTimezone("America/Los_Angeles").getDayOfWeek());
    }

    @Test
    public void toTimezoneString(){
        Time t1 = new Time("2020-002T00:00:00");
        assertEquals("2020-001T16:00:00.000000", t1.toTimezoneString("America/Los_Angeles"));
        assertEquals("2020-001T16:00:00.000000", t1.toTimezoneString("America/Los_Angeles", 6));
        assertEquals("2020-002T05:30:00", t1.toTimezoneString("Asia/Kolkata", 0));
        assertEquals("2020-001T14:00:00.000", t1.toTimezoneString("America/Adak", 3));
    }

    @Test
    public void toGPSSeconds(){
        Time t1 = new Time("2021-001T00:00:00");
        assertEquals(1293494418, t1.toGPSSeconds(), 0.000000001);
        Time t2 = new Time("2021-001T00:00:00.9758");
        assertEquals(1293494418.9758, t2.toGPSSeconds(), 0.000000001);

    }

    @Test
    public void fromGPSSeconds(){
        Time t = Time.fromGPSSeconds(1293494418.9758);
        assertEquals("2021-001T00:00:00.975800", t.toString());
    }

    @Test
    public void toGPS(){
        Time t1 = new Time("2021-001T00:00:00");
        assertEquals("2021-001T00:00:18.000", t1.toGPS(3));
        Time t2 = new Time("2015-250T00:00:00.777111");
        assertEquals("2015-250T00:00:17.777111", t2.toGPS(6));
    }

    @Test
    public void fromGPS(){
        assertEquals("2021-001T00:00:00", Time.fromGPS("2021-001T00:00:18.000").toUTC(0));
        assertEquals("2015-250T00:00:00.777111", Time.fromGPS("2015-250T00:00:17.777111").toUTC(6));
    }

    @Test
    public void toExcelUTC(){
        Time t = new Time("2019-052T18:45:00");
        assertEquals("02/21/19 18:45:00", t.toExcelUTC());
        assertEquals("02/21/2019 18:45:00", t.toExcelUTC(true));
    }

    @Test
    public void fromExcel(){
        Time excelTime = Time.fromExcelUTC(" 02/21/19 18:45");
        assertEquals(new Time("2019-052T18:45:00"), excelTime);

        Time excelTime4 = Time.fromExcelUTC("02/21/2019 18:45");
        assertEquals(new Time("2019-052T18:45:00"), excelTime4);
    }

    @Test
    public void SCET2ERT(){
        Time.setDefaultSpacecraftId(-189);
        Time t = new Time("2019-200T00:00:00");
        assertEquals("2019-200T00:21:47.925378", Time.SCET2ERT(t, -189900).toString());
        assertEquals("2019-200T00:21:47.925378", Time.SCET2ERT(t).toString());
    }

    @Test
    public void SCET2ETT(){
        Time.setDefaultSpacecraftId(-189);
        Time t = new Time("2019-200T00:00:00");
        assertEquals("2019-199T23:38:12.138717", Time.SCET2ETT(t, -189900).toString());
        assertEquals("2019-199T23:38:12.138717", Time.SCET2ETT(t).toString());
    }

    @Test
    public void ERT2SCET(){
        Time t = new Time("2019-200T00:00:00");
        Time t2 = Time.ERT2SCET(t, -189900);
        assertEquals(Time.SCET2ERT(t2, -189900), t);
    }

    @Test
    public void ETT2SCET(){
        Time t = new Time("2019-200T00:00:00");
        Time t2 = Time.ETT2SCET(t, -189900);
        assertEquals(Time.SCET2ETT(t2, -189900), t);
    }

    @Test
    public void ETT2ERT(){
        Time t = new Time("2019-199T23:38:12.138717");
        Time t2 = Time.ETT2ERT(t, -189900);
        assertEquals("2019-200T00:21:47.925378", t2.toUTC());
        assertEquals(t, Time.ERT2ETT(t2, -189900));
    }

    private class LightTimeIsAlwaysFiveMinutes implements LightTimeProvider{

        @Override
        public Duration downleg(Time t, int sc_id, int body_id, String time_reference) {
            return new Duration("00:05:00");
        }

        @Override
        public Duration upleg(Time t, int sc_id, int body_id, String time_reference) {
            return new Duration("00:05:00");
        }
    }

    @Test
    public void LightTimeProvider(){
        Time.setLightTimeProvider(new LightTimeIsAlwaysFiveMinutes());
        Time t = new Time("2019-200T00:00:00");
        assertEquals("2019-200T00:05:00", Time.SCET2ERT(t).toUTC(0));
        assertEquals("2019-199T23:55:00", Time.SCET2ETT(t).toUTC(0));
        assertEquals("2019-200T00:10:00", Time.ETT2ERT(t).toUTC(0));
        Time.setLightTimeProvider(new SPKLightTimeProvider());
    }

    @Test
    public void getMilliseconds() {
        Time t2 = new Time("1970-001T00:00:10");
        assertEquals("10000", t2.getMilliseconds());
    }

    @Test
    public void valueOf() {
        Time t = new Time("2000-001T00:00:00");
        t.valueOf("1970-001T02:00:00.001");
        assertEquals(new Time("1970-001T02:00:00.001"), t);
    }

    @Test
    public void equals() {
        Time t = new Time("2000-001T00:00:00");
        Time t2 = new Time("2000-001T00:00:00");
        assertEquals(t2, t);
    }

    @Test
    public void equalToWithin() {
        Time t1 = new Time("2000-001T00:00:00");
        Time t2 = t1;
        Duration d1 = new Duration("00:00:10");
        Duration d2 = d1.divide(3).multiply(3);

        Time t3 = t1.add(d1);
        Time t4 = t2.add(d2);

        assertTrue(t3.equalToWithin(t4, Duration.MICROSECOND_DURATION));
    }

    @Test
    public void round() {
        Time t1 = new Time("2019-001T00:33:00");
        Time referenceTime1 = new Time("2019-001T00:00:00");
        Time referenceTime2 = new Time("2019-001T00:01:00");
        Duration resolution = new Duration("00:05:00");
        Time expectedTime1 = new Time("2019-001T00:35:00");
        Time expectedTime2 = new Time("2019-001T00:31:00");
        assertEquals(t1.round(resolution), expectedTime1);
        assertEquals(t1.round(resolution, referenceTime1), expectedTime1);
        assertEquals(t1.round(resolution, referenceTime2), expectedTime2);
    }

    @Test
    public void ceil() {
        Time t1 = new Time("2019-001T00:33:00");
        Time referenceTime1 = new Time("2019-001T00:00:00");
        Time referenceTime2 = new Time("2019-001T00:01:00");
        Duration resolution = new Duration("00:05:00");
        Time expectedTime1 = new Time("2019-001T00:35:00");
        Time expectedTime2 = new Time("2019-001T00:36:00");
        assertEquals(t1.ceil(resolution), expectedTime1);
        assertEquals(t1.ceil(resolution, referenceTime1), expectedTime1);
        assertEquals(t1.ceil(resolution, referenceTime2), expectedTime2);
    }

    @Test
    public void floor() {
        Time t1 = new Time("2019-001T00:33:00");
        Time referenceTime1 = new Time("2019-001T00:00:00");
        Time referenceTime2 = new Time("2019-001T00:01:00");
        Duration resolution = new Duration("00:05:00");
        Time expectedTime1 = new Time("2019-001T00:30:00");
        Time expectedTime2 = new Time("2019-001T00:31:00");
        assertEquals(t1.floor(resolution), expectedTime1);
        assertEquals(t1.floor(resolution, referenceTime1), expectedTime1);
        assertEquals(t1.floor(resolution, referenceTime2), expectedTime2);
    }

    @Test
    public void tics()
    {
        Time t1 = new Time("2000-001T12:00:00");
        assertEquals(t1, Time.fromTics(0L));
    }

    @Test
    public void useSpiceForMath(){
        Time.setUseSpiceForMath(true);
        Time t3 = new Time("2016-366T23:59:59").add(Duration.SECOND_DURATION);
        assertEquals("2016-12-31T23:59:60.000000", t3.toISOC());
        // we mean for this second to be repeated in this mode
        assertEquals("2016-12-31T23:59:59Z[UTC]", t3.toTimezone("UTC").toString());
        Time.setUseSpiceForMath(false);
    }
}
