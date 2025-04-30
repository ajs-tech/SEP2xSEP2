package model;

import model.enums.PerformanceTypeEnum;
import model.log.Log;
import model.logic.DataManager;
import model.logic.laptopLogic.LaptopData;
import model.logic.reservationsLogic.ReservationManager;
import model.logic.studentLogic.StudentData;
import model.models.Laptop;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- STARTER SYSTEMET ---");

        // 1. Opret DataManager, LaptopData og ReservationManager
        DataManager dataManager = new DataManager();
        LaptopData laptopData = dataManager.getLaptopDataObject();
        StudentData studentData = dataManager.getStudentDataObject();
        ReservationManager reservationManager = dataManager.getReservationManagerObject();
        Log log = Log.getInstance();


        /*
        Laptop laptop1 = new Laptop("Dell", "XPS 15", 512, 16, PerformanceTypeEnum.HIGH);
        Laptop laptop2 = new Laptop("HP", "Pavilion", 256, 8, PerformanceTypeEnum.LOW);
        Laptop laptop3 = new Laptop("Lenovo", "ThinkPad", 512, 16, PerformanceTypeEnum.HIGH);
        */

        System.out.println("\n--- Opretter laptops ---");
        Laptop laptop1= laptopData.createLaptop(PerformanceTypeEnum.LOW, reservationManager);
        Laptop laptop2 = laptopData.createLaptop(PerformanceTypeEnum.HIGH, reservationManager);
        Laptop laptop3 = laptopData.createLaptop(PerformanceTypeEnum.LOW, reservationManager);

        Laptop laptop4 = dataManager.createLaptop(PerformanceTypeEnum.LOW, reservationManager);

        System.out.println(laptop1.toString());
        log.addToLog(laptop4.toString());
        log.addToLog("HEJ MED DIG!");




















        /*

        System.out.println("Laptops oprettet og der er allerede tilføjet ReservationsManager som listener");

        System.out.println("\n--- Opretter studerende ---");
        Student student1 = dataManager.createStudent("Alice", new Date(), "Computer Science", 12345, "alice@example.com", 12345678, PerformanceTypeEnum.HIGH);
        Student student2 = dataManager.createStudent("Bob", new Date(), "Software Engineering", 67890, "bob@example.com", 87654321, PerformanceTypeEnum.LOW);
        Student student3 = dataManager.createStudent("Charlie", new Date(), "Data Science", 11223, "charlie@example.com", 11223344, PerformanceTypeEnum.HIGH);
        Student student4 = dataManager.createStudent("Diana", new Date(), "AI Engineering", 44556, "diana@example.com", 44556677, PerformanceTypeEnum.HIGH);

        System.out.println("Studerende oprettet og tilføjet til systemet.");

        System.out.println("\n--- Køstatus ---");
        System.out.println("High Performance Queue Size: " + reservationManager.getHighNeedingQueueSize());
        System.out.println("Low Performance Queue Size: " + reservationManager.getLowNeedingQueueSize());

        System.out.println("Next in High Performance Queue: " + (reservationManager.getNextInLineForHighPerformance() != null ? reservationManager.getNextInLineForHighPerformance().getName() : "Ingen"));
        System.out.println("Next in Low Performance Queue: " + (reservationManager.getNextInLineForLowPerformance() != null ? reservationManager.getNextInLineForLowPerformance().getName() : "Ingen"));

        System.out.println("\n--- Skifter laptop-tilstand til AvailableState ---");
        laptop1.changeState(new model.models.AvailableState());
        laptop2.changeState(new model.models.AvailableState());
        laptop3.changeState(new model.models.AvailableState());

        System.out.println("\n--- Reservationer ---");
        System.out.println("Antal reservationer: " + reservationManager.getAmountOfReservationsToDate());
        System.out.println("Antal aktive reservationer: " + reservationManager.getAmountOfActiveReservations());

        System.out.println("\n--- Fjern en reservation ---");
        if (reservationManager.getAmountOfReservationsToDate() > 0) {
            reservationManager.removeReservation(reservationManager.getLastReservationAdded().getReservationId());
            System.out.println("Reservation fjernet.");
        }

        System.out.println("Antal reservationer efter fjernelse: " + reservationManager.getAmountOfReservationsToDate());

        System.out.println("\n--- Slut på systemet ---");

         */
    }
}
