package com.mycompany.smarthome;

import com.smarthome.environment.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PlantSensorClient {
    public static void main(String[] args) throws InterruptedException {
        // 1. Create gRPC channel
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        // 2. Prepare metadata for API key
        Metadata headers = new Metadata();
        Metadata.Key<String> API_KEY_HEADER = Metadata.Key.of("api_key", Metadata.ASCII_STRING_MARSHALLER);
        headers.put(API_KEY_HEADER, "my-secret-key");

        // 3. Attach metadata to stub
        PlantSensorServiceGrpc.PlantSensorServiceStub stub =
                MetadataUtils.attachHeaders(PlantSensorServiceGrpc.newStub(channel), headers);

        // 4. Setup countdown latch for async response
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<PlantSensorReading> requestObserver = stub.sendSensorReadings(
                new StreamObserver<PlantSensorSummary>() {
                    @Override
                    public void onNext(PlantSensorSummary summary) {
                        System.out.printf("🌱 Summary: %d readings, Avg Moisture: %.2f%%, Avg Light: %.2f lumens%n",
                                summary.getTotalReadings(),
                                summary.getAverageMoisture(),
                                summary.getAverageLight());
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.err.println("❌ Error: " + t.getMessage());
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("✅ Done sending plant readings.");
                        latch.countDown();
                    }
                });

        // 5. Simulate sending 3 readings
        for (int i = 1; i <= 3; i++) {
            PlantSensorReading reading = PlantSensorReading.newBuilder()
                    .setMoisture(40 + Math.random() * 20)
                    .setLight(300 + Math.random() * 200)
                    .build();

            System.out.printf("📤 Reading %d: Moisture=%.2f%%, Light=%.2f lumens%n",
                    i, reading.getMoisture(), reading.getLight());

            requestObserver.onNext(reading);
            Thread.sleep(300);
        }

        requestObserver.onCompleted();
        latch.await(5, TimeUnit.SECONDS);
        channel.shutdown();
    }
}
