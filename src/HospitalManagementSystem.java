//import java.sql.*;
//import java.util.*;
//
//// ----------------- Main Class -----------------
//public class HospitalManagementSystem {
//    static Scanner sc = new Scanner(System.in);
//
//    public static void main(String[] args) {
//        while (true) {
//            System.out.println("\n===== Hospital Management System =====");
//            System.out.println("1. Patient Menu");
//            System.out.println("2. Doctor Menu (Password Protected)");
//            System.out.println("3. Room Status / Allocation");
//            System.out.println("4. Bill Generation");
//            System.out.println("5. View Waiting List"); // <-- new option
//            System.out.println("6. Exit");
//
//            int choice = validateIntInput();
//            switch (choice) {
//                case 1 -> PatientModule.patientMenu();
//                case 2 -> DoctorModule.doctorMenu();
//                case 3 -> RoomModule.showRoomStatus();
//                case 4 -> BillingModule.generateBill();
//                case 5 -> AppointmentModule.viewWaitingList(); // new
//                case 6 -> {
//                    System.out.println("Exiting system. Goodbye!");
//                    System.exit(0);
//                }
//                default -> System.out.println("Invalid choice. Please try again.");
//            }
//
//        }
//    }
//
//    public static int validateIntInput() {
//        while (!sc.hasNextInt()) {
//            System.out.print("Invalid input. Enter a number: ");
//            sc.next();
//        }
//        int num = sc.nextInt();
//        sc.nextLine(); // consume newline
//        return num;
//    }
//}
//
//// ----------------- Database Connection -----------------
//class DBConnection {
//    public static Connection getConnection() {
//        try {
//            return DriverManager.getConnection(
//                    "jdbc:mysql://localhost:3306/A2", "root", "");
//        } catch (SQLException e) {
//            System.out.println("Database connection failed: " + e.getMessage());
//            return null;
//        }
//    }
//}
//
//// ----------------- Disease Module -----------------
//class DiseaseModule {
//
//    public static void showDiseases() {
//        try (Connection conn = DBConnection.getConnection();
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery("SELECT * FROM Disease")) {
//
//            System.out.println("\n--- Predefined Diseases ---");
//            while (rs.next()) {
//                System.out.println(rs.getInt("disease_id") + ". " + rs.getString("disease_name"));
//            }
//
//        } catch (SQLException e) {
//            System.out.println("Error fetching diseases: " + e.getMessage());
//        }
//    }
//
//    public static String getSpecializationById(int diseaseId) {
//        try (Connection conn = DBConnection.getConnection();
//             PreparedStatement ps = conn.prepareStatement("SELECT specialization FROM Disease WHERE disease_id=?")) {
//            ps.setInt(1, diseaseId);
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) return rs.getString("specialization");
//        } catch (SQLException e) {
//            System.out.println("Error fetching specialization: " + e.getMessage());
//        }
//        return "General";
//    }
//
//    public static String getDiseaseNameById(int diseaseId) {
//        try (Connection conn = DBConnection.getConnection();
//             PreparedStatement ps = conn.prepareStatement("SELECT disease_name FROM Disease WHERE disease_id=?")) {
//            ps.setInt(1, diseaseId);
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) return rs.getString("disease_name");
//        } catch (SQLException e) {
//            System.out.println("Error fetching disease name: " + e.getMessage());
//        }
//        return null;
//    }
//
//    public static boolean isValidDiseaseId(int diseaseId) {
//        return getDiseaseNameById(diseaseId) != null;
//    }
//}
//
//// ----------------- Room Module -----------------
//class RoomModule {
//
//    public static int getFreeRoom() {
//        try (Connection conn = DBConnection.getConnection();
////             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery("SELECT room_id FROM Room WHERE status='free' LIMIT 1")) {
//            if (rs.next()) return rs.getInt("room_id");
//        } catch (SQLException e) {
//            System.out.println("Error fetching free room: " + e.getMessage());
//        }
//        return -1;
//    }
//
//    public static void showRoomStatus() {
//        try (Connection conn = DBConnection.getConnection();
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery("SELECT * FROM Room")) {
//            System.out.println("\n--- Room Status ---");
//            while (rs.next()) {
//                System.out.println("Room ID: " + rs.getInt("room_id") +
//                        " | Status: " + rs.getString("status") +
//                        " | Charge: ₹" + rs.getDouble("charge"));
//            }
//        } catch (SQLException e) {
//            System.out.println("Error fetching room status: " + e.getMessage());
//        }
//    }
//
//    public static void updateRoomStatus(int roomId, String status) {
//        try (Connection conn = DBConnection.getConnection()) {
//            PreparedStatement ps = conn.prepareStatement("UPDATE Room SET status=? WHERE room_id=?");
//            ps.setString(1, status);
//            ps.setInt(2, roomId);
//            ps.executeUpdate();
//        } catch (SQLException e) {
//            System.out.println("Error updating room status: " + e.getMessage());
//        }
//    }
//
//    public static double getRoomCharge(int roomId) {
//        try (Connection conn = DBConnection.getConnection();
//             PreparedStatement ps = conn.prepareStatement("SELECT charge FROM Room WHERE room_id=?")) {
//            ps.setInt(1, roomId);
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) return rs.getDouble("charge");
//        } catch (SQLException e) {
//            System.out.println("Error fetching room charge: " + e.getMessage());
//        }
//        return 0;
//    }
//}
//
//// ----------------- Doctor Module -----------------
//class DoctorModule {
//    static Scanner sc = new Scanner(System.in);
//    static int attempts = 0;
//
//    public static void doctorMenu() {
//        while (attempts < 5) {
//            System.out.print("Enter doctor menu password: ");
//            String input = sc.nextLine();
//            if (input.equals("admin123")) {
//                showDoctorOptions();
//                return;
//            } else {
//                attempts++;
//                System.out.println("Incorrect password. Attempts left: " + (5 - attempts));
//            }
//        }
//        System.out.println("Too many failed attempts. Access denied.");
//    }
//
//    public static void showDoctorOptions() {
//        System.out.println("\n--- Doctor Menu ---");
//        System.out.println("1. Add Doctor");
//        System.out.println("2. Remove Doctor");
//        System.out.println("3.Back to main menu");
//        System.out.print("Enter your choice: ");
//        int choice = HospitalManagementSystem.validateIntInput();
//
//        switch (choice) {
//            case 1 : addDoctor();
//                break;
//            case 2 : removeDoctor();
//            break;
//            case 3 :{return;}
//            default :System.out.println("Invalid choice.");
//            break;
//        }
//    }
//
//    public static void addDoctor() {
//        try (Connection conn = DBConnection.getConnection()) {
//            if (conn == null) return;
//
//            System.out.print("Enter doctor name: ");
//            String name = sc.nextLine();
//            System.out.print("Enter specialization: ");
//            String spec = sc.nextLine();
//            System.out.print("Enter experience (years): ");
//            int exp = HospitalManagementSystem.validateIntInput();
//
//            PreparedStatement ps = conn.prepareStatement(
//                    "INSERT INTO Doctor(name, specialization, experience) VALUES (?, ?, ?)");
//            ps.setString(1, name);
//            ps.setString(2, spec);
//            ps.setInt(3, exp);
//            ps.executeUpdate();
//            System.out.println("Doctor added successfully.");
//
//        } catch (SQLException e) {
//            System.out.println("Error adding doctor: " + e.getMessage());
//        }
//    }
//    public static void suggestDoctor() {
//        DiseaseModule.showDiseases();
//        System.out.print("Enter Disease ID to find doctor: ");
//        int diseaseId = HospitalManagementSystem.validateIntInput();
//
//        if (!DiseaseModule.isValidDiseaseId(diseaseId)) {
//            System.out.println("Invalid Disease ID");
//            return;
//        }
//
//        suggestDoctorForPatient(diseaseId);
//    }
//    public static void suggestDoctorForPatient(int diseaseId) {
//        String specialization = DiseaseModule.getSpecializationById(diseaseId);
//
//        try (Connection conn = DBConnection.getConnection();
//             PreparedStatement ps = conn.prepareStatement(
//                     "SELECT * FROM Doctor WHERE specialization=?")) {
//
//            ps.setString(1, specialization);
//            ResultSet rs = ps.executeQuery();
//            boolean found = false;
//            System.out.println("\n--- Suggested Doctors ---");
//            while (rs.next()) {
//                found = true;
//                System.out.println("Name: " + rs.getString("name") +
//                        " | Experience: " + rs.getInt("experience") + " years");
//            }
//            if (!found) System.out.println("No doctor available for this disease.");
//        } catch (SQLException e) {
//            System.out.println("Error fetching doctors: " + e.getMessage());
//        }
//    }
//
//    public static void removeDoctor() {
//        try (Connection conn = DBConnection.getConnection()) {
//            if (conn == null) return;
//
//            System.out.print("Enter doctor ID to remove: ");
//            int id = HospitalManagementSystem.validateIntInput();
//
//            PreparedStatement ps = conn.prepareStatement("DELETE FROM Doctor WHERE doctor_id=?");
//            ps.setInt(1, id);
//            int rows = ps.executeUpdate();
//            System.out.println(rows > 0 ? "Doctor removed successfully." : "Doctor ID not found.");
//        } catch (SQLException e) {
//            System.out.println("Error removing doctor: " + e.getMessage());
//        }
//    }
//}
//
//// ----------------- Patient Module -----------------
//class PatientModule {
//    static Scanner sc = new Scanner(System.in);
//
//    public static void patientMenu() {
//        while (true) {
//            System.out.println("\n--- Patient Menu ---");
//            System.out.println("1. Add New Patient");
//            System.out.println("2. Remove Patient");
//            System.out.println("3. View Patients");
//            System.out.println("4. Back to Main Menu");
//            System.out.print("Enter your choice: ");
//            int choice = HospitalManagementSystem.validateIntInput();
//
//            switch (choice) {
//                case 1 -> addPatient();
//                case 2 -> removePatient();
//                case 3 -> viewPatients();
//                case 4 -> { return; }
//                default -> System.out.println("Invalid choice.");
//            }
//        }
//    }
//
//    public static void addPatient() {
//        try (Connection conn = DBConnection.getConnection()) {
//            if (conn == null) return;
//
//            System.out.print("Enter patient name: ");
//            String name = sc.nextLine().trim();
//            if (name.isEmpty()) {
//                System.out.println("Name cannot be empty");
//                return;
//            }
//
//            System.out.print("Enter age: ");
//            int age = HospitalManagementSystem.validateIntInput();
//            if (age <= 0) {
//                System.out.println("Invalid age");
//                return;
//            }
//
//            DiseaseModule.showDiseases();
//            System.out.print("Enter disease ID from list: ");
//            int diseaseId = HospitalManagementSystem.validateIntInput();
//            if (!DiseaseModule.isValidDiseaseId(diseaseId)) {
//                System.out.println("Invalid disease ID");
//                return;
//            }
//            String disease = DiseaseModule.getDiseaseNameById(diseaseId);
//
//            int roomId = RoomModule.getFreeRoom();
//            if (roomId == -1) {
//                System.out.println("No rooms available");
//                return;
//            }
//
//            // Insert patient and get generated patient_id
//            PreparedStatement ps = conn.prepareStatement(
//                    "INSERT INTO Patient(name, age, disease, room_id) VALUES(?,?,?,?)",
//                    Statement.RETURN_GENERATED_KEYS); // important
//
//            ps.setString(1, name);
//            ps.setInt(2, age);
//            ps.setString(3, disease);
//            ps.setInt(4, roomId);
//            ps.executeUpdate();
//
//            ResultSet rs = ps.getGeneratedKeys();
//            int patientId = -1;
//            if (rs.next()) {
//                patientId = rs.getInt(1);
//            }
//            rs.close();
//
//            System.out.println("Patient added successfully. Patient ID: " + patientId);
//
//            // Mark room as occupied
//            RoomModule.updateRoomStatus(roomId, "occupied");
//
//            // Suggest doctor automatically
//            DoctorModule.suggestDoctorForPatient(diseaseId);  // modified method below
//
//            // Book appointment using patientId and diseaseId
//            AppointmentModule.bookAppointment(patientId, diseaseId);
//
//        } catch (SQLException e) {
//            System.out.println("Error adding patient: " + e.getMessage());
//        }
//    }
//
//
//    // removePatient() and viewPatients() unchanged (already correct)
//    public static void removePatient() {
//        try (Connection conn = DBConnection.getConnection()) {
//            if (conn == null) return;
//
//            Statement stmt = conn.createStatement();
//            ResultSet rs = stmt.executeQuery("SELECT * FROM Patient");
//            int count = 0;
//            System.out.println("\n--- Patients ---");
//            while (rs.next()) {
//                count++;
//                System.out.println(count + ". ID:" + rs.getInt("patient_id") +
//                        " | Name:" + rs.getString("name") +
//                        " | Disease:" + rs.getString("disease") +
//                        " | Room:" + rs.getInt("room_id"));
//            }
//
//            if (count == 0) { System.out.println("No patients to remove"); return; }
//
//            System.out.print("Enter Patient ID to remove: ");
//            int id = HospitalManagementSystem.validateIntInput();
//
//            // Get room id to free
//            PreparedStatement psGet = conn.prepareStatement("SELECT room_id FROM Patient WHERE patient_id=?");
//            psGet.setInt(1, id);
//            ResultSet rsRoom = psGet.executeQuery();
//            int roomId = -1;
//            if (rsRoom.next()) roomId = rsRoom.getInt("room_id");
//
//            PreparedStatement ps = conn.prepareStatement("DELETE FROM Patient WHERE patient_id=?");
//            ps.setInt(1, id);
//            int rows = ps.executeUpdate();
//            if (rows > 0) {
//                System.out.println("Patient removed successfully.");
//                if (roomId != -1) RoomModule.updateRoomStatus(roomId, "free");
//            } else System.out.println("Patient ID not found.");
//
//        } catch (SQLException e) {
//            System.out.println("Error removing patient: " + e.getMessage());
//        }
//    }
//
//    public static void viewPatients() {
//        try (Connection conn = DBConnection.getConnection();
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery("SELECT * FROM Patient")) {
//
//            System.out.println("\n--- Patients ---");
//            boolean hasPatient = false;
//            while (rs.next()) {
//                hasPatient = true;
//                System.out.println("ID:" + rs.getInt("patient_id") +
//                        " | Name:" + rs.getString("name") +
//                        " | Age:" + rs.getInt("age") +
//                        " | Disease:" + rs.getString("disease") +
//                        " | Room:" + rs.getInt("room_id"));
//            }
//            if (!hasPatient) System.out.println("No patients added yet.");
//
//        } catch (SQLException e) {
//            System.out.println("Error fetching patients: " + e.getMessage());
//        }
//    }
//}
//// ----------------- Appointment Module -----------------
//class AppointmentModule {
//    static Scanner sc = new Scanner(System.in);
//    static MyQueue waitingList = new MyQueue(); // in-memory queue
//
//    public static void bookAppointment(int patientId, int diseaseId) {
//        try (Connection conn = DBConnection.getConnection()) {
//            if (conn == null) return;
//
//            // Get patient info
//            PreparedStatement psPatient = conn.prepareStatement("SELECT * FROM Patient WHERE patient_id=?");
//            psPatient.setInt(1, patientId);
//            ResultSet rsPatient = psPatient.executeQuery();
//            if (!rsPatient.next()) return;
//
//            String patientName = rsPatient.getString("name");
//            String diseaseName = DiseaseModule.getDiseaseNameById(diseaseId);
//            String specialization = DiseaseModule.getSpecializationById(diseaseId);
//
//            // Find free doctor
//            PreparedStatement psDoctor = conn.prepareStatement(
//                    "SELECT * FROM Doctor WHERE specialization=? AND status='free' LIMIT 1");
//            psDoctor.setString(1, specialization);
//            ResultSet rsDoctor = psDoctor.executeQuery();
//
//            if (rsDoctor.next()) {
//                int doctorId = rsDoctor.getInt("doctor_id");
//                String doctorName = rsDoctor.getString("name");
//
//                // Mark doctor as busy
//                PreparedStatement psUpdate = conn.prepareStatement(
//                        "UPDATE Doctor SET status='busy' WHERE doctor_id=?");
//                psUpdate.setInt(1, doctorId);
//                psUpdate.executeUpdate();
//
//                System.out.println("Appointment confirmed!");
//                System.out.println("Patient: " + patientName + " | Doctor: " + doctorName);
//
//            } else {
//                // Add to in-memory queue
//                waitingList.enqueue(patientName + " (" + diseaseName + ")");
//
//                // Add to database waiting list
//                PreparedStatement psWait = conn.prepareStatement(
//                        "INSERT INTO WaitingList(patient_id, disease_id) VALUES(?,?)");
//                psWait.setInt(1, patientId);
//                psWait.setInt(2, diseaseId);
//                psWait.executeUpdate();
//
//                System.out.println("No doctor available. Added to waiting list (queue + DB).");
//            }
//
//        } catch (SQLException e) {
//            System.out.println("Error booking appointment: " + e.getMessage());
//        }
//    }
//
//    // View waiting list from both queue and database
//    public static void viewWaitingList() {
//        System.out.println("\n--- In-Memory Waiting List ---");
//        waitingList.display();
//
//        System.out.println("\n--- Database Waiting List ---");
//        try (Connection conn = DBConnection.getConnection();
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(
//                     "SELECT w.id, p.name, d.disease_name, w.status " +
//                             "FROM WaitingList w " +
//                             "JOIN Patient p ON w.patient_id = p.patient_id " +
//                             "JOIN Disease d ON w.disease_id = d.disease_id " +
//                             "ORDER BY w.created_at ASC")) {
//
//            boolean empty = true;
//            while (rs.next()) {
//                empty = false;
//                System.out.println("ID:" + rs.getInt("id") +
//                        " | Patient: " + rs.getString("name") +
//                        " | Disease: " + rs.getString("disease_name") +
//                        " | Status: " + rs.getString("status"));
//            }
//            if (empty) System.out.println("No patients in database waiting list.");
//        } catch (SQLException e) {
//            System.out.println("Error fetching database waiting list: " + e.getMessage());
//        }
//    }
//}
//
//
//
//class MyQueue {
//    static class Node {
//        String data;
//        Node next;
//        Node(String data) { this.data = data; }
//    }
//
//    private Node front;
//    private Node rear;
//
//    // Check if queue is empty
//    public boolean isEmpty() {
//        return front == null;
//    }
//
//    // Enqueue: add to rear
//    public void enqueue(String data) {
//        Node newNode = new Node(data);
//        if (rear != null) rear.next = newNode;
//        rear = newNode;
//        if (front == null) front = rear;
//    }
//
//    // Dequeue: remove from front
//    public String dequeue() {
//        if (isEmpty()) return null;
//        String value = front.data;
//        front = front.next;
//        if (front == null) rear = null;
//        return value;
//    }
//
//    // Peek: see the front element
//    public String peek() {
//        if (isEmpty()) return null;
//        return front.data;
//    }
//
//    // Display queue
//    public void display() {
//        if (isEmpty()) {
//            System.out.println("No patients in waiting list.");
//            return;
//        }
//        Node temp = front;
//        int i = 1;
//        System.out.println("\n--- Waiting List ---");
//        while (temp != null) {
//            System.out.println(i + ". " + temp.data);
//            temp = temp.next;
//            i++;
//        }
//    }
//}
//
//// ----------------- Billing Module -----------------
//class BillingModule {
//    static Scanner sc = new Scanner(System.in);
//
//    public static void generateBill() {
//        try (Connection conn = DBConnection.getConnection()) {
//            Statement stmt = conn.createStatement();
//            ResultSet rs = stmt.executeQuery("SELECT * FROM Patient");
//            int count = 0;
//            List<Integer> patientIds = new ArrayList<>();
//            System.out.println("\n--- Patients ---");
//            while (rs.next()) {
//                count++;
//                patientIds.add(rs.getInt("patient_id"));
//                System.out.println(count + ". ID:" + rs.getInt("patient_id") +
//                        " | Name:" + rs.getString("name") +
//                        " | Disease:" + rs.getString("disease"));
//            }
//
//            if (count == 0) { System.out.println("No patients to bill."); return; }
//
//            System.out.print("Enter patient number to generate bill: ");
//            int selection = HospitalManagementSystem.validateIntInput();
//            if (selection <= 0 || selection > patientIds.size()) {
//                System.out.println("Invalid selection."); return;
//            }
//
//            int patientId = patientIds.get(selection - 1);
//
//            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Patient WHERE patient_id=?");
//            ps.setInt(1, patientId);
//            ResultSet patientRs = ps.executeQuery();
//            if (patientRs.next()) {
//                String name = patientRs.getString("name");
//                String disease = patientRs.getString("disease");
//                int roomId = patientRs.getInt("room_id");
//                double roomCharge = RoomModule.getRoomCharge(roomId);
//                double totalBill = 700 + roomCharge; // fixed ₹700 + room charge
//                System.out.println("Patient: " + name + " | Disease: " + disease +
//                        " | Room Charge: ₹" + roomCharge +
//                        " | Total Bill: ₹" + totalBill);
//            }
//
//        } catch (SQLException e) {
//            System.out.println("Error generating bill: " + e.getMessage());
//        }
//    }
//}
