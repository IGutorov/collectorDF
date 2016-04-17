package com.epam.rcrd.coreDF;

import com.epam.rcrd.coreDF.IConnectionsCore.IProgressIndicator;

final class WaitingGetData extends Thread {

    private boolean                  processFinished;
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
                    // e.printStackTrace("Ошибка мониторинга получения данных.");
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
                public void StartProcess() {}
                @Override
                public void NextStep() {}
                @Override
                public void FinishProcess() {}
            };
        }
        indicator = progressIndicator;
    }

}
