package org.example.Initialisation;
import io.jsonwebtoken.io.IOException;
import org.example.Application;
import org.example.CustomLogger;
import org.example.ServerUI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Properties;

public class PortManager {
    private static final CustomLogger logger = new CustomLogger(PortManager.class);

    public static int portResolver(int desiredPort) {
        int port = desiredPort;
        Integer processPid = getProcessPidByPort(port);
        if (processPid == null) return port;

        Properties props = EnvironmentVariablesManager.loadDotEnv();
        String altPortStr = props.getProperty("BACKEND_PORT");
        if (altPortStr != null) {
            try {
                int altPort = Integer.parseInt(altPortStr.trim());
                if (altPort != port && getProcessPidByPort(altPort) == null) {
                    System.setProperty("BACKEND_PORT", String.valueOf(altPort));
                    return altPort;
                }
            } catch (Exception ignored) {}
        }
        String[] options = {"Kill Process", "Use Different Port", "Exit"};
        int choice = ServerUI.PromptUI.confirm("Port " + port + " is taken by PID " + processPid,
                "Port Conflict", options);

        if (choice == 0) {
            if (killProcessByPid(processPid)) {
                return port;
            } else {
                ServerUI.PromptUI.alert("Failed to kill process.", "Error");
                System.exit(1);
            }
        } else if (choice == 1) {
            ArrayList<String> availablePorts = getAvailablePorts();
            while (true) {
                String alt = ServerUI.PromptUI.prompt("Some available ports: " + String.join(", ", availablePorts)
                        + "\nEnter new port:", "Choose Port");
                try {
                    port = Integer.parseInt(alt.trim());
                    String save = ServerUI.PromptUI.prompt("Do you want to save " + port + " as the new default port? (yes/no)", "Save Port");
                    if (save != null && save.equalsIgnoreCase("yes")) {
                        savePortAsDefault(port);
                    }
                    break;
                } catch (Exception e) {
                    ServerUI.PromptUI.alert("Invalid port. Please enter a valid number.", "Invalid Input");
                }
            }
        } else {
            System.exit(1);
        }

        return port;
    }

    private static void savePortAsDefault(int port) {
        Properties props = EnvironmentVariablesManager.loadDotEnv();
        props.setProperty("BACKEND_PORT", String.valueOf(port));
        EnvironmentVariablesManager.saveDotEnv(props);
        System.setProperty("BACKEND_PORT", String.valueOf(port));
    }

    private static Integer getProcessPidByPort(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return null;
        } catch (IOException | java.io.IOException e) {
            return findPidOnPort(port);
        }
    }

    private static Integer findPidOnPort(int port) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "netstat -aon | findstr :" + port});
            String line = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
            if (line != null && !line.isEmpty()) {
                String[] tokens = line.trim().split("\\s+");
                return Integer.parseInt(tokens[tokens.length - 1]);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static boolean killProcessByPid(int processPid) {
        try {
            Runtime.getRuntime().exec("taskkill /F /PID " + processPid).waitFor();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static ArrayList<String> getAvailablePorts() {
        ArrayList<String> availablePorts = new ArrayList<>();
        int startPort = 1024;
        int endPort = 65535;
        for (int port = startPort; port <= endPort; port++) {
            try (ServerSocket socket = new ServerSocket(port)) {
                availablePorts.add(String.valueOf(port));
                if (availablePorts.size() >= 10) break;
            } catch (IOException | java.io.IOException ignored) {}
        }
        return availablePorts;
    }

}
