package com.mycompany.smarthome;

import com.smarthome.environment.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class PlantSensorServer {

    private static final Logger logger = Logger.getLogger(PlantSensorServer.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50053;

        //  Setup file-based logging
        FileHandler fileHandler = new FileHandler("plant-sensor-server-log.txt", true);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);

        // Start gRPC server
        Server server = ServerBuilder.forPort(port)
                .intercept(new ApiKeyInterceptor())
                .addService(new PlantSensorServiceImpl())
                .build();

        // Register with JmDNS
        JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
        ServiceInfo serviceInfo = ServiceInfo.create(
                "_plant._tcp.local.",
                "PlantSensorService",
                port,
                "path=/plant"
        );
        jmdns.registerService(serviceInfo);

        logger.info("🌱 PlantSensorServer starting on port " + port + "...");
        server.start();
        logger.info("✅ PlantSensorServer registered via JmDNS.");
        server.awaitTermination();

        jmdns.unregisterAllServices();
    }

    static class PlantSensorServiceImpl extends PlantSensorServiceGrpc.PlantSensorServiceImplBase {

        @Override
        public StreamObserver<PlantSensorReading> sendSensorReadings(StreamObserver<PlantSensorSummary> responseObserver) {
            return new StreamObserver<PlantSensorReading>() {

                int count = 0;
                double totalMoisture = 0;
                double totalLight = 0;

                @Override
                public void onNext(PlantSensorReading reading) {
                    logger.info("📥 Reading received: Moisture=" + reading.getMoisture() +
                            "%, Light=" + reading.getLight() + " lumens");
                    count++;
                    totalMoisture += reading.getMoisture();
                    totalLight += reading.getLight();
                }

                @Override
                public void onError(Throwable t) {
                    logger.severe("❌ Error receiving readings: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    double avgMoisture = count == 0 ? 0 : totalMoisture / count;
                    double avgLight = count == 0 ? 0 : totalLight / count;

                    PlantSensorSummary summary = PlantSensorSummary.newBuilder()
                            .setTotalReadings(count)
                            .setAverageMoisture(avgMoisture)
                            .setAverageLight(avgLight)
                            .build();

                    logger.info("📤 Sending summary: " + summary.toString());
                    responseObserver.onNext(summary);
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
