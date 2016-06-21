package com.epam.rcrd.swingDF;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;

import com.epam.common.igLib.CustomLogger;
import com.epam.rcrd.swingDF.DeltafactMainForm.AreaConsoleCallBack;

import java.awt.Component;
import java.io.OutputStream;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Appender;

final class MainTabbedPane extends JTabbedPane implements AreaConsoleCallBack {

    private static final Logger            logger           = CustomLogger.getDefaultLogger();

    private final Map<Object, AreaConsole> map              = new HashMap<Object, AreaConsole>();

    //    private final OutputStream os;
    private static final long              serialVersionUID = 1L;
    private final Appender                 defaultAppender;
    private final JTextComponent           jAreaConsole;

    public MainTabbedPane(JTextComponent jAreaConsole) {
        super(JTabbedPane.NORTH, JTabbedPane.WRAP_TAB_LAYOUT);
        this.jAreaConsole = jAreaConsole;
        defaultAppender = CustomLogger.getAppender(addAreaConsole(null));
        logger.addAppender(defaultAppender);
        CustomLogger.removeDummyAppender();

        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
                reDrawAreaConsole();
            }
        });
    }

    private OutputStream addAreaConsole(Object identObject) {
        AreaConsole parent = map.get(null);
        String init = (parent == null) ? null : parent.toString();
        AreaConsole result = new AreaConsole(identObject, this, init);
        map.put(identObject, result);
        return result;
    }

    void reDrawAreaConsole() {
        AreaConsole ac = map.get(getSelectedComponent());
        if (ac != null) {
            jAreaConsole.setText(ac.toString());
        }
    }

    @Override
    public void stackUpdated(Object identObject) {
        if (getSelectedComponent() == identObject) {
            reDrawAreaConsole();
        }
    }

    public void add(String title, Component component, Logger mergeLogger) {
        super.add(title, component);
        mergeLogger.addAppender(CustomLogger.getAppender(addAreaConsole(component)));
        logger.removeAppender(defaultAppender);
    }

}
