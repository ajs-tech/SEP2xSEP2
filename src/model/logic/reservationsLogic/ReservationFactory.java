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
        Log.getInstance().addToLog("Reservation oprettet med id:" + reservation.getReservationId() + " >> Laptop [" + laptop.getBrand() + " " + laptop.getModel() + "] tildelt til student [" + student.getName() + "].");
        return reservation;
    }


}
