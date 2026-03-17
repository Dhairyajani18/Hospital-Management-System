// Combined Hospital Management System with Login System
//Enhanced with Admin, Patient, and Doctor Login
import HMS_db.DatabaseConnection;
import HMS_ds.MyQueue;
import HMS_room.RoomManagement;
import java.sql.*;
import java.util.*;
import java.util.InputMismatchException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

 

// User Authentication Classes
class User {
    int id;
    String username;
    String password;
    String role;
    String name;

    public User(int id, String username, String password, String role, String name) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getName() { return name; }
}

class Admin extends User {
    public Admin(int id, String username, String password, String name) {
        super(id, username, password, "ADMIN", name);
    }
}

class PatientUser extends User {
    int patientId;

    public PatientUser(int id, String username, String password, String name, int patientId) {
        super(id, username, password, "PATIENT", name);
        this.patientId = patientId;
    }

    public int getPatientId() { return patientId; }
}

class DoctorUser extends User {
    int doctorId;
    String specialization;

    public DoctorUser(int id, String username, String password, String name, int doctorId, String specialization) {
        super(id, username, password, "DOCTOR", name);
        this.doctorId = doctorId;
        this.specialization = specialization;
    }

    public int getDoctorId() { return doctorId; }
    public String getSpecialization() { return specialization; }
}

class AuthenticationManager {
    static Connection conn;

    public static void setConnection(Connection connection) {
        conn = connection;
    }

    // Use plain text password (simplified for this application)
    public static String hashPassword(String password) {
        return password; // Return password as-is
    }

    // Authenticate user and return User object
    public static User authenticate(String username, String password) {
        try {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                int id = rs.getInt("id");
                String name = rs.getString("name");

                switch (role.toUpperCase()) {
                    case "ADMIN":
                        return new Admin(id, username, password, name);
                    case "PATIENT":
                        int patientId = rs.getInt("patient_id");
                        return new PatientUser(id, username, password, name, patientId);
                    case "DOCTOR":
                        int doctorId = rs.getInt("doctor_id");
                        String specialization = rs.getString("specialization");
                        return new DoctorUser(id, username, password, name, doctorId, specialization);
                }
            }
        } catch (SQLException e) {
            System.out.println("Authentication failed: " + e.getMessage());
        }
        return null;
    }

    // Create user account
    public static boolean createUser(String username, String password, String role, String name, Integer patientId, Integer doctorId, String specialization) {
        try {
            // Check if username already exists
            String checkSql = "SELECT username FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("Username already exists!");
                return false;
            }

            String sql = "INSERT INTO users (username, password, role, name, patient_id, doctor_id, specialization) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role.toUpperCase());
            stmt.setString(4, name);
            stmt.setObject(5, patientId);
            stmt.setObject(6, doctorId);
            stmt.setString(7, specialization);

            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.out.println("Error creating user: " + e.getMessage());
            return false;
        }
    }
}



class h2 {
    static final Scanner sc = new Scanner(System.in);
    static Connection conn;
    static HashMap<Integer, Doctor> doctors = new HashMap<>();
    static RoomManagement rm = new RoomManagement();
    static final int MAX_APPOINTMENTS_PER_DOCTOR = 5;
    static MyQueue<Appointment> waitingList = new MyQueue<>();

    static User currentUser = null;


    public static void main(String[] args) throws SQLException {
        conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        AuthenticationManager.setConnection(conn);
        initializeTables();

        // Main login loop
        while (true) {
            if (currentUser == null) {
                showLoginMenu();
            } else {
                showRoleBasedMenu();
            }
        }
    }

