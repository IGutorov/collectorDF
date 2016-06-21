package com.epam.rcrd.swingDF;

import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import static com.epam.common.igLib.LibFormats.*;

import com.epam.rcrd.coreDF.IConnectionsSetter;
import com.epam.rcrd.coreDF.IMergerStarter.TypeReconciliation;

final class MergeTabSaldo extends MergeTab {

    private static final String PAGE_NAME = "Реконсиляция остатков";

    private JFormattedTextField calcDateSaldo;
    private JRadioButton        rbBalanceSaldo;
    private JRadioButton        rbVnebalnceSaldo;

    MergeTabSaldo(final MainTabbedPane mainTabbedPane, final IConnectionsSetter connectionsSetter) throws Exception {
        super(mainTabbedPane, connectionsSetter, TypeReconciliation.AccountBalance);
    }

    @Override
    protected String getPageName() {
        return PAGE_NAME;
    }

    @Override
    protected void addParamsComponentsOnPanel(final JPanel jPanelParams) {
        try {
            calcDateSaldo = getJFormDateField(getStrDate104(getToday()));
        } catch (Exception e) {
            logger.error("format failed", e);
        }
        rbBalanceSaldo = new JRadioButton("Баланс(А)");
        rbVnebalnceSaldo = new JRadioButton("Внебаланс(В)");

        ButtonGroup bCalcModeSaldo = new ButtonGroup();
        bCalcModeSaldo.add(rbBalanceSaldo);
        bCalcModeSaldo.add(rbVnebalnceSaldo);
        rbBalanceSaldo.setSelected(true);

        jPanelParams.add(new JLabel("Входящие остатки на : "));
        jPanelParams.add(calcDateSaldo);
        jPanelParams.add(rbBalanceSaldo);
        jPanelParams.add(rbVnebalnceSaldo);
    }

    @Override
    protected void setParams() {
        try {
            mergerStarter.setParam("balanceA", "" + rbBalanceSaldo.isSelected());
        } catch (Exception e) {
            logger.error("setParams failed", e);
        }
        setCalcDateParam("" + calcDateSaldo.getValue());
    }

    @Override
    protected void disableParams() {
        calcDateSaldo.setEnabled(false);
        rbBalanceSaldo.setEnabled(false);
        rbVnebalnceSaldo.setEnabled(false);
    }

}
