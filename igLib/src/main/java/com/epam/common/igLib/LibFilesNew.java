package com.epam.common.igLib;

import java.awt.Image;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

import static com.epam.common.igLib.LibFormatsNew.*;

public final class LibFilesNew {

    public static final String  UTF_CHARSET                 = "UTF-8";
    public static final String  WIN_CHARSET                 = "Cp1251";
    private static final String DEFAULT_CHARSET             = UTF_CHARSET; // "Cp866" // "UTF-8"

    public static final String  LINE_SEPARATOR              = System.getProperty("line.separator");

    private static final String packageAbsolutePath;                                               // constant defined runtime

    private static final String DEFAULT_RESOURCE_DIRIECTORY = "resources";

    private LibFilesNew() {
    }

    private static final int SIZE_BLOCK_READ = 10000;

    // Поблочное чтение файла InputStreamReader.read(char cbuf[], int offset, int length)
    // Reads characters into a portion of an array.
    private static String readInputStream(InputStreamReader inputStreamReader) throws IOException {
        int readedSize = 0;
        char[] charBuffer = new char[SIZE_BLOCK_READ];
        StringBuilder sb = new StringBuilder(SIZE_BLOCK_READ);
        do {
            readedSize = inputStreamReader.read(charBuffer, 0, SIZE_BLOCK_READ);
            if (readedSize > 0)
                sb.append(charBuffer, 0, readedSize);
        } while (readedSize == SIZE_BLOCK_READ);
        return sb.toString();
    }

    public static String getResourceAsString(InputStream inputStream) throws IOException {
        return getResourceAsString(inputStream, null);
    }

    public static String getResourceAsString(InputStream inputStream, String charsetName) throws IOException {
        if (charsetName == null || charsetName.isEmpty())
            charsetName = DEFAULT_CHARSET;

        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, charsetName);
            return readInputStream(inputStreamReader);
        } finally {
            if (inputStream != null)
                inputStream.close();
        }
    }

    public static InputStream getInputStreamByName(String resourceName, Class<?> className) throws IOException {
        InputStream result = className.getClassLoader().getResourceAsStream(resourceName);
        if (result == null)
            throw new FileNotFoundException("Resource not found: " + resourceName);
        return result;
    }

    public static InputStream getInputStreamByName(String resourceName) throws IOException {
        return getInputStreamByName(resourceName, LibFilesNew.class);
    }

/*    
    public static File getInnerResourceAsFile(String resourceName) throws IOException {
        URL url = LibFilesNew.class.getClassLoader().getResource(resourceName);
        if (url == null)
            System.out.println("getInnerResourceAsFile.resourceName = " + resourceName);
        else
            System.out.println("url.getPath() = " + url.getPath());
        
       // logger.profile ??
        return (url == null) ? null : new File(url.getFile());
    }

    public static File getOuterResourceAsFile(String fileName) throws IOException {
        return getOuterResourceAsFile(fileName, null);
    }

    public static File getOuterResourceAsFile(String fileName, String relativePath) throws IOException {
        String outerFileName = getFullFileName(fileName, relativePath);
        return new File(outerFileName);
    }
*/
    public static InputStream getInnerResource(String resourceName) throws IOException {
        return getInputStreamByName(resourceName);
    }

    public static InputStream getOuterResource(String fileName) throws IOException {
        return getOuterResource(fileName, null);
    }

    public static InputStream getOuterResource(String fileName, String relativePath) throws IOException {
        String outerFileName = getFullFileName(fileName, relativePath);
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

    public static String[] getStringsFromResource(String resourceName) throws IOException {
        InputStream inputStream = getInnerResource(resourceName);
        String result = getResourceAsString(inputStream);
        return result.split(LINE_SEPARATOR);
    }
    
    public static String[] getOuterResourcesListByExt(String subFolder, String fileExtFilter) {
        
        String outerFileName = getFullFileName(subFolder, null);
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

/*
    public static String[] getResourcesListByExt(String subFolderName, String fileExtFilter) throws IOException {
        File dirOuter = null;        
        File dirInner = getInnerResourceAsFile(subFolderName);
        try {
            dirOuter = getOuterResourceAsFile(subFolderName);
        } catch (IOException e) {
            // logger.profile ??
        }
        String[] innerStringArray = getResourcesListByExt(dirInner, fileExtFilter);
        String[] outerStringArray = getResourcesListByExt(dirOuter, fileExtFilter);
        return concatDisticnctArrayString(innerStringArray, outerStringArray);
    }

    private static String[] getResourcesListByExt(File dir, String fileExtFilter) {
        System.out.println("getResourcesListByExt started");
        if (dir == null)
            return EMPTY_STRING_ARRAY;
        File[] fileList = dir.listFiles();
        System.out.println("dir = " + dir.getAbsolutePath() + " dir.listFiles() = " + dir.listFiles());
        if (fileList == null || fileList.length == 0)
            return EMPTY_STRING_ARRAY;
        if (fileExtFilter == null || fileExtFilter.isEmpty())
            return EMPTY_STRING_ARRAY;

        System.out.println("fileList.length = " + fileList.length);

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
*/
    private static String getFullFileName(String fileName, String relativePath) {
        if (relativePath == null)
            relativePath = DEFAULT_RESOURCE_DIRIECTORY;

        StringBuilder fullName = new StringBuilder(packageAbsolutePath);
        if (relativePath != null && !relativePath.isEmpty()) {
            if (relativePath.substring(0, 1).equals(File.separator) && relativePath.length() > 1)
                relativePath = relativePath.substring(1);
            if (!relativePath.equals(File.separator))
                fullName.append(File.separator).append(relativePath);
        }
        fullName.append(File.separator).append(fileName);
        
        return fullName.toString();
    }

    public static InputStream getResource(String resourceName) throws IOException {
        return getResource(resourceName, null);
    }

    public static InputStream getResource(String resourceName, String relativePath) throws IOException {
        InputStream result = null;
        try {
            result = getOuterResource(resourceName, relativePath);
        } catch (IOException e) {            
            // logger.profile ??
        }
        return (result != null) ? result : getInnerResource(resourceName);
    }

    public static Image loadResourceIcon(String resourceName) throws IOException {
        return loadIcon(resourceName, null);
    }

    public static Image loadIcon(String resourceName, String relativePath) throws IOException {
        return (Image) ImageIO.read(getResource(resourceName, relativePath));
    }

    public static Image loadIcon(String resourceName) throws IOException {
        return loadIcon(resourceName, null);
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
                throw new IOException("remove file is not complete");

        // если текста нет, то нового файла не создаём, только удаляем файл
        if (text == null || text.isEmpty())
            return;

        OutputStreamWriter osw = null;
        Writer out = null;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(file), charset);
            out = new BufferedWriter(osw);
            out.write(text);
        } finally {
            if (out != null)
                out.close();
            if (osw != null)
                osw.close();
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
            // ?? logger.profile("Outer resource " + path + " not found."
        }
        return innerProperties;
    }

    static {
        // (new File(LibFilesNew.class.getProtectionDomain().getCodeSource().getLocation().getPath())).getParent();
        ProtectionDomain pd = LibFilesNew.class.getProtectionDomain();
        CodeSource cs = pd.getCodeSource();
        URL url = cs.getLocation();
        String jarPath = url.getPath();
        File jarFile = new File(jarPath);
        packageAbsolutePath = jarFile.getParent();
        // ?? logger.profile ("jarDirectory = " + packageAbsolutePath
    }

}
