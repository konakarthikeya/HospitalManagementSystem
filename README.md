# Hospital Management System

A Java Swing-based Hospital Appointment and Patient Management System developed as a college project.

## Features

- **Patient Management:** Register and manage patient details.
- **Doctor Selection:** Select doctors for appointments.
- **Appointment Booking:** Schedule appointments with date and time.
- **Appointment Modification:** Modify existing appointments.
- **Appointment Cancellation:** Cancel scheduled appointments.
- **Patient Search:** Search for patients using Patient ID.
- **Doctor-wise Statistics:** View appointment statistics for each doctor.
- **Input Validation:** Validate user input and prevent duplicate appointment slots.
- **Graphical User Interface:** Interactive desktop interface built with Java Swing.

## Technologies Used

- Java
- Java Swing
- AWT Event Handling
- Object-Oriented Programming
- Collections and Service Classes

## Project Structure

```text
HospitalManagementSystem/
├── hospital/
│   ├── model/
│   ├── service/
│   ├── ui/
│   ├── HospitalManagementApp.java
│   └── ServiceTest.java
└── README.md
```

## How to Run

### Prerequisites
- Java Development Kit (JDK) installed.
- A terminal or command prompt.

### Compile

Run these commands from the directory containing the `hospital` folder:

```bash
javac -d out hospital/model/*.java hospital/service/*.java hospital/ui/*.java hospital/HospitalManagementApp.java hospital/ServiceTest.java
```

### Launch the application

```bash
java -cp out hospital.HospitalManagementApp
```

## Purpose

This project demonstrates Java programming concepts, GUI development, event handling, input validation, and modular application design.

## Author

Developed as an academic project.
