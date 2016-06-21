package com.epam.rcrd.coreDF;

import java.io.File;
import java.io.IOException;

import static com.epam.common.igLib.LibFiles.*;
import static com.epam.common.igLib.LibFormats.*;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

final class ResultSaveFile {

    // Result folder: "\..\..\data". It's relative path from file coreDF-0.0.1-SNAPSHOT.jar
    private static final File    DATA_FOLDER;
    private static final boolean EXISTS_DATA_FOLDER;

    static {
        DATA_FOLDER = getSubFolder(getParentFolderIfExists(getParentFolderIfExists(getPackageFolder())), "data");
        EXISTS_DATA_FOLDER = DATA_FOLDER.exists();
    }
    private static final String  FILENAME_EXTENSION = ".html";

    private final String         prefixFileName;
    private final File           resultFolder;

    private boolean              folderCreated;
    private String               resultFileName;

    void addFileAppender(Logger logger) throws IOException {
        String fileName = getFile("log.txt").getAbsolutePath();
        logger.addAppender(new FileAppender(new PatternLayout("%d{ISO8601} [%-5p][%-16.16t][%32.32c] - %m%n"), fileName));
    }

    String saveResult(String data) throws Exception {
        resultFileName = "";
        File file = getFile(prefixFileName + FILENAME_EXTENSION);
        if (file != null) {
            saveData(file, data);
            resultFileName = file.getAbsolutePath();
        }
        return resultFileName;
    }

    private File getFile(String fileName) {
        if (!EXISTS_DATA_FOLDER)
            return null;
        if (!folderCreated)
            folderCreated = resultFolder.mkdir();
        if (!folderCreated)
            return null;
        return new File(resultFolder + File.separator + fileName);
    }

    String getResultFileName() {
        return resultFileName;
    }

    ResultSaveFile(String prefixFileName, String beginFolderName) {
        this.prefixFileName = prefixFileName;
        folderCreated = false;
        resultFolder = new File(DATA_FOLDER.getAbsolutePath() + File.separator + prefixFileName + File.separator
                + beginFolderName + '_' + getGUID());
    }
}
