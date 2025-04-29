package model.logic.reservationsLogic;

import model.enums.PerformanceTypeEnum;
import model.models.Laptop;
import model.models.LoanedState;
import model.log.Log;
import model.models.Reservation;
import model.models.Student;

public class ReservationFactory {
    private Log log;

    public ReservationFactory(){
        log = Log.getInstance();

    }

    public Reservation createReservation(Laptop laptop, Student student){
        laptop.changeState(new LoanedState());
        student.setHasLaptopToOpposite();
        Reservation reservation = new Reservation(student, laptop);
        log.addToLog("Reservation oprettet. ID er: " + reservation.getReservationId());
        return reservation;
    }


}
