package model.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Log {
    private static Log instance;
    private static Object lock = new Object();
    private ArrayList<LogLine> logLines;
    private String path = "src/model/log/loglines.txt";

    private Log(){
        logLines = new ArrayList<>();
        createFile();
    }

    public static Log getInstance(){
        if (instance == null){
            synchronized (lock){
                if (instance == null){
                    instance = new Log();
                }
            }
        }
        return instance;
    }

    // getters

    public int getLoglineSize(){
        return logLines.size();
    }

    public LogLine getLastAddedLogline(){
        return logLines.getLast();
    }

    // Setters

    private void createFile(){
        File file = new File(path);
        if (file.getParentFile() != null){
            file.getParentFile().mkdirs();
        }

        try {
            if (file.createNewFile()){
                System.out.println("File created. Path at: " + file.getAbsolutePath());
            } else System.out.println("File already exists. Path at: " + file.getAbsolutePath());
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println(e.toString());
            throw new RuntimeException(e);
        }
    }

    public void addToLog(String text){
        LogLine logLine = new LogLine(text);
        logLines.add(logLine);
        addToFile(logLine);
    }

    private void addToFile(LogLine logLine){
        try(PrintWriter out = new PrintWriter(new FileWriter(path, true))) {
            out.println(logLine.toString());
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
