package model.logic.studentLogic;

import model.enums.PerformanceTypeEnum;
import model.models.Student;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class StudentData implements StudentDataInterface {
    private ArrayList<Student> allStudents;

    public StudentData() {
        allStudents = new ArrayList<>();
    }


    @Override
    public ArrayList<Student> getAllStudents() {
        return allStudents;
    }

    @Override
    public int getStudentCount() {
        return allStudents.size();
    }

    @Override
    public Student getStudentByID(int id) {
        for (Student student : allStudents){
            if (student.getViaId() == id){
                return student;
            }
        }
        return null;
    }

    @Override
    public ArrayList<Student> getStudentWithHighPowerNeeds() {
        ArrayList<Student> powerHungryStudents = new ArrayList<>();
        for (Student student : allStudents){
            if (student.getPerformanceNeeded().equals(PerformanceTypeEnum.HIGH)){
                powerHungryStudents.add(student);
            }
        }
        return powerHungryStudents;
    }

    @Override
    public int getStudentCountOfHighPowerNeeds() {
        int count = 0;
        for (Student student : allStudents){
            if (student.getPerformanceNeeded().equals(PerformanceTypeEnum.HIGH)){
                count++;
            }
        }
        return count;
    }

    @Override
    public ArrayList<Student> getStudentWithLowPowerNeeds() {
        ArrayList<Student> lowPowerStudents = new ArrayList<>();
        for (Student student : allStudents){
            if (student.getPerformanceNeeded().equals(PerformanceTypeEnum.LOW)){
                lowPowerStudents.add(student);
            }
        }
        return lowPowerStudents;
    }

    @Override
    public int getStudentCountOfLowPowerNeeds() {
        int count = 0;
        for (Student student : allStudents){
            if (student.getPerformanceNeeded().equals(PerformanceTypeEnum.LOW)){
                count++;
            }
        }
        return count;
    }

    @Override
    public ArrayList<Student> getThoseWhoHaveLaptop() {
        ArrayList<Student> studentsWithLaptop = new ArrayList<>();
        for (Student student : allStudents){
            if (student.isHasLaptop()){
                studentsWithLaptop.add(student);
            }
        }
        return studentsWithLaptop;
    }

    @Override
    public int getCountOfWhoHasLaptop() {
        int count = 0;
        for (Student student : allStudents){
            if (student.isHasLaptop()){
                count++;
            }
        }
        return count;
    }

    @Override
    public Student createStudent(String name, Date degreeEndDate, String degreeTitle, int viaId, String email, int phoneNumber, PerformanceTypeEnum performanceNeeded){
        Student student = new Student(name, degreeEndDate, degreeTitle, viaId, email, phoneNumber, performanceNeeded);
        allStudents.add(student);
        return student;
    }
}
