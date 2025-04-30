package model.models;

import model.enums.PerformanceTypeEnum;
import model.log.Log;
import model.logic.reservationsLogic.ReservationManager;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.UUID;

public class Laptop implements UnnamedPropertyChangeSubject {
    private final UUID id;
    private String brand;
    private String model;
    private int gigabyte;
    private int ram;
    private final PerformanceTypeEnum performanceType;
    // private Student loanedBy;
    private LaptopState theState;
    private final PropertyChangeSupport support;

    public Laptop(String brand, String model, int gigabyte, int ram, PerformanceTypeEnum performanceType, ReservationManager reservationManager){
        this.brand = brand;
        this.model = model;
        this.gigabyte = gigabyte;
        this.ram = ram;
        this.performanceType = performanceType;
        // loanedBy = null;
        theState = new AvailableState();
        id = UUID.randomUUID();
        support = new PropertyChangeSupport(this);
        this.addListener(reservationManager);
    }

    // Getters og setters er her

    public UUID getId(){
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getGigabyte() {
        return gigabyte;
    }

    public void setGigabyte(int gigabyte) {
        this.gigabyte = gigabyte;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public String toString(){
        return "Laptop id: " + id + " with the following specs: " + performanceType + " " +brand + " " + model + " " + gigabyte + " " + ram;
    }


    /*public Student getLoanedBy() {
        return loanedBy;
    }*/

    /*public void setLoanedBy(Student loanedBy) {
        this.loanedBy = loanedBy;
    }*/

    public PerformanceTypeEnum getPerformanceType(){
        return performanceType;
    }


    // Koden angående laptop state

    public LaptopState getState() {
        return theState;
    }

    public boolean isAvailable() {
        return theState instanceof AvailableState;
    }

    public boolean isLoaned(){
        return theState instanceof LoanedState;
    }


    public void changeState(LaptopState newState){
        LaptopState oldState = theState;
        theState = newState;
        if (newState instanceof AvailableState){
            support.firePropertyChange("toAvailableState", oldState , theState);
            Log.getInstance().addToLog("Laptop [ID: " + id + ", " + brand + " " + model + "] skiftet til " + newState.getClass().getSimpleName() + ".");
        } else if (newState instanceof LoanedState) {
            Log.getInstance().addToLog("Laptop [ID: " + id + ", " + brand + " " + model + "] skiftet til " + newState.getClass().getSimpleName() + ".");        }
    }



    // Observer mønster kode


    @Override
    public void addListener(PropertyChangeListener listener){
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener){
        support.removePropertyChangeListener(listener);
    }
}
