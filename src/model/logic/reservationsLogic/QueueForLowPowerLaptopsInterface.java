package model.logic.reservationsLogic;

import model.models.Student;

public interface QueueForLowPowerLaptopsInterface {
    int getLowNeedingQueueSize();
    Student getNextInLineForLowPerformance();
    Student getAndRemoveNextInLineForLowPerformance();
    void addToLowPerformanceQueue(Student student);
}
