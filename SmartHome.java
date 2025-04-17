package com.mycompany.smarthome;

import com.smarthome.environment.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

import java.time.LocalTime;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SmartHome {

    private static final String API_KEY = "my-secret-key";
    private static final Metadata.Key<String> API_KEY_HEADER =
            Metadata.Key.of("api_key", Metadata.ASCII_STRING_MARSHALLER);

    public static void main(String[] args) throws Exception {
        Logger.getLogger(SmartHome.class.getName()).info("SmartHome client starting...");

        // ----------- ClimateControlService (Unary) -----------
        ManagedChannel climateChannel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        Metadata climateHeaders = new Metadata();
        climateHeaders.put(API_KEY_HEADER, API_KEY);
        ClimateControlServiceGrpc.ClimateControlServiceBlockingStub climateStub =
                MetadataUtils.attachHeaders(
                        ClimateControlServiceGrpc.newBlockingStub(climateChannel),
                        climateHeaders
                );

        TemperatureRequest tempRequest = TemperatureRequest.newBuilder()
                .setRoom("Living Room")
                .setTemperature(22.5)
                .build();

        TemperatureResponse tempResponse = climateStub.setTemperature(tempRequest);
        System.out.println("ClimateControlService Response: " + tempResponse.getMessage());

        climateChannel.shutdown();

        // ----------- EnergyRoutineService (Server Streaming) -----------
        ManagedChannel energyChannel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        Metadata energyHeaders = new Metadata();
        energyHeaders.put(API_KEY_HEADER, API_KEY);
        EnergyRoutineServiceGrpc.EnergyRoutineServiceBlockingStub energyStub =
                MetadataUtils.attachHeaders(
                        EnergyRoutineServiceGrpc.newBlockingStub(energyChannel),
                        energyHeaders
                );

        EnergyUsageRequest energyRequest = EnergyUsageRequest.newBuilder()
                .setDate("2025-04-16")
                .build();

        System.out.println("Streaming hourly energy usage:");
        Iterator<EnergyUsageData> usageData = energyStub.streamHourlyEnergyUsage(energyRequest);
        usageData.forEachRemaining(data ->
                System.out.printf(" - Hour %02d: %.2f kWh%n", data.getHour(), data.getUsageKwh())
        );

        energyChannel.shutdown();

        // ----------- PlantSensorService (Client Streaming) -----------
        ManagedChannel plantChannel = ManagedChannelBuilder.forAddress("localhost", 50053)
                .usePlaintext()
                .build();

        Metadata plantHeaders = new Metadata();
        plantHeaders.put(API_KEY_HEADER, API_KEY);
        PlantSensorServiceGrpc.PlantSensorServiceStub plantStub =
                MetadataUtils.attachHeaders(
                        PlantSensorServiceGrpc.newStub(plantChannel),
                        plantHeaders
                );

        CountDownLatch plantLatch = new CountDownLatch(1);

        StreamObserver<PlantSensorReading> plantRequestObserver = plantStub.sendSensorReadings(
                new StreamObserver<PlantSensorSummary>() {
                    @Override
                    public void onNext(PlantSensorSummary summary) {
                        System.out.println("Plant Sensor Summary:");
                        System.out.println(" - Readings: " + summary.getTotalReadings());
                        System.out.printf(" - Avg Moisture: %.2f%%\n", summary.getAverageMoisture());
                        System.out.printf(" - Avg Light: %.2f lumens\n", summary.getAverageLight());
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.err.println("Plant sensor error: " + t.getMessage());
                        plantLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Plant sensor data streaming complete.");
                        plantLatch.countDown();
                    }
                });

        for (int i = 0; i < 3; i++) {
            PlantSensorReading reading = PlantSensorReading.newBuilder()
                    .setMoisture(40 + Math.random() * 20)
                    .setLight(300 + Math.random() * 200)
                    .build();

            System.out.printf("Sending plant reading %d: Moisture=%.2f%%, Light=%.2f lumens%n",
                    i + 1, reading.getMoisture(), reading.getLight());
            plantRequestObserver.onNext(reading);
            Thread.sleep(400);
        }

        plantRequestObserver.onCompleted();
        plantLatch.await(5, TimeUnit.SECONDS);
        plantChannel.shutdown();

        // ----------- SecurityService (Bidirectional Streaming) -----------
        ManagedChannel securityChannel = ManagedChannelBuilder.forAddress("localhost", 51999)
                .usePlaintext()
                .build();

        Metadata securityHeaders = new Metadata();
        securityHeaders.put(API_KEY_HEADER, API_KEY);
        SecurityServiceGrpc.SecurityServiceStub securityStub =
                MetadataUtils.attachHeaders(
                        SecurityServiceGrpc.newStub(securityChannel),
                        securityHeaders
                );

        CountDownLatch securityLatch = new CountDownLatch(1);

        StreamObserver<DoorEvent> securityRequestObserver =
                securityStub.monitorDoor(new StreamObserver<DoorAlert>() {
                    @Override
                    public void onNext(DoorAlert alert) {
                        System.out.printf("ALERT from Server [%s]: %s%n",
                                alert.getDoorId(), alert.getAlertMessage());
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.err.println("Security client error: " + t.getMessage());
                        securityLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Security client done sending.");
                        securityLatch.countDown();
                    }
                });

        for (int i = 1; i <= 3; i++) {
            DoorEvent event = DoorEvent.newBuilder()
                    .setDoorId("FrontDoor")
                    .setIsOpen(i % 2 == 1)
                    .setTimestamp(LocalTime.now().toString())
                    .build();

            System.out.printf("Sending door event %d: %s%n", i, event.getIsOpen() ? "OPEN" : "CLOSED");
            securityRequestObserver.onNext(event);
            Thread.sleep(400);
        }

        securityRequestObserver.onCompleted();
        securityLatch.await(5, TimeUnit.SECONDS);
        securityChannel.shutdown();

        System.out.println("SmartHome client session complete.");
    }
}
