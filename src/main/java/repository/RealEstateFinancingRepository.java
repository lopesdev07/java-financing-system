package repository;

import model.RealEstateFinancing;
import model.FinancingStatus;
import model.AmortizationType;
import model.PropertyType;
import service.Session;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RealEstateFinancingRepository {

    public void saveFinancing(RealEstateFinancing financing) throws SQLException {

        String sql = """
                INSERT INTO realestate_financing
                (financed_amount, loan_term_months, annual_interest_rate, amortization_type, property_type, financing_status, user_id, bedrooms, parking_spaces, land_area, floor, elevator, condominium_fee, zoning) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setBigDecimal(1, financing.getFinancedAmount());
            stmt.setInt(2, financing.getLoanTermInMonths());
            stmt.setBigDecimal(3, financing.getAnnualInterestRate());
            stmt.setString(4, financing.getAmortizationType().name());
            stmt.setString(5, financing.getPropertyType().name());
            stmt.setString(6, financing.getStatus().name());
            stmt.setInt(7, financing.getUserId());
            stmt.setObject(8, financing.getBedrooms(), java.sql.Types.INTEGER);
            stmt.setObject(9, financing.getParkingSpaces(), java.sql.Types.INTEGER);
            stmt.setObject(10, financing.getLandArea(), java.sql.Types.DECIMAL);
            stmt.setObject(11, financing.getFloor(), java.sql.Types.INTEGER);
            stmt.setObject(12, financing.hasElevator(), java.sql.Types.BOOLEAN);
            stmt.setObject(13, financing.getCondominiumFee(), java.sql.Types.DECIMAL);
            stmt.setString(14, financing.getZoning());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {

                if (rs.next()) {
                    financing.setFinancingId(rs.getInt(1));
                }

            }

        }
    }

    public List<RealEstateFinancing> findAllByUser() throws SQLException {

        String sql = """
        SELECT financing_id,
               financed_amount,
               loan_term_months,
               annual_interest_rate,
               amortization_type,
               property_type,
               financing_status
        FROM realestate_financing
        WHERE user_id = ?
    """;

        List<RealEstateFinancing> financings = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Session.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    RealEstateFinancing financing =
                            new RealEstateFinancing(
                                    rs.getBigDecimal("financed_amount"),
                                    rs.getInt("loan_term_months"),
                                    rs.getBigDecimal("annual_interest_rate"),
                                    AmortizationType.valueOf(rs.getString("amortization_type")),
                                    PropertyType.valueOf(rs.getString("property_type")),
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

    public RealEstateFinancing findById(int financingId) throws SQLException {

        String sql = """
                SELECT financing_id,
                 financed_amount,
                 loan_term_months,
                 annual_interest_rate,
                 amortization_type,
                 property_type,
                 financing_status,
                 bedrooms,
                 parking_spaces,
                 land_area,
                 floor,
                 elevator,
                 condominium_fee,
                 zoning,
                 user_id
          FROM realestate_financing
          WHERE financing_id = ? AND user_id = ?
           \s""";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, financingId);
            stmt.setInt(2, Session.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    PropertyType propertyType = PropertyType.valueOf(rs.getString("property_type"));
                    AmortizationType amortizationType = AmortizationType.valueOf(rs.getString("amortization_type"));
                    FinancingStatus status = FinancingStatus.valueOf(rs.getString("financing_status"));

                    RealEstateFinancing financing = new RealEstateFinancing(
                            rs.getBigDecimal("financed_amount"),
                            rs.getInt("loan_term_months"),
                            rs.getBigDecimal("annual_interest_rate"),
                            amortizationType,
                            propertyType,
                            status,
                            rs.getObject("bedrooms", Integer.class),
                            rs.getObject("parking_spaces", Integer.class),
                            rs.getBigDecimal("land_area"),
                            rs.getObject("floor", Integer.class),
                            rs.getObject("elevator", Boolean.class),
                            rs.getBigDecimal("condominium_fee"),
                            rs.getString("zoning"),
                            rs.getInt("user_id")
                    );

                    financing.setFinancingId(rs.getInt("financing_id"));

                    return financing;
                }
            }
        }

        return null;
    }

    public void updateFinancing(RealEstateFinancing financing) throws SQLException {

        String sql = """
                UPDATE realestate_financing
                SET financed_amount = ?,
                    loan_term_months = ?,
                    annual_interest_rate = ?,
                    amortization_type = ?,
                    property_type = ?,
                    financing_status = ?,
                    bedrooms = ?,
                    parking_spaces = ?,
                    land_area = ?,
                    floor = ?,
                    elevator = ?,
                    condominium_fee = ?,
                    zoning = ?
                WHERE financing_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, financing.getFinancedAmount());
            stmt.setInt(2, financing.getLoanTermInMonths());
            stmt.setBigDecimal(3, financing.getAnnualInterestRate());
            stmt.setString(4, financing.getAmortizationType().name());
            stmt.setString(5, financing.getPropertyType().name());
            stmt.setString(6, financing.getStatus().name());
            stmt.setObject(7, financing.getBedrooms(), java.sql.Types.INTEGER);
            stmt.setObject(8, financing.getParkingSpaces(), java.sql.Types.INTEGER);
            stmt.setObject(9, financing.getLandArea(), java.sql.Types.DECIMAL);
            stmt.setObject(10, financing.getFloor(), java.sql.Types.INTEGER);
            stmt.setObject(11, financing.hasElevator(), java.sql.Types.BOOLEAN);
            stmt.setObject(12, financing.getCondominiumFee(), java.sql.Types.DECIMAL);
            stmt.setString(13, financing.getZoning());
            stmt.setInt(14, financing.getFinancingId());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated == 0) {
                throw new SQLException("No financing record was updated. Please verify the financing ID.");
            }
        }
    }

}