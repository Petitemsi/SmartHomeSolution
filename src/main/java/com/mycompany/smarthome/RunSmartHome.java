package com.mycompany.smarthome;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.mycompany.smarthome.ClimateControlServer.ClimateControlServiceImpl;
import com.mycompany.smarthome.EnergyRoutineServer.EnergyRoutineServiceImpl;
import com.mycompany.smarthome.PlantSensorServer.PlantSensorServiceImpl;
import com.mycompany.smarthome.SecurityServer.SecurityServiceImpl;

public class RunSmartHome {

    private static final Logger logger = Logger.getLogger(RunSmartHome.class.getName());
    private List<Server> servers = new ArrayList<>();

    private boolean isPortAvailable(int port) {
        try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private Server startServer(int port, String serviceName, io.grpc.BindableService service) throws IOException {
        if (!isPortAvailable(port)) {
            logger.severe("❌ Port " + port + " is already in use for " + serviceName);
            throw new IOException("Port " + port + " is not available");
        }

        Server server = ServerBuilder.forPort(port)
                .intercept(new ApiKeyInterceptor())
                .addService(service)
                .build();
        server.start();
        logger.info(serviceName + " started on port " + port);
        return server;
    }

    public void startAllServers() {
        try {
            // Setup logging
            FileHandler fileHandler = new FileHandler("smart-home-servers.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Climate Control Server
            Server climateServer = startServer(50051, "🌡️ Climate Control Server", new ClimateControlServiceImpl());
            servers.add(climateServer);

            // Energy Routine Server
            Server energyServer = startServer(50052, "⚡ Energy Routine Server", new EnergyRoutineServiceImpl());
            servers.add(energyServer);

            // Plant Sensor Server
            Server plantServer = startServer(50053, "🌿 Plant Sensor Server", new PlantSensorServiceImpl());
            servers.add(plantServer);

            logger.info("✅ All SmartHome servers are running!");

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down all servers...");
                stopAllServers();
            }));

            // Keep the servers running
            for (Server server : servers) {
                server.awaitTermination();
            }

        } catch (IOException | InterruptedException e) {
            logger.severe("❌ Error starting servers: " + e.getMessage());
            stopAllServers();
        }
    }

    private void stopAllServers() {
        for (Server server : servers) {
            if (server != null) {
                try {
                    server.shutdown().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    logger.warning("Error shutting down server: " + e.getMessage());
                    server.shutdownNow();
                }
            }
        }
        logger.info("All servers stopped");
    }

    public static void main(String[] args) {
        RunSmartHome smartHome = new RunSmartHome();
        smartHome.startAllServers();
    }
}
