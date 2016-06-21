package com.epam.common.igLib;

import java.io.OutputStream;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.WriterAppender;

import static com.epam.common.igLib.LibFiles.*;

public class CustomLogger {

    private static final Logger logger = Logger.getLogger("com.epam");
    private static final String DUMMY_APPENDER = "dummyCL";

    public static Logger getDefaultLogger() {
        return logger;
    }

    static {
        if (!Logger.getRootLogger().getAllAppenders().hasMoreElements()) {
            // dummy
            Properties properties = new Properties();
            properties.put("log4j.debug", "false");
            properties.put("log4j.rootLogger", "INFO, ConsoleAppender"); // INFO // DEBUG
            properties.put("log4j.appender.ConsoleAppender", "org.apache.log4j.ConsoleAppender");
            properties.put("log4j.appender.ConsoleAppender.name", DUMMY_APPENDER);
            properties.put("log4j.appender.ConsoleAppender.encoding", IBM_CHARSET);
            properties.put("log4j.appender.ConsoleAppender.layout", "org.apache.log4j.PatternLayout");
            properties.put("log4j.appender.ConsoleAppender.layout.ConversionPattern",
                    "%d{ISO8601} [%-5p][%-16.16t][%32.32c] - %m%n");
            PropertyConfigurator.configure(properties);
        }
    }

    public static void removeDummyAppender() {
        Logger.getRootLogger().removeAppender(DUMMY_APPENDER);
    }

    public static Appender getAppender(OutputStream outputStream) {
        PatternLayout pl = new PatternLayout("%d{ISO8601} [%-5p][%-16.16t][%32.32c] - %m%n");
        return new WriterAppender(pl, outputStream);
    }
}
