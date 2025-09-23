package gov.nasa.jpl.lighttime;

import gov.nasa.jpl.time.Duration;
import gov.nasa.jpl.time.Time;
import spice.basic.CSPICE;
import spice.basic.SpiceErrorException;

public class SPKLightTimeProvider implements LightTimeProvider{

    /**
     * Gets the upleg duration to a spacecraft represented by sc_id from a body represented by body_id at the specified Time t. The Spice
     * calculation of the duration is different depending on your time_reference frame, though they are usually very close.
     * @param t
     * @param sc_id
     * @param body_id
     * @param time_reference
     * @return A new Duration object
     */
    public Duration upleg(Time t, int sc_id, int body_id, String time_reference){
        if(time_reference.equals("SCET")){
            return Duration.fromSeconds(getLightTime(t.toET(), sc_id, "<-", body_id));
        }
        else if(time_reference.equals("ETT") || time_reference.equals("ERT")){
            return Duration.fromSeconds(getLightTime(t.toET(), body_id, "->", sc_id));
        }
        else{
            throw new RuntimeException("Error calculating upleg with input time_reference " + time_reference + ". This value must be either SCET, ERT, or ETT.");
        }
    }

    /**
     * Gets the downleg duration from a spacecraft represented by sc_id to a body represented by body_id at the specified Time t. The Spice
     * calculation of the duration is different depending on your time_reference frame, though they are usually very close.
     * @param t
     * @param sc_id
     * @param body_id
     * @param time_reference
     * @return A new Duration object
     */
    public Duration downleg(Time t, int sc_id, int body_id, String time_reference){
        if(time_reference.equals("SCET")){
            return Duration.fromSeconds(getLightTime(t.toET(), sc_id, "->", body_id));
        }
        else if(time_reference.equals("ETT") || time_reference.equals("ERT")){
            return Duration.fromSeconds(getLightTime(t.toET(), body_id, "<-", sc_id));
        }
        else{
            throw new RuntimeException("Error calculating downleg with input time_reference " + time_reference + ". This value must be either SCET, ERT, or ETT.");
        }
    }

    // returns one-way light time in seconds - basically just wraps CSPICE.ltime()
    private static double getLightTime(double et, int observerID, String arrow, int targetID){
        double[] ettarg = new double[1];
        double[] elapsd = new double[1];
        try {
            CSPICE.ltime(et, observerID, arrow, targetID, ettarg, elapsd);
        } catch (SpiceErrorException e) {
            throw new RuntimeException("Error getting lighttime to " + targetID + " from " + observerID + " at et: " + et + " .\n SPICE needs to be initialized and have the proper kernels to perform calculations. See full information:\n" + e.getMessage());
        }
        return elapsd[0];
    }
}
