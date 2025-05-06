
package com.example.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PatientDashboardApp extends Application {

    private Stage primaryStage;
    private String patientName;
    private String patientId;
    private List<VitalRecord> vitalsHistory = new ArrayList<>();
    private int emergencyClickCount = 0;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showRegistrationScene();
    }

    private void showRegistrationScene() {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        Label nameLabel = new Label("Enter Name:");
        TextField nameField = new TextField();

        Label idLabel = new Label("Enter Patient ID:");
        TextField idField = new TextField();

        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> {
            patientName = nameField.getText();
            patientId = idField.getText();
            showDashboardScene();
        });

        root.getChildren().addAll(nameLabel, nameField, idLabel, idField, registerButton);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Patient Registration");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showDashboardScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #fbc2eb, #a6c1ee);");

        Text title = new Text("Welcome, " + patientName + " 😊");
        title.setFont(Font.font("Arial", 26));
        title.setStyle("-fx-fill: #333;");

        Text idText = new Text("Patient ID: " + patientId);
        idText.setStyle("-fx-font-size: 16;");

        Button vitalsButton = createStyledButton("Vitals ❤️", "#ffb347");
        vitalsButton.setOnAction(e -> showVitalsOptionsScene());

        Button emergencyButton = createStyledButton("Emergency 🚨 (Press Twice)", "#ff4c4c");
        emergencyButton.setOnAction(e -> {
            emergencyClickCount++;
            if (emergencyClickCount >= 2) {
                showEmergencyAlert("Manual Emergency Triggered!");
                emergencyClickCount = 0;
            }
        });

        Button chatButton = createStyledButton("Chat 💬", "#87cefa");
        chatButton.setOnAction(e -> showErrorAlert("Chat feature coming soon!"));

        root.getChildren().addAll(title, idText, vitalsButton, emergencyButton, chatButton);

        Scene scene = new Scene(root, 400, 450);
        primaryStage.setTitle("Patient Dashboard");
        primaryStage.setScene(scene);
    }

    private void showVitalsOptionsScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #a1c4fd, #c2e9fb);");

        Button addVitalsButton = createStyledButton("➕ Add/Update Vitals", "#ffdd59");
        Button showVitalsButton = createStyledButton("📊 Show Vitals", "#70a1ff");
        Button downloadButton = createStyledButton("⬇️ Download CSV", "#7bed9f");

        addVitalsButton.setOnAction(e -> showAddVitalsScene());
        showVitalsButton.setOnAction(e -> showVitalsChartScene());
        downloadButton.setOnAction(e -> downloadVitalsAsCSV());

        root.getChildren().addAll(addVitalsButton, showVitalsButton, downloadButton);

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setTitle("Vitals Options");
        primaryStage.setScene(scene);
    }

    private void showAddVitalsScene() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to right, #fbc2eb, #a6c1ee);");

        TextField tempField = createSizedTextField(150);
        TextField systolicField = createSizedTextField(150);
        TextField diastolicField = createSizedTextField(150);
        TextField heartRateField = createSizedTextField(150);
        TextField oxygenField = createSizedTextField(150);

        VBox inputs = new VBox(10,
                createLabeledInput("🌡️ Temperature (°C):", tempField),
                createLabeledInput("🩺 Systolic BP (mmHg):", systolicField),
                createLabeledInput("🩸 Diastolic BP (mmHg):", diastolicField),
                createLabeledInput("❤️ Heart Rate (bpm):", heartRateField),
                createLabeledInput("🫁 Oxygen Level (%):", oxygenField)
        );
        inputs.setAlignment(Pos.CENTER);

        Button saveButton = createStyledButton("💾 Save Vitals", "#2ed573");
        saveButton.setOnAction(e -> {
            try {
                double temperature = Double.parseDouble(tempField.getText());
                double systolic = Double.parseDouble(systolicField.getText());
                double diastolic = Double.parseDouble(diastolicField.getText());
                double heartRate = Double.parseDouble(heartRateField.getText());
                double oxygen = Double.parseDouble(oxygenField.getText());

                vitalsHistory.add(new VitalRecord(LocalDateTime.now(), temperature, systolic, diastolic, heartRate, oxygen));

                if (isEmergencyCondition(temperature, systolic, diastolic, heartRate, oxygen)) {
                    showEmergencyAlert("Automatic Emergency Detected based on Vitals!");
                } else {
                    showDashboardScene();
                }
            } catch (NumberFormatException ex) {
                showErrorAlert("Invalid input! Please enter valid numbers.");
            }
        });

        root.getChildren().addAll(inputs, saveButton);

        Scene scene = new Scene(root, 400, 550);
        primaryStage.setTitle("Add/Update Vitals");
        primaryStage.setScene(scene);

    }
    private TextField createSizedTextField(double width) {
        TextField tf = new TextField();
        tf.setPrefWidth(width);
        tf.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-padding: 5 10 5 10;" +
                        "-fx-font-size: 14px;"
        );
        return tf;
    }

    private VBox createLabeledInput(String labelText, TextField inputField) {
        Label label = createStyledLabel(labelText);
        VBox box = new VBox(5, label, inputField);
        box.setAlignment(Pos.CENTER);
        return box;
    }


    private void showVitalsChartScene() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time Index");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("📈 Vitals Over Time");

        // Define all series
        XYChart.Series<Number, Number> tempSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> sysSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> diaSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> hrSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> oxySeries = new XYChart.Series<>();

        tempSeries.setName("Temperature");
        sysSeries.setName("Systolic");
        diaSeries.setName("Diastolic");
        hrSeries.setName("Heart Rate");
        oxySeries.setName("Oxygen");

        // Fill data once
        for (int i = 0; i < vitalsHistory.size(); i++) {
            VitalRecord vr = vitalsHistory.get(i);
            tempSeries.getData().add(new XYChart.Data<>(i, vr.temperature));
            sysSeries.getData().add(new XYChart.Data<>(i, vr.systolic));
            diaSeries.getData().add(new XYChart.Data<>(i, vr.diastolic));
            hrSeries.getData().add(new XYChart.Data<>(i, vr.heartRate));
            oxySeries.getData().add(new XYChart.Data<>(i, vr.oxygen));
        }

        // Checkboxes
        CheckBox tempCheck = new CheckBox("🌡️ Temperature");
        CheckBox sysCheck = new CheckBox("🩺 Systolic");
        CheckBox diaCheck = new CheckBox("🩸 Diastolic");
        CheckBox hrCheck = new CheckBox("❤️ Heart Rate");
        CheckBox oxyCheck = new CheckBox("🫁 Oxygen");

        // Default all selected
        tempCheck.setSelected(true);
        sysCheck.setSelected(true);
        diaCheck.setSelected(true);
        hrCheck.setSelected(true);
        oxyCheck.setSelected(true);

        // Add all selected series initially
        lineChart.getData().addAll(tempSeries, sysSeries, diaSeries, hrSeries, oxySeries);

        // Checkbox event handler
        tempCheck.setOnAction(e -> toggleSeries(lineChart, tempCheck, tempSeries));
        sysCheck.setOnAction(e -> toggleSeries(lineChart, sysCheck, sysSeries));
        diaCheck.setOnAction(e -> toggleSeries(lineChart, diaCheck, diaSeries));
        hrCheck.setOnAction(e -> toggleSeries(lineChart, hrCheck, hrSeries));
        oxyCheck.setOnAction(e -> toggleSeries(lineChart, oxyCheck, oxySeries));

        VBox checkboxBox = new VBox(10, tempCheck, sysCheck, diaCheck, hrCheck, oxyCheck);
        checkboxBox.setPadding(new Insets(10));
        checkboxBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #333;");
        checkboxBox.setAlignment(Pos.TOP_LEFT);

        Button backButton = createStyledButton("⬅️ Back", "#57606f");
        backButton.setOnAction(e -> showDashboardScene());

        VBox leftPanel = new VBox(20, checkboxBox, backButton);
        leftPanel.setPadding(new Insets(10));

        HBox root = new HBox(10, leftPanel, lineChart);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 1000, 550);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Show Vitals");
    }


    private void toggleSeries(LineChart<Number, Number> chart, CheckBox checkBox, XYChart.Series<Number, Number> series) {
        if (checkBox.isSelected()) {
            if (!chart.getData().contains(series)) {
                chart.getData().add(series);
            }
        } else {
            chart.getData().remove(series);
        }
    }



    private void downloadVitalsAsCSV() {
        try (FileWriter writer = new FileWriter("vitals_history.csv")) {
            writer.write("DateTime,Temperature,Systolic,Diastolic,HeartRate,Oxygen\n");
            for (VitalRecord vr : vitalsHistory) {
                writer.write(vr.toCSV() + "\n");
            }
            showErrorAlert("CSV saved successfully as vitals_history.csv");
        } catch (IOException e) {
            showErrorAlert("Failed to save CSV: " + e.getMessage());
        }
    }

    private boolean isEmergencyCondition(double temperature, double systolic, double diastolic, double heartRate, double oxygen) {
        return temperature > 38.0 || temperature < 35.0 ||
                systolic > 140 || systolic < 90 ||
                diastolic > 90 || diastolic < 60 ||
                heartRate > 120 || heartRate < 50 ||
                oxygen < 90;
    }

    private void showEmergencyAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Emergency Alert");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        EmailUtil.sendEmergencyEmail(patientName, message);
        showDashboardScene();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Alert");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20 10 20;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;"
        );
        return button;
    }

    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 16));
        label.setStyle("-fx-text-fill: #2f3542;");
        return label;
    }

    static class VitalRecord {
        LocalDateTime timestamp;
        double temperature, systolic, diastolic, heartRate, oxygen;

        public VitalRecord(LocalDateTime timestamp, double temperature, double systolic, double diastolic, double heartRate, double oxygen) {
            this.timestamp = timestamp;
            this.temperature = temperature;
            this.systolic = systolic;
            this.diastolic = diastolic;
            this.heartRate = heartRate;
            this.oxygen = oxygen;
        }

        public String toCSV() {
            return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "," + temperature + "," + systolic + "," + diastolic + "," + heartRate + "," + oxygen;
        }
    }

    static class EmailUtil {
        public static void sendEmergencyEmail(String patientName, String messageBody) {
            String to = "ahmedali253721@gmail.com";
            String from = "doctor420.420.420f@gmail.com";
            String password = "mmqmvvicipvpoyom";
            String host = "smtp.gmail.com";

            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props,
                    new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(from, password);
                        }
                    });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject("🚨 Emergency Alert for " + patientName);
                message.setText(messageBody);

                Transport.send(message);
                System.out.println("Email sent successfully.");
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
