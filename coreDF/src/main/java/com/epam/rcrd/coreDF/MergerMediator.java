package com.epam.rcrd.coreDF;

import java.io.File;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.epam.common.igLib.RunBatFile;
import com.epam.rcrd.coreDF.CompareSystem.IOnePairRecType;
import com.epam.rcrd.coreDF.IConnectionsCore.ICallBack;
import com.epam.rcrd.coreDF.IConnectionsCore.IProgressIndicator;

final class MergerMediator implements IMergerStarter {

    static volatile int            logCount;

    private final Logger           logger;

    private ICallBack              callBack;
    private IProgressIndicator     progressIndicator;
    private final ConnectionsStats connectionGetter;
    private final MergeParams      mergeParams;

    private static final long      DEFAULT_PAUSE = 1500;

    private synchronized Logger getNewLogger() {
        return Logger.getLogger("com.epam.MergerNum_" + (++logCount) + "_Log");
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    MergerMediator(IOnePairRecType pairType, ConnectionsStats connectionGetter) throws Exception {
        this.connectionGetter = connectionGetter;
        logger = getNewLogger();
        String logLevel = PackageProperties.getProperty("Logger.level").toUpperCase();
        logger.setLevel(Level.toLevel(logLevel));
        mergeParams = new MergeParams(pairType);
    }

    @Override
    public boolean isAvailableXls() {
        return (mergeParams.getAbsoluteFileName() != null);
    }

    @Override
    public void showXls() {
        String absoluteFileName = mergeParams.getAbsoluteFileName();
        logger.info(absoluteFileName);
        if (absoluteFileName != null && !absoluteFileName.isEmpty()) {
            String batFileName = "resources" + File.separator + "startExcel.bat";
            RunBatFile.startBat(batFileName, "start excel " + absoluteFileName, DEFAULT_PAUSE, logger);
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
            logger.error("Ќе определены об€зательные параметры сверки");
            return;
        }
        try {
            (new Merger(mergeParams, connectionGetter, progressIndicator, logger, callBack)).start();
        } catch (Exception e) {
            logger.error("Merger not started", e);
        }
    }

    @Override
    public void setParam(String param, String value) throws Exception {
        mergeParams.setParam(param, value);
    }

    @Override
    public boolean checkHiddenParam(String paramName) {
        return mergeParams.checkHiddenParam(paramName);
    }
}
