package com.epam.rcrd.swingDF;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.epam.common.igLib.CustomLogger;
import com.epam.rcrd.coreDF.IConnectionsSetter;
import com.epam.rcrd.coreDF.IConnectionsCore.NumberConnection;
import com.epam.rcrd.swingDF.MainTabbedPane;

final class JPanelConnect extends JPanel {

    private static final long   serialVersionUID        = 1L;

    private static final String ENCRYPTED_PASSWORD_VIEW = "**********";
    private static final char   PASSWORD_CHAR           = '*';

    private static final Logger logger                  = CustomLogger.getDefaultLogger();

    interface IAfterCheckConnections {
        void connectionsIsChecked() throws Exception;
    }

    private boolean              disabled    = false;
    private final JTextField     login1      = new JTextField("", 10);
    private final JPasswordField password1   = new JPasswordField(10);
    private final JButton        btnConnect1 = new JButton("Connect");

    // @SuppressWarnings("rawtypes") // annotation for Java 7+
    private final JComboBox      aliasName;                           // java 6 compatible

    // @SuppressWarnings("rawtypes") // annotation for Java 7+
    private JComboBox getJComboBox(String[] aliasList) {
        return new JComboBox(aliasList);
    }

    JPanelConnect(final NumberConnection number, final IConnectionsSetter connectionsSetter,
            final MainTabbedPane mainTabbedPane) {

        aliasName = getJComboBox(connectionsSetter.getAliasList());
        aliasName.setBackground(Color.WHITE);
        password1.setEchoChar(PASSWORD_CHAR);

        this.add(new JLabel(number.getSystemName()));
        this.add(aliasName);
        this.add(new JLabel("login: "));
        this.add(login1);
        this.add(new JLabel("password: "));
        this.add(password1);
        this.add(btnConnect1);

        aliasName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (disabled)
                    return;
                clearAll();
                if (getCurrentSystem().isEmpty())
                    return;
                try {
                    login1.setText(connectionsSetter.getAliasLogin(getCurrentSystem()));
                    boolean isEncryptedPassword = connectionsSetter.isCryptedPass(getCurrentSystem());
                    password1.setEnabled(!isEncryptedPassword);
                    if (isEncryptedPassword)
                        password1.setText(ENCRYPTED_PASSWORD_VIEW);
                } catch (Exception e) {
                    logger.error("load alias-properties", e);
                }
            }
        });

        btnConnect1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    if (connectionsSetter.checkSetConnection(number, getCurrentSystem(), login1.getText(), new String(
                            password1.getPassword()))) {
                        logger.info(connectionsSetter.getConnectionOptions(number));
                        disableAll();
                        AddComponent.connectionsIsChecked(mainTabbedPane, connectionsSetter);
                    }
                } catch (Exception e) {
                    logger.error("check connection", e);
                }
            }
        });
    }

    private void disableAll() {
        disabled = true;
        aliasName.setEnabled(false);
        login1.setEnabled(false);
        password1.setEnabled(false);
        btnConnect1.setEnabled(false);
    }

    private void clearAll() {
        login1.setText("");
        password1.setText("");
        password1.setEnabled(true);
    }

    private String getCurrentSystem() {
        return (String) aliasName.getSelectedItem();
    }
}
