package model.logic.reservationsLogic;

import model.models.Student;

public interface QueueForHighPowerLaptopsInterface {
    int getHighNeedingQueueSize();
    Student getNextInLineForHighPerformance();
    Student getAndRemoveNextInLineForHighPerformance();
    void addToHighPerformanceQueue(Student student);
}
