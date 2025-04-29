package model.logic.reservationsLogic;

import model.log.Log;
import model.models.Student;

import java.util.ArrayDeque;
import java.util.Queue;

public class QueueForLowPowerLaptops implements QueueForLowPowerLaptopsInterface {
    private Queue<Student> queue;
    private Log log;

    public QueueForLowPowerLaptops(){
        queue = new ArrayDeque<>();
        log = Log.getInstance();
    }


    @Override
    public int getLowNeedingQueueSize() {
        return queue.size();
    }

    @Override
    public Student getNextInLineForLowPerformance() {
        return queue.peek();
    }

    @Override
    public Student getAndRemoveNextInLineForLowPerformance() {
        return queue.poll();
    }

    @Override
    public void addToLowPerformanceQueue(Student student){
        queue.offer(student);
    }
}
