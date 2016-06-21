package com.epam.common.igLib;

import java.io.IOException;

import org.apache.log4j.Logger;

import static com.epam.common.igLib.LibFiles.*;

public final class RunBatFile extends Thread {

    private final Logger logger;

    private final String  batFileName;
    private final String  fileText;
    private final long    sleepTime;

    public static void startBat(String batFileName, String fileText, long sleepTime, Logger logger) {
        if (logger == null) {
            logger = CustomLogger.getDefaultLogger();
        }
        // 1) Ёто "небезопасный" метод (concurrency).
        // –есурс (файл) общий дл€ всех пользователей (и всех инстансов) приложени€. 
        // 2) Ёто "небезопасный" метод (security). «апуск исполн€емого bat-файла.
        (new RunBatFile(batFileName, fileText, sleepTime, logger)).start();
    }

    private RunBatFile(String batFileName, String fileText, long sleepTime, Logger logger) {
        super("BatRunner");
        this.batFileName = getOuterResourceAbsolutePath(batFileName);        
        this.fileText = fileText;
        this.sleepTime = sleepTime;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            saveText2File(batFileName, fileText, WIN_CHARSET);
            Runtime.getRuntime().exec(batFileName);
            if (sleepTime > 0)
                sleep(sleepTime);
        } catch (IOException e) {
            logger.error("Error runtime IO start Excel", e);
        } catch (InterruptedException e) {
            logger.error("Error runtime IR start Excel", e);
        } finally {
            try {
                saveText2File(batFileName, "");
            } catch (IOException e) {
                logger.error("Error runtime IO del bat-file", e);
            }
        }
    }
}
