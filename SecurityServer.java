package com.mycompany.smarthome;
import com.smarthome.environment.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.time.LocalTime;

public class SecurityServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50054)
                .addService(new SecurityServiceImpl())
                .build();

        System.out.println("🔐 SecurityServer running on port 50054");
        server.start();
        server.awaitTermination();
    }

    static class SecurityServiceImpl extends SecurityServiceGrpc.SecurityServiceImplBase {
        @Override
        public StreamObserver<DoorEvent> monitorDoor(StreamObserver<DoorAlert> responseObserver) {
            return new StreamObserver<DoorEvent>() {
                @Override
                public void onNext(DoorEvent event) {
                    System.out.printf("🚪 Event received: Door [%s] is %s%n",
                            event.getDoorId(), event.getIsOpen() ? "OPEN" : "CLOSED");

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
                    System.err.println("❌ Error receiving door events.");
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                    System.out.println("✅ Finished receiving door events.");
                }
            };
        }
    }
}
