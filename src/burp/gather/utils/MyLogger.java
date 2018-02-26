package burp.gather.utils;

import javax.swing.*;

public class MyLogger {
    private static MyLogger instance = new MyLogger();
    private JTextArea logArea = null;
    private MyLogger(){}
    public static MyLogger getInstance(){
        return instance;
    }
    public void setLogArea(JTextArea jTextArea){
        logArea = jTextArea;
    }
    public void logAddLine(String line){
        logArea.append(line+"\n");
        //set log area always show new message
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

}
