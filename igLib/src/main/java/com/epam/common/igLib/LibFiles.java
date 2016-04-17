package com.epam.common.igLib;

import java.awt.Image;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public final class LibFiles {

    private LibFiles() {
    }
/*
    public static final String WIN_CHARSET = "Cp1251";

    private static final String   DEFAULT_CHARSET = WIN_CHARSET; // "Cp866" // "UTF-8"
    private static final int      SIZE_BLOCK_READ = 10000;
    private static final String[] SPACE_LIST      = { "" };

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    // Поблочное чтение файла InputStreamReader.read(char cbuf[], int offset, int length)
    // Reads characters into a portion of an array.
    private static String readInputStream(final InputStreamReader inputStream) throws IOException {
        int readedSize = 0;
        char[] charBuffer = new char[SIZE_BLOCK_READ];
        StringBuilder sb = new StringBuilder(SIZE_BLOCK_READ);
        do {
            readedSize = inputStream.read(charBuffer, 0, SIZE_BLOCK_READ);
            if (readedSize > 0)
                sb.append(charBuffer, 0, readedSize);
        } while (readedSize == SIZE_BLOCK_READ);
        return sb.toString();
    }

    public static String getStringFromFile(final String fileName, final String charsetName)
            throws UnsupportedEncodingException, FileNotFoundException, IOException {
        return getStringFromFile(new File(fileName), charsetName);
    }

    public static String getStringFromFile(final String fileName)
            throws UnsupportedEncodingException, FileNotFoundException, IOException {
        return getStringFromFile(new File(fileName), null);
    }

    public static String getStringFromFile(final File file)
            throws UnsupportedEncodingException, FileNotFoundException, IOException {
        return getStringFromFile(file, null);
    }

    public static String getStringFromFile(final File file, String charsetName)
            throws UnsupportedEncodingException, FileNotFoundException, IOException {
        if (charsetName == null || charsetName.isEmpty())
            charsetName = DEFAULT_CHARSET;

        InputStreamReader inputStream = null;
        try {
            inputStream = new InputStreamReader(new FileInputStream(file), charsetName);
            return readInputStream(inputStream);
        } finally {
            if (inputStream != null)
                inputStream.close();
        }
    }

    public static String getStringFromFileL(final File file) throws IOException {
        return getStringFromFileL(file, DEFAULT_CHARSET);
    }

    public static String getStringFromFileL(final String fileName) throws IOException {
        return getStringFromFileL(new File(fileName));
    }

    // Построчное чтение файла Scanner.nextLine()
    // (с автозаменой любых line.separator на локальный)
    public static String getStringFromFileL(final File file, String charsetName) throws IOException {

        if (charsetName == null || charsetName.isEmpty())
            charsetName = DEFAULT_CHARSET;

        if (!file.canRead())
            throw new IOException("Не удалось открыть файл: " + file.getAbsolutePath());

        Scanner in = null;
        StringBuilder resultBuffer = new StringBuilder();

        try {
            in = new Scanner(file, charsetName);
            while (in.hasNext())
                resultBuffer.append(in.nextLine()).append(LINE_SEPARATOR);
        } catch (Exception e) {
            throw new IOException("Не удалось прочитать файл: " + file.getAbsolutePath(), e);
        } finally {
            if (in != null)
                in.close();
        }
        return resultBuffer.toString();
    }

    public static Image loadIcon(final File file) throws IOException {
        return (Image) ImageIO.read(file);
    }

    public static Image loadIcon(final String fileName) throws IOException {
        return loadIcon(new File(fileName));
    }

    public static Image loadResourceIcon(final String fileName) throws IOException {
        return loadIcon("resources" + File.separator + fileName);
    }

    public static ImageIcon getImageIcon(final String imageFileName) throws IOException {
        Image image = loadResourceIcon(imageFileName);
        if (image != null)
            return new ImageIcon(image);
        else
            return null;
    }

    public static void saveText2File(final String fileName, final String text) throws IOException {
        saveText2File(fileName, text, DEFAULT_CHARSET);
    }

    public static void saveText2File(final String fileName, final String text, final String charset) throws IOException {
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
            if (charset == null)
                osw = new OutputStreamWriter(new FileOutputStream(file)); // default ? (1251)
            else
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

    public static String getCurrentFolder() {
        final File getFilePath = new File("getFilePath.txt");
        final String strFileWithPath = getFilePath.getAbsolutePath();
        return strFileWithPath.substring(0, strFileWithPath.length() - getFilePath.getName().length() - 1);
    }

    public static String getExtensionFile(final String fileNameWithExt) {
        final StringBuilder reverseStr = (new StringBuilder(fileNameWithExt)).reverse();
        final int lenExtension = reverseStr.indexOf(".");
        if (lenExtension < 1)
            return "";
        return (new StringBuilder(reverseStr.substring(0, lenExtension))).reverse().toString();
    }

    public static String[] getFileListByExt(final String folderName, final String fileExtFilter) {
        final File[] fileList = (new File(folderName)).listFiles();
        if (fileList == null || fileList.length == 0)
            return SPACE_LIST.clone();

        String[] strFileList = new String[fileList.length];

        strFileList[0] = "";
        int countList = 1;
        for (File currFile : fileList) {
            String currFileName = currFile.getName();
            if (getExtensionFile(currFileName).equalsIgnoreCase(fileExtFilter))
                strFileList[countList++] = currFileName.substring(0, currFileName.length() - 4);
        }
        return Arrays.copyOf(strFileList, countList);
    }
*/
}
