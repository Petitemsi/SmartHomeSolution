package com.mycompany.smarthome;
import com.smarthome.environment.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PlantSensorClient {
    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50053)
                .usePlaintext()
                .build();

        PlantSensorServiceGrpc.PlantSensorServiceStub asyncStub =
                PlantSensorServiceGrpc.newStub(channel);

        CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<PlantSensorReading> requestObserver =
                asyncStub.sendSensorReadings(new StreamObserver<PlantSensorSummary>() {
                    @Override
                    public void onNext(PlantSensorSummary summary) {
                        System.out.println("🌱 Summary Received:");
                        System.out.println("  Total Readings: " + summary.getTotalReadings());
                        System.out.printf("  Avg Moisture: %.2f%%\n", summary.getAverageMoisture());
                        System.out.printf("  Avg Light: %.2f lumens\n", summary.getAverageLight());
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.err.println("❌ Error in client: " + t.getMessage());
                        finishLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("✅ Done sending all readings.");
                        finishLatch.countDown();
                    }
                });

        // Send 5 simulated readings
        for (int i = 1; i <= 5; i++) {
            PlantSensorReading reading = PlantSensorReading.newBuilder()
                    .setMoisture(30 + Math.random() * 20)  // simulate 30-50%
                    .setLight(200 + Math.random() * 100)   // simulate 200-300 lumens
                    .build();

            System.out.printf("📤 Sending reading %d: Moisture=%.2f%%, Light=%.2f lumens\n",
                    i, reading.getMoisture(), reading.getLight());

            requestObserver.onNext(reading);
            Thread.sleep(200); // simulate time between readings
        }

        requestObserver.onCompleted();

        // Wait for response
        finishLatch.await(5, TimeUnit.SECONDS);

        channel.shutdown();
    }
}
