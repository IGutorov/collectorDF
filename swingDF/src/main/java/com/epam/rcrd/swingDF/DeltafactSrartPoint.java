package com.epam.rcrd.swingDF;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class DeltafactSrartPoint implements Runnable {

    private DeltafactSrartPoint() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        SwingUtilities.invokeLater(this);
    }

    @Override
    public void run() {
        try {
            new DeltafactMainForm();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void NewGUIFrame() {
        new DeltafactSrartPoint();
    }

    public static void main(String[] args) {
        new DeltafactSrartPoint();
    }
}
