package org.example;
import org.example.Initialisation.EnvironmentVariablesManager;
import org.example.Initialisation.SqlDumpManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ServerUI extends JFrame {
 /*   private static ServerUI instance = null;
    private final HashMap<Component, Boolean> currentState = new HashMap<>();


    private final ArrayList<Component> mainComponents=new ArrayList<>();
    private final ArrayList<Component> sqlDumpComponents=new ArrayList<>();
    private final ArrayList<Component> environmentAndSystemPropertyComponents =new ArrayList<>();

    private ServerUI() {
        setTitle("Server UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(null);
        setAlwaysOnTop(true);
        setVisible(true);
        toFront();
        requestFocus();
        setAlwaysOnTop(false);

        populateComponents();
    }
    public static ServerUI getInstance() {
        if (instance == null) instance = new ServerUI();
        else instance.toFront();
        return instance;
    }
    @Override
    public void dispose() {
        instance = null;
        super.dispose();
    }

    private void populateComponents(){
        populateMainComponents();
        populateSqlDumpManagerComponents();
        populateEnvironmentAndSystemPropertyComponents();
    }
    private void populateMainComponents() {
        JButton sqlDumpButton = new JButton("SQL Dump");
        final int x=50,gap=50,width=200,height=30;
        sqlDumpButton.setBounds(x, gap, 200, 30);
        sqlDumpButton.addActionListener(e -> showOnly(sqlDumpComponents));

        JButton environmentAndSystemPropertiesButton = new JButton("Environment & Properties Variables");
        environmentAndSystemPropertiesButton.setBounds(x, sqlDumpButton.getY()+gap, width, height);
        environmentAndSystemPropertiesButton.addActionListener(e -> showOnly(environmentAndSystemPropertyComponents));

        mainComponents.add(sqlDumpButton);
        mainComponents.add(environmentAndSystemPropertiesButton);

        for (Component c : mainComponents) add(c);
    }

    private void populateSqlDumpManagerComponents() {
        JButton button1 = new JButton("generate sql dump files of current database without data");
        JButton button2 = new JButton("generate sql dump files of current database including data");
        JButton confirmPathButton = new JButton("Confirm");

        JTextField pathField = new JTextField(30);
        JLabel pathLabel = new JLabel("save path (Directory Only): ");
        JLabel errorLabel = new JLabel("");

        errorLabel.setForeground(Color.RED);

        ActionListener validateAndRun = e -> {
            button1.setVisible(false);
            button2.setVisible(false);
            pathLabel.setVisible(true);
            pathField.setVisible(true);
            confirmPathButton.setVisible(true);
            confirmPathButton.putClientProperty("includeData",e.getSource()==button2);
        };
        ActionListener confirmPathListener=e->{
            String path=pathField.getText().trim();
            File dir = new File(path);
            if (!dir.exists()) {
                errorLabel.setVisible(true);
                errorLabel.setText("Directory does not exist");
            } else if (!dir.isDirectory()) {
                errorLabel.setVisible(true);
                errorLabel.setText("Not a directory");
            } else if (!dir.canWrite()) {
                errorLabel.setVisible(true);
                errorLabel.setText("Can't write to file");
            }else{
                errorLabel.setVisible(false);
                errorLabel.setText("");
                SqlDumpManager.generateSchemaDumps(dir.getPath(), confirmPathButton.getClientProperty("includeData").equals(Boolean.TRUE));
            }
        };

        button1.addActionListener(validateAndRun);
        button2.addActionListener(validateAndRun);
        confirmPathButton.addActionListener(confirmPathListener);

        sqlDumpComponents.add(button1);
        sqlDumpComponents.add(button2);
        sqlDumpComponents.add(confirmPathButton);

        sqlDumpComponents.add(pathLabel);
        sqlDumpComponents.add(pathField);
        sqlDumpComponents.add(errorLabel);

        for (Component component : sqlDumpComponents) {
            component.setVisible(false);
        }
    }

    private void populateEnvironmentAndSystemPropertyComponents() {
        JButton addPropertyButton = new JButton("add system property");
        environmentAndSystemPropertyComponents.add(addPropertyButton);
        JButton removePropertyButton = new JButton("remove system property");
        environmentAndSystemPropertyComponents.add(removePropertyButton);

        JTextArea textArea = new JTextArea(20, 60);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane();
        JButton returnButton = new JButton("return");

        environmentAndSystemPropertyComponents.add(returnButton);
        environmentAndSystemPropertyComponents.add(scrollPane);

        ActionListener displayButtonsListener = e -> {
            saveCurrentState();
            for (Component component : environmentAndSystemPropertyComponents) component.setVisible(false);

            scrollPane.setViewportView(textArea);
            scrollPane.setVisible(true);
            returnButton.setVisible(true);
            textArea.setVisible(true);
        };

        JButton displayCustomEnvVarsButton=new JButton("display custom environment variables");
        displayCustomEnvVarsButton.addActionListener( e -> {
            displayButtonsListener.actionPerformed(e);
            textArea.setText(EnvironmentVariablesManager.getCustomEnvVars().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("\n")));
        });
        environmentAndSystemPropertyComponents.add(displayCustomEnvVarsButton);

        JButton displayEnvVarsButton=new JButton("display environment variables");
        displayEnvVarsButton.addActionListener(e -> {
            displayButtonsListener.actionPerformed(e);
            textArea.setText(EnvironmentVariablesManager.getEnvVars().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("\n")));
        });
        environmentAndSystemPropertyComponents.add(displayEnvVarsButton);

        JButton displayCustomPropertyVarsButton = new JButton("display custom system properties");
        displayCustomPropertyVarsButton.addActionListener(e -> {
            displayButtonsListener.actionPerformed(e);
            textArea.setText(EnvironmentVariablesManager.getCustomSystemProperties().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("\n")));
        });
        environmentAndSystemPropertyComponents.add(displayCustomPropertyVarsButton);

        JButton displayPropertyVarsButton = new JButton("display system properties");
        displayPropertyVarsButton.addActionListener(e -> {
            displayButtonsListener.actionPerformed(e);
            textArea.setText(EnvironmentVariablesManager.getSystemProperties().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("\n")));
        });
        environmentAndSystemPropertyComponents.add(displayPropertyVarsButton);

        JButton overwritePropertyButton = new JButton("overwrite system property");
        overwritePropertyButton.addActionListener(e -> {
            saveCurrentState();
            for (Component component : environmentAndSystemPropertyComponents) component.setVisible(false);

            AtomicReference<Map<String, String>> props = new AtomicReference<>(EnvironmentVariablesManager.getCustomSystemProperties());

            JPanel buttonListPanel = new JPanel(new GridLayout(0, 2, 5, 5));

            JPanel inputPanel = new JPanel();
            JTextField inputField = new JTextField(30);
            JLabel instructionLabel = new JLabel();
            JButton overwriteReturnButton = new JButton("return");
            JButton confirmButton = new JButton("confirm");

            inputField.setVisible(false);
            instructionLabel.setVisible(false);
            overwriteReturnButton.setVisible(false);
            confirmButton.setVisible(false);

            overwriteReturnButton.addActionListener(e1 -> scrollPane.setViewportView(buttonListPanel));
            confirmButton.addActionListener(e1 -> {
                EnvironmentVariablesManager.setPropertyAndPersist(confirmButton.getClientProperty("key").toString(),inputField.getText());
                props.set(EnvironmentVariablesManager.getCustomSystemProperties());
                returnButton.getAction().actionPerformed(e1);
            });

            inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
            inputPanel.add(instructionLabel);
            inputPanel.add(inputField);
            inputPanel.add(overwriteReturnButton);

            for (Map.Entry<String, String> entry : props.get().entrySet()) {
                JButton keyButton = new JButton(entry.getKey());
                JLabel valueLabel = new JLabel(entry.getValue());

                keyButton.addActionListener(e1 -> {
                    instructionLabel.setText("Enter new value for: " + entry.getKey());
                    confirmButton.putClientProperty("key",entry.getKey());
                    inputField.setText(entry.getValue());

                    scrollPane.setViewportView(inputPanel);
                    instructionLabel.setVisible(true);
                    inputField.setVisible(true);
                    overwriteReturnButton.setVisible(true);
                });

                buttonListPanel.add(keyButton);
                buttonListPanel.add(valueLabel);
            }

            scrollPane.setViewportView(buttonListPanel);
            scrollPane.setVisible(true);


            environmentAndSystemPropertyComponents.add(instructionLabel);
            environmentAndSystemPropertyComponents.add(inputField);
            environmentAndSystemPropertyComponents.add(overwriteReturnButton);
        });
        environmentAndSystemPropertyComponents.add(overwritePropertyButton);


        ActionListener returnButtonListener = e -> restorePreviousState();
        returnButton.addActionListener(returnButtonListener);

        for (Component component : environmentAndSystemPropertyComponents) {
            component.setVisible(false);
        }
    }
    private void showOnly(ArrayList<Component> toShow) {
        for (Component c : mainComponents) c.setVisible(false);
        for (Component c : sqlDumpComponents) c.setVisible(false);
        for (Component c : environmentAndSystemPropertyComponents) c.setVisible(false);
        for (Component c : toShow) c.setVisible(true);
        repaint();
    }

    public void saveCurrentState() {
        currentState.clear();
        for (Component c : getContentPane().getComponents()) {
            if (c.isVisible()) currentState.put(c, true);
            c.setVisible(false);
        }
    }
    private void restorePreviousState() {
        for (Component c : getContentPane().getComponents()) c.setVisible(currentState.getOrDefault(c, false));
        currentState.clear();
    }*/

    public static class PromptUI {
        public static String prompt(String message, String title) {
            return JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE);
        }

        public static void alert(String message, String title) {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
        }

        public static int confirm(String message, String title, String[] options) {
            return JOptionPane.showOptionDialog(
                    null, message, title,
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
        }
    }
}
