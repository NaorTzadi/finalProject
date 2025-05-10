package org.example.Initialisation;
import org.example.CustomLogger;
import org.example.ServerUI;
import org.springframework.stereotype.Component;
import java.io.*;
import java.sql.*;
import java.util.*;

@Component
public class SqlDumpManager {
    private static final CustomLogger logger = new CustomLogger(SqlDumpManager.class);
    private static final boolean isWindows=System.getProperty("os.name").toLowerCase().contains("win");
    private static String executeSqlFilesPath="C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe";
    private static String generateSqlFilesPath="C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe";

    public static boolean executeSQLDump(String path) {
        try {
            if (path == null || path.isBlank()) return false;
            File file = new File(path);
            if (!file.exists()) return false;

            String dbUsername = System.getenv("DATABASE_USERNAME");
            if (dbUsername == null) dbUsername = System.getProperty("DATABASE_USERNAME");
            if (dbUsername == null) dbUsername = "root";

            String dbPassword = System.getenv("DATABASE_PASSWORD");
            if (dbPassword == null) dbPassword = System.getProperty("DATABASE_PASSWORD");
            if (dbPassword == null) dbPassword = "";

            String mysqlHost = System.getenv("MYSQL_HOST");
            if (mysqlHost == null) mysqlHost = System.getProperty("MYSQL_HOST");
            if (mysqlHost == null) mysqlHost = "localhost:3306";

            String[] hostParts = mysqlHost.split(":");
            String host = hostParts[0];
            String port = (hostParts.length > 1) ? hostParts[1] : "3306";

            String mysqlPath = isWindows ? executeSqlFilesPath : "/usr/bin/mysql";
            if (!isPathResolved(mysqlPath)) return false;

            List<File> sqlFiles = new ArrayList<>();
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    if (f.isFile() && f.getName().endsWith(".sql")) sqlFiles.add(f);
                }
            } else if (file.isFile() && file.getName().endsWith(".sql")) {
                sqlFiles.add(file);
            } else return false;

