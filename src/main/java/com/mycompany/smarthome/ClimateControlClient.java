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

public class ClimateControlClient {
    public static void main(String[] args) {
        ManagedChannel channel = null;

        try {
            // Create gRPC channel
            channel = ManagedChannelBuilder
                    .forAddress("localhost", 50051)
                    .usePlaintext()
                    .build();

            // Attach API key using gRPC metadata
            Metadata headers = new Metadata();
            Metadata.Key<String> API_KEY_HEADER = Metadata.Key.of("api_key", Metadata.ASCII_STRING_MARSHALLER);
            headers.put(API_KEY_HEADER, "my-secret-key");

            ClimateControlServiceGrpc.ClimateControlServiceBlockingStub stub =
                    MetadataUtils.attachHeaders(ClimateControlServiceGrpc.newBlockingStub(channel), headers);

            // Build request
            TemperatureRequest request = TemperatureRequest.newBuilder()
                    .setRoom("Living Room")
                    .setTemperature(22.5)
                    .build();

            // Send request and print response
            TemperatureResponse response = stub.setTemperature(request);
            System.out.println("🌡️ Climate Response: " + response.getMessage());

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        } finally {
            if (channel != null) channel.shutdown();
        }
    }
}
