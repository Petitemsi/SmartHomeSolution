package com.mycompany.smarthome;

import com.smarthome.environment.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SecurityClient {
    public static void main(String[] args) throws InterruptedException {
        // 1. Create the gRPC channel
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50054)
                .usePlaintext()
                .build();

        // 2. Add the API key to metadata
        Metadata headers = new Metadata();
        Metadata.Key<String> API_KEY_HEADER = Metadata.Key.of("api_key", Metadata.ASCII_STRING_MARSHALLER);
        headers.put(API_KEY_HEADER, "my-secret-key"); // must match ApiKeyInterceptor on server

        // 3. Attach metadata to the stub
        SecurityServiceGrpc.SecurityServiceStub stubWithApiKey =
                MetadataUtils.attachHeaders(SecurityServiceGrpc.newStub(channel), headers);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<DoorEvent> requestObserver =
                stubWithApiKey.monitorDoor(new StreamObserver<DoorAlert>() {
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
                        System.out.println("✅ Server finished sending alerts.");
                        latch.countDown();
                    }
                });

        // 4. Simulate sending door events
        for (int i = 1; i <= 3; i++) {
            DoorEvent event = DoorEvent.newBuilder()
                    .setDoorId("FrontDoor")
                    .setIsOpen(i % 2 == 1)  // alternate OPEN/CLOSED
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
