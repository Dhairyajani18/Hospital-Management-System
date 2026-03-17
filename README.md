======Hospital Management System======

📌 Project Description
The Hospital Management System is a Java-based console application designed to manage hospital operations efficiently. It allows administrators to handle patients, doctors, appointments, billing, and room allocation using JDBC and core Data Structures.

🚀 Features
👨‍⚕️ Admin Functionalities
Patient Management (Add, View, Update, Delete)
Doctor Management (Add, Remove, Update, View)
Appointment Management
Billing System (Inpatient & Outpatient)
Room Management (Assign & Track Rooms)
User Management (Admin/Staff)
Waiting List Management (Queue)
View All Appointments
Secure Login & Logout

🧠 Data Structures Used
Queue → Managing patient waiting list
Linked List → Storing dynamic records
Stack → (Optional) Undo operations
Arrays / Collections → Data handling

💻 Technologies Used
Java (Core Java)
JDBC (Java Database Connectivity)
MySQL /hos Database
IntelliJ IDEA

🗄️ Database
Stores patient, doctor, appointment, billing, and room data
Connected using JDBC
SQL file included for database setup.

Login Credentials (Demo)
Username: admin
Password: admin@123

Project Structure
src/
 ├── Patient.java
 ├── Doctor.java
 ├── Appointment.java
 ├── Room.java
 ├── Bill.java
 ├── Admin.java
 ├── DatabaseConnection.java
 └── HospitalManagementSystem.java

 ▶️ How to Run

Open project in IntelliJ IDEA

Configure database connection in DatabaseConnection.java

Import SQL file into your database

Run HospitalManagementSystem.java

Login using admin credentials

📊 Billing Logic

Outpatient → Fixed ₹700

Inpatient → Room Charges + Treatment Charges


🎯 Project Objective

To simplify hospital operations and demonstrate the use of Java, JDBC, and Data Structures in a real-world application.


📌 Future scope of the project

GUI (JavaFX / Swing)

Online appointment system

Email/SMS notifications

Advanced reporting system
