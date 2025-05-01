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
    private ReservationManager manager;

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
        this.theState = new AvailableState(); // Initialize theState here!
        this.support = new PropertyChangeSupport(this);
        this.manager = null;
    }

    public void registerWithManager(ReservationManager manager)
    {
        this.manager = manager;
        addPropertyChangeListener(manager);
    }

    public UUID getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getGigabyte() {
        return gigabyte;
    }

    public int getRam() {
        return ram;
    }

    public PerformanceTypeEnum getPerformanceType() {
        return performanceType;
    }

    public LaptopState getTheState() {
        return theState;
    }

    public void changeState(LaptopState newState) {
        LaptopState oldState = this.theState;
        this.theState = newState;
        // Triggerer lyttere - OBS!!: Kaldes kun hvis tilstanden er ændret
        if(oldState != theState) {
            this.support.firePropertyChange(oldState.getPropertyName(), oldState, newState);
        }
    }

    public boolean isAvailable()
    {
        return theState.isAvailable();
    }

    public boolean isLoaned()
    {
        return theState.isLoaned();
    }

    public String getStateClassName() {
        return theState.getClass().getSimpleName();
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
        } else if ("AvailableState".equals(stateName)){
            if (!(theState instanceof AvailableState)) {
                theState = new AvailableState();
            }
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        this.support.addPropertyChangeListener(pcl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        this.support.removePropertyChangeListener(pcl);
    }
}
