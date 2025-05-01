package model.database;

import model.enums.PerformanceTypeEnum;
import model.enums.ReservationStatusEnum;
import model.logic.reservationsLogic.ReservationManager;
import model.models.Laptop;
import model.models.Student;
import model.models.Reservation;

import java.sql.SQLException;
import java.util.*;

/**
 * Interaktivt test-program til at afprøve DAO-funktionalitet via konsol
 */
public class DAOTester {
  private static Scanner scanner = new Scanner(System.in);
  private static LaptopDAO laptopDAO = new LaptopDAO();
  private static StudentDAO studentDAO = new StudentDAO();
  private static ReservationDAO reservationDAO = new ReservationDAO();
  private static QueueDAO queueDAO = new QueueDAO(); // Tilføjet QueueDAO
  private static ReservationManager reservationManager = null;

  public static void main(String[] args) {
    boolean running = true;

    System.out.println("===== DAO Test Program =====");

    while (running) {
      System.out.println("\nVælg en test:");
      System.out.println("1: Vis alle laptops");
      System.out.println("2: Vis alle studerende");
      System.out.println("3: Vis alle reservationer");
      System.out.println("4: Tilføj ny laptop");
      System.out.println("5: Tilføj ny student");
      System.out.println("6: Opret reservation");
      System.out.println("7: Opdater reservation status");
      System.out.println("8: Find laptop via ID");
      System.out.println("9: Find student via ID");
      // Nye menu-punkter til queue-funktionalitet
      System.out.println("10: Tilføj student til kø");
      System.out.println("11: Vis studerende i kø");
      System.out.println("12: Fjern student fra kø");
      System.out.println("13: Vis kø-størrelse");
      System.out.println("14: Frigør laptop og tjek automatic reservation");
      System.out.println("0: Afslut");

      System.out.print("Vælg: ");
      int choice = scanner.nextInt();
      scanner.nextLine(); // Fjern newline

      try {
        switch (choice) {
          case 0:
            running = false;
            break;
          case 1:
            showAllLaptops();
            break;
          case 2:
            showAllStudents();
            break;
          case 3:
            showAllReservations();
            break;
          case 4:
            addNewLaptop();
            break;
          case 5:
            addNewStudent();
            break;
          case 6:
            createReservation();
            break;
          case 7:
            updateReservationStatus();
            break;
          case 8:
            findLaptopById();
            break;
          case 9:
            findStudentById();
            break;
          // Nye test-metoder til queue-funktionalitet
          case 10:
            addStudentToQueue();
            break;
          case 11:
            showStudentsInQueue();
            break;
          case 12:
            removeStudentFromQueue();
            break;
          case 13:
            showQueueSize();
            break;
          case 14:
            freeLaptopAndCheckAutomaticReservation(); // NY FUNKTION
            break;
          default:
            System.out.println("Ugyldigt valg. Prøv igen.");
        }
      } catch (SQLException e) {
        System.out.println("Database fejl: " + e.getMessage());
        e.printStackTrace();
      } catch (Exception e) {
        System.out.println("Fejl: " + e.getMessage());
        e.printStackTrace();
      }
    }

    System.out.println("Program afsluttet.");
    // Luk Connection Pool
    DatabaseConnection.closePool();
  }

  // Metode 1: Vis alle laptops
  private static void showAllLaptops() throws SQLException {
    System.out.println("\n=== Alle Laptops ===");
    List<Laptop> laptops = laptopDAO.getAllLaptops();

    if (laptops.isEmpty()) {
      System.out.println("Ingen laptops fundet i databasen.");
    } else {
      System.out.println("ID | Brand | Model | GB | RAM | Performance");
      System.out.println("-----------------------------------------");

      for (Laptop laptop : laptops) {
        System.out.printf("%s | %s | %s | %d | %d | %s%n",
                laptop.getId(),
                laptop.getBrand(),
                laptop.getModel(),
                laptop.getGigabyte(),
                laptop.getRam(),
                laptop.getPerformanceType());
      }
      System.out.println("Total: " + laptops.size() + " laptops");
    }
  }

  private static ReservationManager getReservationManager() {
    if (reservationManager == null) {
      reservationManager = new ReservationManager();
    }
    return reservationManager;
  }

