package org.example;
import io.jsonwebtoken.io.IOException;
import org.example.Initialisation.EnvironmentVariablesManager;
import org.example.Initialisation.SqlDumpManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import java.io.File;
import static org.example.Initialisation.PortManager.portResolver;

@SpringBootApplication
@EnableScheduling
@Component

public class Application   {

    public static void main(String[] args) {
        EnvironmentVariablesManager.checkForRequiredEnvironmentAndProperties();
        Constants.BACKEND_PORT = portResolver(Constants.BACKEND_PORT);
        System.setProperty("server.port", String.valueOf(Constants.BACKEND_PORT));
        runChecksIfFirstTime();
        SpringApplication.run(Application.class, args);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void runChecksIfFirstTime(){

        if (new File("resources/devJourney.txt").exists()) return;
        if (!SqlDumpManager.executeSQLDump("resources/SQLDump")) return;

        try {
            new File("resources").mkdirs();
            new File("resources/devJourney.txt").createNewFile();
        } catch (IOException | java.io.IOException e) {
            e.printStackTrace();
        }
    }


}