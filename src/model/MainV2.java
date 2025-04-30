package model;

import model.enums.PerformanceTypeEnum;
import model.enums.ReservationStatusEnum;
import model.log.Log;
import model.logic.DataManager;
import model.models.Laptop;
import model.models.Reservation;
import model.models.Student;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainV2 {
    public static void main(String[] args) {
        // Ryd logfilen ved start
        Log.getInstance().addToLog("===== SYSTEMSTART: TEST AF LAPTOP UDLÅNSSYSTEM =====");

        // Opret central DataManager
        DataManager dataManager = new DataManager();
        System.out.println("DataManager oprettet og klar til brug");
        Log.getInstance().addToLog("System initialiseret: DataManager er klar til brug");

        // --- TEST 1: OPRETTELSE AF LAPTOPS ---
        System.out.println("\n===== TEST 1: OPRETTELSE AF LAPTOPS =====");
        Log.getInstance().addToLog("===== TEST 1: OPRETTELSE AF LAPTOPS =====");

        // Opret forskellige laptops (5 høj ydelse, 5 lav ydelse)
        System.out.println("Opretter 10 laptops (5 høj ydelse, 5 lav ydelse)...");

        for (int i = 0; i < 5; i++) {
            Laptop highPowerLaptop = dataManager.createLaptop(
                    PerformanceTypeEnum.HIGH,
                    dataManager.getReservationManagerObject()
            );
            highPowerLaptop.setBrand("Dell");
            highPowerLaptop.setModel("XPS " + (i+1));
            highPowerLaptop.setRam(32);
            highPowerLaptop.setGigabyte(1000);

            Laptop lowPowerLaptop = dataManager.createLaptop(
                    PerformanceTypeEnum.LOW,
                    dataManager.getReservationManagerObject()
            );
            lowPowerLaptop.setBrand("Lenovo");
            lowPowerLaptop.setModel("IdeaPad " + (i+1));
            lowPowerLaptop.setRam(8);
            lowPowerLaptop.setGigabyte(256);
        }

        // Vis statistik for laptops
        System.out.println("Antal laptops i systemet: " + dataManager.getAllLaptops().size());
        System.out.println("Ledige laptops: " + dataManager.getAmountOfAvailableLaptops());
        System.out.println("Udlånte laptops: " + dataManager.getAmountOfLoanedLaptops());
        System.out.println("Høj-ydelses laptops: " + countLaptopsByPerformance(dataManager, PerformanceTypeEnum.HIGH));
        System.out.println("Lav-ydelses laptops: " + countLaptopsByPerformance(dataManager, PerformanceTypeEnum.LOW));

        Log.getInstance().addToLog("Oprettet 10 laptops: " +
                countLaptopsByPerformance(dataManager, PerformanceTypeEnum.HIGH) + " med høj ydelse, " +
                countLaptopsByPerformance(dataManager, PerformanceTypeEnum.LOW) + " med lav ydelse");

        // --- TEST 2: OPRETTELSE AF STUDERENDE OG AUTOMATISK RESERVATION ---
        System.out.println("\n===== TEST 2: OPRETTELSE AF STUDERENDE OG AUTOMATISK RESERVATION =====");
        Log.getInstance().addToLog("===== TEST 2: OPRETTELSE AF STUDERENDE OG AUTOMATISK RESERVATION =====");

        // Opret 3 studerende - de vil få laptops automatisk hvis tilgængelige
        System.out.println("Opretter 3 studerende med forskellige laptop-behov...");

        Student student1 = createStudent(dataManager, "Anna Hansen", PerformanceTypeEnum.HIGH);
        Student student2 = createStudent(dataManager, "Peter Jensen", PerformanceTypeEnum.LOW);
        Student student3 = createStudent(dataManager, "Maria Nielsen", PerformanceTypeEnum.HIGH);

        // Vis statistik efter studerendeoprettelse
        System.out.println("Antal studerende i systemet: " + dataManager.getAllStudents().size());
        System.out.println("Studerende med høj-ydelses behov: " + dataManager.getStudentCountOfHighPowerNeeds());
        System.out.println("Studerende med lav-ydelses behov: " + dataManager.getStudentCountOfLowPowerNeeds());
        System.out.println("Studerende med tildelt laptop: " + dataManager.getCountOfWhoHasLaptop());
        System.out.println("Aktive reservationer: " + dataManager.getReservationManagerObject().getAmountOfActiveReservations());

        Log.getInstance().addToLog("Oprettet 3 studerende: " +
                dataManager.getStudentCountOfHighPowerNeeds() + " med høj-ydelses behov, " +
                dataManager.getStudentCountOfLowPowerNeeds() + " med lav-ydelses behov");
        Log.getInstance().addToLog("Automatisk oprettet " +
                dataManager.getReservationManagerObject().getAmountOfActiveReservations() + " reservationer");

        // --- TEST 3: KØ-HÅNDTERING - FLERE STUDERENDE END LAPTOPS ---
        System.out.println("\n===== TEST 3: KØ-HÅNDTERING - FLERE STUDERENDE END LAPTOPS =====");
        Log.getInstance().addToLog("===== TEST 3: KØ-HÅNDTERING - FLERE STUDERENDE END LAPTOPS =====");

        // Opret flere studerende end der er laptops tilgængelige
        System.out.println("Opretter 10 ekstra studerende (5 høj ydelse, 5 lav ydelse)...");

        for (int i = 0; i < 5; i++) {
            Student highPerformanceStudent = createStudent(dataManager,
                    "Høj-behov Student " + (i+4), PerformanceTypeEnum.HIGH);
            Student lowPerformanceStudent = createStudent(dataManager,
                    "Lav-behov Student " + (i+4), PerformanceTypeEnum.LOW);
        }

        // Vis kø-status
        System.out.println("Studerende i kø til høj-ydelses laptops: " +
                dataManager.getReservationManagerObject().getHighNeedingQueueSize());
        System.out.println("Studerende i kø til lav-ydelses laptops: " +
                dataManager.getReservationManagerObject().getLowNeedingQueueSize());

        Log.getInstance().addToLog("Kø-status: " +
                dataManager.getReservationManagerObject().getHighNeedingQueueSize() + " venter på høj-ydelses laptop, " +
                dataManager.getReservationManagerObject().getLowNeedingQueueSize() + " venter på lav-ydelses laptop");

        // --- TEST 4: AFLEVERING AF LAPTOP OG KØ-HÅNDTERING ---
        System.out.println("\n===== TEST 4: AFLEVERING AF LAPTOP OG KØ-HÅNDTERING =====");
        Log.getInstance().addToLog("===== TEST 4: AFLEVERING AF LAPTOP OG KØ-HÅNDTERING =====");

        // Hent alle aktive reservationer
        ArrayList<Reservation> activeReservations = dataManager.getReservationManagerObject().getAllActiveReservations();

        if (!activeReservations.isEmpty()) {
            // Aflever den første laptop i listen for at se køhåndtering i aktion
            Reservation returnReservation = activeReservations.get(0);
            Laptop returnedLaptop = returnReservation.getLaptop();
            Student returnStudent = returnReservation.getStudent();

            System.out.println("Afleverer laptop: " + returnedLaptop.getBrand() + " " +
                    returnedLaptop.getModel() + " fra " + returnStudent.getName());

            // Markér reservationen som afsluttet
            returnReservation.changeStatus(ReservationStatusEnum.Completed);
            Log.getInstance().addToLog("Reservation afsluttet: " + returnStudent.getName() +
                    " har afleveret " + returnedLaptop.getBrand() + " " + returnedLaptop.getModel());

            // Nulstil student status
            returnStudent.setHasLaptopToOpposite();
            Log.getInstance().addToLog(returnStudent.getName() + " har ikke længere en laptop");

            // Gør laptop tilgængelig igen - dette burde udløse observer-mønsteret
            returnedLaptop.changeState(new model.models.AvailableState());

            // Vent lidt for at sikre, at observer-mønsteret har tid til at reagere
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Tjek kø-status igen efter laptop er blevet tilgængelig
            System.out.println("\nEfter aflevering:");
            System.out.println("Studerende i kø til høj-ydelses laptops: " +
                    dataManager.getReservationManagerObject().getHighNeedingQueueSize());
            System.out.println("Studerende i kø til lav-ydelses laptops: " +
                    dataManager.getReservationManagerObject().getLowNeedingQueueSize());
            System.out.println("Aktive reservationer: " +
                    dataManager.getReservationManagerObject().getAmountOfActiveReservations());

            Log.getInstance().addToLog("Kø-status efter laptop-aflevering: " +
                    dataManager.getReservationManagerObject().getHighNeedingQueueSize() + " venter på høj-ydelses laptop, " +
                    dataManager.getReservationManagerObject().getLowNeedingQueueSize() + " venter på lav-ydelses laptop");
        } else {
            System.out.println("Ingen aktive reservationer at teste aflevering med");
            Log.getInstance().addToLog("Ingen aktive reservationer til test af aflevering");
        }

        // --- TEST 5: AFBRYDELSE AF RESERVATION ---
        System.out.println("\n===== TEST 5: AFBRYDELSE AF RESERVATION =====");
        Log.getInstance().addToLog("===== TEST 5: AFBRYDELSE AF RESERVATION =====");

        activeReservations = dataManager.getReservationManagerObject().getAllActiveReservations();

        if (!activeReservations.isEmpty()) {
            // Tag den sidste reservation i listen
            Reservation cancelReservation = activeReservations.get(activeReservations.size() - 1);
            Laptop cancelledLaptop = cancelReservation.getLaptop();
            Student cancelStudent = cancelReservation.getStudent();

            System.out.println("Afbryder reservation: " + cancelledLaptop.getBrand() + " " +
                    cancelledLaptop.getModel() + " fra " + cancelStudent.getName());

            // Markér reservationen som afbrudt
            cancelReservation.changeStatus(ReservationStatusEnum.Cancelled);
            Log.getInstance().addToLog("Reservation afbrudt: " + cancelStudent.getName() +
                    " - laptop " + cancelledLaptop.getBrand() + " " + cancelledLaptop.getModel());

            // Nulstil student status
            cancelStudent.setHasLaptopToOpposite();

            // Gør laptop tilgængelig igen
            cancelledLaptop.changeState(new model.models.AvailableState());

            // Vent lidt
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Vis final status
            System.out.println("\nFinal status:");
            System.out.println("Total antal laptops: " + dataManager.getAllLaptops().size());
            System.out.println("Ledige laptops: " + dataManager.getAmountOfAvailableLaptops());
            System.out.println("Udlånte laptops: " + dataManager.getAmountOfLoanedLaptops());
            System.out.println("Totalt antal studerende: " + dataManager.getStudentCount());
            System.out.println("Studerende med laptop: " + dataManager.getCountOfWhoHasLaptop());
            System.out.println("Aktive reservationer: " +
                    dataManager.getReservationManagerObject().getAmountOfActiveReservations());
            System.out.println("Afsluttede reservationer: " +
                    dataManager.getReservationManagerObject().getAmountOfCompletedReservations());
            System.out.println("Afbrudte reservationer: " +
                    dataManager.getReservationManagerObject().getAmountOfCancelledReservations());

            Log.getInstance().addToLog("===== TESTKØRSEL AFSLUTTET =====");
            Log.getInstance().addToLog("Final status: " +
                    dataManager.getAmountOfAvailableLaptops() + " ledige laptops, " +
                    dataManager.getAmountOfLoanedLaptops() + " udlånte laptops, " +
                    dataManager.getReservationManagerObject().getHighNeedingQueueSize() + " studerende venter på høj-ydelses laptop, " +
                    dataManager.getReservationManagerObject().getLowNeedingQueueSize() + " studerende venter på lav-ydelses laptop");
        } else {
            System.out.println("Ingen aktive reservationer at teste afbrydelse med");
            Log.getInstance().addToLog("Ingen aktive reservationer til test af afbrydelse");
        }
    }

    // Hjælpemetode til at tælle laptops efter performance type
    private static int countLaptopsByPerformance(DataManager manager, PerformanceTypeEnum type) {
        int count = 0;
        for (Laptop laptop : manager.getAllLaptops()) {
            if (laptop.getPerformanceType().equals(type)) {
                count++;
            }
        }
        return count;
    }

    // Hjælpemetode til at oprette studerende med dynamiske datoer
    private static Student createStudent(DataManager manager, String name, PerformanceTypeEnum performanceNeeded) {
        // Opret afslutningsdato (1-2 år fra nu)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1 + (int)(Math.random() * 2));
        Date endDate = calendar.getTime();

        // Generer unikke værdier
        int viaId = 300000 + (int)(Math.random() * 99999);
        int phoneNumber = 10000000 + (int)(Math.random() * 89999999);

        // Opret studerende
        return manager.createStudent(
                name,                           // navn
                endDate,                        // afslutningsdato
                "Software Engineering",         // uddannelsestitel
                viaId,                          // VIA ID
                name.toLowerCase().replace(" ", ".") + "@via.dk",  // email
                phoneNumber,                    // telefonnummer
                performanceNeeded               // performancetype
        );
    }
}
