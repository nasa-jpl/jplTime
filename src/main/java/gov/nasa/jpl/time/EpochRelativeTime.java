package gov.nasa.jpl.time;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gov.nasa.jpl.time.Duration.*;

/**
 * This class represents epoch-relative times and inherits from the Time class in the same package in order to
 * inter-operate with it as smoothly as possible. In addition to the number of tics from the absolute base class,
 * this gets two new fields: a string epoch name and a Duration offset from that epoch. The toString() method is overwritten
 * to preserve those quantities in file outputs, but all other output methods are not, since one needs an absolute times
 * to do comparisons or geometric calculations.
 */
public class EpochRelativeTime extends Time {
    //<editor-fold desc="static fields and methods that allow epoch processing">

    private static Map<String, Time> epochs = new HashMap<>();

    /**
     * Sets a new epochs map for all new EpochRelativeTimes (does not change already existing ones)
     * @param epochs Map that new EpochRelativeTimes will look up their string in
     */
    public static void setEpochs(Map<String, Time> epochs){
        EpochRelativeTime.epochs = epochs;
    }

    /**
     * Returns the map of epochs to times.
     * @return
     */
    public static Map<String, Time> getEpochs() {
        return epochs;
    }

    /**
     * Adds a new epoch name, time pair to existing epoch map
     * @param epochName Name of epoch, cannot contain spaces
     * @param toInsert Absolute time
     */
    public static void addEpoch(String epochName, Time toInsert){
        epochs.put(epochName, toInsert);
    }

    /**
     * Removes epoch from map of times that new EpochRelativeTimes can look up string names in
     * @param epochName
     */
    public static void removeEpoch(String epochName){
        epochs.remove(epochName);
    }

    /**
     * Wraps .containsKey() for epoch map
     * @param epochName
     * @return
     */
    public static boolean isEpochDefined(String epochName){
        return epochs.containsKey(epochName);
    }

    /**
     * Returns an absolute time if the string can be interpreted as one, or an epoch-relative time if it cannot
     * @param timeString the time string that should be turned into a Time object
     * @return either an absolute or epoch-relative corresponding Time object
     */
    public static Time getAbsoluteOrRelativeTime(String timeString){
        Time t;
        try{
            t = new Time(timeString);
        }
        catch(RuntimeException e){
            try {
                t = new EpochRelativeTime(timeString);
            }
            catch(RuntimeException ex){
                if(ex.getMessage().contains(" did not match expected form")) {
                    throw new RuntimeException("Time value '" + timeString + "' could not be parsed into either an absolute or relative time");
                }
                else{
                    throw ex;
                }
            }
        }
        return t;
    }

    /**
     * Reads all epochs defined in CVF and adds them to epoch map
     */
    public static void readEpochCVF(String epochFileName) throws IOException{
        String line;
        String nextEpochName = null;

        try (BufferedReader br = new BufferedReader(new FileReader(epochFileName))) {
            while ((line = br.readLine()) != null) {
                String clean_line = line.trim();
                if(clean_line.length() > 0){
                    // epoch name begins with a slash
                    if(nextEpochName == null && clean_line.startsWith("/")){
                        nextEpochName = clean_line.substring(1);
                    }
                    // epoch value begins with "const"
                    else if(clean_line.startsWith("\"const\" ")) {
                        if(nextEpochName!= null){
                            epochs.put(nextEpochName, new Time(clean_line.substring(8)));
                            nextEpochName = null;
                        }
                        else{
                            throw new IOException("Input CVF is not formatted correctly.\nEpoch name must be prefaced by / with a following line containing the epoch ISOD value prefaced by \"const\"");
                        }
                    }
                }

            }
        }
    }

    /**
     * Writes out all epochs defined in memory to file with default header in time order
     * @param epochFileName file name to write to
     */
    public static void writeEpochCVF(String epochFileName) throws IOException{
        writeEpochCVF(epochFileName, epochs.keySet());
    }

