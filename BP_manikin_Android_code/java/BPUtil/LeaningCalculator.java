import java.util.ArrayList;

public class LeaningCalculator {
    private static final int MAX_LOG_SIZE = 5;

    private ArrayList<Integer> leaningDepths;
    private float avgLeaningDepth;

    public LeaningCalculator() {
        leaningDepths = new ArrayList<>();

        avgLeaningDepth = 0;
    }

    /**
     * call this after every compression
     * @param depth leaning depth at the end of the compression
     * @return average leaning depth based on log
     */
    public float registerLeaningDepth(int depth) {
        leaningDepths.add(depth);
        if(leaningDepths.size() > MAX_LOG_SIZE) {
            leaningDepths.remove(0);
        }
        return calculateAverageLeaningDepth();
    }

    /**
     * calculates avg leaning depth based on Log
     * @return average leaning
     */
    private float calculateAverageLeaningDepth() {
        if(leaningDepths.size() < 1) {
            return 0; //We can't average if there are no values
        }
        float totalDepth = 0;
        for(int i = 0; i < leaningDepths.size() - 1; i++) {
            totalDepth += leaningDepths.get(i);
        }
        avgLeaningDepth = totalDepth / (float)(leaningDepths.size());
        return avgLeaningDepth;
    }

    public float adjustSBPForLeaning(float pressure) {
        double leaningPenalty = .0237*Math.pow(Math.log(avgLeaningDepth+1), 1.62);

        return (float)(pressure * (1-leaningPenalty));
    }

    public float adjustDBPForLeaning(float pressure) {
        double leaningPenalty = .0501*Math.pow(Math.log(avgLeaningDepth+1), 1.62);

        return (float)(pressure * (1-leaningPenalty));
    }

    public void refreshLeaning() {
        leaningDepths.clear();
        avgLeaningDepth = 0;
    }

    public int setLeaningToLatestValue() {
        if(leaningDepths.size() == 0) return 0;

        for(int i = 0; i < leaningDepths.size() - 1; i++) {
            leaningDepths.remove(i);
        }

        return leaningDepths.get(0);
    }
}