    static void showLoginMenu() throws SQLException {
        System.out.println("\n=== HOSPITAL MANAGEMENT SYSTEM ===");
        System.out.println("1. Login");
        System.out.println("2. Register New User (Admin Only)");
        System.out.println("3. Exit");
        System.out.print("Enter choice: ");

        int choice = -1;
        try {
            choice = sc.nextInt();
            sc.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input! Please enter a number.");
            sc.nextLine();
        }


        switch (choice) {
            case 1 -> performLogin();
            case 2 -> registerUser();
            case 3 -> {
                System.out.println("Thank you for using Hospital Management System!");
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
            default -> System.out.println("Invalid choice!");
        }
    }

    static void performLogin() {
        int attempts = 0;

        while (true) {
            System.out.print("Username: ");
            String username = sc.nextLine();
            System.out.print("Password: ");
            String password = sc.nextLine();

            User user = AuthenticationManager.authenticate(username, password);

            if (user != null) {
                currentUser = user;
                System.out.println("\nLogin successful! Welcome, " + user.getName() + " (" + user.getRole() + ")");
                break;
            } else {
                attempts++;
                System.out.println("\nInvalid username or password!");

                // After maximum 5 attempts, show forgot password option
                if (attempts >= 5) {
                    while (true) {
                        System.out.println("Do you want to  retrieve password?");
                        System.out.println("1. Yes");
                        System.out.println("2. No");
                        System.out.print("Enter your choice: ");
                        String input = sc.nextLine();

                        int choice;
                        try {
                            choice = Integer.parseInt(input);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input! Only numbers are allowed.");
                            continue;
                        }

                        switch (choice) {
                            case 1 : {
                                // Forgot password flow
                                while (true) {
                                    System.out.print("Enter your username: ");
                                    String uname = sc.nextLine();
                                    System.out.print("Enter your ID: ");
                                    int id;
                                    try {
                                        id = Integer.parseInt(sc.nextLine());
                                    } catch (NumberFormatException e) {
                                        System.out.println("ID must be a number!");
                                        continue;
                                    }

                                    try {

                                        String sql = "SELECT password FROM users WHERE username=? AND (patient_id=? OR doctor_id=?)";
                                        PreparedStatement pst = conn.prepareStatement(sql);
                                        pst.setString(1, uname);
                                        pst.setInt(2, id);
                                        pst.setInt(3, id);
                                        ResultSet rs = pst.executeQuery();

                                        if (rs.next()) {
                                            System.out.println("Your password is: " + rs.getString("password"));
                                            break;
                                        } else {
                                            System.out.println("Username or ID not found! Please try again.");
                                        }
                                    } catch (SQLException e) {
                                        System.out.println("Database error: " + e.getMessage());
                                    }
                                }
                                break; // exit forgot password option
                            }
                            case 2 : {
                                System.out.println("Returning to login...");
                                break; // exit forgot password option
                            }
                            default : System.out.println("Invalid choice! Enter 1 or 2.");
                        }

                        if (choice == 1 || choice == 2) break;
                    }
                    attempts = 0; // reset attempts after forgot password option
                }
            }
        }
    }


    static void showRoleBasedMenu() {
        System.out.println("\n=== " + currentUser.getRole() + " DASHBOARD ===");
        System.out.println("Logged in as: " + currentUser.getName());

        switch (currentUser.getRole()) {
            case "ADMIN" : showAdminMenu();break;
            case "PATIENT" : showPatientMenu();break;
            case "DOCTOR" : showDoctorMenu();break;
        }
    }

    static void showAdminMenu() {
        System.out.println("\n--- Admin Menu ---");
        System.out.println("1. Patient Management");
        System.out.println("2. Doctor Management");
        System.out.println("3. Billing Management");
        System.out.println("4. Room Management");
        System.out.println("5. View All Appointments");
        System.out.println("6. User Management");
        System.out.println("7. View Waiting List");
        System.out.println("8. Logout");
        System.out.print("Enter choice: ");

        int choice = sc.nextInt();
        sc.nextLine();

        try {
            switch (choice) {
                case 1 : patientManagementMenu();break;
                case 2 : doctorManagementMenu();break;
                case 3 : billingManagementMenu();break;
                case 4 : rm.roomMenu();break;
                case 5 :viewAllAppointments();break;
                case 6 : userManagementMenu();break;
                case 7 : viewAllWaitingList();break;
                case 8 : {
                    currentUser = null;
                    System.out.println("Logged out successfully!");
                }break;
                default :System.out.println("Invalid choice!");break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void initializeTables() {
        try {
            String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(50) UNIQUE NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        role ENUM('ADMIN', 'PATIENT', 'DOCTOR') NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        patient_id INT NULL,
                        doctor_id INT NULL,
                        specialization VARCHAR(100) NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )""";

            Statement stmt = conn.createStatement();
            stmt.execute(createUsersTable);

            // Create default admin if not exists or recreate with correct hash
            String checkAdmin = "SELECT * FROM users WHERE username = 'admin' AND role = 'ADMIN'";
            ResultSet rs = stmt.executeQuery(checkAdmin);
            if (!rs.next()) {
                AuthenticationManager.createUser("admin", "admin@123", "ADMIN", "System Administrator", null, null, null);
                System.out.println("Default admin created: username=admin, password=admin@123");
            } else {
                // If admin exists, ensure it has the correct hash
                String deleteOldAdmin = "DELETE FROM users WHERE username = 'admin'";
                stmt.executeUpdate(deleteOldAdmin);
                AuthenticationManager.createUser("admin", "admin@123", "ADMIN", "System Administrator", null, null, null);
                System.out.println("Admin user recreated with correct password");
            }
        } catch (SQLException e) {
            System.out.println("Error initializing tables: " + e.getMessage());
        }
    }

    static void registerUser() throws SQLException {
        System.out.println("\n--- User Registration ---");
        System.out.print("Admin Username: ");
        String adminUsername = sc.nextLine();
        System.out.print("Admin Password: ");
        String adminPassword = sc.nextLine();

        User admin = AuthenticationManager.authenticate(adminUsername, adminPassword);
        if (admin == null || !admin.getRole().equals("ADMIN")) {
            System.out.println("Access denied! Only admins can register new users.");
            return;
        }

        System.out.print("New Username: ");
        String username = sc.nextLine();
        System.out.print("New Password: ");
        String password = sc.nextLine();
        String name;
        while (true) {
            System.out.print("Full Name: ");
            name = sc.nextLine();

            boolean valid = true;

            for (int i = 0; i < name.length(); i++) {
                char ch = name.charAt(i);

                // allow only letters and spaces
                if (!(Character.isLetter(ch) || Character.isSpaceChar(ch))) {
                    valid = false;
                    break;
                }
            }

            if (valid && name.trim().length() > 0) {
                break;
            } else {
                System.out.println(" Invalid name. Only letters and spaces are allowed.");
            }
        }


        System.out.println("Select Role:");
        System.out.println("1. Admin");
        System.out.println("2. Patient");
        System.out.println("3. Doctor");
        System.out.print("Choice: ");
        int roleChoice = sc.nextInt();
        sc.nextLine();

        String role = "";
        Integer patientId = null;
        Integer doctorId = null;
        String specialization = null;

        switch (roleChoice) {
            case 1 -> role = "ADMIN";
            case 2 -> {
                role = "PATIENT";
                //  unique patient ID validation

                while (true) {
                    System.out.print("Patient ID: ");
                    patientId = sc.nextInt();
                    sc.nextLine();
                    String sql = "SELECT patient_id FROM users WHERE patient_id=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, patientId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        System.out.println(" Patient ID already exists. Try again.");
                    } else break;
                }
                patientId = patientId; // store after validation
            }

            case 3 -> {
                role = "DOCTOR";
                // unique doctor ID validation

                while (true) {
                    System.out.print("Doctor ID: ");
                    doctorId = sc.nextInt();
                    sc.nextLine();
                    String sql = "SELECT doctor_id FROM users WHERE doctor_id=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, doctorId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        System.out.println(" Doctor ID already exists. Try again.");
                    } else break;
                }
                doctorId = doctorId;

                System.out.print("Specialization: ");
                specialization = sc.nextLine();
            }

            default -> {
                System.out.println("Invalid role selection!");
                return;
            }
        }

        if (AuthenticationManager.createUser(username, password, role, name, patientId, doctorId, specialization)) {
            System.out.println(" User registered successfully!");
        } else {
            System.out.println(" Registration failed!");
        }
    }

    static void showPatientMenu() {
        PatientUser patient = (PatientUser) currentUser;
        System.out.println("\n--- Patient Menu ---");
        System.out.println("1. View My Profile");
        System.out.println("2. Update My Information");
        System.out.println("3. Book Appointment");
        System.out.println("4. View My Appointments");
        System.out.println("5. Cancel Appointment");
        System.out.println("6. View My Bills");
        System.out.println("7. View Available Doctors");
        System.out.println("8. Logout");
        System.out.print("Enter choice: ");

        int choice = -1;
        try {
            choice = sc.nextInt();
            sc.nextLine(); // consume newline
        } catch (Exception e) {
            System.out.println("Invalid input! Please enter a number.");
            sc.nextLine(); // clear invalid input
        }


        try {
            switch (choice) {
                case 1 :viewMyProfile(patient.getPatientId());break;
                case 2 : updateMyProfile(patient.getPatientId());break;
                case 3 : makeAppointmentForPatient(patient.getPatientId());break;
                case 4 : viewMyAppointments(patient.getPatientId());break;
                case 5 : cancelMyAppointment(patient.getPatientId());break;
                case 6 : viewMyBills(patient.getPatientId());break;
                case 7 : viewAvailableDoctors();break;
                case 8 : {
                    currentUser = null;
                    System.out.println("Logged out successfully!");
                }
                break;
                default : System.out.println("Invalid choice!");break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void showDoctorMenu() {
        DoctorUser doctor = (DoctorUser) currentUser;

        int choice = -1;
        while (true) {
            System.out.println("\n--- Doctor Menu ---");
            System.out.println("1. View My Profile");
            System.out.println("2. View My Appointments");
            System.out.println("3. View My Schedule");
            System.out.println("4. Update My Availability");
            System.out.println("5. View Patient Details");
            System.out.println("6. Generate Patient Bill");
            System.out.println("7. Logout");
            System.out.print("Enter choice: ");

            try {
                choice = sc.nextInt();
                sc.nextLine(); // consume newline
                break; // valid int → exit loop
            } catch (InputMismatchException e) {
                System.out.println(" Invalid input! Please enter a number (1–7).");
                sc.nextLine(); // clear invalid input
            }
        }

        try {
            switch (choice) {
                case 1 : viewDoctorProfile(doctor.getDoctorId());break;
                case 2 : viewDoctorAppointments(doctor.getDoctorId());break;
                case 3 : viewDoctorSchedule(doctor.getDoctorId());break;
                case 4 : updateDoctorAvailability(doctor.getDoctorId());break;
                case 5 : viewPatient();break;
                case 6 : generateBill();break;
                case 7 : {
                    currentUser = null;
                    System.out.println("Logged out successfully!");
                }break;
                default : System.out.println("Invalid choice!");break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void userManagementMenu() {
        System.out.println("\n--- User Management ---");
        System.out.println("1. View All Users");
        System.out.println("2. Create New User");
        System.out.println("3. Delete User");
        System.out.println("4. Back");
        System.out.print("Enter choice: ");

        int choice = sc.nextInt();
        sc.nextLine();

        try {
            switch (choice) {
                case 1 :viewAllUsers();break;
                case 2 : registerUser();break;
                case 3 : deleteUser();break;
                case 4 : {
                    return;
                }
                default : System.out.println("Invalid choice!");break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void viewAllUsers() throws SQLException {
        String sql = "SELECT id, username, role, name, patient_id, doctor_id FROM users ORDER BY role, name";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        System.out.println("\n--- All Users ---");
        System.out.printf("%-5s %-15s %-10s %-20s %-10s %-10s%n", "ID", "Username", "Role", "Name", "Patient ID", "Doctor ID");
        System.out.println("=".repeat(80));

        while (rs.next()) {
            System.out.printf("%-5d %-15s %-10s %-20s %-10s %-10s%n",
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getString("name"),
                    rs.getObject("patient_id") != null ? rs.getInt("patient_id") : "-",
                    rs.getObject("doctor_id") != null ? rs.getInt("doctor_id") : "-");
        }
    }

    static void deleteUser() throws SQLException {
        System.out.print("Enter username to delete: ");
        String username = sc.nextLine();

        if (username.equals("admin")) {
            System.out.println("Cannot delete admin user!");
            return;
        }

        String sql = "DELETE FROM users WHERE username = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);

        int result = stmt.executeUpdate();
        if (result > 0) {
            System.out.println("User deleted successfully!");
        } else {
            System.out.println("User not found!");
        }
    }

    // Patient-specific methods
    static void viewMyProfile(int patientId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, patientId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            System.out.println("\n--- My Profile ---");
            System.out.println("ID: " + rs.getInt("id"));
            System.out.println("Name: " + rs.getString("name"));
            System.out.println("Age: " + rs.getInt("age"));
            System.out.println("Gender: " + rs.getString("gender"));
            System.out.println("Contact: " + rs.getString("contact"));
            System.out.println("Address: " + rs.getString("address"));
            System.out.println("Medical History: " + rs.getString("history"));
            if (rs.getString("type") != null) {
                System.out.println("Type: " + rs.getString("type"));
            }
        } else {
            System.out.println("Profile not found!");
        }
    }

    static void updateMyProfile(int patientId) throws SQLException {
        System.out.println("\n--- Update My Profile ---");
        String contact;
        while (true) {
            System.out.print("New contact (10 digits): ");
            contact = sc.nextLine();
            if (contact.matches("\\d{10}")) {
                break;
            } else {
                System.out.println("Invalid contact. Enter 10 digits only.");
            }
        }

        System.out.print("New address: ");
        String address = sc.nextLine();
        // Medical history input with validation (only text allowed, no numbers)
        String history;
        while (true) {
            System.out.print("Update medical history (diseases only, no numbers): ");
            history = sc.nextLine().trim();

            if (history.isEmpty()) {
                System.out.println("Medical history cannot be empty. Please enter disease information.");
                continue;
            }

            // Check if input contains any digits
            boolean hasDigit = false;
            for (int i = 0; i < history.length(); i++) {
                char ch = history.charAt(i);
                if (Character.isDigit(ch)) {
                    hasDigit = true;
                    break;
                }
            }

            if (hasDigit) {
                System.out.println("Medical history should contain disease names only. Numbers are not allowed.");
                continue;
            }

            // Check if input contains at least one letter using core Java
            boolean hasLetter = false;
            for (int i = 0; i < history.length(); i++) {
                char ch = history.charAt(i);
                if (Character.isLetter(ch)) {
                    hasLetter = true;
                    break;
                }
            }

            if (!hasLetter) {
                System.out.println("Please enter valid disease names or medical conditions.");
                continue;
            }

            break;
        }

        String sql = "UPDATE patients SET contact=?, address=?, history=? WHERE id=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, contact);
        stmt.setString(2, address);
        stmt.setString(3, history);
        stmt.setInt(4, patientId);

        int result = stmt.executeUpdate();
        if (result > 0) {
            System.out.println("Profile updated successfully!");
        } else {
            System.out.println("Update failed!");
        }
    }

    static void makeAppointmentForPatient(int patientId) throws SQLException {
        System.out.println("\n--- Book Appointment ---");

        // Display available doctors
        System.out.println("Available Doctors:");
        PreparedStatement docStmt = conn.prepareStatement("SELECT * FROM doctor LIMIT 10");
        ResultSet docs = docStmt.executeQuery();
        while (docs.next()) {
            System.out.println("ID: " + docs.getInt("id") +
                    ", Name: " + docs.getString("name") +
                    ", Specialization: " + docs.getString("specialization") +
                    ", Charge: ₹" + docs.getDouble("treatment_charge"));
        }

        int doctorId = -1;
        boolean valid = false;

        while (!valid) {
            System.out.print("Enter Doctor ID: ");
            try {
                doctorId = sc.nextInt();
                sc.nextLine(); // consume newline
                valid = true;  // input is correct, exit loop
            } catch (Exception e) {
                System.out.println("Invalid input! Doctor ID must be a number.");
                sc.nextLine(); // clear invalid input
            }
        }


        // Validate doctor exists
        PreparedStatement checkDoctor = conn.prepareStatement("SELECT id FROM doctor WHERE id = ?");
        checkDoctor.setInt(1, doctorId);
        ResultSet doctorRS = checkDoctor.executeQuery();
        if (!doctorRS.next()) {
            System.out.println(" Doctor not found!");
            return;
        }

        // Validate appointment date
        String date;
        LocalDate appointmentDate;
        while (true) {
            System.out.print("Enter date (YYYY-MM-DD): ");
            date = sc.nextLine();
            try {
                appointmentDate = LocalDate.parse(date);

                if (appointmentDate.isBefore(LocalDate.now())) {
                    System.out.println("Cannot book appointment in the past. Enter a future date or today's date.");
                    continue;
                }

                break;
            } catch (DateTimeParseException e) {
                System.out.println(" Invalid date format. Try again (YYYY-MM-DD).");
            }
        }

        // Validate appointment time
        String time;
        LocalTime appointmentTime;
        while (true) {
            System.out.print("Enter time (HH:mm): ");
            time = sc.nextLine();
            try {
                appointmentTime = LocalTime.parse(time);

                // If date is today, ensure time is in the future
                if (appointmentDate.equals(LocalDate.now()) && appointmentTime.isBefore(LocalTime.now())) {
                    System.out.println(" Cannot book appointment in the past time today. Enter a future time.");
                    continue;
                }

                break;
            } catch (DateTimeParseException e) {
                System.out.println(" Invalid time format. Try again (HH:mm).");
            }
        }

        // Check doctor availability
        int count = getDoctorAppointmentCount(doctorId, date);

        if (count >= MAX_APPOINTMENTS_PER_DOCTOR) {
            System.out.println(" Doctor has reached maximum appointments (" + MAX_APPOINTMENTS_PER_DOCTOR + ") for " + date + ".");
            System.out.println("You are in waiting list.");
            addToWaitingList(patientId, doctorId, date, time);
            displayWaitingListForDoctor(doctorId);
            return;
        }

        // Book appointment
        String sql = "INSERT INTO appointment (Patient_ID, Doctor_ID, Date, Time) VALUES (?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, patientId);
        pst.setInt(2, doctorId);
        pst.setString(3, date);
        pst.setString(4, time);
        pst.executeUpdate();

        System.out.println(" Appointment booked successfully!");
    }

    static void viewMyAppointments(int patientId) throws SQLException {
        String sql = "SELECT a.*, d.name as doctor_name, d.specialization FROM APPOINTMENT a JOIN doctor d ON a.Doctor_ID = d.id WHERE a.Patient_ID = ? ORDER BY a.Date, a.Time";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, patientId);
        ResultSet rs = stmt.executeQuery();

        System.out.println("\n--- My Appointments ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("Appointment ID: " + rs.getInt("Appointment_ID"));
            System.out.println("Doctor: " + rs.getString("doctor_name") + " (" + rs.getString("specialization") + ")");
            System.out.println("Date: " + rs.getString("Date"));
            System.out.println("Time: " + rs.getString("Time"));
            System.out.println("-".repeat(30));
        }

        if (!found) {
            System.out.println("No appointments found.");
        }
    }

    static void cancelMyAppointment(int patientId) throws SQLException {
        viewMyAppointments(patientId);

        System.out.print("Enter Appointment ID to cancel: ");

        int appointmentId = -1;
        boolean valid = false;

        while (!valid) {
            System.out.print("Enter Appointment ID to cancel: ");
            try {
                appointmentId = sc.nextInt();
                sc.nextLine(); // consume newline
                valid = true;  // input is valid → exit loop
            } catch (Exception e) {
                System.out.println("Invalid input! Appointment ID must be a number.");
                sc.nextLine(); // clear invalid input
            }
        }


        String sql = "DELETE FROM APPOINTMENT WHERE Appointment_ID = ? AND Patient_ID = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, appointmentId);
        stmt.setInt(2, patientId);

        int result = stmt.executeUpdate();
        if (result > 0) {
            System.out.println("Appointment cancelled successfully!");
            // Process waiting list for the doctor
            processWaitingListForCancelledAppointment(appointmentId);
        } else {
            System.out.println("Appointment not found or access denied!");
        }
    }

    static void viewMyBills(int patientId) throws SQLException {
        String sql = "SELECT * FROM bills WHERE patient_id = ? ORDER BY bill_date DESC";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, patientId);
        ResultSet rs = stmt.executeQuery();

        System.out.println("\n--- My Bills ---");
        boolean found = false;
        double totalAmount = 0;

        while (rs.next()) {
            found = true;
            double amount = rs.getDouble("amount");
            totalAmount += amount;

            System.out.println("Bill ID: " + rs.getInt("bill_id"));
            System.out.println("Date: " + rs.getDate("bill_date"));
            System.out.println("Amount: ₹" + amount);
            System.out.println("Status: " + rs.getString("status"));
            System.out.println("-".repeat(30));
        }

        if (found) {
            System.out.println("Total Amount: ₹" + totalAmount);
        } else {
            System.out.println("No bills found.");
        }
    }

    static void viewAvailableDoctors() throws SQLException {
        String sql = "SELECT * FROM doctor ORDER BY specialization, name";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        System.out.println("\n--- Available Doctors ---");
        System.out.printf("%-5s %-20s %-20s %-10s%n", "ID", "Name", "Specialization", "Charge");
        System.out.println("=".repeat(60));

        while (rs.next()) {
            System.out.printf("%-5d %-20s %-20s ₹%-10.2f%n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("specialization"),
                    rs.getDouble("treatment_charge"));
        }
    }

    // Doctor-specific methods
    static void viewDoctorProfile(int doctorId) throws SQLException {
        String sql = "SELECT * FROM doctor WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, doctorId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            System.out.println("\n--- My Profile ---");
            System.out.println("ID: " + rs.getInt("id"));
            System.out.println("Name: " + rs.getString("name"));
            System.out.println("Specialization: " + rs.getString("specialization"));
            System.out.println("Treatment Charge: ₹" + rs.getDouble("treatment_charge"));
        } else {
            System.out.println("Profile not found!");
        }
    }

    static void viewDoctorAppointments(int doctorId) throws SQLException {
        String sql = "SELECT a.*, p.name as patient_name FROM APPOINTMENT a JOIN patients p ON a.Patient_ID = p.id WHERE a.Doctor_ID = ? ORDER BY a.Date, a.Time";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, doctorId);
        ResultSet rs = stmt.executeQuery();

        System.out.println("\n--- My Appointments ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("Appointment ID: " + rs.getInt("Appointment_ID"));
            System.out.println("Patient: " + rs.getString("patient_name"));
            System.out.println("Date: " + rs.getString("Date"));
            System.out.println("Time: " + rs.getString("Time"));
            System.out.println("-".repeat(30));
        }

        if (!found) {
            System.out.println("No appointments found.");
        }
    }

    static void viewDoctorSchedule(int doctorId) throws SQLException {
        String sql = "SELECT slot FROM DoctorSlots WHERE doctor_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, doctorId);
        ResultSet rs = stmt.executeQuery();

        System.out.println("\n--- My Schedule ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("Time Slot: " + rs.getString("slot"));
        }

        if (!found) {
            System.out.println("No schedule found.");
        }
    }

    static void updateDoctorAvailability(int doctorId) throws SQLException {
        System.out.println("\n--- Update Availability ---");
        String[] slots;

        while (true) {
            System.out.println("Enter new time slots (comma separated). Example: 09:00-10:00,10:00-11:00");
            String slotsInput = sc.nextLine();
            slots = slotsInput.split(",");

            boolean valid = true;

            for (String slot : slots) {
                String[] times = slot.trim().split("-");
                if (times.length != 2) {
                    valid = false;
                    break;
                }

                try {
                    LocalTime start = LocalTime.parse(times[0].trim());
                    LocalTime end = LocalTime.parse(times[1].trim());
                    if (!end.isAfter(start)) {
                        valid = false;
                        break;
                    }
                } catch (DateTimeParseException e) {
                    valid = false;
                    break;
                }
            }

            if (valid) break;
            System.out.println("Invalid slot(s) detected. Make sure format is HH:mm-HH:mm and end time is after start time.");
        }

        // Delete existing slots
        String deleteSql = "DELETE FROM DoctorSlots WHERE doctor_id = ?";
        PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
        deleteStmt.setInt(1, doctorId);
        deleteStmt.executeUpdate();

        // Insert new slots
        String insertSql = "INSERT INTO DoctorSlots (doctor_id, slot) VALUES (?, ?)";
        PreparedStatement insertStmt = conn.prepareStatement(insertSql);

        for (String slot : slots) {
            insertStmt.setInt(1, doctorId);
            insertStmt.setString(2, slot.trim());
            insertStmt.addBatch();
        }

        insertStmt.executeBatch();
        System.out.println("Availability updated successfully!");
    }


    // Waiting List Management Methods
    static void addToWaitingList(int patientId, int doctorId, String date, String time) {
        try {
            String sql = "INSERT INTO waiting_list (patient_id, doctor_id, requested_date, requested_time) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setString(3, date);
            stmt.setString(4, time);
            stmt.executeUpdate();

            System.out.println("Added to waiting list successfully!");
            System.out.println("You will be notified when a slot becomes available.");
        } catch (SQLException e) {
            System.out.println("Error adding to waiting list: " + e.getMessage());
        }
    }

    static void displayWaitingListForDoctor(int doctorId) {
        try {
            String sql = "SELECT w.id, w.patient_id, p.name as patient_name, w.requested_date, w.requested_time, w.added_at " +
                    "FROM waiting_list w " +
                    "JOIN patients p ON w.patient_id = p.id " +
                    "WHERE w.doctor_id = ? AND w.status = 'waiting' " +
                    "ORDER BY w.added_at";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            // Get doctor name
            String doctorSql = "SELECT name FROM doctor WHERE id = ?";
            PreparedStatement doctorStmt = conn.prepareStatement(doctorSql);
            doctorStmt.setInt(1, doctorId);
            ResultSet doctorRs = doctorStmt.executeQuery();
            String doctorName = doctorRs.next() ? doctorRs.getString("name") : "Unknown Doctor";

            System.out.println("\n WAITING LIST FOR " + doctorName.toUpperCase() + " (ID: " + doctorId + ")");
            System.out.println("=".repeat(80));

            boolean hasWaitingPatients = false;
            int position = 1;

            while (rs.next()) {
                hasWaitingPatients = true;
                System.out.printf("Position %d: %s (ID: %d)%n",
                        position++,
                        rs.getString("patient_name"),
                        rs.getInt("patient_id"));
                System.out.printf("   Requested: %s at %s%n",
                        rs.getString("requested_date"),
                        rs.getString("requested_time"));
                System.out.printf("   Added to queue: %s%n",
                        rs.getTimestamp("added_at"));
                System.out.println("-".repeat(50));
            }

            if (!hasWaitingPatients) {
                System.out.println("No patients currently in waiting list for this doctor.");
            } else {
                System.out.println("Total patients waiting: " + (position - 1));
            }

        } catch (SQLException e) {
            System.out.println("Error displaying waiting list: " + e.getMessage());
        }
    }

    static void processWaitingListForCancelledAppointment(int appointmentId) {
        try {
            // Get doctor and date from cancelled appointment
            String getAppointmentSql = "SELECT Doctor_ID, Date FROM APPOINTMENT WHERE Appointment_ID = ?";
            PreparedStatement getStmt = conn.prepareStatement(getAppointmentSql);
            getStmt.setInt(1, appointmentId);
            ResultSet appointmentRs = getStmt.executeQuery();

            if (!appointmentRs.next()) {
                // Try to get from recently deleted appointment (this might not work if already deleted)
                System.out.println("Processing waiting list...");
                return;
            }

            int doctorId = appointmentRs.getInt("Doctor_ID");
            String date = appointmentRs.getString("Date");

            // Check if doctor still has capacity for that date
            int currentCount = getDoctorAppointmentCount(doctorId, date);

            if (currentCount < MAX_APPOINTMENTS_PER_DOCTOR) {
                // Get next patient from waiting list for this doctor
                String waitingSql = "SELECT w.id, w.patient_id, w.requested_date, w.requested_time, p.name " +
                        "FROM waiting_list w " +
                        "JOIN patients p ON w.patient_id = p.id " +
                        "WHERE w.doctor_id = ? AND w.status = 'waiting' " +
                        "ORDER BY w.added_at LIMIT 1";
                PreparedStatement waitingStmt = conn.prepareStatement(waitingSql);
                waitingStmt.setInt(1, doctorId);
                ResultSet waitingRs = waitingStmt.executeQuery();

                if (waitingRs.next()) {
                    int waitingId = waitingRs.getInt("id");
                    int patientId = waitingRs.getInt("patient_id");
                    String requestedDate = waitingRs.getString("requested_date");
                    String requestedTime = waitingRs.getString("requested_time");
                    String patientName = waitingRs.getString("name");

                    // Create appointment for waiting patient
                    String createAppointmentSql = "INSERT INTO APPOINTMENT (Patient_ID, Doctor_ID, Date, Time) VALUES (?, ?, ?, ?)";
                    PreparedStatement createStmt = conn.prepareStatement(createAppointmentSql);
                    createStmt.setInt(1, patientId);
                    createStmt.setInt(2, doctorId);
                    createStmt.setString(3, requestedDate);
                    createStmt.setString(4, requestedTime);
                    createStmt.executeUpdate();

                    // Update waiting list status
                    String updateWaitingSql = "UPDATE waiting_list SET status = 'processed' WHERE id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateWaitingSql);
                    updateStmt.setInt(1, waitingId);
                    updateStmt.executeUpdate();

                    System.out.println("✓ Appointment automatically created for " + patientName + " from waiting list!");
                    System.out.println("   Date: " + requestedDate + " Time: " + requestedTime);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error processing waiting list: " + e.getMessage());
        }
    }

    static void viewAllWaitingList() {
        try {
            String sql = "SELECT w.id, p.name as patient_name, d.name as doctor_name, " +
                    "w.requested_date, w.requested_time, w.added_at, w.status " +
                    "FROM waiting_list w " +
                    "JOIN patients p ON w.patient_id = p.id " +
                    "JOIN doctor d ON w.doctor_id = d.id " +
                    "WHERE w.status = 'waiting' " +
                    "ORDER BY w.doctor_id, w.added_at";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("\n ALL WAITING LIST PATIENTS");
            System.out.println("=".repeat(100));
            System.out.printf("%-5s %-20s %-20s %-12s %-8s %-20s%n",
                    "ID", "Patient", "Doctor", "Date", "Time", "Added");
            System.out.println("-".repeat(100));

            boolean hasWaitingPatients = false;
            while (rs.next()) {
                hasWaitingPatients = true;
                System.out.printf("%-5d %-20s %-20s %-12s %-8s %-20s%n",
                        rs.getInt("id"),
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        rs.getString("requested_date"),
                        rs.getString("requested_time"),
                        rs.getTimestamp("added_at"));
            }

            if (!hasWaitingPatients) {
                System.out.println("No patients currently in waiting list.");
            }

        } catch (SQLException e) {
            System.out.println("Error viewing waiting list: " + e.getMessage());
        }
    }


    static class Doctor {
        int id;
        String name;
        String specialization;
        double treatmentCharge;
        ArrayDeque<String> availableSlots = new ArrayDeque<>();

        Doctor(int id, String name, String specialization, double treatmentCharge, List<String> slots) {
            this.id = id;
            this.name = name;
            this.specialization = specialization;
            this.treatmentCharge = treatmentCharge;
            this.availableSlots.addAll(slots);
        }
        boolean isAvailable() {
            return !availableSlots.isEmpty();
        }
        String assignSlot() {
            return availableSlots.poll();
        }
        void displayInfo() {
            System.out.println("ID: " + id + ", Name: " + name + ", Specialization: " + specialization + ", Charge: ₹" + treatmentCharge);
            System.out.println("Available Slots: " + availableSlots);
        }
    }


    static class Appointment {
        int id, patientId, doctorId;
        String date, time;

        Appointment(int id, int patientId, int doctorId, String date, String time) {
            this.id = id;
            this.patientId = patientId;
            this.doctorId = doctorId;
            this.date = date;
            this.time = time;
        }
    }

    static void patientManagementMenu() {
        while (true) {
            System.out.println("\n--- Patient Management ---");
            System.out.println("1. Add Patient");
            System.out.println("2. Delete Patient");
            System.out.println("3. Update Patient");
            System.out.println("4. View Patient");
            System.out.println("5. View Patient History");
            System.out.println("6. Search Patient");
            System.out.println("7. Book Appointment");
            System.out.println("8. Cancel Appointment");
            System.out.println("9. View All Appointments");
            System.out.println("10. Back");
            System.out.print("Enter choice: ");
            int ch = -1;
            try {
                ch = sc.nextInt();
                sc.nextLine();
            } catch (Exception e) {
                System.out.println("Invalid input! Please enter a number.");
                sc.nextLine();
            }


            try {
                switch (ch) {
                    case 1 : addPatient();break;
                    case 2 : deletePatient();break;
                    case 3 : updatePatient();break;
                    case 4 : viewPatient();break;
                    case 5 : viewPatientHistory();break;
                    case 6 : searchPatient();break;
                    case 7 : makeAppointment();break;
                    case 8 : cancelAppointment();break;
                    case 9 : viewAllAppointments();break;
                    case 10 : {
                        return;
                    }
                    default : System.out.println("Invalid choice.");break;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    static void billingManagementMenu() {
        while (true) {
            System.out.println("\n--- Billing Management ---");
            System.out.println("1. Generate Bill");
            System.out.println("2. View Bill");
            System.out.println("3. Update Bill Status");
            System.out.println("4. Back");
            System.out.print("Enter choice: ");
            int ch = -1;
            try {
                ch = sc.nextInt();
                sc.nextLine();
            } catch (Exception e) {
                System.out.println("Invalid input! Please enter a number.");
                sc.nextLine();
            }


            try {
                switch (ch) {
                    case 1 : generateBill();break;
                    case 2 : viewBill();break;
                    case 3 : updateBillStatus();break;
                    case 4 : {
                        return;
                    }
                    default : System.out.println("Invalid choice.");break;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ----------------- Patient Methods ----------------


    static void addPatient() throws SQLException {
        String type;

        // Ask patient type
        while (true) {
            System.out.println("Select Patient Type:");
            System.out.println("1. Inpatient");
            System.out.println("2. Outpatient");
            System.out.print("Enter choice (1 or 2): ");

            if (sc.hasNextInt()) {
                int typeChoice = sc.nextInt();
                sc.nextLine();
                if (typeChoice == 1) {
                    type = "inpatient";
                    break;
                } else if (typeChoice == 2) {
                    type = "outpatient";
                    break;
                } else {
                    System.out.println("Invalid choice. Try again.");
                }
            } else {
                System.out.println("Please enter a valid number (1 or 2).");
                sc.nextLine();
            }
        }

        // Name input with validation (only characters allowed, no digits)
        String name;
        while (true) {
            System.out.print("Enter name (characters only, no numbers): ");
            name = sc.nextLine().trim();

            if (name.isEmpty()) {
                System.out.println("Name cannot be empty. Please enter a valid name.");
                continue;
            }

            // Check if name contains any digits
            boolean hasDigit = false;
            for (int i = 0; i < name.length(); i++) {
                char ch = name.charAt(i);
                if (Character.isDigit(ch)) {
                    hasDigit = true;
                    break;
                }
            }

            if (hasDigit) {
                System.out.println("Name should contain only characters. Numbers are not allowed.");
                continue;
            }

            // Check if name contains at least one letter
            boolean hasLetter = false;
            for (int i = 0; i < name.length(); i++) {
                char ch = name.charAt(i);
                if (Character.isLetter(ch)) {
                    hasLetter = true;
                    break;
                }
            }

            if (!hasLetter) {
                System.out.println("Please enter a valid name with letters.");
                continue;
            }

            break;
        }

        // Age input with validation
        int age;
        while (true) {
            System.out.print("Enter age: ");
            try {
                age = sc.nextInt();
                sc.nextLine();
                if (age < 0) {
                    System.out.println("Age cannot be negative. Please enter a valid age.");
                } else if (age > 150) {
                    System.out.println("Age cannot be greater than 150. Please enter a valid age.");
                } else {
                    break;
                }
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid number for age.");
                sc.nextLine();
            }
        }

        // Validate Gender - only male or female
        String gender;
        while (true) {
            System.out.print("Enter gender (male/female): ");
            gender = sc.nextLine().toLowerCase().trim();
            if (gender.equals("male") || gender.equals("female")) {
                break;
            } else {
                System.out.println("Gender must be either 'male' or 'female'. Try again.");
            }
        }

        // Validate Contact
        String contact;
        while (true) {
            System.out.print("Enter contact (10 digit number): ");
            contact = sc.nextLine();

            if (contact.matches("\\d{10}")) {  // only 10 digits allowed
                break;
            } else {
                System.out.println("Invalid contact number. It must be exactly 10 digits and only numbers.");
            }
        }

        System.out.print("Enter address: ");
        String address = sc.nextLine();

        // Medical history input with validation (only text allowed, no numbers)
        String history;
        while (true) {
            System.out.print("Enter medical history (diseases only, no numbers): ");
            history = sc.nextLine().trim();

            if (history.isEmpty()) {
                System.out.println("Medical history cannot be empty. Please enter disease information.");
                continue;
            }

            // Check if input contains any digits
            boolean hasDigit = false;
            for (int i = 0; i < history.length(); i++) {
                char ch = history.charAt(i);
                if (Character.isDigit(ch)) {
                    hasDigit = true;
                    break;
                }
            }

            if (hasDigit) {
                System.out.println("Medical history should contain disease names only. Numbers are not allowed.");
                continue;
            }

            // Check if input contains at least one letter
            boolean hasLetter = false;
            for (int i = 0; i < history.length(); i++) {
                char ch = history.charAt(i);
                if (Character.isLetter(ch)) {
                    hasLetter = true;
                    break;
                }
            }

            if (!hasLetter) {
                System.out.println("Please enter valid disease names or medical conditions.");
                continue;
            }

            break;
        }


        String sql = "INSERT INTO patients (name, age, gender, contact, address, history, type) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setInt(2, age);
            stmt.setString(3, gender);
            stmt.setString(4, contact);
            stmt.setString(5, address);
            stmt.setString(6, history);
            stmt.setString(7, type);

            stmt.executeUpdate();

            // Get the auto-generated ID
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int generatedId = generatedKeys.getInt(1);
                System.out.println("Patient added successfully with ID: " + generatedId);
            } else {
                System.out.println("Patient added successfully.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Patient ID already exists. Please use a unique ID.");
        }
    }


    public static void deletePatient() throws SQLException {
        System.out.print("Enter Patient ID to delete: ");
        int patientId = -1;
        try {
            patientId = sc.nextInt();
            sc.nextLine();
        } catch (Exception e) {
            System.out.println("Invalid input! Patient ID must be a number.");
            sc.nextLine();
        }



        // Delete related bills first
        String deleteBillsSQL = "DELETE FROM bills WHERE patient_id = ?";
        try (PreparedStatement billStmt = conn.prepareStatement(deleteBillsSQL)) {
            billStmt.setInt(1, patientId);
            billStmt.executeUpdate();
        }

        // Now delete patient
        String deletePatientSQL = "DELETE FROM patients WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deletePatientSQL)) {
            stmt.setInt(1, patientId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Patient and related bills deleted successfully.");
            } else {
                System.out.println("No patient found with that ID.");
            }
        }
    }


    static void updatePatient() throws SQLException {
        System.out.print("Enter patient ID: ");
        int id = -1;

        try {
            id = sc.nextInt();
            sc.nextLine();
        } catch (Exception e) {
            System.out.println("Invalid input! Patient ID must be a number.");
            sc.nextLine();
        }


        // Check if patient ID exists in database
        String checkSQL = "SELECT COUNT(*) FROM patients WHERE id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count == 0) {
                System.out.println("ID is not in database. Please enter a valid patient ID.");
                return;
            }
        }

        // Contact validation loop
        String contact;
        while (true) {
            System.out.print("New contact (10 digits only): ");
            contact = sc.nextLine();
            if (contact.matches("\\d{10}")) { // only digits, exactly 10
                break;
            } else {
                System.out.println(" Invalid contact. Must be exactly 10 digits and numbers only.");
            }
        }

        System.out.print("New address: ");
        String address = sc.nextLine();

        // Medical history input with validation (only text allowed, no numbers)
        String history;
        while (true) {
            System.out.print("Update medical history (diseases only, no numbers): ");
            history = sc.nextLine().trim();

            if (history.isEmpty()) {
                System.out.println("Medical history cannot be empty. Please enter disease information.");
                continue;
            }

            // Check if input contains any digits using core Java
            boolean hasDigit = false;
            for (int i = 0; i < history.length(); i++) {
                char ch = history.charAt(i);
                if (Character.isDigit(ch)) {
                    hasDigit = true;
                    break;
                }
            }

            if (hasDigit) {
                System.out.println("Medical history should contain disease names only. Numbers are not allowed.");
                continue;
            }

            // Check if input contains at least one letter using core Java
            boolean hasLetter = false;
            for (int i = 0; i < history.length(); i++) {
                char ch = history.charAt(i);
                if (Character.isLetter(ch)) {
                    hasLetter = true;
                    break;
                }
            }

            if (!hasLetter) {
                System.out.println("Please enter valid disease names or medical conditions.");
                continue;
            }

            break;
        }

        String sql = "UPDATE patients SET contact=?, address=?, history=? WHERE id=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, contact);
        stmt.setString(2, address);
        stmt.setString(3, history);
        stmt.setInt(4, id);
        int rowsAffected = stmt.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("Patient updated successfully.");
        } else {
            System.out.println("Failed to update patient.");
        }
    }


    static void viewPatient() throws SQLException {
        int id = -1;
        while (true) {
            System.out.print("Enter patient ID: ");
            try {
                id = Integer.parseInt(sc.nextLine()); // read as string and parse
                if (id <= 0) {
                    System.out.println("Patient ID must be greater than zero. Please try again.");
                    continue;
                }
                break; // valid positive integer, exit loop
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Patient ID must be an integer. Please try again.");
            }
        }

        String sql = "SELECT * FROM patients WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            String name = rs.getString("name");
            String contact = rs.getString("contact");
            String history = rs.getString("history"); // column in DB

            System.out.println("\n--- Patient Details ---");
            System.out.println("ID: " + id);
            System.out.println("Name: " + name);
            System.out.println("Contact: " + contact);
            System.out.println("History: " + (history != null && !history.isEmpty() ? history : "No history available"));
        } else {
            System.out.println("Patient ID " + id + " not found.");
        }
    }


    static void viewPatientHistory() throws SQLException {
        System.out.print("Enter patient ID: ");
        int id = -1;

        try {
            id = sc.nextInt();
            sc.nextLine();
        } catch (Exception e) {
            System.out.println("Invalid input! Patient ID must be a number.");
            sc.nextLine();
        }

        String sql = "SELECT history FROM patients WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) System.out.println("History: " + rs.getString("history"));
        else System.out.println("Not found.");
    }

    static void searchPatient() throws SQLException {
        // Name input with validation (only characters allowed, no digits)
        String name;
        while (true) {
            System.out.print("Enter name (characters only, no numbers): ");
            name = sc.nextLine().trim();

            if (name.isEmpty()) {
                System.out.println("Name cannot be empty. Please enter a valid name.");
                continue;
            }

            // Check if name contains any digits using core Java
            boolean hasDigit = false;
            for (int i = 0; i < name.length(); i++) {
                char ch = name.charAt(i);
                if (Character.isDigit(ch)) {
                    hasDigit = true;
                    break;
                }
            }

            if (hasDigit) {
                System.out.println("Name should contain only characters. Numbers are not allowed.");
                continue;
            }

            // Check if name contains at least one letter using core Java
            boolean hasLetter = false;
            for (int i = 0; i < name.length(); i++) {
                char ch = name.charAt(i);
                if (Character.isLetter(ch)) {
                    hasLetter = true;
                    break;
                }
            }

            if (!hasLetter) {
                System.out.println("Please enter a valid name with letters.");
                continue;
            }

            break;
        }

        String sql = "SELECT * FROM patients WHERE name LIKE ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, "%" + name + "%");
        ResultSet rs = stmt.executeQuery();

        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("ID: " + rs.getInt("id") +
                    ", Name: " + rs.getString("name") +
                    ", Contact: " + rs.getString("contact") +
                    ", Address: " + rs.getString("address") +
                    ", History: " + rs.getString("history"));
        }

        if (!found) {
            System.out.println(" Patient not found.");
        }
    }

    static void makeAppointment() throws SQLException {
        int pid = -1;
        boolean validPatient = false;

        // Patient ID validation with try-catch (max 3 attempts)
        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.print("Enter patient ID: ");
            try {
                pid = sc.nextInt();
                sc.nextLine();

                if (isPatientExists(pid)) {
                    validPatient = true;
                    break; // patient found
                } else {
                    System.out.println("Patient not found. Attempt " + attempt + " of 3.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter numbers only. Attempt " + attempt + " of 3.");
                sc.nextLine(); // clear invalid input
            }
        }

        if (!validPatient) {
            System.out.println("Too many invalid attempts. Appointment booking cancelled.");
            return; // stop method
        }

        //  Display available doctors
        System.out.println("--- Available Doctors ---");
        PreparedStatement docStmt = conn.prepareStatement("SELECT * FROM doctor LIMIT 10");
        ResultSet docs = docStmt.executeQuery();
        while (docs.next()) {
            System.out.println("ID: " + docs.getInt("id") +
                    ", Name: " + docs.getString("name") +
                    ", Specialization: " + docs.getString("specialization") +
                    ", Charge: ₹" + docs.getDouble("treatment_charge"));
        }

        // Doctor ID validation with try-catch (max 3 attempts)
        int did = -1;
        boolean validDoctor = false;

        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.print("Enter Doctor ID: ");
            try {
                did = sc.nextInt();
                sc.nextLine();

                PreparedStatement checkDoctor = conn.prepareStatement("SELECT id FROM doctor WHERE id = ?");
                checkDoctor.setInt(1, did);
                ResultSet doctorRS = checkDoctor.executeQuery();
                if (doctorRS.next()) {
                    validDoctor = true;
                    break; // doctor found
                } else {
                    System.out.println("Doctor not found. Attempt " + attempt + " of 3.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter numbers only. Attempt " + attempt + " of 3.");
                sc.nextLine(); // clear invalid input
            }
        }

        if (!validDoctor) {
            System.out.println("Too many invalid attempts. Appointment booking cancelled.");
            return;
        }


        // Check if doctor has reached daily appointment limit (5 appointments per doctor)
        int todayAppointments = getDoctorAppointmentCount(did);

        if (todayAppointments >= MAX_APPOINTMENTS_PER_DOCTOR) {
            System.out.println("Doctor has reached maximum appointments for today (" + MAX_APPOINTMENTS_PER_DOCTOR + ").");
            System.out.println("You are in waiting list.");

            // Add to waiting list
            String waitingSql = "INSERT INTO waiting_list (patient_id, doctor_id, requested_date, requested_time) VALUES (?, ?, CURDATE(), CURTIME())";
            PreparedStatement waitingStmt = conn.prepareStatement(waitingSql);
            waitingStmt.setInt(1, pid);
            waitingStmt.setInt(2, did);
            waitingStmt.executeUpdate();
            System.out.println("Patient added to waiting list successfully.");
            return;
        }

        // Insert appointment with current date and time (using timestamp)
        String sql = "INSERT INTO APPOINTMENT (Patient_ID, Doctor_ID, Date, Time) VALUES (?, ?, CURDATE(), CURTIME())";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, pid);
        pst.setInt(2, did);
        pst.executeUpdate();
        System.out.println("Appointment booked successfully with current date and time.");
    }

    static boolean isPatientExists(int pid) throws SQLException {
        String sql = "SELECT id FROM patients WHERE id = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, pid);
        ResultSet rs = pst.executeQuery();
        return rs.next();
    }


    static int getDoctorAppointmentCount(int doctorId) {
        try {
            PreparedStatement pst = conn.prepareStatement("SELECT COUNT(*) FROM APPOINTMENT WHERE Doctor_ID = ? AND Date = CURDATE()");
            pst.setInt(1, doctorId);
            ResultSet rs = pst.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    static int getDoctorAppointmentCount(int doctorId, String date) {
        try {
            PreparedStatement pst = conn.prepareStatement("SELECT COUNT(*) FROM APPOINTMENT WHERE Doctor_ID = ? AND Date = ?");
            pst.setInt(1, doctorId);
            pst.setString(2, date);
            ResultSet rs = pst.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    static void viewAllAppointments() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM APPOINTMENT");
        System.out.println("\n=== ALL APPOINTMENTS ===");

        boolean hasAppointments = false;
        while (rs.next()) {
            hasAppointments = true;
            System.out.println("Appointment ID: " + rs.getInt("Appointment_ID") +
                    ", Patient ID: " + rs.getInt("Patient_ID") +
                    ", Doctor ID: " + rs.getInt("Doctor_ID") +
                    ", Created At: " + rs.getTimestamp("created_at"));
        }

        if (!hasAppointments) {
            System.out.println("No appointments found.");
        }
    }

    static void cancelAppointment() throws SQLException {
        System.out.print("Enter Appointment ID: ");

        int appId;
        try {
            appId = sc.nextInt();
            sc.nextLine();
            if (appId <= 0) {
                System.out.println("Appointment ID must be greater than zero.");
                return;
            }
        } catch (InputMismatchException e) {
            System.out.println(" Invalid input. Appointment ID must be an integer.");
            sc.nextLine(); // clear bad input
            return;
        }

        // Check if appointment exists and get doctor
        String doctorQuery = "SELECT doctor_id FROM appointment WHERE appointment_id = ?";
        PreparedStatement doctorStmt = conn.prepareStatement(doctorQuery);
        doctorStmt.setInt(1, appId);
        ResultSet doctorRs = doctorStmt.executeQuery();

        if (!doctorRs.next()) {
            System.out.println("Appointment ID " + appId + " not found.");
            return;
        }
        int doctorId = doctorRs.getInt("doctor_id");

        //  Cancel appointment
        String deleteSql = "DELETE FROM appointment WHERE appointment_id = ?";
        PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
        deleteStmt.setInt(1, appId);
        int rows = deleteStmt.executeUpdate();

        if (rows == 0) {
            System.out.println(" Failed to cancel appointment. Try again.");
            return;
        }

        System.out.println("✔ Appointment canceled.");

        //  Process waiting list
        System.out.println("Checking waiting list for doctor " + doctorId + "...");

        String waitQuery = "SELECT id, patient_id FROM waiting_list WHERE doctor_id = ? ORDER BY added_at LIMIT 1";
        PreparedStatement waitStmt = conn.prepareStatement(waitQuery);
        waitStmt.setInt(1, doctorId);
        ResultSet waitRs = waitStmt.executeQuery();

        if (!waitRs.next()) {
            System.out.println("ℹ No patients in waiting list for this doctor.");
            return;
        }

        int waitId = waitRs.getInt("id");
        int nextPatientId = waitRs.getInt("patient_id");

        // Double check patient still exists
        String patientCheck = "SELECT id, name FROM patients WHERE id = ?";
        PreparedStatement patientStmt = conn.prepareStatement(patientCheck);
        patientStmt.setInt(1, nextPatientId);
        ResultSet patientRs = patientStmt.executeQuery();

        if (!patientRs.next()) {
            System.out.println(" Patient " + nextPatientId + " not found. Removing from waiting list.");
            String deleteInvalidWait = "DELETE FROM waiting_list WHERE id = ?";
            PreparedStatement delInvalidWait = conn.prepareStatement(deleteInvalidWait);
            delInvalidWait.setInt(1, waitId);
            delInvalidWait.executeUpdate();
            return;
        }
        String patientName = patientRs.getString("name");

        // Insert new appointment
        String insertApp = "INSERT INTO appointment(patient_id, doctor_id, Date, Time, status, created_at) " +
                "VALUES (?, ?, CURDATE(), CURTIME(), 'scheduled', NOW())";
        PreparedStatement insertStmt = conn.prepareStatement(insertApp);
        insertStmt.setInt(1, nextPatientId);
        insertStmt.setInt(2, doctorId);
        insertStmt.executeUpdate();


        // Remove from waiting list
        String deleteWait = "DELETE FROM waiting_list WHERE id = ?";
        PreparedStatement delWaitStmt = conn.prepareStatement(deleteWait);
        delWaitStmt.setInt(1, waitId);
        delWaitStmt.executeUpdate();

        System.out.println(" Patient " + nextPatientId + " (" + patientName + ") moved from waiting list to confirmed appointment!");
    }


    // ----------------- Doctor Management ----------------
    static void doctorManagementMenu() {
        while (true) {
            System.out.println("\n--- Doctor Management ---");
            System.out.println("1. Add Doctor\n2. Remove Doctor\n3. Update Doctor\n4. View Availability\n5. Back");
            System.out.print("Choice: ");
            int ch = sc.nextInt();
            sc.nextLine();
            switch (ch) {
                case 1 : addDoctor();break;
                case 2 : removeDoctor();break;
                case 3 : updateDoctor();break;
                case 4 : viewDoctorAvailability();break;
                case 5 : {
                    return;
                }
                default : System.out.println("Invalid.");break;
            }
        }
    }

    static void addDoctor() {
        try {
            // Validate Name
            String name;
            while (true) {
                System.out.print("Name: ");
                name = sc.nextLine();
                boolean valid = true;
                for (int i = 0; i < name.length(); i++) {
                    char ch = name.charAt(i);
                    if (!(Character.isLetter(ch) || Character.isSpaceChar(ch))) {
                        valid = false;
                        break;
                    }
                }
                if (valid && name.trim().length() > 0) {
                    break;
                } else {
                    System.out.println(" Invalid name. Only alphabets and spaces allowed.");
                }
            }

            // Validate Specialization
            String spec;
            while (true) {
                System.out.print("Specialization: ");
                spec = sc.nextLine();
                boolean valid = true;
                for (int i = 0; i < spec.length(); i++) {
                    char ch = spec.charAt(i);
                    if (!(Character.isLetter(ch) || Character.isSpaceChar(ch))) {
                        valid = false;
                        break;
                    }
                }
                if (valid && spec.trim().length() > 0) {
                    break;
                } else {
                    System.out.println(" Invalid specialization. Only alphabets and spaces allowed.");
                }
            }

            // Treatment charge
            double charge;
            while (true) {
                try {
                    System.out.print("Treatment Charge: ");
                    charge = sc.nextDouble();
                    sc.nextLine();
                    if (charge >= 0) break;
                    else System.out.println(" Charge cannot be negative.");
                } catch (InputMismatchException e) {
                    System.out.println(" Invalid input. Enter a valid number.");
                    sc.nextLine();
                }
            }

            // Slot input with validation
            List<String> slotList = new ArrayList<>();
            while (true) {
                try {
                    System.out.println("Enter slots (comma separated). Example: 09:00-10:00, 10:00-11:00");
                    String[] slots = sc.nextLine().split(",");
                    slotList.clear();

                    boolean allValid = true;
                    for (String slot : slots) {
                        slot = slot.trim();
                        // Simple format validation: must contain "-" and length >= 5
                        if (!slot.matches("\\d{2}:\\d{2}-\\d{2}:\\d{2}")) {
                            allValid = false;
                            break;
                        }
                        slotList.add(slot);
                    }

                    if (allValid && !slotList.isEmpty()) {
                        break; // exit loop only if all slots valid
                    } else {
                        System.out.println(" Invalid slot format. Use HH:MM-HH:MM format.");
                    }

                } catch (Exception e) {
                    System.out.println(" Error in slot input. Try again.");
                }
            }

            // Insert into Doctor table
            PreparedStatement ps1 = conn.prepareStatement(
                    "INSERT INTO doctor (name, specialization, treatment_charge) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps1.setString(1, name);
            ps1.setString(2, spec);
            ps1.setDouble(3, charge);
            ps1.executeUpdate();

            // Get generated ID
            ResultSet generatedKeys = ps1.getGeneratedKeys();
            int generatedId = 0;
            if (generatedKeys.next()) {
                generatedId = generatedKeys.getInt(1);
                System.out.println("Doctor added successfully with ID: " + generatedId);
            }
// Insert into DoctorSlots table using the generated ID
            PreparedStatement ps2 = conn.prepareStatement(
                    "INSERT INTO DoctorSlots (doctor_id, slot) VALUES (?, ?)"
            );
            for (String slot : slotList) {
                ps2.setInt(1, generatedId);
                ps2.setString(2, slot);
                ps2.addBatch();
            }
            ps2.executeBatch();


            doctors.put(generatedId, new Doctor(generatedId, name, spec, charge, slotList));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    static void removeDoctor() {
        try {
            System.out.print("Enter ID: ");
            int id = sc.nextInt();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM doctor WHERE id = ?");
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                doctors.remove(id);
                System.out.println("Removed.");
            } else System.out.println("Not found.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void updateDoctor() {
        try {
            int id = -1;
            boolean found = false;
            int attempts = 0;

            //  Step 1: Check doctor ID up to 3 times
            while (attempts < 3) {
                System.out.print("Enter Doctor ID: ");

                try {
                    id = sc.nextInt();
                    sc.nextLine(); // consume newline
                } catch (Exception e) {
                    System.out.println("Invalid input! Doctor ID must be a number.");
                    sc.nextLine(); // clear invalid input
                }


                PreparedStatement check = conn.prepareStatement("SELECT * FROM doctor WHERE id=?");
                check.setInt(1, id);
                ResultSet rs1 = check.executeQuery();
                if (rs1.next()) {
                    found = true;
                    break;
                } else {
                    System.out.println("Doctor ID not found. Try again.");
                    attempts++;
                }
            }

            if (!found) {
                System.out.println("Too many invalid attempts. Update cancelled.");
                return;
            }

            //  Step 2: Get current doctor from HashMap / DB
            Doctor doc = doctors.get(id);
            if (doc == null) {
                PreparedStatement getDoc = conn.prepareStatement("SELECT * FROM doctor WHERE id=?");
                getDoc.setInt(1, id);
                ResultSet rs2 = getDoc.executeQuery();
                if (rs2.next()) {
                    doc = new Doctor(
                            rs2.getInt("id"),
                            rs2.getString("name"),
                            rs2.getString("specialization"),
                            rs2.getDouble("treatment_charge"),
                            new ArrayList<>()
                    );
                    doctors.put(id, doc);
                }
            }

            //  Step 3: Name validation
            System.out.print("New name (blank to skip): ");
            String name = sc.nextLine();
            if (!name.isEmpty()) {
                boolean valid = name.chars().allMatch(c -> Character.isLetter(c) || c == ' ');
                if (valid) doc.name = name;
                else System.out.println("Invalid name! Only letters allowed. Name unchanged.");
            }

            //  Step 4: Specialization validation
            System.out.print("New specialization (blank to skip): ");
            String spec = sc.nextLine();
            if (!spec.isEmpty()) {
                boolean valid = spec.chars().allMatch(c -> Character.isLetter(c) || c == ' ');
                if (valid) doc.specialization = spec;
                else System.out.println("Invalid specialization! Only letters allowed. Specialization unchanged.");
            }

            //  Step 5: Treatment charge validation
            System.out.print("New treatment charge (blank to skip): ");
            String chargeInput = sc.nextLine();
            if (!chargeInput.isEmpty()) {
                try {
                    double newCharge = Double.parseDouble(chargeInput);
                    if (newCharge > 0) {
                        doc.treatmentCharge = newCharge;
                    } else {
                        System.out.println("Charge must be positive. Charge unchanged.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid charge! Must be a number. Charge unchanged.");
                }
            }


            // Ask until valid input (yes or no)
            String choice;
            while (true) {
                System.out.print("Update slots? (yes/no): ");
                choice = sc.nextLine().trim().toLowerCase();
                if (choice.equals("yes") || choice.equals("no")) {
                    break;
                } else {
                    System.out.println("Invalid input! Please enter 'yes' or 'no'.");
                }
            }

            if (choice.equals("yes")) {
                while (true) { // keep asking until valid slots
                    System.out.println("Example format: 09:00-10:00, 10:30-11:30");
                    System.out.print("New slots (comma separated): ");
                    String[] slots = sc.nextLine().split(",");
                    doc.availableSlots.clear();

                    boolean allValid = true;
                    for (String s : slots) {
                        String slot = s.trim();

                        // validate slot manually
                        String[] parts = slot.split("-");
                        if (parts.length != 2) {
                            allValid = false;
                            break;
                        }

                        if (!isValidTime(parts[0]) || !isValidTime(parts[1])) {
                            allValid = false;
                            break;
                        }

                        doc.availableSlots.add(slot);
                    }

                    if (!allValid || doc.availableSlots.isEmpty()) {
                        System.out.println("Invalid slot format! Try again.\n");
                    } else {
                        // Slots are valid → update DB
                        PreparedStatement del = conn.prepareStatement("DELETE FROM DoctorSlots WHERE doctor_id=?");
                        del.setInt(1, id);
                        del.executeUpdate();

                        PreparedStatement ins = conn.prepareStatement(
                                "INSERT INTO DoctorSlots (doctor_id, slot) VALUES (?, ?)"
                        );
                        for (String s : doc.availableSlots) {
                            ins.setInt(1, id);
                            ins.setString(2, s);
                            ins.addBatch();
                        }
                        ins.executeBatch();

                        System.out.println(" Slots updated successfully.");
                        break; // exit the while loop
                    }
                }
            }

            //  Step 7: Update Doctor table
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE doctor SET name=?, specialization=?, treatment_charge=? WHERE id=?"
            );
            ps.setString(1, doc.name);
            ps.setString(2, doc.specialization);
            ps.setDouble(3, doc.treatmentCharge);
            ps.setInt(4, id);
            ps.executeUpdate();

            System.out.println("Doctor updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //  Helper method to check time format (HH:MM)
    static boolean isValidTime(String time) {
        try {
            String[] parts = time.split(":");
            if (parts.length != 2) return false;
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59);
        } catch (NumberFormatException e) {
            return false;
        }
    }


    static void viewDoctorAvailability() {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM doctor");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String specialization = rs.getString("specialization");
                double charge = rs.getDouble("treatment_charge");

                // Fetch slots
                PreparedStatement slotStmt = conn.prepareStatement("SELECT slot FROM DoctorSlots WHERE doctor_id = ?");
                slotStmt.setInt(1, id);
                ResultSet slotRS = slotStmt.executeQuery();
                List<String> slots = new ArrayList<>();
                while (slotRS.next()) {
                    slots.add(slotRS.getString("slot"));
                }

                // Display doctor info
                System.out.println("ID: " + id + ", Name: " + name + ", Specialization: " + specialization + ", Charge: ₹" + charge);
                System.out.println("Available Slots: " + (slots.isEmpty() ? "None" : String.join(", ", slots)));
                System.out.println("--------------------------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    //--------Bill method------
    static void generateBill() throws SQLException {
        System.out.print("Enter Patient ID: ");
        int patientId = -1;

        try {
            patientId = sc.nextInt();
            sc.nextLine(); // consume newline
        } catch (Exception e) {
            System.out.println("Invalid input! Patient ID must be a number.");
            sc.nextLine(); // clear invalid input
        }


        //  Check if patient exists
        String checkQuery = "SELECT id, name FROM patients WHERE id = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        checkStmt.setInt(1, patientId);
        ResultSet patientRs = checkStmt.executeQuery();

        if (!patientRs.next()) {
            System.out.println(" Patient not found.");
            return;
        }
        String patientName = patientRs.getString("name");

        //  Billing options
        System.out.println("\nBilling Options:");
        System.out.println("1. Auto-calculate from recent appointment");
        System.out.println("2. Manual amount entry");
        System.out.print("Choose option (1-2): ");
        int billingOption = sc.nextInt();
        sc.nextLine();

        double billAmount = 0;
        int billId = -1;
        String description = "";
        String status = "pending";

        if (billingOption == 1) {
            //  Call stored procedure (AutoGenerateBill)
            CallableStatement cs = conn.prepareCall("{CALL AutoGenerateBill(?, ?, ?)}");
            cs.setInt(1, patientId);
            cs.registerOutParameter(2, Types.INTEGER); // bill_id
            cs.registerOutParameter(3, Types.DOUBLE);  // amount
            cs.execute();

            billId = cs.getInt(2);
            billAmount = cs.getDouble(3);

            if (billId == -1) {
                System.out.println(" No recent appointments found. Switching to manual entry.");
                billingOption = 2;
            } else {
                System.out.println(" Bill generated automatically (₹" + billAmount + ")");
            }
        }

        if (billingOption == 2) {
            // Manual bill entry
            while (true) {
                System.out.print("Enter bill amount (₹): ");
                try {
                    billAmount = sc.nextDouble();
                    sc.nextLine();
                    if (billAmount > 0) {
                        break;
                    } else {
                        System.out.println("Amount must be positive!");
                    }
                } catch (InputMismatchException e) {
                    System.out.println(" Invalid amount! Enter a number.");
                    sc.nextLine();
                }
            }

            // Description validation (optional)
            while (true) {
                System.out.print("Enter description (optional, text only): ");
                description = sc.nextLine();
                if (description.isEmpty()) break;

                boolean isValid = true;
                for (int i = 0; i < description.length(); i++) {
                    char c = description.charAt(i);
                    if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
                            c == ' ' || c == '.' || c == ',' || c == '!' ||
                            c == '?' || c == '\'' || c == '-')) {
                        isValid = false;
                        break;
                    }
                }
                if (isValid) break;
                else System.out.println("Invalid description! Only letters, spaces, and punctuation allowed.");
            }

            // Bill status
            System.out.println("\nSelect bill status:");
            System.out.println("1. Pending");
            System.out.println("2. Paid");
            System.out.println("3. Cancelled");
            System.out.print("Enter choice (1-3): ");
            int statusChoice = -1;
            try {
                statusChoice = sc.nextInt();
                sc.nextLine(); // consume newline
            } catch (Exception e) {
                System.out.println("Invalid input! Please enter a number.");
                sc.nextLine(); // clear invalid input
            }


            status = switch (statusChoice) {
                case 2 -> "paid";
                case 3 -> "cancelled";
                default -> "pending";
            };

            // Insert manual bill
            String insertBillSql = "INSERT INTO bills (patient_id, amount, bill_date, description, status) " +
                    "VALUES (?, ?, CURDATE(), ?, ?)";
            PreparedStatement billStmt = conn.prepareStatement(insertBillSql, Statement.RETURN_GENERATED_KEYS);
            billStmt.setInt(1, patientId);
            billStmt.setDouble(2, billAmount);
            billStmt.setString(3, description);
            billStmt.setString(4, status);
            billStmt.executeUpdate();

            ResultSet generatedKeys = billStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                billId = generatedKeys.getInt(1);
            }
        }

        //Show formatted summary
        if (billId != -1) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("           BILL GENERATED SUCCESSFULLY");
            System.out.println("=".repeat(50));
            System.out.println("Bill ID      : " + billId);
            System.out.println("Patient ID   : " + patientId);
            System.out.println("Patient Name : " + patientName);
            System.out.println("Amount       : ₹" + String.format("%.2f", billAmount));

            // Fetch date + time separately
            PreparedStatement tsStmt = conn.prepareStatement(
                    "SELECT bill_date, created_at, status, description FROM bills WHERE bill_id=?"
            );
            tsStmt.setInt(1, billId);
            ResultSet tsRs = tsStmt.executeQuery();
            if (tsRs.next()) {
                java.sql.Date billDate = tsRs.getDate("bill_date");
                java.sql.Timestamp createdAt = tsRs.getTimestamp("created_at");

                System.out.println("Date         : " + billDate.toString());
                System.out.println("Time         : " + createdAt.toLocalDateTime().toLocalTime());
                System.out.println("Status       : " + tsRs.getString("status").toUpperCase());

                String desc = tsRs.getString("description");
                if (desc != null && !desc.isEmpty()) {
                    System.out.println("Description  : " + desc);
                }
            }
            System.out.println("=".repeat(50));
        } else {
            System.out.println("Bill generation failed.");
        }
    }


    static void viewBill() throws SQLException {
        int patientId;
        while (true) {
            System.out.print("Enter Patient ID to view bills: ");
            String input = sc.nextLine();
            try {
                patientId = Integer.parseInt(input);
                break; // exit loop if valid
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Patient ID must be an integer.");
            }
        }

        // Get patient name
        String patientNameSql = "SELECT name FROM patients WHERE id = ?";
        PreparedStatement nameStmt = conn.prepareStatement(patientNameSql);
        nameStmt.setInt(1, patientId);
        ResultSet nameRs = nameStmt.executeQuery();

        String patientName;
        if (nameRs.next()) {
            patientName = nameRs.getString("name");
        } else {
            System.out.println("Patient ID not found!");
            return; // exit method if patient doesn't exist
        }

        // Fetch bills
        String sql = "SELECT * FROM bills WHERE patient_id = ? ORDER BY bill_date DESC";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, patientId);
        ResultSet rs = stmt.executeQuery();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("                BILLING HISTORY");
        System.out.println("Patient: " + patientName + " (ID: " + patientId + ")");
        System.out.println("=".repeat(60));

        boolean found = false;
        double totalAmount = 0;
        int pendingCount = 0, paidCount = 0, cancelledCount = 0;

        while (rs.next()) {
            found = true;
            double amount = rs.getDouble("amount");
            String status = rs.getString("status");
            totalAmount += amount;

            // Count by status
            switch (status.toLowerCase()) {
                case "pending":
                    pendingCount++;
                    break;
                case "paid":
                    paidCount++;
                    break;
                case "cancelled":
                    cancelledCount++;
                    break;
            }


            System.out.printf("Bill ID: %-8d | Date: %-12s | Amount: ₹%-10.2f | Status: %-10s%n",
                    rs.getInt("bill_id"),
                    rs.getString("bill_date"),
                    amount,
                    status.toUpperCase());

            if (rs.getString("description") != null && !rs.getString("description").isEmpty()) {
                System.out.println("Description: " + rs.getString("description"));
            }
            System.out.println("-".repeat(60));
        }

        if (found) {
            System.out.println("SUMMARY:");
            System.out.println("Total Bills: " + (pendingCount + paidCount + cancelledCount));
            System.out.println("Pending: " + pendingCount + " | Paid: " + paidCount + " | Cancelled: " + cancelledCount);
            System.out.println("Total Amount: ₹" + String.format("%.2f", totalAmount));
            System.out.println("=".repeat(60));
        } else {
            System.out.println("No bills found for Patient: " + patientName);
            System.out.println("=".repeat(60));
        }
    }

    static void updateBillStatus() throws SQLException {
        int billId;
        while (true) {
            System.out.print("Enter Bill ID to update: ");
            try {
                billId = Integer.parseInt(sc.nextLine());
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Bill ID must be an integer.");
            }
        }

        String checkSql = "SELECT patient_id, amount, status FROM bills WHERE bill_id = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setInt(1, billId);
        ResultSet rs = checkStmt.executeQuery();

        if (!rs.next()) {
            System.out.println("Bill ID not found.");
            return;
        }

        String currentStatus = rs.getString("status");
        System.out.println("Current Status: " + currentStatus.toUpperCase());

        int statusChoice;
        while (true) {
            System.out.println("Select new status:");
            System.out.println("1. Pending");
            System.out.println("2. Paid");
            System.out.println("3. Cancelled");
            System.out.print("Enter choice: ");

            try {
                statusChoice = Integer.parseInt(sc.nextLine());
                if (statusChoice >= 1 && statusChoice <= 3) break;
                else System.out.println("Invalid choice! Enter 1, 2,3");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Enter an integer (1-3).");
            }
        }

        String newStatus;
        switch (statusChoice) {
            case 1:
                newStatus = "pending";
                break;
            case 2:
                newStatus = "paid";
                break;
            case 3:
                newStatus = "cancelled";
                break;
            default:
                newStatus = currentStatus;
        }


        if (!newStatus.equals(currentStatus)) {
            String updateSql = "UPDATE bills SET status = ? WHERE bill_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, newStatus);
            updateStmt.setInt(2, billId);
            int rows = updateStmt.executeUpdate();

            if (rows > 0) System.out.println("Bill status updated successfully to " + newStatus.toUpperCase());
            else System.out.println("Failed to update bill status.");
        } else {
            System.out.println("Bill status remains unchanged.");
        }
    }

}
