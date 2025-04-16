package com.mycompany.smarthome;

import com.smarthome.environment.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.time.LocalTime;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SmartHome {
    public static void main(String[] args) throws Exception {

        // Channels for each service
        ManagedChannel climateChannel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();
        ManagedChannel energyChannel = ManagedChannelBuilder.forAddress("localhost", 50052).usePlaintext().build();
        ManagedChannel plantChannel = ManagedChannelBuilder.forAddress("localhost", 50053).usePlaintext().build();
        ManagedChannel securityChannel = ManagedChannelBuilder.forAddress("localhost", 50054).usePlaintext().build();

        // 1. Unary RPC - ClimateControlService
        ClimateControlServiceGrpc.ClimateControlServiceBlockingStub climateStub =
                ClimateControlServiceGrpc.newBlockingStub(climateChannel);

        TemperatureRequest tempRequest = TemperatureRequest.newBuilder()
                .setRoom("Living Room")
                .setTemperature(22.5)
                .build();

        TemperatureResponse tempResponse = climateStub.setTemperature(tempRequest);
        System.out.println("✅ ClimateControlService Response: " + tempResponse.getMessage());

        // 2. Server Streaming RPC - EnergyRoutineService
        EnergyRoutineServiceGrpc.EnergyRoutineServiceBlockingStub energyStub =
                EnergyRoutineServiceGrpc.newBlockingStub(energyChannel);

        EnergyUsageRequest energyRequest = EnergyUsageRequest.newBuilder()
                .setDate("2025-04-13")
                .build();

        System.out.println("\n🔌 Streaming hourly energy usage:");
        Iterator<EnergyUsageData> energyData = energyStub.streamHourlyEnergyUsage(energyRequest);
        while (energyData.hasNext()) {
            EnergyUsageData data = energyData.next();
            System.out.printf("  - Hour %02d: %.2f kWh%n", data.getHour(), data.getUsageKwh());
        }

        // 3. Client Streaming RPC - PlantSensorService
        PlantSensorServiceGrpc.PlantSensorServiceStub plantStub =
                PlantSensorServiceGrpc.newStub(plantChannel);

        CountDownLatch plantLatch = new CountDownLatch(1);

        StreamObserver<PlantSensorReading> plantRequestObserver = plantStub.sendSensorReadings(
                new StreamObserver<PlantSensorSummary>() {
                    @Override
                    public void onNext(PlantSensorSummary summary) {
                        System.out.printf("\n🌱 Plant Sensor Summary:\n - Readings: %d\n - Avg Moisture: %.2f%%\n - Avg Light: %.2f lumens%n",
                                summary.getTotalReadings(),
                                summary.getAverageMoisture(),
                                summary.getAverageLight());
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.err.println("❌ Plant sensor error: " + t.getMessage());
                        plantLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("✅ Plant sensor data streaming complete.");
                        plantLatch.countDown();
                    }
                });

        for (int i = 0; i < 3; i++) {
            PlantSensorReading reading = PlantSensorReading.newBuilder()
                    .setMoisture(40 + Math.random() * 20)
                    .setLight(300 + Math.random() * 200)
                    .build();

            System.out.printf("🌿 Sending reading %d: Moisture=%.2f%%, Light=%.2f lumens%n", i + 1, reading.getMoisture(), reading.getLight());
            plantRequestObserver.onNext(reading);
            Thread.sleep(500);
        }

        plantRequestObserver.onCompleted();
        plantLatch.await(3, TimeUnit.SECONDS);

        // 4. Bidirectional Streaming RPC - SecurityService
        SecurityServiceGrpc.SecurityServiceStub securityStub =
                SecurityServiceGrpc.newStub(securityChannel);

        CountDownLatch securityLatch = new CountDownLatch(1);

        StreamObserver<DoorEvent> securityRequest = securityStub.monitorDoor(
                new StreamObserver<DoorAlert>() {
                    @Override
                    public void onNext(DoorAlert alert) {
                        System.out.println("🚨 ALERT from Server [" + alert.getDoorId() + "]: " + alert.getAlertMessage());
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.err.println("❌ Security client error: " + t.getMessage());
                        securityLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("✅ Security client done sending.");
                        securityLatch.countDown();
                    }
                });

        for (int i = 1; i <= 3; i++) {
            DoorEvent event = DoorEvent.newBuilder()
                    .setDoorId("FrontDoor")
                    .setIsOpen(i % 2 == 1)
                    .setTimestamp(LocalTime.now().toString())
                    .build();

            System.out.printf("🚪 Sending door event %d: %s%n", i, event.getIsOpen() ? "OPEN" : "CLOSED");
            securityRequest.onNext(event);
            Thread.sleep(500);
        }

        securityRequest.onCompleted();
        securityLatch.await(3, TimeUnit.SECONDS);

        // Shutdown all channels
        climateChannel.shutdown();
        energyChannel.shutdown();
        plantChannel.shutdown();
        securityChannel.shutdown();

        System.out.println("\n🏁 SmartHome client session finished.");
    }
}
