package com.epam.rcrd.coreDF;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import static com.epam.rcrd.coreDF.PackageConsts.*;
import com.epam.common.igLib.ISaveTrace;
import static com.epam.common.igLib.LibFormats.*;
import com.epam.common.igLib.TimeProfiler;
import com.epam.rcrd.coreDF.IConnectionsCore.ICallBack;
import com.epam.rcrd.coreDF.IConnectionsCore.IProgressIndicator;
import com.epam.rcrd.coreDF.IMergerStarter.TableDesign;

final class Merger extends Thread {

    private ReconciliationConnection GLConnection;
    private ReconciliationConnection masterConnection;

    private ComparedResultSet leftCompared;
    private ComparedResultSet rightCompared;

    private MergeResults mergeResults;

    private final MergeParams      mergeParams;
    private final ISaveTrace       saveTrace;
    private final WaitingGetData   waitData;
    private final ICallBack        callBack;
    private final ConnectionsStats connectionGetter;
    private final boolean          resultSave;
    private final boolean          logSave;

    // ?? summary result sign (move to mergeParams ??)
    // int                      diffCount                  = 0; // количество расхождений (0 - Ок)
    // boolean                  procressIsFailed           = false; // Сверка прервана

    private LoopCounter loopCounter;

    interface IRunQuery {
        ResultSet getResultSet(String query) throws SQLException;
        
        void closeStatement() throws SQLException;

        void closeConnection() throws SQLException;
        
        String getKeyValue(ResultSet resultSet, String key) throws Exception;

        String getInterfaceProductName();
    }

    private ComparedResultSet getComparedResultSet(final IRunQuery connection, final ConnectionSide comparedSide) {
        return ComparedResultSetImpl.getComparedResultSet(connection, comparedSide, mergeParams, saveTrace);
    }

    private void runInner() {

        leftCompared = getComparedResultSet((IRunQuery) GLConnection, ConnectionSide.LEFT);
        rightCompared = getComparedResultSet((IRunQuery) masterConnection, ConnectionSide.RIGHT);
        final String mainHeader = mergeParams.getMainHeader(GLConnection.getInterfaceProductName(),
                masterConnection.getInterfaceProductName());

        saveTrace.saveMessage(mainHeader);

        mergeResults = MergeResultsImpl.getMergeResults(mergeParams.getPairType(),
                (TableModelMerger) mergeParams.getTableModel(TableDesign.DataTable), leftCompared, rightCompared,
                mainHeader);

        ResultSaveFile resFile = null;

        if (resultSave || logSave) {
            resFile = new ResultSaveFile(mergeParams.getType().getPrefixFileName(), getBeginFolderName(),
                    getUniqueTailFileName());
        }

        saveTrace.saveMessage("GL SPID: " + GLConnection.getCurrentSPID());
        saveTrace.saveMessage("Master SPID: " + masterConnection.getCurrentSPID());
        saveTrace.saveMessage("!! Process started: " + getStrDate108(new Date()));

        TimeProfiler timeProfiler = new TimeProfiler();

        try {
            waitData.start();

            try {
                executeMerge();
            } catch (Exception e) {
                mergeParams.setCompleted(false, e.getMessage()); // ??
                saveTrace.saveException(e);
            } finally {
                leftCompared.closeResultSet();
                rightCompared.closeResultSet();
            }

            saveTrace.saveMessage("Общее время выполнения(сек): " + timeProfiler.getTimeAndNextLap() / 1000);

            mergeParams.uploadDataToModel(mergeResults);

            saveTrace.saveMessage("Формирование результата (сек): " + (timeProfiler.getTimeAndNextLap() / 100) / 10.);

            if (resFile != null)
                if (resultSave || logSave) {
                    try {
                        resFile.createFolder();
                        if (logSave)
                            resFile.saveLog(saveTrace.getTrace());
                        if (resultSave) {
                            resFile.saveResult(mergeResults.getResultHTML());
                            mergeParams.setAbsoluteFileName(resFile.getResultFileName());
                        }
                    } catch (Exception e) {
                        saveTrace.saveException(e);
                    }
                }
            saveTrace.saveMessage("Сохранение результата (мсек): " + timeProfiler.getTimeAndNextLap());

        } finally {
            waitData.terminate();
        }

        mergeParams.setCompleted(true);

        if (callBack != null)
            callBack.threadCompleted();

    }

    @Override
    public void run() {

        try {
            GLConnection = connectionGetter.getNewGeneralConnection();
            masterConnection = connectionGetter.getNewMasterConnection();
            
            runInner();

        } catch (Exception e1) {
            saveTrace.saveException(e1);
        } finally {
            try {
                if (GLConnection != null)
                    GLConnection.closeConnection();
                if (masterConnection != null)
                    masterConnection.closeConnection();
            } catch (SQLException e) {
                saveTrace.saveException(e);
            }
        }

    }

