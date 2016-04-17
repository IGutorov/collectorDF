package com.epam.common.igLib;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.epam.common.igLib.LibFilesNew.*;

public final class RunBatFile extends Thread {

    // ??
    private static class defaultLogger implements ILogger {

        private static Logger log = Logger.getLogger(defaultLogger.class.getName());
        
        @Override
        public void saveLog(String messsage, Exception exception) {
            log.log(Level.SEVERE, messsage, exception);
        }        
    }

    private final ILogger    logger; // ??
    private final String     batFileName;
    private final String     fileText;
    private final long       sleepTime;

    public RunBatFile(String batFileName, String fileText) {
        this(null, batFileName, fileText, 0);
    }

    public RunBatFile(ILogger logger, String batFileName, String fileText, long sleepTime) {
        if (logger == null)
            this.logger = new defaultLogger();
        else
            this.logger = logger;
        this.batFileName = batFileName;
        this.fileText = fileText;
        this.sleepTime = sleepTime;
    }
    
    public RunBatFile(ILogger logger, String batFileName, String fileText) {
        this(logger, batFileName, fileText, 0);
    }

    @Override
    public void run() {
        try {
            saveText2File(batFileName, fileText, WIN_CHARSET);
            Runtime.getRuntime().exec(batFileName);
            if (sleepTime > 0)
                sleep(sleepTime);
        } catch (IOException e) {
            logger.saveLog("Error runtime IO start Excel", e);
        } catch (InterruptedException e) {
            logger.saveLog("Error runtime IR start Excel", e);
        } finally {
            try {
                saveText2File(batFileName, "");
            } catch (IOException e) {
                logger.saveLog("Error runtime IO del bat-file", e);
            }
        }
    }
}
