package com.epam.rcrd.coreDF;

import javax.swing.table.AbstractTableModel;

import com.epam.common.igLib.LegendArrayOfNamedObjects;
import com.epam.rcrd.coreDF.IConnectionsCore.ICallBack;
import com.epam.rcrd.coreDF.IConnectionsCore.IProgressIndicator;

import static com.epam.rcrd.coreDF.PackageConsts.*;
import static com.epam.common.igLib.LibDateFormats.*;

public interface IMergerStarterCore {
/*

IConnectionsSetter connectionsSetter = StartDF.getConnectionsSetter();
connectionsSetter.checkSetConnection(number, getCurrentSystem(), login1.getText(), String.valueOf(password1.getPassword()))
connectionsSetter.checkSetConnection(number, getCurrentSystem(), login1.getText(), String.valueOf(password1.getPassword()))
IMergerStarterExtension mergerStarter = connectionsSetter.getNewMerger(TypeReconciliation  mainType)

org.apache.log4j.Logger logger = mergerStarter.getLogger();

mergerStarter.registerProgressIndicator((IProgressIndicator) progressLabel);
mergerStarter.registerCallBack((com.epam.rcrd.coreDF.IConnectionsCore.ICallBack) this);

mergerStarter.setParam(String param, String value);

AbstractTableModel tableModel = mergerStarter.getTableModel(tableDesign);   // TableDesign.SummaryTable, TableDesign.DataTable

if (mergerStarter.isParamsChecked()) mergerStarter.mergeGo();

*/
    boolean isParamsChecked();

    void registerProgressIndicator(IProgressIndicator progressIndicator);

    void registerCallBack(ICallBack callBack);

    AbstractTableModel getTableModel(TableDesign tableDesign);

    void setParam(String param, String value) throws Exception;

    void mergeGo();

    public enum TableDesign {
        SummaryTable, DataTable, DataClassicTable;
    }

    public enum TypeReconciliation {
        AccountBalance {
            @Override
            LegendArrayOfNamedObjects getLegendDataTypes() {
                return LEGEND_REST;
            }

            @Override
            String getPrefixFileName() {
                return "saldo";
            }

            @Override
            String getFinishMessageTemplate() {
                return "ќстатки в %1$s рассчитаны за: ";
            }

            @Override
            String getMainHeader(MergeParams mergeParams, String GLProductName, String masterProductName) {
                return String.format("–еконсил€ци€ вход€щих остатков на %1$s между системами %2$s и %3$s",
                        getStrDate104(mergeParams.getMainDate()) + "г", GLProductName, masterProductName);
            }
        },
        Turns {
            @Override
            LegendArrayOfNamedObjects getLegendDataTypes() {
                return LEGEND_TURN;
            }

            @Override
            String getPrefixFileName() {
                return "turn";
            }

            @Override
            boolean isAvailableClassic() {
                return true;
            }

            @Override
            String getFinishMessageTemplate() {
                return "ќбороты из %1$s получены за: ";
            }

            @Override
            String getMainHeader(MergeParams mergeParams, String GLProductName, String masterProductName) {
                return String.format("–еконсил€ци€ оборотов за %1$s между системами %2$s и %3$s",
                        getStrDate104(mergeParams.getMainDate()) + "г", GLProductName, masterProductName);
            }
        },
        Documents {
            @Override
            LegendArrayOfNamedObjects getLegendDataTypes() {
                return LEGEND_DOCS;
            }

            @Override
            String getPrefixFileName() {
                return "doc";
            }

            @Override
            String getFinishMessageTemplate() {
                return "ƒокументы из %1$s получены за: ";
            }

            @Override
            String getMainHeader(MergeParams mergeParams, String GLProductName, String masterProductName) {
                return String.format("ќнлайн сверка документов за %1$s между системами %2$s и %3$s",
                        getStrDate104(mergeParams.getMainDate()) + "г", GLProductName, masterProductName);
            }
        },
        AccountStatement {
            @Override
            LegendArrayOfNamedObjects getLegendDataTypes() {
                return null; // not supported
            }

            @Override
            String getPrefixFileName() {
                return "account";
            }

            @Override
            String getFinishMessageTemplate() {
                return "?? %1$s за: ";
            }

            @Override
            String getMainHeader(MergeParams mergeParams, String GLProductName, String masterProductName) {
                return String.format("–еконсил€ци€ оборотов по счЄту %1$s с %2$s по %3$s между системами %4$s и %5$s",
                        mergeParams.getAccount(), getStrDate104(mergeParams.getStartDatePeriod()) + "г",
                        getStrDate104(mergeParams.getEndDatePeriod()) + "г", GLProductName, masterProductName);
            }
        };

        public static TypeReconciliation getTypeByString(String in) {
            if (in == null || in.isEmpty())
                return null;
            if (PARAM_TURN.equalsIgnoreCase(in))
                return Turns;
            if (PARAM_REST.equalsIgnoreCase(in))
                return AccountBalance;
            if (PARAM_ACCS.equalsIgnoreCase(in))
                return AccountStatement;
            if (PARAM_DOCS.equalsIgnoreCase(in))
                return Documents;
            return null;
        }

        abstract LegendArrayOfNamedObjects getLegendDataTypes();

        abstract String getPrefixFileName();

        abstract String getFinishMessageTemplate();

        abstract String getMainHeader(MergeParams mergeParams, String GLProductName, String masterProductName);

        boolean isAvailableClassic() {
            return false;
        };
    }
}
