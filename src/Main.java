//import java.util.InputMismatchException;
//
////---------------Room management---------------
//static class RoomList {
//    static class Node {
//        int roomNumber;
//        String type; // e.g., "General", "Private", etc.
//        double charge;
//        boolean available;
//        Node next;
//
//        Node(int roomNumber, String type, double charge) {
//            this.roomNumber = roomNumber;
//            this.type = type;
//            this.charge = charge;
//            this.available = true;
//        }
//    }
//
//    private Node head = null;
//
//    public void insertFirst(int roomNumber, String type, double charge) {
//        Node newNode = new Node(roomNumber, type, charge);
//        newNode.next = head;
//        head = newNode;
//    }
//
//    public void insertLast(int roomNumber, String type, double charge) {
//        Node newNode = new Node(roomNumber, type, charge);
//        if (head == null) {
//            head = newNode;
//            return;
//        }
//        Node temp = head;
//        while (temp.next != null)
//            temp = temp.next;
//        temp.next = newNode;
//    }
//    public boolean search(int roomNumber) {
//        Node temp = head;
//        while (temp != null) {
//            if (temp.roomNumber == roomNumber) {
//                return true; // found
//            }
//            temp = temp.next;
//        }
//        return false; // not found
//    }
//
//    public Node allocateRoom() {
//        Node temp = head;
//        while (temp != null) {
//            if (temp.available) {
//                temp.available = false;
//                return temp;
//            }
//            temp = temp.next;
//        }
//        return null;
//    }
//
//    public void releaseRoom(int roomNumber) {
//        Node temp = head;
//        while (temp != null) {
//            if (temp.roomNumber == roomNumber) {
//                temp.available = true;
//                return;
//            }
//            temp = temp.next;
//        }
//    }
//
//    public void displayRooms() {
//        Node temp = head;
//        System.out.println("---- Room List ----");
//        while (temp != null) {
//            System.out.println("Room: " + temp.roomNumber + ", Type: " + temp.type + ", Charge: ₹" + temp.charge + ", Available: " + temp.available);
//            temp = temp.next;
//        }
//    }
//
//    public boolean isEmpty() {
//        return head == null;
//    }
//}
//        static RoomList availableRooms = new RoomList();
//
//        static void addRoom() {
//            int num;
//            double charge = 0.0;
//            String type = "";
//
//            //  Room Number validation (unique + integer only)
//            while (true) {
//                System.out.print("Enter Room Number: ");
//                try {
//                    num = sc.nextInt();
//                    sc.nextLine(); // consume newline
//
//                    if (availableRooms.search(num)) {
//                        System.out.println(" Room number already exists! Enter a unique room number.");
//                        continue;
//                    }
//                    break; // valid
//                } catch (InputMismatchException e) {
//                    System.out.println(" Room number must be an integer.");
//                    sc.nextLine(); // clear invalid input
//                }
//            }
//
//            //  Room Type (choice via switch case)
//            while (true) {
//                System.out.println("Select Room Type:");
//                System.out.println("1. General");
//                System.out.println("2. Private");
//                System.out.println("3. ICU");
//                System.out.println("4. Deluxe");
//                System.out.print("Enter choice (1-4): ");
//
//                int choice;
//                try {
//                    choice = sc.nextInt();
//                    sc.nextLine(); // consume newline
//
//                    switch (choice) {
//                        case 1: type = "General"; break;
//                        case 2: type = "Private"; break;
//                        case 3: type = "ICU"; break;
//                        case 4: type = "Deluxe"; break;
//                        default:
//                            System.out.println(" Invalid choice. Please select between 1-4.");
//                            continue;
//                    }
//                    break; // valid choice
//                } catch (InputMismatchException e) {
//                    System.out.println(" Please enter a valid number (1-4).");
//                    sc.nextLine(); // clear invalid input
//                }
//            }
//
//            //  Room Charge validation (must be numeric and >0)
//            while (true) {
//                System.out.print("Enter Room Charge: ");
//                try {
//                    charge = sc.nextDouble();
//                    sc.nextLine(); // consume newline
//
//                    if (charge <= 0) {
//                        System.out.println(" Room charge must be greater than 0.");
//                        continue;
//                    }
//                    break; // valid
//                } catch (InputMismatchException e) {
//                    System.out.println(" Invalid input. Room charge must be a number.");
//                    sc.nextLine(); // clear invalid input
//                }
//            }
//
//            //  Insert into Linked List
//            availableRooms.insertLast(num, type, charge);
//            System.out.println(" Room added successfully!");
//        }
//
//        static void assignRoom() {
//            RoomList.Node room = availableRooms.allocateRoom();
//            if (room == null) {
//                System.out.println("No rooms available right now.");
//            } else {
//                System.out.println("Assigned Room: " + room.roomNumber + " (" + room.type + "), ₹" + room.charge);
//            }
//        }
//        static void releaseRoom() {
//            System.out.print("Enter room number to release: ");
//            int r = sc.nextInt(); sc.nextLine();
//            availableRooms.releaseRoom(r);
//            System.out.println("Room " + r + " released and now available.");
//        }
//        static void showAllRooms() {
//            availableRooms.displayRooms();
//        }
//}