  // Metode 2: Vis alle studerende
  private static void showAllStudents() throws SQLException {
    System.out.println("\n=== Alle Studerende ===");
    List<Student> students = studentDAO.getAllStudents();

    if (students.isEmpty()) {
      System.out.println("Ingen studerende fundet i databasen.");
    } else {
      System.out.println("VIA ID | Navn | Email | Performance behov | Har laptop");
      System.out.println("-----------------------------------------------------");

      for (Student student : students) {
        System.out.printf("%d | %s | %s | %s | %s%n",
                student.getViaId(),
                student.getName(),
                student.getEmail(),
                student.getPerformanceNeeded(),
                student.isHasLaptop() ? "Ja" : "Nej");
      }
      System.out.println("Total: " + students.size() + " studerende");
    }
  }

  // Metode 3: Vis alle reservationer
  private static void showAllReservations() throws SQLException {
    System.out.println("\n=== Alle Reservationer ===");
    List<Reservation> reservations = reservationDAO.getAllReservations();

    if (reservations.isEmpty()) {
      System.out.println("Ingen reservationer fundet i databasen.");
    } else {
      System.out.println("Reservations ID | Status | Student | Laptop");
      System.out.println("------------------------------------------");

      for (Reservation reservation : reservations) {
        System.out.printf("%s | %s | %s (ID: %d) | %s %s%n",
                reservation.getReservationId(),
                reservation.getStatus(),
                reservation.getStudent().getName(),
                reservation.getStudent().getViaId(),
                reservation.getLaptop().getBrand(),
                reservation.getLaptop().getModel());
      }
      System.out.println("Total: " + reservations.size() + " reservationer");
    }
  }

  // Metode 4: Tilføj ny laptop
  private static void addNewLaptop() throws SQLException {
    System.out.println("\n=== Tilføj Ny Laptop ===");

    System.out.print("Brand: ");
    String brand = scanner.nextLine();

    System.out.print("Model: ");
    String model = scanner.nextLine();

    int gigabyte = getIntInput("Gigabyte (hårddisk): ");
    int ram = getIntInput("RAM: ");


    System.out.print("Performance Type (LOW/HIGH): ");
    String perfType = scanner.nextLine().toUpperCase();
    PerformanceTypeEnum performanceType = PerformanceTypeEnum.valueOf(perfType);

    // Opdater konstruktøren til at inkludere reservationManager
    Laptop laptop = new Laptop(brand, model, gigabyte, ram, performanceType);
    laptop.registerWithManager(getReservationManager());
    boolean success = laptopDAO.insert(laptop);

    if (success) {
      System.out.println("Laptop oprettet med succes! ID: " + laptop.getId());
    } else {
      System.out.println("Kunne ikke oprette laptop.");
    }
  }
  // Helper method to get validated integer input
  private static int getIntInput(String prompt) {
    int value = 0;
    boolean valid = false;
    do {
      try {
        System.out.print(prompt);
        value = scanner.nextInt();
        valid = true;
      } catch (InputMismatchException e) {
        System.out.println("Ugyldig indtastning. Skal være et heltal.");
        scanner.nextLine(); // Clear the invalid input
      }
    } while (!valid);
    scanner.nextLine(); // Consume the remaining newline
    return value;
  }

  // Metode 5: Tilføj ny student
  private static void addNewStudent() throws SQLException {
    System.out.println("\n=== Tilføj Ny Student ===");

    System.out.print("Navn: ");
    String name = scanner.nextLine();

    System.out.print("Uddannelse: ");
    String degree = scanner.nextLine();

    int viaId = getIntInput("VIA ID: ");

    System.out.print("Email: ");
    String email = scanner.nextLine();

    int phoneNumber = getIntInput("Telefonnummer: ");


    System.out.print("Performance behov (LOW/HIGH): ");
    String perfType = scanner.nextLine().toUpperCase();
    PerformanceTypeEnum performanceNeeded = PerformanceTypeEnum.valueOf(perfType);

    // Opret en slutdato for uddannelsen (1 år frem)
    Date degreeEndDate = new Date(System.currentTimeMillis() + 31536000000L);

    Student student = new Student(name, degreeEndDate, degree, viaId, email, phoneNumber, performanceNeeded);
    boolean success = studentDAO.insert(student);

    if (success) {
      System.out.println("Student oprettet med succes! VIA ID: " + student.getViaId());
    } else {
      System.out.println("Kunne ikke oprette student.");
    }
  }

