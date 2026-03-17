package HMS_room;

import HMS_db.DatabaseConnection;

import java.sql.*;
import java.util.*;


public class RoomManagement {
    static Scanner sc = new Scanner(System.in);
    static DatabaseConnection DatabaseConnection;
    static Connection conn = DatabaseConnection.getConnection();


    // ---------- Linked List ----------
    static class RoomList {
        static class Node {
            int roomNumber;
            String type;
            double charge;
            boolean available;
            Node next;

            Node(int roomNumber, String type, double charge, boolean available) {
                this.roomNumber = roomNumber;
                this.type = type;
                this.charge = charge;
                this.available = available;
            }
        }

        Node head = null;

        public void insertLast(int roomNumber, String type, double charge, boolean available) {
            Node newNode = new Node(roomNumber, type, charge, available);
            if (head == null) {
                head = newNode;
                return;
            }
            Node temp = head;
            while (temp.next != null)
                temp = temp.next;
            temp.next = newNode;
        }

        public boolean search(int roomNumber) {
            Node temp = head;
            while (temp != null) {
                if (temp.roomNumber == roomNumber) return true;
                temp = temp.next;
            }
            return false;
        }

        public Node allocateRoom() {
            Node temp = head;
            while (temp != null) {
                if (temp.available) {
                    temp.available = false;
                    return temp;
                }
                temp = temp.next;
            }
            return null;
        }

        public void releaseRoom(int roomNumber) {
            Node temp = head;
            while (temp != null) {
                if (temp.roomNumber == roomNumber) {
                    temp.available = true;
                    return;
                }
                temp = temp.next;
            }
        }

        public void deleteRoom(int roomNumber) {
            if (head == null) return;
            if (head.roomNumber == roomNumber) {
                head = head.next;
                return;
            }
            Node temp = head;
            while (temp.next != null) {
                if (temp.next.roomNumber == roomNumber) {
                    temp.next = temp.next.next;
                    return;
                }
                temp = temp.next;
            }
        }

        public void displayRooms() {
            try {
                String sql = "SELECT * FROM rooms";
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                System.out.println("---- Room List ----");
                while (rs.next()) {
                    System.out.println("Room: " + rs.getInt("room_id") +
                            ", Type: " + rs.getString("type") +
                            ", Charge: ₹" + rs.getDouble("charge") +
                            ", Available: " + rs.getBoolean("available") +
                            ", Patient ID: " + rs.getString("patient_id"));
                }
            } catch (Exception e) {
                System.out.println("Error displaying rooms: " + e.getMessage());
            }
        }

    }

    static RoomList availableRooms = new RoomList();

