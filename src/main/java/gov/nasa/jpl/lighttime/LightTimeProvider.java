package gov.nasa.jpl.lighttime;

import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;

/**
 * This interface provides a downleg and upleg duration given a time, the two objects, and which frame the duration
 * should be expressed in. The idea is that information from  SPICE itself, a 'light time file' for SEQGEN, or Monte
 * can all be injected in and provide the values that underlie the time frame conversion functions.
 */
public interface LightTimeProvider {
    /**
     * @param t The time at which the light time is desired
     * @param sc_id The SPICE ID of the spacecraft
     * @param body_id The SPICE ID where one wants the downleg to
     * @param time_reference Either 'ETT', 'ERT', or 'SCET', depending on the desired frame
     * @return The time it takes light to travel from the spacecraft to the body_id
     */
    Duration downleg(Time t, int sc_id, int body_id, String time_reference);

    /**
     * @param t The time at which the light time is desired
     * @param sc_id The SPICE ID of the spacecraft
     * @param body_id The SPICE ID where one wants the downleg to
     * @param time_reference Either 'ETT', 'ERT', or 'SCET', depending on the desired frame
     * @return The time it takes light to travel from the body_id to the spacecraft
     */
    Duration upleg  (Time t, int sc_id, int body_id, String time_reference);
}
