package hospital;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class HospitalSystem {
    // Database credentials - update username ("root") and password ("") if needed
    static final String DB_URL = "jdbc:mysql://localhost:3306/hospital_db";
    static final String USER = "root";
    static final String PASS = "Aiswarya@2007";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        System.out.println("==========================================");
        System.out.println("  Hospital Appointment System (MySQL DB)  ");
        System.out.println("==========================================");

        do {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Book a New Appointment");
            System.out.println("2. View All Appointments");
            System.out.println("3. Cancel an Appointment");
            System.out.println("4. Exit");
            System.out.print("Enter your choice (1-4): ");

            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input! Please enter a number (1-4): ");
                scanner.next();
            }
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    System.out.print("\nEnter Patient Name: ");
                    String pName = scanner.nextLine();
                    
                    System.out.print("Enter Doctor Name (e.g., Dr. Smith): ");
                    String dName = scanner.nextLine();
                    
                    System.out.print("Enter Appointment Date (DD-MM-YYYY): ");
                    String date = scanner.nextLine();

                    // Inserting data into MySQL database
                    try {
                        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                        String query = "INSERT INTO appointments (patient_name, doctor_name, appointment_date) VALUES (?, ?, ?)";
                        PreparedStatement pst = conn.prepareStatement(query);
                        pst.setString(1, pName);
                        pst.setString(2, dName);
                        pst.setString(3, date);
                        pst.executeUpdate();
                        conn.close();
                        
                        System.out.println("-> Appointment booked and saved to MySQL successfully!");
                    } catch (Exception e) {
                        System.out.println("Database Error: " + e.getMessage());
                    }
                    break;

                case 2:
                    System.out.println("\n--- Current Scheduled Appointments (from MySQL) ---");
                    try (
                        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                        Statement stmt = conn.createStatement();
                    ) {
                        // 1. Automatically delete past appointments
                        String deleteSql = "DELETE FROM appointments WHERE STR_TO_DATE(appointment_date, '%d-%m-%Y') < CURDATE()";
                        stmt.executeUpdate(deleteSql);

                        // 2. Fetch and display the remaining appointments once
                        String selectSql = "SELECT * FROM appointments";
                        ResultSet rs = stmt.executeQuery(selectSql);
                        
                        boolean hasAppointments = false;
                        while (rs.next()) {
                            hasAppointments = true;
                            System.out.println("ID: " + rs.getInt("id") + 
                                               " | Patient: " + rs.getString("patient_name") + 
                                               " | Doctor: " + rs.getString("doctor_name") + 
                                               " | Date: " + rs.getString("appointment_date"));
                        }
                        
                        if (!hasAppointments) {
                            System.out.println("No appointments found in the database yet.");
                        }
                    } catch (Exception e) {
                        System.out.println("Database Error: " + e.getMessage());
                    }
                    break;
                    
                case 3:
                    System.out.print("Enter Appointment ID to cancel: ");
                    int cancelId = scanner.nextInt();
                    scanner.nextLine(); // consume newline

                    String deleteQuery = "DELETE FROM appointments WHERE id = ?";
                    try (
                        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                        PreparedStatement pstmt = conn.prepareStatement(deleteQuery);
                    ) {
                        pstmt.setInt(1, cancelId);
                        int rowsAffected = pstmt.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("-> Appointment ID " + cancelId + " cancelled successfully!");
                        } else {
                            System.out.println("-> Error: Appointment ID not found.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case 4:
                    System.out.println("\nExiting system. Thank you!");
                    break;

                default:
                    System.out.println("Invalid choice! Please choose between 1 and 4.");
            }

        } while (choice != 4);

        scanner.close();
    }
}