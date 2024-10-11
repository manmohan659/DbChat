package example.dbchatbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import example.dbchatbot.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class DatabaseService {


    private Connection connection;
    private String schema;


    public void connectAndFetchSchema(DataBaseConnection dbConnection) throws Exception {
        if(this.connection == null) {
        String url = "jdbc:mysql://" + dbConnection.getHost() + ":" + dbConnection.getPort() + "/" + dbConnection.getDatabase();
        try  {
                this.connection = DriverManager.getConnection(url, dbConnection.getUsername(), dbConnection.getPassword());
                    if (connection != null) {
                        System.out.println("Connection established successfully");
                        log.info("Connection established successfully");
                        this.schema = fetchSchemaFromDatabase(this.connection, dbConnection.getDatabase());
                        System.out.println("Schema fetched successfully: " + this.schema);
                        log.info("Schema fetched successfully: " + this.schema);
                    } else {
                        throw new Exception("Failed to establish connection.");
                    }

        }catch (SQLException e) {
                    System.out.println("SQL Exception occurred: " + e.getMessage());
                    e.printStackTrace();
                    throw new Exception("SQL Exception: " + e.getMessage());
                }

        }else{
            System.out.println("Reusing the existing database connection");
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

    public List<Map<String, Object>> executeQuery(String query) throws SQLException {
     if(this.connection == null){
            throw new SQLException("No active database connection. Please connect first.");
        }
     try(Statement stmt = this.connection.createStatement()) {
         ResultSet rs = stmt.executeQuery(query);

         ResultSetMetaData rsmd = rs.getMetaData();
         int columnCount = rsmd.getColumnCount();
         List<Map<String, Object>> list = new ArrayList<>();
         while (rs.next()) {
             Map<String, Object> row = new HashMap<>();
             for (int i = 1; i <= columnCount; i++) {
                 row.put(rsmd.getColumnName(i), rs.getObject(i));
             }
             list.add(row);
         }
         return list;
     }
    }

}