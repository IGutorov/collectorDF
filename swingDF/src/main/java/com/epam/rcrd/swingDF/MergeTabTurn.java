package com.epam.rcrd.swingDF;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import static com.epam.common.igLib.LibFormats.*;
import com.epam.rcrd.coreDF.IConnectionsSetter;
import com.epam.rcrd.coreDF.IMergerStarter.TypeReconciliation;

final class MergeTabTurn extends MergeTab {

    private static final String PAGE_NAME = "Реконсиляция оборотов";

    private JFormattedTextField calcDateTurn;

    MergeTabTurn(MainTabbedPane mainTabbedPane, IConnectionsSetter connectionsSetter) throws Exception {
        super(mainTabbedPane, connectionsSetter, TypeReconciliation.Turns);
    }

    @Override
    protected String getPageName() {
        return PAGE_NAME;
    }

    @Override
    protected void addParamsComponentsOnPanel(JPanel jPanelParams) {
        try {
            calcDateTurn = getJFormDateField(getStrDate104(getCurrentDateWithShift(-15))); // после 15:00 - сегодня, до - вчера
        } catch (Exception e) {
            logger.error("format failed", e);
        }
        jPanelParams.add(new JLabel("Дата : "));
        jPanelParams.add(calcDateTurn);
    }

    @Override
    protected void setParams() {
        setCalcDateParam("" + calcDateTurn.getValue());
    }

    @Override
    protected void disableParams() {
        calcDateTurn.setEnabled(false);
    }
}
