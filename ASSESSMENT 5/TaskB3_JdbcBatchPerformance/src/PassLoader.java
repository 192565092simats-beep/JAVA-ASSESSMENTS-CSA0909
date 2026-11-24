import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Task B3: Complete the Code - PassLoader.java
 * High-performance batch data ingestion using JDBC PreparedStatement and ACID Transactions.
 */
public class PassLoader {

    /**
     * Loads all passes in one transaction, in batches of batchSize.
     * Returns total number of rows loaded.
     */
    public static int loadAll(Connection con, List<String[]> passes) throws SQLException {
        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES (?, ?, ?, ?, ?)";
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false); // Begin transaction boundary

        int count = 0;
        int batchSize = 1000;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (String[] p : passes) {
                /**
                 * [TODO 1 Completed]:
                 * Set the five parameters from p, checking for valid data.
                 * Add the row to the batch, and executeBatch() every batchSize rows.
                 */
                if (p == null || p.length < 5) {
                    throw new IllegalArgumentException("Invalid pass record: insufficient field count");
                }

                String passId = p[0].trim();
                String regNo = p[1].trim();
                String name = p[2].trim();
                int routeNo = Integer.parseInt(p[3].trim());
                double amount = Double.parseDouble(p[4].trim());

                ps.setString(1, passId);
                ps.setString(2, regNo);
                ps.setString(3, name);
                ps.setInt(4, routeNo);
                ps.setDouble(5, amount);

                ps.addBatch();
                count++;

                if (count % batchSize == 0) {
                    ps.executeBatch();
                }
            }

            // Flush any remaining batched rows
            ps.executeBatch();

            /**
             * [TODO 2 Completed]:
             * Make all inserted rows permanent.
             */
            con.commit();

        } catch (SQLException | RuntimeException e) {
            /**
             * [TODO 3 Completed]:
             * Undo every insert made in this call, and rethrow the exception.
             */
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit); // Restore connection state
        }

        return count;
    }
}
