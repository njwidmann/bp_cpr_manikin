public class DataPoint {

    public long time;
    public int depth;
    public int endTitle;
    public int vents;
    public BPManager.DEPTH_DIRECTION direction;
    public float sbp;
    public float dbp;

    public DataPoint(long time, int depth, int vents) {
        this.time = time;
        this.depth = depth;
        this.vents = vents;
    }

    public DataPoint(long time, int depth, DataPoint dataPoint) {
        this.time = time;
        this.depth = depth;
        this.vents = dataPoint.vents;
        this.endTitle = dataPoint.endTitle;
        this.direction = dataPoint.direction;
        this.sbp = dataPoint.sbp;
        this.dbp = dataPoint.dbp;
    }
}
