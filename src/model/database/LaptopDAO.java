package model.database;

import model.enums.PerformanceTypeEnum;
import model.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LaptopDAO {

    /**
     * Indsætter en ny laptop i databasen
     *
     * @param laptop Laptop objektet som skal indsættes
     * @return true hvis operationen lykkedes
     * @throws SQLException
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
     * Henter alle laptops fra databasen
     *
     * @return En liste af alle laptops i databasen
     * @throws SQLException
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
     * Henter en laptop fra databasen baseret på ID
     *
     * @param id ID'et på den laptop der skal hentes
     * @return Laptop objektet med det givne ID eller null hvis ikke fundet
     * @throws SQLException
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
     * Henter alle tilgængelige laptops med en given ydelsestype
     * @param performanceType Ydelsestypen som skal matches
     * @return Liste af tilgængelige laptops med den matchende ydelsestype
     * @throws SQLException
     */
    public List<Laptop> getAvailableLaptopsByPerformance(PerformanceTypeEnum performanceType) throws SQLException {
        List<Laptop> laptops = new ArrayList<>();
        String sql = "SELECT laptop_uuid, brand, model, gigabyte, ram, performance_type, state FROM Laptop WHERE performance_type = ? AND state = 'AvailableState'";

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
     * Opdaterer en laptop i databasen
     * @param laptop Laptop objekt med de nye værdier
     * @return true hvis operationen lykkedes
     * @throws SQLException
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
     * Mapper et ResultSet objekt til et Laptop objekt
     *
     * @param rs ResultSet objektet som skal mappes
     * @return Det Laptop objekt som er mappet fra ResultSet objektet
     * @throws SQLException
     */
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
