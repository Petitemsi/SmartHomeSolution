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

public class ClimateControlServer {

    private static final Logger logger = Logger.getLogger(ClimateControlServer.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {
        // Enable file logging
        FileHandler fileHandler = new FileHandler("climate-server-log.txt", true);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);

        // Start gRPC server
        Server server = ServerBuilder.forPort(50051)
                .intercept(new ApiKeyInterceptor())
                .addService(new ClimateControlServiceImpl())
                .build();

        logger.info("🌡️ ClimateControlServer starting on port 50051...");
        server.start();
        logger.info("✅ ClimateControlServer started.");

        // Register with JmDNS for service discovery
        JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
        ServiceInfo serviceInfo = ServiceInfo.create("_grpc._tcp.local.", "ClimateControlService", 50051, "service=climate");
        jmdns.registerService(serviceInfo);
        logger.info("🌍 ClimateControlService registered with JmDNS.");

        server.awaitTermination();
    }

    static class ClimateControlServiceImpl extends ClimateControlServiceGrpc.ClimateControlServiceImplBase {
        @Override
        public void setTemperature(TemperatureRequest request, StreamObserver<TemperatureResponse> responseObserver) {
            try {
                logger.info("📥 Received temperature request: Room=" + request.getRoom() +
                        ", Temperature=" + request.getTemperature());

                TemperatureResponse response = TemperatureResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("✅ Temperature set to " + request.getTemperature() + "°C in " + request.getRoom())
                        .build();

                logger.info("📤 Sending response: " + response.getMessage());

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (Exception e) {
                logger.severe("❌ Error handling setTemperature request: " + e.getMessage());
                responseObserver.onError(e);
            }
        }
    }
}
