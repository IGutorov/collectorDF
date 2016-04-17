package com.epam.rcrd.swingDF;

import java.awt.Color;

import javax.swing.JComboBox;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import com.epam.rcrd.coreDF.IConnectionsSetter;
import com.epam.rcrd.coreDF.IMergerStarter.TypeReconciliation;

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

    static void addNewMergeTabs(final JTabbedPane mainTabbedPane, final IConnectionsSetter connectionsSetter)
            throws Exception {
        for (TypeReconciliation curr : connectionsSetter.getTypes())
            addNewMergeTab(mainTabbedPane, connectionsSetter, curr);
    }

    static MergeTab addNewMergeTab(final JTabbedPane mainTabbedPane, final IConnectionsSetter connectionsSetter,
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
