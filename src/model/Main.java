package model;

import model.enums.PerformanceTypeEnum;
import model.logic.reservationsLogic.ReservationManager;
import model.models.Laptop;
import model.models.Student;

import java.util.Date;

public class Main {
    public static void main(String[] args) {
        // 1. Opret ReservationManager
        ReservationManager reservationManager = new ReservationManager();

        // 2. Opret Laptops
        Laptop laptop1 = new Laptop("Dell", "XPS 15", 512, 16, PerformanceTypeEnum.HIGH);
        Laptop laptop2 = new Laptop("HP", "Pavilion", 256, 8, PerformanceTypeEnum.LOW);

        // Tilføj ReservationManager som observer til laptops
        laptop1.addListener(reservationManager);
        laptop2.addListener(reservationManager);

        // 3. Opret Studerende og tilføj dem til køerne
        Student student1 = new Student("Alice", new Date(), "Computer Science", 12345, "alice@example.com", 12345678, PerformanceTypeEnum.HIGH);
        Student student2 = new Student("Bob", new Date(), "Software Engineering", 67890, "bob@example.com", 87654321, PerformanceTypeEnum.LOW);
        Student student3 = new Student("Charlie", new Date(), "Data Science", 11223, "charlie@example.com", 11223344, PerformanceTypeEnum.HIGH);

        // Tilføj studerende til køerne via ReservationManager
        reservationManager.addToHighPerformanceQueue(student1);
        reservationManager.addToLowPerformanceQueue(student2);
        reservationManager.addToHighPerformanceQueue(student3);

        // 4. Test køfunktioner
        System.out.println("High Performance Queue Size: " + reservationManager.getHighNeedingQueueSize());
        System.out.println("Low Performance Queue Size: " + reservationManager.getLowNeedingQueueSize());

        System.out.println("Next in High Performance Queue: " + reservationManager.getNextInLineForHighPerformance().getName());
        System.out.println("Next in Low Performance Queue: " + reservationManager.getNextInLineForLowPerformance().getName());

        // 5. Skift laptop-tilstand til AvailableState og observer reaktion
        System.out.println("\n--- Skifter laptop1 til AvailableState ---");
        laptop1.changeState(new model.models.AvailableState());

        System.out.println("\n--- Skifter laptop2 til AvailableState ---");
        laptop2.changeState(new model.models.AvailableState());

        // 6. Test reservationer
        System.out.println("\n--- Reservationer ---");
        System.out.println("Antal reservationer: " + reservationManager.getAmountOfReservationsToDate());
        System.out.println("Antal aktive reservationer: " + reservationManager.getAmountOfActiveReservations());

        // 7. Fjern en reservation
        System.out.println("\n--- Fjern en reservation ---");
        if (reservationManager.getAmountOfReservationsToDate() > 0) {
            reservationManager.removeReservation(reservationManager.getLastReservationAdded().getReservationId());
            System.out.println("Reservation fjernet.");
        }

        System.out.println("Antal reservationer efter fjernelse: " + reservationManager.getAmountOfReservationsToDate());
    }
}
