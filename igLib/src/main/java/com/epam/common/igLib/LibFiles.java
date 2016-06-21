package com.epam.common.igLib;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import static com.epam.common.igLib.LibFormats.*;

public final class LibFiles {

    private static final Logger logger                      = CustomLogger.getDefaultLogger();

    public static final String  UTF_CHARSET                 = "UTF-8";
    public static final String  WIN_CHARSET                 = "Cp1251";
    public static final String  IBM_CHARSET                 = "Cp866";
    private static final String DEFAULT_CHARSET             = UTF_CHARSET;

    public static final String  LINE_SEPARATOR              = System.getProperty("line.separator");

    private static final String packageAbsolutePath;                                               // constant defined runtime

    private static final String DEFAULT_RESOURCE_DIRIECTORY = "resources";

    private LibFiles() {
    }

    private static final int SIZE_BLOCK_READ = 8192;

    // Поблочное чтение файла InputStreamReader.read(char cbuf[], int offset, int length)
    // Reads characters into a portion of an array.
    private static String readInputStream(BufferedReader inputStreamReader) throws IOException {
        int readedSize = 0;
        char[] charBuffer = new char[SIZE_BLOCK_READ];
        StringBuilder sb = new StringBuilder(SIZE_BLOCK_READ);
        do {
            readedSize = inputStreamReader.read(charBuffer, 0, SIZE_BLOCK_READ);
            if (readedSize > 0)
                sb.append(charBuffer, 0, readedSize);
        } while (readedSize == SIZE_BLOCK_READ);
        inputStreamReader.close();
        return sb.toString();
    }

    public static String getResourceAsString(InputStream inputStream) throws IOException {
        return getResourceAsString(inputStream, null);
    }

    public static String getResourceAsString(InputStream inputStream, String charsetName) throws IOException {
        if (charsetName == null || charsetName.isEmpty())
            charsetName = DEFAULT_CHARSET;

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charsetName), SIZE_BLOCK_READ);
            return readInputStream(bufferedReader);
        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
        }
    }

    public static InputStream getInputStreamByName(String resourceName, Class<?> className) throws IOException {
        InputStream result = className.getClassLoader().getResourceAsStream(resourceName);
        if (result == null)
            throw new FileNotFoundException("Resource not found: " + resourceName);
        return result;
    }

    public static InputStream getInputStreamByName(String resourceName) throws IOException {
        return getInputStreamByName(resourceName, LibFiles.class);
    }

    public static InputStream getInnerResource(String resourceName) throws IOException {
        return getInputStreamByName(resourceName);
    }

    public static InputStream getOuterResource(String fileName) throws IOException {
        String outerFileName = getFullFileName(fileName);
        File file = new File(outerFileName);
        if (!file.exists())
            throw new FileNotFoundException("File <" + outerFileName + "> not found.");
        return new FileInputStream(file);
    }

    public static String getExtensionFile(final String fileNameWithExt) {
        final StringBuilder reverseStr = (new StringBuilder(fileNameWithExt)).reverse();
        final int lenExtension = reverseStr.indexOf(".");
        if (lenExtension < 1)
            return "";
        return (new StringBuilder(reverseStr.substring(0, lenExtension))).reverse().toString();
    }

    public static String[] getStringsFromResource(String resourceName, String subFolder) throws IOException {
        InputStream inputStream = getInnerResource(subFolder + "/" + resourceName);
        String result = getResourceAsString(inputStream);
        return result.split(LINE_SEPARATOR);
    }

    public static String[] getOuterResourcesListByExt(String subFolder, String fileExtFilter) {

        String outerFileName = getFullFileName(subFolder);
        File dir = new File(outerFileName);
        File[] fileList = dir.listFiles();
        if (fileList == null || fileList.length == 0)
            return EMPTY_STRING_ARRAY;
        if (fileExtFilter == null || fileExtFilter.isEmpty())
            return EMPTY_STRING_ARRAY;

        String[] strFileList = new String[fileList.length + 1];
        strFileList[0] = "";
        int countList = 1;
        int lenExt = fileExtFilter.length() + 1;
        for (File currFile : fileList) {
            String currFileName = currFile.getName();
            if (getExtensionFile(currFileName).equalsIgnoreCase(fileExtFilter))
                strFileList[countList++] = currFileName.substring(0, currFileName.length() - lenExt);
        }
        return Arrays.copyOf(strFileList, countList);
    }

    public static File getSubFolder(File in, String folderName) {
        String absolutePath = (in == null) ? null : in.getAbsolutePath();
        return new File(absolutePath + File.separator + folderName);
    }

    public static File getParentFolderIfExists(File in) {
        File res = (in != null) ? in.getParentFile() : null;
        return (res == null) ? in : res;
    }

    public static String getOuterResourceAbsolutePath(String fileName) {
        return (new File(packageAbsolutePath + File.separator + fileName)).getAbsolutePath();
    }

    public static File getPackageFolder() {
        return new File(packageAbsolutePath);
    }

    private static String getFullFileName(String fileName) {
        StringBuilder fullName = new StringBuilder(packageAbsolutePath);
        fullName.append(File.separator).append(DEFAULT_RESOURCE_DIRIECTORY);
        fullName.append(File.separator).append(fileName);
        return fullName.toString();
    }

    public static InputStream getResource(String resourceName, String subFolder) throws IOException {
        return getResource(subFolder + "/" + resourceName);
    }

    private static InputStream getResource(String resourceName) throws IOException {
        InputStream result = null;
        try {
            result = getOuterResource(resourceName);
        } catch (IOException e) {
            logger.debug("Outer resource " + resourceName + " not loaded.", e);
        }
        return (result != null) ? result : getInnerResource(resourceName);
    }

    public static Image loadIcon(String resourceName) throws IOException {
        return (Image) ImageIO.read(getResource(resourceName));
    }

    public static void saveData(File file, String data) throws Exception {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(data);
        } finally {
            if (fileWriter != null)
                fileWriter.close();
        }
    }

    public static void saveText2File(final String fileName, final String text) throws IOException {
        saveText2File(fileName, text, WIN_CHARSET);
    }

    public static void saveText2File(final String fileName, final String text, final String charset) throws IOException {
        if (charset == null || charset.isEmpty())
            throw new IOException("Charset not defined");

        File file = new File(fileName);

        if (file.exists())
            if (!file.delete())
                throw new IOException("Remove file is not completed.");

        // only delete for empty text
        if (text == null || text.isEmpty())
            return;

        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
            writer.write(text);
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    public static Properties getResourceProperties(String resourceName) throws IOException {
        Properties innerProperties = new Properties();
        Properties outerProperties = new Properties();
        innerProperties.load(getInnerResource(resourceName));
        try {
            outerProperties.load(getOuterResource(resourceName));
            for (Object key : outerProperties.keySet())
                innerProperties.setProperty((String) key, outerProperties.getProperty((String) key));
        } catch (IOException e) {
            logger.debug("Outer resource " + resourceName + " not loaded.", e);
        }
        return innerProperties;
    }

    static {
        // (new File(LibFiles.class.getProtectionDomain().getCodeSource().getLocation().getPath())).getParent();
        ProtectionDomain pd = LibFiles.class.getProtectionDomain();
        CodeSource cs = pd.getCodeSource();
        URL url = cs.getLocation();
        String jarPath = url.getPath();
        File jarFile = new File(jarPath);
        packageAbsolutePath = jarFile.getParent();
        logger.debug("packageAbsolutePath = " + packageAbsolutePath);
    }

}
