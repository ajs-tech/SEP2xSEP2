package model.database;

import model.enums.PerformanceTypeEnum;
import model.models.Laptop;
import model.models.AvailableState;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.lang.reflect.Field;

/**
 * Data Access Object for Laptop entities
 * Forbedret med korrekt UUID håndtering
 */
public class LaptopDAO {

    /**
     * Henter alle laptops fra databasen
     * @return Liste af laptops
     */
    public List<Laptop> getAllLaptops() throws SQLException {
        List<Laptop> laptops = new ArrayList<>();
        String sql = "SELECT laptop_uuid, brand, model, gigabyte, ram, performance_type FROM Laptop";

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
        String sql = "SELECT laptop_uuid, brand, model, gigabyte, ram, performance_type FROM Laptop WHERE laptop_uuid = ?";

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
     * @param laptop Laptop objekt (ID is assumed to be generated in constructor)
     * @return true hvis operationen lykkedes
     */
    public boolean insert(Laptop laptop) throws SQLException {
        String sql = "INSERT INTO Laptop (laptop_uuid, brand, model, gigabyte, ram, performance_type) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, laptop.getId().toString());
            stmt.setString(2, laptop.getBrand());
            stmt.setString(3, laptop.getModel());
            stmt.setInt(4, laptop.getGigabyte());
            stmt.setInt(5, laptop.getRam());
            stmt.setString(6, laptop.getPerformanceType().name());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Opdaterer en eksisterende laptop
     * @param laptop Laptop objekt med opdaterede oplysninger (contains the UUID)
     * @return true hvis operationen lykkedes
     */
    public boolean update(Laptop laptop) throws SQLException {
        String sql = "UPDATE Laptop SET brand = ?, model = ?, gigabyte = ?, ram = ?, performance_type = ? WHERE laptop_uuid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, laptop.getBrand());
            stmt.setString(2, laptop.getModel());
            stmt.setInt(3, laptop.getGigabyte());
            stmt.setInt(4, laptop.getRam());
            stmt.setString(5, laptop.getPerformanceType().name());
            stmt.setString(6, laptop.getId().toString());

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
        // Først slet afhængige reservationer
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM Reservation WHERE laptop_uuid = ?")) {
            stmt.setString(1, id.toString());
            stmt.executeUpdate(); // Vi ignorerer resultatet, da der måske ikke er nogen reservationer
        } catch (SQLException e) {
            System.err.println("Advarsel: Kunne ikke slette reservationer for laptop " + id + ": " + e.getMessage());
            // Vi fortsætter alligevel - måske var der ingen reservationer
        }

        // Så slet selve laptop
        String sql = "DELETE FROM Laptop WHERE laptop_uuid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Hjælpemetode til at konvertere ResultSet til Laptop objekt
     * Løser UUID-problemet ved at bruge reflection til at sætte ID'et korrekt
     */
    private Laptop mapResultSetToLaptop(ResultSet rs) throws SQLException {
        UUID laptopId = UUID.fromString(rs.getString("laptop_uuid"));
        String brand = rs.getString("brand");
        String model = rs.getString("model");
        int gigabyte = rs.getInt("gigabyte");
        int ram = rs.getInt("ram");
        PerformanceTypeEnum performanceType = PerformanceTypeEnum.valueOf(rs.getString("performance_type"));

        // Skab ny laptop instans
        Laptop laptop = new Laptop(brand, model, gigabyte, ram, performanceType);

        // Brug reflection til at sætte UUID til det faktiske UUID fra databasen
        try {
            Field idField = Laptop.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(laptop, laptopId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("ADVARSEL: Kunne ikke sætte korrekt UUID på laptop. Dette kan forårsage problemer: " + e.getMessage());
        }

        // Sæt state til AvailableState (dette bør ideelt tjekkes mod reservationer)
        laptop.changeState(new AvailableState());

        return laptop;
    }
}