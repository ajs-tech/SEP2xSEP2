package model.database;

import model.models.Laptop;
import model.models.Student;
import model.models.Reservation;
import model.enums.ReservationStatusEnum;

import java.util.Date; // Tilføjet import for java.util.Date
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Data Access Object for Reservation entiteter med transaktion support
 */
public class ReservationDAO {
    private static final Logger logger = Logger.getLogger(ReservationDAO.class.getName());

    // DAO dependencies
    private LaptopDAO laptopDAO = new LaptopDAO();
    private StudentDAO studentDAO = new StudentDAO();

    /**
     * Henter alle reservationer fra databasen
     * @return Liste af reservationer
     */
    public List<Reservation> getAllReservations() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.reservation_uuid, r.status, r.laptop_uuid, r.student_via_id, r.creation_date " +
                "FROM Reservation r";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Reservation reservation = mapResultSetToReservation(rs);
                if (reservation != null) {
                    reservations.add(reservation);
                }
            }
        }
        return reservations;
    }

    /**
     * Henter en specifik reservation baseret på UUID
     * @param id Reservation UUID
     * @return Reservation objekt eller null hvis ikke fundet
     */
    public Reservation getById(UUID id) throws SQLException {
        String sql = "SELECT reservation_uuid, status, laptop_uuid, student_via_id, creation_date " +
                "FROM Reservation WHERE reservation_uuid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReservation(rs);
                }
            }
        }
        return null;
    }

    /**
     * Henter alle reservationer for en bestemt student
     * @param studentViaId Student VIA ID
     * @return Liste af reservationer for den pågældende student
     */
    public List<Reservation> getByStudentId(int studentViaId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT reservation_uuid, status, laptop_uuid, student_via_id, creation_date " +
                "FROM Reservation WHERE student_via_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentViaId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = mapResultSetToReservation(rs);
                    if (reservation != null) {
                        reservations.add(reservation);
                    }
                }
            }
        }
        return reservations;
    }

    /**
     * Indsætter en ny reservation i databasen
     * @param reservation Reservation objekt
     * @return true hvis operationen lykkedes
     */
    public boolean insert(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO Reservation (reservation_uuid, laptop_uuid, student_via_id, status, creation_date) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reservation.getReservationId().toString());
            stmt.setString(2, reservation.getLaptop().getId().toString());
            stmt.setInt(3, reservation.getStudent().getViaId());
            stmt.setString(4, reservation.getStatus().name());
            stmt.setTimestamp(5, new Timestamp(reservation.getCreationDate().getTime()));

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Opdaterer en eksisterende reservation
     * @param reservation Reservation objekt med opdaterede oplysninger
     * @return true hvis operationen lykkedes
     */
    public boolean update(Reservation reservation) throws SQLException {
        String sql = "UPDATE Reservation SET status = ? WHERE reservation_uuid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reservation.getStatus().name());
            stmt.setString(2, reservation.getReservationId().toString());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Sletter en reservation baseret på UUID
     * @param reservationId Reservation UUID
     * @return true hvis operationen lykkedes
     */
    public boolean delete(UUID reservationId) throws SQLException {
        String sql = "DELETE FROM Reservation WHERE reservation_uuid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reservationId.toString());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Opret reservation med transaktionssupport, der opdaterer laptop og student status
     * @param reservation Reservationsobjekt at oprette
     * @return true hvis operationen lykkedes
     */
    public boolean createReservationWithTransaction(Reservation reservation) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Indsæt reservation
            String sql = "INSERT INTO Reservation (reservation_uuid, laptop_uuid, student_via_id, status, creation_date) " +
                    "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, reservation.getReservationId().toString());
                stmt.setString(2, reservation.getLaptop().getId().toString());
                stmt.setInt(3, reservation.getStudent().getViaId());
                stmt.setString(4, reservation.getStatus().name());
                stmt.setTimestamp(5, new Timestamp(reservation.getCreationDate().getTime()));
                stmt.executeUpdate();
            }

            // 2. Opdater laptop tilstand
            sql = "UPDATE Laptop SET state = 'LoanedState' WHERE laptop_uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, reservation.getLaptop().getId().toString());
                stmt.executeUpdate();
            }

            // 3. Opdater student has_laptop
            sql = "UPDATE Student SET has_laptop = TRUE WHERE via_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, reservation.getStudent().getViaId());
                stmt.executeUpdate();
            }

            // Commit transaktionen
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.log(Level.WARNING, "Transaction rolled back: " + e.getMessage());
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error during rollback: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Error resetting connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Opdater reservationsstatus med transaktion, der også opdaterer laptop og student status
     * @param reservation Reservationsobjekt med den nye status
     * @return true hvis operationen lykkedes
     */
    public boolean updateStatusWithTransaction(Reservation reservation) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Hent den nuværende status
            String selectSql = "SELECT status FROM Reservation WHERE reservation_uuid = ?";
            ReservationStatusEnum currentStatus;
            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setString(1, reservation.getReservationId().toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return false; // Reservation findes ikke
                    }
                    currentStatus = ReservationStatusEnum.valueOf(rs.getString("status"));
                }
            }

            // 2. Opdater reservation status
            String updateSql = "UPDATE Reservation SET status = ? WHERE reservation_uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, reservation.getStatus().name());
                stmt.setString(2, reservation.getReservationId().toString());
                stmt.executeUpdate();
            }

            // 3. Hvis status ændres fra Active til completed eller cancelled
            if (currentStatus == ReservationStatusEnum.ACTIVE &&
                    (reservation.getStatus() == ReservationStatusEnum.COMPLETED ||
                            reservation.getStatus() == ReservationStatusEnum.CANCELLED)) {

                // Opdater laptop tilstand til Available
                String laptopSql = "UPDATE Laptop SET state = 'AvailableState' WHERE laptop_uuid = ?";
                try (PreparedStatement stmt = conn.prepareStatement(laptopSql)) {
                    stmt.setString(1, reservation.getLaptop().getId().toString());
                    stmt.executeUpdate();
                }

                // Opdater student has_laptop status
                String studentSql = "UPDATE Student SET has_laptop = FALSE WHERE via_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(studentSql)) {
                    stmt.setInt(1, reservation.getStudent().getViaId());
                    stmt.executeUpdate();
                }
            }

            // Commit transaktionen
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.log(Level.WARNING, "Transaction rolled back: " + e.getMessage());
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error during rollback: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Error resetting connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Forbedret metode til at konvertere ResultSet til Reservation objekt uden reflection
     */
    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        UUID reservationId = UUID.fromString(rs.getString("reservation_uuid"));
        ReservationStatusEnum status = ReservationStatusEnum.valueOf(rs.getString("status"));
        UUID laptopUUID = UUID.fromString(rs.getString("laptop_uuid"));
        int studentViaId = rs.getInt("student_via_id");
        Timestamp creationTimestamp = rs.getTimestamp("creation_date");
        Date creationDate = creationTimestamp != null ? new Date(creationTimestamp.getTime()) : new Date();

        // Hent tilknyttet Laptop og Student ved hjælp af deres DAOs
        Laptop laptop = laptopDAO.getById(laptopUUID);
        Student student = studentDAO.getById(studentViaId);

        // Tjek om Laptop og Student blev fundet
        if (laptop == null) {
            logger.log(Level.WARNING, "Laptop med UUID " + laptopUUID + " blev ikke fundet til reservation " + reservationId);
            return null;
        }
        if (student == null) {
            logger.log(Level.WARNING, "Student med VIA ID " + studentViaId + " blev ikke fundet til reservation " + reservationId);
            return null;
        }

        // Opret Reservation objekt med den korrekte konstruktør
        return new Reservation(reservationId, student, laptop, status, creationDate);
    }
}