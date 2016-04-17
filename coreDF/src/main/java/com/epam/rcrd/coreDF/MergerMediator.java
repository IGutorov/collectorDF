package com.epam.rcrd.coreDF;

import java.io.File;

import javax.swing.table.AbstractTableModel;

import com.epam.common.igLib.ISaveTrace;
import com.epam.common.igLib.MapTraces;
import com.epam.common.igLib.RunBatFile;
import com.epam.rcrd.coreDF.CompareSystem.IOnePairRecType;
import com.epam.rcrd.coreDF.IConnectionsCore.ICallBack;
import com.epam.rcrd.coreDF.IConnectionsCore.IProgressIndicator;

final class MergerMediator implements IMergerStarter {

    private ICallBack                  callBack;
    private ISaveTrace                 saveTrace;
    private IProgressIndicator         progressIndicator;
    private final ConnectionsStats connectionGetter;
    private final MergeParams          mergeParams;

    private static final long DEFAULT_PAUSE = 1500L;  
    
    MergerMediator(IOnePairRecType pairType, final ConnectionsStats connectionGetter) throws Exception {
        this.connectionGetter = connectionGetter;
        saveTrace = MapTraces.getMainTrace();        
        mergeParams = new MergeParams(pairType);
    }

    @Override
    public ISaveTrace getNewTrace(Object identTraceObject) {
        saveTrace = MapTraces.addTrace(identTraceObject);
        return saveTrace;
    }

    @Override
    public boolean isAvailableXls() {
        return (mergeParams.getAbsoluteFileName() != null);
    }

    @Override
    public void showXls() {
        String absoluteFileName = mergeParams.getAbsoluteFileName();
        saveTrace.saveMessage(absoluteFileName);
        if (absoluteFileName != null && !absoluteFileName.isEmpty()) {
            // Ёто "небезопасный" метод.
            // –есурс (файл) общий дл€ всех пользователей (и всех инстансов) приложени€. 
            (new RunBatFile(saveTrace, "resources" + File.separator + "startExcel.bat",
                    "start excel " + absoluteFileName, DEFAULT_PAUSE)).start();
        }
    }

    @Override
    public boolean isParamsChecked() {
        return mergeParams.isParamsChecked();
    }

    @Override
    public void registerProgressIndicator(IProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }

    @Override
    public void registerCallBack(ICallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public AbstractTableModel getTableModel(TableDesign tableDesign) {
        return mergeParams.getTableModel(tableDesign);
    }

    @Override
    public Integer[] getColumnSizes(TableDesign tableDesign) {
        return mergeParams.getColumnSizes(tableDesign);
    }

    @Override
    public void mergeGo() {
        if (!isParamsChecked()) {
            saveTrace.saveMessage("Ќе определены об€зательные параметры сверки");            
            return;
        }
        try {
            (new Merger(mergeParams, connectionGetter, progressIndicator, saveTrace, callBack)).start();
        } catch (Exception e) {
            saveTrace.saveException(e);
        }
    }

    @Override
    public void setParam(String param, String value) {
        try {
            mergeParams.setParam(param, value);
        } catch (Exception e) {
            saveTrace.saveException(e);
        }
    }

    @Override
    public boolean checkHiddenParam(String paramName) {
        return mergeParams.checkHiddenParam(paramName);
    }
}
