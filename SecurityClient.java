package com.mycompany.smarthome;
import com.smarthome.environment.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SecurityClient {
    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50054)
                .usePlaintext()
                .build();

        SecurityServiceGrpc.SecurityServiceStub stub = SecurityServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<DoorEvent> requestObserver =
                stub.monitorDoor(new StreamObserver<DoorAlert>() {
                    @Override
                    public void onNext(DoorAlert alert) {
                        System.out.printf("📢 ALERT from Server [%s]: %s\n",
                                alert.getDoorId(), alert.getAlertMessage());
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.err.println("❌ Client received an error: " + t.getMessage());
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("✅ Server completed sending alerts.");
                        latch.countDown();
                    }
                });

        // Simulate sending door events
        for (int i = 1; i <= 3; i++) {
            DoorEvent event = DoorEvent.newBuilder()
                    .setDoorId("FrontDoor")
                    .setIsOpen(i % 2 == 1)  // alternate between open/close
                    .setTimestamp(LocalTime.now().toString())
                    .build();

            System.out.printf("📤 Sending door event %d: %s\n", i, event.getIsOpen() ? "OPEN" : "CLOSED");
            requestObserver.onNext(event);

            Thread.sleep(400);
        }

        requestObserver.onCompleted();
        latch.await(5, TimeUnit.SECONDS);
        channel.shutdown();
    }
}
