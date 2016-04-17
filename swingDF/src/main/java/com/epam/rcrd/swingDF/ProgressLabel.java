package com.epam.rcrd.swingDF;

import javax.swing.JLabel;

import com.epam.rcrd.coreDF.IConnectionsCore.IProgressIndicator;

final class ProgressLabel extends JLabel implements IProgressIndicator {

    private static final long serialVersionUID = 1L;

    private static final int MAX_COUNTER = 100;

    private int count;

    ProgressLabel() {
        super("");
        StartProcess();
    }

    @Override
    public void NextStep() {
        int smallCount = (count++ % (2 * MAX_COUNTER));
        if (smallCount >= MAX_COUNTER)
            smallCount = 2 * MAX_COUNTER - smallCount;
        this.setText("" + smallCount);
    }

    @Override
    public void StartProcess() {
        count = 0;
        this.setText("");
        this.setVisible(true);        
    }

    @Override
    public void FinishProcess() {
        this.setText("");
        this.setVisible(false);        
    }

}
