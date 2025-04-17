package com.mycompany.smarthome;

import com.smarthome.environment.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;

public class EnergyRoutineClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 50052;

        // 🔍 Discover the energy service using JmDNS
        try {
            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
            ServiceInfo serviceInfo = jmdns.getServiceInfo("_energy._tcp.local.", "EnergyRoutineService", 3000);
            if (serviceInfo != null) {
                host = serviceInfo.getHostAddresses()[0];
                port = serviceInfo.getPort();
                System.out.println("🔍 EnergyRoutineService discovered at: " + host + ":" + port);
            } else {
                System.err.println("❌ EnergyRoutineService not found via JmDNS.");
            }
        } catch (IOException e) {
            System.err.println("❌ JmDNS discovery failed: " + e.getMessage());
        }

        // 1. Connect to the discovered host and port
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();

        // 2. Set up API key in metadata
        Metadata headers = new Metadata();
        Metadata.Key<String> API_KEY_HEADER = Metadata.Key.of("api_key", Metadata.ASCII_STRING_MARSHALLER);
        headers.put(API_KEY_HEADER, "my-secret-key");

        // 3. Attach metadata to stub
        EnergyRoutineServiceGrpc.EnergyRoutineServiceBlockingStub stub =
                MetadataUtils.attachHeaders(EnergyRoutineServiceGrpc.newBlockingStub(channel), headers);

        // 4. Build and send request
        EnergyUsageRequest request = EnergyUsageRequest.newBuilder()
                .setDate("2025-04-17")
                .build();

        System.out.println("🔋 Energy usage by hour:");
        Iterator<EnergyUsageData> usageData = stub.streamHourlyEnergyUsage(request);
        usageData.forEachRemaining(data ->
                System.out.printf(" - Hour %02d: %.2f kWh%n", data.getHour(), data.getUsageKwh())
        );

        // 5. Shutdown
        channel.shutdown();
    }
}
