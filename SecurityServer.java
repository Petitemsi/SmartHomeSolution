package com.mycompany.smarthome;

import com.smarthome.environment.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SecurityServer {

    private static final Logger logger = Logger.getLogger(SecurityServer.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50054;

        // Enable logging to a file
        FileHandler fileHandler = new FileHandler("security-server-log.txt", true);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);

        // Setup gRPC server
        Server server = ServerBuilder.forPort(port)
                .intercept(new ApiKeyInterceptor())
                .addService(new SecurityServiceImpl())
                .build();

        // Register the service with JmDNS
        JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
        ServiceInfo serviceInfo = ServiceInfo.create(
                "_security._tcp.local.",
                "SecurityService",
                port,
                "path=/security"
        );
        jmdns.registerService(serviceInfo);

        logger.info("🔐 SecurityServer starting on port " + port + "...");
        server.start();
        logger.info("✅ SecurityServer registered with JmDNS.");
        server.awaitTermination();

        jmdns.unregisterAllServices();
    }

    static class SecurityServiceImpl extends SecurityServiceGrpc.SecurityServiceImplBase {
        @Override
        public StreamObserver<DoorEvent> monitorDoor(StreamObserver<DoorAlert> responseObserver) {
            return new StreamObserver<DoorEvent>() {
                @Override
                public void onNext(DoorEvent event) {
                    logger.info("📥 Received door event: " + event.getDoorId() + " is " +
                            (event.getIsOpen() ? "OPEN" : "CLOSED"));

                    String message = event.getIsOpen()
                            ? "⚠️ ALERT: Door " + event.getDoorId() + " was opened!"
                            : "✅ Door " + event.getDoorId() + " is now closed.";

                    DoorAlert alert = DoorAlert.newBuilder()
                            .setDoorId(event.getDoorId())
                            .setAlertMessage(message)
                            .setTimestamp(LocalTime.now().toString())
                            .build();

                    responseObserver.onNext(alert);
                }

                @Override
                public void onError(Throwable t) {
                    logger.severe("❌ Error in door monitoring: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                    logger.info("✅ Completed door monitoring.");
                }
            };
        }
    }
}
