package model.logic.reservationsLogic;

import model.log.Log;
import model.models.Student;

import java.util.ArrayDeque;
import java.util.Queue;

public class QueueForHighPowerLaptops implements QueueForHighPowerLaptopsInterface {
    private Queue<Student> queue;
    private Log log;

    public QueueForHighPowerLaptops(){
        queue = new ArrayDeque<>();
        log = Log.getInstance();
    }


    @Override
    public int getHighNeedingQueueSize() {
        return queue.size();
    }

    @Override
    public Student getNextInLineForHighPerformance() {
        return queue.peek();
    }

    @Override
    public Student getAndRemoveNextInLineForHighPerformance() {
        return queue.poll();
    }

    public void addToHighPerformanceQueue(Student student){
        queue.offer(student);
    }
}
