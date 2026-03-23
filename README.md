======Hospital Management System======

📌 Project Description
The Hospital Management System is a Java-based console application designed to manage hospital operations efficiently. It allows administrators to handle patients, doctors, appointments, billing, and room allocation using JDBC and core Data Structures.

🚀 Features

🔐 1. Login System:
User enters username & password

System verifies credentials from database

Based on role (Admin / Doctor / Patient), dashboard is displayed.

👨🏻‍💼 Admin Module:Manage patients, doctors, appointments, billing, and room allocation with full control.

👨‍⚕️ Doctor Module:View profile, appointments, schedules, update availability, and generate patient bills.

🧑‍🤝‍🧑 Patient Module:Register, book/cancel appointments, and view details.

🛏️ Room Management (Data Structures):Efficient allocation of ICU, Private, and General rooms using Queue and Linked List.

📅 Appointment System:Doctor-wise scheduling with specialization, charges, and time slots.

💰 Billing System:Auto-calculation from recent appointment and manual entry option for customized billing (room + treatment charges for inpatients).


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
