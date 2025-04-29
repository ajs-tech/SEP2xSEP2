package model.log;

public class LogLine {
    private String text;
    private DateAndTime time;

    public LogLine(String text){
        this.text = text;
        time = new DateAndTime();
    }

    public String getText(){
        return text;
    }

    public String getTime(){
        return time.toString();
    }

    public String toString(){
        return text + " >> " + time.toString();
    }
}
