# ☕ Coffee cApp — Vending Machine Management System

A distributed application designed to manage a network of **smart vending machines**, including user interaction, vending operations, and machine monitoring.

This project was developed as part of the **Web System and Design Architecture** assignments.

---

## 📌 Project Overview

The system is composed of **two separate applications** contained in the same repository:

### 1. Spring Application — Main System
Built with **Spring Boot**, this is the main application of the project.

It handles:

- user management
- authentication and authorization
- customer credit
- vending operations
- machine interaction
- administrative functionalities

### 2. JakartaEE Application — Monitoring Service
Built with **JakartaEE**, this service is responsible for monitoring the vending infrastructure.

It handles:

- machine heartbeat tracking
- machine operational status
- machine location and state monitoring
- fault detection

---

## 📁 Repository Structure

```text
coffee-capp/
│
├── spring-app/              # Main application (Spring Boot)
│   ├── src/
│   ├── pom.xml
│   └── ...
│
├── jakarta-monitoring/      # Monitoring service (JakartaEE)
│   ├── src/
│   ├── pom.xml
│   └── ...
│
├── database/
│   ├── kaffeknd_spring.sql
│   └── kaffeknd_jakarta.sql
│
├── README.md
└── .gitignore
```

---

## 👥 Features by User Role

The system supports **four different user roles**, each with dedicated functionalities.

---

### 🖥️ 1. Vending Machine Screen

The vending machine acts as an active node in the distributed system.

#### Features
- **Heartbeat System**
  - Sends an asynchronous heartbeat signal to the monitoring service every **60 seconds**
  - Confirms that the machine is online and operational

- **Dispensing Logic**
  - Verifies the connected user’s available credit
  - Deducts balance after drink selection

- **User Detection**
  - Periodically checks for connected users
  - Supports **JSON/XML-based communication**

---

### 👤 2. Customer

Customers can interact directly with vending machines and manage their accounts.

#### Features
- **Account Management**
  - User registration and login

- **Machine Interaction**
  - Connect/disconnect to a vending machine using its unique ID
  - Perform **credit top-ups**
  - Purchase drinks from connected machines

---

### 🛠️ 3. Maintenance Worker

Maintenance staff can inspect and manage machine conditions.

#### Features
- **Status Monitoring**
  - Access machine status information
  - View:
    - supply levels
    - machine faults
    - operational conditions

- **XML Integration**
  - Export and consume XML streams for machine-related data

- **State Management**
  - Update machine operational state:
    - `Active`
    - `In Maintenance`

---

### 🧑‍💼 4. System Manager

The system manager has administrative control over the vending network.

#### Features
- **Infrastructure Management**
  - Full CRUD operations for:
    - vending machines
    - maintenance workers

- **Global Monitoring**
  - Search and inspect all vending machines in the network
  - Monitor their current status and availability

---

## 🔐 Security

The **Spring Boot application** has been secured using **Spring Security**.

Authentication and authorization have been implemented for the following roles:

- `CUSTOMER`
- `VENDING_MACHINE`
- `MAINTENANCE_WORKER`
- `SYSTEM_MANAGER`

### Security Features
- Login support for all four user roles
- Role-based authorization
- Protected controllers based on permissions
- Security Filter Chain configuration for route protection

### Access Control
Each user can only access the endpoints and functionalities related to their role.

For example:

- **Customers** can access account and vending operations
- **Vending machines** can access machine-side communication endpoints
- **Maintenance workers** can access maintenance and machine status operations
- **System managers** can access administrative and infrastructure management features

> The **JakartaEE monitoring service** remains accessible without authentication.

---

## 🏗️ System Architecture & Persistence

The system follows a **distributed architecture** with a separation between the main operational logic and the monitoring service.

### Spring Boot Backend
Uses a **relational database** to store:

- user profiles
- customer credit
- vending machine data
- business logic data

### JakartaEE Monitoring Backend
Uses a separate database to store:

- vending machine coordinates
- machine states
- heartbeat tracking information

Supported machine states:

- `Active`
- `Maintenance`
- `Broken`

### Fault Detection Logic
The monitoring service automatically marks a machine as `Broken` if **no heartbeat is received within 3 minutes**.

---

## ⚙️ Tech Stack

### Backend
- **Java 17**
- **Spring Boot**
- **JakartaEE**
- **Spring Security**
- **Maven**

### Database
- **MySQL**

### Data Exchange
- **JSON**
- **XML**
- **XSD Schemas**

### Frontend
- **Single Page Applications (SPA)** for the user interfaces

---

## 🚀 Installation & Setup

## Prerequisites

Before running the project, make sure you have installed:

- **Java 17** or higher
- **Maven**
- **MySQL**
- A **JakartaEE-compatible application server**

---

## 🗄️ Database Configuration

### MySQL Credentials
- **Username:** `kaffe`
- **Password:** `kaffe`

### SQL Files
The SQL files used to initialize the databases are located in:

```text
database/kaffeknd_spring.sql
database/kaffeknd_jakarta.sql
```

---

## ▶️ Running the Spring Boot Application

### Default Port
```text
8081
```

### Steps

1. Navigate to the Spring Boot project folder:

```bash
cd spring-app
```

2. Configure your database connection inside:

```properties
src/main/resources/application.properties
```

3. Import and run the SQL file for the Spring database:

```text
database/kaffeknd_spring.sql
```

4. Run the application:

```bash
./mvnw spring-boot:run
```

or

```bash
mvn spring-boot:run
```

---

## ▶️ Running the JakartaEE Monitoring Service

### Default Port
```text
8080
```

### Steps

1. Navigate to the JakartaEE project folder:

```bash
cd jakarta-monitoring
```

2. Import and run the SQL file for the monitoring database:

```text
database/kaffeknd_jakarta.sql
```

3. Build and deploy the generated `.war` file to your JakartaEE application server.

---

## 📂 Data Representations

To ensure structured and consistent communication, the project includes dedicated **XML Schemas (XSD)**.

### Included XML Components

- **Machine Status XSD**
  - Defines the structure of machine state and monitoring data

- **Maintenance Staff XML**
  - Stores and displays maintenance staff information
  - Used in tabular frontend representations

---

## 🖼️ Frontend Notes

The user interfaces for the following roles are implemented as **Single Page Applications (SPA)**:

- Customer
- Maintenance Worker
- Vending Machine Screen

This approach provides a smoother and more interactive user experience.
