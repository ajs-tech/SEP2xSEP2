package model.logic.reservationsLogic;

import model.database.QueueDAO;
import model.database.ReservationDAO;
import model.database.LaptopDAO;
import model.database.StudentDAO;
import model.enums.PerformanceTypeEnum;
import model.enums.ReservationStatusEnum;
import model.log.Log;
import model.models.Laptop;
import model.models.LoanedState;
import model.models.Reservation;
import model.models.Student;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * ReservationManager med database integration og kø persistering
 */
public class ReservationManager implements PropertyChangeListener {
    private static final Logger logger = Logger.getLogger(ReservationManager.class.getName());
    private final List<Reservation> reservationList;
    private final QueueForLowPowerLaptops lowPerformanceQueue;
    private final QueueForHighPowerLaptops highPerformanceQueue;
    private final ReservationFactory reservationFactory;
    private final QueueDAO queueDAO;
    private final ReservationDAO reservationDAO;
    private final LaptopDAO laptopDAO;
    private final StudentDAO studentDAO;
    private final Log log;

    public ReservationManager() {
        // Initialize all instance variables FIRST.
        reservationList = new ArrayList<>();
        lowPerformanceQueue = new QueueForLowPowerLaptops();
        highPerformanceQueue = new QueueForHighPowerLaptops();
        reservationFactory = new ReservationFactory();
        queueDAO = new QueueDAO();
        reservationDAO = new ReservationDAO();
        laptopDAO = new LaptopDAO();
        studentDAO = new StudentDAO();
        log = Log.getInstance();

        // Then, proceed with operations that might throw exceptions.
        try {
            // Omslut med try-catch for at undgå krasn ved startup
            try {
                loadReservationsFromDatabase();
                loadQueuesFromDatabase();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Fejl ved indlæsning fra database: " + e.getMessage(), e);
                log.addToLog("Fejl ved indlæsning fra database: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Kritisk fejl ved initialisering af ReservationManager: " + e.getMessage(), e);
            log.addToLog("Kritisk fejl ved initialisering af ReservationManager: " + e.getMessage()); // Added more detailed logging
        }
    }

    private void loadReservationsFromDatabase() throws SQLException {
        try {
            List<Reservation> dbReservations = reservationDAO.getAllReservations();
            for (Reservation reservation : dbReservations) {
                if (reservation.getStatus() == ReservationStatusEnum.ACTIVE) {
                    reservationList.add(reservation);
                }
            }
            logger.info("Indlæst " + reservationList.size() + " aktive reservationer fra databasen");
            log.addToLog("Indlæst " + reservationList.size() + " aktive reservationer fra databasen");
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Fejl ved indlæsning af reservationer: " + e.getMessage());
            log.addToLog("Fejl ved indlæsning af reservationer: " + e.getMessage());
            // Rethrow så den overordnede try-catch kan håndtere det
            throw e;
        }
    }

    private void loadQueuesFromDatabase() throws SQLException {
        // Indlæs lav-ydelses kø
        List<Student> lowPerformanceStudents = queueDAO.getStudentsInQueue(PerformanceTypeEnum.LOW);
        for (Student student : lowPerformanceStudents) {
            lowPerformanceQueue.addToLowPerformanceQueue(student);
        }

        // Indlæs høj-ydelses kø
        List<Student> highPerformanceStudents = queueDAO.getStudentsInQueue(PerformanceTypeEnum.HIGH);
        for (Student student : highPerformanceStudents) {
            highPerformanceQueue.addToHighPerformanceQueue(student);
        }

        logger.info("Indlæst " + lowPerformanceStudents.size() + " studerende i lav-ydelses kø");
        logger.info("Indlæst " + highPerformanceStudents.size() + " studerende i høj-ydelses kø");
        log.addToLog("Indlæst køer fra databasen: " + lowPerformanceStudents.size() +
                " i lav-ydelses kø, " + highPerformanceStudents.size() + " i høj-ydelses kø");
    }

    public Reservation createReservation(Laptop laptop, Student student) {
        try {
            // Opret reservation objekt
            Reservation reservation = reservationFactory.createReservation(laptop, student);

            // Gem i databasen med transaction support
            boolean success = reservationDAO.createReservationWithTransaction(reservation);

            if (success) {
                // Opdater in-memory liste
                reservationList.add(reservation);

                // Opdater in-memory objekter
                laptop.changeState(new LoanedState());
                student.setHasLaptopToOpposite();

                // Log handlingen
                log.addToLog("Reservation oprettet: " + laptop.getBrand() + " " +
                        laptop.getModel() + " til " + student.getName());

                return reservation;
            } else {
                logger.warning("Kunne ikke oprette reservation i databasen");
                log.addToLog("Fejl: Kunne ikke oprette reservation i databasen");
                return null;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Fejl ved oprettelse af reservation: " + e.getMessage(), e);
            log.addToLog("Fejl ved oprettelse af reservation: " + e.getMessage());
            return null;
        }
    }

    public boolean updateReservationStatus(UUID reservationId, ReservationStatusEnum newStatus) {
        try {
            // Find reservationen i hukommelsen
            Reservation reservation = null;
            for (Reservation r : reservationList) {
                if (r.getReservationId().equals(reservationId)) {
                    reservation = r;
                    break;
                }
            }

            if (reservation == null) {
                // Hvis ikke i hukommelsen, prøv at hente fra databasen
                reservation = reservationDAO.getById(reservationId);
                if (reservation == null) {
                    return false;
                }
                // Add to in memory list if found in database
                reservationList.add(reservation); // Added
            }

            // Opdater status
            reservation.changeStatus(newStatus);

            // Opdater i databasen med transaktionssupport
            boolean success = reservationDAO.updateStatusWithTransaction(reservation);

            if (success) {
                // Fjern fra in-memory listen hvis cancelled/completed
                if (newStatus == ReservationStatusEnum.CANCELLED ||
                        newStatus == ReservationStatusEnum.COMPLETED) {
                    reservationList.removeIf(r -> r.getReservationId().equals(reservationId));
                }

                log.addToLog("Reservation " + reservationId + " opdateret til status: " + newStatus);
                return true;
            } else {
                logger.warning("Kunne ikke opdatere reservation i database");
                log.addToLog("Fejl: Kunne ikke opdatere reservation i database");
                return false;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Fejl ved opdatering af reservation: " + e.getMessage(), e);
            log.addToLog("Fejl ved opdatering af reservation: " + e.getMessage());
            return false;
        }
    }

    public void addToHighPerformanceQueue(Student student) {
        try {
            // check if student is already in any queue or has a laptop, if so return.
            if(student.isHasLaptop() || queueDAO.isStudentInAnyQueue(student.getViaId()))
            {
                logger.info("Student " + student.getName() + " har allerede en laptop eller er i en anden kø, tilføjes ikke til kø");
                log.addToLog("Student " + student.getName() + " har allerede en laptop eller er i en anden kø, tilføjes ikke til kø");
                return;
            }
            // Tjek om studentens ydelsesbehov passer til køen
            if (student.getPerformanceNeeded() != PerformanceTypeEnum.HIGH) {
                logger.info("Student " + student.getName() + " har ikke behov for høj ydelse, omdirigerer");
                addToLowPerformanceQueue(student);
                return;
            }

            // Tilføj til database kø
            boolean added = queueDAO.addToQueue(student, PerformanceTypeEnum.HIGH);

            if (added) {
                // Tilføj til in-memory kø
                highPerformanceQueue.addToHighPerformanceQueue(student);
                log.addToLog("Student " + student.getName() + " tilføjet til høj-ydelses kø");

                // Tjek om der er en tilgængelig laptop med det rette ydelsesniveau
                try {
                    List<Laptop> availableLaptops = laptopDAO.getAvailableLaptopsByPerformance(PerformanceTypeEnum.HIGH);
                    if (!availableLaptops.isEmpty()) {
                        // Der er en tilgængelig laptop, tildel den med det samme
                        Laptop laptop = availableLaptops.get(0);

                        // Fjern studenten fra køen in-memory
                        Student removedStudent = highPerformanceQueue.getAndRemoveNextInLineForHighPerformance(); // Changed
                        // Fjern student fra databasen.
                        queueDAO.removeFromQueue(student.getViaId(), PerformanceTypeEnum.HIGH); // Changed
                        // Opret reservation
                        createReservation(laptop, student);
                    }
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Fejl ved tjek af tilgængelige laptops: " + e.getMessage(), e);
                }
            } else {
                logger.warning("Kunne ikke tilføje student til kø i databasen");
                log.addToLog("Fejl: Kunne ikke tilføje student til kø i databasen");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Fejl ved tilføjelse til høj-ydelses kø: " + e.getMessage(), e);
            log.addToLog("Fejl ved tilføjelse til høj-ydelses kø: " + e.getMessage());
        }
    }

    public void addToLowPerformanceQueue(Student student) {
        try {
            // check if student is already in any queue or has a laptop, if so return.
            if(student.isHasLaptop() || queueDAO.isStudentInAnyQueue(student.getViaId()))
            {
                logger.info("Student " + student.getName() + " har allerede en laptop eller er i en anden kø, tilføjes ikke til kø");
                log.addToLog("Student " + student.getName() + " har allerede en laptop eller er i en anden kø, tilføjes ikke til kø");
                return;
            }
            // Tjek om studentens ydelsesbehov passer til køen
            if (student.getPerformanceNeeded() != PerformanceTypeEnum.LOW) {
                logger.info("Student " + student.getName() + " har behov for høj ydelse, omdirigerer");
                addToHighPerformanceQueue(student);
                return;
            }

            // Tilføj til database kø
            boolean added = queueDAO.addToQueue(student, PerformanceTypeEnum.LOW);

            if (added) {
                // Tilføj til in-memory kø
                lowPerformanceQueue.addToLowPerformanceQueue(student);
                log.addToLog("Student " + student.getName() + " tilføjet til lav-ydelses kø");

                // Tjek om der er en tilgængelig laptop med det rette ydelsesniveau
                try {
                    List<Laptop> availableLaptops = laptopDAO.getAvailableLaptopsByPerformance(PerformanceTypeEnum.LOW);
                    if (!availableLaptops.isEmpty()) {
                        // Der er en tilgængelig laptop, tildel den med det samme
                        Laptop laptop = availableLaptops.get(0);
                        // Fjern først fra køen
                        Student removedStudent = lowPerformanceQueue.getAndRemoveNextInLineForLowPerformance(); // Changed
                        queueDAO.removeFromQueue(student.getViaId(), PerformanceTypeEnum.LOW); // Changed

                        // Opret reservation
                        createReservation(laptop, student);
                    }
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Fejl ved tjek af tilgængelige laptops: " + e.getMessage(), e);
                }
            } else {
                logger.warning("Kunne ikke tilføje student til kø i databasen");
                log.addToLog("Fejl: Kunne ikke tilføje student til kø i databasen");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Fejl ved tilføjelse til lav-ydelses kø: " + e.getMessage(), e);
            log.addToLog("Fejl ved tilføjelse til lav-ydelses kø: " + e.getMessage());
        }
    }

    public int getHighNeedingQueueSize() {
        try {
            return queueDAO.getQueueSize(PerformanceTypeEnum.HIGH);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Fejl ved hentning af høj-ydelses kø størrelse: " + e.getMessage(), e);
            log.addToLog("WARNING: Fejl ved hentning af høj-ydelses kø størrelse: " + e.getMessage() + ". Returning in-memory queue size as fallback.");// Added more specific log
            // Returner in-memory størrelse som fallback
            return highPerformanceQueue.getHighNeedingQueueSize();
        }
    }

    public int getLowNeedingQueueSize() {
        try {
            return queueDAO.getQueueSize(PerformanceTypeEnum.LOW);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Fejl ved hentning af lav-ydelses kø størrelse: " + e.getMessage(), e);
            log.addToLog("WARNING: Fejl ved hentning af lav-ydelses kø størrelse: " + e.getMessage() + ". Returning in-memory queue size as fallback.");// Added more specific log
            // Returner in-memory størrelse som fallback
            return lowPerformanceQueue.getLowNeedingQueueSize();
        }
    }

    public int getAmountOfReservationsToDate() {
        try {
            return reservationDAO.getAllReservations().size();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Fejl ved hentning af alle reservationer: " + e.getMessage(), e);
            log.addToLog("WARNING: Fejl ved hentning af alle reservationer: " + e.getMessage() + ". Returning in-memory list size as fallback."); // Added more specific log
            // Returner in-memory størrelse som fallback
            return reservationList.size();
        }
    }

    public int getAmountOfActiveReservations() {
        return reservationList.size();
    }

    public ArrayList<Reservation> getAllActiveReservations() {
        return new ArrayList<>(reservationList);
    }

    public Reservation getLastReservationAdded() {
        if (reservationList.isEmpty()) {
            return null;
        }
        return reservationList.get(reservationList.size() - 1);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("toAvailableState".equals(evt.getPropertyName())) {
            Laptop laptop = (Laptop) evt.getSource();
            logger.info("Laptop " + laptop.getId() + " er blevet tilgængelig, tjekker køer");
            log.addToLog("Laptop " + laptop.getBrand() + " " + laptop.getModel() + " er blevet tilgængelig");

            try {
                // Opdater laptop tilstand i databasen
                laptopDAO.updateState(laptop);

                // Tjek om der er nogle studerende i køen til denne laptops ydelsestype
                PerformanceTypeEnum performanceType = laptop.getPerformanceType();
                int queueSize = queueDAO.getQueueSize(performanceType);

                if (queueSize > 0 && laptop.isAvailable()) {
                    // Hent næste student fra databasen
                    Student nextStudent = queueDAO.getAndRemoveNextInQueue(performanceType);

                    if (nextStudent != null) {
                        // Opdater in-memory kø
                        if (performanceType == PerformanceTypeEnum.LOW) {
                            lowPerformanceQueue.getAndRemoveNextInLineForLowPerformance();
                        } else {
                            highPerformanceQueue.getAndRemoveNextInLineForHighPerformance();
                        }

                        // Opret reservation
                        createReservation(laptop, nextStudent);
                    }
                }
                else
                {
                    logger.info("No student found in database to assign to laptop" + laptop.getId());
                    log.addToLog("No student found in database to assign to laptop" + laptop.getId());
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Fejl ved håndtering af tilgængelig laptop: " + e.getMessage(), e);
                log.addToLog("Fejl ved håndtering af tilgængelig laptop: " + e.getMessage());
            }
        }
    }
}
