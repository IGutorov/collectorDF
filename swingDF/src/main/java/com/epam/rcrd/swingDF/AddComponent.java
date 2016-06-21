package com.epam.rcrd.swingDF;

import java.awt.Color;

import javax.swing.JComboBox;
import javax.swing.JSplitPane;

import com.epam.rcrd.coreDF.IConnectionsSetter;
import com.epam.rcrd.coreDF.IMergerStarter.TypeReconciliation;
import com.epam.rcrd.swingDF.MainTabbedPane;


final class AddComponent {
    
    private AddComponent() {
    }

    static JSplitPane getNewSplitter() {
        JSplitPane newSplit = new JSplitPane();
        newSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
        newSplit.setDividerSize(2);
        newSplit.setEnabled(false);
        return newSplit;
    }

    // @SuppressWarnings({ "unchecked", "rawtypes" }) // annotation for Java 7 or higher
    static JComboBox getJComboBox(String[] list) {
        JComboBox result = new JComboBox(list); // java 6 compatible
        result.setBackground(Color.WHITE);
        return result;
    }

    static void connectionsIsChecked(MainTabbedPane mainTabbedPane, IConnectionsSetter connectionsSetter) throws Exception {
        if (!connectionsSetter.checkBothConnections())
            return;
        createAvaibleMergeTabs(mainTabbedPane, connectionsSetter);
        mainTabbedPane.reDrawAreaConsole();
    }
    
    static void createAvaibleMergeTabs(MainTabbedPane mainTabbedPane, IConnectionsSetter connectionsSetter)
            throws Exception {
        for (TypeReconciliation curr : connectionsSetter.getTypes())
            addOneMergeTab(mainTabbedPane, connectionsSetter, curr);
    }

    static MergeTab addOneMergeTab(MainTabbedPane mainTabbedPane, final IConnectionsSetter connectionsSetter,
            final TypeReconciliation mainType) throws Exception {
        switch (mainType) {
            case Turns:
                return new MergeTabTurn(mainTabbedPane, connectionsSetter);
            case AccountBalance:
                return new MergeTabSaldo(mainTabbedPane, connectionsSetter);
            // case AccountStatement: return new MergeTabAccount(mainTabbedPane, connectionsSetter);
            case Documents:
                return new MergeTabDocs(mainTabbedPane, connectionsSetter);
            default:
                return null;
        }
    }
}
