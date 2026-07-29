package repository;

import model.VehicleFinancing;
import model.FinancingStatus;
import model.AmortizationType;
import model.VehicleType;
import model.VehicleCondition;
import service.Session;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VehicleFinancingRepository {

    public void saveFinancing(VehicleFinancing financing) throws SQLException {

        String sql = """
                INSERT INTO vehicle_financing
                (financed_amount, loan_term_months, annual_interest_rate, amortization_type, vehicle_type, vehicle_condition, financing_status, user_id, vehicle_value, down_payment, brand, model, manufacture_year, mileage) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setBigDecimal(1, financing.getFinancedAmount());
            stmt.setInt(2, financing.getLoanTermInMonths());
            stmt.setBigDecimal(3, financing.getAnnualInterestRate());
            stmt.setString(4, financing.getAmortizationType().name());
            stmt.setString(5, financing.getVehicleType().name());
            stmt.setString(6, financing.getVehicleCondition().name());
            stmt.setString(7, financing.getStatus().name());
            stmt.setInt(8, financing.getUserId());
            stmt.setObject(9, financing.getVehicleValue(), java.sql.Types.DECIMAL);
            stmt.setObject(10, financing.getDownPayment(), java.sql.Types.DECIMAL);
            stmt.setString(11, financing.getBrand());
            stmt.setString(12, financing.getModel());
            stmt.setObject(13, financing.getManufactureYear(), java.sql.Types.INTEGER);
            stmt.setObject(14, financing.getMileage(), java.sql.Types.INTEGER);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {

                if (rs.next()) {
                    financing.setFinancingId(rs.getInt(1));
                }

            }

        }
    }

    public List<VehicleFinancing> findAllByUser() throws SQLException {

        String sql = """
        SELECT financing_id,
               financed_amount,
               loan_term_months,
               annual_interest_rate,
               amortization_type,
               vehicle_type,
               vehicle_condition,
               financing_status
        FROM vehicle_financing
        WHERE user_id = ?
    """;

        List<VehicleFinancing> financings = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Session.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    VehicleFinancing financing =
                            new VehicleFinancing(
                                    rs.getBigDecimal("financed_amount"),
                                    rs.getInt("loan_term_months"),
                                    rs.getBigDecimal("annual_interest_rate"),
                                    AmortizationType.valueOf(rs.getString("amortization_type")),
                                    VehicleType.valueOf(rs.getString("vehicle_type")),
                                    VehicleCondition.valueOf(rs.getString("vehicle_condition")),
                                    FinancingStatus.valueOf(rs.getString("financing_status")),
                                    Session.getUserId()
                            );

                    financing.setFinancingId(rs.getInt("financing_id"));
                    financings.add(financing);
                }
            }
        }

        return financings;
    }

    public VehicleFinancing findById(int financingId) throws SQLException {

        String sql = """
                SELECT financing_id,
                 financed_amount,
                 loan_term_months,
                 annual_interest_rate,
                 amortization_type,
                 vehicle_type,
                 vehicle_condition,
                 financing_status,
                 vehicle_value,
                 down_payment,
                 brand,
                 model,
                 manufacture_year,
                 mileage,
                 user_id
          FROM vehicle_financing
          WHERE financing_id = ? AND user_id = ?
           \s""";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, financingId);
            stmt.setInt(2, Session.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    VehicleType vehicleType = VehicleType.valueOf(rs.getString("vehicle_type"));
                    VehicleCondition vehicleCondition = VehicleCondition.valueOf(rs.getString("vehicle_condition"));
                    AmortizationType amortizationType = AmortizationType.valueOf(rs.getString("amortization_type"));
                    FinancingStatus status = FinancingStatus.valueOf(rs.getString("financing_status"));

                    VehicleFinancing financing = new VehicleFinancing(
                            rs.getBigDecimal("financed_amount"),
                            rs.getInt("loan_term_months"),
                            rs.getBigDecimal("annual_interest_rate"),
                            amortizationType,
                            vehicleType,
                            vehicleCondition,
                            status,
                            rs.getString("brand"),
                            rs.getString("model"),
                            rs.getObject("manufacture_year", Integer.class),
                            rs.getObject("mileage", Integer.class),
                            rs.getInt("user_id")
                    );

                    financing.setFinancingId(rs.getInt("financing_id"));
                    financing.setVehicleValue(rs.getBigDecimal("vehicle_value"));
                    financing.setDownPayment(rs.getBigDecimal("down_payment"));

                    return financing;
                }
            }
        }

        return null;
    }

    public void updateFinancing(VehicleFinancing financing) throws SQLException {

        String sql = """
                UPDATE vehicle_financing
                SET financed_amount = ?,
                    loan_term_months = ?,
                    annual_interest_rate = ?,
                    amortization_type = ?,
                    vehicle_type = ?,
                    vehicle_condition = ?,
                    financing_status = ?,
                    vehicle_value = ?,
                    down_payment = ?,
                    brand = ?,
                    model = ?,
                    manufacture_year = ?,
                    mileage = ?
                WHERE financing_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, financing.getFinancedAmount());
            stmt.setInt(2, financing.getLoanTermInMonths());
            stmt.setBigDecimal(3, financing.getAnnualInterestRate());
            stmt.setString(4, financing.getAmortizationType().name());
            stmt.setString(5, financing.getVehicleType().name());
            stmt.setString(6, financing.getVehicleCondition().name());
            stmt.setString(7, financing.getStatus().name());
            stmt.setObject(8, financing.getVehicleValue(), java.sql.Types.DECIMAL);
            stmt.setObject(9, financing.getDownPayment(), java.sql.Types.DECIMAL);
            stmt.setString(10, financing.getBrand());
            stmt.setString(11, financing.getModel());
            stmt.setObject(12, financing.getManufactureYear(), java.sql.Types.INTEGER);
            stmt.setObject(13, financing.getMileage(), java.sql.Types.INTEGER);
            stmt.setInt(14, financing.getFinancingId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated == 0) {
                throw new SQLException("No financing record was updated. Please verify the financing ID.");
            }
        }
    }

    public void updateFinancingStatus(VehicleFinancing financing) throws SQLException {

        String sql = """
            UPDATE vehicle_financing
            SET financing_status = ?
            WHERE financing_id = ? AND user_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, financing.getStatus().name());
            stmt.setInt(2, financing.getFinancingId());
            stmt.setInt(3, Session.getUserId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated == 0) {
                throw new SQLException("No financing record was updated. Please verify the financing ID and user ID.");
            }
        }
    }

}
