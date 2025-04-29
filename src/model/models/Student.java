package model.models;

import model.enums.PerformanceTypeEnum;

import java.util.Date;

public class Student {
    private String name;
    private Date degreeEndDate;
    private String degreeTitle;
    private int viaId;
    private String email;
    private int phoneNumber;
    private boolean hasLaptop;
    private PerformanceTypeEnum performanceNeeded;

    public Student(String name, Date degreeEndDate, String degreeTitle, int viaId, String email, int phoneNumber, PerformanceTypeEnum performanceNeeded){
        this.name = name;
        this.degreeEndDate = degreeEndDate;
        this.degreeTitle = degreeTitle;
        this.viaId = viaId;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.hasLaptop = false;
        this.performanceNeeded = performanceNeeded;
    }

    // Getters er her forneden

    public String getName() {
        return name;
    }

    public Date getDegreeEndDate() {
        return degreeEndDate;
    }

    public String getDegreeTitle() {
        return degreeTitle;
    }

    public int getViaId() {
        return viaId;
    }

    public String getEmail() {
        return email;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public PerformanceTypeEnum getPerformanceNeeded(){
        return performanceNeeded;
    }

     public boolean isHasLaptop() {
        return hasLaptop;
    }

    public String toString(){
        return "Oplysninger >> " + name + "\n" + viaId;
    }

    // Setters er her forneden


    public void setName(String name) {
        this.name = name;
    }

    public void setDegreeEndDate(Date degreeEndDate) {
        this.degreeEndDate = degreeEndDate;
    }

    public void setDegreeTitle(String degreeTitle) {
        this.degreeTitle = degreeTitle;
    }

    public void setViaId(int viaId) {
        this.viaId = viaId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPerformanceNeeded(PerformanceTypeEnum newPerformanceType){
        performanceNeeded = newPerformanceType;
    }

    public void setHasLaptopToOpposite(){
        hasLaptop = !hasLaptop;
    }
}
