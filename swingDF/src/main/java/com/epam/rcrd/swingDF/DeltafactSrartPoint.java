package com.epam.rcrd.swingDF;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.epam.common.igLib.CustomLogger;

public final class DeltafactSrartPoint implements Runnable {

    private static final Logger logger = CustomLogger.getDefaultLogger();

    private DeltafactSrartPoint() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        SwingUtilities.invokeLater(this);
    }

    @Override
    public void run() {
        try {
            new DeltafactMainForm();
        } catch (Exception e) {
            logger.error("SWT form error ", e);
        }
    }

    public static void NewGUIFrame() {
        // for old start-method : com.epam.rcrd.deltafact6.DeltafactStartPoint.main
        new DeltafactSrartPoint();
    }

    public static void main(String[] args) {
        new DeltafactSrartPoint();
    }
}
