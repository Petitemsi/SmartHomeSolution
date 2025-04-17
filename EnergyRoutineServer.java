package com.mycompany.smarthome;

import com.smarthome.environment.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class EnergyRoutineServer {

    private static final Logger logger = Logger.getLogger(EnergyRoutineServer.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {
        // Setup logger to write to a file
        FileHandler fileHandler = new FileHandler("energy-server-log.txt", true);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);

        // Define server port
        int port = 50052;

        // Start gRPC server with API key interceptor
        Server server = ServerBuilder.forPort(port)
                .intercept(new ApiKeyInterceptor())
                .addService(new EnergyRoutineServiceImpl())
                .build();

        // Register service with JmDNS
        try {
            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
            ServiceInfo serviceInfo = ServiceInfo.create(
                    "_energy._tcp.local.", // service type
                    "EnergyRoutineService", // service name
                    port,
                    "Energy usage monitoring service"
            );
            jmdns.registerService(serviceInfo);
            logger.info("🔎 EnergyRoutineService registered with JmDNS.");
        } catch (IOException e) {
            logger.warning("❌ JmDNS registration failed: " + e.getMessage());
        }

        logger.info("⚡ EnergyRoutineServer starting on port " + port + "...");
        server.start();
        logger.info("✅ EnergyRoutineServer started successfully.");
        server.awaitTermination();
    }

    // gRPC service implementation
    static class EnergyRoutineServiceImpl extends EnergyRoutineServiceGrpc.EnergyRoutineServiceImplBase {

        @Override
        public void streamHourlyEnergyUsage(EnergyUsageRequest request, StreamObserver<EnergyUsageData> responseObserver) {
            logger.info("📥 Client requested energy usage data for: " + request.getDate());

            Random random = new Random();

            for (int hour = 0; hour < 24; hour++) {
                double usage = 0.5 + (2.0 * random.nextDouble());

                EnergyUsageData data = EnergyUsageData.newBuilder()
                        .setHour(hour)
                        .setUsageKwh(usage)
                        .build();

                responseObserver.onNext(data);
                logger.info(String.format("📊 Hour %02d: %.2f kWh", hour, usage));

                try {
                    Thread.sleep(100); // simulate delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warning("⚠️ Energy streaming interrupted.");
                    break;
                }
            }

            responseObserver.onCompleted();
            logger.info("✅ Completed streaming energy usage to client.");
        }
    }
}
