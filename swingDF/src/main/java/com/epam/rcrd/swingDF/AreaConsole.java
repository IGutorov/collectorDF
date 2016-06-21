package com.epam.rcrd.swingDF;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.epam.common.igLib.CustomLogger;
import com.epam.rcrd.swingDF.DeltafactMainForm.AreaConsoleCallBack;

class AreaConsole extends OutputStream {

    private static final Logger logger = CustomLogger.getDefaultLogger();

    private final OutputStream stream;
    private final Object identObject;
    private final AreaConsoleCallBack callback;

    AreaConsole(Object identObject, AreaConsoleCallBack callback, String initMessage) {
        this.identObject = identObject;
        this.callback = callback;
        this.stream = new ByteArrayOutputStream();
        if (initMessage != null) {
            try {
                stream.write(initMessage.getBytes());
            } catch (IOException e) {
                logger.error("AreaConsole stream not init", e);
            }
        }
    }

    @Override
    public String toString() {
        return stream.toString();
    }

    private void updatedStream() {
        callback.stackUpdated(identObject);
    }
    
    @Override
    public void write(int b) throws IOException {
        stream.write(b);
        updatedStream();
    }

    @Override
    public void write(byte b[]) throws IOException {
        stream.write(b);
        updatedStream();
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        stream.write(b, off, len);
        updatedStream();
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
        updatedStream();
    }

    @Override
    public void close() throws IOException {
        updatedStream();
        stream.close();
    }
}
