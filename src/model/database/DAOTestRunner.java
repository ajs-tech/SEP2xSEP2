package model.database;

import model.database.LaptopDAO;
import model.database.StudentDAO;
import model.database.ReservationDAO;
import model.enums.PerformanceTypeEnum;
import model.enums.ReservationStatusEnum;
import model.models.Laptop;
import model.models.Student;
import model.models.Reservation;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DAOTestRunner {
  private static final Logger logger = Logger.getLogger(DAOTestRunner.class.getName());

  public static void main(String[] args) {
    System.out.println("--- Starting DAO Manual Test Runner ---");

    LaptopDAO laptopDAO = new LaptopDAO();
    StudentDAO studentDAO = new StudentDAO();
    ReservationDAO reservationDAO = new ReservationDAO();
    QueueDAO queueDAO = new QueueDAO(); // Tilføjet QueueDAO for test

    // --- Test Data ---
    // Brug den nye konstruktør med UUID.randomUUID() i stedet for new UUID(6, 6)
    Laptop testLaptop = new Laptop("ManualTest", "DAO-Check", 256, 8, PerformanceTypeEnum.LOW);
    UUID laptopId = testLaptop.getId();
    System.out.println("Generated Test Laptop UUID: " + laptopId);

    // Use a unique VIA ID for testing
    int studentViaId = 999999;
    Student testStudent = new Student(
            "Test Student",
            new Date(System.currentTimeMillis() + 31536000000L), // Approx 1 year later
            "Test Degree",
            studentViaId,
            "test.student@example.com",
            12345670,
            PerformanceTypeEnum.LOW
    );

    Reservation testReservation = null; // To be created after student/laptop inserted
    UUID reservationId = null;

    // --- Clean Before Test (Optional but recommended) ---
    System.out.println(" --- Attempting Pre-Test Cleanup ---");
    try {
      if (reservationId != null) {
        reservationDAO.delete(reservationId);
      }
    } catch (Exception e) {
      System.out.println("No reservation to clean up: " + e.getMessage());
    }
    try {
      laptopDAO.delete(laptopId);
    } catch (Exception e) {
      System.out.println("No laptop to clean up: " + e.getMessage());
    }
    try {
      studentDAO.delete(studentViaId);
    } catch (Exception e) {
      System.out.println("No student to clean up: " + e.getMessage());
    }
    try {
      queueDAO.removeFromQueue(studentViaId, PerformanceTypeEnum.LOW);
    } catch (Exception e) {
      System.out.println("No queue entry to clean up: " + e.getMessage());
    }
    System.out.println("Cleanup attempted.");

    // --- LaptopDAO Test ---
    System.out.println(" --- Testing LaptopDAO ---");
    try {
      System.out.println("Inserting Laptop...");
      boolean inserted = laptopDAO.insert(testLaptop);
      System.out.println("Laptop Inserted: " + inserted);

      if (inserted) {
        System.out.println("Getting Laptop by ID: " + laptopId);
        Laptop retrieved = laptopDAO.getById(laptopId);
        if (retrieved != null) {
          System.out.println("Retrieved Laptop: " + retrieved.getBrand() + " " + retrieved.getModel() +
                  " (UUID: " + retrieved.getId() + ")");
          System.out.println("State: " + retrieved.getStateClassName());

          System.out.println("Updating Laptop...");
          retrieved.setModel("DAO-Check-Updated"); // Update the retrieved object
          boolean updated = laptopDAO.update(retrieved); // Pass the object with the DB's UUID
          System.out.println("Laptop Updated: " + updated);

          System.out.println("Getting Updated Laptop by ID: " + retrieved.getId());
          Laptop retrievedUpdated = laptopDAO.getById(retrieved.getId());
          if(retrievedUpdated != null) {
            System.out.println("Retrieved Updated Laptop Model: " + retrievedUpdated.getModel());
          } else {
            System.out.println("Failed to retrieve updated laptop.");
          }

        } else {
          System.out.println("Failed to retrieve laptop after insert.");
        }
      } else {
        System.out.println("Skipping further laptop tests due to insert failure.");
      }

    } catch (SQLException e) {
      System.err.println("LaptopDAO Error: " + e.getMessage());
      e.printStackTrace();
    }

    // --- StudentDAO Test ---
    System.out.println(" --- Testing StudentDAO ---");
    try {
      System.out.println("Inserting Student...");
      boolean inserted = studentDAO.insert(testStudent);
      System.out.println("Student Inserted: " + inserted);

      if (inserted) {
        System.out.println("Getting Student by ID: " + studentViaId);
        Student retrieved = studentDAO.getById(studentViaId);
        if (retrieved != null) {
          System.out.println("Retrieved Student: " + retrieved.getName() + " (VIA ID: " + retrieved.getViaId() + ")");

          System.out.println("Updating Student...");
          retrieved.setEmail("updated.student@example.com");
          boolean updated = studentDAO.update(retrieved);
          System.out.println("Student Updated: " + updated);

          System.out.println("Getting Updated Student by ID: " + studentViaId);
          Student retrievedUpdated = studentDAO.getById(studentViaId);
          if(retrievedUpdated != null) {
            System.out.println("Retrieved Updated Student Email: " + retrievedUpdated.getEmail());
          } else {
            System.out.println("Failed to retrieve updated student.");
          }
        } else {
          System.out.println("Failed to retrieve student after insert.");
        }
      } else {
        System.out.println("Skipping further student tests due to insert failure.");
      }

    } catch (SQLException e) {
      System.err.println("StudentDAO Error: " + e.getMessage());
      e.printStackTrace();
    }

    // --- QueueDAO Test ---
    System.out.println(" --- Testing QueueDAO ---");
    try {
      System.out.println("Adding Student to Queue...");
      boolean addedToQueue = queueDAO.addToQueue(testStudent, PerformanceTypeEnum.LOW);
      System.out.println("Student Added to Queue: " + addedToQueue);

      if (addedToQueue) {
        System.out.println("Getting Queue Size...");
        int queueSize = queueDAO.getQueueSize(PerformanceTypeEnum.LOW);
        System.out.println("Low Performance Queue Size: " + queueSize);

        System.out.println("Getting Students in Queue...");
        java.util.List<Student> studentsInQueue = queueDAO.getStudentsInQueue(PerformanceTypeEnum.LOW);
        System.out.println("Students in Queue: " + studentsInQueue.size());

        if (!studentsInQueue.isEmpty()) {
          System.out.println("First Student in Queue: " + studentsInQueue.get(0).getName());

          System.out.println("Getting and Removing Next Student in Queue...");
          Student nextStudent = queueDAO.getAndRemoveNextInQueue(PerformanceTypeEnum.LOW);
          if (nextStudent != null) {
            System.out.println("Next Student in Queue: " + nextStudent.getName());

            System.out.println("Checking Queue Size After Remove...");
            queueSize = queueDAO.getQueueSize(PerformanceTypeEnum.LOW);
            System.out.println("Low Performance Queue Size After Remove: " + queueSize);
          } else {
            System.out.println("Failed to get next student in queue.");
          }
        }
      } else {
        System.out.println("Skipping further queue tests due to insertion failure.");
      }
    } catch (SQLException e) {
      System.err.println("QueueDAO Error: " + e.getMessage());
      e.printStackTrace();
    }

    // --- ReservationDAO Test ---
    System.out.println(" --- Testing ReservationDAO ---");
    // Retrieve fresh student/laptop objects as they might have changed
    Laptop currentLaptop = null;
    Student currentStudent = null;
    try {
      currentLaptop = laptopDAO.getById(laptopId); // Use original ID
      currentStudent = studentDAO.getById(studentViaId);
    } catch (SQLException e) {
      System.err.println("Failed to retrieve student/laptop for reservation test: " + e.getMessage());
    }

    if (currentLaptop != null && currentStudent != null) {
      System.out.println("Laptop and Student retrieved for reservation test.");
      try {
        // Brug den nye konstruktør
        testReservation = new Reservation(currentStudent, currentLaptop);
        reservationId = testReservation.getReservationId();
        System.out.println("Generated Test Reservation UUID: " + reservationId);

        System.out.println("Inserting Reservation...");
        boolean inserted = reservationDAO.insert(testReservation);
        System.out.println("Reservation Inserted: " + inserted);

        if (inserted) {
          System.out.println("Getting Reservation by ID: " + reservationId);
          // Use the ID generated by the object we inserted
          Reservation retrieved = reservationDAO.getById(reservationId);
          if (retrieved != null) {
            System.out.println("Retrieved Reservation Status: " + retrieved.getStatus());
            System.out.println("  -> Laptop: " + retrieved.getLaptop().getModel());
            System.out.println("  -> Student: " + retrieved.getStudent().getName());

            System.out.println("Updating Reservation Status...");
            retrieved.changeStatus(ReservationStatusEnum.COMPLETED);
            boolean updated = reservationDAO.update(retrieved);
            System.out.println("Reservation Updated: " + updated);

            System.out.println("Getting Updated Reservation by ID: " + reservationId);
            Reservation retrievedUpdated = reservationDAO.getById(reservationId);
            if (retrievedUpdated != null) {
              System.out.println("Retrieved Updated Reservation Status: " + retrievedUpdated.getStatus());
            } else {
              System.out.println("Failed to retrieve updated reservation.");
            }
          } else {
            System.out.println("Failed to retrieve reservation after insert.");
            reservationId = null; // Can't delete if not retrieved
          }
        } else {
          System.out.println("Skipping further reservation tests due to insert failure.");
          reservationId = null;
        }
      } catch (SQLException e) {
        System.err.println("ReservationDAO Error: " + e.getMessage());
        e.printStackTrace();
        reservationId = null;
      }
    } else {
      System.out.println("Skipping Reservation tests: Could not retrieve prerequisite Laptop or Student.");
    }

    // --- Testing Transaction-based Methods ---
    System.out.println(" --- Testing Transaction-based Methods ---");
    try {
      // Først, nulstil laptop og student status
      if (currentLaptop != null) {
        currentLaptop.setStateFromDatabase("AvailableState");
        laptopDAO.updateState(currentLaptop);
      }
      if (currentStudent != null && currentStudent.isHasLaptop()) {
        currentStudent.setHasLaptopToOpposite();
        studentDAO.update(currentStudent);
      }

      // Opret en ny reservation med transaktionssupport
      if (currentLaptop != null && currentStudent != null) {
        System.out.println("Creating Reservation with Transaction...");
        Reservation transactionReservation = new Reservation(currentStudent, currentLaptop);
        boolean created = reservationDAO.createReservationWithTransaction(transactionReservation);
        System.out.println("Reservation Created with Transaction: " + created);

        // Tjek om laptop og student status blev opdateret korrekt
        if (created) {
          Laptop updatedLaptop = laptopDAO.getById(currentLaptop.getId());
          Student updatedStudent = studentDAO.getById(currentStudent.getViaId());

          System.out.println("Laptop State After Transaction: " + updatedLaptop.getStateClassName());
          System.out.println("Student Has Laptop After Transaction: " + updatedStudent.isHasLaptop());

          // Opdater reservation status med transaktionssupport
          System.out.println("Updating Reservation Status with Transaction...");
          transactionReservation.changeStatus(ReservationStatusEnum.COMPLETED);
          boolean updated = reservationDAO.updateStatusWithTransaction(transactionReservation);
          System.out.println("Reservation Updated with Transaction: " + updated);

          // Tjek om laptop og student status blev opdateret korrekt efter afslutning
          if (updated) {
            updatedLaptop = laptopDAO.getById(currentLaptop.getId());
            updatedStudent = studentDAO.getById(currentStudent.getViaId());

            System.out.println("Laptop State After Completion: " + updatedLaptop.getStateClassName());
            System.out.println("Student Has Laptop After Completion: " + updatedStudent.isHasLaptop());
          }
        }
      }
    } catch (SQLException e) {
      System.err.println("Transaction Test Error: " + e.getMessage());
      e.printStackTrace();
    }

    // --- Final Cleanup ---
    System.out.println("--- Attempting Post-Test Cleanup ---");
    try {
      if (reservationId != null) {
        System.out.println("Deleting Reservation: " + reservationId);
        boolean deleted = reservationDAO.delete(reservationId);
        System.out.println("Reservation Deleted: " + deleted);
      }
    } catch (SQLException e) {
      System.err.println("Cleanup Reservation Error: " + e.getMessage());
    }

    try {
      // Ryd op i alle reservationer for laptopen (inkl. dem fra transaktionstest)
      if (laptopId != null) {
        System.out.println("Cleaning up all reservations for laptop...");
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement("DELETE FROM Reservation WHERE laptop_uuid = ?")) {
          stmt.setString(1, laptopId.toString());
          int count = stmt.executeUpdate();
          System.out.println("Cleaned up " + count + " reservations");
        }
      }
    } catch (SQLException e) {
      System.err.println("Cleanup Extra Reservations Error: " + e.getMessage());
    }

    try {
      System.out.println("Deleting Laptop: " + laptopId);
      boolean deleted = laptopDAO.delete(laptopId);
      System.out.println("Laptop Deleted: " + deleted);
    } catch (SQLException e) {
      System.err.println("Cleanup Laptop Error: " + e.getMessage());
    }

    try {
      System.out.println("Deleting Student: " + studentViaId);
      boolean deleted = studentDAO.delete(studentViaId);
      System.out.println("Student Deleted: " + deleted);
    } catch (SQLException e) {
      System.err.println("Cleanup Student Error: " + e.getMessage());
    }

    try {
      System.out.println("Cleaning up any queue entries...");
      queueDAO.removeFromQueue(studentViaId, PerformanceTypeEnum.LOW);
      queueDAO.removeFromQueue(studentViaId, PerformanceTypeEnum.HIGH);
    } catch (SQLException e) {
      System.err.println("Cleanup Queue Error: " + e.getMessage());
    }

    System.out.println("DAO Manual Test Runner Finished");
  }
}