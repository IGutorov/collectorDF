package com.epam.rcrd.swingDF;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import static com.epam.common.igLib.LibFormats.*;

import com.epam.rcrd.coreDF.IConnectionsSetter;
import com.epam.rcrd.coreDF.IMergerStarter.TypeReconciliation;

final class MergeTabAccount extends MergeTab {

    private static final String PAGE_NAME = "Сверка оборотов по счёту за период";

    private static final String SEPARATOR_ACC = "\u2022";

    private JFormattedTextField calcBriefAccount;
    private JFormattedTextField calcBeginDateAccount;
    private JFormattedTextField calcEndDateAccount;

    MergeTabAccount(final MainTabbedPane mainTabbedPane, final IConnectionsSetter connectionsSetter) throws Exception {
        super(mainTabbedPane, connectionsSetter, TypeReconciliation.AccountStatement);
    }

    @Override
    protected String getPageName() {
        return PAGE_NAME;
    }

    @Override
    protected void addParamsComponentsOnPanel(final JPanel jPanelParams) {
        try {
            calcBriefAccount = getJFormTextField("#####" + SEPARATOR_ACC + "###" + SEPARATOR_ACC + "#" + SEPARATOR_ACC
                    + "####" + SEPARATOR_ACC + "#######", '*', null); // 5-3-1-4-7 //
            calcBeginDateAccount = getJFormDateField(getStrDate104(getFirstDatePreviousYear())); // начало прошлого года
            calcEndDateAccount = getJFormDateField(getStrDate104(getYesterdayDate())); // вчера
        } catch (Exception e) {
            logger.error("default params failed", e);
        }

        jPanelParams.add(new JLabel("Обороты по счёту "));
        jPanelParams.add(calcBriefAccount);
        jPanelParams.add(new JLabel(" с: "));
        jPanelParams.add(calcBeginDateAccount);
        jPanelParams.add(new JLabel(" по: "));
        jPanelParams.add(calcEndDateAccount);
    }

    private String getBriefAccount() {
        String result = null;
        if (calcBriefAccount.isEditValid()) {
            String formatAccount = (String) calcBriefAccount.getValue();
            if (formatAccount != null)
                result = formatAccount.replaceAll(SEPARATOR_ACC, "");
        }
        return result;
    }

    @Override
    protected void setParams() {
        try {
            mergerStarter.setParam("startDatePeriod", (String) calcBeginDateAccount.getValue());
            mergerStarter.setParam("endDatePeriod", (String) calcEndDateAccount.getValue()); // as is (dd.MM.yyyy)
            mergerStarter.setParam("account", getBriefAccount()); // as is (dd.MM.yyyy)
        } catch (Exception e) {
            logger.error("setParams failed", e);
        }
    }

    @Override
    protected void disableParams() {
        calcBriefAccount.setEnabled(false);
        calcBeginDateAccount.setEnabled(false);
        calcEndDateAccount.setEnabled(false);
    }

}
