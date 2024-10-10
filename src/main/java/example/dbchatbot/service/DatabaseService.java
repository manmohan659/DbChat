package example.dbchatbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import example.dbchatbot.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

import java.io.IOException;

@Service
public class DatabaseService {

    private String schema;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    


    public void connectAndFetchSchema(DataBaseConnection connection) throws Exception {
        String url = "jdbc:mysql://" + connection.getHost() + ":" + connection.getPort() + "/" + connection.getDatabase();
        try (Connection conn = DriverManager.getConnection(url, connection.getUsername(), connection.getPassword())) {
            if (conn != null) {
                System.out.println("Connection established successfully");
                this.schema = fetchSchemaFromDatabase(conn, connection.getDatabase());
                System.out.println("Schema fetched successfully: " + this.schema);
            } else {
                throw new Exception("Failed to establish connection.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception occurred: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("SQL Exception: " + e.getMessage());
        }
    }


  //  public boolean testConnection() {
  //      try {
   //         jdbcTemplate.execute("SELECT 1");
   //         logger.info("Database connection successful");
   //         return true;
   //    } catch (Exception e) {
  //          logger.error("Database connection failed", e);
    //        return false;
    //    }
    //}

    private String fetchSchemaFromDatabase(Connection conn, String databaseName) throws SQLException {
        StringBuilder schemaBuilder = new StringBuilder();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet tables = metaData.getTables(databaseName, null, "%", new String[]{"TABLE"});

        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + tableName)) {
                if (rs.next()) {
                    String createTable = rs.getString(2) + ";";
                    schemaBuilder.append(createTable).append("\n\n");
                }
            }
        }
        return schemaBuilder.toString();
    }

    public String getSchema() throws Exception {
        if (this.schema == null || this.schema.isEmpty()) {
            throw new Exception("Schema is not available. Please connect to a database first.");
        }
        return this.schema;
    }

    public String getSchemaFromFile() throws IOException {
        return Files.readString(Path.of("/Users/manmohan/dumps/Schema/testDbSchema.txt"));
    }

}