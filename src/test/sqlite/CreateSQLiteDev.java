package test.sqlite;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;

public class CreateSQLiteDev {
    /**
     * TODO: verify it's working then refac this code
     * @author sqlitetutorial.net
     */
    public static void createDB(String path, String fileName) {
        Path filePath = Paths.get(Paths.get(System.getProperty("user.dir")).toString(),path, fileName);
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        System.out.println(ts +" log: " +
                "a db is about to be created under: \n" + filePath.toString());
        String url = "server.jdbc:sqlite:" + filePath;
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println(ts +" log: " +
                        "The driver name is " + meta.getDriverName());
                System.out.println(ts +" log: " +
                        "A new database has been created.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}
