package model.database;

import model.enums.PerformanceTypeEnum;

import model.logic.reservationsLogic.ReservationManager;
import model.models.Laptop;
import model.models.AvailableState;
import model.models.LoanedState;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Data Access Object for Laptop entiteter med forbedret UUID håndtering
 */
public class LaptopDAO {
    private static final Logger logger = Logger.getLogger(LaptopDAO.class.getName());

    /**
     * Henter alle laptops fra databasen
     * @return Liste af laptops
     */
    public List<Laptop> getAllLaptops() throws SQLException {
        List<Laptop> laptops = new ArrayList<>();
        String sql = "SELECT laptop_uuid, brand, model, gigabyte, ram, performance_type, state FROM Laptop";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Laptop laptop = mapResultSetToLaptop(rs);
                laptops.add(laptop);
            }
        }
        return laptops;
    }

    /**
     * Henter laptop baseret på UUID
     * @param id Laptop UUID
     * @return Laptop object eller null hvis ikke fundet
     */
    public Laptop getById(UUID id) throws SQLException {
        String sql = "SELECT laptop_uuid, brand, model, gigabyte, ram, performance_type, state FROM Laptop WHERE laptop_uuid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLaptop(rs);
                }
            }
        }
        return null;
    }

    /**
     * Indsætter en ny laptop i databasen
     * @param laptop Laptop objekt
     * @return true hvis operationen lykkedes
     */
    public boolean insert(Laptop laptop) throws SQLException {
        String sql = "INSERT INTO Laptop (laptop_uuid, brand, model, gigabyte, ram, performance_type, state) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, laptop.getId().toString());
            stmt.setString(2, laptop.getBrand());
            stmt.setString(3, laptop.getModel());
            stmt.setInt(4, laptop.getGigabyte());
            stmt.setInt(5, laptop.getRam());
            stmt.setString(6, laptop.getPerformanceType().name());
            stmt.setString(7, laptop.getStateClassName());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Opdaterer en eksisterende laptop
     * @param laptop Laptop objekt med opdaterede oplysninger
     * @return true hvis operationen lykkedes
     */
    public boolean update(Laptop laptop) throws SQLException {
        String sql = "UPDATE Laptop SET brand = ?, model = ?, gigabyte = ?, ram = ?, performance_type = ?, state = ? WHERE laptop_uuid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, laptop.getBrand());
            stmt.setString(2, laptop.getModel());
            stmt.setInt(3, laptop.getGigabyte());
            stmt.setInt(4, laptop.getRam());
            stmt.setString(5, laptop.getPerformanceType().name());
            stmt.setString(6, laptop.getStateClassName());
            stmt.setString(7, laptop.getId().toString());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Opdaterer kun en laptops tilstand i databasen
     * @param laptop Laptop objekt med den nye tilstand
     * @return true hvis operationen lykkedes
     */
    public boolean updateState(Laptop laptop) throws SQLException {
        String sql = "UPDATE Laptop SET state = ? WHERE laptop_uuid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, laptop.getStateClassName());
            stmt.setString(2, laptop.getId().toString());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Sletter en laptop fra databasen
     * @param id Laptop UUID
     * @return true hvis operationen lykkedes
     */
    public boolean delete(UUID id) throws SQLException {
        String sql = "DELETE FROM Laptop WHERE laptop_uuid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Henter alle tilgængelige laptops med en specifik ydelsesfaktor
     * @param performanceType ydelsesfaktor at søge efter
     * @return Liste af tilgængelige laptops med den angivne ydelsesfaktor
     */
    public List<Laptop> getAvailableLaptopsByPerformance(PerformanceTypeEnum performanceType) throws SQLException {
        List<Laptop> laptops = new ArrayList<>();
        String sql = "SELECT laptop_uuid, brand, model, gigabyte, ram, performance_type, state FROM Laptop " +
                "WHERE performance_type = ? AND state = 'AvailableState'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, performanceType.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Laptop laptop = mapResultSetToLaptop(rs);
                    laptops.add(laptop);
                }
            }
        }
        return laptops;
    }

    /**
     * Forbedret metode til at konvertere ResultSet til Laptop objekt
     * Bruger UUID fra databasen i stedet for at generere et nyt
     */
    // I LaptopDAO.java, ændr mapResultSetToLaptop metoden hvis den indeholder ReservationManager referencer:
    private Laptop mapResultSetToLaptop(ResultSet rs) throws SQLException {
        UUID laptopId = UUID.fromString(rs.getString("laptop_uuid"));
        String brand = rs.getString("brand");
        String model = rs.getString("model");
        int gigabyte = rs.getInt("gigabyte");
        int ram = rs.getInt("ram");
        PerformanceTypeEnum performanceType = PerformanceTypeEnum.valueOf(rs.getString("performance_type"));

        // Fjern eventuelle kald der involverer ReservationManager her
        Laptop laptop = new Laptop(laptopId, brand, model, gigabyte, ram, performanceType);

        // Sæt tilstanden baseret på databaseværdien
        String stateName = rs.getString("state");
        if (stateName != null) {
            laptop.setStateFromDatabase(stateName);
        }

        return laptop;
    }
}