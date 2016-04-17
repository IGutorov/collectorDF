package com.epam.common.igLib;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple "logger" (OutputStream). Several logs one listener. Singleton.
 */
public final class MapTraces {

    // Singleton pattern
    private static final MapTraces instance = new MapTraces();

    private MapTraces() {
    }

    // Command pattern
    public interface IUpdateStack {
        void uploadStack(String stack);

        void addToStack(String addStack);
    }

    // Callback
    private IUpdateStack reDrawer;

    private static final String ON_STRING = "on";

    private class OneTrace implements ISaveTrace {

        private final Object       identObj;
        private final OutputStream currentStream;

        private PrintWriter stackWriter;
        private String      propertyTracelog;

        private OneTrace(Object identObj) {
            this(identObj, (OutputStream) new ByteArrayOutputStream());
            stackWriter = new PrintWriter(currentStream, true);
        }

        private OneTrace(Object identObj, PrintStream printStream) {
            this(identObj, (OutputStream) printStream);
        }

        private OneTrace(Object identObj, OutputStream outputStream) {
            this.identObj = identObj;
            this.currentStream = outputStream;
        }

        private boolean isPrintStream() {
            return currentStream instanceof PrintStream;
        }

        private void changeTrace() {
            preReDraw(identObj, this);
        }

        @Override
        public void saveTraceMessage(String message) {
            if (propertyTracelog == null)
                propertyTracelog = PackageProperties.getProperty("Trace.log");
            if (ON_STRING.equalsIgnoreCase(propertyTracelog))
                saveMessage(message);
        }

        @Override
        public void saveMessage(String message) {
            if (message != null && !message.isEmpty())
                if (isPrintStream())
                    ((PrintStream) currentStream).println(message);
                else {
                    stackWriter.println(message);
                    changeTrace();
                }
        }

        @Override
        public void addLetter(String letter) {
            if (letter != null && !letter.isEmpty())
                if (isPrintStream())
                    ((PrintStream) currentStream).print(letter);
                else {
                    stackWriter.print(letter);
                    preAddedLetter(identObj, letter);
                }
        }

        @Override
        public void saveException(Exception exception) {
            saveMessage(" ");
            saveMessage(" ");
            saveMessage("STACK : ");
            saveMessage(" ");
            if (isPrintStream())
                exception.printStackTrace(((PrintStream) currentStream));
            else {
                exception.printStackTrace(stackWriter);
                changeTrace();
            }
            saveMessage(" ");
        }

        @Override
        public void saveMessageWithException(String message, Exception exception) {
            saveMessage(message);
            saveException(exception);
        }

        @Override
        public String getTrace() {
            if (isPrintStream())
                return "";
            else
                return currentStream.toString();
        }

        @Override
        public void saveLog(String messsage, Exception exception) {
            saveMessageWithException(messsage, exception);
        }
    }

    private OneTrace mainTrace = null;

    private Map<Object, OneTrace> allTraces = new HashMap<Object, OneTrace>();
    private Object                currentIdentObj;

    public static void setCurrentMergePage(Object identObj) {
        if (identObj != null)
            instance.setCurrentMergePageInner(identObj);
    }

    private void setCurrentMergePageInner(Object identObj) {
        if (currentIdentObj != identObj) {
            currentIdentObj = identObj;
            OneTrace trace = allTraces.get(identObj);
            if (trace != null)
                preReDraw(identObj, trace);
        }
    }

    private void preAddedLetter(Object identObj, String addedLetter) {
        if (currentIdentObj == null)
            currentIdentObj = identObj;

        if (currentIdentObj == identObj && reDrawer != null)
            reDrawer.addToStack(addedLetter);
    }

    private void preReDraw(Object identObj, OneTrace trace) {
        if (currentIdentObj == null)
            currentIdentObj = identObj;

        if (currentIdentObj == identObj && reDrawer != null)
            reDrawer.uploadStack(trace.getTrace());
    }

    private void initNewTrace(Object identObj, OneTrace newTrace) {
        allTraces.put(identObj, newTrace);
        if (mainTrace == null)
            mainTrace = newTrace;
        else
            newTrace.saveMessage(mainTrace.getTrace());
        currentIdentObj = identObj;
    }

    public static ISaveTrace getMainTrace() {
        return instance.getMainTraceInner();
    }

    private ISaveTrace getMainTraceInner() {
        if (mainTrace == null)
            addTraceInner(new Object(), System.out);
        return (ISaveTrace) mainTrace;
    }

    public static ISaveTrace addTrace(Object identObj, PrintStream ps) {
        return instance.addTraceInner(identObj, ps);
    }

    private ISaveTrace addTraceInner(Object identObj, PrintStream ps) {
        OneTrace trace = new OneTrace(identObj, ps);
        initNewTrace(identObj, trace);
        return (ISaveTrace) trace;
    }

    public static ISaveTrace addTrace(Object identObj) {
        return instance.addTraceInner(identObj);
    }

    private ISaveTrace addTraceInner(Object identObj) {
        if (identObj == null)
            return getMainTraceInner();
        OneTrace trace = new OneTrace(identObj);
        initNewTrace(identObj, trace);
        return (ISaveTrace) trace;
    }

    public static void registerReDrawer(IUpdateStack reDrawer) {
        instance.reDrawer = reDrawer;
    }
}
