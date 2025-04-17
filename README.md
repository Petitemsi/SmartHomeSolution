# SmartHome – Distributed Systems Assignment (2025)

## Project Overview
SmartHome is a distributed Java-based application demonstrating the use of gRPC and JmDNS for microservice communication. It includes four core services designed to simulate key aspects of a smart home system:

- **ClimateControlService**: Set and receive temperature adjustments.
- **EnergyRoutineService**: Stream hourly energy usage data.
- **PlantSensorService**: Collect and analyze plant sensor data (moisture and light).
- **SecurityService**: Monitor and stream door events with metadata validation.

## Technologies Used
- Java 8
- gRPC
- Protocol Buffers
- JmDNS for service discovery
- Maven
- SLF4J for logging

## Services and Ports
| Service              | Port  |
|----------------------|-------|
| ClimateControlServer | 50051 |
| EnergyRoutineServer  | 50052 |
| PlantSensorServer    | 50053 |
| SecurityServer       | 50054 |

## How to Run

### 1. Start Servers (in separate terminals or IDE tabs):
```sh
mvn exec:java -Dexec.mainClass="com.mycompany.smarthome.ClimateControlServer"
mvn exec:java -Dexec.mainClass="com.mycompany.smarthome.EnergyRoutineServer"
mvn exec:java -Dexec.mainClass="com.mycompany.smarthome.PlantSensorServer"
mvn exec:java -Dexec.mainClass="com.mycompany.smarthome.SecurityServer"
```

### 2. Launch the GUI Client
Once all servers are running, start the GUI application:
```sh
mvn exec:java -Dexec.mainClass="com.mycompany.smarthome.SmartHomeGUI"
```

## Features
- JmDNS service registration and discovery
- gRPC unary and streaming RPCs
- Custom metadata validation using interceptors
- Real-time data simulation and analysis
- Intuitive Swing-based GUI for user interaction

## Author
**Tansel Seray Aydinli**  
Student ID: 23348381  
Module: Distributed Systems (HDCSDEV_INT)  
Lecturer: Caitriona Nic Lughadha
