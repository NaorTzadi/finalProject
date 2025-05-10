package org.example;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class Utility {
    private static final Logger logger = LoggerFactory.getLogger(Utility.class);
    private final DataSource dataSource;

    @Autowired
    public Utility(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void clearTable(String tableName) {
        String deleteSQL = "DELETE FROM " + tableName;
        String resetSQL = "ALTER TABLE " + tableName + " AUTO_INCREMENT = 1";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL);
             PreparedStatement resetStatement = connection.prepareStatement(resetSQL)) {
            deleteStatement.executeUpdate();
            resetStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error clearing table", e);
        }
    }

}
