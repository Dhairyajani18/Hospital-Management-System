-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 17, 2026 at 05:13 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `hos`
--

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `AutoGenerateBill` (IN `p_patient_id` INT, OUT `p_bill_id` INT, OUT `p_amount` DOUBLE)   BEGIN
    DECLARE v_doctor_id INT;
    DECLARE v_amount DOUBLE DEFAULT 0;

    -- Get most recent appointment
    SELECT a.Doctor_ID, d.treatment_charge
    INTO v_doctor_id, v_amount
    FROM appointment a
    JOIN doctor d ON a.Doctor_ID = d.id
    WHERE a.Patient_ID = p_patient_id
    ORDER BY a.Date DESC, a.Time DESC
    LIMIT 1;

    IF v_doctor_id IS NULL THEN
        SET p_amount = 0;
        SET p_bill_id = -1;
    ELSE
        -- Insert bill automatically
        INSERT INTO bills(patient_id, amount, bill_date, status)
        VALUES (p_patient_id, v_amount, NOW(), 'pending');

        SET p_bill_id = LAST_INSERT_ID();
        SET p_amount = v_amount;
    END IF;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `appointment`
--

CREATE TABLE `appointment` (
  `Appointment_ID` int(11) NOT NULL,
  `Patient_ID` int(11) NOT NULL,
  `Doctor_ID` int(11) NOT NULL,
  `Date` date NOT NULL,
  `Time` time NOT NULL,
  `status` enum('scheduled','completed','cancelled') DEFAULT 'scheduled',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `appointment`
--

INSERT INTO `appointment` (`Appointment_ID`, `Patient_ID`, `Doctor_ID`, `Date`, `Time`, `status`, `created_at`) VALUES
(19, 1, 4, '2025-08-21', '11:36:08', 'scheduled', '2025-08-21 06:06:08'),
(20, 19, 12, '2025-08-21', '11:49:41', 'scheduled', '2025-08-21 06:19:41'),
(22, 19, 12, '2025-08-21', '12:00:00', 'scheduled', '2025-08-21 06:21:34'),
(23, 20, 1, '2022-12-25', '13:00:00', 'scheduled', '2025-08-21 08:01:39'),
(24, 19, 12, '2200-09-09', '12:00:00', 'scheduled', '2025-08-22 02:55:59'),
(25, 19, 12, '2025-08-22', '10:44:30', 'scheduled', '2025-08-22 05:14:30'),
(26, 19, 12, '2026-06-06', '20:20:00', 'scheduled', '2025-08-22 05:21:47'),
(28, 19, 12, '2025-09-09', '22:00:00', 'scheduled', '2025-08-22 12:53:44');

-- --------------------------------------------------------

--
-- Table structure for table `bills`
--

CREATE TABLE `bills` (
  `bill_id` int(11) NOT NULL,
  `patient_id` int(11) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `bill_date` date NOT NULL DEFAULT curdate(),
  `status` enum('pending','paid','cancelled') DEFAULT 'pending',
  `description` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bills`
--

INSERT INTO `bills` (`bill_id`, `patient_id`, `amount`, `bill_date`, `status`, `description`, `created_at`) VALUES
(4, 3, 2400.00, '2025-08-21', 'pending', NULL, '2025-08-20 19:15:53'),
(5, 3, 4600.00, '2025-08-21', 'pending', NULL, '2025-08-20 19:17:07'),
(6, 3, 890.00, '2025-08-21', 'paid', '', '2025-08-20 19:18:33'),
(8, 19, 12300.00, '2025-08-21', 'pending', NULL, '2025-08-21 07:05:10'),
(9, 19, 12300.00, '2025-08-22', 'pending', NULL, '2025-08-22 06:03:12'),
(10, 19, 12300.00, '2025-08-22', 'pending', NULL, '2025-08-22 13:17:07'),
(11, 19, 12300.00, '2025-08-22', 'pending', NULL, '2025-08-22 13:32:20'),
(12, 20, 1500.00, '2025-08-22', 'paid', NULL, '2025-08-22 13:32:30'),
(13, 1, 1000.00, '2025-08-22', 'cancelled', NULL, '2025-08-22 13:34:14');

-- --------------------------------------------------------

--
-- Table structure for table `doctor`
--

CREATE TABLE `doctor` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `specialization` varchar(100) NOT NULL,
  `treatment_charge` decimal(10,2) DEFAULT 0.00,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `doctor`
--

INSERT INTO `doctor` (`id`, `name`, `specialization`, `treatment_charge`, `created_at`) VALUES
(1, 'Dr. Smith', 'Cardiology', 1500.00, '2025-08-20 12:32:39'),
(2, 'Dr. Johnson', 'Neurology', 2000.00, '2025-08-20 12:32:39'),
(3, 'Dr. Williams', 'Orthopedics', 1200.00, '2025-08-20 12:32:39'),
(4, 'Dr. Brown', 'Pediatrics', 1000.00, '2025-08-20 12:32:39'),
(12, 'dr jani', 'dermatologist', 12300.00, '2025-08-21 06:13:08');

-- --------------------------------------------------------

--
-- Table structure for table `doctorslots`
--

CREATE TABLE `doctorslots` (
  `id` int(11) NOT NULL,
  `doctor_id` int(11) NOT NULL,
  `slot` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `doctorslots`
--

INSERT INTO `doctorslots` (`id`, `doctor_id`, `slot`) VALUES
(1, 1, '09:00-10:00'),
(2, 1, '10:00-11:00'),
(3, 1, '14:00-15:00'),
(4, 2, '10:00-11:00'),
(5, 2, '11:00-12:00'),
(6, 2, '15:00-16:00'),
(7, 3, '09:00-10:00'),
(8, 3, '13:00-14:00'),
(9, 3, '16:00-17:00'),
(10, 4, '08:00-09:00'),
(11, 4, '09:00-10:00'),
(12, 4, '14:00-15:00'),
(21, 12, '09:00-10:00');

-- --------------------------------------------------------

--
-- Table structure for table `patients`
--

CREATE TABLE `patients` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `age` int(11) NOT NULL,
  `gender` varchar(10) NOT NULL,
  `contact` varchar(15) NOT NULL,
  `address` text NOT NULL,
  `history` text DEFAULT NULL,
  `type` enum('inpatient','outpatient') DEFAULT 'outpatient',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `patients`
--

INSERT INTO `patients` (`id`, `name`, `age`, `gender`, `contact`, `address`, `history`, `type`, `created_at`) VALUES
(1, 'John Doe', 35, 'Male', '9876543210', '123 Main St, City', 'No major health issues', 'outpatient', '2025-08-20 12:32:39'),
(2, 'Jane Smith', 28, 'Female', '9876543211', '456 Oak Ave, City', 'Diabetes', 'outpatient', '2025-08-20 12:32:39'),
(3, 'Bob Johnson', 45, 'Male', '9876543212', '789 Pine St, City', 'Hypertension', 'inpatient', '2025-08-20 12:32:39'),
(19, 'shiv', 14, 'male', '1234567890', 'asd123', 'skin issues', 'inpatient', '2025-08-21 06:19:07'),
(20, 'asd', 15, 'male', '6789345234', 'aswdfg', 'asdf', 'outpatient', '2025-08-21 07:56:05');

--
-- Triggers `patients`
--
DELIMITER $$
CREATE TRIGGER `validate_patient_age` BEFORE INSERT ON `patients` FOR EACH ROW BEGIN
    IF NEW.age < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Age cannot be negative';
    END IF;
    IF NEW.age > 150 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Age cannot be greater than 150';
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `validate_patient_age_update` BEFORE UPDATE ON `patients` FOR EACH ROW BEGIN
    IF NEW.age < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Age cannot be negative';
    END IF;
    IF NEW.age > 150 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Age cannot be greater than 150';
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `rooms`
--

CREATE TABLE `rooms` (
  `room_id` int(11) NOT NULL,
  `type` varchar(50) DEFAULT NULL,
  `charge` double DEFAULT NULL,
  `available` tinyint(1) DEFAULT NULL,
  `patient_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `rooms`
--

INSERT INTO `rooms` (`room_id`, `type`, `charge`, `available`, `patient_id`) VALUES
(101, 'General', 1000, 1, NULL),
(103, 'Private', 1200, 0, 19),
(104, 'General', 1200, 1, NULL),
(105, 'Private', 1234, 1, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','PATIENT','DOCTOR') NOT NULL,
  `name` varchar(100) NOT NULL,
  `patient_id` int(11) DEFAULT NULL,
  `doctor_id` int(11) DEFAULT NULL,
  `specialization` varchar(100) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `role`, `name`, `patient_id`, `doctor_id`, `specialization`, `created_at`) VALUES
(7, 'om', 'om@123', 'PATIENT', 'om ', 34, NULL, NULL, '2025-08-20 12:49:41'),
(51, 'dr. patel', 'patel@123', 'DOCTOR', 'Diya patel', NULL, 6, 'neurologist', '2025-08-21 05:58:52'),
(52, 'dr dabhi', 'dabhi@123', 'DOCTOR', 'Pruthvi Dabhi', NULL, 7, 'orthopadic', '2025-08-21 06:00:50'),
(54, 'raj', 'raj@123', 'PATIENT', 'raj patel', 4, NULL, NULL, '2025-08-21 06:02:34'),
(58, 'dr.jani', 'jani@123', 'DOCTOR', 'Dhairya jani', NULL, 12, 'dermatologist', '2025-08-21 06:13:48'),
(60, 'shiv', 'shiv@123', 'PATIENT', 'shiv patel', 19, NULL, NULL, '2025-08-21 06:20:43'),
(67, 'asd12', 'asd', 'PATIENT', 'asdf12', 20, NULL, NULL, '2025-08-21 07:58:02'),
(79, 'jay', 'jay@123', 'PATIENT', 'jay ', 20, NULL, NULL, '2025-08-22 05:28:21'),
(82, 'harry', 'harry@123', 'PATIENT', 'harry', 21, NULL, NULL, '2025-08-22 05:40:11'),
(96, 'admin', 'admin@123', 'ADMIN', 'System Administrator', NULL, NULL, NULL, '2025-08-22 15:54:00');

-- --------------------------------------------------------

--
-- Table structure for table `waiting_list`
--

CREATE TABLE `waiting_list` (
  `id` int(11) NOT NULL,
  `patient_id` int(11) NOT NULL,
  `doctor_id` int(11) NOT NULL,
  `requested_date` date NOT NULL,
  `requested_time` time NOT NULL,
  `priority` int(11) DEFAULT 1,
  `status` enum('waiting','processed','cancelled') DEFAULT 'waiting',
  `added_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `appointment`
--
ALTER TABLE `appointment`
  ADD PRIMARY KEY (`Appointment_ID`),
  ADD KEY `Patient_ID` (`Patient_ID`),
  ADD KEY `Doctor_ID` (`Doctor_ID`);

--
-- Indexes for table `bills`
--
ALTER TABLE `bills`
  ADD PRIMARY KEY (`bill_id`),
  ADD KEY `patient_id` (`patient_id`);

--
-- Indexes for table `doctor`
--
ALTER TABLE `doctor`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `doctorslots`
--
ALTER TABLE `doctorslots`
  ADD PRIMARY KEY (`id`),
  ADD KEY `doctor_id` (`doctor_id`);

--
-- Indexes for table `patients`
--
ALTER TABLE `patients`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `rooms`
--
ALTER TABLE `rooms`
  ADD PRIMARY KEY (`room_id`),
  ADD KEY `fk_patient` (`patient_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `unique_username` (`username`),
  ADD UNIQUE KEY `unique_password` (`password`),
  ADD UNIQUE KEY `unique_doctor_id` (`doctor_id`);

--
-- Indexes for table `waiting_list`
--
ALTER TABLE `waiting_list`
  ADD PRIMARY KEY (`id`),
  ADD KEY `patient_id` (`patient_id`),
  ADD KEY `doctor_id` (`doctor_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `appointment`
--
ALTER TABLE `appointment`
  MODIFY `Appointment_ID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT for table `bills`
--
ALTER TABLE `bills`
  MODIFY `bill_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `doctor`
--
ALTER TABLE `doctor`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `doctorslots`
--
ALTER TABLE `doctorslots`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT for table `patients`
--
ALTER TABLE `patients`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=97;

--
-- AUTO_INCREMENT for table `waiting_list`
--
ALTER TABLE `waiting_list`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `appointment`
--
ALTER TABLE `appointment`
  ADD CONSTRAINT `appointment_ibfk_1` FOREIGN KEY (`Patient_ID`) REFERENCES `patients` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `appointment_ibfk_2` FOREIGN KEY (`Doctor_ID`) REFERENCES `doctor` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `bills`
--
ALTER TABLE `bills`
  ADD CONSTRAINT `bills_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `doctorslots`
--
ALTER TABLE `doctorslots`
  ADD CONSTRAINT `doctorslots_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `rooms`
--
ALTER TABLE `rooms`
  ADD CONSTRAINT `fk_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`);

--
-- Constraints for table `waiting_list`
--
ALTER TABLE `waiting_list`
  ADD CONSTRAINT `waiting_list_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `waiting_list_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
