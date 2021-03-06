import android.util.Log;
import java.util.ArrayList;

public class BPMCalculator {

    private static final int MAX_LOG_SIZE = 2;
    public static final int DEFAULT_BPM = 70; //if there's only one start time in the log we return default

    private ArrayList<Long> compressionStartTimes;
    private float bpm;

    public BPMCalculator() {
        compressionStartTimes = new ArrayList<>();
        bpm = 0;
    }

    public void registerCompressionStart(long time) {
        compressionStartTimes.add(time);
        if(compressionStartTimes.size() > MAX_LOG_SIZE) {
            compressionStartTimes.remove(0);
        }
    }

    private float calculateAverageCompressionTime() {
        long totalTime = 0L;
        for(int i = 0; i < compressionStartTimes.size() - 1; i++) {
            long time1 = compressionStartTimes.get(i);
            long time2 = compressionStartTimes.get(i + 1);
            totalTime += (time2 - time1);
        }
        return (float) totalTime / (compressionStartTimes.size() - 1);
    }

    public float calculateBPM() {
        if(compressionStartTimes.size() < 2) {
            return DEFAULT_BPM; //we need at least 2 values in the log to know the time of at least 1 compression cycle
        }
        bpm = 60000f / calculateAverageCompressionTime();
        return bpm;
    }

    public void refreshBPM() {
        compressionStartTimes.clear();
        bpm = 0;
    }

    public float adjustPressureForBPM(float pressure) {
        return pressure * getBPMScaleFactor(calculateBPM());
    }

    private float getBPMScaleFactor(float rate) {

        if(rate < 60) {
            return 1.0f/(float)(1+Math.exp(-0.25*(rate-40)));
        } else if(rate < 150) {
            return 1;
        } else { //rate >= 150
            return -0.0025f*rate+1.375f;
        }
    }
}
