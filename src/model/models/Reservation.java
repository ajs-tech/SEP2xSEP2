package model.models;

import model.enums.ReservationStatusEnum;

import java.util.UUID;
import java.util.Date;

public class Reservation {
    private UUID reservationID;
    private Student student;
    private Laptop laptop;
    private ReservationStatusEnum status;
    private Date creationDate;

    /**
     * Konstruktør til oprettelse af en ny reservation med automatisk genereret UUID
     */
    public Reservation(Student student, Laptop laptop){
        this(UUID.randomUUID(), student, laptop, ReservationStatusEnum.ACTIVE, new Date());
    }

    /**
     * Konstruktør til oprettelse af en reservation med specifikt UUID og status
     * Bruges ved indlæsning fra databasen
     */
    public Reservation(UUID reservationID, Student student, Laptop laptop, ReservationStatusEnum status, Date creationDate){
        this.reservationID = reservationID;
        this.student = student;
        this.laptop = laptop;
        this.status = status;
        this.creationDate = creationDate != null ? creationDate : new Date();
    }

    /**
     * Konstruktør til oprettelse af en reservation med specifikt UUID og status
     * Bruges ved indlæsning fra databasen
     */
    public Reservation(UUID reservationID, Student student, Laptop laptop, ReservationStatusEnum status){
        this(reservationID, student, laptop, status, new Date());
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

    public Date getCreationDate() {
        return creationDate;
    }

    // Setters

    public void changeStatus(ReservationStatusEnum newStatus){
        ReservationStatusEnum oldStatus = this.status;
        this.status = newStatus;

        // Hvis en reservation afsluttes, opdater laptop og student tilstand
        if (oldStatus == ReservationStatusEnum.ACTIVE &&
                (newStatus == ReservationStatusEnum.COMPLETED || newStatus == ReservationStatusEnum.CANCELLED)) {
            // Gør laptopen tilgængelig igen
            if (laptop.isLoaned()) {
                laptop.changeState(new AvailableState());
            }

            // Opdater student har laptop status hvis nødvendigt
            if (student.isHasLaptop()) {
                student.setHasLaptopToOpposite();
            }
        }
    }

    @Override
    public String toString() {
        return "Reservation: " + student.getName() + " - " + laptop.getModel() + " (" + status + ")";
    }
}