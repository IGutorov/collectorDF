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
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.epam.common.igLib.ISaveTrace;
import com.epam.common.igLib.MapTraces.IUpdateStack;
import static com.epam.common.igLib.LibFilesNew.*;

import com.epam.rcrd.coreDF.IConnectionsSetter;
import com.epam.rcrd.coreDF.IConnectionsCore.NumberConnection;
import com.epam.rcrd.coreDF.StartDF;
import com.epam.rcrd.swingDF.JPanelConnect.ICheckConnections;
import static com.epam.rcrd.swingDF.AddComponent.*;

final class DeltafactMainForm implements IUpdateStack, ICheckConnections {

    private static final String DEFAULT_APP_TITLE = "Deltafact";

    private final JTabbedPane mainTabbedPane = new JTabbedPane(JTabbedPane.NORTH, JTabbedPane.WRAP_TAB_LAYOUT);
    private final JTextArea   jAreaConsole   = new JTextArea("");

    private final IConnectionsSetter connectionsSetter;

    DeltafactMainForm() throws Exception {
        connectionsSetter = StartDF.getConnectionsSetter();
        ISaveTrace mainTrace = connectionsSetter.registerReDrawer(this);

        final JFrame frame = new JFrame(getDefaultStr("Application.title", DEFAULT_APP_TITLE));

        // icon
        Image icon = null;
        try {
            icon = loadIcon("icon.png");
            if (icon != null)
                frame.setIconImage(icon);
        } catch (IOException e) {
            mainTrace.saveException(e);
        }

        mainTabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
                setCurrentTrace();
            }
        });

        // GUI components (created and forgotten)
        final JSplitPane splitConnect1 = getNewSplitter();
        final JSplitPane splitConnect2 = getNewSplitter();
        final JPanel jPanelConsole = new JPanel();
        final JLabel labelVersion = new JLabel("Version : " + getDefaultStr("Application.version", "???"));

        // design (part 1)
        splitConnect1.setTopComponent(
                new JPanelConnect(NumberConnection.FIRST_CONNECTION, connectionsSetter, mainTrace, this));
        splitConnect1.setBottomComponent(splitConnect2);
        splitConnect2.setTopComponent(
                new JPanelConnect(NumberConnection.SECOND_CONNECTION, connectionsSetter, mainTrace, this));
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

    private void setCurrentTrace() {
        connectionsSetter.setCurrentMergePage(mainTabbedPane.getSelectedComponent());
    }

    private String getDefaultStr(final String propertyName, final String defaultValue) {
        String result = PackageProperties.getProperty(propertyName);
        if (!result.isEmpty())
            return result;
        else
            return defaultValue;
    }

    @Override
    public void uploadStack(String stack) {
        jAreaConsole.setText(stack);
    }

    @Override
    public void addToStack(String addStack) {
        uploadStack(jAreaConsole.getText() + addStack);
    }

    @Override
    public void callBack() throws Exception {
        if (!connectionsSetter.checkBothConnections())
            return;
        addNewMergeTabs(mainTabbedPane, connectionsSetter);
        setCurrentTrace();
    }
}
