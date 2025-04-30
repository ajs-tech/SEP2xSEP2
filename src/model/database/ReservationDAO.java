package model.database;

import model.models.Laptop;
import model.models.Student;
import model.models.Reservation;
import model.enums.ReservationStatusEnum;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.lang.reflect.Field;

/**
 * Data Access Object for Reservation entities
 * Forbedret med korrekt status-håndtering
 */
public class ReservationDAO {

    private LaptopDAO laptopDAO = new LaptopDAO();
    private StudentDAO studentDAO = new StudentDAO();

    /**
     * Henter alle reservationer fra databasen
     * @return Liste af reservationer
     */
    public List<Reservation> getAllReservations() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.reservation_uuid, r.status, r.laptop_uuid, r.student_via_id FROM Reservation r";

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
        String sql = "SELECT reservation_uuid, status, laptop_uuid, student_via_id FROM Reservation WHERE reservation_uuid = ?";

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
     * Henter alle reservationer for en bestemt student (using VIA ID)
     * @param studentViaId Student VIA ID
     * @return Liste af reservationer for den pågældende student
     */
    public List<Reservation> getByStudentId(int studentViaId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT reservation_uuid, status, laptop_uuid, student_via_id FROM Reservation WHERE student_via_id = ?";

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
        String sql = "INSERT INTO Reservation (reservation_uuid, laptop_uuid, student_via_id, status) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reservation.getReservationId().toString());
            stmt.setString(2, reservation.getLaptop().getId().toString());
            stmt.setInt(3, reservation.getStudent().getViaId());

            // Konverter enum til uppercase streng for database
            stmt.setString(4, convertStatusForDB(reservation.getStatus()));

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Opdaterer en eksisterende reservation (primarily status)
     * @param reservation Reservation objekt med opdaterede oplysninger
     * @return true hvis operationen lykkedes
     */
    public boolean update(Reservation reservation) throws SQLException {
        String sql = "UPDATE Reservation SET status = ? WHERE reservation_uuid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Konverter enum til uppercase streng for database
            stmt.setString(1, convertStatusForDB(reservation.getStatus()));
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
     * Hjælpemetode til at konvertere ResultSet til Reservation objekt
     */
    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        UUID reservationId = UUID.fromString(rs.getString("reservation_uuid"));
        String statusStr = rs.getString("status");
        ReservationStatusEnum status = convertDBStatusToEnum(statusStr);
        UUID laptopUUID = UUID.fromString(rs.getString("laptop_uuid"));
        int studentViaId = rs.getInt("student_via_id");

        // Hent associated Laptop and Student via DAOs
        Laptop laptop = laptopDAO.getById(laptopUUID);
        Student student = studentDAO.getById(studentViaId);

        // Tjek om Laptop og Student blev fundet
        if (laptop == null) {
            System.err.println("Warning: Laptop with UUID " + laptopUUID + " not found for reservation " + reservationId);
            return null;
        }
        if (student == null) {
            System.err.println("Warning: Student with VIA ID " + studentViaId + " not found for reservation " + reservationId);
            return null;
        }

        // Opret Reservation objekt
        Reservation reservation = new Reservation(student, laptop);

        // Manuelt overskrid det genererede ID og status med værdier fra DB
        try {
            Field idField = Reservation.class.getDeclaredField("reservationID");
            idField.setAccessible(true);
            idField.set(reservation, reservationId);

            // Sæt status via den offentlige metode
            reservation.changeStatus(status);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Error: Failed to set reservationID via reflection for reservation " + reservationId);
            throw new SQLException("Failed to map Reservation due to reflection error", e);
        }

        return reservation;
    }

    /**
     * Konverterer ReservationStatusEnum til korrekt database-værdi
     */
    private String convertStatusForDB(ReservationStatusEnum status) {
        // Konverter enum værdier til uppercase database-værdier
        switch (status) {
            case ACTIVE:
                return "ACTIVE";
            case COMPLETED:
                return "COMPLETED";
            case CANCELLED:
                return "CANCELLED";
            default:
                return status.name().toUpperCase();
        }
    }

    /**
     * Konverterer database status string til den korrekte enum værdi
     */
    private ReservationStatusEnum convertDBStatusToEnum(String dbStatus) {
        if (dbStatus == null) {
            return ReservationStatusEnum.ACTIVE; // Default værdi
        }

        // Håndter både uppercase og lowercase værdier fra databasen
        switch (dbStatus.toUpperCase()) {
            case "ACTIVE":
                return ReservationStatusEnum.ACTIVE;
            case "COMPLETED":
                return ReservationStatusEnum.COMPLETED;
            case "CANCELLED":
                return ReservationStatusEnum.CANCELLED;
            default:
                System.err.println("Warning: Unknown reservation status '" + dbStatus + "' in database");
                return ReservationStatusEnum.ACTIVE; // Default til Active hvis ukendt
        }
    }
}