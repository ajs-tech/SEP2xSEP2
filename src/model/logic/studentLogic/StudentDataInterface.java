package model.logic.studentLogic;

import model.enums.PerformanceTypeEnum;
import model.models.Student;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public interface StudentDataInterface {
    ArrayList<Student> getAllStudents();
    int getStudentCount();
    Student getStudentByID(int id);
    ArrayList<Student> getStudentWithHighPowerNeeds();
    int getStudentCountOfHighPowerNeeds();
    ArrayList<Student> getStudentWithLowPowerNeeds();
    int getStudentCountOfLowPowerNeeds();
    ArrayList<Student> getThoseWhoHaveLaptop();
    int getCountOfWhoHasLaptop();
    Student createStudent(String name, Date degreeEndDate, String degreeTitle, int viaId, String email, int phoneNumber, PerformanceTypeEnum performanceNeeded);

}
