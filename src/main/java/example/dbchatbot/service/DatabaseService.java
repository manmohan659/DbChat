package example.dbchatbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

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
}