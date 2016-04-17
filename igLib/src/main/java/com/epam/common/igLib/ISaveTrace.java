package com.epam.common.igLib;

public interface ISaveTrace extends ILogger {

    void saveMessage(String message);
    void saveTraceMessage(String message); 
    void addLetter(String letter);
    void saveException(Exception exception);
    void saveMessageWithException(String message, Exception exception);
    String getTrace();
}
