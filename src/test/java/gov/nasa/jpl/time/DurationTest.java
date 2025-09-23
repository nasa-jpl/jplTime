package gov.nasa.jpl.time;

import org.junit.Before;
import org.junit.Test;
import spice.basic.CSPICE;
import spice.basic.SpiceErrorException;

import java.util.NoSuchElementException;
import java.util.regex.Matcher;

import static gov.nasa.jpl.time.Duration.*;
import static org.junit.Assert.*;

public class DurationTest {

    @Before
    public void setUp(){
        System.loadLibrary("JNISpice");
        try {
            CSPICE.furnsh("kernels/naif0012.tls");
        } catch (SpiceErrorException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getTics() {
        Duration d1 = new Duration("00:00:00.000001");
        assertEquals(100, d1.getTics());
        Duration d2 = new Duration("00:00:14.759484");
        assertEquals(1475948400, d2.getTics());
    }

    @Test
    public void getHours(){
        Duration d1 = new Duration("T10:00:00");
        assertEquals(10, d1.getHours());

        Duration d2 = new Duration("00:30:00");
        assertEquals(0, d2.getHours());
    }

    @Test
    public void verifyPresets() {
        assertEquals(Duration.ZERO_DURATION, new Duration("00:00:00"));
        assertEquals(Duration.SECOND_DURATION, new Duration("00:00:01"));
        assertEquals(Duration.MINUTE_DURATION, new Duration("00:01:00"));
        assertEquals(Duration.HOUR_DURATION, new Duration("01:00:00"));
        assertEquals(Duration.DAY_DURATION, new Duration("24:00:00"));
        assertEquals(Duration.MICROSECOND_DURATION, new Duration("00:00:00.000001"));
    }

    @Test
    public void staticFromMethods() {
        Duration d1 = Duration.fromSeconds(10);
        Duration d2 = Duration.fromSeconds(10.1234);

        Duration d3 = Duration.fromMinutes(100);
        Duration d4 = Duration.fromMinutes(-123.456);

        Duration d5 = Duration.fromHours(5);
        Duration d6 = Duration.fromHours(5.1245);

        Duration d7 = Duration.fromDays(16);
        Duration d8 = Duration.fromDays(-123.456);

        assertEquals(10, d1.getSeconds());
        assertEquals(new Duration("00:00:10.1234"), d2);

        assertEquals(100, d3.getMinutes());
        assertEquals(new Duration("-02:03:27.360"), d4);

        assertEquals(5, d5.getHours());
        assertEquals(new Duration("05:07:28.200"), d6);

        assertEquals(16, d7.getDays());
        assertEquals(new Duration("1T00:00:00").multiply(-123.456), d8);
    }

    @Test
    public void marsDurations() {
        Duration.setDefaultOutputPrecision(6);

        assertEquals("1M00:00:00", Duration.fromMarsDur("1M00:00:00").toMarsDurString(0));
        assertEquals("1M00:00:00.000000", Duration.fromMarsDur("1M00:00:00").toMarsDurString());
        assertEquals("1M00:00:00.0", new Duration("1T00:39:35.244").toMarsDurString(1));
        assertEquals("M00:00:00", ZERO_DURATION.toMarsDurString(0));
    }


    @Test
    public void totalSeconds() {
        Duration d1 = new Duration("00:00:01.753000");
        assertEquals(1.753, d1.totalSeconds(), 0.0000001);
    }

    @Test
    public void add() {
        Duration d1 = new Duration("00:00:00.000001");
        Duration d2 = new Duration("00:00:14.759484");
        assertEquals(1475948500, d1.add(d2).getTics());
    }

    @Test
    public void subtract() {
        Duration d1 = new Duration("00:00:01");
        Duration d2 = new Duration("00:01:00");
        assertEquals(Duration.SECOND_DURATION.multiply(59), d2.subtract(d1));
        assertEquals(Duration.SECOND_DURATION.multiply(-59), d1.subtract(d2));
    }

    @Test
    public void multiply() {
        Duration d1 = new Duration("00:00:01");
        Duration d2 = new Duration("00:01:10.000");
        assertEquals(d1.multiply(70.0), d2);
        assertEquals(d1.multiply(70L), d2);
        assertEquals(d1.multiply(70), d2);
        assertEquals(d1.multiply(-70), d2.multiply(-1));

        Duration d3 = new Duration("1T00:00:00");
        Duration d4 = new Duration("7T00:00:00");
        assertEquals(d3.multiply(7), d4);
        assertEquals(d3.multiply(7L), d4);
        assertEquals(d3.multiply(7.0), d4);

        assertEquals(d3, d4.add(d3.multiply(-6)));
    }

    @Test
    public void divide() {
        Duration d1 = new Duration("00:20:00");
        Duration d2 = new Duration("00:00:10");
        assertEquals(d1.divide(120.0), d2);
        assertEquals(d1.divide(120L), d2);
        assertEquals(d1.divide(120), d2);
        assertEquals(d1.divide(-120), d2.multiply(-1));
        assertEquals(d1.divide(d2), 120.0, 0.000001);

        Duration d3 = d2.multiply(2).divide(3);
        Duration d4 = d2.divide(3).multiply(2);
        assertTrue(d3.equalToWithin(d4, Duration.MICROSECOND_DURATION));

        Duration d5 = d2.divide(3).multiply(3);
        assertTrue(d2.equalToWithin(d5, Duration.MICROSECOND_DURATION));

        assertEquals(new Duration("00:00:03.33333333"), d2.divide(3));
        assertEquals(new Duration("00:02:51.42857143"), d1.divide(7));
        assertEquals(new Duration("00:02:51.42857143"), d1.divide(7.0));

        assertEquals(new Duration("200T00:00:00"), new Duration("1400T00:00:00").divide(7));
        assertEquals(new Duration("127T06:32:43.63636364"), new Duration("1400T00:00:00").divide(11));

    }

    @Test
    public void mod(){
        Duration d1 = DAY_DURATION;
        Duration d2 = new Duration("26:00:00");
        Duration d3 = new Duration("-26:00:00");
        Duration d4 = new Duration("00:00:00.1");
        Duration d5 = new Duration("00:00:00.75");
        Duration two_hours = new Duration("02:00:00");

        assertEquals(two_hours, d2.mod(d1));
        assertEquals(two_hours.multiply(-1), d3.mod(d1));
        assertEquals(two_hours, two_hours.mod(d1));
        assertEquals(d4, d4.mod(two_hours));
        assertEquals(new Duration("00:00:00.05"), d5.mod(d4));
        assertEquals(ZERO_DURATION, d1.mod(d1));
        assertEquals(ZERO_DURATION, ZERO_DURATION.mod(two_hours));
    }

    @Test
    public void abs() {
        Duration d1 = new Duration("00:20:00");
        Duration d2 = new Duration("-00:20:00");
        assertEquals(d1.abs(), d1);
        assertEquals(d2.abs(), d1);
        assertNotEquals(d1, d2);

        d1 = new Duration("5T00:20:00");
        d2 = new Duration("-5T00:20:00");
        assertEquals(d1.abs(), d1);
        assertEquals(d2.abs(), d1);
        assertNotEquals(d1, d2);
    }

    // named differently to avoid Java thinking it's this class's toString method
    @Test
    public void to_string(){
        Duration d = new Duration("00:00:01.12345678");
        assertEquals("00:00:01", d.toString(0));
        assertEquals("00:00:01.1", d.toString(1));
        assertEquals("00:00:01.12", d.toString(2));
        assertEquals("00:00:01.1234568", d.toString(7));
        assertEquals("00:00:01.12345678", d.toString(8));
        assertEquals("00:00:01.12345678", d.toString(9));

        Duration d2 = new Duration("00:59:59.8");
        assertEquals("01:00:00", d2.toString(0));
        assertEquals("00:59:59.800", d2.toString(3));

        Duration d3 = new Duration("23:59:59.99");
        assertEquals("1T00:00:00", d3.toString(0));
    }

    @Test
    public void format(){
        Duration d1 = new Duration("3T12:45:11.012777");
        assertEquals("3 days 12 hours 45 minutes 11 seconds and 013 milliseconds", d1.format("d 'days' HH 'hours' mm 'minutes' ss 'seconds and' S 'milliseconds'"));

        Duration d2 = new Duration("00:45:11.009");
        assertEquals("45M11S009ms", d2.format("mm'M'ss'S'SSSSSS'ms'"));
        assertEquals("0 days 0 hours 45 minutes 11 seconds and 009 milliseconds", d2.format("d 'days' HH 'hours' mm 'minutes' ss 'seconds and' S 'milliseconds'"));
    }

    @Test
    public void valueOf() {
        Duration d1 = new Duration("00:00:00");
        d1.valueOf("001T00:00:00");
        assertEquals(Duration.ONE_DAY, d1.getTics());
        d1.valueOf("1T00:00:01");
        assertEquals(Duration.ONE_DAY + Duration.ONE_SECOND, d1.getTics());
        d1.valueOf("00:00:01.005");
        assertEquals(Duration.ONE_SECOND + 500000, d1.getTics());
    }

    @Test
    public void fromMatcher() {
        String s1 = "00:35:12";
        Matcher durationMatcher = durationPattern.matcher(s1);
        assertEquals(true, durationMatcher.find());

        Duration d1 = Duration.fromMatcher(durationMatcher);
        assertEquals(35, d1.getMinutes());
        assertEquals(2112, d1.getSeconds());
    }

    @Test
    public void fromTics(){
        Duration d = Duration.fromTics(ONE_SECOND);
        assertEquals(SECOND_DURATION, d);
        Duration d2 = Duration.fromTics(50000000);
        assertEquals(d2.toString(3), "00:00:00.500");
    }

    @Test
    public void fromMilliseconds(){
        Duration d = Duration.fromMilliseconds(50);
        assertEquals(d.toString(3), "00:00:00.050");
        Duration d2 = Duration.fromMilliseconds(1500.0);
        assertEquals(new Duration("00:00:01.5"), d2);
    }

    @Test
    public void round() {
        Duration d1 = new Duration("10:05:12.123");
        Duration roundedD1 = d1.round(new Duration("00:05:00"));
        Duration expectedD1 = new Duration("10:05:00");
        assertEquals(roundedD1, expectedD1);

        Duration d2 = new Duration("00:11:00");
        Duration roundedD2 = d2.round(new Duration("00:07:00"));
        Duration expectedD2 = new Duration("00:14:00");
        assertEquals(roundedD2, expectedD2);
    }

    @Test
    public void ceil() {
        Duration d1 = new Duration("10:05:12.123");
        Duration roundedD1 = d1.ceil(new Duration("00:05:00"));
        Duration expectedD1 = new Duration("10:10:00");
        assertEquals(roundedD1, expectedD1);

        Duration d2 = new Duration("00:11:00");
        Duration roundedD2 = d2.ceil(new Duration("00:07:00"));
        Duration expectedD2 = new Duration("00:14:00");
        assertEquals(roundedD2, expectedD2);
    }

    @Test
    public void floor() {
        Duration d1 = new Duration("10:05:12.123");
        Duration roundedD1 = d1.floor(new Duration("00:05:00"));
        Duration expectedD1 = new Duration("10:05:00");
        assertEquals(roundedD1, expectedD1);

        Duration d2 = new Duration("00:11:00");
        Duration roundedD2 = d2.floor(new Duration("00:07:00"));
        Duration expectedD2 = new Duration("00:07:00");
        assertEquals(roundedD2, expectedD2);
    }

    @Test
    public void min(){
        Duration d1 = new Duration("00:00:10");
        Duration d2 = new Duration("00:00:20");
        Duration d3 = new Duration("00:00:30");
        Duration d4 = new Duration("-00:00:05");

        assertEquals(new Duration("00:00:10"), Duration.min(d1, d2));
        assertEquals(new Duration("00:00:10"), Duration.min(d1, d2, d3));
        assertEquals(new Duration("-00:00:05"), Duration.min(d1, d2, d3, d4));
        assertEquals(new Duration("00:00:10"), Duration.min(d1));
        try{
            Duration.max();
            fail();
        }
        catch(NoSuchElementException e){
        }
    }

    @Test
    public void max(){
        Duration d1 = new Duration("00:00:10");
        Duration d2 = new Duration("00:00:20");
        Duration d3 = new Duration("00:00:30");
        Duration d4 = new Duration("00:00:05");

        assertEquals(new Duration("00:00:20"), Duration.max(d1, d2));
        assertEquals(new Duration("00:00:30"), Duration.max(d1, d2, d3, d4));
        assertEquals(new Duration("00:00:30"), Duration.max(d3, d2, d1));
        assertEquals(new Duration("00:00:10"), Duration.max(d1));
        try{
            Duration.max();
            fail();
        }
        catch(NoSuchElementException e){
        }
    }

    @Test
    public void compareTo() {
        Duration d1 = new Duration("00:00:10");
        Duration d2 = new Duration("00:00:10");
        Duration d3 = new Duration("00:00:05");
        Duration d4 = new Duration("00:00:15");

        assertEquals(-1, d1.compareTo(d4));
        assertEquals(0, d1.compareTo(d2));
        assertEquals(1, d2.compareTo(d3));
    }
}