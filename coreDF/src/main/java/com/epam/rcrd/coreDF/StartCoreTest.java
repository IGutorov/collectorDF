package com.epam.rcrd.coreDF;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.epam.common.igLib.LibFilesNew;
import com.epam.common.igLib.LibFormatsNew;

public class StartCoreTest {

    public static String[] getStringsFromFileL(File file, String charsetName) throws IOException {

        if (charsetName == null || charsetName.isEmpty())
            charsetName = "Cp1251"; // ??

//        if (!file.canRead())
//            throw new IOException("Не удалось открыть файл: " + file.getAbsolutePath());

        Scanner in = null;
        List<String> resList = new ArrayList<String>();
        try {
            in = new Scanner(file, charsetName);
            while (in.hasNext())
                resList.add(in.nextLine());
        } catch (Exception e) {
            throw new IOException("Не удалось прочитать файл: " + file.getAbsolutePath(), e);
        } finally {
            if (in != null)
                in.close();
        }
        
        return LibFormatsNew.getStringList(resList.toArray());
    }

    
    public static String getExtensionFile(final String fileNameWithExt) {
        final StringBuilder reverseStr = (new StringBuilder(fileNameWithExt)).reverse();
        final int lenExtension = reverseStr.indexOf(".");
        if (lenExtension < 1)
            return "";
        return (new StringBuilder(reverseStr.substring(0, lenExtension))).reverse().toString();
    }
    
    public void inner1(File f) {
        System.out.println("f.getAbsolutePath() = " + f.getAbsolutePath());
        File[] fileList = f.listFiles();
        
        if (fileList != null && fileList.length > 0) {

            String[] strFileList = new String[fileList.length + 1];

            strFileList[0] = "";
            int countList = 1;
            for (File currFile : fileList) {
                System.out.println("currFile = " + currFile.getAbsolutePath());
                String currFileName = currFile.getName();
                if (getExtensionFile(currFileName).equalsIgnoreCase("xml"))
                    strFileList[countList++] = currFileName.substring(0, currFileName.length() - 4);
            }
            // Arrays.sort(a);
            System.out.println("servers/*.xls ---> " + Arrays.toString(Arrays.copyOf(strFileList, countList)));
        }
    }

    public StartCoreTest() {
        System.out.println("PackageProperties. = " + PackageProperties.getProperty("Default.lagTime"));
        System.out.println("printAllStats : " + PackageProperties.getStats());
        System.out.println(this);

        
        try {
            String myStr = LibFilesNew.getResourceAsString(
                    LibFilesNew.getInnerResource("servers/servers.list")
                    );
            System.out.println("myStr = " + myStr);
            String[] arr4 =  myStr.split(LibFilesNew.LINE_SEPARATOR);
            System.out.println("arr4 = " + Arrays.toString(arr4));
            
            
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
    }

    public StartCoreTest(String param) {
        System.out.println(this + " -> " + param);
    }

    public static void main(String[] args) {
        // ?? del
        new StartCoreTest();
    }
}