            for (File sqlFile : sqlFiles) {
                logger.dev("Processing SQL file: " + sqlFile.getAbsolutePath());

                String scriptContent;
                if (isWindows) {
                    scriptContent = "@echo off\n\"" + mysqlPath + "\" -h " + host + " -P " + port + " -u " + dbUsername +
                            " -p" + dbPassword + " < \"" + sqlFile.getAbsolutePath() + "\"\n";
                } else {
                    scriptContent = "#!/bin/sh\n\"" + mysqlPath + "\" -h " + host + " -P " + port + " -u " + dbUsername +
                            " -p" + dbPassword + " < \"" + sqlFile.getAbsolutePath() + "\"\n";
                }

                logger.dev("Generated script:\n" + scriptContent);

                File tempScript = File.createTempFile("import_", isWindows ? ".bat" : ".sh");
                try (FileWriter fw = new FileWriter(tempScript)) {
                    fw.write(scriptContent);
                }

                if (!isWindows) new ProcessBuilder("chmod", "+x", tempScript.getAbsolutePath()).start().waitFor();

                logger.dev("Executing script: " + tempScript.getAbsolutePath());

                ProcessBuilder pb = isWindows
                        ? new ProcessBuilder("cmd.exe", "/c", tempScript.getAbsolutePath())
                        : new ProcessBuilder("sh", tempScript.getAbsolutePath());

                pb.redirectErrorStream(true);
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) logger.dev("[mysql] " + line);
                }

                int exitCode = process.waitFor();
                logger.dev("MySQL process for file '" + sqlFile.getName() + "' exited with code: " + exitCode);
                tempScript.delete();

                if (exitCode != 0) return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error executing SQL dump via cmd", e);
        }
        return false;
    }

    private static boolean isPathResolved(String path) {
        File file = new File(path);
        if (file.exists()) return true;

        while (true) {
            String input = ServerUI.PromptUI.prompt(
                    "mysql not found at:\n" + path + "\n\nEnter full path manually:", "MySQL Path Required");

            if (input == null || input.isBlank()) {
                int choice = ServerUI.PromptUI.confirm(
                        "No path provided.\nDo you want to try again?", "Path Missing", new String[]{"Yes", "No"});
                if (choice != 0) return false;
                continue;
            }

            File newPath = new File(input.trim());
            if (newPath.exists()) {
                executeSqlFilesPath = newPath.getAbsolutePath();
                return true;
            }

            int choice = ServerUI.PromptUI.confirm(
                    "The path you entered is invalid:\n" + input + "\n\nTry again?", "Invalid Path", new String[]{"Yes", "No"});
            if (choice != 0) return false;
        }
    }


    private static Set<String> getCustomSchemas() {

        String dbUsername = System.getenv("DATABASE_USERNAME");
        if (dbUsername == null) dbUsername = System.getProperty("DATABASE_USERNAME");
        if (dbUsername == null) dbUsername = "root";

        String dbPassword = System.getenv("DATABASE_PASSWORD");
        if (dbPassword == null) dbPassword = System.getProperty("DATABASE_PASSWORD");
        if (dbPassword == null) dbPassword = "";


        String mysqlHost = System.getenv("MYSQL_HOST");
        if (mysqlHost == null) mysqlHost = System.getProperty("MYSQL_HOST");
        if (mysqlHost == null) mysqlHost = "localhost:3306";

        Set<String> schemas = new HashSet<>();
        String url = "jdbc:mysql://"+mysqlHost+"/?useSSL=false";

        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT schema_name FROM information_schema.schemata " +
                             "WHERE schema_name NOT IN ('mysql', 'information_schema', 'performance_schema', 'sys')"
             );
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                schemas.add(rs.getString("schema_name"));
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch database schemas", e);
        }
        return schemas;
    }
    public static void generateSchemaDumps(String dirPath,boolean withData) {
        String dbUsername = System.getenv("DATABASE_USERNAME");
        if (dbUsername == null) dbUsername = System.getProperty("DATABASE_USERNAME");
        if (dbUsername == null) dbUsername = "root";

        String dbPassword = System.getenv("DATABASE_PASSWORD");
        if (dbPassword == null) dbPassword = System.getProperty("DATABASE_PASSWORD");
        if (dbPassword == null) dbPassword = "";
        final String mysqldumpPath = isWindows
                ? generateSqlFilesPath
                : "mysqldump";
        for (String schema : getCustomSchemas()) {
            String filePath = dirPath + "/" + schema + "_dump.sql";

            List<String> command = Arrays.asList(
                    mysqldumpPath,
                    "-u", dbUsername,
                    "-p" + dbPassword,
                    "--databases", schema,
                    "--add-drop-table",
                    "--disable-keys",
                    "--routines",
                    "--triggers",
                    "--events",
                    (withData?"":"--no-data")
            );

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectOutput(new File(filePath));
            builder.redirectError(ProcessBuilder.Redirect.INHERIT);

            try {
                Process process = builder.start();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    logger.error("mysqldump failed for schema: " + schema);
                }
            } catch (IOException | InterruptedException e) {
                logger.error("Failed to dump schema: " + schema, e);
            }
        }
    }

    private void dumpSchemaToFile(String schema, String outputPath) {
        String dbUsername = System.getenv("DATABASE_USERNAME");
        if (dbUsername == null) dbUsername = System.getProperty("DATABASE_USERNAME");
        if (dbUsername == null) dbUsername = "root";

        String dbPassword = System.getenv("DATABASE_PASSWORD");
        if (dbPassword == null) dbPassword = System.getProperty("DATABASE_PASSWORD");
        if (dbPassword == null) dbPassword = "";

        try {
            List<String> command = List.of(
                    "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump",
                    "-u", dbUsername,
                    "-p" + dbPassword,
                    "--databases", schema,
                    "--add-drop-table",
                    "--disable-keys",
                    "--routines",
                    "--triggers",
                    "--events",
                    "--no-data"
            );
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectOutput(new File(outputPath));
            pb.redirectErrorStream(true);
            pb.start().waitFor();
        } catch (Exception e) {
            logger.error("Failed to dump schema: " + schema, e);
        }
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) for (File f : file.listFiles()) deleteRecursive(f);
        file.delete();
    }


}