  // Metode 6: Opret reservation
  private static void createReservation() throws SQLException {
    System.out.println("\n=== Opret Reservation ===");

    // Find først student
    System.out.print("Student VIA ID: ");
    int studentId = scanner.nextInt();
    scanner.nextLine(); // Fjern newline

    Student student = studentDAO.getById(studentId);
    if (student == null) {
      System.out.println("Student ikke fundet.");
      return;
    }

    // Find laptop
    System.out.print("Laptop UUID: ");
    String laptopIdStr = scanner.nextLine();
    UUID laptopId = UUID.fromString(laptopIdStr);

    Laptop laptop = laptopDAO.getById(laptopId);
    if (laptop == null) {
      System.out.println("Laptop ikke fundet.");
      return;
    }

    Reservation reservation = new Reservation(student, laptop);
    boolean success = reservationDAO.insert(reservation);

    if (success) {
      System.out.println("Reservation oprettet med succes! ID: " + reservation.getReservationId());
    } else {
      System.out.println("Kunne ikke oprette reservation.");
    }
  }

  // Metode 7: Opdater reservation status
  private static void updateReservationStatus() throws SQLException {
    System.out.println("\n=== Opdater Reservation Status ===");

    System.out.print("Reservation UUID: ");
    String reservationIdStr = scanner.nextLine();
    UUID reservationId = UUID.fromString(reservationIdStr);

    Reservation reservation = reservationDAO.getById(reservationId);
    if (reservation == null) {
      System.out.println("Reservation ikke fundet.");
      return;
    }

    System.out.println("Nuværende status: " + reservation.getStatus());
    System.out.println("Vælg ny status:");
    System.out.println("1: ACTIVE");
    System.out.println("2: COMPLETED");
    System.out.println("3: CANCELLED");

    int statusChoice = getIntInput("Choose: ");

    ReservationStatusEnum newStatus;
    switch (statusChoice) {
      case 1:
        newStatus = ReservationStatusEnum.ACTIVE;
        break;
      case 2:
        newStatus = ReservationStatusEnum.COMPLETED;
        break;
      case 3:
        newStatus = ReservationStatusEnum.CANCELLED;
        break;
      default:
        System.out.println("Ugyldigt valg. Status ikke ændret.");
        return;
    }

    reservation.changeStatus(newStatus);
    boolean success = reservationDAO.update(reservation);

    if (success) {
      System.out.println("Reservation status opdateret til: " + newStatus);
    } else {
      System.out.println("Kunne ikke opdatere reservation status.");
    }
  }

  // Metode 8: Find laptop via ID
  private static void findLaptopById() throws SQLException {
    System.out.println("\n=== Find Laptop via ID ===");

    System.out.print("Laptop UUID: ");
    String laptopIdStr = scanner.nextLine();
    UUID laptopId = UUID.fromString(laptopIdStr);

    Laptop laptop = laptopDAO.getById(laptopId);

    if (laptop == null) {
      System.out.println("Ingen laptop fundet med ID: " + laptopId);
    } else {
      System.out.println("Fundet laptop:");
      System.out.println("ID: " + laptop.getId());
      System.out.println("Brand: " + laptop.getBrand());
      System.out.println("Model: " + laptop.getModel());
      System.out.println("Gigabyte: " + laptop.getGigabyte());
      System.out.println("RAM: " + laptop.getRam());
      System.out.println("Performance Type: " + laptop.getPerformanceType());
      System.out.println("Status: " + (laptop.isAvailable() ? "Tilgængelig" : "Udlånt"));
    }
  }

