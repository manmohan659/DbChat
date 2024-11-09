package example.dbchatbot.model;
import lombok.Data;

@Data
public class DataBaseConnection {
    private String host;
    private String port;
    private String username;
    private String password;
    private String database;

    public String getDatabase() {
        return database;
    }
}

