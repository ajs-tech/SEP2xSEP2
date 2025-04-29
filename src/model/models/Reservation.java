package model.models;

import model.enums.ReservationStatusEnum;

import java.util.UUID;

public class Reservation {
    private UUID reservationID;
    private Student student;
    private Laptop laptop;
    private ReservationStatusEnum status;

    public Reservation(Student student, Laptop laptop){
        this.reservationID = UUID.randomUUID();
        this.student = student;
        this.laptop = laptop;
        status = ReservationStatusEnum.Active;
    }

    // Getters

    public UUID getReservationId(){
        return reservationID;
    }

    public Student getStudent(){
        return student;
    }

    public String getStudentDetailsString(){
        return student.toString();
    }

    public Laptop getLaptop(){
        return laptop;
    }

    public String getLaptopDetailsString(){
        return laptop.toString();
    }

    public ReservationStatusEnum getStatus(){
        return status;
    }

    // Setters

    public void changeStatus(ReservationStatusEnum newStatus){
        this.status = newStatus;
    }
}