  // Metode 9: Find student via ID
  private static void findStudentById() throws SQLException {
    System.out.println("\n=== Find Student via ID ===");

    System.out.print("Student VIA ID: ");
    int studentId = scanner.nextInt();
    scanner.nextLine(); // Fjern newline

    Student student = studentDAO.getById(studentId);

    if (student == null) {
      System.out.println("Ingen student fundet med VIA ID: " + studentId);
    } else {
      System.out.println("Fundet student:");
      System.out.println("VIA ID: " + student.getViaId());
      System.out.println("Navn: " + student.getName());
      System.out.println("Email: " + student.getEmail());
      System.out.println("Telefon: " + student.getPhoneNumber());
      System.out.println("Uddannelse: " + student.getDegreeTitle());
      System.out.println("Uddannelse slut: " + student.getDegreeEndDate());
      System.out.println("Performance behov: " + student.getPerformanceNeeded());
      System.out.println("Har laptop: " + (student.isHasLaptop() ? "Ja" : "Nej"));

      // Hent også reservationer for denne student
      try {
        List<Reservation> studentReservations = reservationDAO.getByStudentId(studentId);
        System.out.println("\nStudentens reservationer (" + studentReservations.size() + "):");

        if (!studentReservations.isEmpty()) {
          for (Reservation reservation : studentReservations) {
            System.out.printf("ID: %s | Status: %s | Laptop: %s %s%n",
                    reservation.getReservationId(),
                    reservation.getStatus(),
                    reservation.getLaptop().getBrand(),
                    reservation.getLaptop().getModel());
          }
        }
      } catch (SQLException e) {
        System.out.println("Kunne ikke hente reservationer: " + e.getMessage());
      }
    }
  }

  // Metode 10: Tilføj student til kø
  private static void addStudentToQueue() throws SQLException {
    System.out.println("\n=== Tilføj Student til Kø ===");

    System.out.print("Student VIA ID: ");
    int studentId = scanner.nextInt();
    scanner.nextLine(); // Fjern newline

    Student student = studentDAO.getById(studentId);
    if (student == null) {
      System.out.println("Student ikke fundet.");
      return;
    }

    if (student.isHasLaptop()) {
      System.out.println("Student har allerede en laptop tildelt og kan ikke tilføjes til kø.");
      return;
    }

    if (queueDAO.isStudentInAnyQueue(studentId)) {
      System.out.println("Student er allerede i en kø.");
      return;
    }

    System.out.println("Vælg kø-type:");
    System.out.println("1: Høj-ydelses laptops");
    System.out.println("2: Lav-ydelses laptops");

    int queueTypeChoice = scanner.nextInt();
    scanner.nextLine(); // Fjern newline

    PerformanceTypeEnum performanceType;
    switch (queueTypeChoice) {
      case 1:
        performanceType = PerformanceTypeEnum.HIGH;
        break;
      case 2:
        performanceType = PerformanceTypeEnum.LOW;
        break;
      default:
        System.out.println("Ugyldigt valg.");
        return;
    }

    boolean success = queueDAO.addToQueue(student, performanceType);

    if (success) {
      System.out.println("Student tilføjet til " + performanceType + " kø med succes!");
    } else {
      System.out.println("Kunne ikke tilføje student til kø.");
    }
  }

  // Metode 11: Vis studerende i kø
  private static void showStudentsInQueue() throws SQLException {
    System.out.println("\n=== Vis Studerende i Kø ===");

    System.out.println("Vælg kø-type:");
    System.out.println("1: Høj-ydelses laptops");
    System.out.println("2: Lav-ydelses laptops");

    int queueTypeChoice = scanner.nextInt();
    scanner.nextLine(); // Fjern newline

    PerformanceTypeEnum performanceType;
    switch (queueTypeChoice) {
      case 1:
        performanceType = PerformanceTypeEnum.HIGH;
        break;
      case 2:
        performanceType = PerformanceTypeEnum.LOW;
        break;
      default:
        System.out.println("Ugyldigt valg.");
        return;
    }

    List<Student> studentsInQueue = queueDAO.getStudentsInQueue(performanceType);

    if (studentsInQueue.isEmpty()) {
      System.out.println("Ingen studerende i " + performanceType + " køen.");
    } else {
      System.out.println("Studerende i " + performanceType + " køen:");
      System.out.println("Række | VIA ID | Navn | Email");
      System.out.println("---------------------------");

      for (int i = 0; i < studentsInQueue.size(); i++) {
        Student student = studentsInQueue.get(i);
        System.out.printf("%d | %d | %s | %s%n",
                i + 1,
                student.getViaId(),
                student.getName(),
                student.getEmail());
      }
      System.out.println("Total: " + studentsInQueue.size() + " studerende i køen");
    }
  }