    // ---------- DB + Linked List Sync ----------
    static void loadRoomsFromDB() {
        try {
            String sql = "SELECT * FROM rooms";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                availableRooms.insertLast(
                        rs.getInt("room_id"),
                        rs.getString("type"),
                        rs.getDouble("charge"),
                        rs.getBoolean("available")
                );
            }
        } catch (Exception e) {
            System.out.println("Error loading rooms: " + e.getMessage());
        }
    }

    public static void addRoom() {
        int num;
        double charge = 0.0;
        String type = "";

        // Room number validation
        while (true) {
            System.out.print("Enter Room Number: ");
            try {
                num = sc.nextInt();
                sc.nextLine();
                if (availableRooms.search(num)) {
                    System.out.println(" Room already exists!");
                    continue;
                }
                break;
            } catch (InputMismatchException e) {
                System.out.println(" Invalid input.");
                sc.nextLine();
            }
        }

        // Room type
        while (true) {
            System.out.println("Select Room Type: 1.General 2.Private 3.ICU 4.Deluxe");
            int ch = sc.nextInt();
            sc.nextLine();
            switch (ch) {
                case 1 -> type = "General";
                case 2 -> type = "Private";
                case 3 -> type = "ICU";
                case 4 -> type = "Deluxe";
                default -> {
                    System.out.println("Invalid choice");
                    continue;
                }
            }
            break;
        }

        // Charge
        while (true) {
            System.out.print("Enter Room Charge: ");
            try {
                charge = sc.nextDouble();
                sc.nextLine();
                if (charge <= 0) {
                    System.out.println(" Must be >0");
                    continue;
                }
                break;
            } catch (Exception e) {
                System.out.println(" Invalid input.");
                sc.nextLine();
            }
        }

        // Add to Linked List
        availableRooms.insertLast(num, type, charge, true);

        // Insert into DB
        try {
            String sql = "INSERT INTO rooms (room_id, type, charge, available) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, num);
            ps.setString(2, type);
            ps.setDouble(3, charge);
            ps.setBoolean(4, true);
            ps.executeUpdate();
            System.out.println(" Room added to DB.");
        } catch (Exception e) {
            System.out.println(" DB Insert Failed: " + e.getMessage());
        }
    }

    static void assignRoom() {
        RoomList.Node room = availableRooms.allocateRoom();
        if (room == null) {
            System.out.println(" No rooms available.");
            return;
        }

        // Ask for patient ID
        System.out.print("Enter Patient ID to assign room " + room.roomNumber + ": ");
        int patientId = sc.nextInt();
        sc.nextLine();

        try {
            //   Check if patient exists
            String checkSql = "SELECT id FROM patients WHERE id=?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setInt(1, patientId);
            ResultSet rs = checkPs.executeQuery();

            if (!rs.next()) {
                System.out.println(" Patient ID not found in database. Cannot assign room.");
                return;
            }

            // Assign room in DB
            String sql = "UPDATE rooms SET available=?, patient_id=? WHERE room_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setBoolean(1, false);
            ps.setInt(2, patientId);
            ps.setInt(3, room.roomNumber);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println(" Assigned Room " + room.roomNumber + " to Patient " + patientId);
            } else {
                System.out.println(" Room update failed in DB.");
            }

        } catch (Exception e) {
            System.out.println(" DB update error: " + e.getMessage());
        }
    }

    static void releaseRoom() {
        System.out.print("Enter room number to release: ");
        int r = sc.nextInt();
        sc.nextLine();
        availableRooms.releaseRoom(r);

        try {
            String sql = "UPDATE rooms SET available=?, patient_id=NULL WHERE room_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setBoolean(1, true);
            ps.setInt(2, r);
            ps.executeUpdate();
            System.out.println("Room released and Patient ID cleared in DB.");
        } catch (Exception e) {
            System.out.println("DB update error: " + e.getMessage());
        }
    }

    static void removeRoom() {
        System.out.print("Enter room number to delete: ");
        int r = sc.nextInt();
        sc.nextLine();

        availableRooms.deleteRoom(r);

        try {
            String sql = "DELETE FROM rooms WHERE room_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, r);
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("Room deleted from DB.");
            else System.out.println("Room not found in DB.");
        } catch (Exception e) {
            System.out.println("DB delete error: " + e.getMessage());
        }
    }

    static void showAllRooms() {
        availableRooms.displayRooms();
    }

    // ---------- Main Menu ----------
    public static void roomMenu() {
        loadRoomsFromDB(); // Load existing DB data into LL

        while (true) {
            System.out.println("\n=== ROOM MANAGEMENT ===");
            System.out.println("1. Add Room");
            System.out.println("2. Assign Room");
            System.out.println("3. Release Room");
            System.out.println("4. Show All Rooms");
            System.out.println("5. Delete Room");
            System.out.println("6. Exit");
            System.out.print("Choice: ");
            int ch = sc.nextInt();

            switch (ch) {
                case 1 -> addRoom();
                case 2 -> assignRoom();
                case 3 -> releaseRoom();
                case 4 -> showAllRooms();
                case 5 -> removeRoom();
                case 6 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}