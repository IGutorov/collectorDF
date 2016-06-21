package com.epam.rcrd.coreDF;

import org.apache.log4j.Logger;

import com.epam.common.igLib.CustomLogger;
import com.epam.rcrd.coreDF.IConnectionsCore.IProgressIndicator;

final class WaitingGetData extends Thread {

    private static final Logger logger = CustomLogger.getDefaultLogger();
    
    private volatile boolean         processFinished;
    private final IProgressIndicator indicator;

    void terminate() {
        processFinished = true;
    }

    @Override
    public void run() {
        processFinished = false;
        try {
            indicator.StartProcess();
            int count = 0;
            while (!processFinished) {
                count++;
                if (count % 10 == 0)
                    indicator.NextStep();
                try {
                    sleep(150);
                } catch (InterruptedException e) {
                    logger.error("Error waiting monitor", e);
                }
            }
        } finally {
            indicator.FinishProcess();
        }
    }

    WaitingGetData(IProgressIndicator progressIndicator) {
        if (progressIndicator == null) {
            progressIndicator = new IProgressIndicator() {
                // default methods allow Java 8
                @Override
                public void StartProcess() {
                }

                @Override
                public void NextStep() {
                }

                @Override
                public void FinishProcess() {
                }
            };
        }
        indicator = progressIndicator;
    }

}
