# Hotel Management System

This repository contains the source code for a Hotel Management System, developed as a 4th-semester college project. The project was designed and implemented to demonstrate the practical application of Object-Oriented Programming (OOP) principles and software engineering fundamentals. The system encompasses end-to-end development, including database schema design, graphical user interface (GUI) implementation, and core business logic integration.

## Technology Stack

The project utilizes robust and widely adopted technologies to ensure a strong foundation in software development:

*   **Java 17:** Serves as the primary programming language. The object-oriented nature of Java provides an ideal framework for modeling entities such as `Room`, `Guest`, and `Booking`, perfectly aligning with the project's OOP requirements.
*   **JavaFX:** Employed for the development of the graphical user interface, enabling the creation of a comprehensive and interactive desktop application.
*   **AtlantaFX:** Integrated as a styling framework to provide a modern, polished, and consistent visual aesthetic across the application's user interface.
*   **SQLite:** Utilized as the relational database management system. Its lightweight, serverless architecture ensures reliable data persistence while facilitating easy deployment and evaluation.
*   **Maven:** Functions as the build automation and dependency management tool, streamlining project configuration and the inclusion of external libraries.

## Core Features

The application is designed to facilitate the fundamental operational requirements of a hotel facility:
*   **Inventory Management:** Enables the addition, modification, and status tracking of hotel rooms.
*   **Reservation System:** Facilitates the creation and management of guest bookings and reservations.
*   **Data Persistence:** Ensures all operational data is securely stored and maintained within the local SQLite database (`hotel.db`).

## Project Objectives and Outcomes

The successful completion of this project yielded significant practical experience in several key areas of software engineering:
*   **Object-Oriented Design:** Practical application of theoretical OOP concepts, including inheritance, encapsulation, and polymorphism, within a comprehensive software architecture.
*   **User Interface Design:** Experience in designing and implementing functional and accessible desktop user interfaces.
*   **Database Integration:** Practical knowledge of establishing connectivity between Java applications and SQL databases utilizing JDBC.
*   **Project Architecture:** Understanding of standard project structuring and dependency management using Maven.

## How to Run

To execute the application locally, ensure that Java 17 and Maven are installed on your system. Navigate to the project's root directory via the command line and execute the following commands:

1.  **Compile the project:**
    ```bash
    mvn clean compile
    ```

2.  **Run the application:**
    ```bash
    mvn javafx:run
    ```

The application will launch, and it will automatically connect to the local SQLite database (`hotel.db`).
