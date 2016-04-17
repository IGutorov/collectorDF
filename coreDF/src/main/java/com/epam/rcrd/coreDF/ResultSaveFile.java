package com.epam.rcrd.coreDF;

import java.io.File;
import java.io.FileWriter;

final class ResultSaveFile {

    private static final String FILENAME_EXTENSION = ".html";
    private static final String DEFAULT_FOLDER     = ".." + File.separator + "data";

    private final String prefixFileName;
    private final String uniqueTailFileName;
    private final String resultFolder;

    private boolean folderCreated;
    private String  resultFileName;

    private void saveData(final File file, final String data) throws Exception {
        FileWriter fileWriter = null;
        try {
            // file.createNewFile(); // ??
            fileWriter = new FileWriter(file);
            fileWriter.write(data);
        } finally {
            if (fileWriter != null)
                fileWriter.close();
        }
    }

    void saveResult(final String data) throws Exception {
        if (!folderCreated)
            return;
        final File file = new File(
                resultFolder + File.separator + prefixFileName + "_" + uniqueTailFileName + FILENAME_EXTENSION);
        saveData(file, data);
        resultFileName = file.getAbsolutePath();
    }

    void saveLog(final String logText) throws Exception {
        if (!folderCreated)
            return;
        File file = new File(resultFolder + File.separator + "log_" + uniqueTailFileName + ".txt");
        saveData(file, logText);
    }

    void createFolder() {
        folderCreated = (new File(resultFolder)).mkdir();
    }

    String getResultFileName() {
        return resultFileName;
    }

    ResultSaveFile(String prefixFileName, final String beginFolderName, final String uniqueTailFileName,
            String workFolder) {
        if (workFolder == null || workFolder.isEmpty())
            workFolder = DEFAULT_FOLDER;
        this.uniqueTailFileName = uniqueTailFileName;
        this.prefixFileName = prefixFileName;
        folderCreated = false;
        resultFolder = workFolder + File.separator + prefixFileName + File.separator + beginFolderName + '_'
                + uniqueTailFileName;
    }

    ResultSaveFile(String prefixFileName, final String beginFolderName, final String uniqueTailFileName) {
        this(prefixFileName, beginFolderName, uniqueTailFileName, null);
    }
}
