package model.models;

import model.enums.PerformanceTypeEnum;
import model.logic.reservationsLogic.ReservationManager;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.UUID;

public class Laptop implements UnnamedPropertyChangeSubject {
    private UUID id;
    private String brand;
    private String model;
    private int gigabyte;
    private int ram;
    private final PerformanceTypeEnum performanceType;
    private LaptopState theState;
    private final PropertyChangeSupport support;

    /**
     * Konstruktør til oprettelse af en ny laptop med et tilfældigt UUID
     */
    public Laptop(String brand, String model, int gigabyte, int ram, PerformanceTypeEnum performanceType) {
        this(UUID.randomUUID(), brand, model, gigabyte, ram, performanceType);
    }

    // Eksisterende fuldkonstruktør
    public Laptop(UUID id, String brand, String model, int gigabyte, int ram, PerformanceTypeEnum performanceType) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.gigabyte = gigabyte;
        this.ram = ram;
        this.performanceType = performanceType;
        this.support = new PropertyChangeSupport(this);
    }

    // Ny metode til registrering hos manager
    public void registerWithManager(ReservationManager manager) {
        if (manager != null) {
            this.addListener(manager);
        }
    }

    // Getters og setters

    public UUID getId() {
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

    public PerformanceTypeEnum getPerformanceType() {
        return performanceType;
    }

    // Koden angående laptop state

    public LaptopState getState() {
        return theState;
    }

    /**
     * Henter statens klassenavn til brug for databasen
     */
    public String getStateClassName() {
        return theState.getClass().getSimpleName();
    }

    public boolean isAvailable() {
        return theState instanceof AvailableState;
    }

    public boolean isLoaned() {
        return theState instanceof LoanedState;
    }

    public void changeState(LaptopState newState) {
        LaptopState oldState = theState;
        theState = newState;
        if (newState instanceof AvailableState) {
            support.firePropertyChange("toAvailableState", oldState, theState);
        }
    }

    /**
     * Sætter laptoppens tilstand baseret på klassens navn fra databasen
     * @param stateName navnet på tilstandsklassen (fx "AvailableState" eller "LoanedState")
     */
    public void setStateFromDatabase(String stateName) {
        if ("LoanedState".equals(stateName)) {
            if (!(theState instanceof LoanedState)) {
                theState = new LoanedState();
            }
        } else {
            if (!(theState instanceof AvailableState)) {
                theState = new AvailableState();
            }
        }
    }

    // Observer mønster kode

    @Override
    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    @Override
    public String toString() {
        return brand + " " + model + " (" + performanceType + ")";
    }
}