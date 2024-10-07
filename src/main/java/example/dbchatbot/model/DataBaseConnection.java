package example.dbchatbot.model;
import lombok.Data;

@Data
public class DataBaseConnection {
    private String host;
    private int port;
    private String username;
    private String password;
    private String database;
}
