package model.logic;

import model.enums.PerformanceTypeEnum;
import model.logic.laptopLogic.LaptopData;
import model.logic.laptopLogic.LaptopDataInterface;
import model.logic.reservationsLogic.ReservationManager;
import model.logic.studentLogic.StudentData;
import model.logic.studentLogic.StudentDataInterface;
import model.models.Laptop;
import model.models.Student;

import java.util.ArrayList;
import java.util.Date;

public class DataManager implements LaptopDataInterface, StudentDataInterface {
    private LaptopData laptopData;
    private StudentData studentData;
    private ReservationManager reservationManager;

    public DataManager(){
        laptopData = new LaptopData();
        studentData = new StudentData();
        reservationManager = new ReservationManager();
    }

    // Laptop getters og setters

    @Override
    public ArrayList<Laptop> getAllLaptops() {
        return laptopData.getAllLaptops();
    }

    @Override
    public int getAmountOfAvailableLaptops(){
        return laptopData.getAmountOfAvailableLaptops();
    }

    @Override
    public int getAmountOfLoanedLaptops(){
        return laptopData.getAmountOfLoanedLaptops();
    }

    @Override
    public int getAmountOfLaptopsByState(String classSimpleName){
        return laptopData.getAmountOfLaptopsByState(classSimpleName);
    }

    @Override
    public Laptop findAvailableLaptop(PerformanceTypeEnum performanceTypeEnum) {
        return laptopData.findAvailableLaptop(performanceTypeEnum);
    }

    @Override
    public Laptop createLaptop(PerformanceTypeEnum performanceTypeEnum) {
        return laptopData.createLaptop(performanceTypeEnum);
    }



    // Students getters og setters

    @Override
    public ArrayList<Student> getAllStudents() {
        return studentData.getAllStudents();
    }

    @Override
    public int getStudentCount() {
        return studentData.getStudentCount();
    }

    @Override
    public Student getStudentByID(int id) {
        return studentData.getStudentByID(id);
    }

    @Override
    public ArrayList<Student> getStudentWithHighPowerNeeds() {
        return studentData.getStudentWithHighPowerNeeds();
    }

    @Override
    public int getStudentCountOfHighPowerNeeds() {
        return studentData.getStudentCountOfHighPowerNeeds();
    }

    @Override
    public ArrayList<Student> getStudentWithLowPowerNeeds() {
        return studentData.getStudentWithLowPowerNeeds();
    }

    @Override
    public int getStudentCountOfLowPowerNeeds() {
        return studentData.getStudentCountOfLowPowerNeeds();
    }

    @Override
    public ArrayList<Student> getThoseWhoHaveLaptop() {
        return studentData.getThoseWhoHaveLaptop();
    }

    @Override
    public int getCountOfWhoHasLaptop() {
        return studentData.getCountOfWhoHasLaptop();
    }

    @Override
    public Student createStudent(String name, Date degreeEndDate, String degreeTitle, int viaId, String email, int phoneNumber, PerformanceTypeEnum performanceNeeded){
        // Opret en ny studerende
        Student studentCreated = studentData.createStudent(name, degreeEndDate, degreeTitle, viaId, email, phoneNumber, performanceNeeded);

        // Tjek efter en ledig laptop med performanceType der matcher student
        Laptop availableLaptop = findAvailableLaptop(studentCreated.getPerformanceNeeded());

        if (availableLaptop != null){
            reservationManager.createReservation(availableLaptop, studentCreated);
        } else {
            if (PerformanceTypeEnum.LOW.equals(studentCreated.getPerformanceNeeded())){
                reservationManager.addToLowPerformanceQueue(studentCreated);
            } else if (PerformanceTypeEnum.HIGH.equals(studentCreated.getPerformanceNeeded())) {
                reservationManager.addToHighPerformanceQueue(studentCreated);
            }
        }
        return studentCreated;

    }




}
