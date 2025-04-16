package com.mycompany.smarthome;

import com.smarthome.environment.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class EnergyRoutineClient {
    public static void main(String[] args) {
        // Connect to server
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        // Create stub
        EnergyRoutineServiceGrpc.EnergyRoutineServiceBlockingStub stub =
                EnergyRoutineServiceGrpc.newBlockingStub(channel);

        // Build request
        EnergyUsageRequest request = EnergyUsageRequest.newBuilder()
                .setDate("2025-04-13")
                .build();

        // Call service and handle streamed responses
        System.out.println("📡 Streaming hourly energy usage data...");
        stub.streamHourlyEnergyUsage(request).forEachRemaining(data -> {
            System.out.printf("🔋 Hour %02d: %.2f kWh\n", data.getHour(), data.getUsageKwh());
        });

        channel.shutdown();
    }
}
