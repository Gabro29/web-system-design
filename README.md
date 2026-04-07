# ☕ Coffee cApp — Vending Machine Management System

A distributed software system designed to manage a network of **smart vending machines**, including **backend services**, **data persistence**, and a dedicated **real-time monitoring service**.

This project was developed as part of the **Web System and Design Architecture** assignments.

---

## 📌 Project Overview

The platform is composed of **two main applications**:

### 1. Main Application — Spring Boot
Handles the core business logic of the vending ecosystem, including:

- user management
- customer credit handling
- vending operations
- machine interaction workflows

### 2. Monitoring Service — JakartaEE
Responsible for monitoring and supervising the vending infrastructure, including:

- real-time machine status tracking
- machine location management
- fault detection and operational supervision

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

The **main application** has been secured using **Spring Security**.

Authentication and authorization have been implemented for the following user roles:

- `CUSTOMER`
- `VENDING_MACHINE`
- `MAINTENANCE_WORKER`
- `SYSTEM_MANAGER`

### Security Features
- **Login support** for all four user roles
- **Role-based authorization**
- **Protected controllers** based on user permissions
- **Security Filter Chain** configuration for route access control

### Access Control
Each user can only access the controllers and functionalities related to their role.

For example:

- **Customers** can access account and vending operations
- **Vending machines** can access machine-side communication endpoints
- **Maintenance workers** can access maintenance and machine status operations
- **System managers** can access administrative and infrastructure management features

> The **monitoring service** remains accessible without authentication, as required by the assignment specification.

---

## 🏗️ System Architecture & Persistence

The system follows a **distributed architecture** with separation between operational logic and infrastructure monitoring.

### Main Backend
Uses a **relational database** to store:

- user profiles
- customer credit
- vending machine data
- operational business data

### Monitoring Backend
Uses an **independent database** (relational or NoSQL) to store:

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

### Persistence
- **MySQL / PostgreSQL** (main backend)

### Data Exchange
- **JSON**
- **XML**
- **XSD Schemas**

### Frontend
- **Single Page Applications (SPA)** for all user-facing interfaces

---

## 🚀 Installation & Setup

## Prerequisites

Before running the project, make sure you have installed:

- **Java 17** or higher
- **Maven**
- A **JakartaEE-compatible application server**
- A SQL database such as **MySQL**

---

## ▶️ Running the Main Application (Spring Boot)

1. Navigate to the `main-app` directory:

```bash
cd main-app
```

2. Configure your database connection in:

```properties
src/main/resources/application.properties
```

3. Run the application:

```bash
./mvnw spring-boot:run
```

---

## ▶️ Running the Monitoring Service (JakartaEE)

1. Navigate to the `monitoring-service` directory:

```bash
cd monitoring-service
```

2. Build and deploy the generated `.war` file to your JakartaEE application server.

3. Ensure that the **monitoring database** is initialized separately from the main backend database.

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
