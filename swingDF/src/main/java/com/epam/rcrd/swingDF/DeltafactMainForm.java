package com.epam.rcrd.swingDF;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import com.epam.common.igLib.CustomLogger;

import static com.epam.common.igLib.LibFiles.*;

import com.epam.rcrd.coreDF.IConnectionsSetter;
import static com.epam.rcrd.coreDF.IConnectionsCore.NumberConnection.*;
import com.epam.rcrd.coreDF.StartDF;

import static com.epam.rcrd.swingDF.AddComponent.*;

final class DeltafactMainForm {

    private static final String DEFAULT_APP_TITLE = "Deltafact";

    private static final Logger logger            = CustomLogger.getDefaultLogger();

    interface AreaConsoleCallBack {
        void stackUpdated(Object identObject);
    }

    DeltafactMainForm() {

        JTextArea jAreaConsole = new JTextArea("");
        MainTabbedPane mainTabbedPane = new MainTabbedPane(jAreaConsole);
        IConnectionsSetter connectionsSetter = StartDF.getConnectionsSetter();
        final JFrame frame = new JFrame(getDefaultStr("Application.title", DEFAULT_APP_TITLE));

        // icon
        Image icon = null;
        try {
            icon = loadIcon("icon.png");
            if (icon != null)
                frame.setIconImage(icon);
        } catch (IOException e) {
            logger.info("icon.png not loaded.", e);
        }

        // GUI components (created and forgotten)
        final JSplitPane splitConnect1 = getNewSplitter();
        final JSplitPane splitConnect2 = getNewSplitter();
        final JPanel jPanelConsole = new JPanel();
        final JLabel labelVersion = new JLabel("Version : " + getDefaultStr("Application.version", "???"));

        // design (part 1)
        splitConnect1.setTopComponent(new JPanelConnect(FIRST_CONNECTION, connectionsSetter, mainTabbedPane));
        splitConnect1.setBottomComponent(splitConnect2);
        splitConnect2.setTopComponent(new JPanelConnect(SECOND_CONNECTION, connectionsSetter, mainTabbedPane));
        splitConnect2.setBottomComponent(mainTabbedPane);

        // design (part 2)
        // mainTabbedPane.add // runtime

        // design (part 3)
        final JScrollPane jScrollPaneConsole = new JScrollPane(jAreaConsole);
        jScrollPaneConsole.setPreferredSize(new Dimension(500, 70));
        jPanelConsole.add(jScrollPaneConsole);
        jPanelConsole.add(labelVersion, BorderLayout.SOUTH);

        // frame
        frame.setMinimumSize(new Dimension(620, 350));
        frame.setLayout(new BorderLayout());
        frame.add(splitConnect1);
        frame.add(jPanelConsole, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(780, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private String getDefaultStr(final String propertyName, final String defaultValue) {
        String result = PackageProperties.getProperty(propertyName);
        if (!result.isEmpty())
            return result;
        else
            return defaultValue;
    }
}