  // Metode 12: Fjern student fra kø
  private static void removeStudentFromQueue() throws SQLException {
    System.out.println("\n=== Fjern Student fra Kø ===");

    System.out.print("Student VIA ID: ");
    int studentId = scanner.nextInt();
    scanner.nextLine(); // Fjern newline

    if (!queueDAO.isStudentInAnyQueue(studentId)) {
      System.out.println("Student er ikke i nogen kø.");
      return;
    }

    System.out.println("Vælg kø-type:");
    System.out.println("1: Høj-ydelses laptops");
    System.out.println("2: Lav-ydelses laptops");

    int queueTypeChoice = scanner.nextInt();
    scanner.nextLine(); // Fjern newline

    PerformanceTypeEnum performanceType;
    switch (queueTypeChoice) {
      case 1:
        performanceType = PerformanceTypeEnum.HIGH;
        break;
      case 2:
        performanceType = PerformanceTypeEnum.LOW;
        break;
      default:
        System.out.println("Ugyldigt valg.");
        return;
    }

    boolean success = queueDAO.removeFromQueue(studentId, performanceType);

    if (success) {
      System.out.println("Student fjernet fra " + performanceType + " kø med succes!");
    } else {
      System.out.println("Kunne ikke fjerne student fra kø. Kontroller at studenten er i den valgte kø.");
    }
  }

  // Metode 13: Vis kø-størrelse
  private static void showQueueSize() throws SQLException {
    System.out.println("\n=== Vis Kø-størrelse ===");

    int highQueueSize = queueDAO.getQueueSize(PerformanceTypeEnum.HIGH);
    int lowQueueSize = queueDAO.getQueueSize(PerformanceTypeEnum.LOW);

    System.out.println("Høj-ydelses kø: " + highQueueSize + " studerende");
    System.out.println("Lav-ydelses kø: " + lowQueueSize + " studerende");
    System.out.println("Total i køer: " + (highQueueSize + lowQueueSize) + " studerende");
  }