    Merger(final MergeParams mergeParams, final ConnectionsStats connectionGetter,
            final IProgressIndicator progressIndicator, final ISaveTrace saveTrace, final ICallBack callBack)
                    throws Exception {

        this.mergeParams = mergeParams;
        this.saveTrace = saveTrace;
        this.callBack = callBack;
        this.waitData = new WaitingGetData(progressIndicator);
        this.connectionGetter = connectionGetter;

        resultSave = !PackageProperties.getProperty("FileResults.SkipSave").equalsIgnoreCase("true");
        logSave = !PackageProperties.getProperty("FileLog.SkipSave").equalsIgnoreCase("true");
    }

    private void saveNewError(final int diffType, final ComparedResultSet currentCompared) throws Exception {
        if (diffType == NOT_EXISTS_GL || diffType == NOT_EXISTS_MASTER)
            if (!mergeParams.checkCutOffTime(currentCompared.getInDateTime()))
                return;

        loopCounter.addError();
        mergeResults.saveDiff(diffType);
        mergeResults.addSummaryOneError(currentCompared.mainCriterion, currentCompared.secondCriterion); // ??
    }

    private void execMainSQLQueries() throws Exception {

        leftCompared.start();
        rightCompared.start();
        try {
            leftCompared.join();
            rightCompared.join();
        } catch (InterruptedException e) {
            throw new Exception("Ошибка получения данных с сервера.", e);

        }
        if (!leftCompared.getSuccess())
            throw new Exception("Ошибка выполнения основного(left) запроса.");
        if (!rightCompared.getSuccess())
            throw new Exception("Ошибка выполнения основного(right) запроса.");
    }

    private void fetchNext(final ComparedResultSet currentCompared) throws Exception {
        int diffType = currentCompared.nextWithCheckKey();
        if (diffType != EQUALS)
            saveNewError(diffType, currentCompared);
        if (currentCompared.isFetched)
            mergeResults.addSummaryResult(currentCompared, 1);
    }

    private void fetchNext(final FetchSide fetchSide) throws Exception {
        if (fetchSide.isLeft())
            fetchNext(leftCompared);
        if (fetchSide.isRight())
            fetchNext(rightCompared);
    }

    private void executeMerge() throws Exception {

        loopCounter = new LoopCounter();

        // сохраним время отсечки (cutOffTime)
        mergeParams.setStartTime(GLConnection.getUTCServerTime());

        execMainSQLQueries();

        final TimeProfiler timeProfiler = new TimeProfiler();

        fetchNext(FetchSide.BOTH);

        if (!leftCompared.isFetched && !rightCompared.isFetched) {
            mergeParams.setNonCriticalMessage(); // Terminate with warning.
            throw new Exception("Ошибка выбора параметров. В системах нет данных за выбранную дату.");
        }
        if (!leftCompared.isFetched) {
            throw new Exception("Error. No data GL (left).");
        }
        if (!rightCompared.isFetched) {
            throw new Exception("Error. No data Master (right).");
        }

        while ((leftCompared.isFetched || rightCompared.isFetched)) {

            FetchSide fetchSide = FetchSide.getFetchSide(leftCompared.isFetched, rightCompared.isFetched);

            if (fetchSide.isBoth()) {
                fetchSide = FetchSide.getFetchSide(leftCompared.compareFetchKey(rightCompared));
                if (fetchSide.isBoth()) {
                    int diffType = leftCompared.compareFetchedValue(rightCompared);
                    if (diffType != EQUALS)
                        saveNewError(diffType, leftCompared);
                }
            }

            if (fetchSide == FetchSide.RIGHT)
                saveNewError(NOT_EXISTS_GL, rightCompared);
            if (fetchSide == FetchSide.LEFT)
                saveNewError(NOT_EXISTS_MASTER, leftCompared);

            fetchNext(fetchSide);

            loopCounter.next();
        }

        saveTrace.saveMessage(
                "Сверка результатов выполнена за: " + (timeProfiler.getTimeInterval() / 100) / 10.0 + " сек.");
    }

    private String getBeginFolderName() {
        return getStrDate112(mergeParams.getMainDate()) + "_" + masterConnection.getCurrentDB();
    }

    private String getUniqueTailFileName() {
        String result = "";
        try {
            result = getStrDate108c(GLConnection.getGLServerTime()) + GLConnection.getCurrentSPID();
        } catch (SQLException e) {
            saveTrace.saveException(e);
        }
        return result;
    }
}
