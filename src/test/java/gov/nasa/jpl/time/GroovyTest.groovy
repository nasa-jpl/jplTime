package gov.nasa.jpl.time

import org.junit.Test
import static org.junit.Assert.*

@Newify([Time, Duration]) class GroovyTest {
    @Test
    void testOverload(){
        Time t1 = Time('2020-180T00:00:00')
        Time t2 = Time('2020-200T00:00:00')
        Time t3 = Time('2020-220T00:00:00')

        Duration d1 = Duration('1T00:00:00')
        Duration d2 = Duration('00:01:00')

        assertEquals(Time('2020-181T00:00:00'), t1 + d1)
        assertEquals(Time('2020-179T00:00:00'), t1 - d1)
        assertEquals(Duration('20T00:00:00'), t2 - t1)
        assertEquals(Time('2020-240T00:00:00'), t3 - t1 + t2)
        assertTrue(t1 < t2)
        assertTrue(t3 > t2)
        assertTrue(t1 == t1)

        assertEquals(Duration('1T00:01:00'), d1 + d2)
        assertEquals(Duration('23:59:00'), d1 - d2)
        assertEquals(Duration('2T00:00:00'), d1 * 2)
        assertEquals(Duration('1T12:00:00'), d1 * 1.5)
        assertEquals(Duration('12:00:00'), d1 / 2)
        assertEquals(1440, d1 / d2, 0.00001)

    }
}
