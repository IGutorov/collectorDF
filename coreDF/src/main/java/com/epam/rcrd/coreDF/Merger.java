package com.epam.rcrd.coreDF;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import static com.epam.rcrd.coreDF.PackageConsts.*;
import static com.epam.common.igLib.LibDateFormats.*;

import com.epam.common.igLib.IConnectorSQL;
import com.epam.common.igLib.TimeProfiler;
import com.epam.rcrd.coreDF.IConnectionsCore.ICallBack;
import com.epam.rcrd.coreDF.IConnectionsCore.IProgressIndicator;
import com.epam.rcrd.coreDF.IMergerStarterCore.TableDesign;

final class Merger extends Thread {

    private IConnectorSQL          GLConnection;
    private IConnectorSQL          masterConnection;

    private ComparedResultSet      leftCompared;
    private ComparedResultSet      rightCompared;

    private MergeResults           mergeResults;

    private final MergeParams      mergeParams;
    private final Logger           logger;
    private final WaitingGetData   waitData;
    private final ICallBack        callBack;
    private final ConnectionsStats connectionGetter;
    private final boolean          resultSave;
    private final boolean          logSave;

    // ?? summary result sign (move to mergeParams ??)
    // int                      diffCount                  = 0; // количество расхождений (0 - Ок)
    // boolean                  procressIsFailed           = false; // Сверка прервана

    private LoopCounter            loopCounter;

    private ComparedResultSet getComparedResultSet(IConnectorSQL connection, ConnectionSide comparedSide) {
        return ComparedResultSetImpl.getComparedResultSet(connection, comparedSide, mergeParams, logger);
    }

    private void runInner() {

        ResultSaveFile resFile = new ResultSaveFile(mergeParams.getType().getPrefixFileName(), getBeginFolderName());
        try {
            if (logSave)
                resFile.addFileAppender(logger);
        } catch (IOException e1) {
            logger.error("Error add fileAppender", e1);
        }

        leftCompared = getComparedResultSet(GLConnection, ConnectionSide.LEFT);
        rightCompared = getComparedResultSet(masterConnection, ConnectionSide.RIGHT);
        String mainHeader = mergeParams.getMainHeader(GLConnection.getConnectorProperty("ProductVersion") + "("
                + GLConnection.getCurrentDB() + ")", masterConnection.getConnectorProperty("ProductVersion") + "("
                + masterConnection.getCurrentDB() + ")");

        logger.info(mainHeader);

        mergeResults = MergeResultsImpl.getMergeResults(mergeParams.getPairType(),
                (TableModelMerger) mergeParams.getTableModel(TableDesign.DataTable), leftCompared, rightCompared,
                mainHeader);

        logger.info("GL SPID: " + GLConnection.getCurrentSPID());
        logger.info("Master SPID: " + masterConnection.getCurrentSPID());
        logger.info("!! Process started: " + getStrDate108(getNow()));

        TimeProfiler timeProfiler = new TimeProfiler();

        try {
            waitData.start();
            try {
                executeMerge();
            } catch (Exception e) {
                mergeParams.setCompleted(false, e.getMessage()); // ??
                logger.error("Reconsilation process terminated.", e);
            } finally {
                leftCompared.closeResultSet();
                rightCompared.closeResultSet();
            }

            logger.info("Общее время выполнения(сек): " + timeProfiler.getTimeAndNextLap() / 1000);

            mergeParams.uploadDataToModel(mergeResults);

            logger.info("Формирование результата (сек): " + (timeProfiler.getTimeAndNextLap() / 100) / 10.);

            if (resultSave) {
                try {
                    String resFileName = resFile.saveResult(mergeResults.getResultHTML());
                    mergeParams.setAbsoluteFileName(resFileName);
                } catch (Exception e) {
                    logger.error("Error save result", e);
                }
            }
            logger.info("Сохранение результата (мсек): " + timeProfiler.getTimeAndNextLap());

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
            logger.error("Error get connection", e1);
        } finally {
            try {
                if (GLConnection != null)
                    GLConnection.closeConnection();
                if (masterConnection != null)
                    masterConnection.closeConnection();
            } catch (SQLException e) {
                logger.error("Error close connection", e);
            }
        }

    }

    Merger(MergeParams mergeParams, ConnectionsStats connectionGetter, IProgressIndicator progressIndicator,
            Logger logger, ICallBack callBack) {
        super("Merger");
        this.mergeParams = mergeParams;
        this.logger = logger;
        this.callBack = callBack;
        this.waitData = new WaitingGetData(progressIndicator);
        this.connectionGetter = connectionGetter;

        resultSave = !PackageProperties.getProperty("FileResults.skipSave").equalsIgnoreCase("true");
        logSave = !PackageProperties.getProperty("FileLog.skipSave").equalsIgnoreCase("true");
    }

    private void saveNewError(DifferenceType diffType, ComparedResultSet currentCompared) throws Exception {
        if (diffType == DifferenceType.NOT_EXISTS_GL || diffType == DifferenceType.NOT_EXISTS_MASTER)
            if (!mergeParams.checkCutOffTime(currentCompared.getInDateTime()))
                return;

        loopCounter.addError();
        mergeResults.saveDiff(diffType);
        mergeResults.addSummaryOneError(currentCompared.mainCriterion, currentCompared.secondCriterion);
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
        DifferenceType diffType = currentCompared.nextWithCheckKey();
        if (diffType.isError())
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

        TimeProfiler timeProfiler = new TimeProfiler();

        fetchNext(FetchSide.BOTH);

        if (!leftCompared.isFetched && !rightCompared.isFetched) {
            mergeParams.setNonCriticalMessage(); // Terminate with warning.
            logger.info("No data for date");
            throw new Exception("Ошибка выбора параметров. В системах нет данных за выбранную дату.");
        }

        if (!leftCompared.isFetched) {
            saveNewError(DifferenceType.NOT_EXISTS_GL, rightCompared);
            throw new Exception("Error. No data in genreral system (left).");
        }
        if (!rightCompared.isFetched) {
            saveNewError(DifferenceType.NOT_EXISTS_MASTER, leftCompared);
            throw new Exception("Error. No data in master system (right).");
        }

        while ((leftCompared.isFetched || rightCompared.isFetched)) {

            FetchSide fetchSide = FetchSide.getFetchSide(leftCompared.isFetched, rightCompared.isFetched);

            if (fetchSide.isBoth()) {
                fetchSide = FetchSide.getFetchSide(leftCompared.compareFetchKey(rightCompared));
                if (fetchSide.isBoth()) {
                    DifferenceType diffType = leftCompared.compareFetchedValue(rightCompared);
                    if (diffType.isError())
                        saveNewError(diffType, leftCompared);
                }
            }

            if (fetchSide == FetchSide.RIGHT)
                saveNewError(DifferenceType.NOT_EXISTS_GL, rightCompared);
            if (fetchSide == FetchSide.LEFT)
                saveNewError(DifferenceType.NOT_EXISTS_MASTER, leftCompared);

            fetchNext(fetchSide);

            loopCounter.next();
        }

        logger.info("Сверка результатов выполнена за: " + (timeProfiler.getTimeInterval() / 100) / 10.0 + " сек.");
    }

    private String getBeginFolderName() {
        return getStrDate112(mergeParams.getMainDate()) + "_" + masterConnection.getCurrentDB();
    }
}
