// class to store measurement results during MP
public class MeasurementResults {
    String type;    //rtt or tput
    int msgSize;    //num. bytes in payload
    int probeNum; // number of probes that will occur in total
    long[] measurements;     //collection of all measurements for this type+size
    int failedProbes;   //number of probes that do not get responses

    public MeasurementResults(int numMeasurements, String type, int msgSize) {
        measurements = new long[numMeasurements];
        probeNum = numMeasurements;
        this.type = type;
        this.msgSize = msgSize;
        failedProbes = 0;
    }
}