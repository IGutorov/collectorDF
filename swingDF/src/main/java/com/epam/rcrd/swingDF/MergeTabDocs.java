package com.epam.rcrd.swingDF;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import static com.epam.common.igLib.LibFormats.*;
import com.epam.rcrd.coreDF.IConnectionsSetter;
import com.epam.rcrd.coreDF.IMergerStarter.TypeReconciliation;
import static com.epam.rcrd.swingDF.AddComponent.*;

final class MergeTabDocs extends MergeTab {

    private static final String PAGE_NAME = "������ ������ ����������";

    private static final int DEFAULT_LAG = 10;

    private JFormattedTextField calcDateDocs;
    private JFormattedTextField lagTimeField;
    // @SuppressWarnings("rawtypes")  // annotation for Java 7 or higher
    private JComboBox           typeDoc;

    MergeTabDocs(final JTabbedPane mainTabbedPane, final IConnectionsSetter connectionsSetter) throws Exception {
        super(mainTabbedPane, connectionsSetter, TypeReconciliation.Documents);
        typeDoc.setVisible(!mergerStarter.checkHiddenParam("typeDoc"));
    }

    private enum TransportTypeDoc {
        PE, DocAllRur, All;

        static String getDefaultValue() {
            return All.name();
        }
    }

    @Override
    protected String getPageName() {
        return PAGE_NAME;
    }

    @Override
    protected void addParamsComponentsOnPanel(final JPanel jPanelParams) {
        String defaultLagTime = PackageProperties.getProperty("Default.lagTime");
        if (defaultLagTime == null || defaultLagTime.isEmpty())
            defaultLagTime = "10";
        try {
            lagTimeField = getJFormTextField("##", '0', defaultLagTime);
            calcDateDocs = getJFormDateField(getStrDate104(getToday()));
        } catch (Exception e) {
            saveTrace.saveException(e);
        }
        typeDoc = getJComboBox(getStringList(TransportTypeDoc.values()));
        typeDoc.setSelectedItem(TransportTypeDoc.getDefaultValue());
        jPanelParams.add(new JLabel("���� : "));
        jPanelParams.add(calcDateDocs);
        jPanelParams.add(typeDoc);
        jPanelParams.add(new JLabel("Lag time(min):"));
        jPanelParams.add(lagTimeField);
    }

    private int getLagTime() {
        if (!lagTimeField.isEditValid())
            return DEFAULT_LAG;
        try {
            return Integer.parseInt((String) lagTimeField.getValue());
        } catch (NumberFormatException e) {
            saveTrace.saveMessageWithException("lagTime NumberFormatException ", e);
            return 0;
        }
    }

    @Override
    protected void setParams() {
        setCalcDateParam("" + calcDateDocs.getValue());
        mergerStarter.setParam("transportTypeDoc", "" + typeDoc.getSelectedItem());
        mergerStarter.setParam("lagTime", "" + getLagTime());
    }

    @Override
    protected void disableParams() {
        calcDateDocs.setEnabled(false);
        lagTimeField.setEnabled(false);
        typeDoc.setEnabled(false);
    }

}