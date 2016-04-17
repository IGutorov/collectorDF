package com.epam.common.igLib;

public final class TimeProfiler {

    private long startProcess;
    private long startLap;

    public TimeProfiler() {
        start();
    }

    public void start() {
        startProcess = System.currentTimeMillis(); 
        startLap = startProcess; 
    }

    public long getTimeAndNextLap() {
        final long currentTime = System.currentTimeMillis();
        try {
            return currentTime - startLap;
        } finally {
            startLap = currentTime;
        }
    }

    public long getTimeInterval() {
        return System.currentTimeMillis() - startProcess;
    }

}
