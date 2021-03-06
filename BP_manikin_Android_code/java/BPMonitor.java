import android.util.Log;

import <PACKAGE NAME>.BPUtil.BPManager;
import <PACKAGE NAME>.BPUtil.DataPoint;

import java.util.ArrayList;

public abstract class BPMonitor {

    private static final String TAG = "BloodPressureMonitor";

    private boolean RUNNING = true;

    private ArrayList<DataPoint> tempDataPointLog = new ArrayList<>();
    private long startTime = -1L;
    private long lastLoggedSystemTime;
    private long lastLoggedTime = 0;
    private int lastLoggedDepth;
    private DataPoint lastLoggedDataPoint;

    BPManager bpManager;

    public BPMonitor() {

        bpManager = new BPManager();

        new UpdateThread().start();
    }

    public void update(long currentTime, int currentDepth) {
        if(currentDepth < 0) currentDepth = 0;
        if(currentTime > lastLoggedTime) { //we don't want to log any points out of order. There's a chance this could happen because we are creating straight data on Android
            lastLoggedTime = currentTime;
            lastLoggedDepth = currentDepth;
            if (startTime < 0) {
                startTime = currentTime;
                lastLoggedSystemTime = getCurrentTime();
            }
            tempDataPointLog.add(new DataPoint(currentTime, currentDepth));
        }
    }

    public float getBPM() { return bpManager.getBPM(); }
    public float getAvgDepth() { return bpManager.getAvgAbsoluteDepth(); }
    public float getAvgLeaningDepth() { return bpManager.getAvgLeaningDepth(); }
    public float getSBP() {return bpManager.getSBP(lastLoggedTime);}
    public float getDBP() {return bpManager.getDBP(lastLoggedTime);}
    public int getEndTitle() {
        return bpManager.getEndTitle();
    }

    private long getStartTime() {
        return startTime;
    }

    private long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }

    public void refresh() {
        startTime = -1L;
        bpManager = new BPManager();
    }

    public void release() {
        RUNNING = false;
    }

    private class UpdateThread extends Thread {
        @Override
        public void run() {
            while(RUNNING) {

                if(tempDataPointLog.size() > 0) {
                    try {

                        bpManager.addDataPoint(tempDataPointLog.remove(0));
                        lastLoggedSystemTime = getCurrentTime();

                        if(bpManager.isDataPointLogFull()) {
                            DataPoint dataPoint = bpManager.getNextDataPoint();

                            BPManager.DEPTH_DIRECTION direction = dataPoint.direction;
                            float pressure = bpManager.getPressure(dataPoint);
                            float endTitle = dataPoint.endTitle;
                            if (direction == BPManager.DEPTH_DIRECTION.INCREASING) {
                                Log.i(TAG, "Time = " + dataPoint.time + "; Depth = " + dataPoint.depth + "; Direction = INCREASING; BPM = " + bpManager.getBPM() + "; AvgDepth = " + bpManager.getAvgRelativeDepth() + "; Pressure = " + pressure + "; SBP = " + dataPoint.sbp + "; DBP = " + dataPoint.dbp);
                            } else if (direction == BPManager.DEPTH_DIRECTION.DECREASING) {
                                Log.i(TAG, "Time = " + dataPoint.time + "; Depth = " + dataPoint.depth + "; Direction = DECREASING; BPM = " + bpManager.getBPM() + "; AvgDepth = " + bpManager.getAvgRelativeDepth() + "; Pressure = " + pressure + "; SBP = " + dataPoint.sbp + "; DBP = " + dataPoint.dbp);
                            } else {
                                Log.i(TAG, "Time = " + dataPoint.time + "; Depth = " + dataPoint.depth + "; Direction = STRAIGHT; BPM = " + bpManager.getBPM() + "; AvgDepth = " + bpManager.getAvgRelativeDepth() + "; Pressure = " + pressure + "; SBP = " + dataPoint.sbp + "; DBP = " + dataPoint.dbp);
                            }

                            //make waveform more smooth by plotting intermediate datapoints
                            if(lastLoggedDataPoint != null) {
                                int deltaDepthTotal = dataPoint.depth - lastLoggedDataPoint.depth;
                                if(Math.abs(deltaDepthTotal) > 1) {
                                    int deltaDepth = deltaDepthTotal / Math.abs(deltaDepthTotal);
                                    long deltaTime = dataPoint.time - lastLoggedDataPoint.time;
                                    deltaTime /= Math.abs(deltaDepthTotal);
                                    for (int i = 1; i < Math.abs(deltaDepthTotal); i++) {
                                        long time = lastLoggedDataPoint.time + deltaTime * i;
                                        int depth = lastLoggedDataPoint.depth + deltaDepth * i;
                                        DataPoint intermediateDataPoint = new DataPoint(time, depth, dataPoint);
                                        float intermediatePressure = bpManager.getPressure(intermediateDataPoint);
                                        plotPressure((intermediateDataPoint.time - startTime) / 1000f, intermediatePressure);
                                    }
                                }
                            }
                            //plot everything. We want to plot lastLoggedDepth so that the depth bar graph is real time. Everything else has a slight delay
                            plotAll((dataPoint.time - startTime) / 1000f, lastLoggedDepth, pressure, endTitle);

                            lastLoggedDataPoint = dataPoint;
                        }

                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }

                } else if(getStartTime() > 0 && getCurrentTime() - lastLoggedSystemTime > 100) {
                    //add straight data. arduino only logs on changes so we can assume constant depth if no update
                    update(lastLoggedTime + 100, lastLoggedDepth);
                }
            }
        }
    }

    public abstract void plotDepth(int depth);
    public abstract void plotPressure(float time, float pressure);
    public abstract void plotEndTitle(float time, float endTitle);
    public abstract void plotAll(float time, int depth, float pressure, float endTitle);
}