    /**
     * Writes out epochs included in epochNamesToWriteOut to epochFileName with default header in time order
     * @param epochFileName
     * @param epochNamesToWriteOut
     */
    public static void writeEpochCVF(String epochFileName, Collection<String> epochNamesToWriteOut) throws IOException{
        String defaultCVFHeader = "DATA_SET_ID = CONTEXT_VARIABLE_FILE;\n";
        writeEpochCVF(epochFileName, epochNamesToWriteOut, defaultCVFHeader);
    }

    /**
     * Writes out epochs included in epochNamesToWriteOut to epochFileName with header provided in time order
     * @param epochFileName
     * @param epochNamesToWriteOut
     * @param header
     */
    public static void writeEpochCVF(String epochFileName, Collection<String> epochNamesToWriteOut, String header) throws IOException{
        writeEpochCVF(epochFileName, epochNamesToWriteOut, header, true);
    }

    /**
     * Writes out epochs included in epochNamesToWriteOut to epochFileName with header provided sorted in time order if sortByTime is true, or alphanumeric by name if sortByTime is false
     * @param epochFileName
     * @param epochNamesToWriteOut
     * @param header
     * @param sortByTime
     */
    public static void writeEpochCVF(String epochFileName, Collection<String> epochNamesToWriteOut, String header, boolean sortByTime) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(epochFileName));
        writer.write(getEpochCVFString(epochNamesToWriteOut, header, sortByTime));
        writer.close();
    }

    static String getEpochCVFString(Collection<String> epochNamesToWriteOut, String header, boolean sortByTime){
        List<Map.Entry<String, Time>> epochEntries = new ArrayList<>();
        for(String epochName : epochNamesToWriteOut){
            if(!epochs.containsKey(epochName)){
                throw new RuntimeException("Asked to write " + epochName + " out to file but no such epoch is defined currently. " +
                        "Currently defined epochs are:\n" + String.join("\n", epochs.keySet()));
            }
            epochEntries.add(new AbstractMap.SimpleImmutableEntry<>(epochName, epochs.get(epochName)));
        }
        if(sortByTime){
            Collections.sort(epochEntries, Map.Entry.comparingByValue());
        }
        else{
            Collections.sort(epochEntries, Map.Entry.comparingByKey());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CCSD3ZF0000100000001NJPL3KS0L015$$MARK$$;\n");
        sb.append(header);
        sb.append("CCSD3RE00000$$MARK$$NJPL3IF0M02300000001;\n");
        sb.append("$$EOH\n\n");
        for(Map.Entry<String, Time> entry : epochEntries){
            sb.append("/" + entry.getKey() + "\n");
            sb.append("\"const\" " + entry.getValue().toUTC(6) + "\n");
            sb.append("\n");
        }
        sb.append("$$EOF\n\n");
        return sb.toString();
    }

    // relative time regex including group names and optional spaces - uses existing Duration regex
    public static String EPOCH_RELATIVE_TIME_REGEX = "(?<epochName>\\w+)\\s*(?<relativeSign>[+-])\\s*(?<offset>" + DURATION_REGEX + ")";
    public static final Pattern EPOCH_RELATIVE_PATTERN = Pattern.compile(EPOCH_RELATIVE_TIME_REGEX);

    //</editor-fold>

    //<editor-fold desc="instance fields that epoch relative times have to allow reading and writing as epoch relative">

    private String epochName;
    private Duration offset;

    //</editor-fold>

    //<editor-fold desc="constructors">

    /**
     * Empty constructor, for use by programs that have to create a blank instance then call valueOf()
     */
    public EpochRelativeTime(){
        super();
        epochName = "";
        offset = ZERO_DURATION;
    }

    /**
     * Most likely standard constructor that takes a single string and mutates the called object to represent it.
     * The epoch name must already be defined in the EpochRelativeTime class before the constructor is called.
     * @param epochPlusOffset A string containing an epoch name plus an offset like 'LAUNCH+00:05:00' or 'TEST_EPOCH - 1T00:00:00.000'
     */
    public EpochRelativeTime(String epochPlusOffset){
        valueOf(epochPlusOffset);
    }

    /**
     * Constructor that takes an epoch string name and a Duration object
     * @param epochName epoch name, must already be in epochs map at time of instantiation
     * @param offset Duration object
     */
    public EpochRelativeTime(String epochName, Duration offset) {
        super(epochs.get(epochName).add(offset));
        this.epochName = epochName;
        this.offset = offset;
    }

    /**
     * Constructor that takes an absolute time and an epoch string and creates an equivalent epoch-relative time
     * relative to the input epoch-string - calculates the offset
     * @param absoluteTime The output time will have the same tics (evaluated time) as this parameter
     * @param epochName The epoch name the output time will be relative to
     */
    public EpochRelativeTime(Time absoluteTime, String epochName){
        super(absoluteTime);
        this.epochName = epochName;
        this.offset = absoluteTime.subtract(epochs.get(epochName));
    }

    //</editor-fold>

    //<editor-fold desc="I/O methods">

    /**
     * Mutates the called object such that its tic count, epoch base, and offset are correct according to the input String
     * Epoch name must already be defined before this method is called.
     * This is the main way that epoch relative times are read in from files or input fields.
     * @param epochPlusOffset
     */
    @Override
    public void valueOf(String epochPlusOffset) {
        Matcher relativeMatcher = EPOCH_RELATIVE_PATTERN.matcher(epochPlusOffset);
        if(relativeMatcher.find()){
            String epochNameLocal = relativeMatcher.group("epochName");
            String sign = relativeMatcher.group("relativeSign");
            String durationString = relativeMatcher.group("offset");

            if(!epochs.containsKey(epochNameLocal)){
                throw new RuntimeException("Error creating epoch relative Time from string " + epochPlusOffset + ". Epoch name " +
                        epochNameLocal + " was not found in map of declared epochs");
            }

            Matcher durationMatcher = durationPattern.matcher(durationString);
            if(!durationMatcher.matches()){
                throw new RuntimeException("Error creating epoch relative Time from string " + epochPlusOffset + ". Offset string " +
                       durationString + " was not a valid duration");
            }

            Duration offsetLocal = new Duration(durationString);
            int coefficient = sign.equals("-") ? -1 : 1;

            // finally set instance fields
            epochName = epochNameLocal;
            offset = offsetLocal.multiply(coefficient);
            tics = epochs.get(epochName).add(offset).tics;
        }
        else{
            throw new RuntimeException("Error creating epoch relative Time from string " + epochPlusOffset + ". String" +
                    " did not match expected form EPOCHNAME+offset");
        }
    }

    /**
     * Overrides Time's toString and calls more detailed toString(int) with Duration's default output precision
     * @return
     */
    @Override
    public String toString(){
        return toString(Duration.getDefaultOutputPrecision());
    }

    /**
     * Instead of writing out some conversion of the tic value like for absolute time, writes out epoch base and offset in a way that can be read back in as relative later
     * @param precision Number of decimal places that are desired to be written
     * @return
     */
    public String toString(int precision) {
        if(offset.greaterThanOrEqualTo(Duration.ZERO_DURATION)){
            return epochName + "+" + offset.toString(precision);
        }
        else{
            return epochName + "-" + offset.abs().toString(precision);
        }
    }

    //</editor-fold>

    //<editor-fold desc="Overridden math methods so you can modify epoch relative times and keep them relative">

    /**
     * Adding a duration to an epoch-relative time creates another epoch-relative time with the same base but summed durations
     * @param d
     * @return
     */
    @Override
    public EpochRelativeTime add(Duration d){
        return new EpochRelativeTime(epochName, offset.plus(d));
    }

    /**
     * Wraps add()
     * @param d
     * @return
     */
    @Override
    public EpochRelativeTime plus(Duration d){
        return add(d);
    }

    /**
     * Subtracting a duration from an epoch-relative time creates another epoch-relative time with the same base but subtracted durations
     * @param d
     * @return
     */
    @Override
    public EpochRelativeTime subtract(Duration d){
        return new EpochRelativeTime(epochName, offset.minus(d));
    }

    /**
     * Wraps subtract()
     * @param d
     * @return
     */
    @Override
    public EpochRelativeTime minus(Duration d){
        return subtract(d);
    }

    //</editor-fold>
}