  private static void freeLaptopAndCheckAutomaticReservation() throws SQLException {
    System.out.println("\n=== Frigør Laptop og Tjek Automatisk Reservation ===");

    // Tjek først om der er studerende i køen
    int highQueueSize = queueDAO.getQueueSize(PerformanceTypeEnum.HIGH);
    int lowQueueSize = queueDAO.getQueueSize(PerformanceTypeEnum.LOW);

    if (highQueueSize == 0 && lowQueueSize == 0) {
      System.out.println("Der er ingen studerende i køen. Test kræver mindst én student i køen.");
      System.out.println("Brug menu-punkt 10 for at tilføje en student til køen først.");
      return;
    }

    // Vis alle aktive reservationer
    System.out.println("Aktive reservationer:");
    List<Reservation> activeReservations = new ArrayList<>();
    for (Reservation res : reservationDAO.getAllReservations()) {
      if (res.getStatus() == ReservationStatusEnum.ACTIVE) {
        activeReservations.add(res);
      }
    }

    if (activeReservations.isEmpty()) {
      System.out.println("Ingen aktive reservationer fundet. Der skal være mindst én aktiv reservation.");
      System.out.println("Brug menu-punkt 6 for at oprette en reservation først.");
      return;
    }

    // Vis reservationerne med numre
    for (int i = 0; i < activeReservations.size(); i++) {
      Reservation res = activeReservations.get(i);
      System.out.printf("%d: %s | Student: %s | Laptop: %s %s (%s)%n",
              i + 1,
              res.getReservationId(),
              res.getStudent().getName(),
              res.getLaptop().getBrand(),
              res.getLaptop().getModel(),
              res.getLaptop().getPerformanceType());
    }

    // Vælg hvilken reservation der skal afsluttes
    int resChoice = getIntInput("Vælg reservation at afslutte (1-" + activeReservations.size() + "): ");


    if (resChoice < 1 || resChoice > activeReservations.size()) {
      System.out.println("Ugyldigt valg.");
      return;
    }

    // Hent valgte reservation
    Reservation selectedReservation = activeReservations.get(resChoice - 1);
    Laptop laptop = selectedReservation.getLaptop();
    PerformanceTypeEnum laptopType = laptop.getPerformanceType();

    // Find om der er studerende i køen der matcher denne laptops ydelsestype
    boolean hasStudentsInQueue = false;
    if (laptopType == PerformanceTypeEnum.HIGH && highQueueSize > 0) {
      hasStudentsInQueue = true;
    } else if (laptopType == PerformanceTypeEnum.LOW && lowQueueSize > 0) {
      hasStudentsInQueue = true;
    }

    if (!hasStudentsInQueue) {
      System.out.println("ADVARSEL: Der er ingen studerende i " + laptopType + " køen.");
      System.out.println("Du vil ikke se den automatiske reservation-mekanisme i aktion.");
      System.out.print("Vil du fortsætte alligevel? (ja/nej): ");
      String confirm = scanner.nextLine();
      if (!confirm.equalsIgnoreCase("ja")) {
        System.out.println("Afbrudt.");
        return;
      }
    }

    System.out.println("\nStatus FØR:");
    System.out.println("- Aktive reservationer: " + activeReservations.size());
    System.out.println("- Studerende i " + laptopType + " kø: " +
            (laptopType == PerformanceTypeEnum.HIGH ? highQueueSize : lowQueueSize));

    // Gem UUID og student fra valgte reservation til senere verificering
    UUID laptopId = laptop.getId();

    // Ændr status på reservationen til COMPLETED via transaktion (frigiver laptop)
    System.out.println("\nAfslutter reservation " + selectedReservation.getReservationId() + "...");
    boolean updated = reservationDAO.updateStatusWithTransaction(selectedReservation);

    if (!updated) {
      System.out.println("Fejl: Kunne ikke afslutte reservationen.");
      return;
    }

    // Giv ReservationManager tid til at reagere (observer-mønsteret)
    System.out.println("Venter på at observer-mønsteret reagerer...");
    try {
      Thread.sleep(1000); // Vent 1 sekund
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // Tjek status EFTER
    System.out.println("\nStatus EFTER:");

    // Hent opdateret antal aktive reservationer
    int newActiveReservationCount = 0;
    for (Reservation res : reservationDAO.getAllReservations()) {
      if (res.getStatus() == ReservationStatusEnum.ACTIVE) {
        newActiveReservationCount++;
      }
    }

    // Hent opdateret kø-størrelse
    int newQueueSize = queueDAO.getQueueSize(laptopType);

    System.out.println("- Aktive reservationer: " + newActiveReservationCount);
    System.out.println("- Studerende i " + laptopType + " kø: " + newQueueSize);

    // Tjek om laptopen nu er tildelt til en ny student
    Laptop updatedLaptop = laptopDAO.getById(laptopId);

    if (updatedLaptop.isLoaned()) {
      System.out.println("\nLaptop blev automatisk tildelt til en ny student!");

      // Find den nye reservation
      Reservation newReservation = null;
      for (Reservation res : reservationDAO.getAllReservations()) {
        if (res.getStatus() == ReservationStatusEnum.ACTIVE &&
                res.getLaptop().getId().equals(laptopId)) {
          newReservation = res;
          break;
        }
      }

      if (newReservation != null) {
        System.out.println("Ny reservation oprettet med ID: " + newReservation.getReservationId());
        System.out.println("Student: " + newReservation.getStudent().getName() +
                " (VIA ID: " + newReservation.getStudent().getViaId() + ")");
      } else {
        System.out.println("ADVARSEL: Laptop er markeret som udlånt, men kunne ikke finde den nye reservation.");
      }
    } else {
      System.out.println("\nLaptop blev ikke automatisk tildelt til en ny student.");
      if (hasStudentsInQueue) {
        System.out.println("Dette er uventet, da der var studerende i køen. Observer-mønsteret fungerede ikke korrekt.");
      } else {
        System.out.println("Dette er forventet, da der ikke var studerende i " + laptopType + " køen.");
      }
    }
  }

}
