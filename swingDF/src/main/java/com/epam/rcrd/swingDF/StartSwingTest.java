package com.epam.rcrd.swingDF;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.epam.rcrd.coreDF.StartCoreTest;

public class StartSwingTest implements Runnable {

    private String header;
    
    @Override
    public void run() {
        final JFrame frame = new JFrame(header);
        String test = PackageProperties.getProperty("Default.ServersFolder");
        frame.add(new JLabel("test " + test));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public StartSwingTest(String header) {
        this.header = header;
        JFrame.setDefaultLookAndFeelDecorated(true);
        SwingUtilities.invokeLater(this);
    }
    
    public static void main(String[] args) {
        new StartCoreTest("swing called ");
        new StartSwingTest("main");
    }

}
