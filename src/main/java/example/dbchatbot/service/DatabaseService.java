package example.dbchatbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import example.dbchatbot.model.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
public class DatabaseService {
    private final Map<String, Connection> activeConnections = new ConcurrentHashMap<>();
    private final SessionService sessionService;

    @Autowired
    public DatabaseService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public void connectAndFetchSchema(String userId, DataBaseConnection dbConnection) throws Exception {
        String connectionId = generateConnectionId(userId, dbConnection);
        
        if (!activeConnections.containsKey(connectionId)) {
            String url = "jdbc:mysql://" + dbConnection.getHost() + ":" + dbConnection.getPort() + "/" + dbConnection.getDatabase();
            
            try {
                Connection connection = DriverManager.getConnection(url, dbConnection.getUsername(), dbConnection.getPassword());
                activeConnections.put(connectionId, connection);
                log.info("New database connection established for user: {}", userId);

                // Fetch and cache schema
                String schema = fetchSchemaFromDatabase(connection, dbConnection.getDatabase());
                sessionService.saveSchema(userId, schema);
                
            } catch (SQLException e) {
                log.error("Failed to establish database connection: {}", e.getMessage());
                throw new Exception("Failed to connect to database: " + e.getMessage());
            }
        } else {
            log.info("Reusing existing connection for user: {}", userId);
        }
    }

    public List<Map<String, Object>> executeQuery(String userId, String query) throws SQLException {
        Connection connection = getConnectionForUser(userId);
        if (connection == null) {
            throw new SQLException("No active database connection found for user: " + userId);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            ResultSetMetaData metadata = rs.getMetaData();
            int columnCount = metadata.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metadata.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }
        }

        return results;
    }

    private Connection getConnectionForUser(String userId) {
        return activeConnections.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(userId + ":"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private String generateConnectionId(String userId, DataBaseConnection dbConnection) {
        return userId + ":" + dbConnection.getHost() + ":" + dbConnection.getPort() + ":" + dbConnection.getDatabase();
    }

    private String fetchSchemaFromDatabase(Connection conn, String databaseName) throws SQLException {
        StringBuilder schemaBuilder = new StringBuilder();
        DatabaseMetaData metaData = conn.getMetaData();
        
        try (ResultSet tables = metaData.getTables(databaseName, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + tableName)) {
                    if (rs.next()) {
                        schemaBuilder.append(rs.getString(2)).append(";\n\n");
                    }
                }

                // Get table comments
                try (ResultSet columns = metaData.getColumns(databaseName, null, tableName, null)) {
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        String remarks = columns.getString("REMARKS");
                        if (remarks != null && !remarks.isEmpty()) {
                            schemaBuilder.append("-- Column ").append(columnName)
                                       .append(" remarks: ").append(remarks).append("\n");
                        }
                    }
                }
            }
        }
        
        return schemaBuilder.toString();
    }

    public void closeConnection(String userId) {
        activeConnections.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(userId + ":")) {
                try {
                    entry.getValue().close();
                    return true;
                } catch (SQLException e) {
                    log.error("Error closing connection: {}", e.getMessage());
                }
            }
            return false;
        });
    }

    public String getSchema(String userId) throws Exception {
        return sessionService.getSchema(userId);
    }
}