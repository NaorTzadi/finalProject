package org.example.Initialisation;
import org.example.CustomLogger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnvironmentVariablesManager {
    private static final CustomLogger logger = new CustomLogger(EnvironmentVariablesManager.class);
    private static final String[] requiredVars = {
            "BACKEND_HOST", "BACKEND_PATH", "BACKEND_PORT", "DATABASE_NAME",
            "DATABASE_PASSWORD", "DATABASE_USERNAME", "EMAIL_PASSWORD", "EMAIL_USERNAME",
            "FRONTEND_PATH", "MYSQL_HOST", "USER_SESSION_SECRET_KEY"
    };

    public static Map<String, String> getCustomEnvVars() {
        return System.getenv().entrySet().stream()
                .filter(e -> !isKnownSystemEnv(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, String> getCustomSystemProperties() {
        return System.getProperties().entrySet().stream()
                .map(e -> Map.entry(e.getKey().toString(), e.getValue().toString()))
                .filter(e -> !isKnownSystemProperty(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, String> getEnvVars() {
        return System.getenv().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, String> getSystemProperties() {
        return System.getProperties().stringPropertyNames().stream().collect(Collectors.toMap(Function.identity(), System::getProperty));
    }

    private static boolean isKnownSystemEnv(String key) {
        return key.startsWith("JAVA_") || key.startsWith("PATH") || key.startsWith("USER")
                || key.equals("OS") || key.equals("TEMP") || key.equals("COMPUTERNAME")
                || key.equals("NUMBER_OF_PROCESSORS") || key.equals("PROCESSOR_IDENTIFIER")
                || key.equals("SystemRoot");
    }
    private static boolean isKnownSystemProperty(String key) {
        return key.startsWith("java.") || key.startsWith("sun.") || key.startsWith("os.") ||
                key.startsWith("user.") || key.startsWith("file.") || key.startsWith("line.") ||
                key.equals("path.separator");
    }

    public static void checkForRequiredEnvironmentAndProperties() {

        Properties saved = loadDotEnv();
        ArrayList<String> missingVars = new ArrayList<>();
        for (String var : requiredVars) {
            String val = System.getenv(var);
            if (val == null) val = System.getProperty(var);
            if (val == null) val = saved.getProperty(var);

            if (val == null) missingVars.add(var);
            else System.setProperty(var, val);
        }

        if (!missingVars.isEmpty()) {
            JPanel panel = new JPanel(new GridLayout(0, 1));
            java.util.Map<String, JTextField> fieldMap = new java.util.HashMap<>();

            panel.add(new JLabel("Optional: Paste bulk env values (format: KEY=val;KEY2=val2):"));
            JTextField bulkInput = new JTextField();
            panel.add(bulkInput);

            for (String var : missingVars) {
                panel.add(new JLabel(var + ":"));
                JTextField field;
                if (var.toUpperCase().contains("PASSWORD")) {
                    field = new JPasswordField();
                } else {
                    field = new JTextField();
                }
                fieldMap.put(var, field);
                panel.add(field);
            }

            // Live update input fields when bulk input changes
            bulkInput.getDocument().addDocumentListener(new DocumentListener() {
                private void updateFields() {
                    String bulk = bulkInput.getText().trim();
                    if (!bulk.isEmpty()) {
                        for (String part : bulk.split(";")) {
                            String[] kv = part.split("=", 2);
                            if (kv.length == 2) {
                                String key = kv[0].trim();
                                String val = kv[1].trim();
                                if (fieldMap.containsKey(key)) fieldMap.get(key).setText(val);
                            }
                        }
                    }
                }

                public void insertUpdate(DocumentEvent e) { updateFields(); }
                public void removeUpdate(DocumentEvent e) { updateFields(); }
                public void changedUpdate(DocumentEvent e) { updateFields(); }
            });

            int result = JOptionPane.showConfirmDialog(null, panel, "Missing Environment Variables",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                boolean updated = false;
                Properties newSaved = loadDotEnv();

                for (String var : missingVars) {
                    String value = fieldMap.get(var).getText().trim();
                    if (!value.isEmpty()) {
                        System.setProperty(var, value);
                        newSaved.setProperty(var, value);
                        updated = true;
                    }
                }

                if (updated) saveDotEnv(newSaved);
            }
        }
    }
    public static Properties loadDotEnv() {
        Properties props = new Properties();
        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#") || !line.contains("=")) continue;
                String[] parts = line.split("=", 2);
                props.setProperty(parts[0].trim(), parts[1].trim());
            }
        } catch (Exception ignored) {}
        return props;
    }

    public static void saveDotEnv(Properties props) {
        try (java.io.FileWriter writer = new java.io.FileWriter(".env")) {
            for (String name : props.stringPropertyNames()) writer.write(name + "=" + props.getProperty(name) + "\n");
        } catch (Exception ignored) {}
    }

    public static void setPropertyAndPersist(String key, String value){
        System.setProperty(key, value);
        Properties props = loadDotEnv();
        props.setProperty(key, value);
        saveDotEnv(props);
    }


}