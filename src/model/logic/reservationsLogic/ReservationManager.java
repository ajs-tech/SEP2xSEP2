package model.logic.reservationsLogic;

import model.enums.PerformanceTypeEnum;
import model.enums.ReservationStatusEnum;
import model.log.Log;
import model.models.Laptop;
import model.models.Reservation;
import model.models.Student;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

public class ReservationManager implements QueueForLowPowerLaptopsInterface, QueueForHighPowerLaptopsInterface, PropertyChangeListener {
    private final List<Reservation> reservationList;
    private final QueueForLowPowerLaptops lowPerformanceQueue;
    private final QueueForHighPowerLaptops highPerformanceQueue;
    private ReservationFactory reservationFactory;

    public ReservationManager(){
        reservationList = new ArrayList<>();
        reservationFactory = new ReservationFactory();
        lowPerformanceQueue = new QueueForLowPowerLaptops();
        highPerformanceQueue = new QueueForHighPowerLaptops();
    }


    // reservations getters and setters

    public int getAmountOfReservationsToDate(){
        return reservationList.size();
    }

    public int getAmountOfActiveReservations(){
        int count = 0;
        for (Reservation reservation : reservationList){
            if (reservation.getStatus().equals(ReservationStatusEnum.Active)){
                count++;
            }
        }
        return count;
    }

    public int getAmountOfCancelledReservations(){
        int count = 0;
        for (Reservation reservation : reservationList){
            if (reservation.getStatus().equals(ReservationStatusEnum.Cancelled)){
                count++;
            }
        }
        return count;
    }

    public int getAmountOfCompletedReservations(){
        int count = 0;
        for (Reservation reservation : reservationList){
            if (reservation.getStatus().equals(ReservationStatusEnum.Completed)){
                count++;
            }
        }
        return count;
    }

    public Reservation getLastReservationAdded(){
        return reservationList.getLast();
    }

    public ArrayList<Reservation> getAllActiveReservations(){
        ArrayList<Reservation> allActive = new ArrayList<>();
        for (Reservation reservation : reservationList){
            if (reservation.getStatus().equals(ReservationStatusEnum.Active)){
                allActive.add(reservation);
            }
        }
        return allActive;
    }

    public void createReservation(Laptop laptop, Student student){
        Reservation reservationCreated = reservationFactory.createReservation(laptop, student);
        reservationList.add(reservationCreated);
    }

    public void removeReservation(UUID id){
        Reservation reservationForRemoval = null;
        for (Reservation reservation : reservationList){
            if (reservation.getReservationId().equals(id)){
                reservationForRemoval = reservation;
                reservationList.remove(reservation);
                Log.getInstance().addToLog("Reservation fjernet: ID [" + reservation.getReservationId() + "].");
                return;
            }
        }
        System.out.println("Kan ikke finde reservationen! >> removeReservation(UUID id):)");
        // Log handlingen
        model.log.Log.getInstance().addToLog("Reservation fjernet: ID [" + reservationForRemoval.getReservationId() + "].");
    }




    // getters og setters for alt angående queue

    public int getQueueSizeByPerformanceType(PerformanceTypeEnum performanceTypeEnum){
        if (performanceTypeEnum.equals(PerformanceTypeEnum.LOW)) {
            return getLowNeedingQueueSize();
        } else if (performanceTypeEnum.equals(PerformanceTypeEnum.HIGH)) {
           return getHighNeedingQueueSize();
        }
        return 0;
    }

    @Override
    public int getHighNeedingQueueSize() {
       return highPerformanceQueue.getHighNeedingQueueSize();
    }

    @Override
    public Student getNextInLineForHighPerformance() {
        return highPerformanceQueue.getNextInLineForHighPerformance();
    }

    @Override
    public Student getAndRemoveNextInLineForHighPerformance() {
        return highPerformanceQueue.getAndRemoveNextInLineForHighPerformance();
    }

    @Override
    public void addToHighPerformanceQueue(Student student) {
        if (student.getPerformanceNeeded().equals(PerformanceTypeEnum.LOW)){
            addToLowPerformanceQueue(student);
            return;
        }

        highPerformanceQueue.addToHighPerformanceQueue(student);
    }

    @Override
    public int getLowNeedingQueueSize() {
        return lowPerformanceQueue.getLowNeedingQueueSize();
    }

    @Override
    public Student getNextInLineForLowPerformance() {
        return lowPerformanceQueue.getNextInLineForLowPerformance();
    }

    @Override
    public Student getAndRemoveNextInLineForLowPerformance() {
        return lowPerformanceQueue.getAndRemoveNextInLineForLowPerformance();
    }

    @Override
    public void addToLowPerformanceQueue(Student student) {
        if (student.getPerformanceNeeded().equals(PerformanceTypeEnum.HIGH)){
            addToHighPerformanceQueue(student);
            return;
        }

        lowPerformanceQueue.addToLowPerformanceQueue(student);
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Laptop laptop = (Laptop) evt.getSource();
        if ("toAvailableState".equals(evt.getPropertyName()) && getQueueSizeByPerformanceType(laptop.getPerformanceType()) > 0 && laptop.isAvailable()){

            Student nextStudent = null;

            if (PerformanceTypeEnum.LOW.equals(laptop.getPerformanceType())){
                nextStudent = lowPerformanceQueue.getAndRemoveNextInLineForLowPerformance();
            } else if (PerformanceTypeEnum.HIGH.equals(laptop.getPerformanceType())) {
                nextStudent = highPerformanceQueue.getAndRemoveNextInLineForHighPerformance();
            }

            if (nextStudent != null){
                createReservation(laptop, nextStudent);
                Log.getInstance().addToLog("Automatisk reservation oprettet fra køen for student [" + nextStudent.getName() + "]");
            } else {
                Log.getInstance().addToLog("Ingen studerende i kø for laptop [" + laptop.getBrand() + " " + laptop.getModel() + "].");
            }
        } else {
            Log.getInstance().addToLog("Kan ikke oprette en ny automatisk reservation for den fornyligt ledig laptop da if statement i propertyChange fra " + getClass().getSimpleName() + " ikke er gået i gennem!");
        }
    }
}
