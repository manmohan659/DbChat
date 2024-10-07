package example.dbchatbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import example.dbchatbot.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseService {

    private String schema;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    public void connectAndFetchSchema(DatabaseConnection connection) throws Exception {
        String url = "jdbc:mysql://" +  connection.getHost() + ":" + connection.getPort();

        try(Connection conn = DriverManager.getConnection(url, connection.getUsername(), connection.getPassword())){
            if (conn != null) {
                this.schema = fetchSchemaFromDatabase(conn, connection.getDatabaseName());
            } else {
                throw new Exception("Failed to establish connection.");
            }

        }catch (SQLException e) {
            throw new Exception("SQL Exception: " + e.getMessage());
        }
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean testConnection() {
        try {
            jdbcTemplate.execute("SELECT 1");
            logger.info("Database connection successful");
            return true;
        } catch (Exception e) {
            logger.error("Database connection failed", e);
            return false;
        }
    }

    private String fetchSchemaFromDatabase(Connection conn, String databaseName) throws SQLException {
        List<String> createTableStatements = new ArrayList<>();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet tables = metaData.getTables(databaseName, null, "%", new String[]{"TABLE"});

        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + tableName);
            if(rs.next()) {
                String createTable = rs.getString(2) + ";";
                createTableStatements.add(createTable);
            }
            rs.close();
            stmt.close();
        }
        tables.close();
        return String.join("\n\n", createTableStatements);
    }

    public String getSchema() throws Exception{
        if(this.schema == null || this.schema.isEmpty()) {
            throw new Exception("Schema is not available");
        }
        return this.schema;
    }

}